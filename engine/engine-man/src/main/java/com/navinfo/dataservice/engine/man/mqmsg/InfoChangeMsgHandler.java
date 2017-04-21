package com.navinfo.dataservice.engine.man.mqmsg;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.model.Infor;
import com.navinfo.dataservice.api.man.model.Program;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.mq.MsgHandler;
import com.navinfo.dataservice.dao.mq.email.EmailPublisher;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.dataservice.engine.man.block.BlockService;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskOperation;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.engine.man.userGroup.UserGroupService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Geometry;

import infor.InforService;
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
	String sql = "INSERT INTO INFOR(INFOR_ID,INFOR_NAME,GEOMETRY,INFOR_LEVEL,PLAN_STATUS,INFOR_CONTENT,feature_Kind) "
			+ "VALUES (?,?,?,?,0,?,?)";

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
//			String content=dataJson.getString("INFO_CONTENT");
//			if(content.length()>=400){
//				content=content.substring(0, 350);
//			}
//			values.add(content);
//			values.add(dataJson.getString("b_featureKind"));
//			
//			String method = dataJson.getString("method");
//			
//			QueryRunner run = new QueryRunner();
//			run.update(conn, sql, values.toArray());
//			
//			//初始化infor_grid_mapping关系表
//			String insertSql = "INSERT INTO infor_grid_mapping(infor_id,grid_id) VALUES(?,?)";
//			String[] inforGeoList = inforGeo.split(";");
//			for (String geoTmp : inforGeoList) {
//				Geometry inforTmp = GeoTranslator.wkt2Geometry(geoTmp);
//				Set<?> grids = (Set<?>) CompGeometryUtil.geo2GridsWithoutBreak(inforTmp);
//				Iterator<String> it = (Iterator<String>) grids.iterator();
//				Object[][] inforGridValues=new Object[grids.size()][2];
//				int num=0;
//				while (it.hasNext()) {
//					List<Object> tmpObjects = new ArrayList<Object>();
//					tmpObjects.add(inforId);
//					tmpObjects.add(Integer.parseInt(it.next()));
//					run.update(conn, insertSql, tmpObjects.toArray());
//					//inforGridValues[num]=tmpObjects;
//					num=num+1;
//				}
//				
//			}
//			
//			//"情报对应方式”字段不为空时，自动创建项目、任务、子任务
//			if(method!=null){
//				generateManAccount(conn,inforId);
//			}
//			
//			//发送消息
//			taskPushMsg(conn, dataJson.getString("INFO_NAME"), 0,inforId);	
//			
//			conn.commit();
//			
//		} catch (SQLException e) {
//			log.error(e.getMessage(), e);
//			DbUtils.rollbackAndCloseQuietly(conn);
//			throw e;
//		} finally {
//			DbUtils.closeQuietly(conn);
//		}
//	}
	public void save(String message) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			//新增情报
			JSONObject dataJson = JSONObject.fromObject(message);
			Infor infor = InforService.getInstance().create(dataJson, 0);
			
			//"情报对应方式”字段不为空时，自动创建项目、任务、子任务
			if(infor.getMethod()!=null){
				generateManAccount(conn,infor);
			}
			
			//发送消息
			taskPushMsg(conn, dataJson.getString("inforName"), 0,infor.getInforId());	
			
			conn.commit();
			
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
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
		program.setName(infor.getInforName() + df.format(new Date()) + programId);
		program.setType(4);

		program.setCollectPlanStartDate((Timestamp)getCalculatedDate(infor.getPublishDate(),1));
		if(infor.getMethod().equals("预采集")){
			program.setCollectPlanEndDate((Timestamp)getCalculatedDate(infor.getPublishDate(),7));
		}else{
			program.setCollectPlanEndDate((Timestamp)getCalculatedDate(infor.getPublishDate(),3));
		}
		
		program.setDayEditPlanStartDate((Timestamp)getCalculatedDate(program.getCollectPlanEndDate(),1));
		program.setDayEditPlanEndDate((Timestamp)getCalculatedDate(program.getCollectPlanEndDate(),2));
		program.setProducePlanStartDate(program.getDayEditPlanEndDate());
		program.setProducePlanEndDate((Timestamp)getCalculatedDate(program.getCollectPlanEndDate(),2));
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
		for(Task task:taskList){
			if(task.getGroupId()!=0){
				taskListToPublish.add(task);
				commontaskIds.add(task.getTaskId());
				if(task.getType()==0){
					collectTaskList.add(task);
				}
			}
		}
		//发布任务
		if(taskListToPublish.size()>0){
			//更新task状态
			TaskOperation.updateStatus(conn, commontaskIds,1);
			//发布消息
			TaskService.getInstance().taskPushMsg(conn, 0, taskListToPublish);
		}
		//采集任务创建子任务
		for(Task task:collectTaskList){
			Subtask subtask = new Subtask();
			int subtaskId = SubtaskOperation.getSubtaskId(conn, subtask);
			subtask.setSubtaskId(subtaskId);
			subtask.setName(infor.getInforName()+"_"+df.format(new Date())+"_"+subtaskId);
			subtask.setType(2);
			subtask.setStage(0);
			subtask.setTaskId(task.getTaskId());
			subtask.setPlanStartDate(task.getPlanStartDate());
			subtask.setPlanEndDate(task.getPlanEndDate());
			subtask.setGridIds(task.getGridIds());
			List<Integer> gridIdList = subtask.getGridIds();
			if(!gridIdList.isEmpty()){
				String wkt = GridUtils.grids2Wkt(JSONArray.fromObject(gridIdList));
				subtask.setGeometry(wkt);
			}
			SubtaskService.getInstance().createSubtaskWithSubtaskId(conn,subtask);
		}
	}

	/**
	 * @param publishDate
	 * @param i
	 * @return
	 */
	private Timestamp getCalculatedDate(Date publishDate, int i) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(publishDate);
		cal.add(Calendar.DATE, 1);
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
	public void taskPushMsg(Connection conn,String infoName, long pushUser, String inforId) {
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
			String message = "{\"geometry\":\"POINT (120.712884 31.363296);POINT (123.712884 32.363296);\",\"rowkey\":\"5f2086de-23a4-4c02-8c08-995bfe4c6f0b\",\"inforLevel\":2,\"feedbackType\":0,\"featureKind\":1,\"sourceCode\":1,\"roadLength\":19,\"adminName\":\"北京\",\"publishDate\":\"2017042015511230\",\"expectDate\":\"2017042015511230\",\"newsDate\":\"2017042015511230\",\"infoCode\":\"sfoiuojkw89234jkjsfjksf\",\"topicName\":\"道路通车\",\"inforName\":\"道路通车\",\"infoContent\":\"广泽路通过广泽桥到来广营东路路段已经通车，需要更新道路要素\",\"infoTypeName\":\"INFO_TYPE_NAME\"}";
			sub.save(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
