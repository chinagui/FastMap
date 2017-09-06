package com.navinfo.dataservice.job.statics.manJob;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;

/**
 * 项目统计
 * @ClassName TaskJob
 * @author songhe
 * @date 2017年9月4日
 * 
 */
public class ProgramJob extends AbstractStatJob {

	private static final String dbName = SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
	private SimpleDateFormat sd = new SimpleDateFormat("yyyyMMddHHmmss");
	
	protected ManApi manApi = null;
	
	public ProgramJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public String stat() throws JobException {
		try {
			long t = System.currentTimeMillis();
			//获取统计时间
			ProgramJobRequest statReq = (ProgramJobRequest)request;
			log.info("start stat "+statReq.getJobType());
			String timestamp = statReq.getTimestamp();
			
			manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			//orical查询所有的任务
			List<Map<String, Object>> programs = manApi.queryProgramStat();
			//查询MAN_TIMELINE表获取相应的数据
			String objName = "program";
			Map<Integer, Map<String, Object>> manTimeline = manApi.queryManTimelineByObjName(objName, 0);
			//根据orical中查出来的数据判断部分统计项
			Map<Integer, Object> programFromOrical = calculateProgram(programs, manTimeline);
			//mongo中查询所有项目下任务的统计数据求和返回
			Map<Integer, Object> programFromMongo = getTaskStatData(timestamp);

			//项目统计数据
			List<Map<String, Object>> programStatList = convertProgramData(programFromMongo, programFromOrical);
			JSONObject result = new JSONObject();
			result.put("program", programStatList);

			log.info("end stat "+statReq.getJobType());
			log.debug("所有项目数据统计完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			
			return result.toString();
			
		} catch (Exception e) {
			log.error("项目统计:"+e.getMessage(), e);
			throw new JobException("项目统计:"+e.getMessage(),e);
		}
	}
	
	
	/**
	 * 查询mongo中task统计数据
	 * @param String
	 * @return Map<Integer, Map<String,Object>>
	 * @throws ServiceException 
	 */
	@SuppressWarnings("unchecked")
	public Map<Integer, Object> getTaskStatData(String timestamp) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(dbName);
			BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
			FindIterable<Document> findIterable = mongoDao.find("task", filter);
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<Integer, Object> stat = new HashMap<Integer, Object>();
			
			//统计数据
			float roadPlanTotal = 0f;
			double roadActualTotal = 0d;
			double collectLinkUpdateTotal = 0d;
			int poiPlanTotal = 0;
			int poiActualTotal = 0;
			int poiAllNum = 0;
			int collectTipsUploadNum = 0;
			double collectLink17UpdateTotal = 0d;
			double link17AllLen = 0d;
			int roadPlanOut = 0;
			int dayEditTipsFinishNum = 0;
			int day2MonthNum = 0;
			int tips2MarkNum = 0;
			int poiFinishNum = 0;
			int monthPoiFinishNum = 0;
			
			String taskCreateDate = "";
			String collectAcutalStartDate = "";
			String collectAcutalEndDate = "";
			String dayAcutalStartDate = "";
			String dayAcutalEndDate = "";
			String monthAcutalStartDate = "";
			String monthAcutalEndDate = "";
			
			//处理数据
			while(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				int programId = (int) jso.get("programId");
				//按批次统计数量
				JSONObject lot1Poi = new JSONObject();
				JSONObject lot2Poi = new JSONObject();
				JSONObject lot3Poi = new JSONObject();
				JSONObject lot1Tips = new JSONObject();
				JSONObject lot2Tips = new JSONObject();
				JSONObject lot3Tips = new JSONObject();
				
				Map<String, Object> taskData = new HashMap<>();
				if(stat.containsKey(programId)){
					taskData = (Map<String, Object>) stat.get(programId);
					roadPlanTotal = Float.valueOf(taskData.get("roadPlanTotal").toString());
					roadActualTotal = Double.valueOf(taskData.get("roadActualTotal").toString());
					collectLinkUpdateTotal = Double.valueOf(taskData.get("collectLinkUpdateTotal").toString());
					poiPlanTotal = (int) taskData.get("poiPlanTotal");
					poiActualTotal = (int) taskData.get("poiActualTotal");
					poiAllNum = (int) taskData.get("poiAllNum");
					collectTipsUploadNum = (int) taskData.get("collectTipsUploadNum");
					collectLink17UpdateTotal = Double.valueOf(taskData.get("collectLink17UpdateTotal").toString());
					link17AllLen = Double.valueOf(taskData.get("link17AllLen").toString());
					roadPlanOut = (int) taskData.get("roadPlanOut");
					dayEditTipsFinishNum = (int) taskData.get("dayEditTipsFinishNum");
					day2MonthNum = (int) taskData.get("day2MonthNum");
					tips2MarkNum = (int) taskData.get("tips2MarkNum");
					poiFinishNum = (int) taskData.get("poiFinishNum");
					monthPoiFinishNum = (int) taskData.get("monthPoiFinishNum");
					if(null != taskData.get("taskCreateDate") && StringUtils.isNotBlank(taskData.get("taskCreateDate").toString())){
						taskCreateDate = taskData.get("taskCreateDate").toString();
					}
					if(null != taskData.get("collectAcutalStartDate") && StringUtils.isNotBlank(taskData.get("collectAcutalStartDate").toString())){
						collectAcutalStartDate = taskData.get("collectAcutalStartDate").toString();
					}
					if(null != taskData.get("collectAcutalEndDate") && StringUtils.isNotBlank(taskData.get("collectAcutalEndDate").toString())){
						collectAcutalEndDate = taskData.get("collectAcutalEndDate").toString();
					}
					if(null != taskData.get("dayAcutalStartDate") && StringUtils.isNotBlank(taskData.get("dayAcutalStartDate").toString())){
						dayAcutalStartDate = taskData.get("dayAcutalStartDate").toString();
					}
					if(null != taskData.get("dayAcutalEndDate") && StringUtils.isNotBlank(taskData.get("dayAcutalEndDate").toString())){
						dayAcutalEndDate = taskData.get("dayAcutalEndDate").toString();
					}
					if(null != taskData.get("monthAcutalStartDate") && StringUtils.isNotBlank(taskData.get("monthAcutalStartDate").toString())){
						monthAcutalStartDate = taskData.get("monthAcutalStartDate").toString();
					}
					if(null != taskData.get("monthAcutalEndDate") && StringUtils.isNotBlank(taskData.get("monthAcutalEndDate").toString())){
						monthAcutalEndDate = taskData.get("monthAcutalEndDate").toString();
					}
					lot1Poi =  (JSONObject) taskData.get("lot1Poi");
					lot2Poi =  (JSONObject) taskData.get("lot2Poi");
					lot3Poi =  (JSONObject) taskData.get("lot3Poi");
					lot1Tips = (JSONObject) taskData.get("lot1Tips");
					lot2Tips = (JSONObject) taskData.get("lot2Tips");
					lot3Tips = (JSONObject) taskData.get("lot3Tips");
				}
				roadPlanTotal += Float.valueOf(jso.get("roadPlanTotal").toString());
				roadActualTotal += jso.getDouble("collectRoadActualTotal");
				collectLinkUpdateTotal += jso.getDouble("collectLinkUpdateTotal");
				poiPlanTotal +=  jso.getInt("poiPlanTotal");
				poiActualTotal += jso.getInt("poiFinishNum");
				poiAllNum += jso.getInt("poiAllNum");
				collectTipsUploadNum += jso.getInt("collectTipsUploadNum");
				collectLink17UpdateTotal += jso.getDouble("collectLink17UpdateTotal");
				link17AllLen += jso.getDouble("link17AllLen");
				roadPlanOut += jso.getInt("roadPlanOut");
				dayEditTipsFinishNum +=  jso.getInt("dayEditTipsFinishNum");
				day2MonthNum +=  jso.getInt("day2MonthNum");
				tips2MarkNum +=  jso.getInt("tips2MarkNum");
				poiFinishNum +=  jso.getInt("poiFinishNum");
				monthPoiFinishNum += jso.getInt("monthPoiFinishNum");
				
				int status = jso.getInt("status");
				int type = jso.getInt("type");
				switch(type){
					case 0:
						if(StringUtils.isBlank(collectAcutalStartDate)){
							collectAcutalStartDate = jso.getString("actualStartDate");
						}else{
							if(collectAcutalStartDate.compareTo(jso.getString("actualStartDate")) > 0){
								collectAcutalStartDate = jso.getString("actualStartDate");
							}
						}
						//关闭的任务才取关闭时间
						if(status == 0){
							if(StringUtils.isBlank(collectAcutalEndDate)){
								collectAcutalEndDate = jso.getString("actualEndDate");
							}else{
								if(collectAcutalEndDate.compareTo(jso.getString("actualEndDate")) < 0){
									collectAcutalEndDate = jso.getString("actualEndDate");
								}
							}
						}
						break;
					case 1:
						//日编开始结束时间
						if(status == 0){
							if(StringUtils.isBlank(dayAcutalEndDate)){
								dayAcutalEndDate = jso.getString("actualEndDate");
							}else{
								if(dayAcutalEndDate.compareTo(jso.getString("actualEndDate")) < 0){
									dayAcutalEndDate = jso.getString("actualEndDate");
								}
							}
						}
						
						if(StringUtils.isBlank(dayAcutalStartDate)){
							dayAcutalStartDate = jso.getString("actualStartDate");
						}else{
							if(dayAcutalStartDate.compareTo(jso.getString("actualStartDate")) > 0){
								dayAcutalStartDate = jso.getString("actualStartDate");
							}
						}
						break;
					case 2:
						//月编开始结束时间
						if(status == 0){
							if(StringUtils.isBlank(monthAcutalEndDate)){
								monthAcutalEndDate = jso.getString("actualEndDate");
							}else{
								if(monthAcutalEndDate.compareTo(jso.getString("actualEndDate")) < 0){
									monthAcutalEndDate = jso.getString("actualEndDate");
								}
							}
						}
						
						if(StringUtils.isBlank(monthAcutalStartDate)){
							monthAcutalStartDate = jso.getString("actualStartDate");
						}else{
							if(monthAcutalStartDate.compareTo(jso.getString("actualStartDate")) > 0){
								monthAcutalStartDate = jso.getString("actualStartDate");
							}
						}
						break;
					default:
						break;
				}
				int lot = jso.getInt("lot");
				//项目分批次详细情况
				switch(lot){
					case 1:
						lot1Poi.put("poiPlanTotal", poiPlanTotal + (lot1Poi.containsKey("poiPlanTotal") ? Integer.parseInt(lot1Poi.get("poiPlanTotal").toString()) : 0));
						lot1Tips.put("roadPlanOut", roadPlanOut + (lot1Tips.containsKey("roadPlanOut") ? Integer.parseInt(lot1Tips.get("roadPlanOut").toString()) : 0));
						lot1Poi.put("poiFinishNum", poiFinishNum + (lot1Poi.containsKey("poiFinishNum") ? Integer.parseInt(lot1Poi.get("poiFinishNum").toString()) : 0));
						lot1Tips.put("collectTipsUploadNum", collectTipsUploadNum + (lot1Tips.containsKey("collectTipsUploadNum") ? Integer.parseInt(lot1Tips.get("collectTipsUploadNum").toString()) : 0));
						lot1Poi.put("day2MonthNum", day2MonthNum + (lot1Poi.containsKey("day2MonthNum") ? Integer.parseInt(lot1Poi.get("day2MonthNum").toString()) : 0));
						lot1Tips.put("tips2MarkNum", tips2MarkNum + (lot1Tips.containsKey("tips2MarkNum") ? Integer.parseInt(lot1Tips.get("tips2MarkNum").toString()) : 0));
						lot1Poi.put("monthPoiFinishNum", monthPoiFinishNum + (lot1Poi.containsKey("monthPoiFinishNum") ? Integer.parseInt(lot1Poi.get("monthPoiFinishNum").toString()) : 0));
						break;
					case 2:
						lot2Poi.put("poiPlanTotal", poiPlanTotal + (lot2Poi.containsKey("poiPlanTotal") ? Integer.parseInt(lot2Poi.get("poiPlanTotal").toString()) : 0));
						lot2Tips.put("roadPlanOut", roadPlanOut + (lot2Tips.containsKey("roadPlanOut") ? Integer.parseInt(lot2Tips.get("roadPlanOut").toString()) : 0));
						lot2Poi.put("poiFinishNum", poiFinishNum + (lot2Poi.containsKey("poiFinishNum") ? Integer.parseInt(lot2Poi.get("poiFinishNum").toString()) : 0));
						lot2Tips.put("collectTipsUploadNum", collectTipsUploadNum + (lot2Tips.containsKey("collectTipsUploadNum") ? Integer.parseInt(lot2Tips.get("collectTipsUploadNum").toString()) : 0));
						lot2Poi.put("day2MonthNum", day2MonthNum + (lot2Poi.containsKey("day2MonthNum") ? Integer.parseInt(lot2Poi.get("day2MonthNum").toString()) : 0));
						lot2Tips.put("tips2MarkNum", tips2MarkNum + (lot2Tips.containsKey("tips2MarkNum") ? Integer.parseInt(lot2Tips.get("tips2MarkNum").toString()) : 0));
						lot2Poi.put("monthPoiFinishNum", monthPoiFinishNum + (lot2Poi.containsKey("monthPoiFinishNum") ? Integer.parseInt(lot2Poi.get("monthPoiFinishNum").toString()) : 0));
						break;
					case 3:
						lot3Poi.put("poiPlanTotal", poiPlanTotal + (lot3Poi.containsKey("poiPlanTotal") ? Integer.parseInt(lot3Poi.get("poiPlanTotal").toString()) : 0));
						lot3Tips.put("roadPlanOut", roadPlanOut + (lot3Tips.containsKey("roadPlanOut") ? Integer.parseInt(lot3Tips.get("roadPlanOut").toString()) : 0));
						lot3Poi.put("poiFinishNum", poiFinishNum + (lot3Poi.containsKey("poiFinishNum") ? Integer.parseInt(lot3Poi.get("poiFinishNum").toString()) : 0));
						lot3Tips.put("collectTipsUploadNum", collectTipsUploadNum + (lot3Tips.containsKey("collectTipsUploadNum") ? Integer.parseInt(lot3Tips.get("collectTipsUploadNum").toString()) : 0));
						lot3Poi.put("day2MonthNum", day2MonthNum + (lot3Poi.containsKey("day2MonthNum") ? Integer.parseInt(lot3Poi.get("day2MonthNum").toString()) : 0));
						lot3Tips.put("tips2MarkNum", tips2MarkNum + (lot3Tips.containsKey("tips2MarkNum") ? Integer.parseInt(lot3Tips.get("tips2MarkNum").toString()) : 0));
						lot3Poi.put("monthPoiFinishNum", monthPoiFinishNum + (lot3Poi.containsKey("monthPoiFinishNum") ? Integer.parseInt(lot3Poi.get("monthPoiFinishNum").toString()) : 0));
						break;
					default:
						break;
				}
				//任务创建最早时间
				if(StringUtils.isBlank(taskCreateDate)){
					taskCreateDate = jso.getString("createDate");
				}else{
					if(taskCreateDate.compareTo(jso.getString("createDate")) > 0){
						taskCreateDate = jso.getString("createDate");
					}
				}
				
				taskData.put("roadPlanTotal", roadPlanTotal);
				taskData.put("roadActualTotal", roadActualTotal);
				taskData.put("collectLinkUpdateTotal", collectLinkUpdateTotal);
				taskData.put("poiPlanTotal", poiPlanTotal);
				taskData.put("poiActualTotal", poiActualTotal);
				taskData.put("poiAllNum", poiAllNum);
				taskData.put("collectTipsUploadNum", collectTipsUploadNum);
				taskData.put("collectLink17UpdateTotal", collectLink17UpdateTotal);
				taskData.put("link17AllLen", link17AllLen);
				taskData.put("roadPlanOut", roadPlanOut);
				taskData.put("dayEditTipsFinishNum", dayEditTipsFinishNum);
				taskData.put("day2MonthNum", day2MonthNum);
				taskData.put("tips2MarkNum", tips2MarkNum);
				taskData.put("poiFinishNum", poiFinishNum);
				taskData.put("monthPoiFinishNum", monthPoiFinishNum);
				taskData.put("taskCreateDate", taskCreateDate);
				taskData.put("monthAcutalStartDate", monthAcutalStartDate);
				taskData.put("monthAcutalEndDate", monthAcutalEndDate);
				taskData.put("dayAcutalEndDate", dayAcutalEndDate);
				taskData.put("dayAcutalStartDate", dayAcutalStartDate);
				taskData.put("collectAcutalStartDate", collectAcutalStartDate);
				taskData.put("collectAcutalEndDate", collectAcutalEndDate);
				
				taskData.put("lot1Poi", lot1Poi);
				taskData.put("lot2Poi", lot2Poi);
				taskData.put("lot3Poi", lot3Poi);
				taskData.put("lot1Tips", lot1Tips);
				taskData.put("lot2Tips", lot2Tips);
				taskData.put("lot3Tips", lot3Tips);
				
				stat.put(programId, taskData);
			}
			return stat;
		} catch (Exception e) {
			log.error("查询mongo中任务统计数据报错" + e.getMessage(), e);
			throw new Exception("查询mongo中查询任务统计数据报错" + e.getMessage(), e);
		}
	}
	
	
	/**
	 * 根据orical中查出来的数据判断部分统计项
	 * @param List<Map<String, Object>>
	 * @return Map<Integer, Object>
	 * 
	 * */
	@SuppressWarnings("unchecked")
	public Map<Integer, Object> calculateProgram(List<Map<String, Object>> programs, Map<Integer, Map<String, Object>> manTimeline){
		Map<Integer, Object> result = new HashMap<>();
		for(int i = 0; i < programs.size(); i++){
			Map<String, Object> programMap = programs.get(i);
			Map<String, Object> programStat = new HashMap<>();
			int programId = (int) programMap.get("programId");
			int inforId = (int) programMap.get("inforId");
			String infoTypeName = "";
			if(programMap.get("infoTypeName") != null){
				infoTypeName = programMap.get("infoTypeName").toString();
			}
			String method = "";
			if(programMap.get("method") != null){
				method = programMap.get("method").toString();
			}
			int planStatus = 0;
			String inforInsertTime = "";
			String inforExpectDate = "";
			String taskCreateDate = "";
			String createDate = "";
			String name = "";
			String actualEndDate = "";
			int isAdopted = 0;
			String denyReason = "";
			String inforCity = "";
			int unPlanBlockNum = 0;
			JSONObject collectOverdueReasonNum = new JSONObject();
			collectOverdueReasonNum.put("现场变化大", 0);
			collectOverdueReasonNum.put("天气影响", 0);
			collectOverdueReasonNum.put("车辆故障", 0);
			collectOverdueReasonNum.put("设备故障", 0);
			collectOverdueReasonNum.put("人员变动", 0);
			collectOverdueReasonNum.put("整体规划变更", 0);
			collectOverdueReasonNum.put("其它原因", 0);
			JSONObject dayOverdueReasonNum = new JSONObject();
			dayOverdueReasonNum.put("外业回图量超出预期", 0);
			dayOverdueReasonNum.put("服务器问题", 0);
			dayOverdueReasonNum.put("其他临时任务", 0);
			dayOverdueReasonNum.put("图幅冲突", 0);
			dayOverdueReasonNum.put("人员变动", 0);
			dayOverdueReasonNum.put("其它原因", 0);
			
			int isProduce = 0;
			int isOverDue = 1;
			int normalClosed = 1;
			int advanceClosed = 2;
			int overdueClosed = 1;
			int collectAdvanceClosed = 2;
			int dayAdvanceClosed = 2;
			int produceAdvanceClosed = 2;
			int collectOverdue = 2;
			int dayOverdue = 2;
			int produceOverdue = 2;
			//快线采集和日编任务是否正常关闭标识,默认正常
			int isCollectTaskClosed = 1;
			int isDayTaskClosed = 1;
			
			if(programMap.get("name") != null && StringUtils.isNotBlank(programMap.get("name").toString())){
				name = programMap.get("name").toString();
			}
			if(programMap.get("inforInsertTime") != null && StringUtils.isNotBlank(programMap.get("inforInsertTime").toString())){
				inforInsertTime = sd.format((Timestamp) programMap.get("inforInsertTime"));
			}
			if(programMap.get("inforExpectDate") != null && StringUtils.isNotBlank(programMap.get("inforExpectDate").toString())){
				inforExpectDate = sd.format((Timestamp) programMap.get("inforExpectDate"));
			}

			if(programMap.get("createDate") != null && StringUtils.isNotBlank(programMap.get("createDate").toString())){
				createDate = sd.format((Timestamp) programMap.get("createDate"));
			}
			int cityId = (int) programMap.get("cityId");
			
			if(manTimeline.containsKey(programId)){
				actualEndDate = (String) manTimeline.get(programId).get("operateDate");
			}else{
				actualEndDate = sd.format(new Date());
			}
			//取当前programId值进行统计项判断
			if(result.containsKey(programId)){
				Map<String, Object> data = (Map<String, Object>) result.get(programId);
				isProduce = (int) data.get("isProduce");
				isOverDue = (int) data.get("isOverDue");
				normalClosed = (int) data.get("normalClosed");
				advanceClosed = (int) data.get("advanceClosed");
				overdueClosed = (int) data.get("overdueClosed");
				collectAdvanceClosed = (int) data.get("collectAdvanceClosed");
				dayAdvanceClosed = (int) data.get("dayAdvanceClosed");
				produceAdvanceClosed = (int) data.get("produceAdvanceClosed");
				collectOverdue = (int) data.get("collectOverdue");
				collectOverdueReasonNum = (JSONObject) data.get("collectOverdueReasonNum");
				dayOverdue = (int) data.get("dayOverdue");
				dayOverdueReasonNum =  (JSONObject) data.get("dayOverdueReasonNum");
				produceOverdue = (int) data.get("produceOverdue");
				unPlanBlockNum = (int) data.get("unPlanBlockNum");
				isAdopted = (int) data.get("isAdopted");
				inforCity = data.get("inforCity").toString();
				denyReason = data.get("denyReason").toString();
				isCollectTaskClosed = (int) data.get("isCollectTaskClosed");
				isDayTaskClosed = (int) data.get("isDayTaskClosed");
				planStatus = (int) data.get("planStatus");
				infoTypeName = data.get("infoTypeName").toString();
				method = data.get("method").toString();
			}

			int type = (int) programMap.get("type");
			int status = (int) programMap.get("status");
			int produceStatus = (int) programMap.get("produceStatus");
			int diffDate = (int) programMap.get("diffDate");
			String producePlanEndDate = "";
			if(programMap.get("producePlanEndDate") != null){
				producePlanEndDate = sd.format((Timestamp) programMap.get("producePlanEndDate"));
			}
			String produceDate = "";
			if(programMap.get("produceDate") != null){
				produceDate = sd.format((Timestamp) programMap.get("produceDate"));
			}
			//取更早的任务创建时间
			if(programMap.get("taskCreateDate") != null && StringUtils.isNotBlank(programMap.get("taskCreateDate").toString())){
				if(StringUtils.isBlank(taskCreateDate)){
					taskCreateDate = sd.format((Timestamp) programMap.get("taskCreateDate"));
				}else{
					String taskDate = sd.format((Timestamp) programMap.get("taskCreateDate"));
					taskCreateDate = taskCreateDate.compareTo(taskDate) > 0 ? taskDate : taskCreateDate;
				}
				
			}
			int taskType = (int) programMap.get("taskType");
			String overdueReason = (String) (programMap.get("overdueReason") == null ? "其他原因" : programMap.get("overdueReason"));
				
			//是否出品
			if(produceStatus == 2){
				isProduce = 1;
			}
			//是否逾期,快线开启和草稿状态的项目进行判断
			if((status == 1 || status == 2) && type == 1){
				if(diffDate < 0){
					isOverDue = 2;
				}
			}
			//是否正常关闭
			if(type == 1){
				if(diffDate != 0){
					normalClosed = 2;
				}
			}
			//是否提前关闭
			if(isOverDue == 1 && status == 0){
				if((type == 1 && diffDate > 0) || produceDate.compareTo(producePlanEndDate) < 0){
					advanceClosed = 1;
				}
				if(type == 4 && diffDate > 0){
					advanceClosed = 1;
				}
			}
			//是否延迟关闭
			if(type == 1 && status == 0){
				if(diffDate < 0){
					overdueClosed = 1;	
				}
			}
			//快线已关闭项目是否正常关闭
			if(type == 4 && status == 0){
				if(diffDate < 0){
					if(taskType == 0){
						isCollectTaskClosed = 2;
					}
					if(taskType == 1){
						isDayTaskClosed = 2;
					}
				}
			}
			//是否采集/日编/出品提前关闭--快线逾期原因统计
			if(type == 4 && isOverDue == 1){
				if((status == 0 && diffDate > 0)){
					if(taskType == 0){
						collectAdvanceClosed = 1;
						if(collectOverdueReasonNum.containsKey(overdueReason)){
							collectOverdueReasonNum.put(overdueReason, Integer.parseInt(collectOverdueReasonNum.get(overdueReason).toString()) + 1);
						}else{
							collectOverdueReasonNum.put(overdueReason, 1);
						}
					}
					//日编任务是否提前关闭的前提是采集任务无逾期
					if(taskType == 1 && isCollectTaskClosed != 2){
						dayAdvanceClosed = 1;
						if(dayOverdueReasonNum.containsKey(overdueReason)){
							dayOverdueReasonNum.put(overdueReason, Integer.parseInt(dayOverdueReasonNum.get(overdueReason).toString()) + 1);
						}else{
							dayOverdueReasonNum.put(overdueReason, 1);
						}
					}
					//出品任务提前关闭前提是月编和采集任务无逾期
					if(isCollectTaskClosed != 2 && isDayTaskClosed != 2){
						produceAdvanceClosed = 1;
					}
				}
			}
			//是否采集/日编逾期  至少有一个采集任务逾期,仅快线
			if(type == 4 && isOverDue == 2){
				if(taskType == 0){
					collectOverdue = 1;
				}
				if(taskType == 1){
					dayOverdue =1;
				}
			}
			//是否出品逾期
			if(type == 4){
				if(collectOverdue == 2 && dayOverdue == 2){
					if(produceDate.compareTo(producePlanEndDate) > 0){
						produceOverdue = 1;
					}
				}
			}
			//采纳情况//采纳原因//情报城市
			if(type == 4){
				isAdopted = (int) programMap.get("isAdopted");
				if(programMap.get("denyReason") != null && StringUtils.isNotBlank(programMap.get("denyReason").toString())){
					denyReason = programMap.get("denyReason").toString();
				}
				if(programMap.get("inforCity") != null && StringUtils.isNotBlank(programMap.get("inforCity").toString())){
					inforCity = programMap.get("inforCity").toString();
				}
			}
			//未规划区县数量
			if(type == 1 && 0 == (int)programMap.get("planStatus")){
				unPlanBlockNum++;
			}
			//城市/情报规划情况
			int cityPlan =  (int) programMap.get("cityPlan");
			int inforPlan = (int) programMap.get("inforPlan");
			if(cityPlan == 1 || inforPlan == 1){
				planStatus = 1;
			}
			programStat.put("isProduce", isProduce);
			programStat.put("isOverDue", isOverDue);
			programStat.put("normalClosed", normalClosed);
			programStat.put("advanceClosed", advanceClosed);
			programStat.put("overdueClosed", overdueClosed);
			programStat.put("collectAdvanceClosed", collectAdvanceClosed);
			programStat.put("dayAdvanceClosed", dayAdvanceClosed);
			programStat.put("produceAdvanceClosed", produceAdvanceClosed);
			programStat.put("collectOverdue", collectOverdue);
			programStat.put("collectOverdueReasonNum", collectOverdueReasonNum);
			programStat.put("dayOverdueReasonNum", dayOverdueReasonNum);
			programStat.put("dayOverdue", dayOverdue);
			programStat.put("produceOverdue", produceOverdue);
			programStat.put("produceDate", produceDate);
			programStat.put("actualEndDate", actualEndDate);
			programStat.put("isAdopted", isAdopted);
			programStat.put("denyReason", denyReason);
			programStat.put("inforCity", inforCity);
			programStat.put("unPlanBlockNum", unPlanBlockNum);
			programStat.put("inforInsertTime", inforInsertTime);
			programStat.put("inforExpectDate", inforExpectDate);
			programStat.put("taskCreateDate", taskCreateDate);
			programStat.put("createDate", createDate);
			programStat.put("name", name);
			programStat.put("cityId", cityId);
			programStat.put("inforId", inforId);
			programStat.put("isCollectTaskClosed", isCollectTaskClosed);
			programStat.put("isDayTaskClosed", isDayTaskClosed);
			programStat.put("type", type);
			programStat.put("planStatus", planStatus);
			programStat.put("infoTypeName", infoTypeName);
			programStat.put("method", method);
			programStat.put("status", status);
			
			result.put(programId, programStat);
		}
		return result;
	}
	
	/**
	 * 统计数据
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> convertProgramData(Map<Integer, Object> programFromMongo, Map<Integer, Object> programFromOrical) throws Exception {
		try {
			//处理从orical和mongo中查询出的项目数据
			List<Map<String, Object>> result = new ArrayList<>();
			for(Entry<Integer, Object> oricalProram : programFromOrical.entrySet()){
				Map<String, Object> programMap = (Map<String, Object>) oricalProram.getValue();
				//统计项
				int programId = oricalProram.getKey();
				int cityId = (int) programMap.get("cityId");
				int inforId = (int) programMap.get("inforId");
				int type = (int) programMap.get("type");
				int planStatus = (int) programMap.get("planStatus");
				int status = (int) programMap.get("status");
				String infoTypeName = programMap.get("infoTypeName").toString();
				String method = programMap.get("method").toString();
				int isProduce = (int) programMap.get("isProduce");
				int isOverDue = (int) programMap.get("isOverDue");
				int normalClosed = (int) programMap.get("normalClosed");
				int advanceClosed = (int) programMap.get("advanceClosed");
				int overdueClosed = (int) programMap.get("overdueClosed");
				int collectAdvanceClosed = (int) programMap.get("collectAdvanceClosed");
				int dayAdvanceClosed = (int) programMap.get("dayAdvanceClosed");
				int produceAdvanceClosed = (int) programMap.get("produceAdvanceClosed");
				int collectOverdue = (int) programMap.get("collectOverdue");
				JSONObject collectOverdueReasonNum = (JSONObject) programMap.get("collectOverdueReasonNum");
				int dayOverdue = (int) programMap.get("dayOverdue");
				JSONObject dayOverdueReasonNum = (JSONObject) programMap.get("dayOverdueReasonNum");
				int produceOverdue = (int) programMap.get("produceOverdue");
				String produceDate = (String) programMap.get("produceDate");
				String actualEndDate = (String) programMap.get("actualEndDate");
				int isAdopted = (int) programMap.get("isAdopted");
				String denyReason = programMap.get("denyReason").toString();
				String inforCity = programMap.get("inforCity").toString();
				int unPlanBlockNum = (int) programMap.get("unPlanBlockNum");
				String inforInsertTime = programMap.get("inforInsertTime").toString();
				String inforExpectDate = programMap.get("inforExpectDate").toString();
				String taskCreateDate = programMap.get("taskCreateDate").toString();
				String createDate = programMap.get("createDate").toString();
				String name = programMap.get("name").toString();
				
				float roadPlanTotal = 0f;
				double roadActualTotal = 0d;
				double collectLinkUpdateTotal = 0d;
				int poiPlanTotal = 0;
				int poiActualTotal = 0;
				int poiAllNum = 0;
				int collectTipsUploadNum = 0;
				double collectLink17UpdateTotal = 0d;
				double link17AllLen = 0d;
				int roadPlanOut = 0;
				int dayEditTipsFinishNum = 0;
				int isDay2Month = 0;
				int isTips2Mark = 0;
				String collectAcutalStartDate = "";
				String collectAcutalEndDate = "";
				String dayAcutalStartDate = "";
				String dayAcutalEndDate = "";
				String monthAcutalStartDate = "";
				String monthAcutalEndDate = "";
				//按批次统计数量
				JSONObject lot1Poi = new JSONObject();
				JSONObject lot2Poi = new JSONObject();
				JSONObject lot3Poi = new JSONObject();
				JSONObject lot1Tips = new JSONObject();
				JSONObject lot2Tips = new JSONObject();
				JSONObject lot3Tips = new JSONObject();
				if(programFromMongo.containsKey(programId)){
					Map<String, Object> programMongo = (Map<String, Object>) programFromMongo.get(programId);
					roadPlanTotal = Float.valueOf(programMongo.get("roadPlanTotal").toString());
					roadActualTotal = Double.valueOf(programMongo.get("roadActualTotal").toString());
					collectLinkUpdateTotal = Double.valueOf(programMongo.get("collectLinkUpdateTotal").toString());
					poiPlanTotal = Integer.parseInt(programMongo.get("poiPlanTotal").toString());
					poiActualTotal = Integer.parseInt(programMongo.get("poiActualTotal").toString());
					poiAllNum = Integer.parseInt(programMongo.get("poiAllNum").toString());
					collectTipsUploadNum = Integer.parseInt(programMongo.get("collectTipsUploadNum").toString());
					collectLink17UpdateTotal = Double.valueOf(programMongo.get("collectLink17UpdateTotal").toString());
					link17AllLen = Double.valueOf(programMongo.get("link17AllLen").toString());
					roadPlanOut = Integer.parseInt(programMongo.get("roadPlanOut").toString());
					dayEditTipsFinishNum = Integer.parseInt(programMongo.get("dayEditTipsFinishNum").toString());
					isDay2Month = Integer.parseInt(programMongo.get("day2MonthNum").toString()) > 0 ? 1 : 0;
					isTips2Mark = Integer.parseInt(programMongo.get("tips2MarkNum").toString()) > 0 ? 1 : 0;
					collectAcutalStartDate = programMongo.get("collectAcutalStartDate").toString();
					collectAcutalEndDate = programMongo.get("collectAcutalEndDate").toString();
					dayAcutalStartDate = programMongo.get("dayAcutalStartDate").toString();
					dayAcutalEndDate = programMongo.get("dayAcutalEndDate").toString();
					monthAcutalStartDate = programMongo.get("monthAcutalStartDate").toString();
					monthAcutalEndDate = programMongo.get("monthAcutalEndDate").toString();
					//按批次统计数量,仅中线有效
					if(type == 1){
						lot1Poi = (JSONObject) programMongo.get("lot1Poi");
						lot2Poi = (JSONObject) programMongo.get("lot2Poi");
						lot3Poi = (JSONObject) programMongo.get("lot3Poi");
						lot1Tips = (JSONObject) programMongo.get("lot1Tips");
						lot2Tips = (JSONObject) programMongo.get("lot2Tips");
						lot3Tips = (JSONObject) programMongo.get("lot3Tips");
					}
				}
				if(lot1Poi.size() == 0){
					lot1Poi.put("poiPlanTotal", 0);
					lot1Poi.put("poiFinishNum", 0);
					lot1Poi.put("day2MonthNum", 0);
					lot1Poi.put("monthPoiFinishNum", 0);
				}
				if(lot2Poi.size() == 0){
					lot2Poi.put("poiPlanTotal", 0);
					lot2Poi.put("poiFinishNum", 0);
					lot2Poi.put("day2MonthNum", 0);
					lot2Poi.put("monthPoiFinishNum", 0);
				}
				if(lot3Poi.size() == 0){
					lot3Poi.put("poiPlanTotal", 0);
					lot3Poi.put("poiFinishNum", 0);
					lot3Poi.put("day2MonthNum", 0);
					lot3Poi.put("monthPoiFinishNum", 0);
				}
				if(lot1Tips.size() == 0){
					lot1Tips.put("roadPlanOut", 0);
					lot1Tips.put("collectTipsUploadNum", 0);
					lot1Tips.put("tips2MarkNum", 0);
				}
				if(lot2Tips.size() == 0){
					lot2Tips.put("roadPlanOut", 0);
					lot2Tips.put("collectTipsUploadNum", 0);
					lot2Tips.put("tips2MarkNum", 0);
				}
				if(lot3Tips.size() == 0){
					lot3Tips.put("roadPlanOut", 0);
					lot3Tips.put("collectTipsUploadNum", 0);
					lot3Tips.put("tips2MarkNum", 0);
					
				}	

				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("programId", programId);
				resultMap.put("cityId", cityId);
				resultMap.put("inforId", inforId);
				resultMap.put("type", type);
				resultMap.put("planStatus", planStatus);
				resultMap.put("status", status);
				resultMap.put("infoTypeName", infoTypeName);
				resultMap.put("method", method);
				resultMap.put("isProduce", isProduce);
				resultMap.put("isOverDue", isOverDue);
				resultMap.put("normalClosed", normalClosed);
				resultMap.put("advanceClosed", advanceClosed);
				resultMap.put("overdueClosed", overdueClosed);
				resultMap.put("collectAdvanceClosed", collectAdvanceClosed);
				resultMap.put("dayAdvanceClosed", dayAdvanceClosed);
				resultMap.put("produceAdvanceClosed", produceAdvanceClosed);
				resultMap.put("collectOverdue", collectOverdue);
				resultMap.put("collectOverdueReasonNum", collectOverdueReasonNum);
				resultMap.put("dayOverdue", dayOverdue);
				resultMap.put("dayOverdueReasonNum", dayOverdueReasonNum);
				resultMap.put("produceOverdue", produceOverdue);
				resultMap.put("produceDate", produceDate);
				resultMap.put("actualEndDate", actualEndDate);
				resultMap.put("isAdopted", isAdopted);
				resultMap.put("denyReason", denyReason);
				resultMap.put("inforCity", inforCity);
				resultMap.put("unPlanBlockNum", unPlanBlockNum);
				resultMap.put("inforInsertTime", inforInsertTime);
				resultMap.put("inforExpectDate", inforExpectDate);
				resultMap.put("taskCreateDate", taskCreateDate);
				resultMap.put("createDate", createDate);
				resultMap.put("name", name);
				resultMap.put("roadPlanTotal", roadPlanTotal);
				resultMap.put("roadActualTotal", roadActualTotal);
				resultMap.put("collectLinkUpdateTotal", collectLinkUpdateTotal);
				resultMap.put("poiPlanTotal", poiPlanTotal);
				resultMap.put("poiActualTotal", poiActualTotal);
				resultMap.put("poiAllNum", poiAllNum);
				resultMap.put("collectTipsUploadNum", collectTipsUploadNum);
				resultMap.put("collectLink17UpdateTotal", collectLink17UpdateTotal);
				resultMap.put("link17AllLen", link17AllLen);
				resultMap.put("roadPlanOut", roadPlanOut);
				resultMap.put("dayEditTipsFinishNum", dayEditTipsFinishNum);
				resultMap.put("isDay2Month", isDay2Month);
				resultMap.put("isTips2Mark", isTips2Mark);
				resultMap.put("collectAcutalStartDate", collectAcutalStartDate);
				resultMap.put("collectAcutalEndDate", collectAcutalEndDate);
				resultMap.put("dayAcutalStartDate", dayAcutalStartDate);
				resultMap.put("dayAcutalEndDate", dayAcutalEndDate);
				resultMap.put("monthAcutalStartDate", monthAcutalStartDate);
				resultMap.put("monthAcutalEndDate", monthAcutalEndDate);
				
				resultMap.put("lot1Poi", lot1Poi);
				resultMap.put("lot2Poi", lot2Poi);
				resultMap.put("lot3Poi", lot3Poi);
				resultMap.put("lot1Tips", lot1Tips);
				resultMap.put("lot2Tips", lot2Tips);
				resultMap.put("lot3Tips", lot3Tips);
				result.add(resultMap);
				
			}
			return result;
		} catch (Exception e) {
			log.error("处理数据出错:" + e.getMessage(), e);
			throw new Exception("处理数据出错:" + e.getMessage(), e);
		}
	}
	
}
