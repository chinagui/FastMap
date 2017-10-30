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
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
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
			log.info("stats:" + JSONObject.fromObject(result).toString());
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
					
					cell.put("aPoiNum", entry.getValue().get("aPoiNum"));
					cell.put("aPoiFreshNum", entry.getValue().get("aPoiFreshNum"));
					cell.put("aPoiAddNum", entry.getValue().get("aPoiAddNum"));
					cell.put("aPoiDelNum", entry.getValue().get("aPoiDelNum"));
					cell.put("aPoiUpdateNum", entry.getValue().get("aPoiUpdateNum"));
					cell.put("aCommitNum", entry.getValue().get("aCommitNum"));
					cell.put("aCommitFreshNum", entry.getValue().get("aCommitFreshNum"));
					cell.put("aCommitAddNum", entry.getValue().get("aCommitAddNum"));
					cell.put("aCommitDelNum", entry.getValue().get("aCommitDelNum"));
					cell.put("aCommitUpdateNum", entry.getValue().get("aCommitUpdateNum"));
					
					cell.put("b1PoiNum", entry.getValue().get("b1PoiNum"));
					cell.put("b1PoiFreshNum", entry.getValue().get("b1PoiFreshNum"));
					cell.put("b1PoiAddNum", entry.getValue().get("b1PoiAddNum"));
					cell.put("b1PoiDelNum", entry.getValue().get("b1PoiDelNum"));
					cell.put("b1PoiUpdateNum", entry.getValue().get("b1PoiUpdateNum"));
					cell.put("b1CommitNum", entry.getValue().get("b1CommitNum"));
					cell.put("b1CommitFreshNum", entry.getValue().get("b1CommitFreshNum"));
					cell.put("b1CommitAddNum", entry.getValue().get("b1CommitAddNum"));
					cell.put("b1CommitDelNum", entry.getValue().get("b1CommitDelNum"));
					cell.put("b1CommitUpdateNum", entry.getValue().get("b1CommitUpdateNum"));
					
					cell.put("b2PoiNum", entry.getValue().get("b2PoiNum"));
					cell.put("b2PoiFreshNum", entry.getValue().get("b2PoiFreshNum"));
					cell.put("b2PoiAddNum", entry.getValue().get("b2PoiAddNum"));
					cell.put("b2PoiDelNum", entry.getValue().get("b2PoiDelNum"));
					cell.put("b2PoiUpdateNum", entry.getValue().get("b2PoiUpdateNum"));
					cell.put("b2CommitNum", entry.getValue().get("b2CommitNum"));
					cell.put("b2CommitFreshNum", entry.getValue().get("b2CommitFreshNum"));
					cell.put("b2CommitAddNum", entry.getValue().get("b2CommitAddNum"));
					cell.put("b2CommitDelNum", entry.getValue().get("b2CommitDelNum"));
					cell.put("b2CommitUpdateNum", entry.getValue().get("b2CommitUpdateNum"));
					
					cell.put("b3PoiNum", entry.getValue().get("b3PoiNum"));
					cell.put("b3PoiFreshNum", entry.getValue().get("b3PoiFreshNum"));
					cell.put("b3PoiAddNum", entry.getValue().get("b3PoiAddNum"));
					cell.put("b3PoiDelNum", entry.getValue().get("b3PoiDelNum"));
					cell.put("b3PoiUpdateNum", entry.getValue().get("b3PoiUpdateNum"));
					cell.put("b3CommitNum", entry.getValue().get("b3CommitNum"));
					cell.put("b3CommitFreshNum", entry.getValue().get("b3CommitFreshNum"));
					cell.put("b3CommitAddNum", entry.getValue().get("b3CommitAddNum"));
					cell.put("b3CommitDelNum", entry.getValue().get("b3CommitDelNum"));
					cell.put("b3CommitUpdateNum", entry.getValue().get("b3CommitUpdateNum"));
					
					cell.put("b4PoiNum", entry.getValue().get("b4PoiNum"));
					cell.put("b4PoiFreshNum", entry.getValue().get("b4PoiFreshNum"));
					cell.put("b4PoiAddNum", entry.getValue().get("b4PoiAddNum"));
					cell.put("b4PoiDelNum", entry.getValue().get("b4PoiDelNum"));
					cell.put("b4PoiUpdateNum", entry.getValue().get("b4PoiUpdateNum"));
					cell.put("b4CommitNum", entry.getValue().get("b4CommitNum"));
					cell.put("b4CommitFreshNum", entry.getValue().get("b4CommitFreshNum"));
					cell.put("b4CommitAddNum", entry.getValue().get("b4CommitAddNum"));
					cell.put("b4CommitDelNum", entry.getValue().get("b4CommitDelNum"));
					cell.put("b4CommitUpdateNum", entry.getValue().get("b4CommitUpdateNum"));
					
					cell.put("cPoiNum", entry.getValue().get("cPoiNum"));
					cell.put("cPoiFreshNum", entry.getValue().get("cPoiFreshNum"));
					cell.put("cPoiAddNum", entry.getValue().get("cPoiAddNum"));
					cell.put("cPoiDelNum", entry.getValue().get("cPoiDelNum"));
					cell.put("cPoiUpdateNum", entry.getValue().get("cPoiUpdateNum"));
					cell.put("cCommitNum", entry.getValue().get("cCommitNum"));
					cell.put("cCommitFreshNum", entry.getValue().get("cCommitFreshNum"));
					cell.put("cCommitAddNum", entry.getValue().get("cCommitAddNum"));
					cell.put("cCommitDelNum", entry.getValue().get("cCommitDelNum"));
					cell.put("cCommitUpdateNum", entry.getValue().get("cCommitUpdateNum"));
					
					
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
				final Map<Integer, Object> poiAddMap = subtaskStat(conn);
				QueryRunner run = new QueryRunner();
				
				StringBuilder sb = new StringBuilder();
				sb.append(" select s.status,                      		");
				sb.append("        p.u_record, p."+"\""+"LEVEL"+"\",    ");
				sb.append("        s.fresh_verified,              		");
				sb.append("        s.quick_subtask_id,            		");
				sb.append("        s.medium_subtask_id,           		");
				sb.append("     substr(p.collect_time,0,8) collect_time ");
				sb.append("   from poi_edit_status s, ix_poi p          ");
				sb.append("   where trunc(substr(p.collect_time,0,8)) = ");
				sb.append("	 '"+timestamp+"'"                            );
				sb.append("   and p.pid = s.pid  and s.status <> 0      ");
				
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
						    String level = rs.getString("LEVEL");
						    if(subtaskId != 0){
						    	statisticsSubTaskData(subtaskStat, subtaskId, status, fresh, record, level, poiAddMap);
						    }
						    if(quickSubTaskId != 0){
						    	statisticsSubTaskData(subtaskStat, quickSubTaskId, status, fresh, record, level, poiAddMap);
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
		public void statisticsSubTaskData(Map<Integer,Map<String,Integer>> subtaskStat, int subtaskId, int status, int fresh, int record, String level, Map<Integer, Object> poiAddMap){
			Map<String,Integer> value = new HashMap<String,Integer>();
	    	int uploadNum = 0;
	    	int freshNum = 0;
	    	int finishNum = 0;
	    	int deleteCount = 0;
	    	int increaseAndAlterCount = 0;
	    	//modify by songhe 20171030  添加了很多统计项
	    	int aPoiNum = 0;
	    	int aPoiFreshNum = 0;
	    	int aPoiAddNum = 0;
	    	int aPoiDelNum = 0;
	    	int aPoiUpdateNum = 0;
	    	int aCommitNum = 0;
	    	int aCommitFreshNum = 0;
	    	int aCommitAddNum = 0;
	    	int aCommitDelNum = 0;
	    	int aCommitUpdateNum = 0;
	    	
	    	int b1PoiNum = 0;
	    	int b1PoiFreshNum = 0;
	    	int b1PoiAddNum = 0;
	    	int b1PoiDelNum = 0;
	    	int b1PoiUpdateNum = 0;
	    	int b1CommitNum = 0;
	    	int b1CommitFreshNum = 0;
	    	int b1CommitAddNum = 0;
	    	int b1CommitDelNum = 0;
	    	int b1CommitUpdateNum = 0;
	    	
	    	int b2PoiNum = 0;
	    	int b2PoiFreshNum = 0;
	    	int b2PoiAddNum = 0;
	    	int b2PoiDelNum = 0;
	    	int b2PoiUpdateNum = 0;
	    	int b2CommitNum = 0;
	    	int b2CommitFreshNum = 0;
	    	int b2CommitAddNum = 0;
	    	int b2CommitDelNum = 0;
	    	int b2CommitUpdateNum = 0;
	    	
	    	int b3PoiNum = 0;
	    	int b3PoiFreshNum = 0;
	    	int b3PoiAddNum = 0;
	    	int b3PoiDelNum = 0;
	    	int b3PoiUpdateNum = 0;
	    	int b3CommitNum = 0;
	    	int b3CommitFreshNum = 0;
	    	int b3CommitAddNum = 0;
	    	int b3CommitDelNum = 0;
	    	int b3CommitUpdateNum = 0;
	    	
	    	int b4PoiNum = 0;
	    	int b4PoiFreshNum = 0;
	    	int b4PoiAddNum = 0;
	    	int b4PoiDelNum = 0;
	    	int b4PoiUpdateNum = 0;
	    	int b4CommitNum = 0;
	    	int b4CommitFreshNum = 0;
	    	int b4CommitAddNum = 0;
	    	int b4CommitDelNum = 0;
	    	int b4CommitUpdateNum = 0;
	    	
	    	int cPoiNum = 0;
	    	int cPoiFreshNum = 0;
	    	int cPoiAddNum = 0;
	    	int cPoiDelNum = 0;
	    	int cPoiUpdateNum = 0;
	    	int cCommitNum = 0;
	    	int cCommitFreshNum = 0;
	    	int cCommitAddNum = 0;
	    	int cCommitDelNum = 0;
	    	int cCommitUpdateNum = 0;
	    	if(subtaskStat.containsKey(subtaskId)){
	    		value = subtaskStat.get(subtaskId);
	    		uploadNum = value.get("uploadNum");
	    		freshNum = value.get("freshNum");
	    		finishNum =  value.get("finishNum");
	    		deleteCount = value.get("deleteCount");
	    		increaseAndAlterCount = value.get("increaseAndAlterCount");
	    		
		        aPoiNum = value.get("aPoiNum");
		        aPoiFreshNum = value.get("aPoiFreshNum");
		    	aPoiAddNum = value.get("aPoiAddNum");
		    	aPoiDelNum = value.get("aPoiDelNum");
		    	aPoiUpdateNum = value.get("aPoiUpdateNum");
		    	aCommitNum = value.get("aCommitNum");
		    	aCommitFreshNum = value.get("aCommitFreshNum");
		    	aCommitAddNum = value.get("aCommitAddNum");
		    	aCommitDelNum = value.get("aCommitDelNum");
		    	aCommitUpdateNum = value.get("aCommitUpdateNum");
		    	
		    	b1PoiNum = value.get("b1PoiNum");
		    	b1PoiFreshNum = value.get("b1PoiFreshNum");
		    	b1PoiAddNum = value.get("b1PoiAddNum");
		    	b1PoiDelNum = value.get("b1PoiDelNum");
		    	b1PoiUpdateNum = value.get("b1PoiUpdateNum");
		    	b1CommitNum = value.get("b1CommitNum");
		    	b1CommitFreshNum = value.get("b1CommitFreshNum");
		    	b1CommitAddNum = value.get("b1CommitAddNum");
		    	b1CommitDelNum = value.get("b1CommitDelNum");
		    	b1CommitUpdateNum = value.get("b1CommitUpdateNum");
		    	
		    	b2PoiNum = value.get("b2PoiNum");
		    	b2PoiFreshNum = value.get("b2PoiFreshNum");
		    	b2PoiAddNum = value.get("b2PoiAddNum");
		    	b2PoiDelNum = value.get("b2PoiDelNum");
		    	b2PoiUpdateNum = value.get("b2PoiUpdateNum");
		    	b2CommitNum = value.get("b2CommitNum");
		    	b2CommitFreshNum = value.get("b2CommitFreshNum");
		    	b2CommitAddNum = value.get("b2CommitAddNum");
		    	b2CommitDelNum = value.get("b2CommitDelNum");
		    	b2CommitUpdateNum = value.get("b2CommitUpdateNum");
		    	
		    	b3PoiNum = value.get("b3PoiNum");
		    	b3PoiFreshNum = value.get("b3PoiFreshNum");
		    	b3PoiAddNum = value.get("b3PoiAddNum");
		    	b3PoiDelNum = value.get("b3PoiDelNum");
		    	b3PoiUpdateNum = value.get("b3PoiUpdateNum");
		    	b3CommitNum = value.get("b3CommitNum");
		    	b3CommitFreshNum = value.get("b3CommitFreshNum");
		    	b3CommitAddNum = value.get("b3CommitAddNum");
		    	b3CommitDelNum = value.get("b3CommitDelNum");
		    	b3CommitUpdateNum = value.get("b3CommitUpdateNum");
		    	
		    	b4PoiNum = value.get("b4PoiNum");
		    	b4PoiFreshNum = value.get("b4PoiFreshNum");
		    	b4PoiAddNum = value.get("b4PoiAddNum");
		    	b4PoiDelNum = value.get("b4PoiDelNum");
		    	b4PoiUpdateNum = value.get("b4PoiUpdateNum");
		    	b4CommitNum = value.get("b4CommitNum");
		    	b4CommitFreshNum = value.get("b4CommitFreshNum");
		    	b4CommitAddNum = value.get("b4CommitAddNum");
		    	b4CommitDelNum = value.get("b4CommitDelNum");
		    	b4CommitUpdateNum = value.get("b4CommitUpdateNum");
		    	
		    	cPoiNum = value.get("cPoiNum");
		    	cPoiFreshNum = value.get("cPoiFreshNum");
		    	cPoiAddNum = value.get("cPoiAddNum");
		    	cPoiDelNum = value.get("cPoiDelNum");
		    	cPoiUpdateNum = value.get("cPoiUpdateNum");
		    	cCommitNum = value.get("cCommitNum");
		    	cCommitFreshNum = value.get("cCommitFreshNum");
		    	cCommitAddNum = value.get("cCommitAddNum");
		    	cCommitDelNum = value.get("cCommitDelNum");
		    	cCommitUpdateNum = value.get("cCommitUpdateNum");
	    	}
	    	finishNum++;
	    	if(status == 1 || status == 2 || status ==3){
	    		uploadNum++;
	    		if(StringUtils.isNotBlank(level)){
		    		switch(level){
		    		case "A" :
		    			aPoiNum++;
		    			if(fresh == 1){
		    				aPoiFreshNum++;
		    			}
		    			if(record == 2){
		    				aPoiDelNum++;
		    			}
		    			if(status == 3){
		    				aCommitNum++;
			    			if(fresh == 1){
			    				aCommitFreshNum++;
			    			}
			    			if(record == 2){
			    				aCommitDelNum++;
			    			}
		    			}
		    			break;
		    		case "B1" :
		    			b1PoiNum++;
		    			if(fresh == 1){
		    				b1PoiFreshNum++;
		    			}
		    			if(record == 2){
		    				b1PoiDelNum++;
		    			}
		    			if(status == 3){
		    				b1CommitNum++;
			    			if(fresh == 1){
			    				b1CommitFreshNum++;
			    			}
			    			if(record == 2){
			    				b1CommitDelNum++;
			    			}
		    			}
		    			break;
		    		case "B2" :
		    			b2PoiNum++;
		    			if(fresh == 1){
		    				b2PoiFreshNum++;
		    			}
		    			if(record == 2){
		    				b2PoiDelNum++;
		    			}
		    			if(status == 3){
		    				b2CommitNum++;
			    			if(fresh == 1){
			    				b2CommitFreshNum++;
			    			}
			    			if(record == 2){
			    				b2CommitDelNum++;
			    			}
		    			}
		    			break;
		    		case "B3" :
		    			b3PoiNum++;
		    			if(fresh == 1){
		    				b3PoiFreshNum++;
		    			}
		    			if(record == 2){
		    				b3PoiDelNum++;
		    			}
		    			if(status == 3){
		    				b3CommitNum++;
			    			if(fresh == 1){
			    				b3CommitFreshNum++;
			    			}
			    			if(record == 2){
			    				b3CommitDelNum++;
			    			}
		    			}
		    			break;
		    		case "B4" :
		    			b4PoiNum++;
		    			if(fresh == 1){
		    				b4PoiFreshNum++;
		    			}
		    			if(record == 2){
		    				b4PoiDelNum++;
		    			}
		    			if(status == 3){
		    				b4CommitNum++;
			    			if(fresh == 1){
			    				b4CommitFreshNum++;
			    			}
			    			if(record == 2){
			    				b4CommitDelNum++;
			    			}
		    			}
		    			break;
		    		case "C" :
		    			cPoiNum++;
		    			if(fresh == 1){
		    				cPoiFreshNum++;
		    			}
		    			if(record == 2){
		    				cPoiDelNum++;
		    			}
		    			if(status == 3){
		    				cCommitNum++;
			    			if(fresh == 1){
			    				cCommitFreshNum++;
			    			}
			    			if(record == 2){
			    				cCommitDelNum++;
			    			}
		    			}
		    			break;
		    		}
	    		}
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
	    	if(poiAddMap.containsKey(subtaskId)){
	    		Map<String, Integer> mapTmp = (Map<String, Integer>) poiAddMap.get(subtaskId);
	    		aPoiAddNum = mapTmp.get("aPoiAddNum");
	    		b1PoiAddNum = mapTmp.get("b1PoiAddNum");
	    		b2PoiAddNum = mapTmp.get("b2PoiAddNum");
	    		b3PoiAddNum = mapTmp.get("b3PoiAddNum");
	    		b4PoiAddNum = mapTmp.get("b4PoiAddNum");
	    		cPoiAddNum = mapTmp.get("cPoiAddNum");
	    		aCommitAddNum = mapTmp.get("aCommitAddNum");
	    		b1CommitAddNum = mapTmp.get("b1CommitAddNum");
	    		b2CommitAddNum = mapTmp.get("b2CommitAddNum");
	    		b3CommitAddNum = mapTmp.get("b3CommitAddNum");
	    		b4CommitAddNum = mapTmp.get("b4CommitAddNum");
	    		cCommitAddNum = mapTmp.get("cCommitAddNum");
	    	}
	    	
    		value.put("aPoiAddNum", aPoiAddNum);
    		value.put("b1PoiAddNum", b1PoiAddNum);
    		value.put("b2PoiAddNum", b2PoiAddNum);
    		value.put("b3PoiAddNum", b3PoiAddNum);
    		value.put("b4PoiAddNum", b4PoiAddNum);
    		value.put("cPoiAddNum", cPoiAddNum);
    		value.put("aCommitAddNum", aCommitAddNum);
    		value.put("b1CommitAddNum", b1CommitAddNum);
    		value.put("b2CommitAddNum", b2CommitAddNum);
    		value.put("b3CommitAddNum", b3CommitAddNum);
    		value.put("b4CommitAddNum", b4CommitAddNum);
    		value.put("cCommitAddNum", cCommitAddNum);
    		
    		value.put("aPoiNum", aPoiNum);
	        value.put("aPoiFreshNum", aPoiFreshNum);
	    	value.put("aPoiDelNum", aPoiDelNum);
	    	aPoiUpdateNum = aPoiNum - aPoiAddNum - aPoiFreshNum - aPoiDelNum;
	    	value.put("aPoiUpdateNum", aPoiUpdateNum);
	    	value.put("aCommitNum", aCommitNum);
	    	value.put("aCommitFreshNum", aCommitFreshNum);
	    	value.put("aCommitDelNum", aCommitDelNum);
	    	aCommitUpdateNum = aCommitNum - aCommitAddNum - aCommitFreshNum - aCommitDelNum;
	    	value.put("aCommitUpdateNum", aCommitUpdateNum);
	    	
    		value.put("b1PoiNum", b1PoiNum);
	        value.put("b1PoiFreshNum", b1PoiFreshNum);
	    	value.put("b1PoiDelNum", b1PoiDelNum);
	    	b1PoiUpdateNum = b1PoiNum - b1PoiAddNum - b1PoiFreshNum - b1PoiDelNum;
	    	value.put("b1PoiUpdateNum", b1PoiUpdateNum);
	    	value.put("b1CommitNum", b1CommitNum);
	    	value.put("b1CommitFreshNum", b1CommitFreshNum);
	    	value.put("b1CommitDelNum", b1CommitDelNum);
	    	b1CommitUpdateNum = b1CommitNum - b1CommitAddNum - b1CommitFreshNum - b1CommitDelNum;
	    	value.put("b1CommitUpdateNum", b1CommitUpdateNum);
	    	
    		value.put("b2PoiNum", b2PoiNum);
	        value.put("b2PoiFreshNum", b2PoiFreshNum);
	    	value.put("b2PoiDelNum", b2PoiDelNum);
	    	b2PoiUpdateNum = b2PoiNum - b2PoiAddNum - b2PoiFreshNum - b2PoiDelNum;
	    	value.put("b2PoiUpdateNum", b2PoiUpdateNum);
	    	value.put("b2CommitNum", b2CommitNum);
	    	value.put("b2CommitFreshNum", b2CommitFreshNum);
	    	value.put("b2CommitDelNum", b2CommitDelNum);
	    	b2CommitUpdateNum = b2CommitNum - b2CommitAddNum - b2CommitFreshNum - b2CommitDelNum;
	    	value.put("b2CommitUpdateNum", b2CommitUpdateNum);
	    	
    		value.put("b3PoiNum", b3PoiNum);
	        value.put("b3PoiFreshNum", b3PoiFreshNum);
	    	value.put("b3PoiDelNum", b3PoiDelNum);
	    	b3PoiUpdateNum = b3PoiNum - b3PoiAddNum - b3PoiFreshNum - b3PoiDelNum;
	    	value.put("b3PoiUpdateNum", b3PoiUpdateNum);
	    	value.put("b3CommitNum", b3CommitNum);
	    	value.put("b3CommitFreshNum", b3CommitFreshNum);
	    	value.put("b3CommitDelNum", b3CommitDelNum);
	    	b3CommitUpdateNum = b3CommitNum - b3CommitAddNum - b3CommitFreshNum - b3CommitDelNum;
	    	value.put("b3CommitUpdateNum", b3CommitUpdateNum);
	    	
    		value.put("b4PoiNum", b4PoiNum);
	        value.put("b4PoiFreshNum", b4PoiFreshNum);
	    	value.put("b4PoiDelNum", b4PoiDelNum);
	    	b4PoiUpdateNum = b4PoiNum - b4PoiAddNum - b4PoiFreshNum - b4PoiDelNum;
	    	value.put("b4PoiUpdateNum", b4PoiUpdateNum);
	    	value.put("b4CommitNum", b4CommitNum);
	    	value.put("b4CommitFreshNum", b4CommitFreshNum);
	    	value.put("b4CommitDelNum", b4CommitDelNum);
	    	b4CommitUpdateNum = b4CommitNum - b4CommitAddNum - b4CommitFreshNum - b4CommitDelNum;
	    	value.put("b4CommitUpdateNum", b4CommitUpdateNum);
	    	
    		value.put("cPoiNum", cPoiNum);
	        value.put("cPoiFreshNum", cPoiFreshNum);
	    	value.put("cPoiDelNum", cPoiDelNum);
	    	cPoiUpdateNum = cPoiNum - cPoiAddNum - cPoiFreshNum - cPoiDelNum;
	    	value.put("cPoiUpdateNum", cPoiUpdateNum);
	    	value.put("cCommitNum", cCommitNum);
	    	value.put("cCommitFreshNum", cCommitFreshNum);
	    	value.put("cCommitDelNum", cCommitDelNum);
	    	aCommitUpdateNum = cCommitNum - cCommitAddNum - cCommitFreshNum - cCommitDelNum;
	    	value.put("cCommitUpdateNum", cCommitUpdateNum);
	    	
	    	subtaskStat.put(subtaskId, value);
		}
		
	}
	
	private Map<Integer, Object> subtaskStat(Connection conn) throws Exception{
		//查询poi实际新增个数
		QueryRunner run = new QueryRunner();
		String sql  = "select a.stk_id, count(1) NUM, p."+"\""+"LEVEL"+"\""+", s.status "
			+ " from log_detail d, log_operation o, log_action a, ix_poi p, poi_edit_status s"
			+ " where d.op_id = o.op_id and o.act_id = a.act_id  and d.tb_nm = 'IX_POI' and s.pid = p.pid  "
			+ " AND d.op_tp = 1 and d.geo_pid = p.pid GROUP BY a.stk_id, p."+"\""+"LEVEL"+"\", s.status ";
		Map<Integer, Object> poiActualAddNumMap = run.query(conn, sql, numRsHandler());
		return poiActualAddNumMap;
	}
	
	
	/**
	 * Map<Integer, Long>
	 * @return
	 */
	private ResultSetHandler<Map<Integer, Object>> numRsHandler(){
		ResultSetHandler<Map<Integer, Object>> rsHandler = new ResultSetHandler<Map<Integer, Object>>() {
			public Map<Integer, Object> handle(ResultSet rs) throws SQLException {
				Map<Integer, Object> result = new HashMap<>();
				while (rs.next()) {
					int subtaskId = rs.getInt("stk_id");
					if(subtaskId == 0){
						continue;
					}
					int aPoiAddNum = 0;
					int b1PoiAddNum = 0;
					int b2PoiAddNum = 0;
					int b3PoiAddNum = 0;
					int b4PoiAddNum = 0;
					int cPoiAddNum = 0;
					int aCommitAddNum = 0;
					int b1CommitAddNum = 0;
					int b2CommitAddNum = 0;
					int b3CommitAddNum = 0;
					int b4CommitAddNum = 0;
					int cCommitAddNum = 0;
					
					Map<String, Integer> map = new HashMap<>();
					int num = rs.getInt("NUM");
					int status = rs.getInt("status");
					if(status == 3 || status == 2 || status == 1){
						String level = rs.getString("LEVEL");
						if(StringUtils.isNotBlank(level)){
							if(result.containsKey(subtaskId)){
								map = (Map<String, Integer>) result.get(subtaskId);
								aPoiAddNum = map.get("aPoiAddNum");
								b1PoiAddNum = map.get("b1PoiAddNum");
								b2PoiAddNum = map.get("b2PoiAddNum");
								b3PoiAddNum = map.get("b3PoiAddNum");
								b4PoiAddNum = map.get("b4PoiAddNum");
								cPoiAddNum = map.get("cPoiAddNum");
							}
							switch(level){
							case "A" :
								aPoiAddNum += num;
								if(status == 3){
									aCommitAddNum = num;
								}
								break;
							case "B1" :
								b1PoiAddNum += num;
								if(status == 3){
									b1CommitAddNum = num;
								}
								break;
							case "B2" :
								b2PoiAddNum += num;
								if(status == 3){
									b2CommitAddNum = num;
								}
								break;
							case "B3" :
								b3PoiAddNum += num;
								if(status == 3){
									b3CommitAddNum = num;
								}
								break;
							case "B4" :
								b4PoiAddNum += num;
								if(status == 3){
									b4CommitAddNum = num;
								}
								break;
							case "C" :
								cPoiAddNum += num;
								if(status == 3){
									cCommitAddNum = num;
								}
								break;
							}
						}
					}
					map.put("aPoiAddNum", aPoiAddNum);
					map.put("b1PoiAddNum", b1PoiAddNum);
					map.put("b2PoiAddNum", b2PoiAddNum);
					map.put("b3PoiAddNum", b3PoiAddNum);
					map.put("b4PoiAddNum", b4PoiAddNum);
					map.put("cPoiAddNum", cPoiAddNum);
					map.put("aCommitAddNum", aCommitAddNum);
					map.put("b1CommitAddNum", b1CommitAddNum);
					map.put("b2CommitAddNum", b2CommitAddNum);
					map.put("b3CommitAddNum", b3CommitAddNum);
					map.put("b4CommitAddNum", b4CommitAddNum);
					map.put("cCommitAddNum", cCommitAddNum);
					result.put(subtaskId, map);
				}
				return result;
			}	
		};
	return rsHandler;
	}

}
