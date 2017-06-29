package com.navinfo.dataservice.engine.man.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;
import com.navinfo.dataservice.api.man.model.TaskProgress;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.websocket.TaskOther2MediumWebSocketHandler;
import com.navinfo.navicommons.database.QueryRunner;

public class TaskProgressOperation {
	private static Logger log = LoggerRepos.getLogger(TaskProgressOperation.class);
	
	public static int taskOther2MediumJob=1;
	
	public static int taskCreate=0;
	public static int taskWorking=1;
	public static int taskSuccess=2;
	public static int taskFail=3;
	
	public static void create(Connection conn,TaskProgress bean) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "INSERT INTO TASK_PROGRESS P"
					+ "  (TASK_ID, PHASE, STATUS, CREATE_DATE, PHASE_ID)"
					+ "VALUES"
					+ "  ("+bean.getTaskId()+","+bean.getPhase()+", 0, SYSDATE, "+bean.getPhaseId()+")" ;
			run.update(conn, selectSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询create TaskProgressOperation，原因为:"+e.getMessage(),e);
		}
	}
	
	public static int getNewPhaseId(Connection conn) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "select task_progress_SEQ.NEXTVAL phaseId from dual";
			return run.query(conn, selectSql, new ResultSetHandler<Integer>(){

				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if(rs.next()){
						return rs.getInt("phaseId");
					}
					return 0;
				}});
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询getNewPhaseId，原因为:"+e.getMessage(),e);
		}
	}
	
	public static int startProgress(Connection conn,Long userId,int phaseId) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String selectSql = "UPDATE TASK_PROGRESS SET STATUS = 1,start_date=sysdate,operator="+userId+" wHERE STATUS IN(0,3) AND PHASE_ID = "+phaseId ;
			return run.update(conn, selectSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("startProgress，原因为:"+e.getMessage(),e);
		}
	}
	
	public static void updateProgress(Connection conn,int phaseId,int status,String message)  throws Exception {
		try{
			if(status==0&&(message==null||message.isEmpty())){return;}
			if(message!=null&&message.length()>500){message=message.substring(0, 500);}
			QueryRunner run = new QueryRunner();
			String selectSql ="";
			if(status==0||status==1){
				selectSql = "UPDATE TASK_PROGRESS SET message=substr(message||?,0,1000) WHERE PHASE_ID = "+phaseId ;
			}else{
				String updateMsg="";
				if(message!=null){updateMsg=",message=substr(message||?,0,1000)";}
				selectSql = "UPDATE TASK_PROGRESS SET STATUS = "+status+updateMsg+",end_date=sysdate WHERE PHASE_ID = "+phaseId ;
			}
			log.info("endProgress:"+selectSql);
			if(message==null||message.isEmpty()){
				run.update(conn, selectSql);}
			else{run.update(conn, selectSql,message);}
			log.info("phaseId:"+phaseId+",status:"+status+",message:"+message);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("endProgress失败，原因为:"+e.getMessage(),e);
		}
	}
	
	public static void endProgressAndSocket(Connection conn,int phaseId,int status,String message) throws Exception{
		updateProgress(conn,phaseId,status,message);
		pushWebsocket(conn,phaseId);
	}
	
	public static void pushWebsocket(Connection conn,int phaseId){
		try{
			TaskProgress taskProgress = queryByPhaseId(conn, phaseId);
			JSONObject msg=new JSONObject();
			msg.put("phaseId", phaseId);
			msg.put("taskId", taskProgress.getTaskId());
			msg.put("status", taskProgress.getStatus());
			String sysMsg = JSONArray.fromObject(msg).toString();
			TaskOther2MediumWebSocketHandler.getInstance().sendMessageToUser(taskProgress.getOperator().toString(),
					new TextMessage(sysMsg));
		}catch (Exception e) {
			log.error("task_progress websocket消息发送失败", e);
		}
	}
	
	/**
	 * 获取任务号对应的phase阶段的最新记录
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	public static TaskProgress queryLatestByTaskId(Connection conn,int taskId,int phase) throws Exception {
		QueryRunner run =null;
		try{
			run = new QueryRunner();
			String selectSql = "SELECT T.PHASE_ID,"
					+ "       T.TASK_ID,"
					+ "       T.status,"
					+ "       T.operator,"
					+ "       T.PHASE"
					+ "  FROM TASK_PROGRESS T"
					+ " WHERE T.TASK_ID = "+taskId
					+ " and T.PHASE = "+phase
					+ " order by t.CREATE_DATE desc";
			ResultSetHandler<TaskProgress> rsHandler = new ResultSetHandler<TaskProgress>() {
				public TaskProgress handle(ResultSet rs) throws SQLException {
					
					while(rs.next()) {
						TaskProgress progress=new TaskProgress();
						progress.setTaskId(rs.getInt("task_id"));
						progress.setPhaseId(rs.getInt("phase_id"));
						progress.setOperator(rs.getLong("operator"));
						progress.setPhase(rs.getInt("phase"));
						progress.setStatus(rs.getInt("status"));
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
	 * 
	 * @param phaseId
	 * @return
	 * @throws Exception 
	 */
	public static TaskProgress queryByPhaseId(Connection conn,int phaseId) throws Exception {
		QueryRunner run =null;
		try{
			run = new QueryRunner();
			String selectSql = "SELECT T.PHASE_ID,"
					+ "       T.TASK_ID,"
					+ "       T.status,"
					+ "       T.operator,"
					+ "       T.PHASE"
					+ "  FROM TASK_PROGRESS T"
					+ " WHERE T.PHASE_id = "+phaseId;
			ResultSetHandler<TaskProgress> rsHandler = new ResultSetHandler<TaskProgress>() {
				public TaskProgress handle(ResultSet rs) throws SQLException {
					
					while(rs.next()) {
						TaskProgress progress=new TaskProgress();
						progress.setTaskId(rs.getInt("task_id"));
						progress.setPhaseId(rs.getInt("phase_id"));
						progress.setOperator(rs.getLong("operator"));
						progress.setPhase(rs.getInt("phase"));
						progress.setStatus(rs.getInt("status"));
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
}
