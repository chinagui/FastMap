package com.navinfo.dataservice.engine.man.mqmsg;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.model.Infor;
import com.navinfo.dataservice.api.man.model.Program;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.dao.mq.email.EmailPublisher;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.dataservice.engine.man.infor.InforService;
import com.navinfo.dataservice.engine.man.log.ManLogOperation;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.engine.man.userGroup.UserGroupService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.GridUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 同步消费消息
 * 
 * @ClassName: InfoChangeMsgHandler
 * @author Xiao Xiaowen
 * @date 2016年6月25日 上午10:42:43
 * @Description: TODO
 * 
 */
public class InfoChangeMsgHandler implements MsgHandler {
	protected Logger log = LoggerRepos.getLogger(this.getClass());

	@Override
	public void handle(String message) {
		try {
			// 解析保存到man库infor表中
			save(message);
		} catch (Exception e) {
			log.warn("接收到info_change消息,但保存失败，该消息已消费。message：" + message);
			log.error(e.getMessage(), e);

		}
	}

	public void save(String message) throws Exception {
		log.info("get infor:"+message);
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			//新增情报
			JSONObject dataJson = JSONObject.fromObject(message);
			dataJson.remove("data");
			dataJson.remove("bSourceId");
			Infor infor = InforService.getInstance().create(dataJson, 0);
			int sourceCode=infor.getSourceCode();
			//"情报对应方式”字段不为空时，自动创建项目、任务、子任务
			if(sourceCode==2||(sourceCode!=2&&infor.getMethod()!=null)){
				generateManAccount(conn,infor);
			}
			log.info("一级poi类型的情报，需要创建任务并调用异步job");
			if("多源制作".equals(infor.getMethod())){
				importPoiData(conn,infor.getInforId(),message);
			}
			//发送消息
			taskPushMsg(conn, dataJson.getString("inforName"), 0,infor.getInforId());	
			
			conn.commit();
			
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			DbUtils.rollbackAndCloseQuietly(conn);
			StringBuilder logs = new StringBuilder();
			logs.append(e.getMessage());
			logs.append(";infor=");
			logs.append(message);
			ManLogOperation.insertLog("inforCreate", logs.toString());
			throw e;
		} finally {
			log.info("end infor:"+message);
			DbUtils.closeQuietly(conn);				
		}
	}
	/**
	 * 一级poi数据型情报,仅包含一条poi，查询对应的任务，子任务，dbid，并创建job
	 * @param conn
	 * @param inforId
	 * @param message
	 * @throws Exception 
	 */
	private void importPoiData(Connection conn, int inforId, String message) throws Exception {
		log.info("query task,subtask,dbId");
		String sql="SELECT T.TASK_ID, S.SUBTASK_ID, R.DAILY_DB_ID"
				+ "  FROM PROGRAM P, TASK T, SUBTASK S, REGION R"
				+ " WHERE P.PROGRAM_ID = T.PROGRAM_ID"
				+ "   AND T.TASK_ID = S.TASK_ID"
				+ "   AND T.REGION_ID = R.REGION_ID"
				+ "   AND P.INFOR_ID = "+inforId;
		QueryRunner runner=new QueryRunner();
		JSONObject returnJson=runner.query(conn,sql,new ResultSetHandler<JSONObject>(){

			@Override
			public JSONObject handle(ResultSet rs) throws SQLException {
				JSONObject returnJson=new JSONObject();
				if(rs.next()){					
					returnJson.put("dbId",rs.getInt("DAILY_DB_ID"));
					returnJson.put("taskId",rs.getInt("TASK_ID"));
					returnJson.put("subtaskId",rs.getInt("SUBTASK_ID"));
				}
				return returnJson;
			}});
		JSONObject dataJson = JSONObject.fromObject(message);
		returnJson.put("data", dataJson.get("data"));
		returnJson.put("bSourceId", dataJson.get("bSourceId"));
		JobApi api=(JobApi) ApplicationContextUtil.getBean("jobApi");
		log.info("create job infoPoiMultiSrc2FmDay");
		api.createJob("infoPoiMultiSrc2FmDay", returnJson, 0, 
				Long.valueOf(String.valueOf(returnJson.getInt("taskId"))), "一级poi情报入日库");
	}

	/**
	 * @param conn
	 * @param infor
	 * @throws Exception 
	 */
	private void generateManAccount(Connection conn, Infor infor) throws Exception {
		//新建项目
		Program program = new Program();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		int programId = ProgramService.getInstance().getNewProgramId(conn);
		program.setProgramId(programId);
		program.setName(infor.getInforName()+"_" + df.format(infor.getPublishDate())+"_" +  programId);
		program.setType(4);

		program.setCollectPlanStartDate((Timestamp)getCalculatedDate(infor.getPublishDate(),1));
		if(infor.getSourceCode()==2){
			program.setCollectPlanEndDate((Timestamp)getCalculatedDate(infor.getPublishDate(),3));
		}else if(infor.getMethod().equals("预采集")){
			program.setCollectPlanEndDate((Timestamp)getCalculatedDate(infor.getPublishDate(),7));
		}else{
			program.setCollectPlanEndDate((Timestamp)getCalculatedDate(infor.getPublishDate(),3));
		}
		
		program.setDayEditPlanStartDate((Timestamp)getCalculatedDate(program.getCollectPlanEndDate(),1));
		program.setDayEditPlanEndDate((Timestamp)getCalculatedDate(program.getCollectPlanEndDate(),2));
		program.setProducePlanStartDate(program.getDayEditPlanEndDate());
		program.setProducePlanEndDate((Timestamp)getCalculatedDate(program.getDayEditPlanEndDate(),1));
		program.setPlanStartDate(program.getCollectPlanStartDate());
		program.setPlanEndDate(program.getProducePlanEndDate());
		program.setInforId(infor.getInforId());
		ProgramService.getInstance().createWithProgramId(conn, program);

		//发布项目,包含了任务的创建
		List<Program> programs = new ArrayList<Program>();
		programs.add(program);
		JSONArray programIds = new JSONArray();
		programIds.add(program.getProgramId());
		ProgramService.getInstance().pushMsgWithConnection(conn,0, programs,programIds);		
		
		//任务发布
		List<Task> taskList = TaskService.getInstance().getTaskByProgramId(conn,programId);	
		List<Task> collectTaskList = new ArrayList<Task>();	
		List<Task> taskListToPublish = new ArrayList<Task>();
		List<Integer> commontaskIds=new ArrayList<Integer>();
		for(Task task:taskList){//配置表正确的情况下，所有非矢量制作任务均应该能找到对应的组id，此处不做二次判断
			if(task.getType()!=2){
				taskListToPublish.add(task);
				commontaskIds.add(task.getTaskId());
			}
			
			if(task.getType()==0){
				collectTaskList.add(task);
			}
			//}
		}
		//发布任务
		if(taskListToPublish.size()>0){
			//更新task状态
			TaskOperation.updateStatus(conn, commontaskIds,1);
			//发布消息
			TaskService.getInstance().taskPushMsg(conn, 0, taskListToPublish);
		}
		//采集任务创建子任务
		JSONArray subtaskIds=new JSONArray();
		for(Task task:collectTaskList){
			Subtask subtask = new Subtask();
			//subtask.setName(infor.getInforName()+"_"+df.format(infor.getPublishDate()));
			subtask.setType(2);
			subtask.setStage(0);
			if(task.getSubWorkKind(3)==1){
				subtask.setWorkKind(3);				
			}if(task.getSubWorkKind(4)==1){
				subtask.setWorkKind(4);	
				//* 快线：情报名称_发布时间_作业员_子任务ID
				// * 中线：任务名称_作业组
				UserGroup userGroup = UserGroupService.getInstance().getGroupByAminCode(conn,infor.getAdminCode(), 5);
				if(userGroup!=null){
					subtask.setExeGroupId(userGroup.getGroupId());
				}
			}else if(task.getSubWorkKind(1)==1){
				subtask.setWorkKind(1);	
				subtask.setDescp("外业自采集情报");
				List<UserGroup> groups = UserGroupService.getInstance().listByUser(conn,Integer.valueOf(String.valueOf(infor.getReportUserId())));
				if(groups!=null){
					for(UserGroup g:groups){
						if(task.getGroupId()!=0 &&g.getGroupId()==task.getGroupId()){
							subtask.setExeUserId(Integer.valueOf(String.valueOf(infor.getReportUserId())));
						}
					}
				}
			}else{
				subtask.setWorkKind(1);
			}
			subtask.setTaskId(task.getTaskId());
			subtask.setPlanStartDate(task.getPlanStartDate());
			subtask.setPlanEndDate(task.getPlanEndDate());
			subtask.setGridIds(task.getGridIds());
			List<Integer> gridIdList = subtask.getGridIds();
			if(!gridIdList.isEmpty()){
				String wkt = GridUtils.grids2Wkt(JSONArray.fromObject(gridIdList));
				subtask.setGeometry(wkt);
			}
			int subtaskId=SubtaskService.getInstance().createSubtaskWithSubtaskId(conn,subtask);
			if(subtask.getExeUserId()!=0){
				subtaskIds.add(subtaskId);
			}
			if(subtask.getExeGroupId()!=0){
				subtaskIds.add(subtaskId);
			}
		}
		if(subtaskIds.size()>0){SubtaskService.getInstance().pushMsg(conn, 0, subtaskIds);}
	}

	/**
	 * @param publishDate
	 * @param i
	 * @return
	 */
	private Timestamp getCalculatedDate(Date publishDate, int i) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(publishDate);
		cal.add(Calendar.DATE, i);
		Date d = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//        yyyy-MM-dd HH24:MI:ss
        String s = sdf.format(d);
        Timestamp t = Timestamp.valueOf(s);
		return t;
	}

	/*新增一级情报
	 *1.所有生管角色
	 *2.分配的采集作业组组长(暂无)
	 * 有新的一级情报，情报名称：XXX，请关注*/
	public void taskPushMsg(Connection conn,String infoName, long pushUser, Integer inforId) {
		try {
			String msgTitle="新增一级情报";
			List<Map<String,Object>> msgContentList=new ArrayList<Map<String,Object>>();
			//List<Long> groupIdList = new ArrayList<Long>();
			Map<String,Object> map = new HashMap<String, Object>();
			String msgContent = "有新的一级情报，情报名称:"+infoName+",请关注";
			map.put("msgContent", msgContent);
			//关联要素
			JSONObject msgParam = new JSONObject();
			msgParam.put("relateObject", "INFOR");
			msgParam.put("relateObjectId", inforId);
			map.put("msgParam", msgParam.toString());
			msgContentList.add(map);
			
			if(msgContentList.size()>0){
				String userSql="SELECT DISTINCT M.USER_ID FROM ROLE_USER_MAPPING M WHERE M.ROLE_ID =3";
				List<Integer> userIdList = UserInfoOperation.getUserListBySql(conn, userSql);
				//查询分配的作业组组长
				//List<Long> leaderIdByGroupId = UserInfoOperation.getLeaderIdByGroupId(conn, groupIdList);
				//for (Long leaderId : leaderIdByGroupId) {
				//userIdList.add(leaderId.intValue());
				//}
				for(int userId:userIdList){
					for(Map<String, Object> msg:msgContentList){
						//查询用户名称
						UserInfo userInfo = UserInfoOperation.getUserInfoByUserId(conn, userId);
						String pushUserName = null;
						if(userInfo != null && userInfo.getUserRealName()!=null){
							pushUserName = (String) userInfo.getUserRealName();
						}
						//发送消息到消息队列
						String manMsgContent = (String) msg.get("msgContent");
						String manMsgParam = (String) msg.get("msgParam");
						SysMsgPublisher.publishMsg(msgTitle, manMsgContent, pushUser, new long[]{userId}, 2, manMsgParam, pushUserName);
					}
				}
				//发送邮件
				String toMail = null;
				String mailTitle = null;
				String mailContent = null;
				//查询用户详情
				for (int userId : userIdList) {
					UserInfo userInfo = UserInfoOperation.getUserInfoByUserId(conn, userId);
					if(userInfo != null && userInfo.getUserEmail() != null){
						for (Map<String, Object> msg : msgContentList) {
							//判断邮箱格式
							String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			                Pattern regex = Pattern.compile(check);
			                Matcher matcher = regex.matcher((CharSequence) userInfo.getUserEmail());
			                if(matcher.matches()){
			                	toMail = (String) userInfo.getUserEmail();
			                	mailTitle = msgTitle;
			                	mailContent = (String) msg.get("msgContent");
			                	//发送邮件到消息队列
			                	//SendEmail.sendEmail(toMail, mailTitle, mailContent);
			                	EmailPublisher.publishMsg(toMail, mailTitle, mailContent);
			                }
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("新增一级情报消息发送失败,原因:"+e.getMessage(), e);
		}
	}
	
//	public void save(String message) throws Exception {
//		Connection conn = null;
//		try {
//			conn = DBConnector.getInstance().getManConnection();
//			Clob c = ConnectionUtil.createClob(conn);
//			JSONObject dataJson = JSONObject.fromObject(message);
//			String inforGeo = dataJson.getString("geometry");
//			String inforId = dataJson.getString("rowkey");
//			c.setString(1, inforGeo);
//			List<Object> values = new ArrayList<Object>();
//			values.add(inforId);
//			values.add(dataJson.getString("INFO_NAME"));
//			values.add(c);
//			values.add(dataJson.getString("i_level"));
//			values.add(dataJson.getString("INFO_CONTENT"));
//			QueryRunner run = new QueryRunner();
//			run.update(conn, sql, values.toArray());
//			
//			//初始化infor_grid_mapping关系表
//			String insertSql = "INSERT INTO infor_grid_mapping(infor_id,grid_id) VALUES(?,?)";
//			String[] inforGeoList = inforGeo.split(";");
//			Set<String> gridsAfter = new HashSet<String>(); 
//			for (String geoTmp : inforGeoList) {
//				Geometry inforTmp = GeoTranslator.wkt2Geometry(geoTmp);
//				Set<?> grids = (Set<?>) CompGeometryUtil.geo2GridsWithoutBreak(inforTmp);
//				//grid扩圈
//				for(Iterator<String> gridsItr = (Iterator<String>)grids.iterator();gridsItr.hasNext();)  
//		        {              
//					String gridId = gridsItr.next();
//					String[] gridAfter = GridUtils.get9NeighborGrids(gridId);
//					List<String> gridIdlist = gridsFilter(conn,gridId,gridAfter);					
//					for(int i=0;i<gridIdlist.size();i++){
//						gridsAfter.add(gridIdlist.get(i));
//					}           
//		        } 		
//			}
//			
//			Iterator<String> it = (Iterator<String>) gridsAfter.iterator();
//			int num=0;
//			while (it.hasNext()) {
//				List<Object> tmpObjects = new ArrayList<Object>();
//				tmpObjects.add(inforId);
//				tmpObjects.add(Integer.parseInt(it.next()));
//				run.update(conn, insertSql, tmpObjects.toArray());
//				//inforGridValues[num]=tmpObjects;
//				num=num+1;
//			}
//			conn.commit();
//		} catch (SQLException e) {
//			log.error(e.getMessage(), e);
//			DbUtils.rollbackAndCloseQuietly(conn);
//			throw e;
//		} finally {
//			DbUtils.closeQuietly(conn);
//		}
//	}

	public static void main(String[] args) {
		try {
			ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
					new String[] { "dubbo-consumer.xml"});
			context.start();
			new ApplicationContextUtil().setApplicationContext(context);
			final InfoChangeMsgHandler sub = new InfoChangeMsgHandler();
//			String message = "{\"geometry\":\"POINT (120.712884 31.363296);POINT (123.712884 32.363296);\",\"rowkey\":\"5f2086de-23a4-4c02-8c08-995bfe4c6f0b\",\"i_level\":2,\"b_sourceCode\":1,\"b_sourceId\":\"sfoiuojkw89234jkjsfjksf\",\"b_reliability\":3,\"INFO_NAME\":\"道路通车\",\"INFO_CONTENT\":\"广泽路通过广泽桥到来广营东路路段已经通车，需要更新道路要素\"}";
//			String message = "{\"geometry\":\"POINT (120.712884 31.363296);POINT (123.712884 32.363296);\",\"rowkey\":\"5f2086de-23a4-4c02-8c08-995bfe4c6f0b\",\"inforLevel\":2,\"feedbackType\":0,\"featureKind\":1,\"sourceCode\":1,\"roadLength\":19,\"adminName\":\"北京\",\"publishDate\":\"2017042015511230\",\"expectDate\":\"2017042015511230\",\"newsDate\":\"2017042015511230\",\"infoCode\":\"sfoiuojkw89234jkjsfjksf\",\"topicName\":\"道路通车\",\"inforName\":\"道路通车2\",\"infoContent\":\"广泽路通过广泽桥到来广营东路路段已经通车，需要更新道路要素\",\"infoTypeName\":\"INFO_TYPE_NAME\"}";
			
			String message = "{\"adminCode\":320200,\"geometry\":\"POINT (120.712884 31.363296);POINT (123.712884 32.363296);\",\"rowkey\":\"e58b02ca56de4f9d92fdd22bdc45d995\",\"inforLevel\":1,\"feedbackType\":1,\"featureKind\":2,\"sourceCode\":\"1\",\"roadLength\":19,\"adminName\":\"云南省|西双版纳傣族自治州\",\"publishDate\":\"2017-05-06 20:20:55\",\"expectDate\":\"2017-05-06 20:20:55\",\"newsDate\":\"2017-05-06 20:20:55\",\"infoCode\":\"20160306QB00000341\",\"topicName\":\"\",\"inforName\":\"0927test\",\"infoTypeName\":\"道路|普通道路\"}";			
			sub.save(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
