package com.navinfo.dataservice.dao.log;

import java.lang.reflect.Field;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;

/**
 * 日志查询类
 */
public class LogReader {
	
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private Connection conn;

	 @SuppressWarnings("serial")
	private final static List<String> filterFdList = new ArrayList<String>(){{ add("\"FIELD_STATE\""); add("\"OLD_NAME\"");
	add("\"OLD_ADDRESS\"");add("\"OLD_KIND\"");add("\"LOG\"");add("\"POI_MEMO\"");
	        add("\"DATA_VERSION\""); add("\"COLLECT_TIME\""); add("\"OLD_Y_GUIDE\"");add("\"OLD_X_GUIDE\"");
	    }};
	   
	    
	public LogReader(Connection conn) {
		this.conn = conn;
	}
	
	

	/**
	 * 根据条件查询
	 * 
	 * @param jsonCondition
	 * @return
	 */
	public List<LogOperation> queryLog(JSONObject jsonCondition) {
		return null;
	}


	public int getObjectState(int objPid, String objTable) throws Exception {
		int state = 0;
		Collection<Long> objPids = new ArrayList<Long>();
		objPids.add(Long.parseLong(String.valueOf(objPid)));
		Map<Integer,Collection<Long>> updatedObjs = getUpdatedObj(objTable,objTable,null,null,null,objPids);
		for(Map.Entry<Integer, Collection<Long>> entry:updatedObjs.entrySet()){
			state = (int)entry.getKey();
			break;
		}
		return state;
		
//		StringBuilder sb = new StringBuilder();
//		sb.append(" WITH A AS\n");
//		sb.append(" (SELECT LO.ACT_ID, V.OB_PID, V. DT FROM LOG_OPERATION LO,(SELECT T.OB_PID, MAX(P.OP_DT) DT FROM LOG_DETAIL T, LOG_OPERATION P\n ");
//		sb.append(" WHERE T.OP_ID = P.OP_ID  AND T.OB_NM ='"+objTable+"' AND T.OB_PID="+objPid+"\n" );
//		sb.append(" GROUP BY OB_PID) V WHERE LO.OP_DT = V.DT),\n");
//		sb.append(" B AS \n");
//		sb.append(" (SELECT L.OB_PID, L.OP_TP FROM LOG_DETAIL L, LOG_OPERATION OP, A \n");
//		sb.append(" WHERE L.OP_ID = OP.OP_ID AND L.OB_PID = A.OB_PID AND OP.ACT_ID = A.ACT_ID AND L.TB_NM ='"+ objTable+"'), \n");
//		sb.append(" C AS \n"); 
//		sb.append(" (SELECT A.OB_PID, 1 OP_TP FROM A \n");
//		sb.append(" WHERE NOT EXISTS (SELECT 1 FROM B WHERE A.OB_PID = B.OB_PID) \n");
//		sb.append(" AND EXISTS (SELECT 1 FROM LOG_DETAIL D \n");
//		sb.append(" WHERE D.OB_PID = A.OB_PID AND D.TB_NM = '"+objTable+"' AND D.OP_TP = 1)),\n ");   
//		sb.append(" D AS (SELECT B.* FROM B WHERE B.OP_TP!=3), \n");          
//		sb.append(" E AS (SELECT B.OB_PID,1 FROM B WHERE B.OP_TP=3 AND EXISTS (SELECT 1 FROM LOG_DETAIL D \n"); 
//		sb.append(" WHERE D.OB_PID = B.OB_PID AND D.TB_NM = '"+objTable+"' AND D.OP_TP = 1)), \n");
//		sb.append(" F AS \n");
//		sb.append(" (SELECT C.* FROM C UNION ALL SELECT D.* FROM D UNION ALL  SELECT E.* FROM E)\n ");
//		sb.append(" SELECT * FROM F \n");        
//		sb.append(" UNION ALL \n");  
//		sb.append(" SELECT A.OB_PID, 3 OP_TP FROM A WHERE NOT EXISTS (SELECT 1 FROM F WHERE A.OB_PID = F.OB_PID) ");  
//
//		PreparedStatement pstmt = null;
//		ResultSet resultSet = null;
//		
//		try {
//			pstmt = this.conn.prepareStatement(sb.toString());
//			resultSet = pstmt.executeQuery();
//			int opStatus = 0;
//			while (resultSet.next()) {
//				opStatus=resultSet.getInt("OP_TP");
//				}
//			return opStatus;
//		} catch (Exception e) {
//			log.error(e.getMessage(),e);
//			throw e;
//		} finally {
//			DBUtils.closeResultSet(resultSet);
//			DBUtils.closeStatement(pstmt);
//		}
	}
	
	/**
	 * 查询对象状态：1新增，2删除，3修改
	 * 
	 * @param objPid
	 * @param objTable
	 * @param dbId
	 * @return
	 * @throws Exception
	 */
	public Map<Long,Integer> getObjectState(Collection<Long> objPids, String objTable) throws Exception {
		
		if(objPids==null||objPids.size()==0){
			return null;
		}
		
		Map<Long,Integer> result = new HashMap<Long,Integer>();
		Map<Integer,Collection<Long>> updatedObjs = getUpdatedObj(objTable,objTable,null,null,null,objPids);
		for(Map.Entry<Integer, Collection<Long>> entry:updatedObjs.entrySet()){
			for(Long pid:entry.getValue()){
				result.put(pid, entry.getKey());
			}
		}
		return result;
//		try {
//			StringBuilder sb = new StringBuilder();
//			sb.append(" WITH A AS\n");
//			sb.append(" (SELECT LO.ACT_ID, V.OB_PID, V. DT FROM LOG_OPERATION LO,(SELECT T.OB_PID, MAX(P.OP_DT) DT FROM LOG_DETAIL T, LOG_OPERATION P\n ");
//			sb.append(" WHERE T.OP_ID = P.OP_ID  AND T.OB_NM ='"+objTable+"' AND T.OB_PID IN (select to_number(column_value) from table(clob_to_table(?))) \n" );
//			sb.append(" GROUP BY OB_PID) V WHERE LO.OP_DT = V.DT),\n");
//			sb.append(" B AS \n");
//			sb.append(" (SELECT L.OB_PID, L.OP_TP FROM LOG_DETAIL L, LOG_OPERATION OP, A \n");
//			sb.append(" WHERE L.OP_ID = OP.OP_ID AND L.OB_PID = A.OB_PID AND OP.ACT_ID = A.ACT_ID AND L.TB_NM ='"+ objTable+"'), \n");
//			sb.append(" C AS \n"); 
//			sb.append(" (SELECT A.OB_PID, 1 OP_TP FROM A \n");
//			sb.append(" WHERE NOT EXISTS (SELECT 1 FROM B WHERE A.OB_PID = B.OB_PID) \n");
//			sb.append(" AND EXISTS (SELECT 1 FROM LOG_DETAIL D \n");
//			sb.append(" WHERE D.OB_PID = A.OB_PID AND D.TB_NM = '"+objTable+"' AND D.OP_TP = 1)),\n ");   
//			sb.append(" D AS (SELECT B.* FROM B WHERE B.OP_TP!=3), \n");          
//			sb.append(" E AS (SELECT B.OB_PID,1 FROM B WHERE B.OP_TP=3 AND EXISTS (SELECT 1 FROM LOG_DETAIL D \n"); 
//			sb.append(" WHERE D.OB_PID = B.OB_PID AND D.TB_NM = '"+objTable+"' AND D.OP_TP = 1)), \n");
//			sb.append(" F AS \n");
//			sb.append(" (SELECT C.* FROM C UNION ALL SELECT D.* FROM D UNION ALL  SELECT E.* FROM E)\n ");
//			sb.append(" SELECT * FROM F \n");        
//			sb.append(" UNION ALL \n");  
//			sb.append(" SELECT A.OB_PID, 3 OP_TP FROM A WHERE NOT EXISTS (SELECT 1 FROM F WHERE A.OB_PID = F.OB_PID) ");  
//			
//			Clob clobPids=ConnectionUtil.createClob(conn);
//			clobPids.setString(1, StringUtils.join(objPids, ","));
//			
//			return new QueryRunner().query(conn, sb.toString(), new ResultSetHandler<Map<Long,Integer>>(){
//
//				@Override
//				public Map<Long, Integer> handle(ResultSet rs) throws SQLException {
//
//					Map<Long,Integer> opStatus = new HashMap<Long,Integer>();
//					while (rs.next()) {
//						opStatus.put(rs.getLong("OB_PID"), rs.getInt("OP_TP"));
//					}
//					return opStatus;
//				}
//				
//			},clobPids);
//		} catch (Exception e) {
//			log.error(e.getMessage(),e);
//			throw e;
//		}
	}

	/**
	 * 查询是否存在某次履历时间之前的新增履历
	 * 
	 * @param objPid
	 * @param objTable
	 * @param dbId
	 * @return
	 * @throws Exception
	 */
	public boolean isExistsAddHis(int objPid, String objTable, Timestamp lastObjOpDate) throws Exception {

		String sql = "SELECT de.row_id,de.op_id,de.tb_nm,de.old,de.new,de.fd_lst,de.op_tp,de.tb_row_id,op.op_dt FROM LOG_DETAIL de,LOG_OPERATION op "
				+ "WHERE de.OP_ID=op.OP_ID AND de.OB_PID= :1 AND de.TB_NM= :2 AND de.OP_TP=:3 AND op.op_dt<:4 ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			pstmt.setInt(1, objPid);
			pstmt.setString(2, objTable);
			pstmt.setInt(3, 1);
			pstmt.setTimestamp(4, lastObjOpDate);

			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				return true;
			}
			return false;
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}

	/**
	 * 查询对象某字段是否修改
	 * 
	 * @param objPid
	 * @param objTable
	 * @param tbNm
	 * @param Feild
	 * @return
	 * @throws Exception
	 */
	public boolean isUpdateforObjFeild(int objPid, String objTable, String tbNm, String Feild) throws Exception {

		String sql = "SELECT de.row_id,de.op_id,de.tb_nm,de.old,de.new,de.fd_lst,de.op_tp,de.tb_row_id,op.op_dt FROM LOG_DETAIL de,LOG_OPERATION op "
				+ "WHERE de.OP_ID=op.OP_ID AND de.OB_PID= :1 AND de.OB_NM= :2 AND de.TB_NM=:3 AND instr(de.FD_LST,:4)>0  ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			pstmt.setInt(1, objPid);
			pstmt.setString(2, objTable);
			pstmt.setString(3, tbNm);
			pstmt.setString(4, Feild);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				return true;
			} 
			return false;
			
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}

	/**
	 * 查询poi是否仅有照片或者备注的履历
	 * 
	 * @param objPid
	 * @param objTable
	 * @param tbNm
	 * @param Feild
	 * @return
	 * @throws Exception
	 */
	public boolean isOnlyPhotoAndMetoHis(int objPid) throws Exception {
		String tb_num=null;
		String fd_lst=null;
		String sql = "SELECT de.row_id,de.op_id,de.tb_nm,de.old,de.new,de.fd_lst,de.op_tp,de.tb_row_id,op.op_dt FROM LOG_DETAIL de,LOG_OPERATION op "
				+ "WHERE de.OP_ID=op.OP_ID AND de.OB_PID= :1 AND (de.TB_NM='IX_POI_PHOTO' OR (de.TB_NM='IX_POI' AND  instr(de.FD_LST,'POI_MEMO')>0)) "
				+ " AND  NOT EXISTS (SELECT 1 FROM LOG_DETAIL WHERE TB_NM NOT IN ('IX_POI_PHOTO','IX_POI') AND OB_PID=de.OB_PID)";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			pstmt.setInt(1, objPid);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				tb_num=resultSet.getString("tb_nm");
				fd_lst=resultSet.getString("fd_lst");
				boolean flag=true;
				if ("IX_POI".equals(tb_num)){
					  String[] arrFd = fd_lst.replace("[", "").replace("]", "").split(",");
					 for(int j= 0 ; j<arrFd.length;j++){ 
			            	if (!filterFdList.contains(arrFd[j])&& !"\"POI_MEMO\"".equals(arrFd[j]))
			            	{ flag=false;break;}
			            }  
			    }
				return flag;
	      }
			return false;
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}

	/**
	 * 查询对象是否存在履历
	 * 
	 * @param objPid
	 * @return
	 * @throws Exception
	 */
	public boolean isFreshVerified(int objPid) throws Exception {
		String tb_num=null;
		String fd_lst=null;

		String sql = "SELECT de.row_id,de.op_id,de.tb_nm,de.old,de.new,de.fd_lst,de.op_tp,de.tb_row_id,op.op_dt FROM LOG_DETAIL de,LOG_OPERATION op "
				+ "WHERE de.OP_ID=op.OP_ID AND de.OB_PID= :1";
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			pstmt.setInt(1, objPid);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				tb_num=resultSet.getString("tb_nm");
				fd_lst=resultSet.getString("fd_lst");
				if ("IX_POI".equals(tb_num)){
					  if(fd_lst.isEmpty()){return false;}
					  String[] arrFd = fd_lst.replace("[", "").replace("]", "").split(",");
					  for(int j= 0 ; j<arrFd.length;j++){
			            	if (!filterFdList.contains(arrFd[j]))
			            	{ return false;}
			            }  
				}else if(!("IX_POI_PHOTO".equals(tb_num)||"IX_POI_AUDIO".equals(tb_num)))
					return false;
				}
			return true;
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}

	/**
	 * 根据履历过滤掉子表单独删除的记录，只留最后一次和主表同时删除的子表记录
	 * 
	 * @param objPid
	 * @return
	 * @throws Exception
	 */
	public void filterValidRowId(IxPoi ixPoi) throws Exception {

		String sql = " WITH OP_ROW AS  (SELECT L.OP_ID  FROM LOG_DETAIL D, LOG_OPERATION L"
				+ "  WHERE L.OP_ID = D.OP_ID  AND D.OB_PID = :1   AND D.OB_NM = :2"
				+ "    AND D.TB_NM = :3   AND D.OP_TP = :4   AND ROWNUM < 2  ORDER BY L.OP_DT DESC)"
				+ " SELECT DE.ROW_ID  FROM LOG_DETAIL DE, OP_ROW O WHERE DE.OB_PID = :5"
				+ "   AND DE.OB_NM = :6  AND DE.TB_NM != :7 AND DE.OP_ID = O.OP_ID";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		ObjHandlerUtils objHandler = new ObjHandlerUtils();
		try {
			pstmt = this.conn.prepareStatement(sql);
			pstmt.setInt(1, ixPoi.getPid());
			pstmt.setString(2, "IX_POI");
			pstmt.setString(3, "IX_POI");
			pstmt.setInt(4, 2);
			pstmt.setInt(5, ixPoi.getPid());
			pstmt.setString(6, "IX_POI");
			pstmt.setString(7, "IX_POI");
			resultSet = pstmt.executeQuery();
			List<String> rowIdList = new ArrayList<String>();
			while (resultSet.next()) {
				rowIdList.add(resultSet.getString("ROW_ID"));
			}
			Class<?> clz = ixPoi.getClass();
			// 获取实体类的所有属性，返回Field数组
			Field[] fields = clz.getDeclaredFields();
			for (Field field : fields) {
				if ("java.util.List".equals(field.getType().getName())) {
					@SuppressWarnings("unchecked")
					List<IRow> fieldValueList = (List<IRow>) objHandler.getFieldValueByName(field.getName(), ixPoi);
					if (fieldValueList == null || fieldValueList.isEmpty()) {
						continue;
					} else {
						List<IRow> newValueList = new ArrayList<IRow>();
						for (IRow fieldValue : fieldValueList) {
							if (!rowIdList.contains(fieldValue.rowId())) {
								newValueList.add(fieldValue);
							}
						}
						objHandler.setFieldValueByName(field, ixPoi, newValueList);
					}
				}
			}
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}
	
	/**
	 * 
	 * @param objName
	 * @param mainTabName
	 * @param grid
	 * @param date
	 * @return key:status,value:pids
	 * @throws SQLException
	 */
	public  Map<Integer,Collection<Long>> getUpdatedObj(String objName,String mainTabName,String grid,String date)throws SQLException{
		Collection<String> grids = new HashSet<String>();
		grids.add(grid);
		return getUpdatedObj(objName,mainTabName,grids,date);
	}
	
	/**
	 * 
	 * @param objName
	 * @param grid
	 * @param date
	 * @return:key:status,value:pids
	 */
	public Map<Integer,Collection<Long>> getUpdatedObj(String objName,String mainTabName,Collection<String> grids,String date)throws SQLException{
		
		Map<Integer,Collection<Long>> updatedObjs = getUpdatedObj(objName,mainTabName,grids,date,null,null);
		return updatedObjs;
		
	}
	
	/*
	 * 精细化作业根据履历获取POI状态
	 * 作业季内新增又删除的数据，不查出
	 * 作业季内新增无删除的数据，状态为新增
	 * 作业季内无新增有删除的数据，状态为删除
	 * 作业季内无新增无删除的数据，状态为修改
	 * objName 对象名称
	 * mainTabName 主表名称
	 * grids 
	 * startDate 履历开始时间
	 * endDate 履历结束时间
	 * 实现内核
	 */
	public Map<Integer,Collection<Long>> getUpdatedObjSpecial(String objName,String mainTabName,Collection<String> grids,String startDate,String endDate)throws SQLException{
		
		StringBuilder sb = new StringBuilder();

		sb.append(" WITH A AS                                                                    ");
		sb.append("  (SELECT D.OB_PID, MAX(P.OP_DT) DT                                           ");
		sb.append("     FROM LOG_DETAIL D, LOG_DETAIL_GRID G, LOG_OPERATION P                    ");
		sb.append("    WHERE D.OP_ID = P.OP_ID                                                   ");
		sb.append("      AND D.ROW_ID = G.LOG_ROW_ID                                             ");
		sb.append("      AND D.OB_NM = ?                                                 ");
		if(grids!=null&&grids.size()>0){
			sb.append("     AND G.GRID_ID IN ("+StringUtils.join(grids, ",")+") AND G.GRID_TYPE = 1");
		}
		if(StringUtils.isNotEmpty(startDate)){
			sb.append("     AND P.OP_DT > TO_DATE('"+startDate+"', 'yyyymmddhh24miss')");
		}
		if(StringUtils.isNotEmpty(endDate)){
			sb.append("     AND P.OP_DT <= TO_DATE('"+endDate+"', 'yyyymmddhh24miss')");
		}
		sb.append("    GROUP BY OB_PID),                                                         ");
		sb.append(" B AS                                                                         ");
		sb.append("  (SELECT D.OB_PID, D.OP_TP                                                   ");
		sb.append("     FROM LOG_DETAIL D, LOG_DETAIL_GRID G, LOG_OPERATION P                    ");
		sb.append("    WHERE D.OP_ID = P.OP_ID                                                   ");
		sb.append("      AND D.ROW_ID = G.LOG_ROW_ID                                             ");
		if(grids!=null&&grids.size()>0){
			sb.append("     AND G.GRID_ID IN ("+StringUtils.join(grids, ",")+") AND G.GRID_TYPE = 1");
		}
		if(StringUtils.isNotEmpty(startDate)){
			sb.append("     AND P.OP_DT > TO_DATE('"+startDate+"', 'yyyymmddhh24miss')");
		}
		if(StringUtils.isNotEmpty(endDate)){
			sb.append("     AND P.OP_DT <= TO_DATE('"+endDate+"', 'yyyymmddhh24miss')");
		}
		sb.append("      AND D.OB_NM = ?                                                 ");
		sb.append("      AND D.TB_NM = ?                                                ),");
		sb.append(" C AS                                                                         ");
		sb.append("  (SELECT DISTINCT A.OB_PID, 3 OP_TP                                          ");
		sb.append("     FROM A                                                                   ");
		sb.append("    WHERE NOT EXISTS (SELECT 1 FROM B WHERE A.OB_PID = B.OB_PID)),            ");
		sb.append(" D AS                                                                         ");
		sb.append("  (SELECT DISTINCT B.OB_PID, 1 OP_TP                                          ");
		sb.append("     FROM B                                                                   ");
		sb.append("    WHERE B.OP_TP = 1                                                         ");
		sb.append("      AND NOT EXISTS (SELECT 1                                                ");
		sb.append("             FROM B BB                                                        ");
		sb.append("            WHERE BB.OB_PID = B.OB_PID                                        ");
		sb.append("              AND BB.OP_TP = 2)),                                              ");
		sb.append(" E AS                                                                         ");
		sb.append("  (SELECT DISTINCT B.OB_PID, 2 OP_TP                                          ");
		sb.append("     FROM B                                                                   ");
		sb.append("    WHERE B.OP_TP = 2                                                         ");
		sb.append("      AND NOT EXISTS (SELECT 1                                                ");
		sb.append("             FROM B BB                                                        ");
		sb.append("            WHERE BB.OB_PID = B.OB_PID                                        ");
		sb.append("              AND BB.OP_TP = 1)),                                              ");
		sb.append(" F AS                                                                         ");
		sb.append("  (SELECT DISTINCT B.OB_PID, 3 OP_TP                                          ");
		sb.append("     FROM B                                                                   ");
		sb.append("    WHERE B.OP_TP = 3                                                         ");
		sb.append("      AND NOT EXISTS (SELECT 1                                                ");
		sb.append("             FROM B BB                                                        ");
		sb.append("            WHERE BB.OB_PID = B.OB_PID                                        ");
		sb.append("              AND BB.OP_TP IN (1, 2)))                                         ");
		sb.append("                                                                              ");
		sb.append(" SELECT * FROM C                                                              ");
		sb.append(" UNION ALL SELECT * FROM D                                                    ");
		sb.append(" UNION ALL SELECT * FROM E                                                    ");
		sb.append(" UNION ALL SELECT * FROM F                                                    ");

		return new QueryRunner().query(conn, sb.toString(), new ObjStatusHandler(),objName,mainTabName,mainTabName);
	}
	
	/*
	 * 编辑/采集根据履历获取POI状态
	 * 作业季内新增又删除的数据，状态为删除
	 * 作业季内新增无删除的数据，状态为新增
	 * 作业季内无新增无删除的数据，状态为修改
	 * objName 对象名称
	 * mainTabName 主表名称
	 * grids 
	 * startDate 履历开始时间
	 * endDate 履历结束时间
	 * objPids 对象PID
	 * 实现内核
	 */
	public Map<Integer,Collection<Long>> getUpdatedObj(String objName,String mainTabName,Collection<String> grids,String startDate,String endDate,Collection<Long> objPids)throws SQLException{
		StringBuilder sb = new StringBuilder();

		boolean flg = false;
		Clob clobPids=ConnectionUtil.createClob(conn);
		
		sb.append(" WITH A AS                                                                    ");
		sb.append("  (SELECT /*+ORDERED*/D.OB_PID, MAX(P.OP_DT) DT                                           ");
		sb.append("     FROM LOG_DETAIL D, LOG_DETAIL_GRID G, LOG_OPERATION P                    ");
		sb.append("    WHERE D.OP_ID = P.OP_ID                                                   ");
		sb.append("      AND D.ROW_ID = G.LOG_ROW_ID                                             ");
		sb.append("      AND D.OB_NM = ?                                                 ");
		if(grids!=null&&grids.size()>0){
			sb.append("     AND G.GRID_ID IN ("+StringUtils.join(grids, ",")+") AND G.GRID_TYPE = 1");
		}
		if(StringUtils.isNotEmpty(startDate)){
			sb.append("     AND P.OP_DT > TO_DATE('"+startDate+"', 'yyyymmddhh24miss')");
		}
		if(StringUtils.isNotEmpty(endDate)){
			sb.append("     AND P.OP_DT <= TO_DATE('"+endDate+"', 'yyyymmddhh24miss')");
		}
		if(objPids!=null&&objPids.size()>0){
			sb.append("     AND D.OB_PID IN (select to_number(column_value) from table(clob_to_table(?)))");
			clobPids.setString(1, StringUtils.join(objPids, ","));
			flg = true;
		}
		sb.append("    GROUP BY OB_PID),                                                         ");
		sb.append(" B AS                                                                         ");
		sb.append("  (SELECT /*+ORDERED*/D.OB_PID, D.OP_TP                                                   ");
		sb.append("     FROM LOG_DETAIL D, LOG_DETAIL_GRID G, LOG_OPERATION P                    ");
		sb.append("    WHERE D.OP_ID = P.OP_ID                                                   ");
		sb.append("      AND D.ROW_ID = G.LOG_ROW_ID                                             ");
		if(grids!=null&&grids.size()>0){
			sb.append("     AND G.GRID_ID IN ("+StringUtils.join(grids, ",")+") AND G.GRID_TYPE = 1");
		}
		if(StringUtils.isNotEmpty(startDate)){
			sb.append("     AND P.OP_DT > TO_DATE('"+startDate+"', 'yyyymmddhh24miss')");
		}
		if(StringUtils.isNotEmpty(endDate)){
			sb.append("     AND P.OP_DT <= TO_DATE('"+endDate+"', 'yyyymmddhh24miss')");
		}
		if(objPids!=null&&objPids.size()>0){
			sb.append("     AND D.OB_PID IN (select to_number(column_value) from table(clob_to_table(?)))");
			clobPids.setString(1, StringUtils.join(objPids, ","));
			flg = true;
		}
		sb.append("      AND D.OB_NM = ?                                                 ");
		sb.append("      AND D.TB_NM = ?                                                ),");
		sb.append(" C AS                                                                         ");
		sb.append("  (SELECT DISTINCT A.OB_PID, 3 OP_TP                                          ");
		sb.append("     FROM A                                                                   ");
		sb.append("    WHERE NOT EXISTS (SELECT 1 FROM B WHERE A.OB_PID = B.OB_PID)),            ");
		sb.append(" D AS                                                                         ");
		sb.append("  (SELECT DISTINCT B.OB_PID, 1 OP_TP                                          ");
		sb.append("     FROM B                                                                   ");
		sb.append("    WHERE B.OP_TP = 1                                                         ");
		sb.append("      AND NOT EXISTS (SELECT 1                                                ");
		sb.append("             FROM B BB                                                        ");
		sb.append("            WHERE BB.OB_PID = B.OB_PID                                        ");
		sb.append("              AND BB.OP_TP = 2)),                                              ");
		sb.append(" E AS                                                                         ");
		sb.append("  (SELECT DISTINCT B.OB_PID, 2 OP_TP                                          ");
		sb.append("     FROM B                                                                   ");
		sb.append("    WHERE B.OP_TP = 2 ),                                              ");
		sb.append(" F AS                                                                         ");
		sb.append("  (SELECT DISTINCT B.OB_PID, 3 OP_TP                                          ");
		sb.append("     FROM B                                                                   ");
		sb.append("    WHERE B.OP_TP = 3                                                         ");
		sb.append("      AND NOT EXISTS (SELECT 1                                                ");
		sb.append("             FROM B BB                                                        ");
		sb.append("            WHERE BB.OB_PID = B.OB_PID                                        ");
		sb.append("              AND BB.OP_TP IN (1, 2)))                                         ");
		sb.append("                                                                              ");
		sb.append(" SELECT * FROM C                                                              ");
		sb.append(" UNION ALL SELECT * FROM D                                                    ");
		sb.append(" UNION ALL SELECT * FROM E                                                    ");
		sb.append(" UNION ALL SELECT * FROM F                                                    ");
		log.info(sb);
		if(flg){
			return new QueryRunner().query(conn, sb.toString(), new ObjStatusHandler(),objName,clobPids,clobPids,objName,mainTabName);
		}else{
			return new QueryRunner().query(conn, sb.toString(), new ObjStatusHandler(),objName,objName,mainTabName);
		}
		
	}
	

	public Map<Integer,Collection<Long>> getUpdatedObj(String objName,String mainTabName,Collection<String> grids,String startDate,String endDate)throws SQLException{
		
		Map<Integer,Collection<Long>> updatedObjs = getUpdatedObj(objName,mainTabName,grids,startDate,endDate,null);
		return updatedObjs;
		
//		StringBuilder sb = new StringBuilder();
//
//		sb.append(" WITH A AS                                                                    ");
//		sb.append("  (SELECT D.OB_PID, MAX(P.OP_DT) DT                                           ");
//		sb.append("     FROM LOG_DETAIL D, LOG_DETAIL_GRID G, LOG_OPERATION P                    ");
//		sb.append("    WHERE D.OP_ID = P.OP_ID                                                   ");
//		sb.append("      AND D.ROW_ID = G.LOG_ROW_ID                                             ");
//		sb.append("      AND D.OB_NM = ?                                                 ");
//		if(grids!=null&&grids.size()>0){
//			sb.append("     AND G.GRID_ID IN ("+StringUtils.join(grids, ",")+") AND G.GRID_TYPE = 1");
//		}
//		if(StringUtils.isNotEmpty(startDate)){
//			sb.append("     AND P.OP_DT > TO_DATE('"+startDate+"', 'yyyymmddhh24miss')");
//		}
//		if(StringUtils.isNotEmpty(endDate)){
//			sb.append("     AND P.OP_DT <= TO_DATE('"+endDate+"', 'yyyymmddhh24miss')");
//		}
//		sb.append("    GROUP BY OB_PID),                                                         ");
//		sb.append(" B AS                                                                         ");
//		sb.append("  (SELECT D.OB_PID, D.OP_TP                                                   ");
//		sb.append("     FROM LOG_DETAIL D, LOG_DETAIL_GRID G, LOG_OPERATION P                    ");
//		sb.append("    WHERE D.OP_ID = P.OP_ID                                                   ");
//		sb.append("      AND D.ROW_ID = G.LOG_ROW_ID                                             ");
//		if(grids!=null&&grids.size()>0){
//			sb.append("     AND G.GRID_ID IN ("+StringUtils.join(grids, ",")+") AND G.GRID_TYPE = 1");
//		}
//		if(StringUtils.isNotEmpty(startDate)){
//			sb.append("     AND P.OP_DT > TO_DATE('"+startDate+"', 'yyyymmddhh24miss')");
//		}
//		if(StringUtils.isNotEmpty(endDate)){
//			sb.append("     AND P.OP_DT <= TO_DATE('"+endDate+"', 'yyyymmddhh24miss')");
//		}
//		sb.append("      AND D.OB_NM = ?                                                 ");
//		sb.append("      AND D.TB_NM = ?                                                ),");
//		sb.append(" C AS                                                                         ");
//		sb.append("  (SELECT DISTINCT A.OB_PID, 3 OP_TP                                          ");
//		sb.append("     FROM A                                                                   ");
//		sb.append("    WHERE NOT EXISTS (SELECT 1 FROM B WHERE A.OB_PID = B.OB_PID)),            ");
//		sb.append(" D AS                                                                         ");
//		sb.append("  (SELECT DISTINCT B.OB_PID, 1 OP_TP                                          ");
//		sb.append("     FROM B                                                                   ");
//		sb.append("    WHERE B.OP_TP = 1                                                         ");
//		sb.append("      AND NOT EXISTS (SELECT 1                                                ");
//		sb.append("             FROM B BB                                                        ");
//		sb.append("            WHERE BB.OB_PID = B.OB_PID                                        ");
//		sb.append("              AND BB.OP_TP = 2)),                                              ");
//		sb.append(" E AS                                                                         ");
//		sb.append("  (SELECT DISTINCT B.OB_PID, 2 OP_TP                                          ");
//		sb.append("     FROM B                                                                   ");
//		sb.append("    WHERE B.OP_TP = 2                                                         ");
//		sb.append("      AND NOT EXISTS (SELECT 1                                                ");
//		sb.append("             FROM B BB                                                        ");
//		sb.append("            WHERE BB.OB_PID = B.OB_PID                                        ");
//		sb.append("              AND BB.OP_TP = 1)),                                              ");
//		sb.append(" F AS                                                                         ");
//		sb.append("  (SELECT DISTINCT B.OB_PID, 3 OP_TP                                          ");
//		sb.append("     FROM B                                                                   ");
//		sb.append("    WHERE B.OP_TP = 3                                                         ");
//		sb.append("      AND NOT EXISTS (SELECT 1                                                ");
//		sb.append("             FROM B BB                                                        ");
//		sb.append("            WHERE BB.OB_PID = B.OB_PID                                        ");
//		sb.append("              AND BB.OP_TP IN (1, 2)))                                         ");
//		sb.append("                                                                              ");
//		sb.append(" SELECT * FROM C                                                              ");
//		sb.append(" UNION ALL SELECT * FROM D                                                    ");
//		sb.append(" UNION ALL SELECT * FROM E                                                    ");
//		sb.append(" UNION ALL SELECT * FROM F                                                    ");
//
//		return new QueryRunner().query(conn, sb.toString(), new ObjStatusHandler(),objName,mainTabName,mainTabName);
	}
	/**
	 * 根据操作，查询某张表某条记录的新旧值变化
	 * @param opCmd
	 * @param tbTb
	 * @param rowId
	 * @return 
	 * @throws Exception
	 */
	public JSONObject getHisByOperate(String opCmd,String tbTb,String rowId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" select * from (SELECT ld.old,ld.new");
		sb.append(" FROM LOG_ACTION LA, LOG_OPERATION LO, LOG_DETAIL LD");
		sb.append(" WHERE LA.ACT_ID = LO.ACT_ID");
		sb.append(" AND LO.OP_ID = LD.OP_ID");
		sb.append(" AND LA.OP_CMD = ?");
		sb.append(" AND LD.TB_NM = ?");
		sb.append(" AND LD.TB_ROW_ID = ?");
		sb.append(" ORDER BY LO.OP_SEQ DESC) where rownum=1");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		JSONObject result = new JSONObject();
		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setString(1, opCmd);
			pstmt.setString(2, tbTb);
			pstmt.setString(3, rowId);
			
			resultSet = pstmt.executeQuery();
			if(resultSet.next()){
				result.put( "old", resultSet.getString("old"));
				result.put( "new", resultSet.getString("new"));
			}
			return result;
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}

	/**
	 * 获取时间段内，对象有变更的pid
	 * @param objName
	 * @param pids
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	public Collection<Long> getUpdatedObjByPids(String objName,Collection<Long> pids,String startDate,String endDate)throws SQLException{
		Collection<Long> updatedPids = new ArrayList<Long>();
		if(pids==null||pids.size()==0)return updatedPids;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT T.OB_PID FROM LOG_DETAIL T,LOG_OPERATION P WHERE T.OP_ID=P.OP_ID AND T.OB_NM=?\n");
		if(StringUtils.isNotEmpty(startDate)){
			sb.append("     AND P.OP_DT > TO_DATE('"+startDate+"', 'yyyymmddhh24miss')\n");
		}
		if(StringUtils.isNotEmpty(endDate)){
			sb.append("     AND P.OP_DT <= TO_DATE('"+endDate+"', 'yyyymmddhh24miss')\n");
		}
		Clob clobPids=null;
		if(pids.size()>1000){
			clobPids = conn.createClob();
			clobPids.setString(1, StringUtils.join(pids, ","));
			sb.append(" AND T.OB_PID IN (select to_number(column_value) from table(clob_to_table(?)))");
		}else{
			sb.append(" AND T.OB_PID IN (" + StringUtils.join(pids, ",") + ")");
		}
		
		ResultSetHandler<Collection<Long>> rsHandler = new ResultSetHandler<Collection<Long>>() {
			public Collection<Long> handle(ResultSet rs) throws SQLException {
				Collection<Long> result = new ArrayList<Long>();
				while (rs.next()) {
					result.add(rs.getLong("OB_PID"));
				}
				return result;
			}
	
		};
		if(clobPids==null){
			updatedPids = new QueryRunner().query(conn, sb.toString(), rsHandler,objName);
		}else{
			updatedPids = new QueryRunner().query(conn, sb.toString(), rsHandler,objName,clobPids);
		}
		return updatedPids;
		
	}

	/**
	 * 传入临时表，字段包括pid,start_date，end_date
	 * 获取时间段内，对象有变更的pid
	 * @param objName
	 * @param pids
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	public Collection<Long> getUpdatedObjByPids(String objName,String tempTable)throws SQLException{
		if(StringUtils.isEmpty(tempTable))return null;
		String sql = "SELECT DISTINCT T.OB_PID FROM LOG_DETAIL T,LOG_OPERATION P,"+tempTable+" S WHERE T.OP_ID=P.OP_ID AND T.OB_PID=S.PID AND P.OP_DT > S.START_DATE AND P.OP_DT <= S.END_DATE";
		return new QueryRunner().query(conn, sql, new ResultSetHandler<Collection<Long>>(){

			@Override
			public Collection<Long> handle(ResultSet rs) throws SQLException {
				List<Long> result = new ArrayList<Long>();
				while(rs.next()){
					result.add(rs.getLong(1));
				}
				return result;
			}
			
		},objName);
	}

	/**
	 * 传入临时表，字段包括fid,start_date，end_date
	 * 获取时间段内，对象有变更的pid
	 * @param objName
	 * @param pids
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	public Collection<String> getUpdatedPoiByTemp(String tempTable)throws SQLException{
		if(StringUtils.isEmpty(tempTable))return null;
		String sql = "SELECT DISTINCT IX.POI_NUM FROM LOG_DETAIL T,LOG_OPERATION P, IX_POI IX,"+tempTable+" S WHERE T.OP_ID=P.OP_ID AND T.OB_PID=IX.PID AND IX.POI_NUM=S.FID AND P.OP_DT > S.START_DATE AND P.OP_DT <= S.END_DATE";
		return new QueryRunner().query(conn, sql, new ResultSetHandler<Collection<String>>(){

			@Override
			public Collection<String> handle(ResultSet rs) throws SQLException {
				List<String> result = new ArrayList<String>();
				while(rs.next()){
					result.add(rs.getString(1));
				}
				return result;
			}
			
		});
	}

	
	/**
	 * 根据pid查询相应的履历
	 * @param objName
	 * @param mainTabName
	 * @param pid
	 * @return 
	 * @throws Exception
	 */
	public Map<Long,List<Map<String,Object>>> getLogByPid(String objName,Collection<Long> objPids) throws Exception {
		Map<Long,List<Map<String,Object>>> result = new HashMap<Long,List<Map<String,Object>>>();
		if(objPids==null||objPids.size()==0)return result;
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT LO.OP_ID,LO.OP_DT,LD.* FROM LOG_OPERATION LO,LOG_DETAIL LD WHERE LO.OP_ID = LD.OP_ID ");
		sb.append(" AND LD.OB_NM = '"+objName+"'");
		
		Clob clobPids=null;
		if(objPids.size()>1000){
			clobPids = ConnectionUtil.createClob(conn);
			clobPids.setString(1, StringUtils.join(objPids, ","));
			sb.append(" AND LD.OB_PID IN (select to_number(column_value) from table(clob_to_table(?)))");
		}else{
			sb.append(" AND LD.OB_PID IN (" + StringUtils.join(objPids, ",") + ")");
		}
		sb.append(" ORDER BY LD.OB_PID,LO.OP_DT ASC");
		
		log.info("根据pid查询相应的履历的sql语句:"+sb.toString());
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			if(clobPids!=null){
				pstmt.setClob(1, clobPids);
			}
			
			resultSet = pstmt.executeQuery();
			while(resultSet.next()){
				Map<String,Object> map = new HashMap<String,Object>();
				long pid = resultSet.getLong("OB_PID");
				int operation = resultSet.getInt("OP_TP");
				String date = DateUtils.dateToString(resultSet.getTimestamp("OP_DT"),DateUtils.DATE_COMPACTED_FORMAT);
				if(!result.containsKey(pid)){
					result.put(pid, new ArrayList<Map<String,Object>>());
				}
				map.put("operation", operation);
				map.put("date", date);
				result.get(pid).add(map);
			}
			return result;
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	
	/**
	 * 根据SubtaskId查询相应的poi数量
	 * @param objName
	 * @param mainTabName
	 * @param pid
	 * @return 
	 * @throws Exception
	 */
	public Map<Integer,Integer> getPoiNumBySubtaskId(String objName) throws Exception {
		Map<Integer,Integer> result = new HashMap<Integer,Integer>();
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT LD.GEO_PID,LA.STK_ID FROM LOG_ACTION LA,LOG_OPERATION LO,LOG_DETAIL LD ");
		sb.append(" WHERE LA.ACT_ID = LO.ACT_ID AND LO.OP_ID = LD.OP_ID ");
		sb.append(" AND LD.GEO_NM = '"+objName+"'");
		
		log.info("根据SubtaskId查询相应的poi数量的sql语句:"+sb.toString());
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			resultSet = pstmt.executeQuery();
			Map<Integer,Set<Long>> map = new HashMap<Integer,Set<Long>>();
			while(resultSet.next()){
				long pid = resultSet.getLong("GEO_PID");
				int subtaskId = resultSet.getInt("STK_ID");
				if(!map.containsKey(subtaskId)){
					map.put(subtaskId, new HashSet<Long>());
				}
				map.get(subtaskId).add(pid);
			}
			for(Entry<Integer, Set<Long>> entry : map.entrySet()){
				result.put(entry.getKey(), entry.getValue().size());
			}
			return result;
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	class ObjStatusHandler implements ResultSetHandler<Map<Integer,Collection<Long>>>{
		@Override
		public Map<Integer, Collection<Long>> handle(ResultSet rs) throws SQLException {
			Map<Integer, Collection<Long>> results = new HashMap<Integer,Collection<Long>>();
			Collection<Long> addPids = new HashSet<Long>();
			Collection<Long> delPids = new HashSet<Long>();
			Collection<Long> modPids = new HashSet<Long>();
			while(rs.next()){
				int status = rs.getInt("OP_TP");
				if(status==1){
					addPids.add(rs.getLong("OB_PID"));
				}else if(status==2){
					delPids.add(rs.getLong("OB_PID"));
				}else if(status==3){
					modPids.add(rs.getLong("OB_PID"));
				}
			}
			if(addPids.size()>0){
				results.put(1,addPids);
			}
			if(delPids.size()>0){
				results.put(2, delPids);
			}
			if(modPids.size()>0){
				results.put(3, modPids);
			}
			return results;
		}
		
	}

	public static void main(String[] args) throws Exception {
		Connection con = DriverManager.getConnection("jdbc:oracle:thin:@192.168.4.61:1521/orcl",
				"regiondb_bvt_d_1", "regiondb_bvt_d_1");
//		boolean flag = new LogReader(con).isUpdateforObjFeild(79887714, "IX_POI","IX_POI","LEVEL");
//		System.out.println(new Date());
//		String objName = "IX_POI";
//		String mainTabName = "IX_POI";
//		Collection<String> grids = null;
//		String startDate = "201707200000";
//		String endDate = "201707230000";
//		Map<Integer,Collection<Long>> map = new LogReader(con).getUpdatedObj(objName, mainTabName, grids, startDate);
//		System.out.println(new Date());
//		System.out.println(new Date());
//		String objTable = "IX_POI";
		int objPid = 55005398 ;
//		int status = new LogReader(con).getObjectState(objPid, objTable);
//		List<Long> pidList = new ArrayList<Long>();
//		pidList.add(505000108L);
//		pidList.add(408000133L);
//		Map<Long, List<Map<String, Object>>> list = new LogReader(con).getLogByPid(objTable, pidList);
//		System.out.println(new Date());
//		System.out.println(list.toString());
		//判断是否为充电站
//		Collection<Long> updatedObjByPids = new LogReader(con).getUpdatedObjByPids(objName, pidList, startDate, endDate);
//		System.out.println(updatedObjByPids);
		
//		Map<Integer,Collection<Long>> updatePids = new LogReader(con).getUpdatedObj(objName, mainTabName, null, "20170722150910", "20170723230000");
//		System.out.println(updatePids);
//		Map<Integer, Integer> poiNumBySubtaskId = new LogReader(con).getPoiNumBySubtaskId(objName);
//		System.out.println(poiNumBySubtaskId);
		LogReader l=new LogReader(con);
		System.out.println(l.isFreshVerified(objPid));
//		System.out.println(l.isOnlyPhotoAndMetoHis(objPid));
	}
}
