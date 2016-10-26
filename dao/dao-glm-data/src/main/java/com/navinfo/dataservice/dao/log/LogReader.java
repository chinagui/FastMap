package com.navinfo.dataservice.dao.log;

import java.lang.reflect.Field;
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
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;

/**
 * 日志查询类
 */
public class LogReader {

	private Connection conn;

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

	/**
	 * 查询对象状态：1新增，2删除，3修改
	 * 
	 * @param objPid
	 * @param objTable
	 * @param dbId
	 * @return
	 * @throws Exception
	 */
	public int getObjectState(int objPid, String objTable) throws Exception {

		String sql = "SELECT de.row_id,de.op_id,de.tb_nm,de.old,de.new,de.fd_lst,de.op_tp,de.tb_row_id,op.op_dt FROM LOG_DETAIL de,LOG_OPERATION op "
				+ "WHERE de.OP_ID=op.OP_ID AND de.OB_PID= :1 AND de.OB_NM= :2 ORDER BY op.OP_DT DESC";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			pstmt.setInt(1, objPid);
			pstmt.setString(2, objTable);
			resultSet = pstmt.executeQuery();
			Timestamp lastObjOpDate = null;
			int opStatus = 0;
			while (resultSet.next()) {
				if (objTable.equals(resultSet.getString("TB_NM")) && opStatus == 0) {
					lastObjOpDate = resultSet.getTimestamp("op_dt");
					opStatus = resultSet.getInt("op_tp");
					if (1 == opStatus || 2 == opStatus) {
						return opStatus;
					} else {
						if (isExistsAddHis(objPid, objTable, lastObjOpDate)) {
							return 1;
						} else {
							return opStatus;
						}
					}
				}

				if (!objTable.equals(resultSet.getString("TB_NM")) && opStatus == 0) {
					lastObjOpDate = resultSet.getTimestamp("op_dt");
					opStatus = 3;
					if (isExistsAddHis(objPid, objTable, lastObjOpDate)) {
						return 1;
					} else {
						return opStatus;
					}
				}
			}

			return opStatus;
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);

		}
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
				+ "WHERE de.OP_ID=op.OP_ID AND de.OB_PID= :1 AND de.OB_NM= :2 AND de.TB_NM=:3 AND de.FD_LST=:4 ";

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

		String sql = "SELECT de.row_id,de.op_id,de.tb_nm,de.old,de.new,de.fd_lst,de.op_tp,de.tb_row_id,op.op_dt FROM LOG_DETAIL de,LOG_OPERATION op "
				+ "WHERE de.OP_ID=op.OP_ID AND de.OB_PID= :1 AND (de.TB_NM not in ('IX_POI','IX_POI_PHOTO') or (de.TB_NM='IX_POI' AND instr(de.FD_LST,'POI_METO')=0)) ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			pstmt.setInt(1, objPid);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
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
	 * 查询对象是否存在履历
	 * 
	 * @param objPid
	 * @return
	 * @throws Exception
	 */
	public boolean isExistObjHis(int objPid) throws Exception {

		String sql = "SELECT de.row_id,de.op_id,de.tb_nm,de.old,de.new,de.fd_lst,de.op_tp,de.tb_row_id,op.op_dt FROM LOG_DETAIL de,LOG_OPERATION op "
				+ "WHERE de.OP_ID=op.OP_ID AND de.OB_PID= :1  ";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = this.conn.prepareStatement(sql);
			pstmt.setInt(1, objPid);
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
		StringBuilder sb = new StringBuilder();
		sb.append("WITH A AS\n");
		sb.append(" (SELECT T.OB_PID, MAX(P.OP_DT) DT FROM LOG_DETAIL T, LOG_DETAIL_GRID G, LOG_OPERATION P\n");
		sb.append("   WHERE T.OP_ID = P.OP_ID AND T.ROW_ID = G.LOG_ROW_ID AND T.OB_NM = '"+objName+"'\n");
		if(grids!=null&&grids.size()>0){
			sb.append("     AND G.GRID_ID IN ("+StringUtils.join(grids, ",")+") AND G.GRID_TYPE = 1\n");
		}
		if(StringUtils.isNotEmpty(date)){
			sb.append("     AND P.OP_DT > TO_DATE('"+date+"', 'yyyymmddhh24miss')\n");
		}
		sb.append("   GROUP BY OB_PID),\n");
		sb.append("B AS\n");
		sb.append(" (SELECT L.OB_PID, L.OP_TP FROM LOG_DETAIL L, LOG_OPERATION OP, A\n");
		sb.append("   WHERE L.OP_ID = OP.OP_ID AND L.OB_PID = A.OB_PID AND OP.OP_DT = A.DT AND L.TB_NM = ?),\n");
		sb.append("C AS\n");
		sb.append(" (SELECT A.OB_PID, 1 OP_TP FROM A\n");
		sb.append("   WHERE NOT EXISTS (SELECT 1 FROM B WHERE A.OB_PID = B.OB_PID)\n");
		sb.append("     AND EXISTS (SELECT 1 FROM LOG_DETAIL D\n");
		sb.append("           WHERE D.OB_PID = A.OB_PID AND D.TB_NM = ? AND D.OP_TP = 1)),\n");
		sb.append("D AS\n");
		sb.append(" (SELECT B.* FROM B UNION ALL SELECT C.* FROM C)\n");
		sb.append("SELECT * FROM D \n");
		sb.append("UNION ALL \n");
		sb.append("SELECT A.OB_PID, 3 OP_TP FROM A WHERE NOT EXISTS (SELECT 1 FROM D WHERE A.OB_PID = D.OB_PID)");
		return new QueryRunner().query(conn, sb.toString(), new ObjStatusHandler(),objName,mainTabName,mainTabName);
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
				"fm_regiondb_sp6_d_1", "fm_regiondb_sp6_d_1");
		int state = new LogReader(con).getObjectState(46332, "IX_POI");
		System.out.println(state);
	}
}
