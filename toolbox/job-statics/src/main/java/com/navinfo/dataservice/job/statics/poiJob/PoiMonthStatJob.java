package com.navinfo.dataservice.job.statics.poiJob;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceRtException;
import com.navinfo.navicommons.exception.ThreadExecuteException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Point;

import oracle.sql.STRUCT;

/** 
 * @ClassName: PoiMonthStatJob
 * @author songdongyan
 * @date 2017年5月25日
 * @Description: PoiMonthStatJob.java
 */
public class PoiMonthStatJob extends AbstractStatJob {
	protected VMThreadPoolExecutor threadPoolExecutor;
	/**
	 * @param jobInfo
	 */
	public PoiMonthStatJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.job.statics.AbstractStatJob#stat()
	 */
	public String stat() throws JobException {
		try {
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			List<Region> regionList = manApi.queryRegionList();
			Set<Integer> dbIds = new HashSet<Integer>();
			for (Region region : regionList) {
				if(region.getMonthlyDbId() != null){
					dbIds.add(region.getMonthlyDbId());
				}
			}
			
			Map<Integer, Map<String,List<Map<String, Integer>>>> stats = new ConcurrentHashMap<Integer,Map<String,List<Map<String,Integer>>>>();
			long t = System.currentTimeMillis();
			int dbSize = dbIds.size();
			if(dbSize==1){
				new PoiMonthStatThread(null,dbIds.iterator().next(),stats).run();
			}else{
				if(dbSize>10){
					initThreadPool(10);
				}else{
					initThreadPool(dbSize);
				}
				final CountDownLatch latch = new CountDownLatch(dbSize);
				threadPoolExecutor.addDoneSignal(latch);
				// 执行转数据
				for(int dbId:dbIds){
					threadPoolExecutor.execute(new PoiMonthStatThread(latch,dbId,stats));
				}
				latch.await();
				if (threadPoolExecutor.getExceptions().size() > 0) {
					throw new Exception(threadPoolExecutor.getExceptions().get(0));
				}
			}
			log.debug("所有poi日库作业数据统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");

			Map<String,List<Map<String,Integer>>> result = new HashMap<String,List<Map<String,Integer>>>();
			result.put("poi_month_statics", new ArrayList<Map<String,Integer>>());

			for(Entry<Integer, Map<String, List<Map<String, Integer>>>> entry:stats.entrySet()){
				result.get("poi_month_statics").addAll(entry.getValue().get("poi_month_statics"));
			}
			return result.toString();
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}
	}
	
	private void initThreadPool(int poolSize)throws Exception{
		log.debug("开始初始化线程池");
        threadPoolExecutor = new VMThreadPoolExecutor(poolSize,
        		poolSize,
				3,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue(),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}
	private void shutDownPoolExecutor(){
		if (threadPoolExecutor != null && !threadPoolExecutor.isShutdown()) {
			log.debug("关闭线程池");
			threadPoolExecutor.shutdownNow();
			try {
				while (!threadPoolExecutor.isTerminated()) {
					log.debug("等待线程结束：线程数为" + threadPoolExecutor.getActiveCount());
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				log.error("关闭线程池失败");
				throw new ServiceRtException("关闭线程池失败", e);
			}
		}
	}
	
	class PoiMonthStatThread implements Runnable{
		CountDownLatch latch = null;
		int dbId=0;
		Map<Integer, Map<String,List<Map<String, Integer>>>> stats;
		PoiMonthStatThread(CountDownLatch latch,int dbId,Map<Integer, Map<String,List<Map<String, Integer>>>> stat){
			this.latch=latch;
			this.dbId=dbId;
			this.stats = stat;
		}
		
		@Override
		public void run() {
			Connection conn=null;
			try{
				conn=DBConnector.getInstance().getConnectionById(dbId);
				QueryRunner run = new QueryRunner();
				
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT S.PID, S.SECOND_WORK_STATUS, P.GEOMETRY, P.MESH_ID  ");
				sb.append("  FROM POI_COLUMN_STATUS S, IX_POI P                       ");
				sb.append(" WHERE S.PID = P.PID                                       ");
				
				String selectSql = sb.toString();

				ResultSetHandler<Map<Integer, Map<String, Integer>>> rsHandler = new ResultSetHandler<Map<Integer, Map<String, Integer>>>() {
					public Map<Integer, Map<String, Integer>> handle(ResultSet rs) throws SQLException {
						Map<Integer,Map<String,Integer>> gridStat = new HashMap<Integer,Map<String,Integer>>();
						while (rs.next()) {
						    int secondWorkStatus = rs.getInt("SECOND_WORK_STATUS");
						    STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
					    	Point geo;
					    	long pid = rs.getLong("PID");
							try {
								geo = (Point)GeoTranslator.struct2Jts(struct);
								int gridId = Integer.parseInt(CompGridUtil.point2Grid(geo.getX(), geo.getY(), rs.getString("MESH_ID")));
								Map<String,Integer> value = new HashMap<String,Integer>();
							    int totalNum = 0 ;
							    int commitNum = 0;
							    value.put("totalNum", 0);
							    value.put("commitNum", 0);
							    if(gridStat.containsKey(gridId)){
							    	value = gridStat.get(gridId);
							    	totalNum = value.get("totalNum");
							    	commitNum = value.get("commitNum");
							    }
							    if(secondWorkStatus==1||secondWorkStatus==2||secondWorkStatus==3){
							    	totalNum++;
							    }
							    if(secondWorkStatus==3){
							    	commitNum++;
							    }
							    value.put("totalNum", totalNum);
							    value.put("commitNum", commitNum);
							    	
							    gridStat.put(gridId,value);
							} catch (Exception e) {
								System.out.println("pid:" + pid);
//								e.printStackTrace();
							}
						}
						return gridStat;
					}	
				};
				log.info("sql:" + selectSql);
				Map<Integer, Map<String, Integer>> result = run.query(conn, selectSql,rsHandler);
				
				List<Map<String,Integer>> poi_month_statics = new ArrayList<Map<String,Integer>>();
				for(Map.Entry<Integer, Map<String,Integer>> entry:result.entrySet()){
					Map<String,Integer> cell = new HashMap<String,Integer>();
					cell.put("gridId", entry.getKey());
					cell.put("totalNum", entry.getValue().get("totalNum"));
					cell.put("commitNum", entry.getValue().get("commitNum"));
					poi_month_statics.add(cell);
				}

				Map<String,List<Map<String,Integer>>> temp = new HashMap<String,List<Map<String,Integer>>>();
				temp.put("poi_month_statics", poi_month_statics);
				stats.put(dbId, temp);

			}catch(Exception e){
				log.error(e.getMessage(),e);
				throw new ThreadExecuteException("dbId("+dbId+")POI日库作业数据统计失败");
			}finally{
				DbUtils.closeQuietly(conn);
				if(latch!=null){
					latch.countDown();
				}
			}
		}
		
	}

}
