package com.navinfo.dataservice.engine.man.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.engine.man.block.BlockOperation;
import com.navinfo.dataservice.engine.man.city.CityOperation;
import com.navinfo.dataservice.engine.man.common.DbOperation;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.inforMan.InforManOperation;
import com.navinfo.dataservice.engine.man.subtask.SubtaskOperation;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.mq.email.EmailPublisher;
import com.navinfo.dataservice.dao.mq.sys.SysMsgPublisher;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.GridUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/** 
* @ClassName:  TaskService 
* @author code generator
* @date 2016-06-06 06:12:30 
* @Description: TODO
*/

public class TaskService {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private JSONArray newTask;
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
				Task bean = (Task) JsonOperation.jsonToBean(taskJson,Task.class);
				bean.setCreateUserId((int) userId);

				//获取grid信息
				List<Integer> gridList = GridService.getInstance().getGridListByBlockId(conn,bean.getBlockId());
				Map<Integer, Integer> gridIds = new HashMap<Integer, Integer>();
				for(Integer gridId:gridList){
					gridIds.put(gridId, 1);
				}
				bean.setGridIds(gridIds);
				
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
	 * @param conn
	 * @param taskList
	 * @return
	 * @throws Exception 
	 */
	public int create(Connection conn, List<Task> taskList) throws Exception {
		// TODO Auto-generated method stub
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

	/**
	 * 根据情报id创建blockMan，若跨2个block，需要创建各自的blockMan
	 * @param conn
	 * @param inforId
	 * @param userId
	 * @param taskId
	 * @throws Exception
	 */
	private void createInforBlock(Connection conn,String inforId,int userId,int taskId) throws Exception{
		//查询情报infor
		//Infor inforObj=InforManService.getInstance().query(inforId);
		//String inforGeo=inforObj.getGeometry();
		//查询情报city100002对应的所有block
		//select block_id,geometry from block where city_id=100002
		String selectSql="SELECT DISTINCT B.BLOCK_ID,I.INFOR_NAME||'_'||B.BLOCK_NAME BLOCK_NAME"
				+ "  FROM BLOCK B, BLOCK_GRID_MAPPING M, INFOR_GRID_MAPPING IM,INFOR I"
				+ " WHERE B.CITY_ID = 100002"
				+ "   AND B.BLOCK_ID = M.BLOCK_ID"
				+ "   AND IM.GRID_ID = M.GRID_ID"
				+ "   AND IM.INFOR_ID = I.INFOR_ID"
				+ "   AND IM.INFOR_ID='"+inforId+"'";
		List<Map<String, Object>> blockList=new ArrayList<Map<String, Object>>();
		try {
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<Map<String, Object>>> rsHandler = new ResultSetHandler<List<Map<String, Object>>>() {
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					while (rs.next()) {
						Map<String, Object> blockTmp=new HashMap<String, Object>();
						blockTmp.put("blockId", rs.getInt("BLOCK_ID"));
						blockTmp.put("blockName", rs.getString("BLOCK_NAME"));
						list.add(blockTmp);
					}
					return list;
				}

			};
			blockList= run.query(conn, selectSql, rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:" + e.getMessage(), e);
		}
		List<Integer> blockIdList=new ArrayList<Integer>();
		for(Map<String, Object> blockId:blockList){
			blockIdList.add((Integer) blockId.get("blockId"));
			createInforBlockMan(conn,(Integer) blockId.get("blockId"),String.valueOf(blockId.get("blockName")),userId,taskId);}
		BlockOperation.openBlockByBlockIdList(conn, blockIdList);
	}
	
	/**
	 * 创建情报任务
	 * @param conn
	 * @param blockId
	 * @param userId
	 * @param taskId
	 * @throws Exception
	 */
	private void createInforBlockMan(Connection conn,Integer blockId,String blockName,int userId,int taskId) throws Exception{
		String sql="insert into block_man (block_man_id,block_id,block_man_name,status,latest,create_user_id,create_date,task_id)"
				+ "values(BLOCK_MAN_SEQ.NEXTVAL,"+blockId+",'"+blockName+"',2,1,"+userId+",sysdate,"+taskId+")";
		QueryRunner run = new QueryRunner();
		run.update(conn,sql);	
	}
	
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
			//查询task数据，包含作业组leaderId
			List<Task> taskList = getTaskListWithLeader(conn,taskIds);
			
			List<Task> updatedTaskList = new ArrayList<Task>();
			List<Integer> updatedTaskIdList = new ArrayList<Integer>();
			int total = 0;
			for(Task task:taskList){
				if(task.getType() == 3){
					//二代任务发布特殊处理
				}else{
					updatedTaskList.add(task);
					updatedTaskIdList.add(task.getTaskId());
					total ++;
				}
			}
			
			//更新task状态
			TaskOperation.updateStatus(conn, taskIds);
			
			//发布消息
			taskPushMsg(conn,userId,updatedTaskList);
			
			return "task批量发布"+total+"个成功，0个失败";
//			//给作业组leader发送消息
//			int total = 0;
//			List<Object[]> msgContentList=new ArrayList<Object[]>();
//			String msgTitle="task发布";
//			for (Task task : taskList) {
//				if(task.getGroupLeader()!=0){
//					Object[] msgTmp=new Object[4];
//					msgTmp[0]=task.getGroupLeader();//收信人
//					msgTmp[1]=msgTitle;//消息头
//					msgTmp[2]="新增task:"+task.getName()+",请关注";//消息内容
//					//关联要素
//					JSONObject msgParam = new JSONObject();
//					msgParam.put("relateObject", "TASK");
//					msgParam.put("relateObjectId", task.getTaskId());
//					msgTmp[3]=msgParam.toString();//消息对象
//					msgContentList.add(msgTmp);
//				}
//				if(msgContentList.size()>0){
//					taskPushMsgByMsg(conn,msgContentList,userId);	
//				}
//				total++;
//			}
//			
//			return "task批量发布"+total+"个成功，0个失败";
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
					msgTmp[2]="新增task:"+task.getName()+",请关注";//消息内容
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
			sb.append("SELECT T.TASK_ID,T.NAME,T.STATUS,T.TYPE,UG.GROUP_ID,UG.LEADER_ID");
			sb.append(" FROM TASK T,USER_GROUP UG");
			sb.append(" WHERE T.GROUP_ID = UG.GROUP_ID");
			sb.append(" AND T.TASK_ID IN (" + StringUtils.join(taskIds.toArray(),",") + ")");
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
						task.setGroupId(rs.getInt("LEADER_ID"));
						task.setGroupLeader(rs.getInt("LEADER_ID"));
						
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
		for(Object[] msgContent:msgContentList){
			msgList[num]=msgContent;
			num+=1;
			//发送邮件
			String toMail = null;
			String mailTitle = null;
			String mailContent = null;
			//查询用户详情
			UserInfo userInfo = UserInfoOperation.getUserInfoByUserId(conn, Long.parseLong((String) msgContent[0]));
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
			//查询用户名称
			String pushUserName = null;
			if(userInfo != null){
				pushUserName = (String) userInfo.getUserRealName();
			}
			//发送消息到消息队列
			SysMsgPublisher.publishMsg((String)msgContent[1], (String)msgContent[2], userId, new long[]{Long.parseLong((String) msgContent[0])}, 2, (String)msgContent[3], pushUserName);
		}
	}
	
	
	public String update(long userId,JSONObject json) throws Exception{
		Connection conn = null;
		int total=0;
		try{
			conn = DBConnector.getInstance().getManConnection();
			JSONObject json2 = new JSONObject();
			Task bean=(Task) JsonOperation.jsonToBean(json,Task.class);
			TaskOperation.updateTask(conn, bean);
			
			//需要发消息的task列表
			List<Task> openTaskList = new ArrayList<Task>();
			Task task1 = queryByTaskId(bean.getTaskId());
			if(task1.getStatus()==1){
				openTaskList.add(task1);
			}
			
			//常规采集任务修改了出品时间或批次，其他常规任务同步更新
			if((task1.getBlockId()!=0)&&(task1.getType()==0)){
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
				List<Task> taskList = getLatestTaskListByBlockId(task1.getBlockId());
				for(Task task2:taskList){
					if((task2.getType()==2)||(task2.getType()==3)){
						Task taskTemp = (Task) JsonOperation.jsonToBean(json2,Task.class);
						taskTemp.setTaskId(task2.getTaskId());
						TaskOperation.updateTask(conn, taskTemp);
						if(task2.getStatus()==1){
							openTaskList.add(task2);
						}
					}
				}
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
				// TODO: handle exception
				e.printStackTrace();
				log.error("block编辑消息发送失败,原因:"+e.getMessage(), e);
			}

			return "任务批量修改"+total+"个成功，0个失败";
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
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
	
	public Page commonList(Connection conn,int planStatus, JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize)throws Exception{
		//常规未发布
		Page page = new Page();
		if(planStatus==1){
			page=TaskOperation.getCommonUnPushListSnapshot(conn,conditionJson,currentPageNum,pageSize);
		}else if(planStatus==2){
			//常规已发布
			page=TaskOperation.getCommonPushListSnapshot(conn,conditionJson,currentPageNum,pageSize);
		}else if(planStatus==3){
			//常规已完成
			page=TaskOperation.getCommonOverListSnapshot(conn,conditionJson,currentPageNum,pageSize);
		}else if(planStatus==4){
			//常规已关闭
			page=TaskOperation.getCommonCloseListSnapshot(conn,conditionJson,currentPageNum,pageSize);
		}
		return page;
	}
	
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
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			//查询条件
			String conditionSql = "";
			Iterator<?> conditionKeys = condition.keys();
			while (conditionKeys.hasNext()) {
				String key = (String) conditionKeys.next();
				//查询条件
				if ("programId".equals(key)) {
					conditionSql+=" AND TASK_LIST.PROGRAM_ID="+condition.getInt(key);
				}
				if ("type".equals(key)) {
					conditionSql+=" AND TASK_LIST.TYPE ="+condition.getInt(key);
				}
				if ("groupId".equals(key)) {
					conditionSql+=" AND TASK_LIST.GROUP_ID ="+condition.getInt(key);
				}
				//任务名称模糊查询
				if ("name".equals(key)) {	
					conditionSql+=" AND TASK_LIST.NAME LIKE '%" + condition.getString(key) +"%'";
				}
				//筛选条件
				//"progress":[1,3] //进度。1采集正常，2采集异常，3日编正常，4日编异常， 5月编正常，6月编异常，7已关闭，8已完成, 9草稿, 11逾期完成，12按时完成，13提前完成
				//1采集正常，2采集异常，3采集完成，4日编正常，5日编异常，6日编完成， 7月编正常，8月编异常，9月编完成，10未规划，11草稿, 12已完成，13已关闭，14按时完成，15提前完成，16逾期完成
				if ("progress".equals(key)){
					JSONArray progress = condition.getJSONArray(key);
					if(progress.isEmpty()){
						continue;
					}
					List<String> progressList = new ArrayList<String>();
					for(Object i:progress){
						int tmp=(int) i;
						//1采集正常，2采集异常，3采集完成
						if(tmp==1){progressList.add(" TASK_LIST.PROGRESS = 1 AND TASK_LIST.TYPE=0 ");}
						if(tmp==2){progressList.add(" TASK_LIST.PROGRESS = 2 AND TASK_LIST.TYPE=0");}
						if(tmp==3){progressList.add(" TASK_LIST.STATUS = 1 AND TASK_LIST.PERCENT = 100 AND TASK_LIST.TYPE=0");}
						//4日编正常，5日编异常，6日编完成
						if(tmp==4){progressList.add(" TASK_LIST.PROGRESS = 1 AND TASK_LIST.TYPE=1 ");}
						if(tmp==5){progressList.add(" TASK_LIST.PROGRESS = 2 AND TASK_LIST.TYPE=1");}
						if(tmp==6){progressList.add(" TASK_LIST.STATUS = 1 AND TASK_LIST.PERCENT = 100 AND TASK_LIST.TYPE=1");}
						//7月编正常，8月编异常，9月编完成
						if(tmp==7){progressList.add(" TASK_LIST.PROGRESS = 1 AND TASK_LIST.TYPE=2 ");}
						if(tmp==8){progressList.add(" TASK_LIST.PROGRESS = 2 AND TASK_LIST.TYPE=2");}
						if(tmp==9){progressList.add(" TASK_LIST.STATUS = 1 AND TASK_LIST.PERCENT = 100 AND TASK_LIST.TYPE=2");}
						//10未规划，11草稿, 12已完成，13已关闭
						if(tmp==10){progressList.add(" TASK_LIST.PLAN_STATUS = 0");}
						if(tmp==11){progressList.add(" TASK_LIST.STATUS = 2 ");}
						if(tmp==12){progressList.add(" TASK_LIST.STATUS = 1 AND TASK_LIST.PERCENT = 100");}
						if(tmp==13){progressList.add(" TASK_LIST.STATUS = 0 ");}
						//14按时完成，15提前完成，16逾期完成
						if(tmp==14){
							progressList.add("TASK_LIST.DIFF_DATE = 0 AND TASK_LIST.PERCENT = 100 ");
						}
						if(tmp==15){
							progressList.add("TASK_LIST.DIFF_DATE > 0 AND TASK_LIST.PERCENT = 100 ");
						}
						if(tmp==16){
							progressList.add("TASK_LIST.DIFF_DATE < 0 AND TASK_LIST.PERCENT = 100 ");
						}

						if(!progressList.isEmpty()){
							String tempSql = StringUtils.join(progressList," OR ");
							conditionSql = " AND (" + tempSql + ")";
						}
					}
				}
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
			sb.append("SELECT TASK_LIST.*,");
			sb.append("               CASE");
			sb.append("                 WHEN (TASK_LIST.STATUS = 2) THEN");
			sb.append("                  3");
			sb.append("                 WHEN (TASK_LIST.STATUS = 0) THEN");
			sb.append("                  6");
			sb.append("                 WHEN (TASK_LIST.STATUS = 4) THEN");
			sb.append("                  4");
			sb.append("                 WHEN (TASK_LIST.STATUS = 1) THEN");
			sb.append("                  CASE");
			sb.append("                    WHEN (TASK_LIST.PERCENT = 100) THEN");
			sb.append("                     5");
			sb.append("                    ELSE");
			sb.append("                     CASE");
			sb.append("                       WHEN (TASK_LIST.SUBTASK_NUM = 0) THEN");
			sb.append("                        2");
			sb.append("                       ELSE");
			sb.append("                        1");
			sb.append("                     END");
			sb.append("                  END");
			sb.append("               END ORDER_STATUS");
			sb.append("          FROM (SELECT DISTINCT P.PROGRAM_ID,");
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
			sb.append("                       NVL(FSOT.PROGRESS, 1) PROGRESS,");
			sb.append("                       NVL(FSOT.PERCENT, 0) PERCENT,");
			sb.append("                       NVL(FSOT.DIFF_DATE, 0) DIFF_DATE,");
			sb.append("                       B.BLOCK_ID,");
			sb.append("                       B.PLAN_STATUS,");
			sb.append("                       (SELECT COUNT(1)");
			sb.append("                          FROM SUBTASK ST");
			sb.append("                         WHERE ST.TASK_ID = T.TASK_ID");
			sb.append("                           AND ST.STATUS = 1) SUBTASK_NUM");
			sb.append("                  FROM BLOCK B, PROGRAM P, TASK T, FM_STAT_OVERVIEW_TASK FSOT,USER_GROUP UG");
			sb.append("                 WHERE T.BLOCK_ID = B.BLOCK_ID");
			sb.append("                   AND T.TASK_ID = FSOT.TASK_ID(+)");
			sb.append("                   AND P.CITY_ID = B.CITY_ID");
			sb.append("                   AND UG.GROUP_ID = T.GROUP_ID");
			sb.append("	             AND T.PROGRAM_ID = P.PROGRAM_ID");
			sb.append("	          UNION");
			sb.append("	          SELECT DISTINCT P.PROGRAM_ID,");
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
			sb.append("	                          1             PROGRESS,");
			sb.append("	                          0             PERCENT,");
			sb.append("	                          0             DIFF_DATE,");
			sb.append("	                          B.BLOCK_ID,");
			sb.append("	                          B.PLAN_STATUS,");
			sb.append("	                          0             SUBTASK_NUM");
			sb.append("	            FROM BLOCK B, PROGRAM P");
			sb.append("	           WHERE P.CITY_ID = B.CITY_ID");
			sb.append("	        	 AND P.LATEST = 1");
			sb.append("	             AND NOT EXISTS (SELECT 1");
			sb.append("	                    FROM TASK T");
			sb.append("	                   WHERE T.BLOCK_ID = B.BLOCK_ID");
			sb.append("	                     AND T.LATEST = 1)) TASK_LIST");
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
						task.put("taskName", rs.getString("NAME"));
						task.put("status", rs.getInt("STATUS"));
						task.put("type", rs.getInt("TYPE"));
						
						task.put("percent", rs.getInt("PERCENT"));
						task.put("diffDate", rs.getInt("DIFF_DATE"));
						task.put("progress", rs.getInt("PROGRESS"));
						
						task.put("groupId", rs.getInt("GROUP_ID"));
						task.put("groupName", rs.getString("GROUP_NAME"));

						Timestamp planStartDate = rs.getTimestamp("PLAN_START_DATE");
						Timestamp planEndDate = rs.getTimestamp("PLAN_START_DATE");
						if(planStartDate != null){
							task.put("planStartDate", df.format(planStartDate));
						}else {task.put("planStartDate", null);}
						if(planEndDate != null){
							task.put("planEndDate",df.format(planEndDate));
						}else{task.put("planEndDate", null);}
						
						task.put("roadPlanTotal", rs.getInt("ROAD_PLAN_TOTAL"));
						task.put("poiPlanTotal", rs.getString("POI_PLAN_TOTAL"));
						totalCount=rs.getInt("TOTAL_RECORD_NUM");
						list.add(task);
					}					
					page.setTotalCount(totalCount);
					page.setResult(list);
					return page;
				}

			};
			
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
	
	/*
	 * 查询task
	 * 关闭task,相应修改block状态
	 * 采集任务:
	 * 		常规采集任务关闭:调整任务范围;调整日编任务范围,调整区域子任务范围;调整二代编辑任务范围
	 * 		快速更新采集任务关闭:调整任务范围;调整日编任务范围,调整区域子任务范围;调整项目范围;
	 * 日编任务:
	 * 		快速更新日编任务关闭:调整项目范围.
	 * 发送消息
	 */
	public String close(int taskId, long userId)throws Exception{
		Connection conn = null;
		try{
			
			conn = DBConnector.getInstance().getManConnection();	
			Task task = queryByTaskId(taskId);
			//更新任务状态
			Task taskTemp = new Task();
			taskTemp.setTaskId(taskId);
			taskTemp.setStatus(0);
			TaskOperation.updateTask(conn, taskTemp);
			//更新block状态：如果所有task都已关闭，则block状态置3
			TaskOperation.closeBlock(conn,task.getBlockId());
			
			//动态调整
			//获取该任务新增的grid
			Map<Integer, Integer> gridIdMap = TaskOperation.getAddedGridMap(conn,taskId);
			//调整该任务范围
			TaskOperation.insertTaskGridMapping(conn, taskId, gridIdMap);
			
			//采集任务
			if(task.getType() == 0){
				//调整日编任务范围
				TaskOperation.updateTaskRegion(conn,taskId,1,gridIdMap);
				//调整区域子任务范围
				List<Subtask> subtaskList = TaskOperation.getSubTaskListByType(conn,taskId,4);
				for(Subtask subtask:subtaskList){
					SubtaskOperation.insertSubtaskGridMapping(conn, subtask.getSubtaskId(), gridIdMap);;
				}
				if(task.getBlockId()==0){
					//调整项目范围
					
				}else{
					//调整二代任务范围
					TaskOperation.updateTaskRegion(conn,taskId,4,gridIdMap);
				}
			}
			//日编任务,快速更新项目
			else if((task.getType()==1)&&(task.getBlockId()==0)){
				//调整项目范围
				
			}
			
			//发送消息
			try {
				List<Object[]> msgContentList=new ArrayList<Object[]>();
				String msgTitle="task发布";
				if(task.getGroupLeader()!=0){
					Object[] msgTmp=new Object[4];
					msgTmp[0]=task.getGroupLeader();//收信人
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
				// TODO: handle exception
				e.printStackTrace();
				log.error("task关闭消息发送失败,原因:"+e.getMessage(), e);
			}
			return "";
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
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
			QueryRunner run=new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT T.TASK_ID,T.NAME,T.STATUS,T.DESCP,T.TYPE,T.PLAN_START_DATE,T.PLAN_END_DATE,");
			sb.append("T.PRODUCE_PLAN_START_DATE,T.PRODUCE_PLAN_END_DATE,");
			sb.append("T.LOT,T.POI_PLAN_TOTAL,T.ROAD_PLAN_TOTAL,");
			sb.append("B.BLOCK_ID,B.BLOCK_NAME,B.WORK_PROPERTY,");
			sb.append("P.PROGRAM_ID,P.NAME PROGRAM_NAME,P.TYPE PROGRAM_TYPE,");
			sb.append("U.USER_ID,U.USER_REAL_NAME,");
			sb.append("UG.GROUP_ID,UG.GROUP_NAME");
			sb.append(" FROM TASK T,BLOCK B,PROGRAM P,USER_GROUP UG,USER_INFO U");
			sb.append(" WHERE T.BLOCK_ID = B.BLOCK_ID");
			sb.append(" AND T.PROGRAM_ID = T.PROGRAM_ID");
			sb.append(" AND T.GROUP_ID = UG.GROUP_ID");
			sb.append(" AND T.CREATE_USER_ID = U.USER_ID");
			sb.append(" AND T.TASK_ID = " + taskId);
			String selectSql= sb.toString();

			ResultSetHandler<Task> rsHandler = new ResultSetHandler<Task>() {
				public Task handle(ResultSet rs) throws SQLException {
					Task task = new Task();
					if (rs.next()) {
						task.setTaskId(rs.getInt("TASK_ID"));
						task.setName(rs.getString("NAME"));
						task.setStatus(rs.getInt("STATUS"));
						task.setDescp(rs.getString("DESCP"));
						task.setType(rs.getInt("TYPE"));
						task.setPlanStartDate(rs.getTimestamp("PLAN_START_DATE"));
						task.setPlanEndDate(rs.getTimestamp("PLAN_END_DATE"));
						task.setProducePlanStartDate(rs.getTimestamp("PRODUCE_PLAN_START_DATE"));
						task.setProducePlanEndDate(rs.getTimestamp("PRODUCE_PLAN_END_DATE"));
						task.setLot(rs.getInt("LOT"));
						task.setPoiPlanTotal(rs.getInt("POI_PLAN_TOTAL"));
						task.setRoadPlanTotal(rs.getInt("ROAD_PLAN_TOTAL"));
						task.setBlockId(rs.getInt("BLOCK_ID"));
						task.setBlockName(rs.getString("BLOCK_NAME"));
						task.setWorkProperty(rs.getInt("WORK_PROPERTY"));
						task.setProgramId(rs.getInt("PROGRAM_ID"));
						task.setProgramName(rs.getString("PROGRAM_NAME"));
						task.setProgramType(rs.getInt("PROGRAM_TYPE"));
						task.setCreateUserId(rs.getInt("USER_ID"));
						task.setCreateUserName(rs.getString("USER_REAL_NAME"));
						task.setGroupId(rs.getInt("GROUP_ID"));
						task.setGroupName(rs.getString("GROUP_NAME"));
						
						Map<Integer, Integer> gridIds;
						try {
							gridIds = getGridMapByTaskId(task.getTaskId());
							task.setGridIds(gridIds);
							
							JSONArray jsonArray = JSONArray.fromObject(gridIds.keySet().toArray());
							String wkt = GridUtils.grids2Wkt(jsonArray);
							task.setGeometry(Geojson.wkt2Geojson(wkt));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					return task;
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

	/*
	 * 返回task详细信息
	 * 包含block,program,几何信息
	 */
	public Map<String,Object> query(int taskId) throws Exception {
		try{
			Task task = queryByTaskId(taskId);
			
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("taskId", task.getTaskId());
			map.put("name", task.getName());
			map.put("status", task.getStatus());
			map.put("descp", task.getDescp());
			map.put("type", task.getType());
			map.put("planStartDate", task.getPlanStartDate());
			map.put("planEndDate", task.getPlanEndDate());
			map.put("producePlanStartDate", task.getProducePlanStartDate());
			map.put("producePlanEndDate", task.getProducePlanEndDate());
			map.put("lot", task.getLot());
			map.put("poiPlanTotal", task.getPoiPlanTotal());
			map.put("roadPlanTotal", task.getRoadPlanTotal());
			map.put("blockId", task.getBlockId());
			map.put("blockName", task.getBlockName());
			map.put("workProperty", task.getWorkProperty());
			map.put("programId", task.getProgramId());
			map.put("programType", task.getProgramName());
			map.put("creatUserId", task.getCreateUserId());
			map.put("creatUserName", task.getCreateUserName());
			map.put("groupId", task.getGroupId());
			map.put("groupName", task.getGroupName());
			map.put("gridIds", task.getGridIds());
			map.put("geometry", task.getGeometry());
			
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
	
	public List<Task> queryTaskAll() throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "SELECT * FROM TASK";
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
					// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			String selectSql = "SELECT M.GRID_ID FROM TASK_GRID_MAPPING M WHERE M.TASK_ID = " + taskId;
			
			ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>() {
				public JSONArray handle(ResultSet rs) throws SQLException {
					ArrayList<String> arrayList = new ArrayList<String>();
					if(rs.next()) {
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
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
