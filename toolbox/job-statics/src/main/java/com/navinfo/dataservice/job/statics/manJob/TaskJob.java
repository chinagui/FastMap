package com.navinfo.dataservice.job.statics.manJob;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.exception.ServiceException;
import net.sf.json.JSONObject;

/**
 * 任务统计
 * @ClassName TaskJob
 * @author Han Shaoming
 * @date 2017年8月4日 下午8:42:39
 * @Description TODO
 */
public class TaskJob extends AbstractStatJob {

	private static final String task = "task";
	private static final String subtask = "subtask";
	private static final String grid_task_tips = "grid_task_tips";
	private static final String grid_notask_tips = "grid_notask_tips";
	private static final String task_day_poi = "task_day_poi";
	private static final String task_grid_tips = "task_grid_tips";
	private static final String fcc = "fcc";
	private static final String grid_month_poi = "grid_month_poi";
	private static final String grid_day_poi = "grid_day_poi";
	private static final String task_day_plan = "task_day_plan";
	private static final String subtask_tips = "subtask_tips";
	private static final String subtask_day_poi = "subtask_day_poi";
	
	private static final String dbName = SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
	
	protected ManApi manApi = null;
	
	public TaskJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String stat() throws JobException {
		try {
			//获取统计时间
			TaskJobRequest statReq = (TaskJobRequest)request;
			log.info("start stat "+statReq.getJobType());
			String timestamp = statReq.getTimestamp();
			//任务统计数据
			List<Map<String, Object>> taskStatList = new ArrayList<Map<String, Object>>();
			//已关闭任务id(不需要统计)
			Set<Integer> taskIdClose = new HashSet<Integer>();
			
			manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			//查询所有的任务
			List<Task> taskAll = manApi.queryTaskAll();
			//查询所有任务的项目类型
			Map<Integer, Integer> programTypes = manApi.queryProgramTypes();
			
			//modify by songhe 2017/9/04
			//查询task对应的tips转aumark数量
			Map<Integer, Integer> tips2MarkMap = manApi.getTips2MarkNumByTaskId();
			//查询mongo库中已统计的数据(状态为关闭)
			Map<Integer, Map<String, Object>> taskStatDataClose = getTaskStatData(timestamp);
			if(taskStatDataClose.size() > 0){
				taskStatList.addAll(taskStatDataClose.values());
				taskIdClose.addAll(taskStatDataClose.keySet());
			}
			//处理从mongo库中获取的统计项
			//处理需要统计的task
			List<Task> taskList = new ArrayList<Task>();
			for (Task task : taskAll) {
				int status = task.getStatus();
				int taskId = task.getTaskId();
				//任务开启
				if(status == 1){
					//查询grids
					Map<Integer, Integer> gridIds = manApi.queryGridIdsByTaskId(taskId);
					task.setGridIds(gridIds);
					//设置项目类型
					int programType = 0;
					if(programTypes.containsKey(taskId)){
						programType = programTypes.get(taskId);
					}
					task.setProgramType(programType);
					taskList.add(task);
				}
				//任务关闭
				if(status == 0){
					if(!taskIdClose.contains(taskId)){
						//查询grids
						Map<Integer, Integer> gridIds = manApi.queryGridIdsByTaskId(taskId);
						task.setGridIds(gridIds);
						//设置项目类型
						int programType = 0;
						if(programTypes.containsKey(taskId)){
							programType = programTypes.get(taskId);
						}
						task.setProgramType(programType);
						taskList.add(task);
					}
				}
			}
			//查询MAN_TIMELINE表获取相应的数据
			String objName = "task";
			Map<Integer, Map<String, Object>> manTimeline = manApi.queryManTimelineByObjName(objName,0);
			//查询mongo中task_grid_tips相应的统计数据
			Map<Integer, Map<String, Object>> taskTipsStatData = getTaskTipsStatData(timestamp);
			//查询mongo中fcc相应的统计数据
			Map<Integer, Map<String, Object>> taskFccStatData = getTaskFccStatData(timestamp);
			//查询mongo中task_day_poi相应的统计数据
			Map<Integer, Map<String, Object>> dayPoiStatData = getDayPoiStatData(timestamp);
			//查询mongo中grid_task_tips相应的统计数据
			Map<Integer, Map<Integer, Map<String, Integer>>> gridTaskTipsStatData = getGridTaskTipsStatData(timestamp);
			//查询mongo中grid_notask_tips相应的统计数据
			Map<Integer, Map<String, Integer>> gridNotaskTipsStatData = getGridNotaskTipsStatData(timestamp);
			//查询mongo中poi月编相应的统计数据
			Map<Integer, Map<String, Integer>> monthPoiStatData = getMonthPoiStatData(timestamp);
			//查询mongo中grid_day_poi相应的统计数据
			Map<Integer, Map<String, Integer>> gridDayPoiStatData = getGridDayPoiStatData(timestamp);
			//查询mongo中task_day_plan相应的统计数据
			Map<Integer, Map<String, Object>> taskDayPlanStatData = getTaskDayPlanStatData(timestamp);
			//查询mongo中subtask_tips相应的统计数据
			Map<Integer, Map<String, Object>> subTipsStatData = getSubTipsStatData(timestamp);
			//查询mongo中subtask_day_poi相应的统计数据
			Map<Integer, Map<String, Object>> subDayPoiStatData = getSubDayPoiStatData(timestamp);
			//查询mongo中子任务的统计数据
			Map<Integer, Map<String, Object>> subtaskStatData = getSubtaskStatData(timestamp);
			
			//统计任务数据
			for(Task task : taskList){
				int taskId = task.getTaskId();
				//查询子任务id
				List<Map<String, Object>> subtaskList = manApi.querySubtaskByTaskId(taskId);
				Set<Integer> collectTasks = manApi.getCollectTaskIdByDayTask(taskId);

				//处理对应任务的tis2aumark数量
				if(tips2MarkMap.containsKey(taskId)){
					task.setTips2MarkNum(tips2MarkMap.get(taskId));
				}else{
					task.setTips2MarkNum(0);
				}
				//获取子任务id
				Set<Integer> subtaskIds = new HashSet<Integer>();
				for (Map<String, Object> map : subtaskList) {
					int subtaskId = (int) map.get("subtaskId");
					subtaskIds.add(subtaskId);
				}
				//处理grid_task_tips相应的统计数据 key:统计描述，value：统计值
				Map<String, Integer> gridTaskTipsStat =new HashMap<>();
				if(task.getType()==1){
					gridTaskTipsStat = handleGridTaskTipsStatData(task, collectTasks,gridTaskTipsStatData);
				}
				//处理grid_notask_tips相应的统计数据
				Map<String, Integer> gridNotaskTipsStat=new HashMap<>();
				if(task.getType()==1){
					gridNotaskTipsStat= handleGridNotaskTipsStatData(task, gridNotaskTipsStatData);
				}
				//处理poi月编相应的统计数据
				Map<String, Integer> MonthPoiStat = handleMonthPoiStatData(task, monthPoiStatData);
				//处理grid_day_poi相应的统计数据
				Map<String, Integer> gridDayPoiStat = handleGridDayPoiStatData(task, gridDayPoiStatData);
				//处理subtask_tips相应的统计数据
				Map<String, Integer> subTipsStat = handleSubTipsStatData(task, subtaskList, subTipsStatData);
				//处理subtask_day_poi相应的统计数据
				Map<String, Integer> subDayPoiStat = handleSubDayPoiStatData(task, subtaskList, subDayPoiStatData);
				//处理子任务相应的统计数据获取实际开始时间
				List<String> subActualStartTimeList = handleSubtaskStatData(task, subtaskStatData, subtaskIds);
				//处理子任务中已关闭的区域粗编子任务个数和所有区域粗编子任务个数
				Map<String, Integer> subtaskAreaData = handleSubtaskArea(task, subtaskList);
				
				//处理mongo库中的查询数据
				Map<String,Object> dataMap = new HashMap<String,Object>();
				if(taskTipsStatData.containsKey(taskId)){
					dataMap.putAll(taskTipsStatData.get(taskId));
				}
				if(dayPoiStatData.containsKey(taskId)){
					dataMap.putAll(dayPoiStatData.get(taskId));
				}
				if(taskDayPlanStatData.containsKey(taskId)){
					dataMap.putAll(taskDayPlanStatData.get(taskId));
				}
				Map<String, Object> fccData = new HashMap<>();
				if(taskFccStatData.containsKey(taskId)){
					fccData=taskFccStatData.get(taskId);
				}
				
				dataMap.putAll(gridTaskTipsStat);
				dataMap.putAll(gridNotaskTipsStat);
				dataMap.putAll(MonthPoiStat);
				dataMap.putAll(gridDayPoiStat);
				dataMap.putAll(subTipsStat);
				dataMap.putAll(subDayPoiStat);
				
				dataMap.putAll(subtaskAreaData);
				//处理实际结束时间
				Map<String, Object> taskManTimeline = null;
				if(manTimeline.containsKey(taskId)){
					taskManTimeline = manTimeline.get(taskId);
				}
				//处理具体数据
				Map<String, Object> taskMap = getTaskStat(task,taskManTimeline,dataMap,subActualStartTimeList,fccData);
				
				taskStatList.add(taskMap);
			}
			//处理数据
			JSONObject result = new JSONObject();
			result.put("task",taskStatList);

			log.info("end stat "+statReq.getJobType());
			long t = System.currentTimeMillis();
			log.debug("所有任务数据统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			
			return result.toString();
			
		} catch (Exception e) {
			log.error("任务统计:"+e.getMessage(), e);
			throw new JobException("任务统计:"+e.getMessage(),e);
		}
	}
	
	
	/**
	 * 查询mongo中上一次的任务统计数据(关闭)
	 * @throws ServiceException 
	 */
	@SuppressWarnings("unchecked")
	public Map<Integer,Map<String,Object>> getTaskStatData(String timestamp) throws Exception{
		try {
			//获取上一次的统计时间
			//String lastTime = DateUtils.addSeconds(timestamp,-60*60);
			MongoDao mongoDao = new MongoDao(dbName);
			//BasicDBObject filter = new BasicDBObject("timestamp", lastTime);
			FindIterable<Document> findIterable = mongoDao.find(task, null).sort(new BasicDBObject("timestamp",-1));
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Object>> stat = new HashMap<Integer,Map<String,Object>>();
			String timestampLast="";
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				String timestampOrigin=String.valueOf(jso.get("timestamp"));
				if(StringUtils.isEmpty(timestampLast)){
					timestampLast=timestampOrigin;
					log.info("最近一次的统计日期为："+timestampLast);
				}
				if(!timestampLast.equals(timestampOrigin)){
					break;
				}
				int taskId = (int) jso.get("taskId");
				int status = (int) jso.get("status");
				if(status == 0){
					Map<String,Object> map = jso;
					stat.put(taskId, map);
				}
			}
			return stat;
		} catch (Exception e) {
			log.error("查询mongo中上一次的任务统计数据报错"+e.getMessage());
			throw new Exception("查询mongo中上一次的任务统计数据报错"+e.getMessage(),e);
		}
	}

	/**
	 * 查询mongo中子任务的统计数据
	 * @throws ServiceException 
	 */
	@SuppressWarnings("unchecked")
	public Map<Integer,Map<String,Object>> getSubtaskStatData(String timestamp) throws Exception{
		try {
			//获取上一次的统计时间
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find(subtask, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Object>> stat = new HashMap<Integer,Map<String,Object>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				int subtaskId = (int) jso.get("subtaskId");
				Map<String,Object> map = jso;
				stat.put(subtaskId, map);
			}
			return stat;
		} catch (Exception e) {
			log.error("查询mongo中子任务的统计数据报错"+e.getMessage());
			throw new Exception("查询mongo中子任务的统计数据报错"+e.getMessage(),e);
		}
	}
	
	
	/**
	 * 处理子任务相应的统计数据获取实际开始时间
	 * @throws ServiceException 
	 */
	public List<String> handleSubtaskStatData(Task task,Map<Integer,Map<String,Object>> subtaskStatData,Set<Integer> subtaskIds) throws Exception{
		try {
			//处理数据
			List<String> startTimeList = new ArrayList<String>();
			for (Integer subtaskId : subtaskIds) {
				if(subtaskStatData.containsKey(subtaskId)){
					Map<String, Object> map = subtaskStatData.get(subtaskId);
					String actualStartDate = (String) map.get("actualStartDate");
					if(StringUtils.isNotEmpty(actualStartDate)){
						startTimeList.add(actualStartDate);
					}
				}
			}
			return startTimeList;
		} catch (Exception e) {
			log.error("处理taskId("+task.getTaskId()+")子任务统计数据报错,"+e.getMessage());
			throw new Exception("处理taskId("+task.getTaskId()+")子任务统计数据报错"+e.getMessage(),e);
		}
	}

	
	/**
	 * 查询mongo中task_grid_tips相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<Integer,Map<String,Object>> getTaskTipsStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find(task_grid_tips, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Object>> stat = new HashMap<Integer,Map<String,Object>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				Map<String,Object> task = new HashMap<String,Object>();
				int taskId = (int) jso.get("taskId");
				double tipsAddLen = 0;
				String tipsAddLenS = (String) jso.get("tipsAddLen");
				if(StringUtils.isNotEmpty(tipsAddLenS)){
					tipsAddLen = Double.parseDouble(tipsAddLenS);
				}
				int tipsUploadNum = (int) jso.get("tipsUploadNum");
				task.put("collectRoadActualTotal", tipsAddLen);
				task.put("collectTipsUploadNum", tipsUploadNum);
				task.put("collectLinkAddTotal", tipsAddLen);
				stat.put(taskId, task);
			}
			return stat;
		} catch (Exception e) {
			log.error("查询mongo中task_grid_tips相应的统计数据报错"+e.getMessage());
			throw new Exception("查询mongo中task_grid_tips相应的统计数据报错"+e.getMessage(),e);
		}
	}
	
	/**
	 * 查询mongo中fcc相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<Integer,Map<String,Object>> getTaskFccStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find(fcc, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Object>> stat = new HashMap<Integer,Map<String,Object>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				Map<String,Object> task = new HashMap<String,Object>();
				int taskId = (int) jso.get("taskId");
				double linkLen = 0;
				String linkLenS = String.valueOf(jso.get("linkLen"));
				if(StringUtils.isNotEmpty(linkLenS)){
					linkLen = Double.parseDouble(linkLenS);
				}
				double link17Len = 0;
				String link17LenS = String.valueOf(jso.get("link17Len"));
				if(StringUtils.isNotEmpty(link17LenS)){
					link17Len = Double.parseDouble(link17LenS);
				}
				task.put("linkLen", linkLen);
				task.put("link17Len", link17Len);
				stat.put(taskId, task);
			}
			return stat;
		} catch (Exception e) {
			log.error("查询mongo中task_grid_tips相应的统计数据报错"+e.getMessage());
			throw new Exception("查询mongo中task_grid_tips相应的统计数据报错"+e.getMessage(),e);
		}
	}
	
	/**
	 * 查询mongo中task_day_poi相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<Integer,Map<String,Object>> getDayPoiStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find(task_day_poi, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Object>> stat = new HashMap<Integer,Map<String,Object>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				Map<String,Object> task = new HashMap<String,Object>();
				int taskId = (int) jso.get("taskId");
				int poiFinishNum = (int) jso.get("poiFinishNum");
				int poiUploadNum = (int) jso.get("poiUploadNum");
				int poiUnfinishNum = (int) jso.get("poiUnfinishNum");
				int poiUnFreshNum = (int) jso.get("poiUnFreshNum");
				int poiFinishAndPlanNum = (int) jso.get("poiFinishAndPlanNum");
				int poiFreshNum = (int) jso.get("poiFreshNum");
				task.put("poiUploadNum", poiUploadNum);
				task.put("poiFinishNum", poiFinishNum);
				task.put("poiUnfinishNum", poiUnfinishNum);
				//task.put("poiActualFinishNum", poiUnFreshNum);
				task.put("poiUnFreshNum", poiUnFreshNum);
				task.put("poiFinishAndPlanNum", poiFinishAndPlanNum);
				task.put("poiFreshNum", poiFreshNum);
				stat.put(taskId, task);
			}
			return stat;
		} catch (Exception e) {
			log.error("查询mongo中task_day_poi相应的统计数据报错"+e.getMessage());
			throw new Exception("查询mongo中task_day_poi相应的统计数据报错"+e.getMessage(),e);
		}
	}
	
	/**
	 * 查询mongo中grid_tips相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<Integer, Map<Integer, Map<String, Integer>>> getGridTaskTipsStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find(grid_task_tips, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			//依次为taskId,gridId,map
			Map<Integer,Map<Integer,Map<String,Integer>>> tipsStat = new HashMap<Integer,Map<Integer,Map<String,Integer>>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				Map<String,Integer> task = new HashMap<String,Integer>();
				int gridId = (int) jso.get("gridId");
				int taskId = (int) jso.get("taskId");
				int taskEditAllNum = (int) jso.get("taskEditAllNum");
				int taskEditFinishNum = (int) jso.get("taskEditFinishNum");
				int taskNoEditAllNum = (int) jso.get("taskNoEditAllNum");
				int taskCreateByEditNum = (int) jso.get("taskCreateByEditNum");
				task.put("dayEditTipsAllNum", taskEditAllNum);
				task.put("dayEditTipsFinishNum", taskEditFinishNum);
				task.put("dayEditTipsNoWorkNum", taskNoEditAllNum);
				task.put("tipsCreateByEditNum", taskCreateByEditNum);
				Map<Integer,Map<String,Integer>> gridStat=new HashMap<>();
				gridStat.put(gridId, task);
				tipsStat.put(taskId, gridStat);
			}
			return tipsStat;
		} catch (Exception e) {
			log.error("查询mongo库中grid_tips统计数据报错"+e.getMessage());
			throw new Exception("查询mongo库中grid_tips统计数据报错"+e.getMessage(),e);
		}
	}
	
	/**
	 * 查询mongo中grid_tips相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<Integer, Map<String, Integer>> getGridNotaskTipsStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find(grid_notask_tips, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			//依次为taskId,gridId,map
			Map<Integer,Map<String,Integer>> tipsStat = new HashMap<Integer,Map<String,Integer>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				Map<String,Integer> task = new HashMap<String,Integer>();
				int gridId = (int) jso.get("gridId");
				int noTaskTotal = (int) jso.get("noTaskTotal");
				task.put("notaskTipsNum", noTaskTotal);
				tipsStat.put(gridId, task);
			}
			return tipsStat;
		} catch (Exception e) {
			log.error("查询mongo库中grid_tips统计数据报错"+e.getMessage());
			throw new Exception("查询mongo库中grid_tips统计数据报错"+e.getMessage(),e);
		}
	}
	

	/**
	 * 处理grid_tips相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<String,Integer> handleGridTaskTipsStatData(Task task,Set<Integer> collectTasks,Map<Integer, Map<Integer, Map<String, Integer>>> gridTaskTipsStatData) throws Exception{
		try {
			//处理任务与grid的关系
			Set<Integer> gridIds = task.getGridIds().keySet();
			int dayEditTipsAllNum = 0;
			int dayEditTipsFinishNum = 0;
			int dayEditTipsUnfinishNum = 0;
			int tipsCreateByEditNum = 0;
			for(Integer ctaskId:collectTasks){
				if(gridTaskTipsStatData.containsKey(ctaskId)){
					Map<Integer, Map<String, Integer>> gridTips=gridTaskTipsStatData.get(ctaskId);
					for (Integer gridId : gridIds) {
						if(gridTaskTipsStatData.containsKey(gridId)){
							Map<String, Integer> map = gridTips.get(gridId);
							dayEditTipsAllNum += map.get("dayEditTipsAllNum");
							dayEditTipsFinishNum += map.get("dayEditTipsFinishNum");
							dayEditTipsUnfinishNum += map.get("dayEditTipsUnfinishNum");
							tipsCreateByEditNum += map.get("tipsCreateByEditNum");
						}
					}
				}
			}
			Map<String,Integer> taskStat = new HashMap<String,Integer>();
			taskStat.put("dayEditTipsAllNum", dayEditTipsAllNum);
			taskStat.put("dayEditTipsFinishNum", dayEditTipsFinishNum);
			taskStat.put("dayEditTipsUnfinishNum", dayEditTipsUnfinishNum);
			taskStat.put("tipsCreateByEditNum", tipsCreateByEditNum);
			return taskStat;
		} catch (Exception e) {
			log.error("处理taskId("+task.getTaskId()+")grid_tips统计数据报错,"+e.getMessage());
			throw new Exception("处理taskId("+task.getTaskId()+")grid_tips统计数据报错"+e.getMessage(),e);
		}
	}
	
	/**
	 * 处理grid_tips相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<String,Integer> handleGridNotaskTipsStatData(Task task,Map<Integer, Map<String, Integer>> gridNotaskTipsStatData) throws Exception{
		try {
			//处理任务与grid的关系
			Set<Integer> gridIds = task.getGridIds().keySet();
			int notaskTipsNum = 0;
			for (Integer gridId : gridIds) {
				if(gridNotaskTipsStatData.containsKey(gridId)){
					Map<String, Integer> map = gridNotaskTipsStatData.get(gridId);
					notaskTipsNum += map.get("notaskTipsNum");
				}
			}
			Map<String,Integer> taskStat = new HashMap<String,Integer>();
			taskStat.put("notaskTipsNum", notaskTipsNum);
			return taskStat;
		} catch (Exception e) {
			log.error("处理taskId("+task.getTaskId()+")grid_tips统计数据报错,"+e.getMessage());
			throw new Exception("处理taskId("+task.getTaskId()+")grid_tips统计数据报错"+e.getMessage(),e);
		}
	}

	/**
	 * 查询mongo中poi月编相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<Integer,Map<String,Integer>> getMonthPoiStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find(grid_month_poi, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Integer>> monthPoiStat = new HashMap<Integer,Map<String,Integer>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				Map<String,Integer> task = new HashMap<String,Integer>();
				int gridId = (int) jso.get("gridId");
				int logAllNum = (int) jso.get("logAllNum");
				int logFinishNum = (int) jso.get("logFinishNum");
				int poiFinishNum = (int) jso.get("poiFinishNum");
				int day2MonthPoiNum = (int) jso.get("day2MonthPoiNum");
				task.put("logAllNum", logAllNum);
				task.put("logFinishNum", logFinishNum);
				task.put("poiFinishNum", poiFinishNum);
				task.put("day2MonthNum", day2MonthPoiNum);
				monthPoiStat.put(gridId, task);
			}
			return monthPoiStat;
		} catch (Exception e) {
			log.error("查询mongo库中grid_month_poi统计数据报错"+e.getMessage());
			throw new Exception("查询mongo库中grid_month_poi统计数据报错"+e.getMessage(),e);
		}
	}
	

	/**
	 * 处理poi月编相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<String,Integer> handleMonthPoiStatData(Task task,Map<Integer,Map<String,Integer>> monthPoiStat) throws Exception{
		try {
			//处理数据
			Set<Integer> gridIds = task.getGridIds().keySet();
			int monthPoiLogTotalNum = 0;
			int monthPoiLogFinishNum = 0;
			int monthPoiFinishNum = 0;
			int day2MonthNum=0;
			for (Integer gridId : gridIds) {
				if(monthPoiStat.containsKey(gridId)){
					Map<String, Integer> map = monthPoiStat.get(gridId);
					monthPoiLogTotalNum += map.get("logAllNum");
					monthPoiLogFinishNum += map.get("logFinishNum");
					monthPoiFinishNum += map.get("poiFinishNum");
					day2MonthNum+=map.get("day2MonthNum");
				}
			}
			Map<String,Integer> taskStat = new HashMap<String,Integer>();
			taskStat.put("monthPoiLogTotalNum", monthPoiLogTotalNum);
			taskStat.put("monthPoiLogFinishNum", monthPoiLogFinishNum);
			taskStat.put("monthPoiFinishNum", monthPoiFinishNum);
			taskStat.put("day2MonthNum", day2MonthNum);
			return taskStat;
		} catch (Exception e) {
			log.error("处理taskId("+task.getTaskId()+")月编poi统计数据报错,"+e.getMessage());
			throw new Exception("处理taskId("+task.getTaskId()+")月编poi统计数据报错"+e.getMessage(),e);
		}
	}

	/**
	 * 查询mongo中grid_day_poi相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<Integer,Map<String,Integer>> getGridDayPoiStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find(grid_day_poi, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Integer>> monthPoiStat = new HashMap<Integer,Map<String,Integer>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				Map<String,Integer> task = new HashMap<String,Integer>();
				int gridId = (int) jso.get("gridId");
				int poiNum = (int) jso.get("poiNum");
				task.put("poiNum", poiNum);
				monthPoiStat.put(gridId, task);
			}
			return monthPoiStat;
		} catch (Exception e) {
			log.error("查询mongo库中grid_day_poi统计数据报错"+e.getMessage());
			throw new Exception("查询mongo库中grid_day_poi统计数据报错"+e.getMessage(),e);
		}
	}
	
	/**
	 * 处理grid_day_poi相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<String,Integer> handleGridDayPoiStatData(Task task,Map<Integer,Map<String,Integer>> gridDayPoiStatData) throws Exception{
		try {
			//处理数据
			Set<Integer> gridIds = task.getGridIds().keySet();
			int notaskPoiNum = 0;
			for (Integer gridId : gridIds) {
				if(gridDayPoiStatData.containsKey(gridId)){
					Map<String, Integer> map = gridDayPoiStatData.get(gridId);
					notaskPoiNum += map.get("poiNum");
				}
			}
			Map<String,Integer> taskStat = new HashMap<String,Integer>();
			taskStat.put("notaskPoiNum", notaskPoiNum);
			return taskStat;
		} catch (Exception e) {
			log.error("处理taskId("+task.getTaskId()+")grid_day_poi统计数据报错,"+e.getMessage());
			throw new Exception("处理taskId("+task.getTaskId()+")grid_day_poi统计数据报错"+e.getMessage(),e);
		}
	}

	/**
	 * 查询mongo中task_day_plan相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<Integer,Map<String,Object>> getTaskDayPlanStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject();
			FindIterable<Document> findIterable = mongoDao.find(task_day_plan, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Object>> stat = new HashMap<Integer,Map<String,Object>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject json = JSONObject.fromObject(iterator.next());
				String taskIdS = (String) json.get("taskId");
				String linkAllLenS = (String) json.get("linkAllLen");
				String link17AllLenS = (String) json.get("link17AllLen");
				String link27AllLenS = (String) json.get("link27AllLen");
				String poiAllNumS = (String) json.get("poiAllNum");

				int taskId = 0;
				double linkAllLen = 0;
				double link17AllLen = 0;
				double link27AllLen = 0;
				int poiAllNum = 0;
				if(StringUtils.isNotEmpty(taskIdS)){
					taskId = Integer.parseInt(taskIdS);
				}
				if(StringUtils.isNotEmpty(linkAllLenS)){
					linkAllLen = Double.parseDouble(linkAllLenS);
				}
				if(StringUtils.isNotEmpty(link17AllLenS)){
					link17AllLen = Double.parseDouble(link17AllLenS);
				}
				if(StringUtils.isNotEmpty(link27AllLenS)){
					link27AllLen = Double.parseDouble(link27AllLenS);
				}
				if(StringUtils.isNotEmpty(poiAllNumS)){
					poiAllNum = Integer.parseInt(poiAllNumS);
				}
				Map<String,Object> task = new HashMap<String,Object>();
				task.put("linkAllLen", linkAllLen);
				task.put("link17AllLen", link17AllLen);
				task.put("link27AllLen", link27AllLen);
				task.put("poiAllNum", poiAllNum);

				stat.put(taskId, task);
			}
			return stat;
		} catch (Exception e) {
			log.error("查询mongo中task_day_plan相应的统计数据报错"+e.getMessage());
			throw new Exception("查询mongo中task_day_plan相应的统计数据报错"+e.getMessage(),e);
		}
	}
	
	/**
	 * 查询mongo中subtask_tips相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<Integer,Map<String,Object>> getSubTipsStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find(subtask_tips, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Object>> stat = new HashMap<Integer,Map<String,Object>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				Map<String,Object> subtask = new HashMap<String,Object>();
				int subtaskId = (int) jso.get("subtaskId");
				int tipsTotal = (int) jso.get("tipsTotal");
				subtask.put("tipsTotal", tipsTotal);
				stat.put(subtaskId, subtask);
			}
			return stat;
		} catch (Exception e) {
			log.error("查询mongo库中subtask_tips统计数据报错"+e.getMessage());
			throw new Exception("查询mongo库中subtask_tips统计数据报错"+e.getMessage(),e);
		}
	}
	
	/**
	 * 处理subtask_tips相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<String,Integer> handleSubTipsStatData(Task task,List<Map<String, Object>> subtasks,Map<Integer,Map<String,Object>> subTipsStatData) throws Exception{
		try {
			//处理数据
			int crowdTipsTotal = 0;
			int inforTipsTotal = 0;
			for (Map<String, Object> subtask : subtasks) {
				int subtaskId = (int) subtask.get("subtaskId");
				int workKind = (int) subtask.get("workKind");
				//众包
				if(workKind == 2){
					if(task.getSubWorkKind(2) == 1){
						if(subTipsStatData.containsKey(subtaskId)){
							Map<String, Object> map = subTipsStatData.get(subtaskId);
							crowdTipsTotal += (int)map.get("tipsTotal");
						}
					}
				}
				//情报矢量
				else if(workKind == 3){
					if(task.getSubWorkKind(3) == 1){
						if(subTipsStatData.containsKey(subtaskId)){
							Map<String, Object> map = subTipsStatData.get(subtaskId);
							inforTipsTotal += (int)map.get("tipsTotal");
						}
					}
				}
			}
			Map<String,Integer> taskStat = new HashMap<String,Integer>();
			taskStat.put("crowdTipsTotal", crowdTipsTotal);
			taskStat.put("inforTipsTotal", inforTipsTotal);
			return taskStat;
		} catch (Exception e) {
			log.error("处理taskId("+task.getTaskId()+")subtask_tips统计数据报错,"+e.getMessage());
			throw new Exception("处理taskId("+task.getTaskId()+")subtask_tips统计数据报错"+e.getMessage(),e);
		}
	}
	
	/**
	 * 查询mongo中subtask_day_poi相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<Integer,Map<String,Object>> getSubDayPoiStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find(subtask_day_poi, filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer,Map<String,Object>> stat = new HashMap<Integer,Map<String,Object>>();
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				Map<String,Object> subtask = new HashMap<String,Object>();
				int subtaskId = (int) jso.get("subtaskId");
				int poiUploadNum = (int) jso.get("poiUploadNum");
				subtask.put("poiUploadNum", poiUploadNum);
				stat.put(subtaskId, subtask);
			}
			return stat;
		} catch (Exception e) {
			log.error("查询mongo库中subtask_day_poi统计数据报错"+e.getMessage());
			throw new Exception("查询mongo库中subtask_day_poi统计数据报错"+e.getMessage(),e);
		}
	}
	
	/**
	 * 处理subtask_day_poi相应的统计数据
	 * @throws ServiceException 
	 */
	public Map<String,Integer> handleSubDayPoiStatData(Task task,List<Map<String, Object>> subtasks,Map<Integer,Map<String,Object>> subDayPoiStatData) throws Exception{
		try {
			//处理数据
			int crowdTipsTotal = 0;
			int multisourcePoiTotal = 0;
			for (Map<String, Object> subtask : subtasks) {
				int subtaskId = (int) subtask.get("subtaskId");
				int workKind = (int) subtask.get("workKind");
				//众包
				if(workKind == 2){
					if(task.getSubWorkKind(2) == 1){
						if(subDayPoiStatData.containsKey(subtaskId)){
							Map<String, Object> map = subDayPoiStatData.get(subtaskId);
							crowdTipsTotal += (int)map.get("poiUploadNum");
						}
					}
				}
				//多源
				else if(workKind == 4){
					if(task.getSubWorkKind(4) == 1){
						if(subDayPoiStatData.containsKey(subtaskId)){
							Map<String, Object> map = subDayPoiStatData.get(subtaskId);
							multisourcePoiTotal += (int)map.get("poiUploadNum");
						}
					}
				}
			}
			Map<String,Integer> taskStat = new HashMap<String,Integer>();
			taskStat.put("crowdTipsTotal", crowdTipsTotal);
			taskStat.put("multisourcePoiTotal", multisourcePoiTotal);
			return taskStat;
		} catch (Exception e) {
			log.error("处理taskId("+task.getTaskId()+")subtask_day_poi统计数据报错,"+e.getMessage());
			throw new Exception("处理taskId("+task.getTaskId()+")subtask_day_poi统计数据报错"+e.getMessage(),e);
		}
	}

	/**
	 * 处理子任务中已关闭的区域粗编子任务个数和所有区域粗编子任务个数
	 * @throws ServiceException 
	 */
	public Map<String,Integer> handleSubtaskArea(Task task,List<Map<String, Object>> subtasks) throws Exception{
		try {
			//处理数据
			int areaAllNum = 0;
			int areaCloseNum = 0;
			for (Map<String, Object> subtask : subtasks) {
				int type = (int) subtask.get("type");
				int status = (int) subtask.get("status");
				//一体化_区域粗编_日编
				if(type == 4){
					if(status == 0){
						areaCloseNum += 1;
					}
					areaAllNum += 1;
				}
			}
			Map<String,Integer> taskStat = new HashMap<String,Integer>();
			taskStat.put("areaAllNum", areaAllNum);
			taskStat.put("areaCloseNum", areaCloseNum);
			return taskStat;
		} catch (Exception e) {
			log.error("处理taskId("+task.getTaskId()+")子任务数数据报错,"+e.getMessage());
			throw new Exception("处理taskId("+task.getTaskId()+")子任务数据报错"+e.getMessage(),e);
		}
	}

	
	/**
	 * 统计数据
	 * @author Han Shaoming
	 * @param task2
	 * @param taskManTimeline
	 * @param dataMap
	 * @param subActualStartTimeList 
	 * @return
	 * @throws Exception 
	 */
	private Map<String, Object> getTaskStat(Task task, Map<String, Object> taskManTimeline,Map<String, Object> dataMap, List<String> subActualStartTimeList,Map<String, Object> fccData) throws Exception {
		int taskId = 0;
		int type = 0;
		int status = 0;
		int planDate = 0;
		
		String actualStartDate = "";
		String actualEndDate = "";
		int diffDate = 0;
		
		float roadPlanTotal = 0;
		int poiPlanTotal = 0;
		int roadPlanIn = 0;
		int roadPlanOut = 0;
		int poiPlanIn = 0;
		int poiPlanOut = 0;
		
		double collectRoadActualTotal = 0;
		int collectTipsUploadNum = 0;
		
		int poiUploadNum = 0;
		int poiFinishNum = 0;
		int poiUnfinishNum = 0;
		//int poiActualFinishNum = 0;
		
		int dayEditTipsAllNum = 0;
		int dayEditTipsNoWorkNum = 0;
		int dayEditTipsFinishNum = 0;
		int tipsCreateByEditNum = 0;
		
		int monthPoiLogTotalNum = 0;
		int monthPoiLogFinishNum = 0;
		int monthPoiFinishNum = 0;
		int day2MonthNum = 0;
		
		double linkAllLen = 0;
		double link17AllLen = 0;
		double link27AllLen = 0;
		double collectLinkUpdateTotal = 0;
		double collectLink17UpdateTotal = 0;
		double collectLinkAddTotal = 0;
		int crowdTipsTotal = 0;
		int inforTipsTotal = 0;
		
		int poiAllNum = 0;
		int poiUnFreshNum = 0;
		int poiFinishAndPlanNum = 0;
		int poiActualAddNum = 0;
		int poiActualUpdateNum = 0;
		int poiActualDeleteNum = 0;
		int poiFreshNum = 0;
		int crowdPoiTotal = 0;
		int multisourcePoiTotal = 0;
		
		int notaskPoiNum = 0;
		int notaskTipsNum = 0;
		int programType=0;
		
		int poiCollectPercent = 0;
		int poiDayPercent = 0;
		int roadPercent = 0;
		int percent = 0;
		int progress = 1;
		int blockId = 0;
		int programId = 0;
		String createDate = "";
		Map<String, Object> taskMap = new HashMap<String, Object>();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			//当前时间
			String systemDate = sdf.format(new Date());		
			//项目Id
			programId = task.getProgramId();
			blockId = task.getBlockId();
			if(task.getCreateDate() != null){
				createDate = sdf.format(task.getCreateDate());
			}
			//任务id
			taskId = task.getTaskId();
			//任务类型
			type = task.getType();
			//任务状态
			status = task.getStatus();
			//项目状态
			programType = task.getProgramType();
			//计划天数
			if(task.getPlanStartDate() != null && task.getPlanEndDate() != null){
				planDate = StatUtil.daysOfTwo(task.getPlanStartDate(), task.getPlanEndDate());
			}
			//实际开始时间
			actualStartDate = startTime(subActualStartTimeList);
			//实际结束时间
			if(task.getStatus() == 0){
				actualEndDate = sdf.format(new Date());
				if(taskManTimeline != null && taskManTimeline.size() > 0){
					actualEndDate = (String) taskManTimeline.get("operateDate");
				}
			}
			//距离计划结束时间天数
			//计划结束时间
			String planEndDate = "" ;
			if(task.getPlanEndDate() != null){
				planEndDate = sdf.format(task.getPlanEndDate());
			}
			if(task.getStatus() == 0){
				//任务为关闭状态：计划结束时间-实际结束时间
				if(StringUtils.isNotEmpty(actualEndDate) && StringUtils.isNotEmpty(planEndDate)){
					diffDate = StatUtil.daysOfTwo(sdf.parse(actualEndDate), sdf.parse(planEndDate));
				}
			}else if(task.getStatus() == 1){
				//任务为非关闭状态：计划结束时间-当前时间
				if(StringUtils.isNotEmpty(systemDate) && StringUtils.isNotEmpty(planEndDate)){
					diffDate = StatUtil.daysOfTwo(sdf.parse(systemDate), sdf.parse(planEndDate));
				}
			}
			//任务road计划量
			roadPlanTotal = task.getRoadPlanTotal();
			//任务poi计划量
			poiPlanTotal = task.getPoiPlanTotal();
			//新增道路计划覆盖度
			roadPlanIn = task.getRoadPlanIn();
			//道路预估产出量
			roadPlanOut = task.getRoadPlanOut();
			//POI计划覆盖度
			poiPlanIn = task.getPoiPlanIn();
			//POI计划产出量
			poiPlanOut = task.getPoiPlanOut();
			
			//统计里程,道路实际作业里程
			if(dataMap.containsKey("collectRoadActualTotal")){
				collectRoadActualTotal = (double) dataMap.get("collectRoadActualTotal");
			}
			/*
			 * 道路实际作业里程【MT-CR-7】
			 * 采集上传现场轨迹匹配的link里程+根据采集任务ID，查找所有新增测线tips，统计里程
			 */
			if(programType == 1){
				if(fccData.containsKey("linkLen")){
					collectRoadActualTotal=collectRoadActualTotal+(double) fccData.get("linkLen");}
			}
			//采集上传个数
			if(dataMap.containsKey("collectTipsUploadNum")){
				collectTipsUploadNum = (int) dataMap.get("collectTipsUploadNum");
			}
			//采集上传POI个数
			if(dataMap.containsKey("poiUploadNum")){
				poiUploadNum = (int) dataMap.get("poiUploadNum");
			}
			//POI提交个数
			if(dataMap.containsKey("poiFinishNum")){
				poiFinishNum = (int) dataMap.get("poiFinishNum");
			}
			//未粗编POI个数
			if(dataMap.containsKey("poiUnfinishNum")){
				poiUnfinishNum = (int) dataMap.get("poiUnfinishNum");
			}
			//POI实际产出量
//			if(dataMap.containsKey("poiActualFinishNum")){
//				poiActualFinishNum = (int) dataMap.get("poiActualFinishNum");
//			}
			//采集上传个数汇总
			if(dataMap.containsKey("dayEditTipsAllNum")){
				dayEditTipsAllNum = (int) dataMap.get("dayEditTipsAllNum");
			}
			//日编不作业tips总量
			if(dataMap.containsKey("dayEditTipsNoWorkNum")){
				dayEditTipsNoWorkNum = (int) dataMap.get("dayEditTipsNoWorkNum");
			}
			//日编tips完成个数
			if(dataMap.containsKey("dayEditTipsFinishNum")){
				dayEditTipsFinishNum = (int) dataMap.get("dayEditTipsFinishNum");
			}
			//内业生成tips个数
			if(dataMap.containsKey("tipsCreateByEditNum")){
				tipsCreateByEditNum = (int) dataMap.get("tipsCreateByEditNum");
			}
			//月编log总量
			if(dataMap.containsKey("monthPoiLogTotalNum")){
				monthPoiLogTotalNum = (int) dataMap.get("monthPoiLogTotalNum");
			}
			//月编log完成个数
			if(dataMap.containsKey("monthPoiLogFinishNum")){
				monthPoiLogFinishNum = (int) dataMap.get("monthPoiLogFinishNum");
			}
			//POI月编完成个数
			if(dataMap.containsKey("monthPoiFinishNum")){
				monthPoiFinishNum = (int) dataMap.get("monthPoiFinishNum");
			}
			//POI日落月数量
			if(dataMap.containsKey("day2MonthNum")){
				day2MonthNum = (int) dataMap.get("day2MonthNum");
			}
			//原库道路里程
			if(dataMap.containsKey("linkAllLen")){
				linkAllLen = (double) dataMap.get("linkAllLen");
			}
			//原库道路里程（1-7级）
			if(dataMap.containsKey("link17AllLen")){
				link17AllLen = (double) dataMap.get("link17AllLen");
			}
			//原库道路里程（2-7级）
			if(dataMap.containsKey("link27AllLen")){
				link27AllLen = (double) dataMap.get("link27AllLen");
			}
			//道路实际更新里程
			if(fccData.containsKey("linkLen")){
				collectLinkUpdateTotal = (double) fccData.get("linkLen");
			}
			//道路实际更新里程（1-7级）
			if(fccData.containsKey("link17Len")){
				collectLink17UpdateTotal = (double) fccData.get("link17Len");
			}
			//新增里程
			if(dataMap.containsKey("collectLinkAddTotal")){
				collectLinkAddTotal = (double) dataMap.get("collectLinkAddTotal");
			}
			//众包tips作业量
			if(dataMap.containsKey("crowdTipsTotal")){
				crowdTipsTotal = (int) dataMap.get("crowdTipsTotal");
			}
			//情报矢量tips作业量
			if(dataMap.containsKey("inforTipsTotal")){
				inforTipsTotal = (int) dataMap.get("inforTipsTotal");
			}
			//原库POI总量
			if(dataMap.containsKey("poiAllNum")){
				poiAllNum = (int) dataMap.get("poiAllNum");
			}
			//POI实际产出量
			if(dataMap.containsKey("poiUnFreshNum")){
				poiUnFreshNum = (int) dataMap.get("poiUnFreshNum");
			}
			//POI规划完成量
			if(dataMap.containsKey("poiFinishAndPlanNum")){
				poiFinishAndPlanNum = (int) dataMap.get("poiFinishAndPlanNum");
			}
			//POI实际新增个数(暂不统计)
			//POI实际修改个数(暂不统计)
			//POI实际删除个数(暂不统计)
			//POI实际鲜度验证个数
			if(dataMap.containsKey("poiFreshNum")){
				poiFreshNum = (int) dataMap.get("poiFreshNum");
			}
			//众包POI作业量
			if(dataMap.containsKey("crowdPoiTotal")){
				crowdPoiTotal = (int) dataMap.get("crowdPoiTotal");
			}
			//多源POI作业量
			if(dataMap.containsKey("multisourcePoiTotal")){
				multisourcePoiTotal = (int) dataMap.get("multisourcePoiTotal");
			}
			//无任务POI数量
			if(dataMap.containsKey("notaskPoiNum")){
				notaskPoiNum = (int) dataMap.get("notaskPoiNum");
			}
			//无任务Tips数量
			if(dataMap.containsKey("notaskTipsNum")){
				notaskTipsNum = (int) dataMap.get("notaskTipsNum");
			}
			//处理完成度
			//中线
			if(programType == 1){
				//采集
				if(task.getType()==0){
					//poi道路完成度
					if(collectRoadActualTotal != 0 && roadPlanTotal == 0){
						roadPercent = 100;
					}else if(roadPlanTotal != 0){
						roadPercent = (int)(collectRoadActualTotal*100/roadPlanTotal);
					}
					//poi采集完成度
					if(poiUploadNum != 0 && poiPlanTotal == 0){
						poiCollectPercent = 100;
					}else if(poiPlanTotal != 0){
						poiCollectPercent = poiUploadNum*100/poiPlanTotal;
					}
					//poi粗编完成度
					if(poiFinishNum != 0 && poiUploadNum == 0){
						poiDayPercent = 100;
					}else if(poiUploadNum != 0){
						poiDayPercent = poiFinishNum*100/poiUploadNum;
					}
					//完成度
					percent = (int) (poiDayPercent*0.17 + poiCollectPercent*0.17 + roadPercent*0.66);
				}
			}
			//快线
			else if(programType == 4){
				//采集任务
				if(task.getType()==0){
					if(task.getStatus() == 0){
						//关闭
						percent = 100;
					}else if(task.getStatus() == 1){
						//开启
						percent = 0;
					}
				}
				//日编任务
				else if(task.getType()==1){
					//grid粗编完成度
					int gridPercent = 0;
					if(dayEditTipsFinishNum != 0 && dayEditTipsAllNum == 0){
						gridPercent = 100;
					}else if(dayEditTipsAllNum != 0){
						gridPercent = dayEditTipsFinishNum*100/dayEditTipsAllNum;
					}
					//区域粗编完成度
					int areaAllNum = 0;
					int areaCloseNum = 0;
					//已关闭的区域粗编子任务个数
					if(dataMap.containsKey("areaCloseNum")){
						areaCloseNum = (int) dataMap.get("areaCloseNum");
					}
					//所有区域粗编子任务个数
					if(dataMap.containsKey("areaAllNum")){
						areaAllNum = (int) dataMap.get("areaAllNum");
					}
					int areaPercent = 0;
					if(areaCloseNum != 0 && areaAllNum == 0){
						gridPercent = 100;
					}else if(areaAllNum != 0){
						areaPercent = areaCloseNum*100/areaAllNum;
					}
					percent = (int) (gridPercent*0.5 + areaPercent*0.5);
				}
			}
			//月编(中线,快线相同)
			if(task.getType()==2){
				if(monthPoiLogFinishNum != 0 && monthPoiLogTotalNum == 0){
					percent = 100;
				}else if(monthPoiLogTotalNum != 0){
					percent = monthPoiLogFinishNum*100/monthPoiLogTotalNum;
				}
			}
			//进度
			if(diffDate < 0){
				progress = 2;
			}else{
				if(planDate == 0){
					if(percent == 100){
						progress = 1;
					}else{
						progress = 2;
					}
				}else{
					int percentSchedule = 100 - diffDate*100/planDate;
					if(percent >= percentSchedule){
						progress = 1;
					}else{
						progress = 2;
					}
				}
			}
			
			//modify by songhe 2017/09/01
			String endTime = "";
			if(0 == task.getStatus()){
				endTime = actualEndDate;
			}else{
				endTime = sdf.format(new Date());
			}
			//生产已执行天数
			int workDate = StatUtil.daysOfTwo(task.getPlanStartDate() == null ? new Date() : task.getPlanStartDate(), sdf.parse(endTime));
			String planStartDate = sdf.format(task.getPlanStartDate() == null ? new Date() : task.getPlanStartDate());
			taskMap.put("planEndDate", planEndDate);
			taskMap.put("planStartDate", planStartDate);
			taskMap.put("workKind", task.getWorkKind() == null ? "" : task.getWorkKind());
			taskMap.put("workDate", workDate);
			taskMap.put("tips2MarkNum", task.getTips2MarkNum());
			
			//保存数据
			taskMap.put("taskId", taskId);
			taskMap.put("type", type);
			taskMap.put("status", status);
			taskMap.put("planDate", planDate);
			taskMap.put("actualStartDate", actualStartDate);
			taskMap.put("actualEndDate", actualEndDate);
			taskMap.put("diffDate", diffDate);
			taskMap.put("roadPlanTotal", roadPlanTotal);
			taskMap.put("poiPlanTotal", poiPlanTotal);
			taskMap.put("roadPlanIn", roadPlanIn);
			taskMap.put("roadPlanOut", roadPlanOut);
			taskMap.put("poiPlanIn", poiPlanIn);
			taskMap.put("poiPlanOut", poiPlanOut);
			taskMap.put("collectRoadActualTotal", collectRoadActualTotal);
			taskMap.put("collectTipsUploadNum", collectTipsUploadNum);
			taskMap.put("poiUploadNum", poiUploadNum);
			taskMap.put("poiFinishNum", poiFinishNum);
			taskMap.put("poiUnfinishNum", poiUnfinishNum);
			//taskMap.put("poiActualFinishNum", poiActualFinishNum);
			taskMap.put("dayEditTipsAllNum", dayEditTipsAllNum);
			taskMap.put("dayEditTipsNoWorkNum", dayEditTipsNoWorkNum);
			taskMap.put("dayEditTipsFinishNum", dayEditTipsFinishNum);
			taskMap.put("tipsCreateByEditNum", tipsCreateByEditNum);
			taskMap.put("monthPoiLogTotalNum", monthPoiLogTotalNum);
			taskMap.put("monthPoiLogFinishNum", monthPoiLogFinishNum);
			taskMap.put("monthPoiFinishNum", monthPoiFinishNum);
			taskMap.put("day2MonthNum", day2MonthNum);
			taskMap.put("linkAllLen", linkAllLen);
			taskMap.put("link17AllLen", link17AllLen);
			taskMap.put("link27AllLen", link27AllLen);
			taskMap.put("collectLinkUpdateTotal", collectLinkUpdateTotal);
			taskMap.put("collectLink17UpdateTotal", collectLink17UpdateTotal);
			taskMap.put("collectLinkAddTotal", collectLinkAddTotal);
			taskMap.put("crowdTipsTotal", crowdTipsTotal);
			taskMap.put("inforTipsTotal", inforTipsTotal);
			taskMap.put("poiAllNum", poiAllNum);
			taskMap.put("poiUnFreshNum", poiUnFreshNum);
			taskMap.put("poiFinishAndPlanNum", poiFinishAndPlanNum);
			taskMap.put("poiActualAddNum", poiActualAddNum);
			taskMap.put("poiActualUpdateNum", poiActualUpdateNum);
			taskMap.put("poiActualDeleteNum", poiActualDeleteNum);
			taskMap.put("poiFreshNum", poiFreshNum);
			taskMap.put("crowdPoiTotal", crowdPoiTotal);
			taskMap.put("multisourcePoiTotal", multisourcePoiTotal);
			taskMap.put("notaskPoiNum", notaskPoiNum);
			taskMap.put("notaskTipsNum", notaskTipsNum);
			taskMap.put("programType", programType);
			taskMap.put("poiCollectPercent", poiCollectPercent);
			taskMap.put("poiDayPercent", poiDayPercent);
			taskMap.put("roadPercent", roadPercent);
			taskMap.put("percent", percent);
			taskMap.put("progress", progress);
			taskMap.put("programId", programId);
			taskMap.put("blockId", blockId);
			taskMap.put("createDate", createDate);
			
			return taskMap;
		} catch (Exception e) {
			log.error(taskMap);
			log.error("处理数据出错:" + e.getMessage(), e);
			throw new Exception("处理数据出错:" + e.getMessage(), e);
		}
	}
	
	/**
	 * 处理开始时间
	 */
	private String startTime(List<String> startTimeList){
		String time2 = "";
		String startTime = "";
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			Calendar c1=Calendar.getInstance();     
			Calendar c2=Calendar.getInstance();     
			if(startTimeList.size() == 1){
				startTime = startTimeList.get(0);
			}
			if(startTimeList.size() > 1){
				startTime = startTimeList.get(0);
				for(int i=1;i<startTimeList.size();i++){
					//取最小时间
					time2 = startTimeList.get(i);
					c1.setTime(df.parse(startTime));     
					c2.setTime(df.parse(time2));     
					int result=c1.compareTo(c2);
					if(result > 0){
						startTime = time2;
					}
				}
			}
		} catch (Exception e) {
			log.error("处理实际开始时间出错:" + e.getMessage(), e);
			e.printStackTrace();
		}
		return startTime;
	}
	
	
	
}
