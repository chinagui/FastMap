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
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceRtException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Point;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/** 
 * @ClassName: PoiDayStatJob
 * @author songhe
 * @date 2017年7月31日
 * 
 */
public class DayPoiJob extends AbstractStatJob {

	protected VMThreadPoolExecutor threadPoolExecutor;

	/**
	 * @param jobInfo
	 */
	public DayPoiJob(JobInfo jobInfo) {
		super(jobInfo);
	}

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
			//dbIds = new HashSet<Integer>();
//			dbIds.add(13);
			log.info("dbIds:"+dbIds);
			//查询所有元数据库中的代理店的数据
			Set<String> dealers = queryDealershipFromMeta();
			
			Map<Integer, Map<String,List<Map<String, Object>>>> stats = new ConcurrentHashMap<Integer,Map<String,List<Map<String, Object>>>>();
			long time = System.currentTimeMillis();
			int dbSize = dbIds.size();
			if(dbSize == 1){
				new PoiDayStatThread(null, dbIds.iterator().next(), stats, dealers).run();
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
					threadPoolExecutor.execute(new PoiDayStatThread(latch,dbId,stats,dealers));
				}
				latch.await();
				if (threadPoolExecutor.getExceptions().size() > 0) {
					throw new Exception(threadPoolExecutor.getExceptions().get(0));
				}
			}
			log.debug("所有poi日库作业数据统计完毕。用时："+((System.currentTimeMillis() - time)/1000)+"s.");
			
			Map<String,List<Map<String, Object>>> result = new HashMap<String,List<Map<String, Object>>>();
			result.put("subtask_day_poi", new ArrayList<Map<String, Object>>());
			result.put("task_day_poi", new ArrayList<Map<String, Object>>());
			result.put("grid_day_poi", new ArrayList<Map<String, Object>>());

			for(Entry<Integer, Map<String, List<Map<String, Object>>>> entry : stats.entrySet()){
				result.get("subtask_day_poi").addAll(entry.getValue().get("subtaskStat"));
				result.get("task_day_poi").addAll(entry.getValue().get("taskStat"));
				result.get("grid_day_poi").addAll(entry.getValue().get("notaskStat"));
			}
			
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
	
	class PoiDayStatThread implements Runnable{
		CountDownLatch latch = null;
		int dbId = 0;
		Set<String> dealers = null;
		Map<Integer, Map<String,List<Map<String, Object>>>> stats;
		PoiDayStatThread(CountDownLatch latch,int dbId,Map<Integer, Map<String,List<Map<String, Object>>>> stat, Set<String> dealers){
			this.latch = latch;
			this.dbId = dbId;
			this.stats = stat;
			this.dealers = dealers;
		}
		
		@Override
		public void run() {
			log.info("start dbId:"+dbId);
			Connection conn = null;
			try{
				conn = DBConnector.getInstance().getConnectionById(dbId);				
				
				List<Map<String, Object>> subtaskStat = subtaskStat(conn);
				List<Map<String, Object>> taskStat = taskStat(conn);
				List<Map<String, Object>> notaskStat = new ArrayList<Map<String, Object>>();
				
				//查询并统计所有数据
				Map<String,Object> result = convertAllTaskData(conn,dealers);
				Map<Integer, Map<String, Object>> notask = (Map<Integer, Map<String, Object>>) result.get("notaskStat");
				for(Entry<Integer, Map<String, Object>> entry : notask.entrySet()){
					Map<String, Object> cell = new HashMap<String, Object>();
					cell.put("gridId", entry.getKey());
					cell.put("poiNum", entry.getValue().get("totalNum"));
					cell.put("dealershipNum", entry.getValue().get("dealershipNum"));
					cell.put("noDealershipNum", entry.getValue().get("noDealershipNum"));
					notaskStat.add(cell);
				}
				
				Map<String,List<Map<String, Object>>> temp = new HashMap<String,List<Map<String, Object>>>();
//				log.info("dbId:"+dbId+"subtaskStatMap:" + subtaskStat);
//				log.info("dbId:"+dbId+"taskStatMap:" + taskStat);
//				log.info("dbId:"+dbId+"notaskStatMap:" + notaskStat);
				temp.put("subtaskStat", subtaskStat);
				temp.put("taskStat", taskStat);
				temp.put("notaskStat", notaskStat);
				stats.put(dbId, temp);
				log.info("end dbId:"+dbId);
			}catch(Exception e){
				log.error("dbId("+dbId+")POI日库作业数据统计失败",e);
				DbUtils.commitAndCloseQuietly(conn);
			}finally{
				DbUtils.commitAndCloseQuietly(conn);
				if(latch!=null){
					latch.countDown();
				}
			}
		}
		
		private List<Map<String, Object>> subtaskStat(Connection conn) throws Exception{
			QueryRunner run = new QueryRunner();
			String sql="SELECT S.QUICK_SUBTASK_ID ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.QUICK_SUBTASK_ID!=0"
					+ " GROUP BY S.QUICK_SUBTASK_ID"
					+ " UNION ALL"
					+ " SELECT S.MEDIUM_SUBTASK_ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.MEDIUM_SUBTASK_ID!=0"
					+ " GROUP BY S.MEDIUM_SUBTASK_ID";
			Map<Integer, Long> taskPoiUploadNum=run.query(conn, sql, numRsHandler());
			
			sql="SELECT S.QUICK_SUBTASK_ID ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.STATUS=3 AND S.QUICK_SUBTASK_ID!=0"
					+ " GROUP BY S.QUICK_SUBTASK_ID"
					+ " UNION ALL"
					+ " SELECT S.MEDIUM_SUBTASK_ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.STATUS=3 AND S.MEDIUM_SUBTASK_ID!=0"
					+ " GROUP BY S.MEDIUM_SUBTASK_ID";
			Map<Integer, Long> taskPoiFinishNum=run.query(conn, sql, numRsHandler());
			
			sql="SELECT S.QUICK_SUBTASK_ID ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.STATUS=1 AND S.QUICK_SUBTASK_ID!=0"
					+ " GROUP BY S.QUICK_SUBTASK_ID"
					+ " UNION ALL"
					+ " SELECT S.MEDIUM_SUBTASK_ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.STATUS=1 AND S.MEDIUM_SUBTASK_ID!=0"
					+ " GROUP BY S.MEDIUM_SUBTASK_ID";
			Map<Integer, Long> taskPoiWaitWork=run.query(conn, sql, numRsHandler());
			
			Map<Integer, String> subTaskDate = queryFirstEditDate(conn);
			
			Map<Integer, String> subTaskCollectDate = queryFirstCollectDate(conn);
			
			//查询poi实际新增个数
			sql  = "select a.stk_id ID,count(1) NUM  from log_detail d,log_operation o,log_action a  "
					+ "where  d.op_id = o.op_id and o.act_id = a.act_id   "
					+ "and d.tb_nm = 'IX_POI' AND d.op_tp = 1 GROUP BY a.stk_id  ";
			Map<Integer, Long> poiActualAddNumMap=run.query(conn, sql, numRsHandler());
			
			//查询poi实际删除个数
			sql  = "select s.medium_subtask_id ID  ,count(1) NUM from ix_poi i,poi_edit_status s  "
					+ "where i.pid = s.pid and s.status in (1,2,3) and s.medium_subtask_id  != 0  "
					+ "and i.u_record =2 group by s.medium_subtask_id ";
			Map<Integer, Long> poiActualDeleteNumMap=run.query(conn, sql, numRsHandler());
			
			Set<Integer> subtaskIdSet=new HashSet<>();
			subtaskIdSet.addAll(taskPoiUploadNum.keySet());
			subtaskIdSet.addAll(taskPoiFinishNum.keySet());
			subtaskIdSet.addAll(taskPoiWaitWork.keySet());
			subtaskIdSet.addAll(subTaskDate.keySet());
			subtaskIdSet.addAll(subTaskCollectDate.keySet());
			subtaskIdSet.addAll(poiActualAddNumMap.keySet());
			subtaskIdSet.addAll(poiActualDeleteNumMap.keySet());
			List<Map<String, Object>> subtaskStat = new ArrayList<Map<String, Object>>();
			for (int subtaskId:subtaskIdSet){
				Map<String, Object> subtaskStatOne=new HashMap<>();
				subtaskStatOne.put("subtaskId", subtaskId);
				subtaskStatOne.put("poiUploadNum", 0);
				subtaskStatOne.put("poiFinishNum", 0);
				subtaskStatOne.put("firstEditDate", "");
				subtaskStatOne.put("firstCollectDate", "");
				subtaskStatOne.put("waitWorkPoi", 0);
				long poiUploadNum = 0;
				if(taskPoiUploadNum.containsKey(subtaskId)){
					poiUploadNum = taskPoiUploadNum.get(subtaskId);
					subtaskStatOne.put("poiUploadNum",taskPoiUploadNum.get(subtaskId));
				}
				if(taskPoiFinishNum.containsKey(subtaskId)){
					subtaskStatOne.put("poiFinishNum", taskPoiFinishNum.get(subtaskId));
				}
				if(subTaskDate.containsKey(subtaskId)){
					subtaskStatOne.put("firstEditDate", (StringUtils.isEmpty(subTaskDate.get(subtaskId)))?"" : subTaskDate.get(subtaskId));
				}
				if(subTaskCollectDate.containsKey(subtaskId)){
					subtaskStatOne.put("firstCollectDate", (StringUtils.isEmpty(subTaskCollectDate.get(subtaskId)))?"" : subTaskCollectDate.get(subtaskId));
				}
				if(taskPoiWaitWork.containsKey(subtaskId)){
					subtaskStatOne.put("waitWorkPoi", taskPoiWaitWork.get(subtaskId));
				}
				long poiActualAddNum = 0;				
				if(poiActualAddNumMap.containsKey(subtaskId)){
					poiActualAddNum = poiActualAddNumMap.get(subtaskId);
					
				}
				subtaskStatOne.put("poiActualAddNum", poiActualAddNum);
				
				long poiActualDeleteNum =  0;
				if(poiActualDeleteNumMap.containsKey(subtaskId)){
					poiActualDeleteNum = poiActualDeleteNumMap.get(subtaskId);
				}
				subtaskStatOne.put("poiActualDeleteNum", poiActualDeleteNum);
				
				long poiActualUploadNum = 0;
				if(poiUploadNum >= (poiActualAddNum+poiActualDeleteNum)){
					poiActualUploadNum = poiUploadNum - (poiActualAddNum+poiActualDeleteNum);
				}
				subtaskStatOne.put("poiActualUpdateNum", poiActualUploadNum);
				
				subtaskStat.add(subtaskStatOne);
			}
			return subtaskStat;
		}
		
		private List<Map<String, Object>> taskStat(Connection conn) throws Exception{
			QueryRunner run = new QueryRunner();
			String sql="SELECT S.QUICK_TASK_ID ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.QUICK_TASK_ID!=0"
					+ " GROUP BY S.QUICK_TASK_ID"
					+ " UNION ALL"
					+ " SELECT S.MEDIUM_TASK_ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.MEDIUM_TASK_ID!=0"
					+ " GROUP BY S.MEDIUM_TASK_ID";
			Map<Integer, Long> taskPoiUploadNum=run.query(conn, sql, numRsHandler());
			
			sql="SELECT S.QUICK_TASK_ID ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.STATUS=3 AND S.QUICK_TASK_ID!=0"
					+ " GROUP BY S.QUICK_TASK_ID"
					+ " UNION ALL"
					+ " SELECT S.MEDIUM_TASK_ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.STATUS=3 AND S.MEDIUM_TASK_ID!=0"
					+ " GROUP BY S.MEDIUM_TASK_ID";
			Map<Integer, Long> taskPoiFinishNum=run.query(conn, sql, numRsHandler());
			
			sql="SELECT S.QUICK_TASK_ID ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.STATUS IN (1,2) AND S.QUICK_TASK_ID!=0"
					+ " GROUP BY S.QUICK_TASK_ID"
					+ " UNION ALL"
					+ " SELECT S.MEDIUM_TASK_ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.STATUS IN (1,2) AND S.MEDIUM_TASK_ID!=0"
					+ " GROUP BY S.MEDIUM_TASK_ID";
			Map<Integer, Long> taskPoiUnfinishNum=run.query(conn, sql, numRsHandler());
			
			sql="SELECT S.QUICK_TASK_ID ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.FRESH_VERIFIED=1 AND S.QUICK_TASK_ID!=0"
					+ " GROUP BY S.QUICK_TASK_ID"
					+ " UNION ALL"
					+ " SELECT S.MEDIUM_TASK_ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.FRESH_VERIFIED=1 AND S.MEDIUM_TASK_ID!=0"
					+ " GROUP BY S.MEDIUM_TASK_ID";
			Map<Integer, Long> taskPoiFreshNum=run.query(conn, sql, numRsHandler());
			
			sql="SELECT S.QUICK_TASK_ID ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.FRESH_VERIFIED=0 AND S.STATUS=3 AND S.QUICK_TASK_ID!=0"
					+ " GROUP BY S.QUICK_TASK_ID"
					+ " UNION ALL"
					+ " SELECT S.MEDIUM_TASK_ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S WHERE S.FRESH_VERIFIED=0 AND S.STATUS=3 AND S.MEDIUM_TASK_ID!=0"
					+ " GROUP BY S.MEDIUM_TASK_ID";
			Map<Integer, Long> taskPoiUnFreshNum=run.query(conn, sql, numRsHandler());
			
			sql="SELECT S.MEDIUM_TASK_ID ID, COUNT(1) NUM"
					+ "  FROM POI_EDIT_STATUS S, DATA_PLAN D"
					+ " WHERE D.PID = S.PID"
					+ "   AND D.DATA_TYPE = 1"
					+ "   AND D.IS_PLAN_SELECTED = 1"
					+ "   AND S.STATUS = 3"
					+ "   AND S.MEDIUM_TASK_ID!=0"
					+ " GROUP BY S.MEDIUM_TASK_ID";
			Map<Integer, Long> taskPoiFinishAndPlanNum=run.query(conn, sql, numRsHandler());
			
			Set<Integer> taskIdSet=new HashSet<>();
			taskIdSet.addAll(taskPoiUploadNum.keySet());
			taskIdSet.addAll(taskPoiFinishNum.keySet());
			taskIdSet.addAll(taskPoiUnfinishNum.keySet());
			taskIdSet.addAll(taskPoiFreshNum.keySet());
			taskIdSet.addAll(taskPoiUnFreshNum.keySet());
			taskIdSet.addAll(taskPoiFinishAndPlanNum.keySet());
			List<Map<String, Object>> taskStat = new ArrayList<Map<String, Object>>();
			for (int taskId:taskIdSet){
				Map<String, Object> taskStatOne=new HashMap<>();
				taskStatOne.put("taskId", taskId);
				taskStatOne.put("poiUploadNum", 0);
				taskStatOne.put("poiFinishNum", 0);
				taskStatOne.put("poiUnfinishNum", 0);
				taskStatOne.put("poiFreshNum", 0);
				taskStatOne.put("poiUnFreshNum", 0);
				taskStatOne.put("poiFinishAndPlanNum", 0);
				if(taskPoiUploadNum.containsKey(taskId)){
					taskStatOne.put("poiUploadNum", taskPoiUploadNum.get(taskId));
				}
				if(taskPoiFinishNum.containsKey(taskId)){
					taskStatOne.put("poiFinishNum", taskPoiFinishNum.get(taskId));
				}
				if(taskPoiUnfinishNum.containsKey(taskId)){
					taskStatOne.put("poiUnfinishNum", taskPoiUnfinishNum.get(taskId));
				}
				if(taskPoiFreshNum.containsKey(taskId)){
					taskStatOne.put("poiFreshNum", taskPoiFreshNum.get(taskId));
				}
				if(taskPoiUnFreshNum.containsKey(taskId)){
					taskStatOne.put("poiUnFreshNum", taskPoiUnFreshNum.get(taskId));
				}
				if(taskPoiFinishAndPlanNum.containsKey(taskId)){
					taskStatOne.put("poiFinishAndPlanNum", taskPoiFinishAndPlanNum.get(taskId));
				}
				taskStat.add(taskStatOne);
			}
			return taskStat;
		}
		
		/**
		 * Map<Integer, Long>
		 * @return
		 */
		private ResultSetHandler<Map<Integer, Long>> numRsHandler(){
			ResultSetHandler<Map<Integer, Long>> rsHandler = new ResultSetHandler<Map<Integer, Long>>() {
				public Map<Integer, Long> handle(ResultSet rs) throws SQLException {
					Map<Integer, Long> result=new HashMap<>();
					while (rs.next()) {
						int subtaskId = rs.getInt("ID");
						long num=rs.getLong("NUM");
						result.put(subtaskId, num);
					}
					return result;
				}	
			};
		return rsHandler;
		}
		
		
		/**
		 * 处理统计任务的数据
		 * 原则:POI实际作业量,中线快线条件都为status == 3
		 *     采集上传POI个数直接根据任务ID获取
		 *     未粗编POI个数STATUS=1，2
		 *     实际鲜度验证个数FRESH_VERIFIED=1(仅中线)
		 *     实际产出量STATUS=3且FRESH_VERIFIED=0(仅中线)
		 *  @param Map<Integer,Map<String,Integer>>
		 *  @param int
		 *  @param int
		 *  @param int
		 *  @param int
		 * 
		 * */
		public void statisticsTaskDataImp(Map<Integer,Map<String,Integer>> taskStat, int taskId, int quickTaskId, int status, int fresh, int planPid){
			Map<String,Integer> value = new HashMap<String,Integer>();
	    	int poiUploadNum = 0 ;
	    	int poiFinishNum = 0;
	    	int poiUnfinishNum = 0;
	    	int poiFreshNum = 0;
	    	int poiUnFreshNum = 0;
	    	int poiFinishAndPlanNum = 0;
	    	
	    	if(taskId != 0 && taskStat.containsKey(taskId)){
	    		value = taskStat.get(taskId);
	    		poiUploadNum = value.get("poiUploadNum");
	    		poiFinishNum = value.get("poiFinishNum");
	    		poiUnfinishNum =  value.get("poiUnfinishNum");
	    		poiFreshNum = value.get("poiFreshNum");
	    		poiUnFreshNum = value.get("poiUnFreshNum");
	    		poiFinishAndPlanNum = value.get("poiFinishAndPlanNum");
	    	}
	    	if(quickTaskId != 0 && taskStat.containsKey(quickTaskId)){
	    		value = taskStat.get(quickTaskId);
	    		poiUploadNum = value.get("poiUploadNum");
	    		poiFinishNum = value.get("poiFinishNum");
	    		poiUnfinishNum =  value.get("poiUnfinishNum");
	    	}
	    	poiUploadNum++;
	    	if(status == 3){
	    		poiFinishNum++;
	    	}
	    	if(status == 1 || status == 2){
	    		poiUnfinishNum++;
	    	}
	    	if(taskId != 0 && fresh == 1){
	    		poiFreshNum++;
	    	}
	    	if(taskId != 0 && status == 3 && fresh == 0){
	    		poiUnFreshNum++;
	    	}
	    	if(taskId != 0 && status == 3 && planPid != 0){
	    		poiFinishAndPlanNum++;
	    	}
	    	
	    	value.put("poiUploadNum", poiUploadNum);
	    	value.put("poiFinishNum", poiFinishNum);
	    	value.put("poiUnfinishNum", poiUnfinishNum);
	    	value.put("poiFreshNum", poiFreshNum);
	    	value.put("poiUnFreshNum", poiUnFreshNum);
	    	value.put("poiFinishAndPlanNum", poiFinishAndPlanNum);
	    	
	    	//中线任务,快线任务必有一个不为0
	    	if(taskId != 0){
	    		taskStat.put(taskId, value);
	    	}else{
	    		taskStat.put(quickTaskId, value);
	    	}
		}
		
		/**
		 * 子任务统计
		 * 原则:采集上传POI个数【MS-C-2】-day：（中线POI采集子任务）POI_EDIT_STATUS表STATUS=1，2，3
		 *     POI提交个数【MS-C-3】-day：（中线POI采集子任务）POI_EDIT_STATUS表STATUS=3
		 *     每个子任务（log_action.stk_Id）对应的第一条履历的时间
		 * 
		 * */
		public void statisticsSubTaskDataImp(Map<Integer, Map<String, Object>> subtaskStat, int subtaskId, int status, Map<Integer, String> subTaskDate, String collectTime){

	    	Map<String, Object> value = new HashMap<String, Object>();
	    	int poiUploadNum = 0;
	    	int poiFinishNum = 0;
	    	int waitWorkPoi = 0;
	    	String firstEditDate = "";
	    	if(subTaskDate.containsKey(subtaskId)){
	    		firstEditDate = subTaskDate.get(subtaskId);
	    	}
	    	
	    	if(subtaskStat.containsKey(subtaskId)){
	    		value = subtaskStat.get(subtaskId);
	    		poiUploadNum = Integer.parseInt(value.get("poiUploadNum").toString());
	    		poiFinishNum = Integer.parseInt(value.get("poiFinishNum").toString());
	    		waitWorkPoi = Integer.parseInt(value.get("waitWorkPoi").toString());
	    		if(value.containsKey("firstCollectDate") && StringUtils.isNotBlank(value.get("firstCollectDate").toString())){
	    			if(StringUtils.isBlank(collectTime)){
	    				collectTime = value.get("firstCollectDate").toString();
	    			}else{
	    				collectTime = (value.get("firstCollectDate").toString().compareTo(collectTime) > 0)?collectTime : value.get("firstCollectDate").toString();
	    			}
	    		}
	    	}
	    	if(status == 1 || status == 2 || status == 3){
	    		poiUploadNum++;
	    	}
	    	if(status == 3){
	    		poiFinishNum++;
	    	}
	    	if(status == 2){
	    		waitWorkPoi++;
	    	}
	    	value.put("poiUploadNum", poiUploadNum);
	    	value.put("poiFinishNum", poiFinishNum);
	    	value.put("firstEditDate", firstEditDate);
	    	value.put("firstCollectDate", collectTime);
	    	value.put("waitWorkPoi", waitWorkPoi);
	    	
	    	subtaskStat.put(subtaskId, value);
	    
		}
		
		/**
		 * 查询所有子任务第一条对应履历的时间
		 * @param Connection
		 * @throws Exception
		 * 
		 * */
		public Map<Integer, String> queryFirstEditDate(Connection conn) throws Exception{
			try{
				QueryRunner run = new QueryRunner();
				String sql = "SELECT A.STK_ID,TO_CHAR(MIN(T.OP_DT),'YYYYMMDDHH24MISS') AS FIRSTTIME FROM LOG_OPERATION T, LOG_ACTION A WHERE A.ACT_ID = T.ACT_ID GROUP BY A.STK_ID";
				ResultSetHandler<Map<Integer, String>> rsHandler = new ResultSetHandler<Map<Integer, String>>() {
					public Map<Integer, String> handle(ResultSet rs) throws SQLException {
						Map<Integer, String> map = new HashMap<Integer, String>();
						while(rs.next()){
							map.put(rs.getInt("STK_ID"), rs.getString("FIRSTTIME"));
						}
						return map;
					}
				};
				return run.query(conn, sql, rsHandler);
			}catch(Exception e){
				log.error(e.getMessage(), e);
				throw e;
			}
		}
		
		/**
		 * 查询所有子任务第一条采集时间
		 * @param Connection
		 * @throws Exception
		 * 
		 * */
		public Map<Integer, String> queryFirstCollectDate(Connection conn) throws Exception{
			try{
				QueryRunner run = new QueryRunner();
				String sql = "SELECT S.QUICK_SUBTASK_ID STK_ID,"
						+ "       TO_CHAR(MIN(TO_DATE(P.COLLECT_TIME, 'YYYYMMDDHH24MISS')),"
						+ "               'YYYYMMDDHH24MISS') AS FIRSTTIME"
						+ "  FROM POI_EDIT_STATUS S, IX_POI P"
						+ " WHERE S.PID = P.PID"
						+ "   AND S.QUICK_SUBTASK_ID != 0"
						+ " GROUP BY S.QUICK_SUBTASK_ID"
						+ " UNION ALL"
						+ " SELECT S.MEDIUM_SUBTASK_ID STK_ID,"
						+ "       TO_CHAR(MIN(TO_DATE(P.COLLECT_TIME, 'YYYYMMDDHH24MISS')),"
						+ "               'YYYYMMDDHH24MISS') AS FIRSTTIME"
						+ "  FROM POI_EDIT_STATUS S, IX_POI P"
						+ " WHERE S.PID = P.PID"
						+ "   AND S.MEDIUM_SUBTASK_ID != 0"
						+ " GROUP BY S.MEDIUM_SUBTASK_ID";
				ResultSetHandler<Map<Integer, String>> rsHandler = new ResultSetHandler<Map<Integer, String>>() {
					public Map<Integer, String> handle(ResultSet rs) throws SQLException {
						Map<Integer, String> map = new HashMap<Integer, String>();
						while(rs.next()){
							map.put(rs.getInt("STK_ID"), rs.getString("FIRSTTIME"));
						}
						return map;
					}
				};
				return run.query(conn, sql, rsHandler);
			}catch(Exception e){
				log.error(e.getMessage(), e);
				throw e;
			}
		}
		
		/**
		 * 处理任务，子任务，无任务数据
		 * @throws Exception 
		 * 
		 * */
		public Map<String,Object> convertAllTaskData(Connection conn,final Set<String> dealers) throws Exception{
			try{				
				QueryRunner run = new QueryRunner();
				
				StringBuilder sb = new StringBuilder();
				sb.append(" SELECT P.GEOMETRY,                    ");
				sb.append("        P.KIND_CODE,                   ");
				sb.append("        P.CHAIN                      ");
				sb.append("   FROM POI_EDIT_STATUS S, IX_POI P    ");
				sb.append("   WHERE P.PID = S.PID  and s.status!=0 and S.MEDIUM_TASK_ID = 0 and s.quick_task_id=0 ");
				
				String selectSql = sb.toString();

				ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
					public Map<String,Object> handle(ResultSet rs) throws SQLException {
						Map<String,Object> result = new HashMap<String,Object>();
						Map<Integer, Map<String, Integer>> notaskStat = new HashMap<Integer, Map<String, Integer>>();
						while (rs.next()) {
					    	STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
					    	String kindCode = rs.getString("KIND_CODE")== null ? "" : rs.getString("KIND_CODE");
							String chain = rs.getString("CHAIN") == null ? "" : rs.getString("CHAIN");
					    	statisticsNoTaskDataImp(dealers, notaskStat, kindCode, chain, struct);					    	
						}
						result.put("notaskStat", notaskStat);
						return result;
					}	
				};
				log.info("sql:" + selectSql);
				return run.query(conn, selectSql,rsHandler);
			}catch(Exception e){
				log.error("从大区库查询处理数据异常:" + e.getMessage(), e);
				throw e;
			}finally{
				DbUtils.closeQuietly(conn);
			}
		}
		
		/**
		 * 处理无任务数据
		 * @param Connection
		 * @param Map<Integer, Map<String, Integer>>
		 * @param String
		 * @param String
		 * @param STRUCT
		 * @param int
		 * 
		 * */
		public void statisticsNoTaskDataImp(Set<String> dealers, Map<Integer, Map<String, Integer>> notaskStat, String kindCode, String chain, STRUCT struct){
			boolean poiType = false;
			Point geo=null;
			try {				
		    	geo = (Point) GeoTranslator.struct2Jts(struct);
				double x = geo.getX();
				double y = geo.getY();
				String[] grids = CompGridUtil.point2Grids(x,y);
				int gridId = Integer.parseInt(grids[0]);
				int totalNum = 0;
				int dealershipNum = 0;
				int noDealershipNum = 0;
				if(notaskStat.containsKey(gridId)){
					totalNum = notaskStat.get(gridId).get("totalNum");
					dealershipNum = notaskStat.get(gridId).get("dealershipNum");
					noDealershipNum = notaskStat.get(gridId).get("noDealershipNum");
				}
				poiType = wetherDealership(dealers, kindCode, chain);
				if(poiType){
					dealershipNum++;
				}else{
					noDealershipNum++;
				}
				Map<String, Integer> grid = new HashMap<>();
				grid.put("totalNum", totalNum+1);
				grid.put("dealershipNum", dealershipNum);
				grid.put("noDealershipNum", noDealershipNum);
				notaskStat.put(gridId, grid);
			} catch (Exception e) {
				if(geo==null){
					log.error("处理任务，子任务，无任务数据坐标，无法获取到gridid:geo=null," + e.getMessage(), e);
				}else{
					log.error("处理任务，子任务，无任务数据坐标，无法获取到gridid:geo="+geo.toString()+"," + e.getMessage(), e);
				}				
			}
		}
		
		/**
		 * 判断是否是代理店数据
		 * @param Connection
		 * @param String
		 * @param String
		 * @throws Exception
		 * 
		 */
		public boolean wetherDealership(Set<String> dealers, String kindCode, final String chain) {
			try {
				if(dealers.contains(kindCode)){
					return true;
				}
				if(dealers.contains(kindCode+chain)){
					return true;
				}
				return false;
			} catch (Exception e) {
				log.error("从元数据库查询数据异常:" + e.getMessage(), e);
				throw e;
			}
		}
	}
	
	/**
	 * 从元数据库查询所有的代理店数据集合
	 * @return Set<String>
	 * @throws Exception 
	 * 
	 * */
	public Set<String> queryDealershipFromMeta() throws Exception{
		Connection metaConn = null;
		try {
			metaConn = DBConnector.getInstance().getMetaConnection();
			QueryRunner run = new QueryRunner();

			String selectSql = "select t.poi_kind from SC_POINT_SPEC_KINDCODE_NEW t where t.type = 7 and t.category = 1 "
					+ "union all select concat(t.poi_kind, t.chain) from SC_POINT_SPEC_KINDCODE_NEW t "
					+ "where t.type = 7 and (t.category = 3 or t.category = 7)";

			ResultSetHandler<Set<String>> rsHandler = new ResultSetHandler<Set<String>>() {
				public Set<String> handle(ResultSet rs) throws SQLException {
					Set<String> result = new HashSet<>(256);
					while (rs.next()) {
						result.add(rs.getString("poi_kind"));
					}
					return result;
				}
			};
			log.info("sql:" + selectSql);
			return run.query(metaConn, selectSql, rsHandler);
		}catch(Exception e){
			log.error("从元数据库查询数据异常:" + e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.closeQuietly(metaConn);
		}
	}

}
