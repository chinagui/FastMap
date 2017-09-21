package com.navinfo.dataservice.scripts.tmp.Tips;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceRtException;
import com.navinfo.navicommons.exception.ThreadExecuteException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: TipsTaskFirstCollectScript.java
 * @author zjf
 * @date 2017-9-20 下午1:50:13
 * @Description: 脚本程序——脚本说明：获取中线采集子任务的最早采集时间，保存至MAN_TIMELINE
 * 
 */
public class TipsTaskFirstCollectScript {

	private static final Logger log = Logger.getLogger(TipsTaskFirstCollectScript.class);

    protected String[] queryColNames = { "track" };
	protected static int total = 0;
	protected static VMThreadPoolExecutor poolExecutor;

	private Integer subTaskId;
	private String tableName;
	private Set<String> rowkeySet;

	public TipsTaskFirstCollectScript(String tableName, Integer subTaskId,
									  Set<String> rowkeySet) {
		this.tableName = tableName;
		this.subTaskId = subTaskId;
		this.rowkeySet = rowkeySet;
	}

	public void doSync() throws Exception {
        log.info("*******************************************doSync " + subTaskId);
		Table htab = null;
		try {
			htab = HBaseConnector.getInstance().getConnection()
					.getTable(TableName.valueOf(tableName));
            Map<String, JSONObject> tipMap = HbaseTipsQuery.getHbaseTipsByRowkeys(rowkeySet, queryColNames);
            String firstCollectTime = null;
            for (String rowkey : rowkeySet) {
                if (!tipMap.containsKey(rowkey)) {
                    log.error("hbase not found " + rowkey);
                    continue;
                }

                JSONObject hbaseTips = tipMap.get(rowkey);
                if(hbaseTips.containsKey("track")) {
                    JSONObject trackObj = hbaseTips.getJSONObject("track");
                    JSONArray trackInfo = trackObj.getJSONArray("t_trackInfo");
                    for(int i = 0; i < trackInfo.size(); i ++) {
                        JSONObject subTrackInfo = trackInfo.getJSONObject(i);
                        int stage = subTrackInfo.getInt("stage");
                        if(stage == 1) {
                            if(firstCollectTime == null) {
                                firstCollectTime = subTrackInfo.getString("date");
                            }else{
                                long lastTime = DateUtils.stringToLong(firstCollectTime, "yyyyMMddHHmmss");
                                long thisTime = DateUtils.stringToLong(subTrackInfo.getString("date"), "yyyyMMddHHmmss");
                                if(thisTime < lastTime) {//如果当前Tips时间小于之前记录的采集时间
                                    firstCollectTime = subTrackInfo.getString("date");
                                }
                            }
                            break;
                        }
                    }

                }
            }
            if(firstCollectTime != null) {
                this.saveTimeLine(subTaskId, firstCollectTime);
            }
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if(htab != null) {
                htab.close();
            }
		}
	}

    public void saveTimeLine(Integer subTaskId, String firstCollectTime) {
        log.info("*******************************************saveTimeLine " + subTaskId + "," +firstCollectTime);
        java.sql.Connection conn = null;
        try{
            conn = DBConnector.getInstance().getManConnection();
            String deleteSql = "DELETE FROM MAN_TIMELINE T\n" +
                    " WHERE T.OBJ_ID = " + subTaskId + "\n" +
                    "   AND T.OBJ_TYPE = 'subtask'\n" +
                    "   AND T.OPERATE_TYPE = 1";
            QueryRunner runDelete = new QueryRunner();
            runDelete.update(conn, deleteSql);

            String insertSql = "insert into MAN_TIMELINE t(t.obj_id,t.obj_type,t.operate_type,t.operate_date,t.operate_desc)"
                    + "values(" + subTaskId + ",'subtask',1,TO_DATE('" + firstCollectTime + "','yyyymmddhh24miss'),'')";
            QueryRunner runInsert = new QueryRunner();
            runInsert.update(conn, insertSql);
        }catch (Exception e) {
            log.error(subTaskId + "saveTimeLine error, " + e.getMessage());
            DbUtils.rollbackAndCloseQuietly(conn);
        }finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

	protected static synchronized void setTotal(Integer subTaskId, int num) {
		log.info("finished task:" + subTaskId + ",num:" + num);
		total += num;
		log.info("total:" + total);
	}

	protected static void initPoolExecutor() {
		int poolSize = 10;
		try {
			poolExecutor = new VMThreadPoolExecutor(poolSize, poolSize, 3,
					TimeUnit.SECONDS, new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceRtException("初始化线程池错误:" + e.getMessage(), e);
		}
	}

	protected static void shutDownPoolExecutor() {
		log.debug("关闭线程池");
		if (poolExecutor != null && !poolExecutor.isShutdown()) {
			poolExecutor.shutdownNow();
			try {
				while (!poolExecutor.isTerminated()) {
					log.debug("等待线程结束：线程数为" + poolExecutor.getActiveCount());
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				log.error("关闭线程池失败");
				throw new ServiceRtException("关闭线程池失败", e);
			}
		}
	}

	private static Map<Integer,Set<String>> getMidCollectSubTaskIds() {
        Map<Integer,Set<String>> taskMap = new HashMap<>();

		java.sql.Connection conn = null;
        Statement stmt = null;
        ResultSet resultSet = null;
        try {
			conn = DBConnector.getInstance().getTipsIdxConnection();
            stmt = conn.createStatement();
            String querySql = "SELECT ID, S_MSUBTASKID FROM TIPS_INDEX WHERE S_MSUBTASKID <> 0";
            resultSet = stmt.executeQuery(querySql);
            resultSet.setFetchSize(5000);
            while(resultSet.next()) {
                String rowkey = resultSet.getString("ID");
                Integer subTaskId = resultSet.getInt("S_MSUBTASKID");
                if(taskMap.containsKey(subTaskId)) {
                    Set<String> rowkeySet = taskMap.get(subTaskId);
                    rowkeySet.add(rowkey);
                }else{
                    Set<String> rowkeySet = new HashSet<>();
                    rowkeySet.add(rowkey);
                    taskMap.put(subTaskId, rowkeySet);
                }
            }
		}catch (Exception e) {
            log.error("getMidCollectSubTaskIds erro, " + e.getMessage());
			DbUtils.closeQuietly(conn);
		}finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
		return taskMap;
	}

	public static void sync() throws Exception {

		long t = System.currentTimeMillis();

		final String tableName = HBaseConstant.tipTab;

		final Map<Integer, Set<String>> taskIdMap = getMidCollectSubTaskIds();
		final CountDownLatch latch4Log = new CountDownLatch(taskIdMap.size());
		poolExecutor.addDoneSignal(latch4Log); // log.debug("开始同步");

		for (final Integer subTaskId : taskIdMap.keySet()) {
			log.debug("add sync thread，task id :" + subTaskId);
			poolExecutor.execute(new Runnable() {

				@Override
				public void run() {
					try {
						new TipsTaskFirstCollectScript(tableName, subTaskId,
								taskIdMap.get(subTaskId)).doSync();
						latch4Log.countDown();
						log.debug("thread finish。subTaskId " + subTaskId);
					} catch (Exception e) {
						throw new ThreadExecuteException("thread error。", e);
					}
				}
			});
		}

		try {
			log.debug("wait thread");
			latch4Log.await();
		} catch (InterruptedException e) {
			log.warn("thread Interrupted");
		}
		if (poolExecutor.getExceptions().size() > 0)
			throw new ServiceRtException("thread error", poolExecutor.getExceptions()
					.get(0));
		log.debug("thread finish,用时：" + (System.currentTimeMillis() - t) + "ms");
	}

	public static void initContext(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-app-scripts.xml","dubbo-scripts.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			initContext();
			initPoolExecutor();
			sync();
			log.info("......................all tips create index Over......................");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			shutDownPoolExecutor();
			log.info("......................all tips create index Over......................");
			System.exit(0);
		}

	}

}
