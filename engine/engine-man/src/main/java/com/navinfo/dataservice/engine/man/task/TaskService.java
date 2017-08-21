package com.navinfo.dataservice.engine.man.task;

import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.navinfo.dataservice.engine.man.job.bean.JobType;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.model.Block;
import com.navinfo.dataservice.api.man.model.Program;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.api.man.model.TaskCmsProgress;
import com.navinfo.dataservice.api.man.model.TaskProgress;
import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.JdbcSqlUtil;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.commons.util.TimestampUtils;
import com.navinfo.dataservice.dao.mq.email.EmailPublisher;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.dataservice.engine.man.block.BlockOperation;
import com.navinfo.dataservice.engine.man.block.BlockService;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.region.RegionService;
import com.navinfo.dataservice.engine.man.statics.StaticsOperation;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.timeline.TimelineService;
import com.navinfo.dataservice.engine.man.userGroup.UserGroupService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoService;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/** 
* @ClassName:  TaskService 
* @author code generator
* @date 2016-06-06 06:12:30 
* @Description: 
*/

public class TaskService {
	private Logger log = LoggerRepos.getLogger(TaskService.class);
	//private JSONArray newTask;
	private TaskService(){}
	private static class SingletonHolder{
		private static final TaskService INSTANCE =new TaskService();
	}
	public static TaskService getInstance(){
		return SingletonHolder.INSTANCE;
	}

	/**
	 * 根据用户id，与tasks的json对象批量创建task
	 * @param userId
	 * @param json
	 * @throws Exception
	 */
	public String create(long userId,JSONObject json) throws Exception{
		Connection conn = null;
		int total=0;
		try{
			if(!json.containsKey("tasks")){
				return "任务批量创建"+total+"个成功，0个失败";
			}
			JSONArray taskArray=json.getJSONArray("tasks");
			conn = DBConnector.getInstance().getManConnection();
			List<Task> taskList = new ArrayList<Task>();
			for (int i = 0; i < taskArray.size(); i++) {
				JSONObject taskJson = taskArray.getJSONObject(i);
				
				JSONArray workKindArray=null;
				if(taskJson.containsKey("workKind")){
					workKindArray=taskJson.getJSONArray("workKind");
					taskJson.remove("workKind");
				}
				Task bean=(Task) JsonOperation.jsonToBean(taskJson,Task.class);
				bean.setWorkKind(workKindArray);
				
				bean.setCreateUserId((int) userId);
				
				//获取grid信息
				List<Integer> gridList = GridService.getInstance().getGridListByBlockId(conn,bean.getBlockId());
				Map<Integer, Integer> gridIds = new HashMap<Integer, Integer>();
				for(Integer gridId:gridList){
					gridIds.put(gridId, 1);
				}
				bean.setGridIds(gridIds);
				
				//常规项目根据blockId获取region信息
				if(bean.getBlockId() != 0){
					int regionId = TaskOperation.getRegionIdByBlockId(bean.getBlockId());
					bean.setRegionId(regionId);
				}
				
				//采集任务 ,workKind外业采集或众包为1,调用组赋值方法				
				if(bean.getType()==0&&(bean.getSubWorkKind(1)==1||bean.getSubWorkKind(2)==1)){
					String adminCode = selectAdminCode(taskJson.getInt("programId"));
					
					if(adminCode != null && !"".equals(adminCode)){
						UserGroup userGroup = UserGroupService.getInstance().getGroupByAminCode(adminCode, 1);
						if(userGroup!=null){
							Integer userGroupID = userGroup.getGroupId();
							bean.setGroupId(userGroupID);
						}
					}
				}

				taskList.add(bean);
			}
			
			total = create(conn,taskList);
			return "任务批量创建"+total+"个成功，0个失败";
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询adminCode
	 * @param int,String
	 * @throws Exception
	 * @author songhe
	 */
	public String selectAdminCode(int programID){
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			String selectSql = "SELECT TO_CHAR(C.ADMIN_ID) ADMIN_CODE"
					+ "  FROM CITY C, PROGRAM P"
					+ " WHERE C.CITY_ID = P.CITY_ID"
					+ "   AND P.PROGRAM_ID = "+programID
					+ " UNION ALL"
					+ " SELECT I.ADMIN_CODE"
					+ "  FROM INFOR I, PROGRAM P"
					+ " WHERE P.INFOR_ID = I.INFOR_ID"
					+ "   AND P.PROGRAM_ID = "+programID;
			
			String adminCode = run.query(conn, selectSql, new ResultSetHandler<String>(){
				@Override
				public String handle(ResultSet rs)
						throws SQLException {
						if(rs.next()){
							return String.valueOf(rs.getString("ADMIN_CODE"));
						}
					return "";
				}
			});
			
			return adminCode;
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
		return null;
	}
	

	
	/**
	 * @param conn
	 * @param taskList
	 * @return
	 * @throws Exception 
	 */
	public int create(Connection conn, List<Task> taskList) throws Exception {
		int total = 0;
		for(Task task:taskList){
			createWithBean(conn,task);
			total ++;
		}
		return total;
	}
	
	/**
	 * 根据task对象生成task数据，并修改相关表状态
	 * @param conn
	 * @param bean
	 * @throws Exception
	 */
	public int createWithBean(Connection conn,Task bean) throws Exception{
		int taskId=0;
		try{
			/*与block关联的常规任务
			 * 1.修改同类型task的latest
			 * 2.如果block没有开启则开启block
			 */
			if(bean.getBlockId()!=0){
				TaskOperation.updateLatest(conn,bean.getProgramId(),bean.getRegionId(),bean.getBlockId(),bean.getType());
				List<Integer> blockList = new ArrayList<Integer>();
				blockList.add(bean.getBlockId());
				BlockOperation.openBlockByBlockIdList(conn,blockList);
			}	
			//创建任务
			taskId=TaskOperation.getNewTaskId(conn);
			bean.setTaskId(taskId);
			TaskOperation.insertTask(conn, bean);
			
			// 插入TASK_GRID_MAPPING
			if(bean.getGridIds() != null){
				TaskOperation.insertTaskGridMapping(conn, bean);
			}
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
		return taskId;
	}

//	/**
//	 * 根据情报id创建blockMan，若跨2个block，需要创建各自的blockMan
//	 * @param conn
//	 * @param inforId
//	 * @param userId
//	 * @param taskId
//	 * @throws Exception
//	 */
//	private void createInforBlock(Connection conn,String inforId,int userId,int taskId) throws Exception{
//		//查询情报infor
//		//Infor inforObj=InforManService.getInstance().query(inforId);
//		//String inforGeo=inforObj.getGeometry();
//		//查询情报city100002对应的所有block
//		//select block_id,geometry from block where city_id=100002
//		String selectSql="SELECT DISTINCT B.BLOCK_ID,I.INFOR_NAME||'_'||B.BLOCK_NAME BLOCK_NAME"
//				+ "  FROM BLOCK B, BLOCK_GRID_MAPPING M, INFOR_GRID_MAPPING IM,INFOR I"
//				+ " WHERE B.CITY_ID = 100002"
//				+ "   AND B.BLOCK_ID = M.BLOCK_ID"
//				+ "   AND IM.GRID_ID = M.GRID_ID"
//				+ "   AND IM.INFOR_ID = I.INFOR_ID"
//				+ "   AND IM.INFOR_ID='"+inforId+"'";
//		List<Map<String, Object>> blockList=new ArrayList<Map<String, Object>>();
//		try {
//			QueryRunner run = new QueryRunner();
//			ResultSetHandler<List<Map<String, Object>>> rsHandler = new ResultSetHandler<List<Map<String, Object>>>() {
//				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
//					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//					while (rs.next()) {
//						Map<String, Object> blockTmp=new HashMap<String, Object>();
//						blockTmp.put("blockId", rs.getInt("BLOCK_ID"));
//						blockTmp.put("blockName", rs.getString("BLOCK_NAME"));
//						list.add(blockTmp);
//					}
//					return list;
//				}
//
//			};
//			blockList= run.query(conn, selectSql, rsHandler);
//		} catch (Exception e) {
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
//		}
//		List<Integer> blockIdList=new ArrayList<Integer>();
//		for(Map<String, Object> blockId:blockList){
//			blockIdList.add((Integer) blockId.get("blockId"));
//			createInforBlockMan(conn,(Integer) blockId.get("blockId"),String.valueOf(blockId.get("blockName")),userId,taskId);}
//		BlockOperation.openBlockByBlockIdList(conn, blockIdList);
//	}
//	
//	/**
//	 * 创建情报任务
//	 * @param conn
//	 * @param blockId
//	 * @param userId
//	 * @param taskId
//	 * @throws Exception
//	 */
//	private void createInforBlockMan(Connection conn,Integer blockId,String blockName,int userId,int taskId) throws Exception{
//		String sql="insert into block_man (block_man_id,block_id,block_man_name,status,latest,create_user_id,create_date,task_id)"
//				+ "values(BLOCK_MAN_SEQ.NEXTVAL,"+blockId+",'"+blockName+"',2,1,"+userId+",sysdate,"+taskId+")";
//		QueryRunner run = new QueryRunner();
//		run.update(conn,sql);	
//	}
//	
//	public String taskPushMsg(long userId,JSONArray taskIds) throws Exception{
//		Connection conn = null;
//		try{
//			conn = DBConnector.getInstance().getManConnection();
//			//发送消息
//			JSONObject condition=new JSONObject();
//			condition.put("taskIds",taskIds);
//			List<Map<String, Object>> openTasks = TaskOperation.queryTaskTable(conn, condition);
//			/*任务创建/编辑/关闭
//			 * 1.所有生管角色
//			 * 2.分配的月编作业组组长
//			 * 任务:XXX(任务名称)内容发生变更，请关注*/			
//			String msgTitle="任务发布";
//			List<Map<String,Object>> msgContentList=new ArrayList<Map<String,Object>>();
//			List<Long> groupIdList = new ArrayList<Long>();
//			for(Map<String, Object> task:openTasks){
//				Map<String,Object> map = new HashMap<String, Object>();
//				String msgContent = "新增任务:"+task.get("taskName")+",请关注";
//				map.put("msgContent", msgContent);
//				groupIdList.add((Long) task.get("monthEditGroupId"));
//				//关联要素
//				JSONObject msgParam = new JSONObject();
//				msgParam.put("relateObject", "TASK");
//				msgParam.put("relateObjectId", task.get("taskId"));
//				map.put("msgParam", msgParam.toString());
//				List<Long> taskGroupIds = new ArrayList<Long>();
//				taskGroupIds.add((Long) task.get("monthEditGroupId"));
//				
//				map.put("taskGroupIds", taskGroupIds);
//				msgContentList.add(map);
//			}
//			if(msgContentList.size()>0){
//				taskPushMsg(conn,msgTitle,msgContentList,groupIdList,userId);
//			}		
//			TaskOperation.updateStatus(conn,taskIds);
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("任务发布消息发送失败，原因为:"+e.getMessage(),e);
//		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//		return "任务批量发布"+taskIds.size()+"个成功，0个失败";
//		
//	}

	public String taskPushMsg(long userId,JSONArray taskIds) throws Exception{
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			if(taskIds==null||taskIds.size()==0){return "没有要发布的任务";}
			//查询task数据，包含作业组leaderId
			List<Task> taskList = getTaskListWithLeader(conn,taskIds);
			
			List<Task> updatedTaskList = new ArrayList<Task>();
			List<Integer> updatedTaskIdList = new ArrayList<Integer>();
			int total = 0;
			//List<Integer> cmsTaskList=new ArrayList<Integer>();
			List<Integer> commontaskIds=new ArrayList<Integer>();
			List<Integer> commonBlockIds=new ArrayList<Integer>();
			//modify by songhe 记录有组ID的subtaskId调用子任务发布接口
			//月编子任务名称赋值List
			JSONArray subPushMsgIds = new JSONArray();

			//POI月编任务
			List<Task> poiMonthlyTask = new ArrayList<Task>();
			
			for(Task task:taskList){
//				//采集任务处理无任务POI和TIPS的批中线任务号操作
//				20170606取消流程：采集任务发布时，批中线任务ID。所有无任务转中线的入口，均为人工触发按钮
//				if(task.getType() == 0){
//					batchNoTaskMidData(conn, task);
//				}
				
//				if(task.getType() == 3){
//					//二代任务发布特殊处理
//					cmsTaskList.add(task.getTaskId());
//				}else{
				commontaskIds.add(task.getTaskId());
				commonBlockIds.add(task.getBlockId());
				updatedTaskList.add(task);
				updatedTaskIdList.add(task.getTaskId());
				total ++;
				//如果为POI月编任务
				if(task.getType() == 2){
					poiMonthlyTask.add(task);
				}
				if(task.getType()==0){//采集任务，workKind情报矢量或多源为1，则需自动创建情报矢量或多源采集子任
					if(task.getSubWorkKind(3)==1){
						createCollectSubtaskByTask(3, task);
					}
					if(task.getSubWorkKind(4)==1){
						createCollectSubtaskByTask(4, task);
					}
				}
				//}
			}
			if(commontaskIds.size()>0){
				//更新task状态
				TaskOperation.updateStatus(conn, commontaskIds,1);
				if(commonBlockIds.size() > 0){
					BlockService.getInstance().updateStatus(conn, commonBlockIds,3);
				}
				//发布消息
				taskPushMsg(conn,userId,updatedTaskList);
				//conn.commit();
			}
			//POI月编任务发布后，自动创建一个“POI专项”类型的子任务，状态为草稿
			if(poiMonthlyTask.size()>0){
				for(Task task:poiMonthlyTask){
					Subtask subtask = new Subtask();
					log.info("先创建质检月编子任务");
					//SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					subtask.setTaskId(task.getTaskId());
					//modify by songhe 
					//月编子任务名称赋值原则：快线调用SubtaskService.autoInforName
					subtask.setExeGroupId(task.getGroupId());
					//subtask = SubtaskService.getInstance().autoInforName(conn, subtask);
					//subtask.setName(name);//任务名称+_作业组					
					subtask.setGridIds(getGridMapByTaskId(task.getTaskId()));
					subtask.setPlanStartDate(task.getPlanStartDate());
					subtask.setPlanEndDate(task.getPlanEndDate());
					subtask.setStatus(2);//草稿
					subtask.setStage(2);
					subtask.setType(7);
					subtask.setIsQuality(1);

					JSONArray gridIds = TaskService.getInstance().getGridListByTaskId(conn,task.getTaskId());
					String wkt = GridUtils.grids2Wkt(gridIds);
					subtask.setGeometry(wkt);

					int qualitySubTaskId = SubtaskService.getInstance().createSubtaskWithSubtaskId(conn,subtask);
					//质检和常规月编子任务创建用同一个对象，只是修改了两者有区别的字段。
					log.info("再创建常规月编子任务");
					if(StringUtils.isBlank(subtask.getName())&&task.getBlockId()!=0){
						subtask.setName(task.getName()+"_"+task.getGroupName());//任务名称+_作业组
					}
					subtask.setIsQuality(0);
					subtask.setQualitySubtaskId(qualitySubTaskId);
					int subTaskId = SubtaskService.getInstance().createSubtaskWithSubtaskId(conn,subtask);
					SubtaskService.getInstance().updateQualityName(conn, qualitySubTaskId);
					//modify by songhe
					//若POI专项子任务有组id，则调用subtask/pushMsg的相关发布方法，将poi专项子任务发布
					//月编子任务名称赋值
					if(subtask.getExeGroupId() != 0){
						subPushMsgIds.add(subTaskId);
					}
				}
			}
//			if(cmsTaskList.size()>0){
//				//获取可发布的cms任务
//				List<Integer> pushCmsTask = TaskOperation.pushCmsTasks(conn, cmsTaskList);
//				erNum=pushCmsTask.size();
//				if(pushCmsTask!=null&&pushCmsTask.size()>0){
//					for(Integer taskId:pushCmsTask){
//						List<Map<String, Integer>> phaseList = queryTaskCmsProgress(taskId);
//						if(phaseList!=null&&phaseList.size()>0){continue;}
//						
//						Set<Integer> collectTaskSet = getCollectTaskIdsByTaskId(taskId);
//						Set<Integer> meshIdSet = new HashSet<Integer>();
//						
//						FccApi fccApi = (FccApi)ApplicationContextUtil.getBean("fccApi");
//						meshIdSet = fccApi.getTipsMeshIdSet(collectTaskSet);
//						log.info("获取tips全图幅"+meshIdSet.toString());
//						Set<Integer> gridIdList = getGridMapByTaskId(conn,taskId).keySet();
//						for(Integer gridId:gridIdList){
//							meshIdSet.add(gridId/100);
//						}
//						
//						JSONObject parameter = new JSONObject();
//						parameter.put("meshIds", meshIdSet);
//						
//						createCmsProgress(conn,taskId,1,parameter);
//						createCmsProgress(conn,taskId,2,parameter);
//						createCmsProgress(conn,taskId,3,parameter);
//						createCmsProgress(conn,taskId,4,parameter);
//						conn.commit();
//						
//						phaseList = queryTaskCmsProgress(taskId);
//						Map<Integer, Integer> phaseIdMap=new HashMap<Integer, Integer>();
//						for(Map<String, Integer> phaseTmp:phaseList){
//							phaseIdMap.put(phaseTmp.get("phase"),phaseTmp.get("phaseId"));
//						}
//						TaskCmsProgress returnProgress=day2month(conn, phaseIdMap.get(1));
//						updateCmsProgressStatus(conn, phaseIdMap.get(1), returnProgress.getStatus(), returnProgress.getMessage());
//						returnProgress=tips2Aumark(conn, phaseIdMap.get(2));
//						updateCmsProgressStatus(conn, phaseIdMap.get(2), returnProgress.getStatus(), returnProgress.getMessage());
//					}}
//				if(erNum==0){return "二代编辑任务发布失败，存在未关闭的采集任务";}
//				else{return "二代编辑任务发布进行中";}
//			}
			//modify by songhe 
			//有组ID的subTask调用子任务发布接口
			if(subPushMsgIds.size() > 0){
				SubtaskService.getInstance().pushMsg(conn, userId, subPushMsgIds);
			}
			return "任务发布成功" + total + "个，失败" + (taskIds.size()-total) + "个";
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("task消息发送失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}		
	}
	
	public void taskPushMsg(Connection conn,long userId,List<Task> updatedTaskList) throws Exception{
		try {
			//给作业组leader发送消息
			List<Object[]> msgContentList=new ArrayList<Object[]>();
			String msgTitle="task发布";
			for (Task task : updatedTaskList) {
				if(task.getGroupLeader()!=0){
					Object[] msgTmp=new Object[4];
					msgTmp[0]=task.getGroupLeader();//收信人
					msgTmp[1]=msgTitle;//消息头
					msgTmp[2]="发布task:"+task.getName()+",请关注";//消息内容
					//关联要素
					JSONObject msgParam = new JSONObject();
					msgParam.put("relateObject", "TASK");
					msgParam.put("relateObjectId", task.getTaskId());
					msgTmp[3]=msgParam.toString();//消息对象
					msgContentList.add(msgTmp);
				}
				if(msgContentList.size()>0){
					taskPushMsgByMsg(conn,msgContentList,userId);	
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new Exception("task消息发送失败，原因为:" + e.getMessage(), e);
		}		
	}
	
	
	/**
	 * @param conn
	 * @param taskIds
	 * @return 返回task基础信息及组leaderId
	 * @throws Exception 
	 */
	private List<Task> getTaskListWithLeader(Connection conn, JSONArray taskIds) throws Exception {
		try{
			QueryRunner run=new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT t.program_id,T.REGION_ID,T.TASK_ID,T.NAME,T.STATUS,T.TYPE,UG.GROUP_ID,UG.LEADER_ID,"
					+ "UG.GROUP_NAME,T.BLOCK_ID,T.PLAN_START_DATE,T.PLAN_END_DATE,t.work_kind");
			sb.append(" FROM TASK T,USER_GROUP UG");
			sb.append(" WHERE T.GROUP_ID = UG.GROUP_ID(+)");
			sb.append(" AND T.TASK_ID IN (" + StringUtils.join(taskIds.toArray(),",") + ")");
			String selectSql= sb.toString();

			log.info("getTaskListWithLeader sql:" + selectSql);
			ResultSetHandler<List<Task>> rsHandler = new ResultSetHandler<List<Task>>() {
				public List<Task> handle(ResultSet rs) throws SQLException {
					List<Task> taskList = new ArrayList<Task>();
					while (rs.next()) {
						Task task = new Task();
						task.setProgramId(rs.getInt("PROGRAM_ID"));
						task.setTaskId(rs.getInt("TASK_ID"));
						task.setName(rs.getString("NAME"));
						task.setStatus(rs.getInt("STATUS"));
						task.setType(rs.getInt("TYPE"));
						task.setGroupId(rs.getInt("GROUP_ID"));
						task.setGroupName(rs.getString("GROUP_NAME"));
						task.setGroupLeader(rs.getInt("LEADER_ID"));
						task.setBlockId(rs.getInt("BLOCK_ID"));
						task.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						task.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						task.setRegionId(rs.getInt("REGION_ID"));
						task.setWorkKind(rs.getString("work_kind"));
						taskList.add(task);
					}
					return taskList;
				}

			};
			
			return run.query(conn, selectSql, rsHandler);	
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}

	private void taskPushMsgByMsg(Connection conn,	List<Object[]> msgContentList, long userId) throws Exception {
		Object[][] msgList=new Object[msgContentList.size()][3];
		int num=0;
		UserInfo pushObj = UserInfoOperation.getUserInfoByUserId(conn, userId);
		for(Object[] msgContent:msgContentList){
			//if(Long.parseLong(msgContent[0].toString())==0){continue;}
			msgList[num]=msgContent;
			num+=1;
			//发送邮件
			String toMail = null;
			String mailTitle = null;
			String mailContent = null;
			//查询用户详情
			UserInfo userInfo = UserInfoOperation.getUserInfoByUserId(conn, Long.parseLong( msgContent[0].toString()));
			if(userInfo != null && userInfo.getUserEmail() != null){
				//判断邮箱格式
				String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
                Pattern regex = Pattern.compile(check);
                Matcher matcher = regex.matcher((CharSequence) userInfo.getUserEmail());
                if(matcher.matches()){
                	toMail = userInfo.getUserEmail();
                	mailTitle = (String) msgContent[1];
                	mailContent = (String) msgContent[2];
                	//发送邮件到消息队列
                	//SendEmail.sendEmail(toMail, mailTitle, mailContent);
                	EmailPublisher.publishMsg(toMail, mailTitle, mailContent);
                }
			}
			//发送消息到消息队列
			SysMsgPublisher.publishMsg((String)msgContent[1], (String)msgContent[2], userId, new long[]{Long.parseLong(msgContent[0].toString())}, 2, (String)msgContent[3], pushObj.getUserRealName());
		}
	}
	
	
	public String update(long userId,JSONObject json) throws Exception{
		Connection conn = null;
		int total=0;
		try{
			conn = DBConnector.getInstance().getManConnection();
			JSONArray workKindArray=null;
			if(json.containsKey("workKind")){
				workKindArray=json.getJSONArray("workKind");
				json.remove("workKind");
			}
			Task bean=(Task) JsonOperation.jsonToBean(json,Task.class);
			bean.setWorkKind(workKindArray);
			
			//获取旧任务信息
			Task oldTask = this.queryByTaskId(conn, bean.getTaskId());
			//采集任务 ,workKind外业采集或众包为1,调用组赋值方法/日编任务			
			if((oldTask.getType()==0&&bean.getGroupId()==0&&(bean.getSubWorkKind(1)==1||bean.getSubWorkKind(2)==1))||
					(oldTask.getType()==1&&bean.getGroupId()==0)){
				String adminCode = selectAdminCode(oldTask.getProgramId());
				int groupType=1;
				if(oldTask.getType()==1){groupType=2;}
				if(adminCode != null && !"".equals(adminCode)){
					UserGroup userGroup = UserGroupService.getInstance().getGroupByAminCode(adminCode, groupType);
					if(userGroup!=null){
						Integer userGroupID = userGroup.getGroupId();
						bean.setGroupId(userGroupID);}
				}
			}
			
			TaskOperation.updateTask(conn, bean);
			
			//状态status为开启时，参数workKind与库中workKind是否有变更，若情报矢量或多源由0变为1了，
			//则需自动创建情报矢量或多源子任务，即subtask里的work_Kind赋对应值
			if(oldTask.getStatus()==1&&oldTask.getType()==0){
				if(bean.getSubWorkKind(3)==1&&oldTask.getSubWorkKind(3)==0){
					log.info("任务修改，变更情报，需自动创建情报子任务");
					createCollectSubtaskByTask(3, bean);
				}
				if(bean.getSubWorkKind(4)==1&&oldTask.getSubWorkKind(4)==0){
					log.info("任务修改，变更多源，需自动创建多源子任务");
					createCollectSubtaskByTask(4, bean);
				}
			}
			
			//需要发消息的task列表		
			JSONArray openTaskIds=new JSONArray();			
			//Task task1 = getTaskListWithLeader(conn, taskIds);
			if(oldTask.getStatus()==1){
				openTaskIds.add(bean.getTaskId());
			}
			
			//常规采集任务修改了出品时间或批次，其他常规任务同步更新
			JSONObject json2 = new JSONObject();
			if((oldTask.getBlockId()!=0)&&(oldTask.getType()==0)){
				if(json.containsKey("lot")){
					json2.put("lot", json.getString("lot"));
				}
				if(json.containsKey("producePlanStartDate")){
					json2.put("producePlanStartDate", json.getString("producePlanStartDate"));
				}
				if(json.containsKey("producePlanEndDate")){
					json2.put("producePlanEndDate", json.getString("producePlanEndDate"));
				}
			}
			
			if(!json2.isEmpty()){
				List<Task> taskList = getLatestTaskListByBlockId(oldTask.getBlockId());
				for(Task task2:taskList){
					if((task2.getType()==1)||(task2.getType()==2)||(task2.getType()==3)){
						Task taskTemp = (Task) JsonOperation.jsonToBean(json2,Task.class);
						taskTemp.setTaskId(task2.getTaskId());
						TaskOperation.updateTask(conn, taskTemp);
						if(task2.getStatus()==1){
							openTaskIds.add(bean.getTaskId());
						}
					}
				}
			}
			
			List<Task> openTaskList = new ArrayList<Task>();
			if(openTaskIds!=null&&openTaskIds.size()>0){
				openTaskList = getTaskListWithLeader(conn, openTaskIds);
			}

			//发送消息
			try {
				List<Object[]> msgContentList=new ArrayList<Object[]>();
				String msgTitle="task发布";
				for (Task task : openTaskList) {
					if(task.getGroupLeader()!=0){
						Object[] msgTmp=new Object[4];
						msgTmp[0]=task.getGroupLeader();//收信人
						msgTmp[1]=msgTitle;//消息头
						msgTmp[2]="新增task:"+task.getName()+",请关注";//消息内容
						//关联要素
						JSONObject msgParam = new JSONObject();
						msgParam.put("relateObject", "TASK");
						msgParam.put("relateObjectId", task.getTaskId());
						msgTmp[3]=msgParam.toString();//消息对象
						msgContentList.add(msgTmp);
					}
					total++;
				}
				if(msgContentList.size()>0){
					taskPushMsgByMsg(conn,msgContentList,userId);	
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("task编辑消息发送失败,原因:"+e.getMessage(), e);
			}

			return "任务批量保存"+total+"个成功，0个失败";
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("保存失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 3情报矢量，4多源
	 * 快线：情报名称_发布时间_作业员_子任务ID
	 * 中线：任务名称_作业组
	 * @param num
	 * @throws Exception 
	 */
	private void createCollectSubtaskByTask(int num,Task task) throws Exception{
		int programType=1;
		if(task.getBlockId()==0){//情报任务
			programType=4;
		}
		//情报子任务
		if(num==3){
			log.info("创建情报子任务");
			Subtask subtask = new Subtask();
			if(programType==1){
				subtask.setName(task.getName());
			}
			//subtask.setExeGroupId(task.getGroupId());
			subtask.setGridIds(getGridMapByTaskId(task.getTaskId()));
			subtask.setPlanStartDate(task.getPlanStartDate());
			subtask.setPlanEndDate(task.getPlanEndDate());
			subtask.setStatus(2);//草稿
			subtask.setStage(0);
			subtask.setType(2);
			subtask.setWorkKind(3);
			subtask.setTaskId(task.getTaskId());
			JSONArray gridIds = TaskService.getInstance().getGridListByTaskId(task.getTaskId());
			String wkt = GridUtils.grids2Wkt(gridIds);
			subtask.setGeometry(wkt);
			SubtaskService.getInstance().createSubtask(subtask);
		}
		//多源子任务
		if(num==4){
			log.info("创建多源子任务");
			Subtask subtask = new Subtask();
			String adminCode = selectAdminCode(task.getProgramId());
			//* 快线：情报名称_发布时间_作业员_子任务ID
			// * 中线：任务名称_作业组
			if(adminCode != null && !"".equals(adminCode)){
				UserGroup userGroup = UserGroupService.getInstance().getGroupByAminCode(adminCode, 5);
				if(userGroup!=null){
					subtask.setExeGroupId(userGroup.getGroupId());
					if(programType==1){
						subtask.setName(task.getName()+"_"+userGroup.getGroupName());
					}//任务名称+_作业组
				}
			}
			//subtask.setExeGroupId(task.getGroupId());
			subtask.setGridIds(getGridMapByTaskId(task.getTaskId()));
			subtask.setPlanStartDate(task.getPlanStartDate());
			subtask.setPlanEndDate(task.getPlanEndDate());
			subtask.setStatus(2);//草稿
			subtask.setStage(0);
			subtask.setType(0);
			subtask.setWorkKind(4);
			subtask.setTaskId(task.getTaskId());
			JSONArray gridIds = TaskService.getInstance().getGridListByTaskId(task.getTaskId());
			String wkt = GridUtils.grids2Wkt(gridIds);
			subtask.setGeometry(wkt);

			SubtaskService.getInstance().createSubtask(subtask);
		}
	}
	
	/**
	 * @param blockId
	 * @return 返回block上创建的task基础信息（id,name,status,type）
	 * @throws Exception 
	 */
	private List<Task> getLatestTaskListByBlockId(Integer blockId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run=new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT T.TASK_ID,T.NAME,T.STATUS,T.TYPE");
			sb.append(" FROM TASK T,BLOCK B");
			sb.append(" WHERE T.BLOCK_ID = B.BLOCK_ID");
			sb.append(" AND T.LATEST = 1");
			sb.append(" AND B.BLOCK_ID = " + blockId);
			String selectSql= sb.toString();

			ResultSetHandler<List<Task>> rsHandler = new ResultSetHandler<List<Task>>() {
				public List<Task> handle(ResultSet rs) throws SQLException {
					List<Task> taskList = new ArrayList<Task>();
					while (rs.next()) {
						Task task = new Task();
						task.setTaskId(rs.getInt("TASK_ID"));
						task.setName(rs.getString("NAME"));
						task.setStatus(rs.getInt("STATUS"));
						task.setType(rs.getInt("TYPE"));
						
						taskList.add(task);
					}
					return taskList;
				}

			};
			
			return run.query(conn, selectSql, rsHandler);	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}



//	/*任务创建/编辑/关闭
//	 * 1.所有生管角色
//	 * 2.分配的月编作业组组长
//	 * 任务:XXX(任务名称)内容发生变更，请关注*/
//	public void taskPushMsg(Connection conn,String msgTitle,List<Map<String, Object>> msgContentList, List<Long> groupIdList, long pushUser) throws Exception {
//		//查询所有生管角色
//		String userSql="SELECT DISTINCT M.USER_ID, I.USER_REAL_NAME,I.USER_EMAIL"
//				+ "  FROM ROLE_USER_MAPPING M, USER_INFO I"
//				+ " WHERE M.ROLE_ID = 3"
//				+ "   AND M.USER_ID = I.USER_ID";
//		Map<Long, UserInfo> userIdList=UserInfoOperation.getUserInfosBySql(conn, userSql);
//		for(Long userId:userIdList.keySet()){
//			String pushUserName =userIdList.get(userId).getUserRealName();
//			for(Map<String, Object> map:msgContentList){
//				//发送消息到消息队列
//				String msgContent = (String) map.get("msgContent");
//				String msgParam = (String) map.get("msgParam");
//				SysMsgPublisher.publishMsg(msgTitle, msgContent, pushUser, new long[]{userId}, 2, msgParam, pushUserName);
//			}
//		}
//		//查询分配的作业组组长
//		Map<Long, UserInfo> leaderIdByGroupId = UserInfoOperation.getLeaderIdByGroupId(conn, groupIdList);
//		//分别发送给对应的日编/采集/月编组长
//		for(Map<String, Object> map:msgContentList){
//			//发送消息到消息队列
//			String msgContent = (String) map.get("msgContent");
//			String msgParam = (String) map.get("msgParam");
//			List<Long> groupIds=(List<Long>) map.get("taskGroupIds");
//			for(Long groupId:groupIds){
//				SysMsgPublisher.publishMsg(msgTitle, msgContent, pushUser,new long[]{Long.valueOf(leaderIdByGroupId.get(groupId).getUserId())},
//						2, msgParam,leaderIdByGroupId.get(groupId).getUserRealName());
//			}
//		}
//		
//		//发送邮件
//		String toMail = null;
//		String mailTitle = null;
//		String mailContent = null;
//		//查询用户详情
//		for (Long userId : userIdList.keySet()) {
//			UserInfo userInfo = userIdList.get(userId);
//			if(userInfo.getUserEmail()!= null&&!userInfo.getUserEmail().isEmpty()){
//				for (Map<String, Object> map : msgContentList) {
//					//判断邮箱格式
//					String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
//	                Pattern regex = Pattern.compile(check);
//	                Matcher matcher = regex.matcher((CharSequence) userInfo.getUserEmail());
//	                if(matcher.matches()){
//	                	toMail = userInfo.getUserEmail();
//	                	mailTitle = msgTitle;
//	                	mailContent = (String) map.get("msgContent");
//	                	//发送邮件到消息队列
//	                	//SendEmail.sendEmail(toMail, mailTitle, mailContent);
//	                	EmailPublisher.publishMsg(toMail, mailTitle, mailContent);
//	                }
//				}
//			}
//		}
//		
//		//分别发送给对应的日编/采集/月编组长
//		for(Map<String, Object> map:msgContentList){
//			//发送消息到消息队列
//			String msgContent = (String) map.get("msgContent");
//			String msgParam = (String) map.get("msgParam");
//			List<Long> groupIds=(List<Long>) map.get("taskGroupIds");
//			for(Long groupId:groupIds){
//				UserInfo userInfo = leaderIdByGroupId.get(groupId);
//				//判断邮箱格式
//				String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
//                Pattern regex = Pattern.compile(check);
//                Matcher matcher = regex.matcher((CharSequence) userInfo.getUserEmail());
//                if(matcher.matches()){
//                	toMail = userInfo.getUserEmail();
//                	mailTitle = msgTitle;
//                	mailContent = (String) map.get("msgContent");
//                	//发送邮件到消息队列
//                	//SendEmail.sendEmail(toMail, mailTitle, mailContent);
//                	EmailPublisher.publishMsg(toMail, mailTitle, mailContent);
//                }
//			}
//		}
//	}
	
//	/*任务创建/编辑/关闭
//	 * 1.所有生管角色
//	 * 2.分配的月编作业组组长
//	 * 任务:XXX(任务名称)内容发生变更，请关注*/
//	public void taskPushMsg(Connection conn,String msgTitle,List<Map<String, Object>> msgContentList, List<Long> groupIdList, long pushUser) throws Exception {
//		//查询所有生管角色
//		String userSql="SELECT DISTINCT M.USER_ID, I.USER_REAL_NAME,I.USER_EMAIL"
//				+ "  FROM ROLE_USER_MAPPING M, USER_INFO I"
//				+ " WHERE M.ROLE_ID = 3"
//				+ "   AND M.USER_ID = I.USER_ID";
//		Map<Long, UserInfo> userIdList=UserInfoOperation.getUserInfosBySql(conn, userSql);
//		for(Long userId:userIdList.keySet()){
//			String pushUserName =userIdList.get(userId).getUserRealName();
//			for(Map<String, Object> map:msgContentList){
//				//发送消息到消息队列
//				String msgContent = (String) map.get("msgContent");
//				String msgParam = (String) map.get("msgParam");
//				SysMsgPublisher.publishMsg(msgTitle, msgContent, pushUser, new long[]{userId}, 2, msgParam, pushUserName);
//			}
//		}
//		//查询分配的作业组组长
//		Map<Long, UserInfo> leaderIdByGroupId = UserInfoOperation.getLeaderIdByGroupId(conn, groupIdList);
//		//分别发送给对应的日编/采集/月编组长
//		for(Map<String, Object> map:msgContentList){
//			//发送消息到消息队列
//			String msgContent = (String) map.get("msgContent");
//			String msgParam = (String) map.get("msgParam");
//			List<Long> groupIds=(List<Long>) map.get("taskGroupIds");
//			for(Long groupId:groupIds){
//				SysMsgPublisher.publishMsg(msgTitle, msgContent, pushUser,new long[]{Long.valueOf(leaderIdByGroupId.get(groupId).getUserId())},
//						2, msgParam,leaderIdByGroupId.get(groupId).getUserRealName());
//			}
//		}
//		
//		//发送邮件
//		String toMail = null;
//		String mailTitle = null;
//		String mailContent = null;
//		//查询用户详情
//		for (Long userId : userIdList.keySet()) {
//			UserInfo userInfo = userIdList.get(userId);
//			if(userInfo.getUserEmail()!= null&&!userInfo.getUserEmail().isEmpty()){
//				for (Map<String, Object> map : msgContentList) {
//					//判断邮箱格式
//					String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
//	                Pattern regex = Pattern.compile(check);
//	                Matcher matcher = regex.matcher((CharSequence) userInfo.getUserEmail());
//	                if(matcher.matches()){
//	                	toMail = userInfo.getUserEmail();
//	                	mailTitle = msgTitle;
//	                	mailContent = (String) map.get("msgContent");
//	                	//发送邮件到消息队列
//	                	//SendEmail.sendEmail(toMail, mailTitle, mailContent);
//	                	EmailPublisher.publishMsg(toMail, mailTitle, mailContent);
//	                }
//				}
//			}
//		}
//		
//		//分别发送给对应的日编/采集/月编组长
//		for(Map<String, Object> map:msgContentList){
//			//发送消息到消息队列
//			String msgContent = (String) map.get("msgContent");
//			String msgParam = (String) map.get("msgParam");
//			List<Long> groupIds=(List<Long>) map.get("taskGroupIds");
//			for(Long groupId:groupIds){
//				UserInfo userInfo = leaderIdByGroupId.get(groupId);
//				//判断邮箱格式
//				String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
//                Pattern regex = Pattern.compile(check);
//                Matcher matcher = regex.matcher((CharSequence) userInfo.getUserEmail());
//                if(matcher.matches()){
//                	toMail = userInfo.getUserEmail();
//                	mailTitle = msgTitle;
//                	mailContent = (String) map.get("msgContent");
//                	//发送邮件到消息队列
//                	//SendEmail.sendEmail(toMail, mailTitle, mailContent);
//                	EmailPublisher.publishMsg(toMail, mailTitle, mailContent);
//                }
//			}
//		}
//	}
//	
//	public Page commonList(Connection conn,int planStatus, JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize)throws Exception{
//		//常规未发布
//		Page page = new Page();
//		if(planStatus==1){
//			page=TaskOperation.getCommonUnPushListSnapshot(conn,conditionJson,currentPageNum,pageSize);
//		}else if(planStatus==2){
//			//常规已发布
//			page=TaskOperation.getCommonPushListSnapshot(conn,conditionJson,currentPageNum,pageSize);
//		}else if(planStatus==3){
//			//常规已完成
//			page=TaskOperation.getCommonOverListSnapshot(conn,conditionJson,currentPageNum,pageSize);
//		}else if(planStatus==4){
//			//常规已关闭
//			page=TaskOperation.getCommonCloseListSnapshot(conn,conditionJson,currentPageNum,pageSize);
//		}
//		return page;
//	}
//	
//	public Page inforList(Connection conn,int planStatus, JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize)throws Exception{
//		//情报未发布
//		Page page = new Page();
//		if(planStatus==1){
//			page=TaskOperation.getInforUnPushListSnapshot(conn,conditionJson,currentPageNum,pageSize);
//		}else if(planStatus==2){
//			//情报已发布
//			page=TaskOperation.getInforPushListSnapshot(conn,conditionJson,currentPageNum,pageSize);
//		}else if(planStatus==3){
//			//情报已完成
//			page=TaskOperation.getInforOverListSnapshot(conn,conditionJson,currentPageNum,pageSize);
//		}else if(planStatus==4){
//			//情报已关闭
//			page=TaskOperation.getInforCloseListSnapshot(conn,conditionJson,currentPageNum,pageSize);
//		}
//		return page;
//	}
		
//	public Page list(int taskType, int planStatus, JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize,int snapshot)throws Exception{
//		Connection conn = null;
//		try{
//			conn = DBConnector.getInstance().getManConnection();
//			if (snapshot==1){
//				if(taskType==4){
//					//情报任务查询列表
//					return this.inforList(conn,planStatus, conditionJson, orderJson, currentPageNum, pageSize);
//				}else{
//					//常规任务查询列表
//					return this.commonList(conn,planStatus, conditionJson, orderJson, currentPageNum, pageSize);
//				}
//			}else{
//				Page page = TaskOperation.getListIntegrate(conn,conditionJson,orderJson,currentPageNum,pageSize);
//				return page;
//			}
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
//		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//	}
	

	public Page list(JSONObject condition,int curPageNum,int pageSize)throws Exception{
		if(condition.containsKey("cityId")){
			return listByCity(condition,curPageNum,pageSize);
		}
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			//查询条件
			String conditionSql = "";
			Iterator<?> conditionKeys = condition.keys();		
			List<String> progressList = new ArrayList<String>();
			while (conditionKeys.hasNext()) {
				String key = (String) conditionKeys.next();
				//查询条件
				if ("programId".equals(key)) {
					conditionSql+=" AND TASK_LIST.PROGRAM_ID="+condition.getInt(key);
				}
				if ("type".equals(key)) {
					conditionSql+=" AND TASK_LIST.TYPE ="+condition.getInt(key);
				}
				//按组查询主要应用场景：采/日/月角色登陆管理平台用，只返回开启任务
				if ("groupId".equals(key)) {
					conditionSql+=" AND TASK_LIST.STATUS in (0,1) AND TASK_LIST.GROUP_ID ="+condition.getInt(key);
				}
				if("planStatus".equals(key)){
					int planStatus = condition.getInt(key);
					//未完成：开启1>待分配2
					if(planStatus==2){
						conditionSql+=" AND TASK_LIST.ORDER_STATUS IN (1,2)";
					}
					//已完成：100%(已完成)5>已关闭6
					else if(planStatus == 3){
						conditionSql+=" AND TASK_LIST.ORDER_STATUS IN (5,6)";
					}
				}
				//任务名称模糊查询
				if ("name".equals(key)) {	
					conditionSql+=" AND (TASK_LIST.NAME LIKE '%" + condition.getString(key) +"%'"
							+ " OR TASK_LIST.BLOCK_NAME LIKE '%" + condition.getString(key) +"%')";
				}
				//筛选条件
				//"progress":[1,3] //进度。
				//1采集正常，2采集异常，3采集完成，4日编正常，5日编异常，6日编完成， 7月编正常，8月编异常，9月编完成，10未规划，11草稿, 12已完成，13已关闭，
				//14按时完成，15提前完成，16逾期完成,17采集待分配，18日编待分配，19月编待分配
				if ("progress".equals(key)){
					JSONArray progress = condition.getJSONArray(key);
					if(progress.isEmpty()){
						continue;
					}
					
					for(Object i:progress){
						int tmp=(int) i;
						//1采集正常，2采集异常，3采集完成
						if(tmp==1){progressList.add(" (TASK_LIST.PROGRESS = 1 AND TASK_LIST.TYPE=0 AND TASK_LIST.STATUS=1) ");}
						if(tmp==2){progressList.add(" (TASK_LIST.PROGRESS = 2 AND TASK_LIST.TYPE=0 AND TASK_LIST.STATUS=1) ");}
						if(tmp==3){progressList.add(" (TASK_LIST.STATUS = 1 AND TASK_LIST.ORDER_STATUS = 5 AND TASK_LIST.TYPE=0)");}
						//4日编正常，5日编异常，6日编完成
						if(tmp==4){progressList.add(" (TASK_LIST.PROGRESS = 1 AND TASK_LIST.TYPE=1  AND TASK_LIST.STATUS=1) ");}
						if(tmp==5){progressList.add(" (TASK_LIST.PROGRESS = 2 AND TASK_LIST.TYPE=1 AND TASK_LIST.STATUS=1) ");}
						if(tmp==6){progressList.add(" (TASK_LIST.STATUS = 1 AND TASK_LIST.ORDER_STATUS = 5 AND TASK_LIST.TYPE=1)");}
						//7月编正常，8月编异常，9月编完成
						if(tmp==7){progressList.add(" (TASK_LIST.PROGRESS = 1 AND TASK_LIST.TYPE in (2,3)  AND TASK_LIST.STATUS=1) ");}
						if(tmp==8){progressList.add(" (TASK_LIST.PROGRESS = 2 AND TASK_LIST.TYPE in (2,3) AND TASK_LIST.STATUS=1) ");}
						if(tmp==9){progressList.add(" (TASK_LIST.STATUS = 1 AND TASK_LIST.ORDER_STATUS = 5 AND TASK_LIST.TYPE=2)");}
						//10未规划，11草稿, 12已完成，13已关闭
						if(tmp==10){progressList.add(" TASK_LIST.PLAN_STATUS = 0");}
						if(tmp==11){progressList.add(" TASK_LIST.STATUS = 2 ");}
						if(tmp==12){progressList.add(" (TASK_LIST.STATUS = 1 AND TASK_LIST.ORDER_STATUS = 5)");}
						if(tmp==13){progressList.add(" TASK_LIST.STATUS = 0 ");}
						//14按时完成，15提前完成，16逾期完成
						if(tmp==14){
							progressList.add("TASK_LIST.DIFF_DATE = 0");
						}
						if(tmp==15){
							progressList.add("TASK_LIST.DIFF_DATE > 0");
						}
						if(tmp==16){
							progressList.add("TASK_LIST.DIFF_DATE < 0");
						}
						if(tmp==17){
							progressList.add("(TASK_LIST.order_status=2 AND TASK_LIST.TYPE=0  AND TASK_LIST.STATUS=1) ");
						}else if(tmp==18){
							progressList.add("(TASK_LIST.order_status=2 AND TASK_LIST.TYPE=1  AND TASK_LIST.STATUS=1) ");
						}else if(tmp==19){
							progressList.add("(TASK_LIST.order_status=2 AND TASK_LIST.TYPE in (2,3)  AND TASK_LIST.STATUS=1) ");
						}
					}
				}
			}
			
			if(!progressList.isEmpty()){
				String tempSql = StringUtils.join(progressList," OR ");
				conditionSql += " AND (" + tempSql + ")";
			}
			QueryRunner run = new QueryRunner();
			long pageStartNum = (curPageNum - 1) * pageSize + 1;
			long pageEndNum = curPageNum * pageSize;
			
			StringBuilder sb = new StringBuilder();
			
			/*• 记录默认排序原则：
			 * ①根据状态排序：开启>待分配>草稿>未规划>100%(已完成)>已关闭
			 * 用order_status来表示这个排序的先后顺序。分别是开启1>待分配2>草稿3>未规划4>100%(已完成)5>已关闭6
			 * ②相同状态中根据剩余工期排序，逾期>0天>剩余/提前
			 * ③开启状态相同剩余工期，根据完成度排序，完成度高>完成度低；其它状态，根据名称
			 */
			sb.append("WITH FINAL_TABLE AS ( ");
			sb.append("SELECT TASK_LIST.* FROM (");
			sb.append("SELECT TASK_LIST1.*,");
			sb.append("               CASE");
			sb.append("                 WHEN (TASK_LIST1.STATUS = 2) THEN");
			sb.append("                  3");
			sb.append("                 WHEN (TASK_LIST1.STATUS = 0) THEN");
			sb.append("                  6");
			sb.append("                 WHEN (TASK_LIST1.STATUS = 4) THEN");
			sb.append("                  4");
			sb.append("                 WHEN (TASK_LIST1.STATUS = 1) THEN");
			sb.append("                  CASE");
			sb.append("                    WHEN (TASK_LIST1.SUBTASK_NUM = 0) THEN");
			sb.append("                     2");
			sb.append("                    ELSE");
			sb.append("                     CASE");
			sb.append("                       WHEN (TASK_LIST1.SUBTASK_NUM = TASK_LIST1.SUBTASK_NUM_CLOSED) THEN");
			sb.append("                        5");
			sb.append("                       ELSE");
			sb.append("                        1");
			sb.append("                     END");
			sb.append("                  END");
			sb.append("               END ORDER_STATUS");
			sb.append("          FROM (SELECT DISTINCT P.PROGRAM_ID,p.type programType,");
			sb.append("                       NVL(T.TASK_ID, 0) TASK_ID,");
			sb.append("                       T.NAME,");
			sb.append("                       NVL(T.STATUS, 4) STATUS,");
			sb.append("                       T.TYPE,");
			sb.append("                       T.GROUP_ID,");
			sb.append("                       UG.GROUP_NAME,");
			sb.append("                       T.PLAN_START_DATE,");
			sb.append("                       T.PLAN_END_DATE,");
			sb.append("                       T.ROAD_PLAN_TOTAL,");
			sb.append("                       T.POI_PLAN_TOTAL,");
			//sb.append("                       T.DATA_PLAN_STATUS,");
			sb.append("                       NVL(FSOT.PROGRESS, 1) PROGRESS,");
			sb.append("                       NVL(FSOT.PERCENT, 0) PERCENT,");
			sb.append("                       NVL(FSOT.DIFF_DATE, 0) DIFF_DATE,");
			sb.append("                       NVL(FSOT.NOTASKDATA_POI_NUM, 0) NOTASKDATA_POI_NUM,");
			sb.append("                       NVL(FSOT.NOTASKDATA_TIPS_NUM, 0) NOTASKDATA_TIPS_NUM,");
			sb.append("                       NVL(FSOT.CONVERT_FLAG, 0) CONVERT_FLAG,");
			sb.append("                       B.BLOCK_ID,");
			sb.append("	                      B.BLOCK_NAME,");
			sb.append("                       B.PLAN_STATUS,");
			sb.append("                       (SELECT COUNT(1)");
			sb.append("                          FROM SUBTASK ST");
			sb.append("                         WHERE ST.TASK_ID = T.TASK_ID ) SUBTASK_NUM,");
			sb.append("                       (SELECT COUNT(1)");
			sb.append("                          FROM SUBTASK ST");
			sb.append("                         WHERE ST.TASK_ID = T.TASK_ID");
			sb.append("                           AND ST.STATUS = 0 ) SUBTASK_NUM_CLOSED,");
//			sb.append("                      NVL((SELECT J.STATUS ");
//			sb.append("         FROM JOB_RELATION JR,JOB J WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=3 "
//					+ "AND J.LATEST=1 AND JR.ITEM_ID=T.TASK_ID AND JR.ITEM_TYPE=2 ),-1) NOTASK2MID,");
			
			sb.append("                      NVL((SELECT J.STATUS ");
			sb.append("         FROM JOB_RELATION JR,JOB J WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=3 AND J.LATEST=1 "
					+ "AND JR.ITEM_ID=T.TASK_ID AND JR.ITEM_TYPE=2 ),-1) other2medium_Status,");
			
			sb.append("                      NVL((SELECT J.STATUS ");
			sb.append("         FROM JOB_RELATION JR,JOB J WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=1 "
					+ "AND J.LATEST=1 AND JR.ITEM_ID=T.TASK_ID AND JR.ITEM_TYPE=2 ),-1) TISP2MARK");
			sb.append("                  FROM BLOCK B, PROGRAM P, TASK T, FM_STAT_OVERVIEW_TASK FSOT,USER_GROUP UG");
			sb.append("                 WHERE T.BLOCK_ID = B.BLOCK_ID");
			sb.append("                   AND T.TASK_ID = FSOT.TASK_ID(+)");
			//sb.append("                   AND T.latest=1");
			sb.append("                   AND P.CITY_ID = B.CITY_ID");
			sb.append("                   AND UG.GROUP_ID(+) = T.GROUP_ID");
			sb.append("	             AND T.PROGRAM_ID = P.PROGRAM_ID");
			sb.append("	             AND p.latest=1");
			sb.append("	             AND P.TYPE = 1");
			sb.append("	          UNION");
			sb.append("	          SELECT DISTINCT P.PROGRAM_ID,p.type programType,");
			sb.append("	                          0             TASK_ID,");
			sb.append("	                          NULL          NAME,");
			sb.append("	                          4             STATUS,");
			sb.append("	                          NULL          TYPE,");
			sb.append("	                          NULL          GROUP_ID,");
			sb.append("                           NULL          GROUP_NAME,");
			sb.append("	                          NULL          PLAN_START_DATE,");
			sb.append("	                          NULL          PLAN_END_DATE,");
			sb.append("	                          NULL          ROAD_PLAN_TOTAL,");
			sb.append("	                          NULL          POI_PLAN_TOTAL,");
			//sb.append("	                          NULL          DATA_PLAN_STATUS,");
			sb.append("	                          1             PROGRESS,");
			sb.append("	                          0             PERCENT,");
			sb.append("	                          0             DIFF_DATE,");
			sb.append("                       0 NOTASKDATA_POI_NUM,");
			sb.append("                       0 NOTASKDATA_TIPS_NUM,"
					+ "0 CONVERT_FLAG,");
			sb.append("	                          B.BLOCK_ID,");
			sb.append("	                          B.BLOCK_NAME,");
			sb.append("	                          B.PLAN_STATUS,");
			sb.append("	                          0             SUBTASK_NUM,");
			sb.append("	                          0             SUBTASK_NUM_CLOSED,-1 other2medium_Status,");
			//sb.append("	                          -1 NOTASK2MID,");
			sb.append("	                          -1 tips2mark_status");
			sb.append("	            FROM BLOCK B, PROGRAM P");
			sb.append("	           WHERE P.CITY_ID = B.CITY_ID");
			sb.append("	        	 AND P.LATEST = 1");
			sb.append("	        	 AND P.TYPE = 1");
			sb.append("	             AND NOT EXISTS (SELECT 1");
			sb.append("	                    FROM TASK T");
			sb.append("	                   WHERE T.BLOCK_ID = B.BLOCK_ID");
			sb.append("	                     AND T.LATEST = 1)");
			sb.append("	          UNION");
			sb.append("           SELECT DISTINCT P.PROGRAM_ID,p.type programType,");
			sb.append("                       NVL(T.TASK_ID, 0) TASK_ID,");
			sb.append("                       T.NAME,");
			sb.append("                       NVL(T.STATUS, 4) STATUS,");
			sb.append("                       T.TYPE,");
			sb.append("                       T.GROUP_ID,");
			sb.append("                       UG.GROUP_NAME,");
			sb.append("                       T.PLAN_START_DATE,");
			sb.append("                       T.PLAN_END_DATE,");
			sb.append("                       T.ROAD_PLAN_TOTAL,");
			sb.append("                       T.POI_PLAN_TOTAL,");
			//sb.append("                       T.DATA_PLAN_STATUS,");
			sb.append("                       NVL(FSOT.PROGRESS, 1) PROGRESS,");
			sb.append("                       NVL(FSOT.PERCENT, 0) PERCENT,");
			sb.append("                       NVL(FSOT.DIFF_DATE, 0) DIFF_DATE,");
			sb.append("                       NVL(FSOT.NOTASKDATA_POI_NUM, 0) NOTASKDATA_POI_NUM,");
			sb.append("                       NVL(FSOT.NOTASKDATA_TIPS_NUM, 0) NOTASKDATA_TIPS_NUM,");
			sb.append("                       NVL(FSOT.CONVERT_FLAG, 0) CONVERT_FLAG,");
			sb.append("                       0 BLOCK_ID,");
			sb.append("	                      '' BLOCK_NAME,");
			sb.append("                       1 PLAN_STATUS,");
			sb.append("                       (SELECT COUNT(1)");
			sb.append("                          FROM SUBTASK ST");
			sb.append("                         WHERE ST.TASK_ID = T.TASK_ID ) SUBTASK_NUM,");
			sb.append("                       (SELECT COUNT(1)");
			sb.append("                          FROM SUBTASK ST");
			sb.append("                         WHERE ST.TASK_ID = T.TASK_ID");
			sb.append("                           AND ST.STATUS = 0 ) SUBTASK_NUM_CLOSED,");
//			sb.append("                      nvl((select tpt.status"
//					+ "          from (select * from task_progress tp where tp.phase=1 order by create_date desc) tpt"
//					+ "         where tpt.task_id = t.task_id"
//					+ "           and rownum = 1),-1) other2medium_Status,");
			sb.append("                      NVL((SELECT J.STATUS ");
			sb.append("         FROM JOB_RELATION JR,JOB J WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=3 AND J.LATEST=1 "
					+ "AND JR.ITEM_ID=T.TASK_ID AND JR.ITEM_TYPE=2 ),-1) other2medium_Status,");
			//sb.append("	                          -1 NOTASK2MID,");
			sb.append("                      NVL((SELECT J.STATUS ");
			sb.append("         FROM JOB_RELATION JR,JOB J WHERE J.JOB_ID=JR.JOB_ID AND J.TYPE=1 AND J.LATEST=1 "
					+ "AND JR.ITEM_ID=T.TASK_ID AND JR.ITEM_TYPE=2 ),-1) tips2mark_STATUS");
			sb.append("                  FROM PROGRAM P, TASK T, FM_STAT_OVERVIEW_TASK FSOT,USER_GROUP UG");
			sb.append("                 WHERE T.TASK_ID = FSOT.TASK_ID(+)");
			sb.append("                   AND UG.GROUP_ID(+) = T.GROUP_ID");
			//sb.append("                   AND t.latest=1");
			sb.append("                   AND p.latest=1");
			sb.append("	             AND T.PROGRAM_ID = P.PROGRAM_ID");
			sb.append("	             AND P.TYPE = 4) TASK_LIST1");
			sb.append("	             ) TASK_LIST");
			sb.append(" WHERE 1=1 ");
			sb.append(conditionSql);
			sb.append(" ORDER BY ORDER_STATUS ASC ,DIFF_DATE DESC, PERCENT DESC");
			sb.append(")");
			sb.append(" SELECT /*+FIRST_ROWS ORDERED*/");
			sb.append(" TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM");
			sb.append("  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_");
			sb.append(" FROM FINAL_TABLE");
			sb.append(" WHERE ROWNUM <= "+pageEndNum+") TT");
			sb.append(" WHERE TT.ROWNUM_ >= "+pageStartNum);

			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
					Page page = new Page();
				    int totalCount = 0;
				    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					while (rs.next()) {
						HashMap<Object,Object> task = new HashMap<Object,Object>();
						task.put("taskId", rs.getInt("TASK_ID"));
						if(rs.getString("NAME")==null){
							task.put("taskName","");
						}else{
							task.put("taskName", rs.getString("NAME"));
						}
						task.put("blockId", rs.getInt("BLOCK_ID"));
						if(rs.getString("BLOCK_NAME")==null){
							task.put("blockName","");
						}else{
							task.put("blockName", rs.getString("BLOCK_NAME"));
						}
						task.put("status", rs.getInt("STATUS"));
						task.put("type", rs.getInt("TYPE"));
						
						task.put("percent", rs.getInt("PERCENT"));
						task.put("diffDate", rs.getInt("DIFF_DATE"));
						task.put("progress", rs.getInt("PROGRESS"));	
						
						int type = rs.getInt("TYPE");
						int status = rs.getInt("STATUS");

						JSONArray jobs = new JSONArray();
						int tisp2markStatus = rs.getInt("TISP2MARK");
						if(tisp2markStatus!=-1){
							JSONObject job = new JSONObject();
							job.put("status",tisp2markStatus);
							job.put("type", JobType.TiPS2MARK.value());
							jobs.add(job);
						}else {
							//关闭的采集任务可执行tips转mark
							if (status == 0 && type == 0) {
								JSONObject job = new JSONObject();
								job.put("status",0);
								job.put("type", JobType.TiPS2MARK.value());
								jobs.add(job);
							}
						}						
						
						//other2mediumJobStatus 1有无任务数据，需要转换；0没有无任务数据需要转换；2无任务转换进行中
						int other2mediumStatus=rs.getInt("other2medium_Status");												
						//采集，中线，开启状态的任务才可能有无任务转中，其他任务没有此按钮
						if(status==1&&rs.getInt("BLOCK_ID")!=0&&type==0){
							int other2mediumJobStatus=0;
							if(tisp2markStatus!=-1){
								other2mediumJobStatus=other2mediumStatus;
							}else{
								other2mediumJobStatus=0;
							}
							JSONObject job = new JSONObject();
							job.put("status",other2mediumJobStatus);
							job.put("type", JobType.NOTASK2MID.value());
							jobs.add(job);
						}
						
						task.put("jobs",jobs);
						
						task.put("groupId", rs.getInt("GROUP_ID"));
						if(rs.getString("GROUP_NAME")==null){
							task.put("groupName","");
						}else{
							task.put("groupName", rs.getString("GROUP_NAME"));
						}
						Timestamp planStartDate = rs.getTimestamp("PLAN_START_DATE");
						Timestamp planEndDate = rs.getTimestamp("PLAN_END_DATE");
						if(planStartDate != null){
							task.put("planStartDate", df.format(planStartDate));
						}else {task.put("planStartDate", "");}
						if(planEndDate != null){
							task.put("planEndDate",df.format(planEndDate));
						}else{task.put("planEndDate", "");}
						
						task.put("roadPlanTotal", rs.getFloat("ROAD_PLAN_TOTAL"));
						task.put("poiPlanTotal", rs.getInt("POI_PLAN_TOTAL"));
						//task.put("dataPlanStatus", rs.getInt("DATA_PLAN_STATUS"));
						task.put("orderStatus", rs.getInt("ORDER_STATUS"));
						task.put("programType", rs.getInt("programType"));
						totalCount=rs.getInt("TOTAL_RECORD_NUM");
						list.add(task);
					}					
					page.setTotalCount(totalCount);
					page.setResult(list);
					return page;
				}

			};
			
			log.info("task list sql:" + sb.toString());
			Page page= run.query(conn, sb.toString(), rsHandler);
			page.setPageNum(curPageNum);
		    page.setPageSize(pageSize);
		    return page;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
//	/**
//	 * 判断是否有无任务数据方法
//	 * 默认无
//	 * 这里先注释掉吧
//	 * */
//	public int noTaskData(HashMap<String,Integer> noTaskMap){
//		int  hasData = 0;
//		if(noTaskMap.containsKey("poi") && noTaskMap.containsKey("tips")){
//			int poiCount = noTaskMap.get("poi");
//			int tipsCount = noTaskMap.get("tips");
//			if(poiCount > 0 || tipsCount > 0){
//				hasData = 1;
//			}
//		}
//		return hasData;
//	}
	
	public Page listByCity(JSONObject condition,int curPageNum,int pageSize)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			//查询条件
			String conditionSql = "";
			Iterator<?> conditionKeys = condition.keys();
			
			while (conditionKeys.hasNext()) {
				String key = (String) conditionKeys.next();
				//查询条件
				if ("cityId".equals(key)) {
					conditionSql+=" AND B.CITY_ID="+condition.getInt(key);
				}
				if ("name".equals(key)) {
					conditionSql+=" AND B.block_name like '%"+condition.getString(key)+"%'";
				}
			}
			
			
			QueryRunner run = new QueryRunner();
			long pageStartNum = (curPageNum - 1) * pageSize + 1;
			long pageEndNum = curPageNum * pageSize;
			String sb="WITH FINAL_TABLE AS ( SELECT B.BLOCK_ID, B.CITY_ID, B.BLOCK_NAME, B.WORK_PROPERTY"
					+ "  FROM BLOCK B"
					+ " WHERE 1=1"
					+conditionSql +")"
					+" SELECT /*+FIRST_ROWS ORDERED*/"
					+" TT.*, (SELECT COUNT(1) FROM FINAL_TABLE) AS TOTAL_RECORD_NUM"
					+"  FROM (SELECT FINAL_TABLE.*, ROWNUM AS ROWNUM_"
					+" FROM FINAL_TABLE"
					+" WHERE ROWNUM <= "+pageEndNum					
					+ ") TT"
					+" WHERE TT.ROWNUM_ >= "+pageStartNum;

			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap<Object,Object>> list = new ArrayList<HashMap<Object,Object>>();
					Page page = new Page();
				    int totalCount = 0;
				    //SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					while (rs.next()) {
						HashMap<Object,Object> task = new HashMap<Object,Object>();
						task.put("taskId", 0);
						task.put("taskName","");
						
						task.put("blockId", rs.getInt("BLOCK_ID"));
						task.put("blockName", rs.getString("BLOCK_NAME"));
						task.put("workProperty", rs.getString("WORK_PROPERTY"));

						task.put("status", 0);
						task.put("type", 1);
						
						task.put("percent", 0);
						task.put("diffDate",0);
						task.put("progress", 0);
						
						task.put("groupId",0);
						task.put("groupName","");
						task.put("programType",1);
						
						task.put("planStartDate", "");
						task.put("planEndDate", "");
						task.put("roadPlanTotal", 0);
						task.put("poiPlanTotal", 0);
						task.put("orderStatus", 5);
						task.put("jobs", new JSONArray());
						totalCount=rs.getInt("TOTAL_RECORD_NUM");
						list.add(task);
					}					
					page.setTotalCount(totalCount);
					page.setResult(list);
					return page;
				}

			};
			log.info("task list sql:" + sb.toString());
			Page page= run.query(conn, sb.toString(), rsHandler);
			page.setPageNum(curPageNum);
		    page.setPageSize(pageSize);
		    return page;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
//	public List<Task> listAll(JSONObject conditionJson,JSONObject orderJson)throws Exception{
//		Connection conn = null;
//		try{
//			conn = DBConnector.getInstance().getManConnection();
//			
//			String selectSql = "select T.*, nvl(C.CITY_NAME,'') CITY_NAME, nvl(U.USER_REAL_NAME,'') USER_REAL_NAME, nvl(G.GROUP_NAME,'') GROUP_NAME"
//					+ "  FROM TASK T, CITY C, USER_INFO U, USER_GROUP G"
//					+ " WHERE T.CITY_ID = C.CITY_ID(+)"
//					+ "   AND T.CREATE_USER_ID = U.USER_ID(+)"
//					+ "   AND T.MONTH_EDIT_GROUP_ID = G.GROUP_ID(+)"
//					+ "   AND T.LATEST = 1";
//			if(null!=conditionJson && !conditionJson.isEmpty()){
//				Iterator keys = conditionJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if ("taskId".equals(key)) {selectSql+=" and T.task_id="+conditionJson.getInt(key);}
//					if ("cityIds".equals(key)) {selectSql+=" and T.city_id in ("+StringUtils.join(conditionJson.getJSONArray(key), ",")+")";}
//					if ("createUserId".equals(key)) {selectSql+=" and T.create_user_id="+conditionJson.getInt(key);}
//					if ("descp".equals(key)) {selectSql+=" and T.descp='"+conditionJson.getString(key)+"'";}
//					if ("name".equals(key)) {selectSql+=" and T.name like '%"+conditionJson.getString(key)+"%'";}
//					if ("status".equals(key)) {selectSql+=" and T.status in ("+conditionJson.getJSONArray(key).join(",")+")";}
//					if ("createUserName".equals(key)) {selectSql+=" and U.USER_REAL_NAME like '%"+conditionJson.getString(key)+"%'";}
//					if ("cityName".equals(key)) {selectSql+=" and C.CITY_NAME like '%"+conditionJson.getString(key)+"%'";}
//					}
//				}
//			if(null!=orderJson && !orderJson.isEmpty()){
//				Iterator keys = orderJson.keys();
//				while (keys.hasNext()) {
//					String key = (String) keys.next();
//					if ("status".equals(key)) {selectSql+=" order by T.status "+orderJson.getString(key);break;}
//					if ("taskId".equals(key)) {selectSql+=" order by T.TASK_ID "+orderJson.getString(key);break;}
//					if ("planStartDate".equals(key)) {selectSql+=" order by T.PLAN_START_DATE "+orderJson.getString(key);break;}
//					if ("planEndDate".equals(key)) {selectSql+=" order by T.PLAN_END_DATE "+orderJson.getString(key);break;}
//					if ("monthEditPlanStartDate".equals(key)) {selectSql+=" order by T.MONTH_EDIT_PLAN_START_DATE "+orderJson.getString(key);break;}
//					if ("monthEditPlanEndDate".equals(key)) {selectSql+=" order by T.MONTH_EDIT_PLAN_END_DATE "+orderJson.getString(key);break;}
//					}
//			}else{
//				selectSql+=" order by T.TASK_ID";
//			}
//			return TaskOperation.selectTaskBySql2(conn, selectSql, null);
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
//		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//	}	
	
	/**
	 * 查询task
	 * 关闭task,相应修改block状态
	 * 快线采集任务关闭，需对poi，tips采集成果批中线任务号
	 * 发送消息
	 */
	public String close(int taskId, long userId,String overdueReason,String overdueOtherReason)throws Exception{
		Connection conn = null;
		Connection dailyConn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();	
			Task task = queryByTaskId(conn,taskId);
			//更新任务状态
			log.info("更新"+taskId+"任务状态为关闭");
			TaskOperation.updateStatus(conn, taskId, 0);
			//任务关闭清空该任务的规划数据
			Region region = RegionService.getInstance().query(conn,task.getRegionId());
			dailyConn = DBConnector.getInstance().getConnectionById(region.getDailyDbId());
			String updateSql="DELETE FROM data_plan t where t.task_id="+ taskId;
			QueryRunner run=new QueryRunner();
			run.execute(dailyConn, updateSql);
			//若有延迟原因，需更新进入任务表
			if(!StringUtils.isEmpty(overdueReason)){
				Task beanTask=new Task();
				beanTask.setTaskId(taskId);
				beanTask.setOverdueReason(overdueReason);
				beanTask.setOverdueOtherReason(overdueOtherReason);
				TaskOperation.updateTask(conn, beanTask);
			}
			//更新block状态：如果所有task都已关闭，则block状态置3
			log.info("更新"+taskId+"任务对应的block状态，如果所有task都已关闭，则block状态置3关闭");
			TaskOperation.closeBlock(conn,task.getBlockId());
			//快线采集任务关闭，需批中线采集任务id
			if(task.getType()==0&&task.getBlockId()==0){
				log.info(taskId+"任务为快线采集任务，关闭时需同步批poi，tips的对应的中线任务号");
				Set<Integer> collectTask = batchMidTask(conn,userId,task);
				if(collectTask!=null&&collectTask.size()>0){
					try {
						log.info(taskId+"任务为快线采集任务，快转中消息推送start");
						List<Object[]> msgContentList=new ArrayList<Object[]>();
						String msgTitle="快线转中线";
						JSONArray taskIds=new JSONArray();
						taskIds.addAll(collectTask);
						List<Task> pushtask = getTaskListWithLeader(conn, taskIds);
						for(Task t:pushtask){
							if(t.getGroupLeader()!=0){
								Object[] msgTmp=new Object[4];
								msgTmp[0]=t.getGroupLeader();//收信人
								msgTmp[1]=msgTitle;//消息头
								msgTmp[2]="快线"+task.getName()+"采集任务的数据，已落入中线"+t.getName()+"采集任务,请关注";//消息内容
								//关联要素
								JSONObject msgParam = new JSONObject();
								msgParam.put("relateObject", "TASK");
								msgParam.put("relateObjectId", t.getTaskId());
								msgTmp[3]=msgParam.toString();//消息对象
								msgContentList.add(msgTmp);
							}
						}
						if(msgContentList.size()>0){
							taskPushMsgByMsg(conn,msgContentList,userId);	
							log.info(taskId+"任务为快线采集任务，快转中消息推送end");
						}						
					} catch (Exception e) {
						e.printStackTrace();
						log.error("task关闭消息发送失败,原因:"+e.getMessage(), e);
					}
				}
			}
			//记录关闭时间
			TimelineService.recordTimeline(taskId, "task",0, conn);
			
			//发送消息
			try {
				List<Object[]> msgContentList=new ArrayList<Object[]>();
				String msgTitle="task关闭";
				JSONArray taskIds=new JSONArray();
				taskIds.add(task.getTaskId());
				List<Task> pushtask = getTaskListWithLeader(conn, taskIds);
				Task taskLeader=new Task();
				if(pushtask!=null&&pushtask.size()>0){taskLeader=pushtask.get(0);}
				if(taskLeader.getGroupLeader()!=0){
					Object[] msgTmp=new Object[4];
					msgTmp[0]=taskLeader.getGroupLeader();//收信人
					msgTmp[1]=msgTitle;//消息头
					msgTmp[2]="关闭task:"+task.getName()+",请关注";//消息内容
					//关联要素
					JSONObject msgParam = new JSONObject();
					msgParam.put("relateObject", "TASK");
					msgParam.put("relateObjectId", task.getTaskId());
					msgTmp[3]=msgParam.toString();//消息对象
					msgContentList.add(msgTmp);
				}
				//生管角色发消息
				String userSql="SELECT DISTINCT M.USER_ID, I.USER_REAL_NAME,I.USER_EMAIL"
						+ "  FROM ROLE_USER_MAPPING M, USER_INFO I"
						+ " WHERE M.ROLE_ID = 3"
						+ "   AND M.USER_ID = I.USER_ID";
				Map<Long, UserInfo> userIdList=UserInfoOperation.getUserInfosBySql(conn, userSql);
				for(Long userIdTmp:userIdList.keySet()){
					//String pushUserName =userIdList.get(userIdTmp).getUserRealName();
					Object[] msgTmp=new Object[4];
					msgTmp[0]=userIdTmp;//收信人
					msgTmp[1]=msgTitle;//消息头
					msgTmp[2]="关闭task:"+task.getName()+",请关注";//消息内容
					//关联要素
					JSONObject msgParam = new JSONObject();
					msgParam.put("relateObject", "TASK");
					msgParam.put("relateObjectId", task.getTaskId());
					msgTmp[3]=msgParam.toString();//消息对象
					msgContentList.add(msgTmp);
				}
				//若是采集任务，还需向其所在区域的日编组长，月编组长发消息
				String userLeaderSql="SELECT DISTINCT I.USER_ID, I.USER_REAL_NAME, I.USER_EMAIL"
						+ "  FROM USER_INFO I, TASK T, TASK CT, USER_GROUP G"
						+ " WHERE CT.TASK_ID = "+task.getTaskId()
						+ "   AND CT.TYPE = 0"
						+ "   AND CT.PROGRAM_ID = T.PROGRAM_ID"
						+ "   AND CT.BLOCK_ID = T.BLOCK_ID"
						+ "   AND T.LATEST = 1"
						+ "   AND T.TYPE IN (1, 2)"
						+ "   AND T.GROUP_ID != 0"
						+ "   AND T.GROUP_ID = G.GROUP_ID"
						+ "   AND G.LEADER_ID = I.USER_ID";
				Map<Long, UserInfo> userIdLeaderList=UserInfoOperation.getUserInfosBySql(conn, userLeaderSql);
				for(Long userIdTmp:userIdLeaderList.keySet()){
					//String pushUserName =userIdList.get(userIdTmp).getUserRealName();
					Object[] msgTmp=new Object[4];
					msgTmp[0]=userIdTmp;//收信人
					msgTmp[1]=msgTitle;//消息头
					msgTmp[2]="关闭task:"+task.getName()+",请关注";//消息内容
					//关联要素
					JSONObject msgParam = new JSONObject();
					msgParam.put("relateObject", "TASK");
					msgParam.put("relateObjectId", task.getTaskId());
					msgTmp[3]=msgParam.toString();//消息对象
					msgContentList.add(msgTmp);
				}
				
				if(msgContentList.size()>0){
					taskPushMsgByMsg(conn,msgContentList,userId);	
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error("task关闭消息发送失败,原因:"+e.getMessage(), e);
			}
			return "";
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			DbUtils.rollbackAndCloseQuietly(dailyConn);
			
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
			DbUtils.commitAndCloseQuietly(dailyConn);
		}
	}
	/**
	 * 根据快线任务，批中线采集任务id
	 * @param conn 
	 * @param task
	 */
	private Set<Integer> batchMidTask(Connection conn, Long userId,Task task) throws Exception{
		Connection dailyConn=null;
		try{
			Region region=RegionService.getInstance().query(conn,task.getRegionId());
			dailyConn=DBConnector.getInstance().getConnectionById(region.getDailyDbId());
			//获取快线采集任务对应的poi/tips的grid集合
			log.info(task.getTaskId()+"任务为快线采集任务，获取其poi与grid的对照关系");
			Map<Long, Integer> poiGridMap=getPoiGridByQuickTask(dailyConn,task.getTaskId());
			log.info(task.getTaskId()+"任务为快线采集任务，获取其tips对应的grid集合");
			FccApi api=(FccApi) ApplicationContextUtil.getBean("fccApi");
			Set<Integer> tipsGrids=api.getTipsGridsBySqTaskId(task.getTaskId());
			Set<Integer> allGrids=new HashSet<Integer>();
			if(tipsGrids!=null&&tipsGrids.size()>0){
				log.info(task.getTaskId()+"任务为快线采集任务，tips对应grid范围"+tipsGrids.toString());
				allGrids.addAll(tipsGrids);
			}
			if(poiGridMap!=null&&poiGridMap.size()>0){
				log.info(task.getTaskId()+"任务为快线采集任务，poi对应grid范围"+poiGridMap.toString());
				allGrids.addAll(poiGridMap.values());
			}
			//判断grid所在项目，区县，返回grid所在中线采集任务id
			if(allGrids==null||allGrids.size()==0){
				log.info(task.getTaskId()+"任务为快线采集任务，poi，tips无数据");
				return null;}
			log.info(task.getTaskId()+"任务为快线采集任务，计算poi，tips所在grid对应的中线采集任务号");
			Map<Integer, Integer> gridMap=getMidTaskIdByGrid(conn,userId,allGrids,task);
			log.info(task.getTaskId()+"任务为快线采集任务，计算poi，tips所在grid对应的中线采集任务号"+gridMap.toString());
			//判断是否所有grid均获取到中线任务
			if(gridMap.size()!=allGrids.size()){
				allGrids.removeAll(gridMap.keySet());
				throw new Exception("存在grid未获取中线任务号，请查看："+allGrids.toString());
			}
			//任务号批数据			
			//poi批中线任务号	
			if(poiGridMap!=null&&poiGridMap.size()>0){
				log.info(task.getTaskId()+"任务为快线采集任务，批poi中线采集任务号");
				Map<Long, Integer> poiTaskMap=new HashMap<Long, Integer>();
				for(Long pid:poiGridMap.keySet()){
					poiTaskMap.put(pid, gridMap.get(poiGridMap.get(pid)));
				}
				batchPoiMidTask(dailyConn,poiTaskMap);
			}			
			
			//tip批中线任务号
			if(tipsGrids!=null&&tipsGrids.size()>0){
				log.info(task.getTaskId()+"任务为快线采集任务，批tips中线采集任务号");
				api.batchUpdateSmTaskId(task.getTaskId(), gridMap);
			}
			Set<Integer> taskIdSet=new HashSet<Integer>();
			taskIdSet.addAll(gridMap.values());
			return taskIdSet;
		}catch(Exception e){
			log.error("", e);
			DbUtils.rollbackAndCloseQuietly(dailyConn);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(dailyConn);
		}	
	}
	/**
	 * poi批中线任务号
	 * @param dailyConn
	 * @param poiTaskMap
	 * @throws SQLException 
	 */
	private void batchPoiMidTask(Connection dailyConn,
			Map<Long, Integer> poiTaskMap) throws SQLException {
		String updateSql="update poi_edit_status set medium_task_id=? where pid=? and medium_task_id=0";
		QueryRunner run=new QueryRunner();
		Object[][] params=new Object[poiTaskMap.keySet().size()][2] ;
		int i=0;
		for(Long pid:poiTaskMap.keySet()){
			Object[] pidMap=new Object[2];
			pidMap[0]=poiTaskMap.get(pid);
			pidMap[1]=pid;
			params[i]=pidMap;
			i++;
		}
		run.batch(dailyConn, updateSql, params);
	}

	private Map<Integer, Integer> getMidTaskIdByGrid(Connection conn,final Long userId,Set<Integer> gridSet,final Task quickTask) throws Exception{
		if(gridSet==null||gridSet.size()==0){return null;}
		List<Clob> values=new ArrayList<Clob>();
		String gridString="";
		String grids=gridSet.toString().replace("[", "").replace("]", "");
		if(gridSet.size()>1000){
			Clob clob=ConnectionUtil.createClob(conn);
			clob.setString(1, grids);
			gridString=" GRID_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
			values.add(clob);
			values.add(clob);
		}else{
			gridString=" GRID_ID IN ("+grids+")";
		}
		String sql="SELECT G.GRID_ID,"
				+ "       C.CITY_ID,"
				+ "       C.CITY_NAME,"
				+ "       C.REGION_ID,"
				+ "       C.PLAN_STATUS CITY_STATUS,"
				+ "       0             PROGRAM_ID,"
				+ "       B.BLOCK_ID,"
				+ "       B.BLOCK_NAME,"				
				+ "       B.PLAN_STATUS BLOCK_STATUS,"
				+ "       0             TASK_ID"
				+ "  FROM GRID G, CITY C, BLOCK B"
				+ " WHERE G.CITY_ID = C.CITY_ID"
				+ "   AND G.BLOCK_ID = B.BLOCK_ID"
				+ "   AND B.PLAN_STATUS IN (0, 2)"
				+ "   AND C.PLAN_STATUS IN (0, 2)"
				+ "   AND G."+gridString
				+ " UNION ALL"
				+ " SELECT G.GRID_ID,"
				+ "       C.CITY_ID,"
				+ "       C.CITY_NAME,"
				+ "       C.REGION_ID,"
				+ "       C.PLAN_STATUS CITY_STATUS,"
				+ "       P.PROGRAM_ID,"
				+ "       B.BLOCK_ID,"
				+ "       B.BLOCK_NAME,"
				+ "       B.PLAN_STATUS BLOCK_STATUS,"
				+ "       0             TASK_ID"
				+ "  FROM GRID G, CITY C, BLOCK B, PROGRAM P"
				+ " WHERE G.CITY_ID = C.CITY_ID"
				+ "   AND G.BLOCK_ID = B.BLOCK_ID"
				+ "   AND C.CITY_ID = P.CITY_ID"
				+ "   AND P.LATEST = 1"
				+ "   AND B.PLAN_STATUS IN (0, 2)"
				+ "   AND C.PLAN_STATUS IN (1, 3)"
				+ "   AND G."+gridString
				+ " UNION ALL"
				+ " SELECT G.GRID_ID,"
				+ "       C.CITY_ID,"
				+ "       C.CITY_NAME,"
				+ "       C.REGION_ID,"
				+ "       C.PLAN_STATUS CITY_STATUS,"
				+ "       0             PROGRAM_ID,"
				+ "       B.BLOCK_ID,"
				+ "       B.BLOCK_NAME,"
				+ "       B.PLAN_STATUS BLOCK_STATUS,"
				+ "       T.TASK_ID"
				+ "  FROM GRID G, CITY C, BLOCK B, TASK T"
				+ " WHERE G.CITY_ID = C.CITY_ID"
				+ "   AND G.BLOCK_ID = B.BLOCK_ID"
				+ "   AND B.PLAN_STATUS IN (1, 3)"
				+ "   AND B.BLOCK_ID = T.BLOCK_ID"
				+ "   AND T.LATEST = 1"
				+ "   AND T.TYPE = 0"
				+ "   AND G."+gridString;
		QueryRunner run=new QueryRunner();
		return run.query(conn, sql, new ResultSetHandler<Map<Integer, Integer>>(){

			@Override
			public Map<Integer, Integer> handle(ResultSet rs)
					throws SQLException {
				Map<Integer, Integer> gridMap=new HashMap<Integer, Integer>();
				Map<Integer, Integer> blockMap=new HashMap<Integer, Integer>();
				String time = new SimpleDateFormat("yyyyMMdd").format(new Date());
				Connection conn=null;
				try{
					conn = DBConnector.getInstance().getManConnection();
					while(rs.next()){
						int blockStatus=rs.getInt("BLOCK_STATUS");
						int gridId=rs.getInt("GRID_ID");
						if(blockStatus==1||blockStatus==3){
							gridMap.put(gridId, rs.getInt("TASK_ID"));
							continue;
						}
						int cityStatus=rs.getInt("CITY_STATUS");
						if(blockStatus==0||blockStatus==2){							
							int blockId=rs.getInt("BLOCK_ID");
							if(blockMap.containsKey(blockId)){
								gridMap.put(gridId, blockMap.get(blockId));
								continue;
							}
							
							JSONObject condition=new JSONObject();
							JSONArray programIds=new JSONArray();
							programIds.add(quickTask.getProgramId());
							condition.put("programIds",programIds);
							List<Program> programList = ProgramService.getInstance().queryProgramTable(conn, condition);
							Program quickProgram = programList.get(0);
							
							int programId=rs.getInt("PROGRAM_ID");
							Program myProgram=null;
							if(cityStatus==0||cityStatus==2){//需创建项目
								log.info(gridId+"无对应中线项目，新建项目");								
								Program program=new Program();
								program.setName(rs.getString("CITY_NAME")+"_"+time);
								program.setCityId(rs.getInt("CITY_ID"));
								program.setType(1);
								program.setDescp("快线项目："+quickProgram.getName()+"转中线");
								program.setCollectPlanStartDate(quickProgram.getCollectPlanStartDate());
								program.setCollectPlanEndDate(quickProgram.getCollectPlanEndDate());
								program.setMonthEditPlanStartDate(TimestampUtils.addDays(quickProgram.getProducePlanEndDate(),1));
								program.setMonthEditPlanEndDate(TimestampUtils.addDays(program.getMonthEditPlanStartDate(),1));
								program.setProducePlanStartDate(TimestampUtils.addDays(program.getMonthEditPlanEndDate(),1));
								program.setProducePlanEndDate(TimestampUtils.addDays(program.getMonthEditPlanEndDate(),10));
								program.setPlanStartDate(quickProgram.getCollectPlanStartDate());
								program.setPlanEndDate(program.getProducePlanEndDate());
								program.setCreateUserId(0);
								programId=ProgramService.getInstance().create(conn,program);
								JSONArray openProgramIds=new JSONArray();
								openProgramIds.add(programId);
								//condition.put("programIds",programIds);
								ProgramService.getInstance().openStatus(conn, openProgramIds);
								myProgram=program;
								log.info(gridId+"无对应中线项目，新建项目："+programId);
							}
							//创建block项目
							List<Integer> gridList = GridService.getInstance().getGridListByBlockId(conn,blockId);
							Map<Integer, Integer> gridIds = new HashMap<Integer, Integer>();
							for(Integer gridtmp:gridList){
								gridIds.put(gridtmp, 1);
							}
							
							if(cityStatus==1||cityStatus==3){
								if(cityStatus==1){
									log.info(gridId+"有对应"+programId+"项目，但项目处于草稿状态，需先进行开启");
									//JSONObject condition=new JSONObject();
									JSONArray openProgramIds=new JSONArray();
									openProgramIds.add(programId);
									//condition.put("programIds",openProgramIds);
									ProgramService.getInstance().openStatus(conn, openProgramIds);
								}
								JSONObject condition1=new JSONObject();
								JSONArray programIds1=new JSONArray();
								programIds1.add(programId);
								condition.put("programIds",programIds1);
								List<Program> programList1 = ProgramService.getInstance().queryProgramTable(conn, condition1);
								myProgram=programList1.get(0);
							}
							int regionId=rs.getInt("REGION_ID");
							log.info(gridId+"无对应中线block任务，新建任务start");
							//创建采集任务
							Task collectTask=new Task();
							collectTask.setProgramId(programId);
							collectTask.setRegionId(regionId);
							collectTask.setBlockId(blockId);
							collectTask.setGridIds(gridIds);
							collectTask.setName(rs.getString("BLOCK_NAME")+"_"+time);
							collectTask.setDescp("快线项目："+quickProgram.getName()+"转中线");
							collectTask.setCreateUserId(0);
							collectTask.setType(0);
							collectTask.setGroupId(quickTask.getGroupId());
							collectTask.setRoadPlanTotal(quickTask.getRoadPlanTotal());
							collectTask.setPoiPlanTotal(quickTask.getPoiPlanTotal());
							collectTask.setWorkKind(quickTask.getWorkKind());
							if(myProgram!=null){
								collectTask.setPlanStartDate(myProgram.getCollectPlanStartDate());
								collectTask.setPlanEndDate(myProgram.getCollectPlanEndDate());
								collectTask.setProducePlanStartDate(myProgram.getProducePlanStartDate());
								collectTask.setProducePlanEndDate(myProgram.getProducePlanEndDate());
							}
							int collectTaskId=createWithBean(conn, collectTask);
							TaskOperation.updateStatus(conn, collectTaskId, 0);
							gridMap.put(gridId, collectTaskId);
							blockMap.put(blockId, collectTaskId);

							//创建月编，二代编辑任务							
							Task monthTask=new Task();
							monthTask.setProgramId(programId);
							monthTask.setRegionId(regionId);
							monthTask.setBlockId(blockId);
							monthTask.setGridIds(gridIds);
							monthTask.setName(rs.getString("BLOCK_NAME")+"_"+time);
							monthTask.setDescp("快线项目："+quickProgram.getName()+"转中线");
							monthTask.setCreateUserId(0);
							monthTask.setType(2);
							monthTask.setRoadPlanTotal(quickTask.getRoadPlanTotal());
							monthTask.setPoiPlanTotal(quickTask.getPoiPlanTotal());
							if(myProgram!=null){
								monthTask.setPlanStartDate(myProgram.getMonthEditPlanEndDate());
								monthTask.setPlanEndDate(myProgram.getMonthEditPlanEndDate());
								monthTask.setProducePlanStartDate(myProgram.getProducePlanStartDate());
								monthTask.setProducePlanEndDate(myProgram.getProducePlanEndDate());
							}
							createWithBean(conn, monthTask);
							
							Task cmsTask=new Task();
							cmsTask.setProgramId(programId);
							cmsTask.setRegionId(regionId);
							cmsTask.setBlockId(blockId);
							cmsTask.setGridIds(gridIds);
							cmsTask.setName(rs.getString("BLOCK_NAME")+"_"+time);
							cmsTask.setDescp("快线项目："+quickProgram.getName()+"转中线");
							cmsTask.setCreateUserId(0);
							cmsTask.setType(3);
							cmsTask.setRoadPlanTotal(quickTask.getRoadPlanTotal());
							cmsTask.setPoiPlanTotal(quickTask.getPoiPlanTotal());
							
							if(myProgram!=null){
								cmsTask.setPlanStartDate(myProgram.getMonthEditPlanEndDate());
								cmsTask.setPlanEndDate(myProgram.getMonthEditPlanEndDate());
								cmsTask.setProducePlanStartDate(myProgram.getProducePlanStartDate());
								cmsTask.setProducePlanEndDate(myProgram.getProducePlanEndDate());
							}
							createWithBean(conn, cmsTask);
							List<Integer> blockIds=new ArrayList<Integer>();
							blockIds.add(blockId);
							BlockService.getInstance().updateStatus(conn, blockIds,3);
							log.info(gridId+"无对应中线block任务，新建任务：end");
						}						
					}
					return gridMap;
				}catch (Exception e){
					DbUtils.rollbackAndCloseQuietly(conn);
					log.error("", e);
				}finally{
					DbUtils.commitAndCloseQuietly(conn);
				}
				return gridMap;
			}
			
		});
	}
	/**
	 * 查询采集任务taskId对应的poi及grid的map
	 * @param dailyConn
	 * @param taskId 快线采集任务id
	 * @return Map<Long, Integer> key：pid value：gridId
	 * @throws Exception
	 */
	private Map<Long, Integer> getPoiGridByQuickTask(Connection dailyConn,int taskId) throws Exception {
		try{
			String sql="SELECT S.PID, P.GEOMETRY"
					+ "  FROM POI_EDIT_STATUS S, IX_POI P"
					+ " WHERE S.QUICK_TASK_ID = "+taskId
					+ "   AND S.PID = P.PID";
			QueryRunner run=new QueryRunner();
			return run.query(dailyConn, sql, new ResultSetHandler<Map<Long,Integer>>(){

				@Override
				public Map<Long, Integer> handle(ResultSet rs)
						throws SQLException {
					Map<Long,Integer> poiGrids=new HashMap<Long, Integer>();
					while(rs.next()){						
						STRUCT struct=(STRUCT)rs.getObject("GEOMETRY");
						try {
							Geometry geo = GeoTranslator.struct2Jts(struct);
							//通过 geo 获取 grid 
							Coordinate[] coordinate = geo.getCoordinates();	
							Integer gridId = Integer.valueOf(CompGridUtil.point2Grids(coordinate[0].x, coordinate[0].y)[0]);
							poiGrids.put(rs.getLong("PID"), gridId);
						} catch (Exception e1) {
							log.error(e1.getMessage(),e1);
						}
					}
					return poiGrids;
				}});
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(dailyConn);
			throw e;
		}
	}
	
	/*
	 * 返回task详细信息
	 * 包含block,program,几何信息
	 */
	public Task queryByTaskId(int taskId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			return queryByTaskId(conn,taskId);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/*
	 * 返回task详细信息
	 * 包含block,program,几何信息
	 */
	public Task queryByTaskId(Connection conn,int taskId) throws Exception {
		try{
			Task task=queryNoGeoByTaskId(conn,taskId);
			//获取任务grid和geo
			Map<Integer, Integer> gridIds = getGridMapByTaskId(conn,task.getTaskId());
			task.setGridIds(gridIds);
			
			JSONArray jsonArray = JSONArray.fromObject(gridIds.keySet().toArray());
			String wkt = GridUtils.grids2Wkt(jsonArray);
			task.setGeometry(Geojson.wkt2Geojson(wkt));
			
			return task;	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/*
	 * 返回task详细信息
	 * 包含block,program,几何信息
	 */
	public Task queryNoGeoByTaskId(Connection conn,int taskId) throws Exception {
		try{
			QueryRunner run=new QueryRunner();
			String sql="SELECT T.TASK_ID,"
					+ "       T.NAME,"
					+ "       T.STATUS,"
					+ "       T.DESCP,"
					+ "       T.TYPE,"
					+ "       T.PLAN_START_DATE,"
					+ "       T.PLAN_END_DATE,"
					+ "       T.PRODUCE_PLAN_START_DATE,"
					+ "       T.PRODUCE_PLAN_END_DATE,"
					+ "       T.LOT,"
					+ "       T.POI_PLAN_TOTAL,"
					+ "       T.ROAD_PLAN_TOTAL,"
					+ "       T.POI_PLAN_IN,"
					+ "       T.POI_PLAN_OUT,"
					+ "       T.ROAD_PLAN_IN,"
					+ "       T.ROAD_PLAN_OUT,"
					+ "       T.DATA_PLAN_STATUS,"
					+ "       T.WORK_KIND,"
					+ "       B.BLOCK_ID,"
					+ "       B.BLOCK_NAME,"
					+ "       B.WORK_PROPERTY,"
					+ "       P.PROGRAM_ID,"
					+ "       P.NAME                    PROGRAM_NAME,"
					+ "       P.TYPE                    PROGRAM_TYPE,"
					+ "       U.USER_ID,"
					+ "       U.USER_REAL_NAME,"
					+ "       UG.GROUP_ID,"
					+ "       UG.GROUP_NAME,"
					+ "       T.REGION_ID,"
					+ "       I.METHOD,"
					+ "       I.ADMIN_NAME,I.INFOR_STAGE"
					+ "  FROM TASK T, BLOCK B, PROGRAM P, USER_GROUP UG, USER_INFO U, INFOR I"
					+ " WHERE T.BLOCK_ID = B.BLOCK_ID(+)"
					+ "   AND T.PROGRAM_ID = P.PROGRAM_ID"
					+ "   AND P.INFOR_ID = I.INFOR_ID(+)"
					+ "   AND T.GROUP_ID = UG.GROUP_ID(+)"
					+ "   AND T.CREATE_USER_ID = U.USER_ID(+)"
					+ "   AND T.TASK_ID = "+taskId;
			
			log.info("queryByTaskId sql:" + sql);

			ResultSetHandler<Task> rsHandler = new ResultSetHandler<Task>() {
				public Task handle(ResultSet rs) throws SQLException {
					Task task = new Task();
					if (rs.next()) {
						task.setTaskId(rs.getInt("TASK_ID"));
						task.setName(rs.getString("NAME"));
						task.setStatus(rs.getInt("STATUS"));
						task.setDescp(rs.getString("DESCP"));
						task.setType(rs.getInt("TYPE"));
						task.setWorkKind(rs.getString("WORK_KIND"));
						task.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						task.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						task.setProducePlanStartDate(rs.getTimestamp("PRODUCE_PLAN_START_DATE"));
						task.setProducePlanEndDate(rs.getTimestamp("PRODUCE_PLAN_END_DATE"));
						task.setLot(rs.getInt("LOT"));
						task.setPoiPlanTotal(rs.getInt("POI_PLAN_TOTAL"));
						task.setRoadPlanTotal(rs.getFloat("ROAD_PLAN_TOTAL"));
						task.setBlockId(rs.getInt("BLOCK_ID"));
						task.setBlockName(rs.getString("BLOCK_NAME"));
						task.setWorkProperty(rs.getString("WORK_PROPERTY"));
						task.setProgramId(rs.getInt("PROGRAM_ID"));
						task.setProgramName(rs.getString("PROGRAM_NAME"));
						task.setProgramType(rs.getInt("PROGRAM_TYPE"));
						task.setCreateUserId(rs.getInt("USER_ID"));
						task.setCreateUserName(rs.getString("USER_REAL_NAME"));
						task.setGroupId(rs.getInt("GROUP_ID"));
						task.setGroupName(rs.getString("GROUP_NAME"));
						task.setRegionId(rs.getInt("REGION_ID"));
						task.setMethod(rs.getString("METHOD"));
						task.setAdminName(rs.getString("ADMIN_NAME"));	
						task.setInforStage(rs.getInt("INFOR_STAGE"));
						task.setDataPlanStatus(rs.getInt("DATA_PLAN_STATUS"));
						task.setPoiPlanIn(rs.getInt("POI_PLAN_IN"));
						task.setPoiPlanOut(rs.getInt("POI_PLAN_OUT"));
						task.setRoadPlanIn(rs.getInt("ROAD_PLAN_IN"));
						task.setRoadPlanOut(rs.getInt("ROAD_PLAN_OUT"));
						task.setVersion(SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
					}
					return task;
				}

			};
			Task task=run.query(conn, sql, rsHandler);
			return task;	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}

	/*
	 * 返回task详细信息
	 * 包含block,program,几何信息
	 */
	public Map<String,Object> query(int taskId) throws Exception {
		try{
			Task task = queryByTaskId(taskId);
			
		    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
			
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("taskId", task.getTaskId());
			map.put("name", task.getName());
			map.put("status", task.getStatus());
			map.put("descp", task.getDescp());
			map.put("type", task.getType());
			map.put("workKind", task.getWorkKindList());
			
			Timestamp planStartDate = task.getPlanStartDate();
			Timestamp planEndDate = task.getPlanEndDate();
			if(planStartDate != null){
				map.put("planStartDate", df.format(planStartDate));
			}else {map.put("planStartDate", "");}
			if(planEndDate != null){
				map.put("planEndDate",df.format(planEndDate));
			}else{map.put("planEndDate", "");}
			
			Timestamp producePlanStartDate = task.getProducePlanStartDate();
			Timestamp producePlanEndDate = task.getProducePlanEndDate();
			if(producePlanStartDate != null){
				map.put("producePlanStartDate", df.format(producePlanStartDate));
			}else {map.put("producePlanStartDate", "");}
			if(producePlanEndDate != null){
				map.put("producePlanEndDate",df.format(producePlanEndDate));
			}else{map.put("producePlanEndDate", "");}

			map.put("lot", task.getLot());
			//modify by songhe
			//删除road_plan_total,poi_plan_total,添加road/poi_plan_in/out  2017/07/25
			map.put("roadPlanIn", task.getRoadPlanIn());
			map.put("roadPlanOut", task.getRoadPlanOut());
			map.put("poiPlanIn", task.getPoiPlanIn());
			map.put("poiPlanOut", task.getPoiPlanOut());
			map.put("poiPlanTotal", task.getPoiPlanTotal());
			map.put("roadPlanTotal", task.getRoadPlanTotal());
			map.put("blockId", task.getBlockId());
			map.put("blockName", task.getBlockName());
			map.put("workProperty", task.getWorkProperty());
			map.put("programId", task.getProgramId());
			map.put("programName", task.getProgramName());
			map.put("programType", task.getProgramType());
			map.put("createUserId", task.getCreateUserId());
			map.put("createUserName", task.getCreateUserName());
			map.put("groupId", task.getGroupId());
			map.put("groupName", task.getGroupName());
			map.put("gridIds", task.getGridIds());
			map.put("geometry", task.getGeometry());
			map.put("version", task.getVersion());
			map.put("method", task.getMethod());
			map.put("adminName", task.getAdminName());
			map.put("dataPlanStatus", task.getDataPlanStatus());
			map.put("inforStage", task.getInforStage());
			
			return map;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}

	
	public Map<Integer,Integer> getGridMapByTaskId(Integer taskId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run=new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT M.GRID_ID,M.TYPE FROM TASK_GRID_MAPPING M WHERE M.TASK_ID = " + taskId);
			String selectSql= sb.toString();

			ResultSetHandler<Map<Integer,Integer>> rsHandler = new ResultSetHandler<Map<Integer,Integer>>() {
				public Map<Integer,Integer> handle(ResultSet rs) throws SQLException {
					Map<Integer,Integer> gridMap = new HashMap<Integer,Integer>();
					while (rs.next()) {
						gridMap.put(rs.getInt("GRID_ID"), rs.getInt("TYPE"));
					}
					return gridMap;
				}
			};
			return run.query(conn, selectSql, rsHandler);	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public Map<Integer,Integer> getGridMapByTaskId(Connection conn,Integer taskId) throws Exception {
		try{
			QueryRunner run=new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT M.GRID_ID,M.TYPE FROM TASK_GRID_MAPPING M WHERE M.TASK_ID = " + taskId);
			String selectSql= sb.toString();

			ResultSetHandler<Map<Integer,Integer>> rsHandler = new ResultSetHandler<Map<Integer,Integer>>() {
				public Map<Integer,Integer> handle(ResultSet rs) throws SQLException {
					Map<Integer,Integer> gridMap = new HashMap<Integer,Integer>();
					while (rs.next()) {
						gridMap.put(rs.getInt("GRID_ID"), rs.getInt("TYPE"));
					}
					return gridMap;
				}
			};
			return run.query(conn, selectSql, rsHandler);	
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}
	}
	
//	public Page queryMonthTask(JSONObject condition, int curPageNum, int curPageSize) throws Exception {
//		Connection conn = null;
//		try{
//			conn = DBConnector.getInstance().getManConnection();	
//			return TaskOperation.queryMonthTask(conn,condition,curPageNum,curPageSize);
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
//		}finally{
//			DbUtils.commitAndCloseQuietly(conn);
//		}
//	}
//	
	public List<Task> queryTaskAll() throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "SELECT *"
					+ "  FROM TASK"
					+ " WHERE LATEST = 1";
					//+ "   AND STATUS IN (0, 1)";
			return TaskOperation.selectTaskBySql2(conn, selectSql, null);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询任务名称列表
	 * @author Han Shaoming
	 * @param userId
	 * @param taskName
	 * @return
	 * @throws ServiceException 
	 */
	public List<Map<String, Object>> queryTaskNameList(long userId, String taskName) throws ServiceException {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner queryRunner = new QueryRunner();
			
			//根据taskName查询任务数据
			String sql = "SELECT * FROM TASK WHERE NAME LIKE '%"+taskName+"%'";
			Object[] params = {};
			//处理结果集
			ResultSetHandler<List<Map<String, Object>>> rsh = new ResultSetHandler<List<Map<String, Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> taskNameList = new ArrayList<Map<String,Object>>();
					while(rs.next()){
						Map<String,Object> map = new HashMap<String,Object>();
						map.put("taskId",rs.getLong("TASK_ID"));
						map.put("taskName",rs.getString("NAME"));
						taskNameList.add(map);
					}
					return taskNameList;
				}
			};
			//获取数据
			List<Map<String, Object>> list = queryRunner.query(conn, sql, rsh, params);
			//日志
			log.info("查询的task数据的sql"+sql);
			log.info("查询的task数据"+list.toString());
			return list;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public JSONArray getGridListByTaskId(int taskId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			return getGridListByTaskId(conn, taskId);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public JSONArray getGridListByTaskId(Connection conn,int taskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "SELECT M.GRID_ID FROM TASK_GRID_MAPPING M WHERE M.TASK_ID = " + taskId;
			
			ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>() {
				public JSONArray handle(ResultSet rs) throws SQLException {
					ArrayList<String> arrayList = new ArrayList<String>();
					while(rs.next()) {
						arrayList.add(rs.getString("GRID_ID"));
					}
					return JSONArray.fromObject(arrayList);
				}
			};
			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 生管角色发布二代编辑任务后，点击打开小窗口可查看发布进度： 查询cms任务发布进度
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public List<Map<String,Integer>> queryTaskCmsProgress(int taskId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			return queryTaskCmsProgress(conn,taskId);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 生管角色发布二代编辑任务后，点击打开小窗口可查看发布进度： 查询cms任务发布进度
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public List<Map<String,Integer>> queryTaskCmsProgress(Connection conn,int taskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "SELECT Phase_id，PHASE,STATUS FROM TASK_CMS_PROGRESS WHERE TASK_ID= " + taskId;
			log.info(selectSql);
			ResultSetHandler<List<Map<String,Integer>>> rsHandler = new ResultSetHandler<List<Map<String,Integer>>>() {
				public List<Map<String,Integer>> handle(ResultSet rs) throws SQLException {
					List<Map<String,Integer>> arrayList = new ArrayList<Map<String,Integer>>();
					while(rs.next()) {
						Map<String,Integer> map=new HashMap<String, Integer>();
						map.put("phaseId", rs.getInt("PHASE_ID"));
						//map.put("taskId", rs.getInt("TASK_ID"));
						map.put("phase", rs.getInt("PHASE"));
						map.put("status", rs.getInt("STATUS"));
						arrayList.add(map);
					}
					return arrayList;
				}
			};
			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 生管角色发布二代编辑任务后，点击打开小窗口可查看发布进度： 查询cms任务发布进度
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public TaskCmsProgress queryTaskCmsProgressByType(Connection conn,int taskId,int phaseType) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "SELECT Phase_id,task_id,PHASE,STATUS,parameter FROM TASK_CMS_PROGRESS "
					+ "WHERE TASK_ID= " + taskId+" AND PHASE="+phaseType;
			log.info(selectSql);
			ResultSetHandler<TaskCmsProgress> rsHandler = new ResultSetHandler<TaskCmsProgress>() {
				public TaskCmsProgress handle(ResultSet rs) throws SQLException {
					while(rs.next()) {
						TaskCmsProgress progress=new TaskCmsProgress();
						progress.setTaskId(rs.getInt("task_id"));
						progress.setPhaseId(rs.getInt("phase_id"));
						progress.setPhase(rs.getInt("phase"));
						
						JSONObject parameter = JSONObject.fromObject(rs.getString("PARAMETER"));
						if(parameter.containsKey("meshIds")){
							List<Integer> meshIds = (List<Integer>) JSONArray.toCollection((JSONArray) parameter.get("meshIds"));
							Set<Integer> meshIdSet = new HashSet<Integer>();
							meshIdSet.addAll(meshIds);
							progress.setMeshIds(meshIdSet);
						}
						return progress;
					}
					return null;
				}
			};
			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 生管角色发布二代编辑任务后，点击打开小窗口可查看发布进度： 查询cms任务发布进度
	 * 其中有关于tip转aumark的功能，有其他系统异步执行。执行成功后调用接口修改进度并执行下一步
	 * status 2成功 3失败
	 * @param phaseId
	 * @return
	 * @throws Exception 
	 */
	public void taskUpdateCmsProgress(int phaseId,int status,String message) throws Exception {
		Connection conn=null;
		try{
			log.info("phaseId"+phaseId+"状态修改为"+status);
			conn= DBConnector.getInstance().getManConnection();
			taskUpdateCmsProgress(conn, phaseId, status,message);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 生管角色发布二代编辑任务后，点击打开小窗口可查看发布进度： 查询cms任务发布进度
	 * 其中有关于tip转aumark的功能，有其他系统异步执行。执行成功后调用接口修改进度并执行下一步
	 * @param phaseId
	 * @return
	 * @throws Exception 
	 */
	public void taskUpdateCmsProgress(Connection conn,int phaseId,int status,String message) throws Exception {
		try{
			//修改本阶段执行状态
			updateCmsProgressStatus(conn,phaseId, status,message);
			conn.commit();
			//执行失败，则停止后续操作
			if(status==3){return;}
			//执行成功，则继续后续步骤
			TaskCmsProgress phase = queryCmsProgreeByPhaseId(conn, phaseId);
			List<Map<String, Integer>> phaseList = queryTaskCmsProgress(conn,phase.getTaskId());
			//查询前2个并行阶段是否执行成功
			Map<Integer, Integer> phaseStatusMap=new HashMap<Integer, Integer>();
			Map<Integer, Integer> phaseIdMap=new HashMap<Integer, Integer>();
			for(Map<String, Integer> phaseTmp:phaseList){
				phaseStatusMap.put(phaseTmp.get("phase"),phaseTmp.get("status"));
				phaseIdMap.put(phaseTmp.get("phase"),phaseTmp.get("phaseId"));
			}
			int curPhase=phase.getPhase();
			//新增状态4。0创建1进行中2成功3失败4tip转aumark（无tips可转）
			if(curPhase==1){
				if(phaseStatusMap.get(2)!=2&&phaseStatusMap.get(2)!=4){return;}
			}else{//2,3,4
				for(int i=1;i<curPhase;i++){
					if(phaseStatusMap.get(i)!=2&&phaseStatusMap.get(i)!=4){return;}
				}
			}
			if(curPhase==1||curPhase==2){//日落月
				TaskCmsProgress returnProgress=closeDay2MonthMesh(conn, phaseIdMap.get(3));
				if(returnProgress.getStatus()==0){return;}
				taskUpdateCmsProgress(conn, phaseIdMap.get(3),returnProgress.getStatus(),returnProgress.getMessage());
				if(returnProgress.getStatus()==3){return;}
				curPhase=3;}
			if(curPhase==3){//cms任务创建
				TaskCmsProgress returnProgress=createCmsTask(conn, phaseIdMap.get(4));
				if(returnProgress.getStatus()==0){return;}
				taskUpdateCmsProgress(conn, phaseIdMap.get(4),returnProgress.getStatus(),returnProgress.getMessage());
				if(returnProgress.getStatus()==3){return;}
				curPhase=4;
			}
			if(curPhase==4){
				//更新task状态
				List<Integer> cmsTaskIdArray=new ArrayList<Integer>();
				cmsTaskIdArray.add(phase.getTaskId());
				TaskOperation.updateStatus(conn, cmsTaskIdArray,1);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**根据阶段自动执行相关步骤
	 * @param 
	 * @return 
	 */
	public void exeCmsProgree(int phaseId,int phase) throws Exception{
		Connection conn=null;
		try{
			conn= DBConnector.getInstance().getManConnection();
			TaskCmsProgress returnProgress =new TaskCmsProgress();
			returnProgress.setStatus(0);
			if(phase==1){
				returnProgress =day2month(conn, phaseId);
			}else if(phase==2){
				returnProgress =tips2Aumark(conn, phaseId);
			}else if(phase==3){
				returnProgress =closeDay2MonthMesh(conn, phaseId);
			}else if(phase==4){
				returnProgress =createCmsTask(conn, phaseId);
			}
			if(returnProgress.getStatus()==0){
				updateCmsProgressStatus(conn, phaseId, returnProgress.getStatus(), returnProgress.getMessage());
				return;}
			taskUpdateCmsProgress(conn, phaseId, returnProgress.getStatus(),returnProgress.getMessage());
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			taskUpdateCmsProgress(conn, phaseId, 3,e.getMessage());
			throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**日落月
	 * @param 
	 * @return 
	 */
	public TaskCmsProgress day2month(Connection conn,int phaseId) throws Exception{
		TaskCmsProgress returnProgress=new TaskCmsProgress();
		try{			
			//阶段状态修改成进行中
			int i=updateCmsProgressStatusStart(conn,phaseId, 1);
			conn.commit();
			if(i==0){returnProgress.setStatus(0);return returnProgress;}
			//创建日落月job
			TaskCmsProgress phase = queryCmsProgreeByPhaseId(conn, phaseId);
			JobApi jobApi=(JobApi) ApplicationContextUtil.getBean("jobApi");
			//{"specRegionId":1,"specMeshes":[605634,605603]}
			JSONObject jobDataJson=new JSONObject();
			jobDataJson.put("specRegionId", phase.getRegionId());
			jobDataJson.put("specMeshes", phase.getMeshIds());
			jobDataJson.put("phaseId", phaseId);
			long jobId=jobApi.createJob("day2MonSync", jobDataJson, phase.getCreateUserId(),Long.valueOf(phase.getTaskId()), "日落月");
			returnProgress.setStatus(0);
			returnProgress.setMessage("jobId:"+jobId);
			return returnProgress;
		}catch(Exception e){
			//DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			returnProgress.setStatus(3);
			returnProgress.setMessage(e.getMessage());
			return returnProgress;
			//throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**tip转aumark
	 * @param 
	 * @return 
	 */
	public TaskCmsProgress tips2Aumark(Connection conn,int phaseId) throws Exception{
		TaskCmsProgress returnProgress=new TaskCmsProgress();
		try{			
			//阶段状态修改成进行中
			int i=updateCmsProgressStatusStart(conn,phaseId, 1);
			conn.commit();
			if(i==0){returnProgress.setStatus(0);return returnProgress;}
			//tip转aumark
			Map<String, Object> cmsInfo = getCmsInfo(conn,phaseId);
			JSONObject par=new JSONObject();
			par.put("gdbid", cmsInfo.get("dbId"));
			DatahubApi datahub = (DatahubApi) ApplicationContextUtil
					.getBean("datahubApi");
			DbInfo auDb = datahub.getOnlyDbByType("gen2Au");
			par.put("au_db_ip", auDb.getDbServer().getIp());
			par.put("au_db_username", auDb.getDbUserName());
			par.put("au_db_password", auDb.getDbUserPasswd());
			par.put("au_db_sid",auDb.getDbServer().getServiceName());
			par.put("au_db_port",auDb.getDbServer().getPort());
			par.put("types","");
			par.put("phaseId",phaseId);
			//par.put("grids",getGridListByTaskId((int)cmsInfo.get("cmsId")));
			par.put("collectTaskIds",getCollectTaskIdsByTaskId((int)cmsInfo.get("cmsId")));

			JSONObject taskPar=new JSONObject();
			taskPar.put("manager_id", cmsInfo.get("collectId"));
			taskPar.put("imp_task_name", cmsInfo.get("collectName"));
			taskPar.put("province", cmsInfo.get("provinceName"));
			taskPar.put("city", cmsInfo.get("cityName"));
			taskPar.put("district", cmsInfo.get("blockName"));
			Object workProperty=cmsInfo.get("workProperty");
			if(workProperty==null){workProperty="更新";}
			taskPar.put("job_nature", workProperty);
			Object workType=cmsInfo.get("workType");
			if(workType==null){workType="行人导航";}
			taskPar.put("job_type", workType);
			//taskPar.put("job_type", null);
			
			par.put("taskid", taskPar);
			log.info("tips2Aumark:"+par);
			
			FccApi fccApi = (FccApi) ApplicationContextUtil
					.getBean("fccApi");
			fccApi.tips2Aumark(par);
			returnProgress.setStatus(0);
			returnProgress.setMessage(par.toString());
			return returnProgress;
		}catch(Exception e){
			//DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			returnProgress.setStatus(3);
			returnProgress.setMessage(e.getMessage());
			return returnProgress;
			//throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * @param taskId
	 * @return
	 * @throws ServiceException 
	 */
	public Set<Integer> getCollectTaskIdsByTaskId(int taskId) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			return getCollectTaskIdsByTaskId(conn,taskId);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("getCollectTaskIdsByTaskId失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * @param taskId
	 * @return
	 * @throws ServiceException 
	 */
	public Set<Integer> getCollectTaskIdsByTaskId(Connection conn,int taskId) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			
			String sql = "SELECT TT.TASK_ID"
					+ "  FROM TASK T, TASK TT"
					+ " WHERE T.BLOCK_ID = TT.BLOCK_ID"
					+ "   AND T.PROGRAM_ID = TT.PROGRAM_ID"
					+ "   AND TT.TYPE = 0"
					+ "   AND T.TASK_ID = " + taskId;
			
			log.info("getCollectTaskIdsByTaskId sql :" + sql);
						
			ResultSetHandler<Set<Integer>> rsHandler = new ResultSetHandler<Set<Integer>>() {
				public Set<Integer> handle(ResultSet rs) throws SQLException {
					Set<Integer> result = new HashSet<Integer>();
					while(rs.next()) {
						result.add(rs.getInt("TASK_ID"));
					}
					return result;
				}
			};
			Set<Integer> result =  run.query(conn, sql,rsHandler);
			return result;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("getCollectTaskIdsByTaskId失败，原因为:" + e.getMessage(), e);
		} 
	}

	/**根据任务id关闭日落月开关
	 * @param taskId
	 * @return 关闭成功，返回2；失败，返回3；若阶段状态修改成进行中失败，返回0
	 */
	public TaskCmsProgress closeDay2MonthMesh(Connection conn,int phaseId) throws Exception{
		TaskCmsProgress returnProgress=new TaskCmsProgress();
		try{			
			//阶段状态修改成进行中
			int i=updateCmsProgressStatusStart(conn,phaseId, 1);
			conn.commit();
			if(i==0){
				returnProgress.setStatus(0);
				return returnProgress;}
			//修改开关
			TaskCmsProgress phase = queryCmsProgreeByPhaseId(conn, phaseId);
			Set<Integer> meshs = phase.getMeshIds();
			String updateSql="UPDATE SC_PARTITION_MESHLIST SET OPEN_FLAG = 0 WHERE MESH IN "
					+meshs.toString().replace("[", "(").replace("]", ")");
			Connection meta = null;
			try{
				meta = DBConnector.getInstance().getMetaConnection();
				QueryRunner run = new QueryRunner();
				run.update(meta,updateSql);
				returnProgress.setStatus(2);
				return returnProgress;
			}catch(Exception e){
				//DbUtils.rollbackAndCloseQuietly(meta);
				log.error(e.getMessage(), e);
				returnProgress.setStatus(3);
				returnProgress.setMessage(e.getMessage());
				return returnProgress;
				//throw new Exception("失败，原因为:"+e.getMessage(),e);
			}finally {
				DbUtils.commitAndCloseQuietly(meta);
			}
		}catch(Exception e){
			//DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			returnProgress.setStatus(3);
			returnProgress.setMessage(e.getMessage());
			return returnProgress;
			//throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}
	}
	/**
	 * 
	 * @param conn
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> getCmsInfo(Connection conn,int phaseId) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "SELECT CMST.NAME CMS_NAME,"
					+ "       CMST.TASK_ID CMS_ID,"
					+ "       CMST.CREATE_USER_ID,"
					+ "       u.user_nick_name,"
					+ "       T.TASK_ID COLLECT_ID,"
					+ "       T.NAME COLLECT_NAME,"
					+ "       P.PHASE_ID,"
					+ "       R.MONTHLY_DB_ID,"
					+ "       C.PROVINCE_NAME,"
					+ "       C.CITY_NAME,"
					+ "       B.BLOCK_NAME,"
					+ "       B.WORK_PROPERTY,"
					+ "       B.WORK_TYPE"
					+ "  FROM TASK CMST, TASK T, BLOCK B, CITY C, TASK_CMS_PROGRESS P, REGION R,user_info u"
					+ " WHERE P.PHASE_ID = "+phaseId
					+ "   AND CMST.REGION_ID = R.REGION_ID"
					+ "   AND CMST.PROGRAM_ID = T.PROGRAM_ID"
					+ "   AND CMST.TASK_ID = P.TASK_ID"
					+ "   AND CMST.BLOCK_ID=T.BLOCK_ID"
					+ "   AND CMST.CREATE_USER_ID = u.user_ID(+)"
					+ "   AND T.TYPE = 0"
					+ "   AND CMST.BLOCK_ID = B.BLOCK_ID"
					+ "   AND B.CITY_ID = C.CITY_ID";
			log.info(selectSql);
			ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					Map<String, Object> result=new HashMap<String, Object>();
					if(rs.next()) {
						result.put("cmsId", rs.getInt("CMS_ID"));
						result.put("cmsName", rs.getString("CMS_NAME"));
						result.put("createUserId", rs.getInt("CREATE_USER_ID"));
						result.put("userNickName", rs.getString("user_nick_name"));
						result.put("collectId", rs.getInt("COLLECT_ID"));
						result.put("collectName", rs.getString("COLLECT_NAME"));
						result.put("dbId", rs.getInt("MONTHLY_DB_ID"));
						result.put("phaseId", rs.getInt("PHASE_ID"));
						result.put("provinceName", rs.getString("PROVINCE_NAME"));
						result.put("cityName", rs.getString("CITY_NAME"));
						result.put("blockName", rs.getString("BLOCK_NAME"));
						result.put("workProperty", rs.getString("WORK_PROPERTY"));
						result.put("workType", rs.getString("WORK_TYPE"));
					}
					return result;
				}
			};
			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**cms任务创建
	 * 管理库中查询cms任务创建所需参数，然后调用cms任务创建http；http返回成功，则成功；否则失败。
	 * @param taskId
	 * @return 关闭成功，返回2；失败，返回3；若阶段状态修改成进行中失败，返回0
	 */
	public TaskCmsProgress createCmsTask(Connection conn,int phaseId) throws Exception{
		TaskCmsProgress returnProgress=new TaskCmsProgress();
		try{
			log.info("start createCmsTask"+phaseId);
			int i=updateCmsProgressStatusStart(conn,phaseId, 1);
			conn.commit();
			if(i==0){
				returnProgress.setStatus(0);
				return returnProgress;}
			Map<String, Object> cmsInfo = getCmsInfo(conn,phaseId);
			JSONObject par=new JSONObject();
			DatahubApi datahub = (DatahubApi) ApplicationContextUtil
					.getBean("datahubApi");
			DbInfo metaDb = datahub.getOnlyDbByType("metaRoad");
			par.put("metaIp", metaDb.getDbServer().getIp());
			par.put("metaUserName", metaDb.getDbUserName());
			
			DbInfo auDb = datahub.getOnlyDbByType("gen2Au");
			par.put("fieldDbIp", auDb.getDbServer().getIp());
			par.put("fieldDbName", auDb.getDbUserName());

			JSONObject taskPar=new JSONObject();
			taskPar.put("taskName", cmsInfo.get("cmsName"));
			taskPar.put("fieldTaskId", cmsInfo.get("collectId"));
			taskPar.put("taskId", cmsInfo.get("cmsId"));
			taskPar.put("province", cmsInfo.get("provinceName"));
			taskPar.put("city", cmsInfo.get("cityName"));
			taskPar.put("town", cmsInfo.get("blockName"));
			Object workProperty=cmsInfo.get("workProperty");
			if(workProperty==null){workProperty="更新";}
			taskPar.put("workType", workProperty);
			Object workType=cmsInfo.get("workType");
			if(workType==null){workType="行人导航";}
			taskPar.put("area",workType);
			taskPar.put("userId", cmsInfo.get("userNickName"));
			taskPar.put("workSeason", SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
			TaskCmsProgress phase = queryCmsProgreeByPhaseId(conn, phaseId);
			taskPar.put("meshs",phase.getMeshIds());
			
			//判断之前tip2aumark的过程，是有tips还是没有tips
			List<Map<String, Integer>> phaseList = queryTaskCmsProgress(conn,phase.getTaskId());
			Map<Integer, Integer> phaseStatusMap=new HashMap<Integer, Integer>();
			for(Map<String, Integer> phaseTmp:phaseList){
				phaseStatusMap.put(phaseTmp.get("phase"),phaseTmp.get("status"));
			}
			taskPar.put("hasAumark",true);
			if(phaseStatusMap.get(2)==4){taskPar.put("hasAumark",false);}
			
			par.put("taskInfo", taskPar);
			
			String cmsUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.cmsUrl);
			Map<String,String> parMap = new HashMap<String,String>();
			parMap.put("parameter", par.toString());
			log.info(par.toString());
			returnProgress.setMessage(par.toString());
			String result = ServiceInvokeUtil.invoke(cmsUrl, parMap, 10000);
			//result="{success:false, msg:\"没有找到用户名为【fm_meta_all_sp6】元数据库版本信息！\"}";
			JSONObject res=new JSONObject();
			try{
				res=JSONObject.fromObject(result);}
			catch(Exception e){
				log.error(e.getMessage(), e);
				returnProgress.setStatus(3);
				returnProgress.setMessage(returnProgress.getMessage()+result);
				return returnProgress;
			}
			boolean success=(boolean)res.get("success");
			log.info("end createCmsTask"+phaseId);
			if(success){
				returnProgress.setStatus(2);
				return returnProgress;
			}
			else{
				log.error("cms error msg"+res.get("msg"));
				returnProgress.setStatus(3);
				returnProgress.setMessage(returnProgress.getMessage()+"cms error:"+res.get("msg").toString());
				return returnProgress;}
		}catch(Exception e){
			log.error(e.getMessage(), e);
			returnProgress.setStatus(3);
			returnProgress.setMessage(returnProgress.getMessage()+e.getMessage());
			return returnProgress;
		}
	}
	
	/**
	 * 是否继续执行后续cms任务
	 * @param phaseId
	 * @return
	 * @throws Exception 
	 */
	public TaskCmsProgress queryCmsProgreeByPhaseId(Connection conn,int phaseId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
//			String selectSql = "SELECT T.PHASE_ID,"
//					+ "       T.TASK_ID,"
//					+ "       M.GRID_ID,"
//					+ "       T.PHASE,"
//					+ "       TS.REGION_ID,"
//					+ "       TS.CREATE_USER_ID,"
//					+ "       U.USER_NICK_NAME"
//					+ "  FROM TASK_CMS_PROGRESS T, TASK_GRID_MAPPING M, TASK TS, USER_INFO U"
//					+ " WHERE T.PHASE_ID = "+phaseId
//					+ "   AND T.TASK_ID = TS.TASK_ID"
//					+ "   AND TS.CREATE_USER_ID = U.USER_ID"
//					+ "   AND T.TASK_ID = M.TASK_ID ";
			String selectSql = "SELECT T.PHASE_ID,"
					+ "       T.TASK_ID,"
					+ "       T.PARAMETER,"
					+ "       T.PHASE,"
					+ "       TS.REGION_ID,"
					+ "       TS.CREATE_USER_ID,"
					+ "       U.USER_NICK_NAME"
					+ "  FROM TASK_CMS_PROGRESS T, TASK TS, USER_INFO U"
					+ " WHERE T.PHASE_ID = "+phaseId
					+ "   AND T.TASK_ID = TS.TASK_ID"
					+ "   AND TS.CREATE_USER_ID = U.USER_ID(+)";
			ResultSetHandler<TaskCmsProgress> rsHandler = new ResultSetHandler<TaskCmsProgress>() {
				public TaskCmsProgress handle(ResultSet rs) throws SQLException {
					
					while(rs.next()) {
						TaskCmsProgress progress=new TaskCmsProgress();
						progress.setTaskId(rs.getInt("task_id"));
						progress.setPhaseId(rs.getInt("phase_id"));
						progress.setPhase(rs.getInt("phase"));
						progress.setCreateUserId(rs.getInt("create_user_id"));
						progress.setUserNickName(rs.getString("user_nick_name"));
						progress.setRegionId(rs.getInt("region_id"));
						
						JSONObject parameter = JSONObject.fromObject(rs.getString("PARAMETER"));
						if(parameter.containsKey("meshIds")){
							List<Integer> meshIds = (List<Integer>) JSONArray.toCollection((JSONArray) parameter.get("meshIds"));
							Set<Integer> meshIdSet = new HashSet<Integer>();
							meshIdSet.addAll(meshIds);
							progress.setMeshIds(meshIdSet);
						}
						return progress;
						
//						if(progress.getGridIds()==null){
//							progress.setGridIds(new HashSet<Integer>());
//						}
//						int gridId=rs.getInt("GRID_ID");
//						progress.getGridIds().add(gridId);
//						if(progress.getMeshIds()==null){
//							progress.setMeshIds(new HashSet<Integer>());
//						}
//						String gridStr=String.valueOf(gridId);
//						String mesh=gridStr.substring(0,gridStr.length()-2);
//						progress.getMeshIds().add(Integer.valueOf(mesh));
					}
					return null;
				}
			};
			return run.query(conn, selectSql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 修改二代编辑任务发布阶段执行状态
	 * @param phaseId
	 * @return
	 * @throws Exception 
	 */
	public void updateCmsProgressStatus(Connection conn,int phaseId,int status,String message) throws Exception {
		try{
			if(status==0&&(message==null||message.isEmpty())){return;}
			if(message!=null&&message.length()>500){message=message.substring(0, 500);}
			QueryRunner run = new QueryRunner();
			String selectSql ="";
			if(status==0){
				selectSql = "UPDATE TASK_CMS_PROGRESS SET message=substr(message||?,0,1000) WHERE PHASE_ID = "+phaseId ;
			}else{
				String updateMsg="";
				if(message!=null){updateMsg=",message=substr(message||?,0,1000)";}
				selectSql = "UPDATE TASK_CMS_PROGRESS SET STATUS = "+status+updateMsg+",end_date=sysdate WHERE PHASE_ID = "+phaseId ;
			}
			log.info("updateCmsProgressStatus:"+selectSql);
			if(message==null||message.isEmpty()){
				run.update(conn, selectSql);}
			else{run.update(conn, selectSql,message);}
			log.info("phaseId:"+phaseId+",status:"+status+",message:"+message);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("updateCmsProgressStatus失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 创建cmsProgress
	 * @param parameter 
	 * @param phaseId
	 * @return
	 * @throws Exception 
	 */
	public void createCmsProgress(Connection conn,int taskId,int phase, JSONObject parameter) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "INSERT INTO TASK_CMS_PROGRESS P"
					+ "  (TASK_ID, PHASE, STATUS, CREATE_DATE, PHASE_ID,PARAMETER)"
					+ "VALUES"
					+ "  ("+taskId+","+phase+", 0, SYSDATE, PHASE_SEQ.NEXTVAL,?)" ;
			Clob clob=ConnectionUtil.createClob(conn);
			clob.setString(1, parameter.toString());
			
			run.update(conn, selectSql,clob);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 修改二代编辑任务发布阶段执行状态
	 * @param phaseId
	 * @return
	 * @throws Exception 
	 */
	public int updateCmsProgressStatusStart(Connection conn,int phaseId,int status) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "UPDATE TASK_CMS_PROGRESS SET STATUS = "+status+",start_date=sysdate WHERE STATUS IN(0,3) AND PHASE_ID = "+phaseId ;
			return run.update(conn, selectSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task下grid列表失败，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * @param taskId
	 * @return
	 * @throws ServiceException 
	 */
	public JSONObject queryWktByTaskId(int taskId) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			String selectSql = "SELECT M.GRID_ID FROM TASK_GRID_MAPPING M WHERE M.TASK_ID = " + taskId;
			log.info("queryWktByTaskId sql :" + selectSql);
			
			ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>() {
				public JSONArray handle(ResultSet rs) throws SQLException {
					JSONArray json = new JSONArray(); 				
					while (rs.next()) {
						json.add(rs.getInt("GRID_ID"));
					}
					return json;
				}
			};
			JSONArray gridIds =  run.query(conn, selectSql,rsHandler);
			String wkt = GridUtils.grids2Wkt(gridIds);
			JSONObject json = Geojson.wkt2Geojson(wkt);
			return json;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询wkt失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 根据任务id获取相同项目block下，类型为type的任务id
	 * @param taskId
	 * @param i
	 * @return
	 * @throws ServiceException 
	 */
	public int getTaskIdByTaskIdAndTaskType(Integer taskId, int type) throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			String selectSql = "SELECT T1.TASK_ID FROM TASK T ,TASK T1"
					+ " WHERE T.LATEST =1"
					+ " AND T1.LATEST = 1"
					+ " AND T1.PROGRAM_ID = T.PROGRAM_ID"
					+ " AND T1.BLOCK_ID = T.BLOCK_ID"
					+ " AND T1.REGION_ID = T.REGION_ID"
					+ " AND T1.TYPE = " + type
					+ " AND T.TASK_ID = " + taskId;
			log.info("getTaskIdByTaskIdAndTaskType sql :" + selectSql);
			
			ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {
				public Integer handle(ResultSet rs) throws SQLException {
					int taskId = 0;				
					if (rs.next()) {
						taskId = rs.getInt("TASK_ID");
					}
					return taskId;
				}
			};
			int result =  run.query(conn, selectSql,rsHandler);
			return result;
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询wkt失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 任务关闭再开启，需要对应：任务关闭再开启后，生成一条新的任务记录，属性全部复制，状态=草稿。原来任务关联的子任务不继承到新任务中
	 * @param userId
	 * @param taskId
	 * @throws ServiceException
	 */
	public void reOpen(Long userId,int taskId)  throws ServiceException {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			int newTaskId=TaskOperation.getNewTaskId(conn);
			//根据新任务号，复制原有任务的信息（task/task_grid_mapping）
			TaskOperation.copyTask(conn,userId,newTaskId,taskId);
			//旧任务状态变更为失效
			TaskOperation.updateLatest(conn, taskId);
			//任务对应的block若为关闭，则同步更新为已规划，否则不动
			TaskOperation.reOpenBlockByTask(conn,newTaskId);
			
			//修改打开二代编辑任务对应的日落月配置表图幅
			Task task = queryByTaskId(conn,taskId);
			//Task task = queryByTaskId(newTaskId);
			if(task.getType() == 3){
				updateDayToMonthMesh(conn,taskId);
			}
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("关闭任务重新开启失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * @param taskId
	 * @throws Exception 
	 */
	private void updateDayToMonthMesh(Connection conn,int taskId) throws Exception {
		TaskCmsProgress progress = queryTaskCmsProgressByType(conn, taskId,1);
		Set<Integer> meshIdSet = new HashSet<Integer>();
		if(progress!=null){
			meshIdSet=progress.getMeshIds();
		}else{
			JSONArray gridList = getGridListByTaskId(conn,taskId);
			
			for(Object gridId:gridList.toArray()){
				meshIdSet.add(Integer.parseInt(gridId.toString().substring(0, gridId.toString().length()-2)));
			}
		}
		if(meshIdSet.size() <= 0){return;}
		Connection meta = null;
		try{
			meta = DBConnector.getInstance().getMetaConnection();
			QueryRunner run = new QueryRunner();
			String updateSql="UPDATE SC_PARTITION_MESHLIST SET OPEN_FLAG = 1 WHERE MESH IN (" + StringUtils.join(meshIdSet, ",") + ")";
			run.update(meta,updateSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(meta);
			log.error(e.getMessage(), e);
			throw new Exception("失败，原因为:"+e.getMessage(),e);
		}finally {
			DbUtils.commitAndCloseQuietly(meta);
		}
	}

	/**
	 * @param conn 
	 * @param programId
	 * @return
	 * @throws ServiceException 
	 */
	public List<Task> getTaskByProgramId(final Connection conn, int programId) throws ServiceException {
		try {
			QueryRunner run = new QueryRunner();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append(" SELECT T.TASK_ID,T.TYPE,T.GROUP_ID,T.PLAN_START_DATE,T.PLAN_END_DATE,t.work_kind");
			sb.append("   FROM TASK T ");
			sb.append("  WHERE T.PROGRAM_ID = " + programId);
			
			String sql = sb.toString();
			
			log.info("getTaskByProgramId sql :" + sql);
			
			
			ResultSetHandler<List<Task>> rsHandler = new ResultSetHandler<List<Task>>() {
				public List<Task> handle(ResultSet rs) throws SQLException {
					List<Task> result = new ArrayList<Task>();
					while(rs.next()) {
						Task task = new Task();
						task.setTaskId(rs.getInt("TASK_ID"));
						task.setType(rs.getInt("TYPE"));
						task.setGroupId(rs.getInt("GROUP_ID"));
						task.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						task.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						task.setWorkKind(rs.getString("WORK_KIND"));
						try {
							task.setGridIds(getGridMapByTaskId(conn,task.getTaskId()));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						result.add(task);
					}
					return result;
				}
			};
			List<Task> result =  run.query(conn, sql,rsHandler);
			return result;
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("getTaskByProgramId失败，原因为:" + e.getMessage(), e);
		}
	}

	/**
	 * @param conn
	 * @param bean
	 * @throws Exception 
	 */
	public void createWithBeanWithTaskId(Connection conn, Task bean) throws Exception {
		try{
			/*与block关联的常规任务
			 * 1.修改同类型task的latest
			 * 2.如果block没有开启则开启block
			 */
			if(bean.getBlockId()!=0){
				TaskOperation.updateLatest(conn,bean.getProgramId(),bean.getRegionId(),bean.getBlockId(),bean.getType());
				List<Integer> blockList = new ArrayList<Integer>();
				blockList.add(bean.getBlockId());
				BlockOperation.openBlockByBlockIdList(conn,blockList);
			}	
			//创建任务
			TaskOperation.insertTask(conn, bean);
			
			// 插入TASK_GRID_MAPPING
			if(bean.getGridIds() != null){
				TaskOperation.insertTaskGridMapping(conn, bean);
			}
			
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}

	public void batchQuickTask(int dbId, int subtaskId,
			int taskId, JSONArray pois, JSONArray tips) throws Exception{
		log.info("batchQuickTask:dbId="+dbId+",subtaskId="+subtaskId+",taskId="+taskId);
		log.info("pois="+pois);
		log.info("tips="+tips);
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getConnectionById(dbId);
			if(pois!=null&&pois.size()>0){//批poi的快线任务号
				List<Long> poiPids=new ArrayList<Long>();
				for(Object poiPid:pois){
					poiPids.add(Long.valueOf(poiPid.toString()));
				}
				batchPoiQuickTask(conn, taskId, subtaskId, poiPids);
			}
//			if(tips!=null&&tips.size()>0){//批tips的快线任务号
//			List<String> tipsPids=new ArrayList<String>(); 
// 				for(Object tipRowkey:tips){ 
// 					tipsPids.add(tipRowkey.toString()); 
// 				}
//				FccApi api=(FccApi)ApplicationContextUtil.getBean("fccApi"); 
//				api.batchQuickTask(taskId, subtaskId,tipsPids); 
// 			}
		}catch(Exception e){
			log.error("", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * poi批快线任务号
	 * @param dailyConn
	 * @param poiTaskMap
	 * @throws SQLException 
	 * @author songhe
	 */
	@SuppressWarnings("unused")
	private void batchPoiQuickTask(Connection dailyConn, int taskId, int subtaskId, List<Long> PoiQuickT) throws SQLException {
		String updateSql = "update POI_EDIT_STATUS set QUICK_TASK_ID=? , QUICK_SUBTASK_ID=? where PID=? and QUICK_TASK_ID = 0";
		QueryRunner run = new QueryRunner();
		Object[][] params = new Object[PoiQuickT.size()][3] ;
		
		int i = 0;
		for(int j = 0; j < PoiQuickT.size(); j++){
			Object[] pidMap = new Object[3];
			pidMap[0] = taskId;
			pidMap[1] = subtaskId;
			pidMap[2] = PoiQuickT.get(j);
			params[i] = pidMap;
			i++;
		}
		run.batch(dailyConn, updateSql, params);
	}

	public Map<String, Object> getCollectGroupByTask(int taskId, int workKind,
			int snapshot) throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			Task task = queryByTaskId(taskId);
			if(workKind==1&&task.getGroupId()!=0){
				UserGroup conditionGroup=new UserGroup();
				conditionGroup.setGroupId(task.getGroupId());
				UserGroup resultGroup=UserGroupService.getInstance().query(conditionGroup);
				List<UserInfo> users=UserInfoService.getInstance().list(conditionGroup);
				JSONObject resultJson = JSONObject.fromObject(resultGroup);
				resultJson.put("users", JSONArray.fromObject(users));
				return resultJson;
			}
			String admin = selectAdminCode(task.getProgramId());
			int type=workKind+1;
			UserGroup resultGroup=UserGroupService.getInstance().getGroupByAminCode(conn, admin, type);
			List<UserInfo> users=UserInfoService.getInstance().list(resultGroup);
			JSONObject resultJson = JSONObject.fromObject(resultGroup);
			resultJson.put("users", JSONArray.fromObject(users));
			return resultJson;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 无任务数据批中线任务号
	 * @param Connection dailyConn
	 * @param int taskID
	 * @param int subtaskId
	 * @param String wkt
	 * @author songhe
	 */
	private int batchNoTaskPoiMidTaskId(Connection dailyConn, int taskID,int subtaskId, String wkt) throws SQLException {
		String selectPid = "select pes.pid"
				 + " from ix_poi ip, poi_edit_status pes"
				 + " where ip.pid = pes.pid"
				 + " and pes.status != 0"
				 + " AND sdo_within_distance(ip.geometry, sdo_geometry(?, 8307), 'mask=anyinteract') = 'TRUE' and pes.medium_task_id = 0 and pes.quick_task_id = 0 and pes.quick_subtask_id = 0";
		String updateSql = "update poi_edit_status set medium_task_id= "+taskID+ ",medium_subtask_id="+subtaskId+ " where pid in ("+selectPid+")";
		QueryRunner run=new QueryRunner();
		Clob clob = ConnectionUtil.createClob(dailyConn);
		clob.setString(1, wkt);
		return run.update(dailyConn, updateSql,clob);
	}
	
	
	/**
	 * 根据中线任务，批无任务数据中线任务号
	 * @param conn 
	 * @param task
	 * @return 
	 */
	private int batchNoTaskMidData(Connection conn, Task task) throws Exception{
		Connection dailyConn=null;
		try{
			Region region = RegionService.getInstance().query(conn,task.getRegionId());
			dailyConn = DBConnector.getInstance().getConnectionById(region.getDailyDbId());
			//无任务tips批中线任务号
			JSONArray gridIds = TaskService.getInstance().getGridListByTaskId(task.getTaskId());
			String wkt = GridUtils.grids2Wkt(gridIds);
//			log.info("无任务的tips批中线任务号:taskId="+task.getTaskId()+",wkt="+wkt);
//			FccApi api=(FccApi) ApplicationContextUtil.getBean("fccApi");
//			api.batchNoTaskDataByMidTask(wkt, task.getTaskId());
			
			//自动创建采集子任务，范围=采集任务范围
			Subtask subtask=new Subtask();
			subtask.setName(task.getName());
			subtask.setWorkKind(1);
			subtask.setDescp("无任务转中自动创建");
			subtask.setPlanStartDate(task.getPlanStartDate());
			subtask.setPlanEndDate(task.getPlanEndDate());
			subtask.setTaskId(task.getTaskId());
			subtask.setType(0);
			subtask.setStage(0);
			subtask.setGridIds(task.getGridIds());
			String subtaskwkt = GridUtils.grids2Wkt(JSONArray.fromObject(subtask.getGridIds()));
			subtask.setGeometry(subtaskwkt);
			int subtaskId=SubtaskService.getInstance().createSubtaskWithSubtaskId(conn, subtask);
			log.info("无任务的poi批中线任务号:dbid="+region.getDailyDbId()+",subtaskId="+subtaskId+",taskId="+task.getTaskId()+",wkt="+wkt);
			//无任务的poi批中线任务号	
			int updateNum=batchNoTaskPoiMidTaskId(dailyConn, task.getTaskId(),subtaskId, wkt);
			if(updateNum==0){
				log.info("该中线任务范围内没有poi成果，所建采集子任务删除：subtaskId="+subtaskId+",task="+task.getTaskId());
				SubtaskService.getInstance().delete(conn,subtaskId);
			}
			//修改无任务转中操作状态为 1已转
			StaticsOperation.changeTaskConvertFlagToOK(conn, task.getTaskId());
			return updateNum;
		}catch(Exception e){
			log.error("", e);
			DbUtils.rollbackAndCloseQuietly(dailyConn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(dailyConn);
		}	
	}
	/**
	 * 应用场景：中线采集任务--无任务转中按钮
		功能：
		1.判断task_progress表中是否有该任务记录
		a.有，创建/进行中时，不做处理
		b.其他，进行第2步
		2.增加新记录，状态创建
		3.创建taskOther2MediumJob的job
		4.记录修改状态为进行中
	 * @param task
	 * @return
	 * @throws ServiceException 
	 */
	public int createTaskOther2MediumJob(Long userId,int taskId) throws ServiceException{
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			int phaseId=0;
			//获取最新的记录
			TaskProgress latestProgress = TaskProgressOperation.queryLatestByTaskId(conn, taskId, TaskProgressOperation.taskOther2MediumJob);
			//无记录/成功/失败时，增加新的记录
			if(latestProgress==null||latestProgress.getStatus()==TaskProgressOperation.taskSuccess||latestProgress.getStatus()==TaskProgressOperation.taskFail){
				phaseId=TaskProgressOperation.getNewPhaseId(conn);
				latestProgress=new TaskProgress();
				latestProgress.setPhaseId(phaseId);
				latestProgress.setTaskId(taskId);
				latestProgress.setPhase(TaskProgressOperation.taskOther2MediumJob);
				TaskProgressOperation.create(conn, latestProgress);
				conn.commit();
			}
			phaseId=latestProgress.getPhaseId();
			JobApi api=(JobApi) ApplicationContextUtil.getBean("jobApi");
			JSONObject request=new JSONObject();
			request.put("phaseId", phaseId);
			request.put("taskId", taskId);
			long jobId=api.createJob("taskOther2MediumJob", request, userId, taskId, "无任务采集成果入中");
			TaskProgressOperation.updateProgress(conn, phaseId, 0, "jobid:"+jobId);
			TaskProgressOperation.startProgress(conn, userId, phaseId);			
			return phaseId;
		}catch(Exception e){
			log.error("", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException(e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 根据taskId批处理对应该任务的无任务POI和TIPS
	 * @param userId 
	 * @param taskId
	 * @return 
	 */
	public int batchMidTaskByTaskId(int taskId) throws ServiceException{
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			Task task = queryByTaskId(conn, taskId);
			return batchNoTaskMidData(conn, task);
		}catch(Exception e){
			log.error("", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException(e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 采集任务列表
	 * @throws Exception 
	 *   
	 */
	public List<Map<String, Object>> midCollectTaskList() throws Exception{
		Connection con = null;
		try {
			con = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			String selectSql = "select st.status as substatus, st.subtask_id, st.geometry as st_geometry, st.name as st_name, st.stage as st_stage, st.work_kind, b.geometry, "
					+ "p.program_id, p.name as p_name, p.type as p_type, p.status as p_status,"
					+ "t.block_id, t.task_id, t.status, t.name, t.plan_start_date, t.plan_end_date "
					+ "from subtask st, task t, program p, block b where t.program_id = p.program_id and t.block_id = b.block_id and st.task_id = t.task_id"
					+ " and t.type = 0 and t.latest = 1";
			
			return run.query(con, selectSql, new ResultSetHandler<List<Map<String, Object>>>(){
				@Override
				public List<Map<String, Object>> handle(ResultSet result) throws SQLException {
					List<Map<String, Object>> taskList = new ArrayList<>();
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
					while(result.next()){
						Map<String, Object> task = new HashMap<>(32);
						task.put("programId", result.getInt("program_id"));
						task.put("programName", result.getString("p_name"));
						task.put("programType", result.getInt("p_type"));
						task.put("programStatus", result.getInt("p_status"));
						task.put("blockId", result.getInt("block_id"));
						task.put("taskId", result.getInt("task_id"));
						task.put("taskStatus", result.getInt("status"));
						task.put("taskName", result.getString("name"));
						Timestamp planStartDate = result.getTimestamp("plan_start_date");
						Timestamp planEndDate = result.getTimestamp("plan_end_date");
						if(planStartDate != null){
							task.put("taskPlanStartDate", df.format(planStartDate));
						}else{
							task.put("taskPlanStartDate", "");
						}
						if(planEndDate != null){
							task.put("taskPlanEndDate", df.format(planEndDate));
						}else{
							task.put("taskPlanEndDate", "");
						}
						
						STRUCT struct = (STRUCT) result.getObject("geometry");
						try {
							String clobStr = GeoTranslator.struct2Wkt(struct);
							task.put("geometry", Geojson.wkt2Geojson(clobStr));
						} catch (Exception e) {
							log.error("geometry转JSON失败，原因为:" + e.getMessage());
						}
						//modify by song
						//服务情报对接变更，添加返回子任务信息2017/08/16
						task.put("subtaskId", result.getInt("subtask_id"));
						STRUCT stStruct = (STRUCT) result.getObject("st_geometry");
						try{
							String clobStr = GeoTranslator.struct2Wkt(stStruct);
							task.put("subtaskGeometry", Geojson.wkt2Geojson(clobStr));
						}catch(Exception e){
							log.error("子任务geometry转JSON失败，原因为:" + e.getMessage());
						}
						task.put("subtaskName", result.getString("st_name"));
						task.put("subtaskStage", result.getInt("st_stage"));
						task.put("subtaskWorkKind", result.getString("work_kind"));
						task.put("subtaskStatus", result.getString("substatus"));
						
						taskList.add(task);
					}
					return taskList;
				}});
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(con);
			log.error("获取采集任务列表失败，原因为：" + e.getMessage());
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(con);
		}
	}
	/**
	 * 获取所有待发布的任务id的列表
	 * 应用场景：任务发布-全选按钮
	 * @param programId
	 * @return
	 */
	public List<Integer> allDraftTask(int programId)  throws Exception{
		Connection con = null;
		try {
			con = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			
			String selectSql = "SELECT t.TASK_ID"
					+ "  FROM TASK t"
					+ " WHERE t.PROGRAM_ID = "+programId
					+ "   AND t.TYPE IN (1, 2)"
					+ "   AND t.STATUS = 2"
					+ "   AND t.LATEST = 1"
					+ "   AND t.GROUP_ID != 0"
					//+ "	  AND t.DATA_PLAN_STATUS <> 0"
					+ " UNION ALL"
					+ " SELECT t1.TASK_ID"
					+ "  FROM TASK t1"
					+ " WHERE t1.PROGRAM_ID = "+programId
					+ "   AND t1.TYPE = 0"
					+ "   AND t1.STATUS = 2"
					+ "   AND t1.LATEST = 1"
					+ "   AND (t1.WORK_KIND LIKE '1|%' OR t1.WORK_KIND LIKE '0|1%')"
					+ "   AND t1.GROUP_ID != 0"
					//+ "	  AND t1.DATA_PLAN_STATUS <> 0"
					+ " UNION ALL"
					+ " SELECT t2.TASK_ID "
					+ "  FROM TASK t2"
					+ " WHERE t2.PROGRAM_ID = "+programId
					+ "   AND t2.TYPE = 0"
					+ "   AND t2.STATUS = 2"
					+ "   AND t2.LATEST = 1"
					+ "   AND t2.WORK_KIND LIKE '0|0%'"
					+ "   AND t2.GROUP_ID = 0";
					//+ "	  AND t2.DATA_PLAN_STATUS <> 0";
			
			return run.query(con, selectSql, new ResultSetHandler<List<Integer>>(){
				@Override
				public List<Integer> handle(ResultSet rs) throws SQLException {
					List<Integer> taskIds=new ArrayList<Integer>();
					while(rs.next()){
						taskIds.add(rs.getInt("TASK_ID"));
					}
					return taskIds;
				}});
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(con);
			log.error("获取采集任务列表失败，原因为：" + e.getMessage());
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(con);
		}
	}
	
	
	/**
	 * 获取待数据规划采集任务列表
	 * 应用场景：中线项目下，具有同时满足草稿状态+未进行数据规划的采集任务的采集任务列表
	 * @author songhe
	 * @return List
	 * @throws SQLException 
	 */
	public List<Map<String, Object>> unPlanlist(JSONObject json) throws SQLException{
		Connection con = null;
		try{
			con = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			StringBuffer sb = new StringBuffer();
			
			//String programId = json.getString("programId");

			sb.append("select t.block_id, t.task_id,t.name,t.region_id from TASK t where ");//t.program_id = "+programId);
			//未规划草稿状态
			sb.append(" t.data_plan_status = 0 and t.work_kind like '%1|%' ");
			//中线采集任务
			sb.append(" and t.type = 0 ");
			
			if(json.containsKey("programId")){
				sb.append(" and t.program_id = "+json.getString("programId"));
			}
			
			if(json.containsKey("condition")){
				if(json.getJSONObject("condition").containsKey("name") && json.getJSONObject("condition").getString("name").length() > 0){
					String name = json.getJSONObject("condition").getString("name");
					sb.append(" and t.name like '%"+name+"%'");
				}
			}
			
			String sql = sb.toString();
			ResultSetHandler<List<Map<String, Object>>> rs = new ResultSetHandler<List<Map<String, Object>>>(){
			@Override
			public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
				List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
				while(rs.next()){
					Map<String, Object> map = new HashMap<>();
					map.put("taskId", rs.getInt("task_id"));
					map.put("name", rs.getString("name"));
					map.put("blockId", rs.getInt("block_id"));
					map.put("regionId", rs.getInt("region_id"));
					result.add(map);
				}
				return result;
			}
		};
		log.info("获取待数据规划采集任务列表SQL:"+ sql);
		return run.query(con, sql, rs);
		}catch(Exception e){
			DbUtils.rollback(con);
			throw e;
		}finally{
			DbUtils.close(con);
		}
	}

	/**
	 * 获取质检子任务的任务列表
	 * @param programId
	 * @return
	 * @throws Exception 
	 */
	public JSONObject unPlanQualitylist(Integer programId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run=new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT DISTINCT t.task_id,t.name,t.block_id FROM TASK T, SUBTASK S  WHERE T.PROGRAM_ID = "+programId);
			sb.append(" AND T.TASK_ID = S.TASK_ID AND T.TYPE = 0 AND T.DATA_PLAN_STATUS = 1 AND S.STATUS IN (1, 2) AND S.IS_QUALITY = 1");
			sb.append(" AND S.REFER_ID != 0 AND S.QUALITY_PLAN_STATUS = 0 ");
			
			String selectSql= sb.toString();
			log.info("unPlanQualitylist sql :" + selectSql);

			ResultSetHandler<JSONObject> rsHandler = new ResultSetHandler<JSONObject>() {
				public JSONObject handle(ResultSet rs) throws SQLException {
					JSONObject jsonObject = new JSONObject();
					JSONArray jsonArray = new JSONArray();
					while (rs.next()) {
						JSONObject jo = new JSONObject();
						jo.put("taskId", rs.getInt(1));
						jo.put("name", rs.getString(2));
						jo.put("blockId", rs.getInt(3));
						jsonArray.add(jo);
					}
					jsonObject.put("result", jsonArray);
					jsonObject.put("totalCount", jsonArray.size());
					return jsonObject;
				}
			};
			return run.query(conn, selectSql, rsHandler);	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 初始化规划数据列表
	 * 1.范围：采集任务对应的block的不规则范围
	 * 2.提取范围内的所有link/poi存入data_plan表中。默认全是作业数据
	 * 
	 * */
	@SuppressWarnings("unchecked")
	public Map<String, Integer> initPlanData(int taskId) throws Exception{
		Connection con = null;
		Connection dailyConn = null;
		try{
			con = DBConnector.getInstance().getManConnection();	
			Task task = queryByTaskId(con, taskId);
			Region region = RegionService.getInstance().query(con,task.getRegionId());
			dailyConn = DBConnector.getInstance().getConnectionById(region.getDailyDbId());
			
			Map<String, Integer> result = new HashMap<>();
			int count = queryInitedTaskData(dailyConn, taskId);
			if(count > 0){
				log.info("对应的taskId:" + taskId + "已经初始化了" + count + "条数据，无法重新初始化该条数据");
				result.put("poiNum", 0);
				result.put("linkNum", 0);
				return result;
			}
			
			//获取block对应的范围
//			String wkt = getBlockRange(taskId);
			Block block = BlockService.getInstance().queryByBlockId(con,task.getBlockId());
			if(block.getOriginGeo()==null || block.getOriginGeo().isEmpty()){
				throw new Exception("taskId:"+taskId+"对应的BlockId:"+task.getBlockId()+"对应的范围信息为空，无法进行初始化，请检查数据");
			}
			String wkt = GeoTranslator.jts2Wkt(block.getOriginGeo());
			result = insertPoiAndLinkToDataPlan(wkt, dailyConn, taskId);
			
			List<String> pois = queryImportantPid();
			if(pois.size() > 0){
				StringBuffer sb = new StringBuffer();
				for(int i = 0; i< pois.size(); i++){
					sb.append(pois.get(i)+",");
				}
				String poi = sb.deleteCharAt(sb.length()-1).toString(); 
				log.info("重要POI一览表中的POI_ID为：" + poi);
				//这里在更新一下对应在重要一览表中存在的数据类型
				updateIsImportant(poi, taskId, dailyConn);
			}
			log.info("DATA_PLAN收集统计信息");
			DataBaseUtils.gatherStats(dailyConn, "DATA_PLAN");
			return result;
		}catch(Exception e){
			log.error("初始化规划数据列表失败,原因为："+e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(con);
			DbUtils.rollbackAndCloseQuietly(dailyConn);
			throw new Exception("初始化规划数据列表失败"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(con);
			DbUtils.commitAndCloseQuietly(dailyConn);
		}
	}
	
	/**
	 * 先获取对应taskId对应的初始化数据条数
	 * @throws Exception 
	 * 
	 * */
	public int queryInitedTaskData(Connection dailyConn, int taskId) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select count(1) from DATA_PLAN t where t.task_id = " + taskId;
			
			ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {
				public Integer handle(ResultSet rs) throws SQLException {
					int count = 0;
					if (rs.next()) {
						count = rs.getInt("count(1)");
					}
					return count;
				}
			};
			return run.query(dailyConn, sql, rsHandler);	
			
			
		}catch(Exception e){
			log.error("获取对应taskId对应的初始化数据条数异常"+e.getMessage(), e);
			throw e;
		}
	}
	/**
	 * 根据元数据库中重要数据一览表更新dataPlan中的重要性字段
	 * @throws SQLException 
	 * 
	 * */
	public void updateIsImportant(String pois, int taskId, Connection dailyConn) throws SQLException{
		try{
			QueryRunner run = new QueryRunner();
			StringBuffer sb = new StringBuffer();
			sb.append("update DATA_PLAN d"
					+ "   set d.is_important = 1"
					+ " where d.pid IN (select p.pid"
					+ "                   from ix_poi p,"
					+ "                        (select column_value from table(clob_to_table(?))) t"
					+ "                  where p.poi_num = t.column_value)");
			Clob clob=ConnectionUtil.createClob(dailyConn);
			clob.setString(1, pois);
			sb.append("and d.task_id = "+taskId);
			sb.append(" and d.is_important = 0");
			
			String sql = sb.toString();
			log.info("根据重要一览表数据更新dataPlan表sql："+sql);
			run.update(dailyConn, sql,clob);
		}catch(Exception e){
			log.error("根据重要POi数据更新dataPlan异常："+e.getMessage(),e);
			throw e;
		}
	}
	
	/**
	 * 获取元数据库中重要POI的数据
	 * @throws SQLException 
	 * 
	 * */
	public List<String> queryImportantPid() throws SQLException{
		//通过api调用
		MetadataApi api = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		List<String> pids = api.queryImportantPid();
		return pids;
	}
	
//	/**
//	 * 保存poi和link数据到dataPlan表
//	 * @param 
//	 * @param
//	 * @throws Exception 
//	 * 
//	 * */
//	public void insertData(Connection dailyConn, List<Map<String, Integer>> pois, List<Integer> links, int taskId) throws Exception{
//		try{
//			String insertSql = "insert into DATA_PLAN t (t.pid, t.data_type, t.task_id, t.is_plan_selected) values(?,?,?,?)";
//			QueryRunner run = new QueryRunner();
//			//POI
//			int i = 0;
//			Object[][] poiParams = new Object[pois.size()][4] ;
//			for(Map<String, Integer> poi : pois){
//				Object[] map = new Object[4];
//				map[0] = poi.get("pid");
//				map[1] = 1;
//				map[2] = taskId;
//				map[3] = poi.get("important");
//				poiParams[i] = map;
//				i++;
//			}
//			//LINK
//			int j = 0;
//			Object[][] linkParams = new Object[links.size()][4] ;
//			for(Integer link : links){
//				Object[] map = new Object[4];
//				map[0] = link;
//				map[1] = 2;
//				map[2] = taskId;
//				map[3] = 0;
//				linkParams[j] = map;
//				j++;
//			}
//			
//			run.batch(dailyConn, insertSql, poiParams);
//			run.batch(dailyConn, insertSql, linkParams);
//		}catch(Exception e){
//			log.error("保存poi和link数据到dataPlan表异常："+e);
//			throw e;
//		}
//	}
	
//	/**
//	 * 根据taskId获取对应block的范围
//	 * 
//	 * */
//	public String getBlockRange(int taskId) throws Exception{
//		Connection con = null;
//		try{
//			con = DBConnector.getInstance().getManConnection();
//			QueryRunner run = new QueryRunner();
//			
//			//获取block对应的范围
//			String sql = "select b.origin_geo from BLOCK b, TASK t where t.block_id = b.block_id and t.task_id = " + taskId;
//			ResultSetHandler<String> rs = new ResultSetHandler<String>(){
//				public String handle(ResultSet rs) throws SQLException {
//				String wkt = "";
//				if(rs.next()){
//					STRUCT struct = (STRUCT) rs.getObject("origin_geo");
//					try {
//						wkt = GeoTranslator.struct2Wkt(struct);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//				return wkt;
//			}
//		};
//			return run.query(con, sql, rs);
//		}catch(Exception e){
//			log.error("据taskId："+taskId+"获取对应block的范围异常：" + e.getMessage());
//			throw e;
//		}finally{
//			DbUtils.close(con);
//		}
//	}
	
	/**
	 * 获取block范围内poi和link的数据保存到dataPlan表
	 * 
	 * */
	public Map<String, Integer> insertPoiAndLinkToDataPlan(String wkt, Connection dailyConn, int taskId) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			
			StringBuffer linksb = new StringBuffer();
			linksb.append("insert into DATA_PLAN d(d.pid, d.data_type, d.task_id) ");
			linksb.append("select t.link_pid, 2, "+taskId+" from RD_LINK t where  t.u_record != 2 and ");
			linksb.append("sdo_relate(T.GEOMETRY,SDO_GEOMETRY(?,8307),'mask=anyinteract') = 'TRUE'");
			String linkSql = linksb.toString();
			Clob clob = ConnectionUtil.createClob(dailyConn);
			clob.setString(1, wkt);
			
			log.info("linkSql"+linkSql);
			int linkNum = run.update(dailyConn, linkSql, clob);
			
			StringBuffer poisb = new StringBuffer();
			poisb.append("insert into DATA_PLAN d(d.pid, d.data_type, d.task_id, d.is_important) ");
			poisb.append("select p.pid, 1, "+taskId+", case when p."+"\""+"LEVEL"+"\""+" = 'A' then 1 else 0 end  from IX_POI p where  p.u_record != 2 and ");
			poisb.append("sdo_relate(p.GEOMETRY,SDO_GEOMETRY(?,8307),'mask=anyinteract') = 'TRUE'");
			String poiSql = poisb.toString();
			
			log.info("poiSql:"+poiSql);
			int poiNum = run.update(dailyConn, poiSql, clob);
			
			Map<String, Integer> result = new HashMap<>();
			result.put("poiNum", poiNum);
			result.put("linkNum", linkNum);
			
			return result;
		}catch(Exception e){
			log.error("获取block范围内poi和link的数据保存到dataPlan表异常："+e.getMessage(),e);
			throw e;
		}
	}
	
	
	//获取待规划子任务的任务列表
		public JSONObject unPlanSubtasklist(int programId)  throws Exception{
			Connection conn = null;
			try {
				conn = DBConnector.getInstance().getManConnection();
				QueryRunner run = new QueryRunner();
				
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT TASK_ID, NAME, BLOCK_ID FROM TASK WHERE STATUS IN (1, 2) ");
				sb.append("AND TYPE = 0 AND DATA_PLAN_STATUS = 1 AND PROGRAM_ID = ");
				sb.append(programId);

				String selectSql= sb.toString();
				log.info("unPlanSubtasklist sql :" + selectSql);

				ResultSetHandler<JSONObject> rsHandler = new ResultSetHandler<JSONObject>() {
					public JSONObject handle(ResultSet rs) throws SQLException {
						JSONObject jsonObject = new JSONObject();
						JSONArray jsonArray = new JSONArray();
						while(rs.next()){
							JSONObject jo = new JSONObject();
							jo.put("taskId", rs.getInt(1));
							jo.put("name", rs.getString(2));
							jo.put("blockId", rs.getInt(3));
							jsonArray.add(jo);
						}
						jsonObject.put("result", jsonArray);
						jsonObject.put("totalCount", jsonArray.size());
						return jsonObject;
					}
				};
				return run.query(conn, selectSql, rsHandler);	
			}catch(Exception e){
				DbUtils.rollbackAndCloseQuietly(conn);
				log.error("获取待规划子任务列表，原因为："+e.getMessage(),e);
				throw e;
			}finally{
				DbUtils.commitAndCloseQuietly(conn);
			}
		}
		
		/**
		 *  规划上传接口（新增）
		 *  原则：修改任务的数据规划状态task表data_Plan_Status=1
		 *  应用场景：独立工具--外业规划--数据规划--上传
		 * @param taskId
		 * @throws Exception
		 * 
		 * */
		public void uploadPlan(int taskId) throws Exception{
			Connection con = null;
			Connection dailyConn = null;
			try{
				QueryRunner run = new QueryRunner();
				con = DBConnector.getInstance().getManConnection();	
//				Task task = queryByTaskId(con, taskId);
//				Region region = RegionService.getInstance().query(con,task.getRegionId());
//				dailyConn = DBConnector.getInstance().getConnectionById(region.getDailyDbId());
			
				String sql = "update TASK t set t.data_plan_status = 1 where t.task_id = " + taskId;
//				String deletesql = "delete DATA_PLAN t where t.task_id = "+taskId+" and t.is_plan_selected = 0";
				run.execute(con, sql);
				//任务规划结果批最后的规划时间
				Task task = queryByTaskId(con, taskId);
				Region region = RegionService.getInstance().query(con,task.getRegionId());
				dailyConn = DBConnector.getInstance().getConnectionById(region.getDailyDbId());
				String updateSql="update data_plan t set t.operate_date=sysdate where t.task_id="+ taskId;
				run.execute(dailyConn, updateSql);
				
				//modify by songhe
				//补充需求，根据dataPlan中的数据更新task表对应的统计值信息
				int poiCount = calculateNeedWordPoiCount(dailyConn, taskId);
				float linkLenght = calculateNeedWordLinkLength(dailyConn, taskId);
				String updateCount = "update TASK t set t.poi_plan_total = "+poiCount+",t.road_plan_total = "+linkLenght+""
						+ " where t.task_id = " + taskId;
				log.info("根据dataPlan更新需要作业的数据sql:"+updateCount);
				run.execute(con, updateCount);
			}catch(Exception e){
				log.error("规划上传接口异常，原因为："+e.getMessage(),e);
				DbUtils.rollbackAndCloseQuietly(con);
				DbUtils.rollbackAndCloseQuietly(dailyConn);
				throw e;
			}finally{
				DbUtils.commitAndCloseQuietly(con);
				DbUtils.commitAndCloseQuietly(dailyConn);
			}
		}
		
		/**
		 * 计算taskId对应的data_plan中需要作业POI总数
		 * @param Connection dailyConn
		 * @param int taskId
		 * @return int
		 * 
		 * */
		public int calculateNeedWordPoiCount(Connection dailyConn, int taskId){
			try{
				QueryRunner run = new QueryRunner();
				//查询某个task下需要作业的poi总数
				ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>(){
					public Integer  handle(ResultSet rs) throws SQLException {
						int result = 0;
						if(rs.next()){
							result = rs.getInt("count(1)");
						}
						return result;
					}
				};
				String poiSql = "select count(1) from data_plan d where d.data_type = 1 and d.task_id = "+taskId+" "
						+ "and d.is_plan_selected = 1";
				log.info("计算taskId对应的data_plan中需要作业POI总数:"+poiSql);
				return run.query(dailyConn, poiSql, rs);
			}catch(Exception e){
				log.error("计算taskId对应的data_plan中需要作业POI总数异常："+e.getMessage(), e);
			}
			return 0;
		}
		
		/**
		 * 计算taskId对应的data_plan中的需要作业的link长度总和
		 * @param Connection dailyConn
		 * @param int taskId
		 * @return float
		 * 
		 * */
		public float calculateNeedWordLinkLength(Connection dailyConn, int taskId){
			try{
				QueryRunner run = new QueryRunner();
				//查询某个task下需要作业的Link长度
				ResultSetHandler<Float> rsh = new ResultSetHandler<Float>(){
					public Float handle(ResultSet rs) throws SQLException {
						float result = 0f;
						if(rs.next()){
							result = rs.getFloat("result");
						}
						return result;
					}
				};
				String linksql = "select sum(t.length) result from RD_LINK t where t.link_pid in ("
						+ "select d.pid from data_plan d where d.data_type = 2 and d.task_id = "+taskId+" "
						+ "and d.is_plan_selected = 1)";
				log.info("计算taskId对应的data_plan中的需要作业的link长度总和:"+linksql);
				return run.query(dailyConn, linksql, rsh);
			}catch(Exception e){
				log.error("计算taskId对应的data_plan中的需要作业的link长度总和异常："+e.getMessage(), e);
			}
			return 0f;
		}
		
		/**
		 *  规划数据保存
		 *  根据条件规划或者范围规划对数据修改规划状态
		 *  应用场景：独立工具--外业规划--数据规划
		 * @param dataJson
		 * @parame userId
		 * @throws Exception
		 * 
		 * */
		public void savePlan(JSONObject dataJson, long userId) throws Exception{
			Connection conn = null;
			Connection dailyConn = null;
			try{
				int taskId =  dataJson.getInt("taskId");
				conn = DBConnector.getInstance().getManConnection();	
				Task task = queryByTaskId(conn, taskId);
				Region region = RegionService.getInstance().query(conn,task.getRegionId());
				dailyConn = DBConnector.getInstance().getConnectionById(region.getDailyDbId());
				//数据类型
				int dataType = dataJson.getInt("dataType");
				//操作类型
				int isPlanStatus = dataJson.getInt("isPlanStatus");
				JSONObject condition = dataJson.getJSONObject("condition");
				String wkt = null;
				if(condition.containsKey("wkt") && condition.getString("wkt").length() > 1){
					wkt = condition.getString("wkt");
					if(StringUtils.isBlank(wkt)){
						throw new Exception("wkt参数为空");
					}
					updateDataPlanStatusByWkt(dailyConn, isPlanStatus, dataType, wkt, taskId);
				}else{
					log.info("没有上传wkt数据，为条件规划");
					TaskProgress taskPrograss = taskInPrograssCount(conn, taskId);
					
					Map<String, Object> dataPlan = convertDataPlanCondition(dataType, condition);
					
					log.info("把不满足条件的数据状态更新为不需要作业");
					updateDataPlanToNoPlan(dailyConn, dataType, taskId);
					log.info("start 日库中的dataPlan更新数据");
					updateDataPlanStatusByCondition(dailyConn, dataPlan, dataType, taskId);
					log.info("end 日库中的dataPlan更新数据");
					if(dataType == 1 || dataType == 3){
						log.info("更改置信度");
						int minCount = condition.getInt("poiMultiMinCount");
						int maxCount = condition.getInt("poiMultiMaxCount");
						//元数据库中的pid，也需要更新到data_plan表中
						List<Integer> reliabilityPid = queryReliabilityPid(minCount, maxCount);
						//更新从元数据库中获取的pid到dataPlan表中
						updateDataPlanStatusByReliability(dailyConn, reliabilityPid);
					}
					//保存到taskPrograss表
					log.info("保存条件到taskPrograss表");
					maintainTaskPrograss(conn, taskPrograss, dataJson, userId);
				}
			}catch(Exception e){
				log.error("规划数据保存失败，原因为："+e.getMessage(),e);
				DBUtils.rollBack(conn);
				DBUtils.rollBack(dailyConn);
				throw new Exception("规划数据保存失败"+e.getMessage(),e);
			}finally{
				DbUtils.commitAndCloseQuietly(conn);
				DbUtils.commitAndCloseQuietly(dailyConn);
			}
		}
		
		/**
		 * 范围规划，根据wkt范围处理范围内的数据作业状态
		 * @param isPlanStatus  作业状态
		 * @param dataType 1 poi 2road 3一体化
		 * @param wkt  范围
		 * @parame taskId
		 * @throws Exception 
		 * 
		 * */
		public void updateDataPlanStatusByWkt(Connection conn, int isPlanStatus, int dataType, String wkt, int taskId) throws Exception{
			try{
				String type = null;
				if(isPlanStatus != 0 && isPlanStatus != 1){
					throw new Exception("dataPlanStatus数据错误！");
				}
				if(dataType != 1 && dataType != 2 && dataType != 3){
					throw new Exception("dataType数据错误！");
				}
				if(dataType == 3){
					type = "1,2";
				}else{
					type = String.valueOf(dataType);
				}
				//根据范围更新数据状态
				QueryRunner run = new QueryRunner();
				StringBuffer sb = new StringBuffer();
				StringBuffer poisb = new StringBuffer();
				StringBuffer linksb = new StringBuffer();
				String poisql = "";
				String linksql = "";
				sb.append("update DATA_PLAN t set t.is_plan_selected = "+isPlanStatus+" where ");
				
				if("1".equals(type) || "1,2".equals(type)){
					poisb.append("t.task_id = "+taskId+" and t.data_type = 1 and t.pid in (");
					poisb.append("select p.pid from IX_POI p where sdo_relate(p.GEOMETRY,SDO_GEOMETRY(?,8307),'mask=anyinteract+contains+inside+touch+covers+overlapbdyintersect') = 'TRUE')");
					poisql = sb.toString()+poisb.toString();
					log.info("根据范围规划数据更新POI："+poisql);
					run.update(conn, poisql, wkt);
				}
				if("2".equals(type) || "1,2".equals(type)){
					linksb.append("t.task_id = "+taskId+" and t.data_type = 2 and t.pid in (");
					linksb.append("select r.link_pid from RD_LINK r where sdo_relate(r.GEOMETRY,SDO_GEOMETRY(?,8307),'mask=anyinteract+contains+inside+touch+covers+overlapbdyintersect') = 'TRUE')");
					linksql = sb.toString()+linksb.toString();
					log.info("根据范围规划数据更新link："+linksql);
					run.update(conn, linksql, wkt);
				}
			}catch(Exception e){
				log.info("根据范围修改数据作业状态异常:"+e.getMessage(),e);
				throw e;
			}
		}
		
		/**
		 * 条件规划：处理数据
		 * 不满足选中条件的数据，为不需要作业；满足条件的数据，为需要作业
		 * @param JSONObject condition
		 * @param int dataType
		 * @return Map<String, Object>
		 * @throws Exception
		 * 
		 * */
		public Map<String, Object> convertDataPlanCondition(int dataType, JSONObject condition) throws Exception{
			try{
				Map<String, Object> result = new HashMap<String, Object>();
				
				String kindCode = "";
				String levels = "";
				String roadFCs = "";
				String roadKinds = "";
				List<String> kindCodes = new ArrayList<>();
				//dataType等于1或者3的时候进行POI数据处理
				if(dataType == 1 || dataType == 3){
					
					JSONArray poiKind = condition.getJSONArray("poiKind");
					//这里处理上传的选中状态的POI的kindCode前四位作为检索条件
					for(int i = 0; i < poiKind.size(); i++){
						JSONObject kindJson = poiKind.getJSONObject(i);
						int flag = kindJson.getInt("flag");
						if(flag == 0){
							continue;
						}
						String classCode = kindJson.getString("classCode");
						JSONArray subClassCodes = kindJson.getJSONArray("subClassCodes");
						for(int j = 0; j < subClassCodes.size(); j++){
							JSONObject subKindJson = subClassCodes.getJSONObject(j);
							int subFlag = subKindJson.getInt("flag");
							if(subFlag == 0){
								continue;
							}
							String subClassCode = subKindJson.getString("classCode");
							kindCode = classCode + subClassCode + "%";
							kindCodes.add(kindCode);
						}
					}
					
					JSONArray poiLevel = condition.getJSONArray("poiLevel");
					StringBuffer level = new StringBuffer();
					for(int i = 0 ; i < poiLevel.size(); i++){
						level.append("'" + poiLevel.get(i) + "',");
					}
					if(level.length() > 0){
						levels = level.deleteCharAt(level.length() - 1).toString();
					}
					if(poiLevel.size() == 0 || StringUtils.isBlank(levels)){
						levels = "''";
					}
					
				}
				//dateType = 2或者 3 时进行道路数据处理
				if(dataType == 2 || dataType == 3){
					JSONArray roadKind = condition.getJSONArray("roadKind");
					StringBuffer kinds = new StringBuffer();
					for(int i = 0 ; i < roadKind.size(); i++){
						kinds.append(roadKind.get(i) + ",");
					}
					if(kinds.length() > 1){
						roadKinds = kinds.deleteCharAt(kinds.length() - 1).toString();
					}
					if(roadKind.size() == 0 || StringUtils.isBlank(roadKinds)){
						roadKinds = "''";
					}
					
					JSONArray roadFC = condition.getJSONArray("roadFC");
					StringBuffer FCs = new StringBuffer();
					for(int i = 0 ; i < roadFC.size(); i++){
						FCs.append(roadFC.get(i) + ",");
					}
					if(FCs.length() > 0){
						roadFCs = FCs.deleteCharAt(FCs.length() - 1).toString();
					}
					if(roadFC.size() == 0 || StringUtils.isBlank(roadFCs)){
						roadFCs = "''";
					}
				}

				result.put("roadKinds",roadKinds);
				result.put("roadFCs",roadFCs);
				result.put("levels",levels);
				result.put("kindCodes",kindCodes);
				return result;
			}catch(Exception e){
				log.info("处理条件数据异常:"+e.getMessage(),e);
				throw e;
			}
		}
		
		/**
		 * 根据是否taskprograss表中有数据保存数据
		 * @param TaskProgress
		 * @param Connection
		 * @param JSONObject
		 * @param long userId
		 * @throws Exception
		 * 
		 * */
		public void maintainTaskPrograss(Connection conn, TaskProgress taskPrograss, JSONObject dataJson, long userId) throws Exception{
			try{
				TaskProgress bean = new TaskProgress();
				int taskId = dataJson.getInt("taskId");
				String parameter = dataJson.getJSONObject("condition").toString();
				int dataType = dataJson.getInt("dataType");
				
				int phaseId = 0;
				bean.setTaskId(taskId);
				bean.setOperator(userId);
				bean.setPhase(2);
				bean.setStatus(0);
				if(taskPrograss == null){
					bean.setParameter(parameter);
					phaseId = TaskProgressOperation.getNewPhaseId(conn);
					bean.setPhaseId(phaseId);
					Timestamp time = new Timestamp(System.currentTimeMillis()); 
					bean.setCreatDate(time);
					TaskProgressOperation.create(conn, bean);
				}else{
					//这里针对poi和道路需要只处理对应的数据，未选中的数据保持原有内容不变
					String taskParameter = convertParameter(taskPrograss.getParameter(), parameter, dataType);
					bean.setParameter(taskParameter);
					phaseId = taskPrograss.getPhaseId();
					bean.setPhaseId(phaseId);
					TaskProgressOperation.updateTaskProgress(conn, bean);
				}
			}catch(Exception e){
				log.error("保存数据到taskPrograss表出错："+e.getMessage(),e);
				throw e;
			}
		}
		
		/**
		 * 处理要保存到task_prograss表内的parameter数据
		 * 根据操作类型增量更新parameter中的内容
		 * @param String原来表中存储的parameter
		 * @param 上传的parameter
		 * @param int 要修改的数据类型
		 * @return String 处理后的parameter
		 * 
		 * */
		public String convertParameter(String parameter, String json, int dateType){
			//原数据库存的parameter
			JSONObject jsonParameter = JSONObject.fromObject(parameter);
			//新上传的parameter的conditon内容
			JSONObject condition = JSONObject.fromObject(json);
			
			if(dateType == 1 || dateType == 3){
				String poiLevel = condition.getJSONArray("poiLevel").toString();
				String poiKind = condition.getJSONArray("poiKind").toString();
				String poiMultiMinCount = condition.getString("poiMultiMinCount");
				String poiMultiMaxCount = condition.getString("poiMultiMaxCount");
				jsonParameter.put("poiLevel", poiLevel);
				jsonParameter.put("poiKind", poiKind);
				jsonParameter.put("poiMultiMinCount", poiMultiMinCount);
				jsonParameter.put("poiMultiMaxCount", poiMultiMaxCount);
			}
			if(dateType == 2 || dateType == 3){
				String roadKind = condition.getJSONArray("roadKind").toString();
				String roadFC = condition.getJSONArray("roadFC").toString();
				jsonParameter.put("roadKind", roadKind);
				jsonParameter.put("roadFC", roadFC);
			}
			return jsonParameter.toString();
		}
		/**
		 * 条件规划：更新不满足条件规划的数据状态为不需要作业
		 * @param Connection
		 * @param int dataType
		 * @param int taskId
		 * @throws Exception 
		 * 
		 * */
		public void updateDataPlanToNoPlan(Connection dailyConn, int dataType, int taskId) throws Exception{
			try{
				QueryRunner run = new QueryRunner();
				String type = String.valueOf(dataType);
				if(dataType == 3){
					type = "1,2";
				}
				String sql = "update DATA_PLAN d set d.is_plan_selected = 0 where d.data_type in ("+type+") and d.task_id = "+taskId;
				run.execute(dailyConn, sql);
			}catch(Exception e){
				throw e;
			}
		}
		
		/**
		 * 条件规划：跟据条件保存数据到dataPlan表中
		 * 不满足选中条件的数据，为不需要作业；满足条件的数据，为需要作业
		 * @param Connection
		 * @param Map<String, Object>
		 * @param int dataType
		 * @parame int taskId
		 * 
		 * */
		public void updateDataPlanStatusByCondition(Connection conn, Map<String, Object> dataPlan, int dataType, int taskId) throws Exception{
			try{
				QueryRunner run = new QueryRunner();
				
				List<String> kindCodes = (List<String>) dataPlan.get("kindCodes");
				String roadKinds = dataPlan.get("roadKinds").toString();
				String roadFCs = dataPlan.get("roadFCs").toString();
				String levels = dataPlan.get("levels").toString();
				
//				String data_type = "1";
//				if(dataType == 3){
//					data_type = "1,2";
//				}else{
//					data_type = String.valueOf(dataType);
//				}
				
				//更新POI,这里把对象的创建放在判断里吧，不符合条件的就不创建对应sql了
				if(dataType == 1 || dataType == 3){
					StringBuffer poiSb = new StringBuffer();
					poiSb.append("update DATA_PLAN p set p.is_plan_selected = 1 where exists (");
					poiSb.append("select 1 from IX_POI t, DATA_PLAN dp where dp.pid = t.pid and dp.pid = p.pid and t.u_record!=2 and ");
					poiSb.append("(t."+"\""+"LEVEL"+"\""+" in ("+levels+") ");
					for(String kindCode : kindCodes){
						poiSb.append(" or t.kind_code like '" + kindCode + "' ");
					}
					poiSb.append(")and dp.data_type = 1 and dp.is_plan_selected = 0 and dp.task_id = " + taskId + ")and p.data_type = 1 and p.is_plan_selected = 0 and p.task_id = " + taskId);
					String poisql = poiSb.toString();
					log.info("跟据条件保存POI数据sql:"+poisql);
					run.execute(conn, poisql);
				}
				
				//更新road
				if(dataType == 2 || dataType == 3){
					StringBuffer linkSb = new StringBuffer();
					linkSb.append("update DATA_PLAN d set d.is_plan_selected = 1 where exists (");
					linkSb.append("select 1 from RD_LINK r, DATA_PLAN dp where dp.pid = r.link_pid and r.u_record!=2 and d.pid = dp.pid and ");
					linkSb.append("(r.function_class in ("+roadFCs+") ");
					if(StringUtils.isNotBlank(roadKinds)){
						linkSb.append("or ");
						linkSb.append("r.kind in ("+roadKinds+") ");
					}
					linkSb.append(")and dp.data_type = 2 and dp.is_plan_selected = 0 and dp.task_id = "+taskId+")and d.data_type = 2 and d.is_plan_selected = 0 and d.task_id = "+taskId);
					String linksql = linkSb.toString();
					log.info("跟据条件保存LINK数据sql:"+linksql);
					run.execute(conn, linksql);
				}
				
			}catch(Exception e){
				log.error("根据条件修改数据作业状态异常:"+e.getMessage(),e);
				throw e;
			}
		}
		
		/**
		 * 条件规划：从元数据库中查询出的可信度范围的pid保存数据到dataPlan表中
		 * @param Connection
		 * @param List<Integer>
		 * @throws Exception 
		 * 
		 * */
		public void updateDataPlanStatusByReliability(Connection conn, List<Integer> reliabilityPid) throws Exception{
			try{
				if(reliabilityPid==null||reliabilityPid.size()==0){return;}
				QueryRunner run = new QueryRunner();
				StringBuffer sb = new StringBuffer();
				for(int i = 0; i < reliabilityPid.size(); i++){
					sb.append(reliabilityPid.get(i)+",");
				}				
				String pids = sb.deleteCharAt(sb.length() - 1).toString();
				
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, pids);
				
				String sql = "update DATA_PLAN d set d.is_plan_selected = 1 "
						+ "where d.pid in (select to_number(column_value) from table(clob_to_table(?))) "
						+ "and d.data_type = 1";
				log.info("从元数据库中查询出的可信度范围的pid保存数据到dataPlan表中sql:"+sql);
				run.update(conn, sql,clob);
			}catch(Exception e){
				throw e;
			}
		}
		
		
		/**
		 * 条件规划：判断task_progress表中是否存在task记录
		 * @param taskId
		 * @param Connection
		 * @return TaskProgress
		 * @throws Exception
		 * 
		 * */
		public TaskProgress taskInPrograssCount(Connection conn, int taskId) throws Exception{
			try{
				QueryRunner run = new QueryRunner();
				String sql = "select t.* from TASK_PROGRESS t where t.phase = 2 and t.task_id = "+taskId;
				ResultSetHandler<TaskProgress> rs = new ResultSetHandler<TaskProgress>(){
					public TaskProgress handle(ResultSet rs) throws SQLException {
						if(rs.next()){
							TaskProgress taskPrograss = new TaskProgress();
							taskPrograss.setTaskId(rs.getInt("TASK_ID"));
							taskPrograss.setPhase(rs.getInt("PHASE"));
							taskPrograss.setStatus(rs.getInt("STATUS"));
							taskPrograss.setCreatDate(rs.getTimestamp("CREATE_DATE"));
							taskPrograss.setStartDate(rs.getTimestamp("START_DATE"));
							taskPrograss.setEndDate(rs.getTimestamp("END_DATE"));
							taskPrograss.setMessage(rs.getString("MESSAGE"));
							try {
								taskPrograss.setParameter(TaskProgressOperation.ClobToString(rs.getClob("PARAMETER")));
							} catch (IOException e) {
								e.printStackTrace();
							}
							taskPrograss.setOperator(rs.getLong("OPERATOR"));
							taskPrograss.setPhaseId(rs.getInt("PHASE_ID"));
							return taskPrograss;
						}
						return null;
					}
				};
				return run.query(conn, sql, rs);
			}catch(Exception e){
				log.error("判断task_progress表中是否存在task记录异常:"+e.getMessage(),e);
				throw e;
			}
		}
		
		/**
		 * 获取元数据库中重要POI的数据
		 * @param int minNumber
		 * @param int maxNumber
		 * @throws SQLException 
		 * 
		 * */
		public List<Integer> queryReliabilityPid(int minNumber, int maxNumber) throws SQLException{
			Connection conn = null;
			try{
				//通过api调用
				MetadataApi api = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				List<Integer> pids = api.queryReliabilityPid(minNumber, maxNumber);
				return pids;
//				return ScQueryReliabilityPid(minNumber,maxNumber);
			}catch(Exception e){
				DbUtils.close(conn);
				log.error("从元数据库中获取重要POI异常："+e.getMessage(),e);
				throw e;
			}finally{
				DbUtils.closeQuietly(conn);
			}
		}
		
//		//这个方法备用，去元数据库查询pid
//		public List<Integer> ScQueryReliabilityPid(int minNumber, int maxNumber) throws SQLException {
//
//			Connection conn = null;
//			try{
//				conn = DBConnector.getInstance().getMetaConnection();
//				QueryRunner run = new QueryRunner();
//				
//				String selectSql = "select t.pid from reliability_table t where t.reliability between "+minNumber+" and "+maxNumber;
//				ResultSetHandler<List<Integer>> rs = new ResultSetHandler<List<Integer>>(){
//					public List<Integer> handle(ResultSet rs) throws SQLException {
//					List<Integer> pids = new ArrayList<>();
//					while(rs.next()){
//						pids.add(rs.getInt("PID"));
//					}
//					return pids;
//				}
//			};
//			return run.query(conn, selectSql, rs);
//			}catch(Exception e){
//				DbUtils.close(conn);
//				log.error("从元数据库依据置信度范围检索PID异常："+e.getMessage());
//				throw e;
//			}finally{
//				DbUtils.closeQuietly(conn);
//			}
//		
//		}
		
		/**
		 *  获取条件规划
		 *  获取该任务保存的条件规划情况task_progess中phase=2的记录的parameter
		 *  应用场景：独立工具--外业规划--数据规划
		 * @param int taskId
		 * @return String parameter
		 * @throws Exception
		 * 
		 * */
		public JSONObject getPlan(int taskId) throws Exception{
			Connection conn = null;
			try{
				conn = DBConnector.getInstance().getManConnection();
				TaskProgress tp = taskInPrograssCount(conn, taskId);
				if(tp==null){
					return null;
				}
				String parameter = tp.getParameter();
				if(StringUtils.isEmpty(parameter)){
					return null;
				}
				JSONObject json = JSONObject.fromObject(parameter);
				return json;
			}catch(Exception e){
				log.error("获取条件规划异常，原因为："+e.getMessage(),e);
				DbUtils.closeQuietly(conn);
				throw new Exception("获取条件规划异常"+e.getMessage(),e);
			}finally{
				DbUtils.closeQuietly(conn);
			}
		}
	
	/**
	 * 查询subtask详细信息
	 * @author Han Shaoming
	 * @return	List<Map<String,Object>> map key:fieldName,value:相应的值
	 * @throws Exception
	 */
	public List<Map<String,Object>> querySubtaskByTaskId(int taskId) throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "SELECT S.* FROM TASK T,SUBTASK S WHERE T.TASK_ID = S.TASK_ID AND T.TASK_ID ="+taskId;
			ResultSetHandler<List<Map<String,Object>>> rs = new ResultSetHandler<List<Map<String,Object>>>() {
				
				@Override
				public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
					while(rs.next()){
						Map<String,Object> map = new HashMap<String,Object>();
						int workKind = rs.getInt("WORK_KIND");
						int subtaskId = rs.getInt("SUBTASK_ID");
						int type = rs.getInt("TYPE");
						int status = rs.getInt("STATUS");
						map.put("subtaskId", subtaskId);
						map.put("workKind", workKind);
						map.put("type", type);
						map.put("status", status);
						result.add(map);
					}
					return result;
				}
			};
			QueryRunner run = new QueryRunner();
			List<Map<String, Object>> result = run.query(conn,selectSql, rs);
			return result;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task的subtaskIds失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 查询task对应的项目类型
	 * @author Han Shaoming
	 * @return	Map<Integer,Integer> key:taskId,value:programType 项目类型。1常规(中线)4快速更新(快线)9 虚拟项目
	 * @throws Exception
	 */
	public Map<Integer,Integer> queryProgramTypes() throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "SELECT T.TASK_ID,P.TYPE FROM TASK T,PROGRAM P WHERE T.PROGRAM_ID = P.PROGRAM_ID ";
			ResultSetHandler<Map<Integer,Integer>> rs = new ResultSetHandler<Map<Integer,Integer>>() {
				
				@Override
				public Map<Integer,Integer> handle(ResultSet rs) throws SQLException {
					Map<Integer,Integer> result = new HashMap<Integer,Integer>();
					while(rs.next()){
						int taskId = rs.getInt("TASK_ID");
						int programType = rs.getInt("TYPE");
						result.put(taskId, programType);
					}
					return result;
				}
			};
			QueryRunner run = new QueryRunner();
			Map<Integer, Integer> result = run.query(conn,selectSql, rs);
			return result;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task对应的项目类型失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public List<Map<String, Object>> forOcms(String date)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "SELECT T.TASK_ID,"
					+ "       T.NAME,"
					+ "       U.GROUP_NAME,"
					+ "       I.USER_REAL_NAME,"
					+ "       T.PLAN_START_DATE,"
					+ "       T.PLAN_END_DATE,"
					+ "       T.CREATE_DATE,"
					+ "       P.TYPE"
					+ "  FROM TASK T, USER_GROUP U, USER_INFO I, PROGRAM P"
					+ " WHERE T.GROUP_ID = U.GROUP_ID"
					+ "   AND U.LEADER_ID = I.USER_ID"
					+ "   AND T.PROGRAM_ID = P.PROGRAM_ID";
			if(!StringUtils.isEmpty(date)){
				selectSql=selectSql+ "   AND T.CREATE_DATE > TO_DATE('"+date+"', 'yyyy-mm-dd')";
			}
			ResultSetHandler<List<Map<String, Object>>> rs = new ResultSetHandler<List<Map<String, Object>>>() {
				
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
					while(rs.next()){
						Map<String, Object> task=new HashMap<>();
						task.put("taskId", rs.getInt("TASK_ID"));
						task.put("name", rs.getString("NAME"));
						task.put("groupName", rs.getString("GROUP_NAME"));
						task.put("userName", rs.getString("USER_REAL_NAME"));
						task.put("planStartDate", DateUtils.format(rs.getTimestamp("PLAN_START_DATE"), DateUtils.DATE_WITH_SPLIT_YMD));
						task.put("planEndDate", DateUtils.format(rs.getTimestamp("PLAN_END_DATE"), DateUtils.DATE_WITH_SPLIT_YMD));
						task.put("createDate", DateUtils.format(rs.getTimestamp("CREATE_DATE"), DateUtils.DATE_DEFAULT_FORMAT));
						task.put("type", rs.getInt("type"));
						result.add(task);
					}
					return result;
				}
			};
			QueryRunner run = new QueryRunner();
			List<Map<String, Object>> result = run.query(conn,selectSql, rs);
			return result;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询task对应的项目类型失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
}
