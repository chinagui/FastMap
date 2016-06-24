package com.navinfo.dataservice.engine.man.task;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.engine.man.city.CityOperation;
import com.navinfo.dataservice.engine.man.common.DbOperation;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.Page;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName:  TaskService 
* @author code generator
* @date 2016-06-06 06:12:30 
* @Description: TODO
*/
@Service
public class TaskService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	/**
	 * 根据用户id，与tasks的json对象批量创建task
	 * @param userId
	 * @param json
	 * @throws Exception
	 */
	public void create(long userId,JSONObject json) throws Exception{
		Connection conn = null;
		try{
			if(!json.containsKey("tasks")){
				return;
			}			
			JSONArray taskArray=json.getJSONArray("tasks");
			conn = DBConnector.getInstance().getManConnection();
			for (int i = 0; i < taskArray.size(); i++) {
				JSONObject taskJson = taskArray.getJSONObject(i);
				Task bean = (Task) JsonOperation.jsonToBean(taskJson,Task.class);
				bean.setCreateUserId((int) userId);
				createWithBean(conn,bean);
			}			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 根据task对象生成task数据，并修改相关表状态
	 * @param conn
	 * @param bean
	 * @throws Exception
	 */
	public void createWithBean(Connection conn,Task bean) throws Exception{
		try{
			TaskOperation.updateLatest(conn,bean.getCityId());
			TaskOperation.insertTask(conn, bean);
			CityOperation.updatePlanStatus(conn,bean.getCityId(),1);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public void update(JSONObject json) throws Exception{
		Connection conn = null;
		try{
			if(!json.containsKey("tasks")){return;}
			
			JSONArray taskArray=json.getJSONArray("tasks");
			
			conn = DBConnector.getInstance().getManConnection();
			
			for (int i = 0; i < taskArray.size(); i++) {
				JSONObject taskJson = taskArray.getJSONObject(i);
				Task bean=(Task) JsonOperation.jsonToBean(taskJson,Task.class);
				TaskOperation.updateTask(conn, bean);				
			}			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public Page list(JSONObject conditionJson,JSONObject orderJson,int currentPageNum,int pageSize)throws Exception{
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
					if ("cityId".equals(key)) {selectSql+=" and T.city_id="+conditionJson.getInt(key);}
					if ("createUserId".equals(key)) {selectSql+=" and T.create_user_id="+conditionJson.getInt(key);}
					if ("descp".equals(key)) {selectSql+=" and T.descp='"+conditionJson.getString(key)+"'";}
					if ("name".equals(key)) {selectSql+=" and T.name like '%"+conditionJson.getString(key)+"%'";}
					if ("status".equals(key)) {selectSql+=" and T.status="+conditionJson.getInt(key);}
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
			return TaskOperation.selectTaskBySql2(conn, selectSql, null,currentPageNum,pageSize);
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
					if ("status".equals(key)) {selectSql+=" and T.status="+conditionJson.getInt(key);}
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
	
	public HashMap<String,String> close(List<Integer> taskidList)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();	
			
			String taskIdStr=taskidList.toString().replace("[", "").replace("]", "").replace("\"", "");
			//判断任务是否可关闭
			String checkSql="SELECT T.TASK_ID, '有未关闭的BLOCK，任务无法关闭'"
					+ "  FROM TASK T, BLOCK B"
					+ " WHERE T.TASK_ID IN ("+taskIdStr+")"
					+ "   AND T.CITY_ID = B.CITY_ID"
					+ "   AND B.PLAN_STATUS <> 2"
					+ " UNION"
					+ " SELECT ST.TASK_ID, '有未关闭的月编子任务，任务无法关闭'"
					+ "  FROM SUBTASK ST"
					+ " WHERE ST.TASK_ID IN ("+taskIdStr+")"
					+ "   AND ST.STAGE = 2"
					+ "   AND ST.STATUS <> 0";
			List<List<String>> checkResult=DbOperation.exeSelectBySql(conn, checkSql, null);
			JSONArray closeTask=new JSONArray();
			List<Integer> newTask=new ArrayList<Integer>();
			newTask.addAll(taskidList);
			HashMap<String,String> checkMap=new HashMap<String,String>();
			if(checkResult.size()>0){
				List<Integer> errorTask=new ArrayList<Integer>();
				for(int i=0;i<checkResult.size();i++){
					String taskIdTmp=checkResult.get(i).get(0);
					errorTask.add(Integer.valueOf(taskIdTmp));
					if(!checkMap.containsKey(taskIdTmp)){checkMap.put(taskIdTmp, "");}
					checkMap.put(taskIdTmp, checkMap.get(taskIdTmp)+checkResult.get(i).get(1));
				}
				newTask.removeAll(errorTask);				
			}
			if(newTask.size()>0){
				String updateSql="UPDATE TASK SET STATUS=0 "
						+ "WHERE TASK_ID IN ("+newTask.toString().replace("[", "").
						replace("]", "").replace("\"", "")+")";
				DbOperation.exeUpdateOrInsertBySql(conn, updateSql);}
	    	return checkMap;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}	
}
