package com.navinfo.dataservice.dao.plus.editman;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.navicommons.database.QueryRunner;


/** 
 * @ClassName: PointaddressEditStatus
 * @author zl
 * @date 2017年10月11日
 * @Description: PointaddressEditStatus.java
 */
public class PointaddressEditStatus {
	
	public static int TO_BE_PRODUCE = 1;
	public static int PRODUCED = 2;

	private static final Logger logger = Logger.getLogger(PointaddressEditStatus.class);
	/**
	 * 
	 */
	public PointaddressEditStatus() {
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * @Title: forCollector
	 * @Description: pa上传时维护POINTADDRESS_edit_status表
	 * @param conn
	 * @param normalPois 非鲜度验证pa
	 * @param freshVerPois 鲜度验证pa
	 * @param subtaskId 
	 * @param taskId
	 * @param taskType
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年10月11日  
	 */
	public static void forCollector(Connection conn, Map<Long, String> normalPois, Set<Long> freshVerPois,
			int subtaskId, int taskId, int taskType) throws Exception {
		PreparedStatement stmt = null;
		try{
			
			//全部pids为空则无上传数据
			if((normalPois==null || normalPois.size()==0) 
					&& (freshVerPois==null || freshVerPois.size()==0)){
				return;
			}
			QueryRunner run = new QueryRunner();
			Set<Long> pids = new HashSet<Long>();
			if(freshVerPois!=null && freshVerPois.size()>0){
				pids.addAll(freshVerPois);
				Clob fPidsClob = ConnectionUtil.createClob(conn);
				fPidsClob.setString(1, StringUtils.join(freshVerPois,","));
				StringBuilder sb = new StringBuilder();
			
				sb.append("UPDATE POI_EDIT_STATUS P \n");
				sb.append("SET P.FRESH_VERIFIED=1, \n");
				sb.append("P.RAW_FIELDS=NULL, \n");
				sb.append("P.STATUS=2 \n");
				sb.append("   WHERE P.PID IN \n");
				sb.append("   (SELECT TO_NUMBER(COLUMN_VALUE) PID FROM TABLE(CLOB_TO_TABLE(?))) \n");
				sb.append("   AND P.STATUS IN (0, 3) \n");
				run.update(conn, sb.toString(), fPidsClob);
				
			}
			
			if(normalPois!=null && normalPois.size()>0){
				pids.addAll(normalPois.keySet());
				StringBuilder sb = new StringBuilder();
				sb.append("MERGE INTO POINTADDRESS_EDIT_STATUS P \n");
				sb.append("USING (SELECT TO_NUMBER(?) PID,? as rawFields \n");
				sb.append("         FROM dual ) T \n");
				sb.append("ON (P.PID = T.PID) \n");
				sb.append("WHEN MATCHED THEN \n");
				sb.append("  UPDATE \n");
				sb.append("     SET P.STATUS         = 1, \n");
				sb.append("         P.FRESH_VERIFIED = 0, \n");
				sb.append("         P.RAW_FIELDS     = T.rawFields, \n");
				sb.append("         WORK_TYPE        = 1 \n");
				sb.append("WHEN NOT MATCHED THEN \n");
				sb.append("  INSERT(P.PID, P.STATUS,P.RAW_FIELDS) VALUES (T.PID, 1,T.rawFields)");
				stmt = conn.prepareStatement(sb.toString());
				for(Entry<Long,String> entry:normalPois.entrySet()){
					String rawFields = "";
					if(entry.getValue() != null){
						rawFields = entry.getValue();
					}
					stmt.setLong(1, entry.getKey());
					stmt.setString(2, rawFields);
					stmt.addBatch();
				}
				
				stmt.executeBatch();
				stmt.clearBatch();
			}
			//write subtask part
			Clob pidsClob = ConnectionUtil.createClob(conn);
			logger.info("pids.size() :"+pids.size());
			pidsClob.setString(1, StringUtils.join(pids,","));
			if(subtaskId>0){
				if(taskType==4){//快线任务
					String sqlQ = "UPDATE POINTADDRESS_EDIT_STATUS SET IS_UPLOAD=1,UPLOAD_DATE=SYSDATE,QUICK_SUBTASK_ID=?,QUICK_TASK_ID=?,MEDIUM_SUBTASK_ID=0,MEDIUM_TASK_ID=0 WHERE PID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
					run.update(conn, sqlQ, subtaskId,taskId,pidsClob);
				}else if(taskType==1){//中线任务
					String sqlM = "UPDATE POINTADDRESS_EDIT_STATUS SET IS_UPLOAD=1,UPLOAD_DATE=SYSDATE,QUICK_SUBTASK_ID=0,QUICK_TASK_ID=0,MEDIUM_SUBTASK_ID=?,MEDIUM_TASK_ID=? WHERE PID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
					run.update(conn, sqlM, subtaskId,taskId,pidsClob);
				}
			}else{
				String sql4NoTask="UPDATE POINTADDRESS_EDIT_STATUS SET IS_UPLOAD=1,UPLOAD_DATE=SYSDATE,QUICK_SUBTASK_ID=0,QUICK_TASK_ID=0,MEDIUM_SUBTASK_ID=0,MEDIUM_TASK_ID=0 WHERE PID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
				run.update(conn, sql4NoTask, pidsClob);
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
			DbUtils.closeQuietly(stmt);
		}
		
	}
	
	
	/**
	 * 查询鲜度验证记录
	 * @param conn
	 * @param dbId
	 * @param uOrDfids
	 * @return
	 * @throws Exception 
	 */
	public static Map<Long,Map<String, Object>> getFreshData(Connection conn,Collection<Long> objPids) throws Exception {
		Map<Long,Map<String, Object>> dataList = new HashMap<Long,Map<String, Object>>();
		try{
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT * FROM POINTADDRESS_EDIT_STATUS WHERE FRESH_VERIFIED = 1 ");
			Clob clobPids=null;
			if(objPids.size()>1000){
				clobPids = ConnectionUtil.createClob(conn);
				clobPids.setString(1, StringUtils.join(objPids, ","));
				sb.append(" AND PID IN (select to_number(column_value) from table(clob_to_table(?)))");
			}else{
				sb.append(" AND PID IN (" + StringUtils.join(objPids, ",") + ")");
			}

			ResultSetHandler<Map<Long,Map<String, Object>>> rsHandler = new ResultSetHandler<Map<Long,Map<String, Object>>>() {
				public Map<Long,Map<String, Object>> handle(ResultSet rs) throws SQLException {
					Map<Long,Map<String, Object>> data = new HashMap<Long,Map<String, Object>>();
					while (rs.next()) {
						Map<String,Object> result = new HashMap<String,Object>();
						long pid = rs.getLong("PID");
						int status = rs.getInt("STATUS");
						String uploadDate = DateUtils.dateToString(rs.getTimestamp("UPLOAD_DATE"),DateUtils.DATE_COMPACTED_FORMAT);
						result.put("status", status);
						result.put("uploadDate", uploadDate);
						data.put(pid,result);
					}
					return data;
				}	
			};
			
			logger.info("getUploadDate sql:" + sb.toString());
			if(clobPids==null){
				dataList = new QueryRunner().query(conn, sb.toString(), rsHandler);
			}else{
				dataList = new QueryRunner().query(conn, sb.toString(), rsHandler,clobPids);
			}
			return dataList;
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("paWithOutSubtask");
		}
	}
	

}
