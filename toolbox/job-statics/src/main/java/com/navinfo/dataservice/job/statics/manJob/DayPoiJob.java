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
			try{
				//查询并统计所有数据
				Map<String,Object> result = convertAllTaskData(dealers);
				
				List<Map<String, Object>> subtaskStat = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> taskStat = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> notaskStat = new ArrayList<Map<String, Object>>();

				Map<Integer, Map<String, Object>> subtask = (Map<Integer, Map<String, Object>>) result.get("subtaskStat");
				for(Map.Entry<Integer, Map<String, Object>> entry : subtask.entrySet()){
					Map<String, Object> cell = new HashMap<String, Object>();
					cell.put("subtaskId", entry.getKey());
					cell.put("poiUploadNum", entry.getValue().get("poiUploadNum"));
					cell.put("poiFinishNum", entry.getValue().get("poiFinishNum"));
					cell.put("firstEditDate", entry.getValue().get("firstEditDate"));
					cell.put("firstCollectDate", entry.getValue().get("firstCollectDate"));
					cell.put("waitWorkPoi", entry.getValue().get("waitWorkPoi"));
					subtaskStat.add(cell);
				}
				
				Map<Integer,Map<String, Object>> task = (Map<Integer, Map<String, Object>>) result.get("taskStat");
				for(Map.Entry<Integer, Map<String, Object>> entry : task.entrySet()){
					Map<String, Object> cell = new HashMap<String, Object>();
					cell.put("taskId", entry.getKey());
					cell.put("poiUploadNum", entry.getValue().get("poiUploadNum"));
					cell.put("poiFinishNum", entry.getValue().get("poiFinishNum"));
					cell.put("poiUnfinishNum", entry.getValue().get("poiUnfinishNum"));
					cell.put("poiFreshNum", entry.getValue().get("poiFreshNum"));
					cell.put("poiUnFreshNum", entry.getValue().get("poiUnFreshNum"));
					cell.put("poiFinishAndPlanNum", entry.getValue().get("poiFinishAndPlanNum"));
					taskStat.add(cell);
				}
				
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
				log.info("dbId:"+dbId+"subtaskStatMap:" + subtaskStat);
				log.info("dbId:"+dbId+"taskStatMap:" + taskStat);
				log.info("dbId:"+dbId+"notaskStatMap:" + notaskStat);
				
				temp.put("subtaskStat", subtaskStat);
				temp.put("taskStat", taskStat);
				temp.put("notaskStat", notaskStat);
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
		 * 处理任务，子任务，无任务数据
		 * @throws Exception 
		 * 
		 * */
		public Map<String,Object> convertAllTaskData(final Set<String> dealers) throws Exception{
			Connection conn = null;
			try{
				conn = DBConnector.getInstance().getConnectionById(dbId);
				final Map<Integer, String> subTaskDate = queryFirstEditDate(conn);
				
				QueryRunner run = new QueryRunner();
				
				StringBuilder sb = new StringBuilder();
				sb.append(" SELECT S.PID,                         ");
				sb.append("        S.STATUS,                      ");
				sb.append("        S.IS_UPLOAD,                   ");
				sb.append("        S.FRESH_VERIFIED,              ");
				sb.append("        S.QUICK_SUBTASK_ID,            ");
				sb.append("        S.MEDIUM_SUBTASK_ID,           ");
				sb.append("        S.QUICK_TASK_ID,               ");
				sb.append("        S.MEDIUM_TASK_ID,              ");
				sb.append("        P.MESH_ID,                     ");
				sb.append("        P.GEOMETRY,                    ");
				sb.append("        P.COLLECT_TIME,                ");
				sb.append("        P.KIND_CODE,                   ");
				sb.append("        P.CHAIN,                       ");
				sb.append("        D.PID  PLAN_PID                ");
				sb.append("   FROM POI_EDIT_STATUS S, IX_POI P    ");
				sb.append("LEFT JOIN DATA_PLAN D ON D.PID = P.PID ");
				sb.append("   AND D.DATA_TYPE = 1                 ");
				sb.append("   AND D.IS_PLAN_SELECTED = 1          ");
				sb.append("   WHERE P.PID = S.PID  and s.status!=0 ");
				
				String selectSql = sb.toString();

				ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
					public Map<String,Object> handle(ResultSet rs) throws SQLException {
						Map<String,Object> result = new HashMap<String,Object>();
						Map<Integer, Map<String, Object>> subtaskStat = new HashMap<Integer,Map<String,Object>>();
						Map<Integer, Map<String, Integer>> taskStat = new HashMap<Integer,Map<String,Integer>>();
						Map<Integer, Map<String, Integer>> notaskStat = new HashMap<Integer, Map<String, Integer>>();
						while (rs.next()) {
						    int subtaskId = rs.getInt("MEDIUM_SUBTASK_ID");
						    int taskId = rs.getInt("MEDIUM_TASK_ID");
						    int quickTaskId = rs.getInt("QUICK_TASK_ID");
						    int status = rs.getInt("STATUS");
						    int fresh = rs.getInt("FRESH_VERIFIED");
						    int planPid = rs.getInt("PLAN_PID");
						    if(subtaskId != 0){
						    	String collectTime = (rs.getString("COLLECT_TIME") == null) ? "" : rs.getString("COLLECT_TIME");
						    	statisticsSubTaskDataImp(subtaskStat, subtaskId, status, subTaskDate, collectTime);
						    }
						    if(taskId != 0){
						    	//调用处理任务统计方法
						    	statisticsTaskDataImp(taskStat, taskId, 0, status, fresh, planPid);
						    	
						    }
						    if(quickTaskId != 0){
						    	//调用处理任务统计方法
						    	statisticsTaskDataImp(taskStat, 0, quickTaskId, status, fresh, planPid);
						    }
						    if(taskId == 0 && quickTaskId == 0){
						    	STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						    	String kindCode = rs.getString("KIND_CODE")== null ? "" : rs.getString("KIND_CODE");
								String chain = rs.getString("CHAIN") == null ? "" : rs.getString("CHAIN");
						    	statisticsNoTaskDataImp(dealers, notaskStat, kindCode, chain, struct);
						    }
						}
						result.put("subtaskStat", subtaskStat);
						result.put("taskStat", taskStat);
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
			try {
				Point geo;
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
				log.error("处理任务，子任务，无任务数据坐标，无法获取到gridid:" + e.getMessage(), e);
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
