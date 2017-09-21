package com.navinfo.dataservice.job.statics.manJob;

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
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceRtException;

import net.sf.json.JSONObject;

/** 
 * @ClassName: PoiDayStatJob
 * @author songhe
 * @date 2017年7月31日
 * 
 */
public class PersonDayJob extends AbstractStatJob {

	protected VMThreadPoolExecutor threadPoolExecutor;

	/**
	 * @param jobInfo
	 */
	public PersonDayJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public String stat() throws JobException {
		PersonDayJobRequest statReq = (PersonDayJobRequest)request;
		try {
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			List<Region> regionList = manApi.queryRegionList();
			Set<Integer> dbIds = new HashSet<Integer>();
			for (Region region : regionList) {
				if(region.getDailyDbId() != null){
					dbIds.add(region.getDailyDbId());
				}
			}
			
			String workDay = statReq.getWorkDay();
			Map<Integer, Map<String,List<Map<String, Object>>>> stats = new ConcurrentHashMap<Integer,Map<String,List<Map<String, Object>>>>();
			long time = System.currentTimeMillis();
			int dbSize = dbIds.size();
			if(dbSize == 1){
				new PersonDayThread(null, dbIds.iterator().next(), stats, workDay).run();
			}else{
				if(dbSize > 10){
					initThreadPool(10);
				}else{
					initThreadPool(dbSize);
				}
				final CountDownLatch latch = new CountDownLatch(dbSize);
				threadPoolExecutor.addDoneSignal(latch);
				// 执行转数据
				for(int dbId:dbIds){
					threadPoolExecutor.execute(new PersonDayThread(latch, dbId, stats, workDay));
				}
				latch.await();
				if (threadPoolExecutor.getExceptions().size() > 0) {
					throw new Exception(threadPoolExecutor.getExceptions().get(0));
				}
			}
			log.debug("所有poi日库作业数据统计完毕。用时："+((System.currentTimeMillis() - time)/1000)+"s.");
			
			Map<String,List<Map<String, Object>>> result = new HashMap<String,List<Map<String, Object>>>();
			result.put("person_day", new ArrayList<Map<String, Object>>());
			
			for(Entry<Integer, Map<String, List<Map<String, Object>>>> entry : stats.entrySet()){
				result.get("person_day").addAll(entry.getValue().get("subtaskStat"));
			}
			JSONObject identifyJson=new JSONObject();
			identifyJson.put("timestamp", statReq.getTimestamp());
			identifyJson.put("workDay", statReq.getWorkDay());
			statReq.setIdentifyJson(identifyJson);
			statReq.setIdentify("timestamp:"+statReq.getTimestamp()+",workDay:"+statReq.getWorkDay());
			//log.info("stats:" + JSONObject.fromObject(result).toString());
			return JSONObject.fromObject(result).toString();
			
		}catch(Exception e) {
			log.error(e.getMessage(), e);
			shutDownPoolExecutor();
			throw new JobException(e.getMessage(), e);
		}finally{
			shutDownPoolExecutor();
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
	
	class PersonDayThread implements Runnable{
		CountDownLatch latch = null;
		int dbId = 0;
		String workDay = "";
		Map<Integer, Map<String,List<Map<String, Object>>>> stats;
		PersonDayThread(CountDownLatch latch,int dbId,Map<Integer, Map<String,List<Map<String, Object>>>> stat, String workDay){
			this.latch = latch;
			this.dbId = dbId;
			this.stats = stat;
			this.workDay = workDay;
		}
		
		@Override
		public void run() {
			try{
				//查询并统计所有子任务数据
				Map<String,Object> result = convertAllTaskData(workDay);
				
				List<Map<String, Object>> subtaskStat = new ArrayList<Map<String, Object>>();

				Map<Integer, Map<String, Object>> subtask = (Map<Integer, Map<String, Object>>) result.get("subtaskStat");
				for(Map.Entry<Integer, Map<String, Object>> entry : subtask.entrySet()){
					Map<String, Object> cell = new HashMap<String, Object>();
					cell.put("subtaskId", entry.getKey());
					cell.put("uploadNum", entry.getValue().get("uploadNum"));
					cell.put("freshNum", entry.getValue().get("freshNum"));
					cell.put("finishNum", entry.getValue().get("finishNum"));
					cell.put("deleteCount", entry.getValue().get("deleteCount"));
					cell.put("increaseAndAlterCount", entry.getValue().get("increaseAndAlterCount"));
					cell.put("workDay", workDay);
					subtaskStat.add(cell);
				}
				
				Map<String,List<Map<String, Object>>> temp = new HashMap<String,List<Map<String, Object>>>();
				log.info("dbId:"+dbId+"subtaskStatMap:" + subtaskStat);
				
				temp.put("subtaskStat", subtaskStat);
				stats.put(dbId, temp);
			}catch(Exception e){
				log.error("dbId("+dbId+")POI日库作业数据统计失败");
			}finally{
				if(latch!=null){
					latch.countDown();
				}
			}
		}
		
		/**
		 * 处理快线子任务，中线子任务数据
		 * @throws Exception 
		 * 
		 * */
		public Map<String,Object> convertAllTaskData(String timestamp) throws Exception{
			Connection conn = null;
			try{
				conn = DBConnector.getInstance().getConnectionById(dbId);
				
				QueryRunner run = new QueryRunner();
				
				StringBuilder sb = new StringBuilder();
				sb.append(" select s.status,                      		");
				sb.append("        p.u_record,                      	");
				sb.append("        s.fresh_verified,              		");
				sb.append("        s.quick_subtask_id,            		");
				sb.append("        s.medium_subtask_id,           		");
				sb.append("     substr(p.collect_time,0,8) collect_time ");
				sb.append("   from poi_edit_status s, ix_poi p          ");
				sb.append("   where trunc(substr(p.collect_time,0,8)) = ");
				sb.append("	 '"+timestamp+"'"                            );
				sb.append("   and p.pid = s.pid  and s.status!=0                       ");
				
				String selectSql = sb.toString();

				ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
					public Map<String, Object> handle(ResultSet rs) throws SQLException {
						Map<String, Object> result = new HashMap<String, Object>();
						Map<Integer, Map<String, Integer>> subtaskStat = new HashMap<Integer,Map<String, Integer>>();
						while (rs.next()) {
						    int subtaskId = rs.getInt("medium_subtask_id");
						    int quickSubTaskId = rs.getInt("quick_subtask_id");
						    int status = rs.getInt("status");
						    int fresh = rs.getInt("fresh_verified");
						    int record = rs.getInt("u_record");
						    if(subtaskId != 0){
						    	statisticsSubTaskData(subtaskStat, subtaskId, status, fresh, record);
						    }
						    if(quickSubTaskId != 0){
						    	statisticsSubTaskData(subtaskStat, quickSubTaskId, status, fresh, record);
						    }
						}
						result.put("subtaskStat", subtaskStat);
						return result;
					}	
				};
				log.info("sql:" + selectSql);
				return run.query(conn, selectSql,rsHandler);
			}catch(Exception e){
				log.error("从大区库查询处理数据异常:" + e.getMessage(), e);
				DbUtils.closeQuietly(conn);
				throw e;
			}finally{
				DbUtils.closeQuietly(conn);
			}
		}
		
		/**
		 * 处理子任务的统计量方法
		 * @param Map<Integer,Map<String,Integer>> subtaskStat
		 * @param int subtaskId
		 * @param int status
		 * @param int fresh
		 * @param int record
		 * 
		 * */
		public void statisticsSubTaskData(Map<Integer,Map<String,Integer>> subtaskStat, int subtaskId, int status, int fresh, int record){
			Map<String,Integer> value = new HashMap<String,Integer>();
	    	int uploadNum = 0 ;
	    	int freshNum = 0;
	    	int finishNum = 0;
	    	int deleteCount = 0;
	    	int increaseAndAlterCount = 0;
	    	if(subtaskStat.containsKey(subtaskId)){
	    		value = subtaskStat.get(subtaskId);
	    		uploadNum = value.get("uploadNum");
	    		freshNum = value.get("freshNum");
	    		finishNum =  value.get("finishNum");
	    		deleteCount = value.get("deleteCount");
	    		increaseAndAlterCount = value.get("increaseAndAlterCount");
	    	}
	    	finishNum++;
	    	if(status == 1 || status == 2 || status ==3){
	    		uploadNum++;
	    	}
	    	if(fresh == 1){
	    		freshNum++;
	    	}
	    	if(record == 2){
	    		deleteCount++;
	    	}else{
	    		increaseAndAlterCount++;
	    	}
	    	value.put("uploadNum", uploadNum);
	    	value.put("freshNum", freshNum);
	    	value.put("finishNum", finishNum);
	    	value.put("increaseAndAlterCount", increaseAndAlterCount);
	    	value.put("deleteCount", deleteCount);
	    	subtaskStat.put(subtaskId, value);
		}
		
	}

}
