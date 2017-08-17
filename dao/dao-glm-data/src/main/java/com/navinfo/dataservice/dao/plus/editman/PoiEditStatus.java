package com.navinfo.dataservice.dao.plus.editman;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;


/** 
 * @ClassName: PoiEditStatus
 * @author songdongyan
 * @date 2016年11月22日
 * @Description: PoiEditStatus.java
 */
public class PoiEditStatus {
	
	public static int TO_BE_PRODUCE = 1;
	public static int PRODUCED = 2;

	private static final Logger logger = Logger.getLogger(PoiEditStatus.class);
	/**
	 * 
	 */
	public PoiEditStatus() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 删选满足作业状态的pid
	 * @param conn
	 * @param pids
	 * @param status//作业状态。0: 未作业；1：待作业,2：已作业,3：已提交
	 * @return
	 * @throws Exception
	 */
	public static Collection<Long> pidFilterByEditStatus(Connection conn
				,Collection<Long> pids,int status) throws Exception{
		
		try{
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT T.PID FROM POI_EDIT_STATUS T WHERE T.STATUS = " + status);
			Clob clobPids=null;
			if(pids.isEmpty()){
				return pids;
			}
			if(pids.size()>1000){
				clobPids = conn.createClob();
				clobPids.setString(1, StringUtils.join(pids, ","));
				sb.append(" AND T.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
			}else{
				sb.append(" AND T.PID IN (" + StringUtils.join(pids, ",") + ")");
			}
				
			ResultSetHandler<Collection<Long>> rsHandler = new ResultSetHandler<Collection<Long>>() {
				public Collection<Long> handle(ResultSet rs) throws SQLException {
					Collection<Long> result = new ArrayList<Long>();
					while (rs.next()) {
						result.add(rs.getLong("PID"));
					}
					return result;
				}
		
			};
			Collection<Long> filteredPids = new ArrayList<Long>();
			if(clobPids==null){
				filteredPids = new QueryRunner().query(conn, sb.toString(), rsHandler);
			}else{
				filteredPids = new QueryRunner().query(conn, sb.toString(), rsHandler,clobPids);
			}
			return filteredPids;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(), e);
			throw new Exception("过滤pid失败:" + e.getMessage(), e);
		}
	}
	
//	public static void tagMultiSrcPoi(Connection conn,Map<Long,String> map) throws Exception{
//		try{
//			Set<Long> pids = map.keySet();
//			if(pids.isEmpty()){
//				return;
//			}
//			
//			//更新poi_edit_status表
//			int status = 1;//待作业
//			int isUpload = 1;
//			Date uploadDate = new Date();
//			int workType = 2;//多源
//			updatePoiEditStatus(conn,pids,status,isUpload,uploadDate,workType);
//			
////			//更新poi_edit_multisrc表
////			updatePoiEditMultiSrc(conn,map);
//				
//		}catch(Exception e){
//			DbUtils.rollbackAndCloseQuietly(conn);
//			logger.error(e.getMessage(),e);
//			throw new Exception("多源POI打标签失败");
//		}
//	}
	
	private static void updatePoiEditMultiSrc(Connection conn,Map<Long, String> map) throws Exception {
		try{
			if(map==null||map.isEmpty()){
				return;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("MERGE INTO POI_EDIT_MULTISRC A");
			sb.append(" USING (SELECT ? PID FROM DUAL) B");
			sb.append(" ON(A.PID=B.PID)");
			sb.append(" WHEN MATCHED THEN");
			sb.append(" UPDATE SET A.SOURCE_TYPE = ?,A.MAIN_TYPE = ?");
			sb.append(" WHEN NOT MATCHED THEN");
			sb.append(" INSERT (PID,SOURCE_TYPE,MAIN_TYPE) VALUES (?, ?,?)");
			
			
			Object[][] values = new Object[map.keySet().size()][];
			int i = 0;
			for(Map.Entry<Long, String> entry:map.entrySet()){
				String sourceType = entry.getValue();
				int mainType = 0;
				if(sourceType.equals("001000020000")){
					mainType = 1;
				}else if(sourceType.equals("001000030000")||sourceType.equals("001000030001")||sourceType.equals("001000030002")){
					mainType = 2;
				}else if(sourceType.equals("001000030003")||sourceType.equals("001000030004")){
					mainType = 3;
				}
				Object[] value = {entry.getKey(),sourceType,mainType,entry.getKey(),sourceType,mainType};
				values[i] = value;
				i++;
			}
			new QueryRunner().batch(conn, sb.toString(),values);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("多源POI打标签失败");
		}
		
	}

	
	/**
	 * poi_edit_status表中新增记录
	 * @param conn
	 * @param pids
	 * @param status
	 * @param uploadDate
	 * @throws Exception
	 */
	public static void insertPoiEditStatus(Connection conn, Set<Long> pids, int status) throws Exception {
		try{
			if(pids.isEmpty()){
				return;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO POI_EDIT_STATUS (PID,STATUS) VALUES (?,?)");

			Object[][] values = new Object[pids.size()][];
			int i = 0;
			for(long pid:pids){
				Object[] value = {pid,status};
				values[i] = value;
				i++;
			}
			new QueryRunner().batch(conn, sb.toString(),values);
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw new Exception("POI打标签失败");
		}
	}



	/**
	 * @param conn
	 * @param pidExistsInPoiEditStatus
	 * @throws Exception 
	 */
	public static void updatePoiEditStatus(Connection conn, Set<Long> pids
			,int status,int isUpload, Date uploadDate) throws Exception {
		try{
			if(pids.isEmpty()){
				return;
			}
			DateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE POI_EDIT_STATUS T SET STATUS="+status);
			sb.append(",IS_UPLOAD="+isUpload);
			sb.append(",UPLOAD_DATE=TO_DATE('" + format.format(uploadDate) + "','yyyy-MM-dd HH24:MI:ss')");
//			sb.append(",WORK_TYPE="+workType);
			Clob clobPids=null;
			if(pids.size()>1000){
				clobPids = conn.createClob();
				clobPids.setString(1, StringUtils.join(pids, ","));
				sb.append(" WHERE T.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
			}else{
				sb.append(" WHERE T.PID IN (" + StringUtils.join(pids, ",") + ")");
			}

			if(clobPids==null){
				new QueryRunner().update(conn, sb.toString());
			}else{
				new QueryRunner().update(conn, sb.toString(),clobPids);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("多源POI打标签失败");
		}
	}


	/**
	 * @Title: updateTaskIdByPid
	 * @Description: 更新poi_edit_status 表中的快线,中线任务标识
	 * @param conn
	 * @param pid
	 * @param quickTaskId
	 * @param centreTaskId
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年2月9日 下午7:02:25 
	 */
	public static void updateTaskIdByPid(Connection conn, Long pid ,Integer quickTaskId,Integer centreTaskId) throws Exception {
		try{
			
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE POI_EDIT_STATUS T SET T.quick_task_id="+quickTaskId);
			sb.append(",T.centre_task_id="+centreTaskId);
			
				sb.append(" WHERE T.PID = "+pid);

				logger.info("updateTaskIdByPid sql : "+sb.toString());
				new QueryRunner().update(conn, sb.toString());
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			logger.error("采集成果自动批任务标识失败");
			//throw new Exception("采集成果自动批任务标识失败");
		}
	}
	
//	public static void main(String[] args) throws Exception{
//
//		System.out.println("ok");
//	}

	/**
	 * 新增POI对象在poi_edit_status表中新增一条初始状态的记录
	 * @param conn
	 * @param result
	 * @throws Exception 
	 */
	public static void insertPoiEditStatus(Connection conn, OperationResult result) throws Exception {
		// TODO Auto-generated method stub
		Set<Long> pids = new HashSet<Long>();
		for(Entry<Long, BasicObj> entry:result.getObjsMapByType(ObjectName.IX_POI).entrySet()){
			if(entry.getValue().opType().equals(OperationType.INSERT)){
				pids.add(entry.getValue().objPid());
			}
		}
		int status = 1;//默认为初始化状态
		//插入poi_edit_status表记录
		if(pids!=null&&!pids.isEmpty()){
			insertPoiEditStatus(conn,pids,status);
		}
	}
	

	/**
	 * 鲜度验证数据直接进入已作业
	 * @param conn
	 * @param pids：只包含鲜度验证的POI
	 * @throws Exception
	 */
	public static void freshVerifiedPoi(Connection conn,Map<Long,String> pids)throws Exception{
		PreparedStatement stmt = null;
		try{
			if(pids==null||pids.size()==0){
				return;
			}
			String sql = "UPDATE POI_EDIT_STATUS SET STATUS=2,FRESH_VERIFIED=1,RAW_FIELDS=? WHERE PID = ? AND STATUS IN (0,3)";
			stmt = conn.prepareStatement(sql);
			for(Entry<Long,String> entry:pids.entrySet()){
				stmt.setString(1, entry.getValue());
				stmt.setLong(2, entry.getKey());
				stmt.addBatch();
			}
			stmt.executeBatch();
			stmt.clearBatch();
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}finally{
			DbUtils.closeQuietly(stmt);
		}
	}

	/**
	 * 
	 * @param conn
	 * @param pids：只包含鲜度验证的POI
	 * @throws Exception
	 */
	public static void updateStatus(Connection conn,Collection<Long> pids,int status)throws Exception{
		try{
			if(pids==null||pids.size()==0){
				return;
			}
			String sql = "UPDATE POI_EDIT_STATUS SET STATUS=? WHERE PID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
			Clob pidsClob = ConnectionUtil.createClob(conn);
			pidsClob.setString(1, StringUtils.join(pids, ","));
			QueryRunner run = new QueryRunner();
			run.update(conn, sql, status,pidsClob);
		}catch(Exception e){
			logger.error(e.getMessage(),e);
			throw e;
		}
	}
	/**
	 * poi操作修改poi状态为待作业
	 * 
	 * @param row
	 * @throws Exception
	 */
	public static void upatePoiStatusForAndroid(Connection conn,  int freshFlag, String rawFields,int status,long pid)
			throws Exception {
		StringBuilder sb = new StringBuilder(" MERGE INTO poi_edit_status T1 ");
		sb.append(" USING (SELECT "+status+" as b," + freshFlag + " as c,");
		if(rawFields != null && StringUtils.isNotEmpty(rawFields)){
			sb.append("'" + rawFields+ "' as d,");
		}else{
			sb.append("null as d,");
		}
		
		sb.append( "sysdate as e,"+ pid + " as f " + "  FROM dual) T2 ");
		sb.append(" ON ( T1.pid=T2.f) ");
		sb.append(" WHEN MATCHED THEN ");
		sb.append(
				" UPDATE SET T1.status = T2.b,T1.fresh_verified= T2.c,T1.is_upload = 1,T1.raw_fields = T2.d,T1.upload_date = T2.e ");
		sb.append(" WHEN NOT MATCHED THEN ");
		sb.append(
				//zl 2016.12.08 新增时为 commit_his_status 字段赋默认值 0 
				" INSERT (T1.status,T1.fresh_verified,T1.is_upload,T1.raw_fields,T1.upload_date,T1.pid,commit_his_status) VALUES(T2.b,T2.c,1,T2.d,T2.e,T2.f,0)");
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			System.out.println("sb.toString(): "+sb.toString());
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;

		} finally {
			DBUtils.closeStatement(pstmt);
		}
	}
	
	/**
	 * 常规数据或众包数据未作业完成，即处于"待作业"或"待提交"状态且存在常规子任务或众包子任务号
	 * @param conn 
	 * @param dbId 
	 * @param fids
	 * @param workKind //1常规，2众包
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, Integer> poiUnderSubtask(Connection conn, int dbId, List<String> fids, int workKind) throws Exception {
		try{
			Map<String, Integer> result = new HashMap<String, Integer>();
			if(fids.isEmpty()){
				return result;
			}
			
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			List<Integer> statusList = new ArrayList<Integer>();
			statusList.add(1);
			final List<Integer> normalSubtaskList = manApi.getSubtaskIdListByDbId(dbId,statusList,workKind);

			StringBuilder sb = new StringBuilder();
			sb.append(" SELECT P.POI_NUM,E.QUICK_SUBTASK_ID,E.MEDIUM_SUBTASK_ID ");
			sb.append("   FROM POI_EDIT_STATUS E, IX_POI P     ");
			sb.append("  WHERE E.STATUS IN (1, 2)              ");
			sb.append("    AND P.PID = E.PID                   ");

			Clob clobSubtaskIds = conn.createClob();
			clobSubtaskIds.setString(1, StringUtils.join(normalSubtaskList, ","));
			sb.append("    AND (E.QUICK_SUBTASK_ID IN (select to_number(column_value) from table(clob_to_table(?))) OR E.MEDIUM_SUBTASK_ID IN (select to_number(column_value) from table(clob_to_table(?)))) ");
			
			Clob clobPids = conn.createClob();
			clobPids.setString(1, StringUtils.join(fids, ","));
			sb.append(" AND P.POI_NUM IN (select (column_value) from table(clob_to_table(?)))");
			logger.info("poiUnderSubtask sql:" + sb.toString());
			Object[] pra = new Object[3];
			pra[0] = clobSubtaskIds;
			pra[1] = clobSubtaskIds;
			pra[2] = clobPids;

			ResultSetHandler<Map<String,Integer>> rsHandler = new ResultSetHandler<Map<String,Integer>>() {
				public Map<String, Integer> handle(ResultSet rs) throws SQLException {
					Map<String, Integer> result = new HashMap<String, Integer>();
					if (rs.next()) {
						String fid = rs.getString("POI_NUM");
						int quickSubtaskId = rs.getInt("QUICK_SUBTASK_ID");
						int mediumSubtaskId = rs.getInt("MEDIUM_SUBTASK_ID");
						if(normalSubtaskList.contains(quickSubtaskId)){
							result.put(fid, quickSubtaskId);
						}else{
							result.put(fid, mediumSubtaskId);
						}
					}
					return result;
				}	
			};
			
			QueryRunner run = new QueryRunner();
			
			return run.query(conn,sb.toString(),pra,rsHandler);
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("多源POI打标签失败");
		}
	}


	/**
	 * @param conn
	 * @param QuickSubtaskIdMap
	 * @param MediumSubtaskIdMap 
	 * @throws Exception 
	 */
	public static void tagMultiSrcPoi(Connection conn, Map<Long, Integer> quickSubtaskIdMap, Map<Long, Integer> mediumSubtaskIdMap,Date uploadDate) throws Exception {
		try{
			if(!quickSubtaskIdMap.isEmpty()){
				updatePoiEditStatusMultiSrc(conn,uploadDate,quickSubtaskIdMap,1);
			}
			if(!mediumSubtaskIdMap.isEmpty()){
				updatePoiEditStatusMultiSrc(conn,uploadDate,mediumSubtaskIdMap,2);
			}
			
				
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("多源POI打标签失败");
		}
		
	}
	
	/**
	 * @param conn
	 * @param uploadDate2 
	 * @param map<pid,subtaskId>
	 * @param type:1快线；2中线
	 * 入库时该POI数据为无任务数据，多源数据入日库并生成履历，但不标识多源子任务！
	 * @throws Exception 
	 */
	private static void updatePoiEditStatusMultiSrc(Connection conn,Date uploadDate,Map<Long, Integer> map, int type) throws Exception {
		try{
			if(map.isEmpty()){
				return;
			}
			//更新poi_edit_status表

			DateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE POI_EDIT_STATUS T SET ");
			if(type==1){
//				sb.append("QUICK_SUBTASK_ID=? WHERE PID = ? AND (QUICK_SUBTASK_ID<>0 OR MEDIUM_SUBTASK_ID <> 0 OR EXISTS (SELECT 1 FROM IX_POI I WHERE I.U_RECORD = 1 AND I.PID = T.PID))");
				sb.append("QUICK_SUBTASK_ID=? WHERE PID = ?");
			}else if(type==2){
//				sb.append("MEDIUM_SUBTASK_ID=? WHERE PID = ? AND (QUICK_SUBTASK_ID<>0 OR MEDIUM_SUBTASK_ID <> 0 OR EXISTS (SELECT 1 FROM IX_POI I WHERE I.U_RECORD = 1 AND I.PID = T.PID))");
				sb.append("MEDIUM_SUBTASK_ID=? WHERE PID = ? ");
			}
			
			Object[][] inParam = new Object[map.size()][];
			int i = 0;
			for(Map.Entry<Long, Integer> entry:map.entrySet()){
				Object[] temp = new Object[2];
				temp[0] = entry.getValue();
				temp[1] = entry.getKey();
				inParam[i] = temp;
				i++;
			}

			new QueryRunner().batch(conn, sb.toString(),inParam);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("多源POI打标签失败");
		}
	}
	

	/**
	 * @param conn
	 * @param dbId
	 * @param uOrDfids
	 * @return
	 * @throws Exception 
	 */
	public static Set<Long> poiWithOutSubtask(Connection conn, int dbId, List<String> uOrDfids) throws Exception {
		try{
			Set<Long> result = new HashSet<Long>();
			if(uOrDfids.isEmpty()){
				return result;
			}

			StringBuilder sb = new StringBuilder();
			sb.append(" SELECT E.PID");
			sb.append("   FROM POI_EDIT_STATUS E, IX_POI P     ");
			sb.append("  WHERE E.STATUS IN (1, 2)              ");
			sb.append("    AND P.PID = E.PID                   ");
			sb.append("    AND (E.QUICK_SUBTASK_ID=0 OR E.MEDIUM_SUBTASK_ID=0) ");
			
			Clob clobPids = conn.createClob();
			clobPids.setString(1, StringUtils.join(uOrDfids, ","));
			sb.append(" AND P.POI_NUM IN (select (column_value) from table(clob_to_table(?)))");
			
			Object[] pra = new Object[1];
			pra[0] = clobPids;

			ResultSetHandler<Set<Long>> rsHandler = new ResultSetHandler<Set<Long>>() {
				public Set<Long> handle(ResultSet rs) throws SQLException {
					Set<Long> result = new HashSet<Long>();
					while (rs.next()) {
						result.add(rs.getLong("PID"));

					}
					return result;
				}	
			};
			
			QueryRunner run = new QueryRunner();
			logger.info("poiWithOutSubtask sql:" + sb.toString());
			return run.query(conn,sb.toString(),pra,rsHandler);
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("poiWithOutSubtask");
		}
	}

	/**
	 * @Title: forCollector
	 * @Description: poi上传时维护poi_edit_status表
	 * @param conn
	 * @param normalPois 非鲜度验证poi
	 * @param freshVerPois 鲜度验证poi
	 * @param subtaskId 
	 * @param taskId
	 * @param taskType
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月6日 下午3:15:14 
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
				sb.append("SET (P.FRESH_VERIFIED, \n");
				sb.append("P.RAW_FIELDS, \n");
				sb.append("P.STATUS)=(SELECT 1, \n");
				sb.append(" NULL, \n");
				sb.append(" CASE \n");
				sb.append(" WHEN EXISTS \n");
				sb.append("  (SELECT 1 \n");                     
				sb.append("  FROM LOG_DETAIL L \n");                     
				sb.append("  WHERE L.OB_PID =E.PID \n");    
				sb.append("  AND (TB_NM = 'IX_POI_PHOTO' \n");   
				sb.append("  OR (L.TB_NM = 'IX_POI' AND \n"); 
				sb.append("  INSTR(L.FD_LST, 'POI_MEMO') > 0))) THEN \n"); 
				sb.append("  1 \n");
				sb.append("  ELSE \n");
				sb.append("   2 \n");
				sb.append("  END POI_STATUS FROM POI_EDIT_STATUS E WHERE E.PID=P.PID) \n");
				sb.append("   WHERE P.PID IN \n");
				sb.append("   (SELECT TO_NUMBER(COLUMN_VALUE) PID FROM TABLE(CLOB_TO_TABLE(?))) \n");
				sb.append("   AND P.STATUS IN (0, 3) \n");
				
				run.update(conn, sb.toString(), fPidsClob);
				
			}
			
			if(normalPois!=null && normalPois.size()>0){
				pids.addAll(normalPois.keySet());
				StringBuilder sb = new StringBuilder();
				sb.append("MERGE INTO POI_EDIT_STATUS P \n");
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
					String sqlQ = "UPDATE POI_EDIT_STATUS SET IS_UPLOAD=1,UPLOAD_DATE=SYSDATE,QUICK_SUBTASK_ID=?,QUICK_TASK_ID=?,MEDIUM_SUBTASK_ID=0,MEDIUM_TASK_ID=0 WHERE PID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
					run.update(conn, sqlQ, subtaskId,taskId,pidsClob);
				}else if(taskType==1){//中线任务
					String sqlM = "UPDATE POI_EDIT_STATUS SET IS_UPLOAD=1,UPLOAD_DATE=SYSDATE,QUICK_SUBTASK_ID=0,QUICK_TASK_ID=0,MEDIUM_SUBTASK_ID=?,MEDIUM_TASK_ID=? WHERE PID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
					run.update(conn, sqlM, subtaskId,taskId,pidsClob);
				}
			}else{
				String sql4NoTask="UPDATE POI_EDIT_STATUS SET IS_UPLOAD=1,UPLOAD_DATE=SYSDATE,QUICK_SUBTASK_ID=0,QUICK_TASK_ID=0,MEDIUM_SUBTASK_ID=0,MEDIUM_TASK_ID=0 WHERE PID IN (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
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
			sb.append("SELECT * FROM POI_EDIT_STATUS WHERE FRESH_VERIFIED = 1 ");
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
			throw new Exception("poiWithOutSubtask");
		}
	}
	
//	public static void main(String[] args) throws Exception {
//		Connection con = DriverManager.getConnection("jdbc:oracle:thin:@192.168.4.61:1521/orcl",
//				"fm_regiondb_trunk_d_1", "fm_regiondb_trunk_d_1");
//		List<Long> pidList = new ArrayList<Long>();
//		pidList.add(407000002L);
//		pidList.add(502000007L);
//		Map<Long, Map<String, Object>> list = getFreshData(con, pidList);
//		System.out.println(list.toString());
//	}
}
