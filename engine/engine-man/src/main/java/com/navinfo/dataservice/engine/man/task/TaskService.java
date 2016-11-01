package com.navinfo.dataservice.engine.man.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.engine.man.message.MessageOperation;
import com.navinfo.dataservice.engine.man.block.BlockOperation;
import com.navinfo.dataservice.engine.man.city.CityOperation;
import com.navinfo.dataservice.engine.man.common.DbOperation;
import com.navinfo.dataservice.engine.man.inforMan.InforManOperation;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
			for (int i = 0; i < taskArray.size(); i++) {
				JSONObject taskJson = taskArray.getJSONObject(i);
				Task bean = (Task) JsonOperation.jsonToBean(taskJson,Task.class);
				//情报任务，需要同时创建block任务
				if(bean.getTaskType()==4){
					bean.setCityId(100002);
				}
				bean.setCreateUserId((int) userId);
				int taskId=createWithBean(conn,bean);
				//情报任务，需要同时创建block任务
				if(bean.getTaskType()==4){
					String inforId=taskJson.getString("inforId");
					createInforBlock(conn,inforId,(int) userId,taskId);
					//修改情报任务状态
					InforManOperation.updateTask(conn,inforId,taskId);
				}				
				total+=1;			
			}
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
	
	public String taskPushMsg(long userId,JSONArray taskIds) throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			//发送消息
			JSONObject condition=new JSONObject();
			condition.put("taskIds",taskIds);
			List<Map<String, Object>> openTasks = TaskOperation.queryTaskTable(conn, condition);
			/*任务创建/编辑/关闭
			 * 1.所有生管角色
			 * 2.分配的月编作业组组长
			 * 任务:XXX(任务名称)内容发生变更，请关注*/			
			String msgTitle="任务开启";
			List<String> msgContentList=new ArrayList<String>();
			List<Long> groupIdList = new ArrayList<Long>();
			for(Map<String, Object> task:openTasks){
				msgContentList.add("新增任务:"+task.get("taskName")+",请关注");
				groupIdList.add((Long) task.get("monthEditGroupId"));
			}
			if(msgContentList.size()>0){
				taskPushMsg(conn,msgTitle,msgContentList,groupIdList,userId);
			}		
			TaskOperation.updateStatus(conn,taskIds);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		return "发布成功";
		
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
			//情报任务不更新
			if(bean.getTaskType()!=4){
				TaskOperation.updateLatest(conn,bean.getCityId());
			}			
			taskId=TaskOperation.getNewTaskId(conn);
			bean.setTaskId(taskId);
			TaskOperation.insertTask(conn, bean);
			CityOperation.updatePlanStatus(conn,bean.getCityId(),1);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
		return taskId;
	}
	
	public String update(long userId,JSONObject json) throws Exception{
		Connection conn = null;
		int total=0;
		try{
			if(!json.containsKey("tasks")){return "任务批量修改"+total+"个成功，0个失败";}
			
			JSONArray taskArray=json.getJSONArray("tasks");
			conn = DBConnector.getInstance().getManConnection();
			JSONObject condition=new JSONObject();
			JSONArray taskStatus=new JSONArray();
			taskStatus.add(1);//任务是开启状态
			condition.put("taskStatus", taskStatus);
			JSONArray taskIds=new JSONArray();
			for (int i = 0; i < taskArray.size(); i++) {
				JSONObject taskJson = taskArray.getJSONObject(i);
				Task bean=(Task) JsonOperation.jsonToBean(taskJson,Task.class);
				taskIds.add(bean.getTaskId());
				TaskOperation.updateTask(conn, bean);		
				total+=1;
			}
			condition.put("taskIds",taskIds);
			List<Map<String, Object>> openTasks = TaskOperation.queryTaskTable(conn, condition);
			/*任务创建/编辑/关闭
			 * 1.所有生管角色
			 * 2.分配的月编作业组组长
			 * 任务:XXX(任务名称)内容发生变更，请关注*/			
			String msgTitle="任务修改";
			List<String> msgContentList=new ArrayList<String>();
			for(Map<String, Object> task:openTasks){
				msgContentList.add("任务:"+task.get("taskName")+"内容发生变更，请关注");
			}
			if(msgContentList.size()>0){
				taskPushMsg(conn,msgTitle,msgContentList);
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
	
	/*任务创建/编辑/关闭
	 * 1.所有生管角色
	 * 2.分配的月编作业组组长
	 * 任务:XXX(任务名称)内容发生变更，请关注*/
	public void taskPushMsg(Connection conn,String msgTile,List<String> msgContentList, List<Long> groupIdList, long pushUser) throws Exception{
		String userSql="SELECT DISTINCT M.USER_ID FROM ROLE_USER_MAPPING M WHERE M.ROLE_ID =3";
		List<Integer> userIdList=UserInfoOperation.getUserListBySql(conn, userSql);
		//查询分配的月编作业组组长
		List<Long> leaderIdByGroupId = UserInfoOperation.getLeaderIdByGroupId(conn, groupIdList);
		for (Long leaderId : leaderIdByGroupId) {
			userIdList.add(leaderId.intValue());
		}
		Object[][] msgList=new Object[userIdList.size()*msgContentList.size()][3];
		int num=0;
		for(int userId:userIdList){
			for(String msgContent:msgContentList){
				msgList[num][0]=userId;
				msgList[num][1]=msgTile;
				msgList[num][2]=msgContent;
				num+=1;
			}
		}
		MessageOperation.batchInsert(conn,msgList,pushUser);
		//发送邮件
		//查询用户详情
		List<Map<String, Object>> userInfoList = new ArrayList<Map<String,Object>>();
		for (int userId : userIdList) {
			Map<String, Object> userInfo = UserInfoOperation.getUserInfoByUserId(conn, userId);
			userInfoList.add(userInfo);
		}
	}
	
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
	
	public Page inforList(Connection conn,int planStatus, JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize)throws Exception{
		//情报未发布
		Page page = new Page();
		if(planStatus==1){
			page=TaskOperation.getInforUnPushListSnapshot(conn,conditionJson,currentPageNum,pageSize);
		}else if(planStatus==2){
			//情报已发布
			page=TaskOperation.getInforPushListSnapshot(conn,conditionJson,currentPageNum,pageSize);
		}else if(planStatus==3){
			//情报已完成
			page=TaskOperation.getInforOverListSnapshot(conn,conditionJson,currentPageNum,pageSize);
		}else if(planStatus==4){
			//情报已关闭
			page=TaskOperation.getInforCloseListSnapshot(conn,conditionJson,currentPageNum,pageSize);
		}
		return page;
	}
		
	public Page list(int taskType, int planStatus, JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize,int snapshot)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			if (snapshot==1){
				if(taskType==4){
					//情报任务查询列表
					return this.inforList(conn,planStatus, conditionJson, orderJson, currentPageNum, pageSize);
				}else{
					//常规任务查询列表
					return this.commonList(conn,planStatus, conditionJson, orderJson, currentPageNum, pageSize);
				}
			}else{
				Page page = TaskOperation.getListIntegrate(conn,conditionJson,orderJson,currentPageNum,pageSize);
				return page;
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public List<Task> listAll(JSONObject conditionJson,JSONObject orderJson)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			
			String selectSql = "select T.*, nvl(C.CITY_NAME,'') CITY_NAME, nvl(U.USER_REAL_NAME,'') USER_REAL_NAME, nvl(G.GROUP_NAME,'') GROUP_NAME"
					+ "  FROM TASK T, CITY C, USER_INFO U, USER_GROUP G"
					+ " WHERE T.CITY_ID = C.CITY_ID(+)"
					+ "   AND T.CREATE_USER_ID = U.USER_ID(+)"
					+ "   AND T.MONTH_EDIT_GROUP_ID = G.GROUP_ID(+)"
					+ "   AND T.LATEST = 1";
			if(null!=conditionJson && !conditionJson.isEmpty()){
				Iterator keys = conditionJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("taskId".equals(key)) {selectSql+=" and T.task_id="+conditionJson.getInt(key);}
					if ("cityIds".equals(key)) {selectSql+=" and T.city_id in ("+StringUtils.join(conditionJson.getJSONArray(key), ",")+")";}
					if ("createUserId".equals(key)) {selectSql+=" and T.create_user_id="+conditionJson.getInt(key);}
					if ("descp".equals(key)) {selectSql+=" and T.descp='"+conditionJson.getString(key)+"'";}
					if ("name".equals(key)) {selectSql+=" and T.name like '%"+conditionJson.getString(key)+"%'";}
					if ("status".equals(key)) {selectSql+=" and T.status in ("+conditionJson.getJSONArray(key).join(",")+")";}
					if ("createUserName".equals(key)) {selectSql+=" and U.USER_REAL_NAME like '%"+conditionJson.getString(key)+"%'";}
					if ("cityName".equals(key)) {selectSql+=" and C.CITY_NAME like '%"+conditionJson.getString(key)+"%'";}
					}
				}
			if(null!=orderJson && !orderJson.isEmpty()){
				Iterator keys = orderJson.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if ("status".equals(key)) {selectSql+=" order by T.status "+orderJson.getString(key);break;}
					if ("taskId".equals(key)) {selectSql+=" order by T.TASK_ID "+orderJson.getString(key);break;}
					if ("planStartDate".equals(key)) {selectSql+=" order by T.PLAN_START_DATE "+orderJson.getString(key);break;}
					if ("planEndDate".equals(key)) {selectSql+=" order by T.PLAN_END_DATE "+orderJson.getString(key);break;}
					if ("monthEditPlanStartDate".equals(key)) {selectSql+=" order by T.MONTH_EDIT_PLAN_START_DATE "+orderJson.getString(key);break;}
					if ("monthEditPlanEndDate".equals(key)) {selectSql+=" order by T.MONTH_EDIT_PLAN_END_DATE "+orderJson.getString(key);break;}
					}
			}else{
				selectSql+=" order by T.TASK_ID";
			}
			return TaskOperation.selectTaskBySql2(conn, selectSql, null);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}	
	
	public List<Integer> close(List<Integer> taskidList)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();	
			
			String taskIdStr=taskidList.toString().replace("[", "").replace("]", "").replace("\"", "");
			//判断任务是否可关闭
			String checkSql="SELECT T.TASK_ID"
					+ "    FROM TASK T, BLOCK_MAN BM"
					+ "   WHERE T.TASK_ID = BM.TASK_ID"
					+ "     AND NOT EXISTS (SELECT 1"
					+ "            FROM BLOCK_MAN BMM"
					+ "           WHERE BMM.TASK_ID = T.TASK_ID"
					+ "             AND BMM.STATUS <> 0)";
					
					/*"SELECT T.TASK_ID, '有未关闭的BLOCK，任务无法关闭'"
					+ "  FROM TASK T, BLOCK B"
					+ " WHERE T.TASK_ID IN ("+taskIdStr+")"
					+ "   AND T.CITY_ID = B.CITY_ID"
					+ "   AND B.PLAN_STATUS <> 2"
					+ " UNION"
					+ " SELECT ST.TASK_ID, '有未关闭的月编子任务，任务无法关闭'"
					+ "  FROM SUBTASK ST"
					+ " WHERE ST.TASK_ID IN ("+taskIdStr+")"
					+ "   AND ST.STAGE = 2"
					+ "   AND ST.STATUS <> 0";*/
			List<List<String>> checkResult=DbOperation.exeSelectBySql(conn, checkSql, null);
			JSONArray closeTask=new JSONArray();
			List<Integer> newTask=new ArrayList<Integer>();
			//newTask.addAll(taskidList);
			HashMap<String,String> checkMap=new HashMap<String,String>();
			
			if(checkResult.size()>0){
				for(int i=0;i<checkResult.size();i++){
					String taskIdTmp=checkResult.get(i).get(0);
					newTask.add(Integer.valueOf(taskIdTmp));
				}		
			}
			
			/*if(checkResult.size()>0){
				List<Integer> errorTask=new ArrayList<Integer>();
				for(int i=0;i<checkResult.size();i++){
					String taskIdTmp=checkResult.get(i).get(0);
					errorTask.add(Integer.valueOf(taskIdTmp));
					if(!checkMap.containsKey(taskIdTmp)){checkMap.put(taskIdTmp, "");}
					checkMap.put(taskIdTmp, checkMap.get(taskIdTmp)+checkResult.get(i).get(1));
				}
				newTask.removeAll(errorTask);				
			}*/
			if(newTask.size()>0){
				String updateSql="UPDATE TASK SET STATUS=0 "
						+ "WHERE TASK_ID IN ("+newTask.toString().replace("[", "").
						replace("]", "").replace("\"", "")+")";
				DbOperation.exeUpdateOrInsertBySql(conn, updateSql);
				
				//关闭对应的city
				CityOperation.close(conn);
				
				//关闭对应的infor
				InforManOperation.closeByTasks(conn,newTask);
			
				//发送消息
				JSONObject condition=new JSONObject();
				condition.put("taskIds",JSONArray.fromObject(newTask));
				List<Map<String, Object>> openTasks = TaskOperation.queryTaskTable(conn, condition);
				/*任务创建/编辑/关闭
				 * 1.所有生管角色
				 * 2.分配的月编作业组组长
				 * 任务:XXX(任务名称)内容发生变更，请关注*/			
				String msgTitle="任务关闭";
				List<String> msgContentList=new ArrayList<String>();
				for(Map<String, Object> task:openTasks){
					msgContentList.add("任务:"+task.get("taskName")+"内容发生变更，请关注");
				}
				if(msgContentList.size()>0){
					taskPushMsg(conn,msgTitle,msgContentList);
				}
			}
	    	return newTask;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public Map<String, Object> query(int taskId) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();	
			List<Map<String, Object>> result=TaskOperation.queryTask(conn,taskId);
			if(result!=null && result.size()>0){
				return result.get(0);
			}
			return null;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public Page queryMonthTask(int monthEditGroupId, JSONObject condition, int curPageNum, int curPageSize) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();	
			return TaskOperation.queryMonthTask(conn,monthEditGroupId,condition,curPageNum,curPageSize);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
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
}
