package com.navinfo.dataservice.engine.man.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.socket.TextMessage;

import com.navinfo.dataservice.api.man.model.TaskProgress;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.websocket.TaskOther2MediumWebSocketHandler;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;

public class TaskProgressOperation {
	private static Logger log = LoggerRepos.getLogger(TaskProgressOperation.class);
	
	public static int taskOther2MediumJob=1;
	
	public static int taskCreate=0;
	public static int taskWorking=1;
	public static int taskSuccess=2;
	public static int taskFail=3;
	
//	public static void create(Connection conn,TaskProgress bean) throws Exception{
//		try{
//			QueryRunner run = new QueryRunner();
//			String selectSql = "INSERT INTO TASK_PROGRESS P"
//					+ "  (TASK_ID, PHASE, STATUS, CREATE_DATE, PHASE_ID)"
//					+ "VALUES"
//					+ "  ("+bean.getTaskId()+","+bean.getPhase()+", 0, SYSDATE, "+bean.getPhaseId()+")" ;
//			run.update(conn, selectSql);
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			log.error(e.getMessage(), e);
//			throw new Exception("查询create TaskProgressOperation，原因为:"+e.getMessage(),e);
//		}
//	}
	
	/**
	 * @param conn
	 * @param bean
	 * @throws Exception  void
	 */
	public static void create(Connection conn,TaskProgress bean) throws Exception{
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			String createSql = "insert into TASK_PROGRESS ";			
			List<String> columns = new ArrayList<String>();
			List<String> placeHolder = new ArrayList<String>();
			List<Object> values = new ArrayList<Object>();
			
			columns.add(" PHASE_ID ");
			placeHolder.add("TASK_PROGRESS_SEQ.NEXTVAL");
				
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TASK_ID")){
				columns.add(" TASK_ID ");
				placeHolder.add("?");
				values.add(bean.getTaskId());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("PHASE")){
				columns.add(" PHASE ");
				placeHolder.add("?");
				values.add(bean.getPhase());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("STATUS")){
				columns.add(" STATUS ");
				placeHolder.add("?");
				values.add(bean.getStatus());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("OPERATOR")){
				columns.add(" OPERATOR ");
				placeHolder.add("?");
				values.add(bean.getOperator());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("PARAMETER")){
				columns.add(" PARAMETER ");
				placeHolder.add("?");
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, bean.getParameter().toString());
				values.add(clob);
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("CREATE_DATE")){
				columns.add(" CREATE_DATE ");
				placeHolder.add("?");
				values.add(bean.getCreatDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("START_DATE")){
				columns.add(" START_DATE ");
				placeHolder.add("?");
				values.add(bean.getStartDate());
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("END_DATE")){
				columns.add(" END_DATE ");
				placeHolder.add("?");
				values.add(bean.getEndDate());
			};
			
			if(!columns.isEmpty()){
				String columsStr = "(" + StringUtils.join(columns.toArray(),",") + ")";
				String placeHolderStr = "(" + StringUtils.join(placeHolder.toArray(),",") + ")";
				createSql = createSql + columsStr + " values " + placeHolderStr;
			}

			run.update(conn, 
					   createSql, 
					   values.toArray() );
		}catch(Exception e){
			log.error(e.getMessage(), e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("创建失败，原因为:"+e.getMessage(),e);
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
			TaskOther2MediumWebSocketHandler.getInstance().sendMessageToUser(taskProgress.getOperator().toString(),
					new TextMessage(msg.toString()));
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
			log.info(selectSql);
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
	 * @Title: updateTaskProgress
	 * @param conn
	 * @param bean
	 * @throws Exception
	 * 
	 */
	public static void updateTaskProgress(Connection conn,TaskProgress bean) throws Exception{
		try{
			String baseSql = "update TASK_PROGRESS set ";
			QueryRunner run = new QueryRunner();
			
			String updateSql="";
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("TASK_ID")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " TASK_ID= " + bean.getTaskId();
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("PHASE")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " PHASE= " + bean.getPhase();
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("STATUS")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " STATUS= " + bean.getStatus();
			};
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("OPERATOR")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				updateSql += " OPERATOR= " + bean.getOperator();
			};
			Clob clob = null;
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("PARAMETER")){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
				clob=ConnectionUtil.createClob(conn);
				clob.setString(1, bean.getParameter().toString());
				
				updateSql += " PARAMETER=  ?";
			};
			updateSql += " where PHASE_ID= " + bean.getPhaseId();
			
			log.info("updateSubtask sql:" + baseSql+updateSql);
			run.update(conn,baseSql+updateSql,clob);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new Exception("更新失败，原因为:"+e.getMessage(),e);
		}
	}
	
	/**
	 * 查询对应taskPrograss的parameter参数
	 * @param phaseId
	 * @return parameter
	 * @throws Exception 
	 */
	public static Clob query(Connection conn,int phaseId) throws Exception {
		QueryRunner run =null;
		try{
			run = new QueryRunner();
			String selectSql = "SELECT T.PARAMETER"
					+ "  FROM TASK_PROGRESS T"
					+ " WHERE T.PHASE_ID = "+phaseId;
			ResultSetHandler<Clob> rsHandler = new ResultSetHandler<Clob>() {
				public Clob handle(ResultSet rs) throws SQLException {
					while(rs.next()) {
						return rs.getClob("PARAMETER");
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
	 * Clob类型的数据转string
	 * @param Clob
	 * @return String
	 * @throws Exception 
	 */
	 public static String ClobToString(Clob clob) throws SQLException, IOException {

		 String reString = "";
		 Reader is = clob.getCharacterStream();// 得到流
		 BufferedReader br = new BufferedReader(is);
		 String s = br.readLine();
		 StringBuffer sb = new StringBuffer();
		 while (s != null) {// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
			 sb.append(s);
			 s = br.readLine();
		 }
		 reString = sb.toString();
		 return reString;
	}
}
