package com.navinfo.dataservice.job.statics.poiJob;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
 * @ClassName: PoiDayStatJob
 * @author songdongyan
 * @date 2017年5月24日
 * @Description: PoiDayStatJob.java
 */
public class PoiDayStatJob extends AbstractStatJob {

	protected VMThreadPoolExecutor threadPoolExecutor;

	/**
	 * @param jobInfo
	 */
	public PoiDayStatJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.job.statics.AbstractStatJob#stat()
	 */
	@Override
	public String stat() throws JobException {
		try {
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			List<Region> regionList = manApi.queryRegionList();
			Set<Integer> dbIds = new HashSet<Integer>();
			for (Region region : regionList) {
				if(region.getDailyDbId() != null){
					dbIds.add(region.getDailyDbId());
				}
			}
			
			Map<Integer, Map<String,List<Map<String, Integer>>>> stats = new ConcurrentHashMap<Integer,Map<String,List<Map<String,Integer>>>>();
			long t = System.currentTimeMillis();
			int dbSize = dbIds.size();
			if(dbSize==1){
				new PoiDayStatThread(null,dbIds.iterator().next(),stats).run();
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
					threadPoolExecutor.execute(new PoiDayStatThread(latch,dbId,stats));
				}
				latch.await();
				if (threadPoolExecutor.getExceptions().size() > 0) {
					throw new Exception(threadPoolExecutor.getExceptions().get(0));
				}
			}
			log.debug("所有poi日库作业数据统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			
			Map<String,List<Map<String,Integer>>> result = new HashMap<String,List<Map<String,Integer>>>();
			result.put("poi_subtask_statics", new ArrayList<Map<String,Integer>>());
			result.put("poi_task_statics", new ArrayList<Map<String,Integer>>());
			result.put("poi_notask_statics", new ArrayList<Map<String,Integer>>());

			for(Entry<Integer, Map<String, List<Map<String, Integer>>>> entry:stats.entrySet()){
				result.get("poi_subtask_statics").addAll(entry.getValue().get("subtaskStat"));
				result.get("poi_task_statics").addAll(entry.getValue().get("taskStat"));
				result.get("poi_notask_statics").addAll(entry.getValue().get("notaskStat"));
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
	
	class PoiDayStatThread implements Runnable{
		CountDownLatch latch = null;
		int dbId=0;
		Map<Integer, Map<String,List<Map<String, Integer>>>> stats;
		PoiDayStatThread(CountDownLatch latch,int dbId,Map<Integer, Map<String,List<Map<String, Integer>>>> stat){
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
				sb.append(" SELECT S.PID,                       ");
				sb.append("        S.STATUS,                    ");
				sb.append("        S.IS_UPLOAD,                 ");
				sb.append("        S.QUICK_SUBTASK_ID,          ");
				sb.append("        S.MEDIUM_SUBTASK_ID,         ");
				sb.append("        S.QUICK_TASK_ID,             ");
				sb.append("        S.MEDIUM_TASK_ID,            ");
				sb.append("        P.MESH_ID,                   ");
				sb.append("        P.GEOMETRY                   ");
				sb.append("   FROM POI_EDIT_STATUS S, IX_POI P  ");
				sb.append("  WHERE P.PID = S.PID                ");
				
				String selectSql = sb.toString();

				ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
					public Map<String,Object> handle(ResultSet rs) throws SQLException {
						Map<String,Object> result = new HashMap<String,Object>();
						Map<Integer,Map<String,Integer>> subtaskStat = new HashMap<Integer,Map<String,Integer>>();
						Map<Integer,Map<String,Integer>> taskStat = new HashMap<Integer,Map<String,Integer>>();
						Map<Integer,Integer> notaskStat = new HashMap<Integer,Integer>();
						while (rs.next()) {
						    int subtaskId = rs.getInt("MEDIUM_SUBTASK_ID");
						    int taskId = rs.getInt("MEDIUM_TASK_ID");
						    int quickTaskId = rs.getInt("QUICK_TASK_ID");
						    int status = rs.getInt("STATUS");
						    if(subtaskId!=0){
						    	Map<String,Integer> value = new HashMap<String,Integer>();
						    	int collectUploadNum = 0 ;
						    	int commitNum = 0;
						    	value.put("collectUploadNum", 0);
						    	value.put("commitNum", 0);
						    	if(subtaskStat.containsKey(subtaskId)){
						    		value = subtaskStat.get(subtaskId);
						    		collectUploadNum = value.get("collectUploadNum");
						    		commitNum = value.get("commitNum");
						    	}
						    	if(status==1||status==2||status==3){
						    		collectUploadNum++;
						    	}
						    	if(status==3){
						    		commitNum++;
						    	}
						    	value.put("collectUploadNum", collectUploadNum);
						    	value.put("commitNum", commitNum);
						    	
						    	subtaskStat.put(subtaskId,value);
						    }
						    if(taskId!=0){
						    	Map<String,Integer> value = new HashMap<String,Integer>();
						    	int collectUploadNum = 0 ;
						    	int commitNum = 0;
						    	value.put("collectUploadNum", 0);
						    	value.put("commitNum", 0);
						    	if(taskStat.containsKey(subtaskId)){
						    		value = taskStat.get(subtaskId);
						    		collectUploadNum = value.get("collectUploadNum");
						    		commitNum = value.get("commitNum");
						    	}
						    	if(status==1||status==2||status==3){
						    		collectUploadNum++;
						    	}
						    	if(status==3){
						    		commitNum++;
						    	}
						    	value.put("collectUploadNum", collectUploadNum);
						    	value.put("commitNum", commitNum);
						    	
						    	taskStat.put(taskId,value);
						    }else if(taskId==0&&quickTaskId==0){
						    	STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						    	Point geo;
						    	long pid = rs.getLong("PID");
								try {
									geo = (Point)GeoTranslator.struct2Jts(struct);
									int gridId = Integer.parseInt(CompGridUtil.point2Grid(geo.getX(), geo.getY(), rs.getString("MESH_ID")));
									int totalNum = 0;
									if(notaskStat.containsKey(gridId)){
										totalNum = notaskStat.get(gridId);									
									}
									notaskStat.put(gridId, totalNum+1);
								} catch (Exception e) {
									System.out.println("pid:" + pid);
//									e.printStackTrace();
								}
						    }
						}
						result.put("subtaskStat", subtaskStat);
						result.put("taskStat", taskStat);
						result.put("notaskStat", notaskStat);
						return result;
					}	
				};
				log.info("sql:" + selectSql);
				Map<String,Object> result = run.query(conn, selectSql,rsHandler);
				
				List<Map<String,Integer>> subtaskStat = new ArrayList<Map<String,Integer>>();
				List<Map<String,Integer>> taskStat = new ArrayList<Map<String,Integer>>();
				List<Map<String,Integer>> notaskStat = new ArrayList<Map<String,Integer>>();

				Map<Integer,Map<String,Integer>> subtask = (Map<Integer, Map<String, Integer>>) result.get("subtaskStat");
				for(Map.Entry<Integer, Map<String,Integer>> entry:subtask.entrySet()){
					Map<String,Integer> cell = new HashMap<String,Integer>();
					cell.put("subtaskId", entry.getKey());
					cell.put("collectUploadNum", entry.getValue().get("collectUploadNum"));
					cell.put("commitNum", entry.getValue().get("commitNum"));
					subtaskStat.add(cell);
				}
				
				Map<Integer,Map<String,Integer>> task = (Map<Integer, Map<String, Integer>>) result.get("taskStat");
				for(Map.Entry<Integer, Map<String,Integer>> entry:task.entrySet()){
					Map<String,Integer> cell = new HashMap<String,Integer>();
					cell.put("taskId", entry.getKey());
					cell.put("collectUploadNum", entry.getValue().get("collectUploadNum"));
					cell.put("commitNum", entry.getValue().get("commitNum"));
					taskStat.add(cell);
				}
				
				Map<Integer,Integer> notask = (Map<Integer, Integer>) result.get("notaskStat");
				for(Map.Entry<Integer,Integer> entry:notask.entrySet()){
					Map<String,Integer> cell = new HashMap<String,Integer>();
					cell.put("gridId", entry.getKey());
					cell.put("totalNum", entry.getValue());
					notaskStat.add(cell);
				}
				
				Map<String,List<Map<String,Integer>>> temp = new HashMap<String,List<Map<String,Integer>>>();
				temp.put("subtaskStat", subtaskStat);
				temp.put("taskStat", taskStat);
				temp.put("notaskStat", notaskStat);
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
