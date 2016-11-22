package com.navinfo.dataservice.dao.glm.selector.poi.deep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName: IxPoiDeepStatusSelector 
* @author: zhangpengpeng 
* @date: 2016年11月16日
* @Desc: 深度信息状态表查询类
*/
public class IxPoiDeepStatusSelector extends AbstractSelector{
	private Connection conn;
	
	public IxPoiDeepStatusSelector(Connection conn) {
		super(conn);
		this.conn = conn;
	}
	
	/**
	 * 查询 作业员名下 已申请未提交的数据量
	 * @param userId
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public int queryHandlerCount(long userId, int type) throws Exception {
		int count = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(1) num");
		sb.append(" FROM poi_deep_status s");
		sb.append(" WHERE s.handler=:1");
		sb.append(" AND s.TYPE=:2");
		sb.append(" AND s.STATUS != 3");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setLong(1, userId);
			pstmt.setInt(2, type);
			
			resultSet = pstmt.executeQuery();
			
			if (resultSet.next()){
				count = resultSet.getInt("num");
			}
			
			return count;
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		
	}
	

	/**
	 * 根据subtask获取 可申请的数据rowIds
	 * @param subtask
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public List<String> getRowIds(Subtask subtask, int type) throws Exception {
		List<String> rowIds = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT s.row_id ");
		sb.append(" FROM IX_POI p,POI_DEEP_STATUS s ");
		sb.append(" WHERE sdo_within_distance(p.geometry, sdo_geometry(    :1  , 8307), 'mask=anyinteract') = 'TRUE'");
		sb.append(" AND s.TYPE=:2");
		sb.append(" AND s.handler is null");
		sb.append(" AND s.STATUS = 1");
		sb.append(" AND p.row_id = s.row_id");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, subtask.getGeometry());
			pstmt.setInt(2, type);
			
			resultSet = pstmt.executeQuery();
			int count = 0;
			//获取100条rowId
			while (resultSet.next()) {
				rowIds.add(resultSet.getString("row_id"));
				count++;
				if (count == 100){
					break;
				}
			}
			
			return rowIds;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 根据status,userid,type 获取可提交的数据rowIds
	 * @param subtask
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public List<String> getRowIdsForRelease(Subtask subtask ,int status, long userid, int type) throws Exception {
		List<String> rowIds = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT s.row_id ");
		sb.append(" FROM POI_DEEP_STATUS s ,ix_poi p ");
		sb.append(" WHERE sdo_within_distance(p.geometry, sdo_geometry(:1  , 8307), 'mask=anyinteract') = 'TRUE'");
		sb.append(" AND s.TYPE=:2");
		sb.append(" AND s.handler=:3");
		sb.append(" AND s.STATUS = :4");
		sb.append(" AND p.row_id = s.row_id");
		sb.append(" AND p.row_id not exist (select in.row_id from  ni_val_exception n AND in.row_id = s.row_id");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, subtask.getGeometry());
			pstmt.setInt(2, type);
			pstmt.setLong(3, userid);
			pstmt.setInt(4, status);
			
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				rowIds.add(resultSet.getString("row_id"));
			}
			
			return rowIds;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 深度信息 按条件查询poi
	 * 
	 * @param jsonReq
	 * @param subtask
	 * @param userId
	 * @return
	 * @throws Exception 
	 */
	public JSONObject loadDeepPoiByCondition(JSONObject jsonReq, Subtask subtask, long userId) throws Exception{
		
		int type = jsonReq.getInt("type");

		JSONArray deepCheckRules = getdeepCheckRules(type);
		
		int status = jsonReq.getInt("status");
		int pageNum  =jsonReq.getInt("pageNum");
		int pageSize = jsonReq.getInt("pageSize");
		
		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		int total = 0;
		int startRow = (pageNum - 1) * pageSize + 1;
		int endRow = pageNum * pageSize;
		String sql = "";
		StringBuilder bufferCondition = new StringBuilder();
		
		bufferCondition.append("select COUNT(1) OVER(PARTITION BY 1) total, ipn.poi_pid pid, ipn.name ");
		bufferCondition.append(" from ix_poi p,poi_deep_status s,ix_poi_name ipn");
		bufferCondition.append(" where ipn.name_class = 1 and ipn.name_type = 2 and (ipn.lang_code = 'CHI' or ipn.lang_code = 'CHT')");
		bufferCondition.append(" and p.row_id = s.row_id and p.pid = ipn.poi_pid");
		bufferCondition.append(" and sdo_within_distance(p.geometry, sdo_geometry('" + subtask.getGeometry() + "', 8307), 'mask=anyinteract') = 'TRUE'");
		bufferCondition.append(" and s.type = " + type + " and s.status = " + status + " and s.handler = " + userId );
		
		if (jsonReq.containsKey("poiName")){
			// poiName模糊查询
			bufferCondition.append(" and ipn.name like '%" + jsonReq.getString("poiName") + "%' ");
		} 
		if (jsonReq.containsKey("pid")) {
			// pid精确查询
			bufferCondition.append(" and p.pid = " + jsonReq.getInt("pid"));
		}
		sql = getSqlFromBufferCondition(bufferCondition, false);
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, endRow);
			pstmt.setInt(2, startRow);
			
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				if (total == 0) {
					total = resultSet.getInt("total");
				}
				JSONObject json = new JSONObject();
				json.put("pid", resultSet.getInt("pid"));
				json.put("name", resultSet.getString("name"));
				json.put("status", status);
				int pid = resultSet.getInt("pid");
				//获取state
				LogReader logRead = new LogReader(conn);
				int poiState = logRead.getObjectState(pid, "IX_POI");
				json.put("state", poiState);
				//获取checkErrorTotal
				int checkErrorTotal = getcheckErrorTotal(pid, deepCheckRules);
				json.put("checkErrorTotal", checkErrorTotal);
				//获取photoTotal
				int photoTotal = getPoiPhotoTotal(pid);
				json.put("photoTotal", photoTotal);

				array.add(json);
			}
			result.put("total", total);

			result.put("rows", array);

			return result;
			
		}  catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}
	
	public String getSqlFromBufferCondition(StringBuilder bufferCondition, boolean isLock) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(" SELECT * ");
		buffer.append(" FROM (SELECT c.*, ROWNUM rn ");
		buffer.append(" FROM ( " + bufferCondition.toString() + "");

		buffer.append(" ) c");
		buffer.append(" WHERE ROWNUM <= :1) ");
		buffer.append("  WHERE rn >= :2 ");
		if (isLock) {
			buffer.append(" for update nowait");
		}

		return buffer.toString();
	}
	
	/**
	 * 根据 type获取深度信息检查规则
	 * @param subCategory
	 * @return
	 * @throws Exception
	 */
	public JSONArray getdeepCheckRules(int type) throws Exception{
		
		String subCategory = new String();
		if (type == 1){
			subCategory = "IX_POI_DETAIL";
		} else if (type == 2){
			subCategory = "IX_POI_PARKING";
		}else if (type == 3) {
			subCategory = "IX_POI_CARRENTAL";
		}
		
		JSONArray deepCheckRules = new JSONArray();
		String sql = "select rule_code from ck_rule where SUBCATEGORY = :1";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		Connection conn = null;
		
		try {
			
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, subCategory);
			
			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				deepCheckRules.add(resultSet.getString("rule_code"));
			}
			
			return deepCheckRules;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
		
	}
	/**
	 * 根据条件获取检查规则号
	 * @param fields
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public JSONArray getCheckRulesByCondition(List<String> fields,List<String> values) throws Exception{
		
		StringBuilder sb = new StringBuilder();
		sb.append("select rule_code from ck_rule where rule_status = 'E' ");
		
		for(int i = 0; i < fields.size(); i++){
			String field=fields.get(i);
			String value=values.get(i);
			sb.append(" and "+field+" = '"+value+"'");
		}
		
		JSONArray CheckRules = new JSONArray();
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		Connection conn = null;
		
		try {
			
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			pstmt = conn.prepareStatement(sb.toString());	
			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				CheckRules.add(resultSet.getString("rule_code"));
			}
			
			return CheckRules;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
		
	}
	
	
	/**
	 * 根据pid查询poi的照片数
	 * @param pid
	 * @return
	 * @throws Exception
	 */
	public int getPoiPhotoTotal(int pid) throws Exception {
		int total = 0;
		String sql = "select count(1) total from ix_poi_photo where poi_pid=:1 and U_RECORD !=2";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, pid);
			
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				total = resultSet.getInt("total");
			}
			
			return total;
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}
	
	/**
	 * 获取poi的检查项错误个数
	 * @param pid
	 * @param deepCheckRules
	 * @return
	 * @throws Exception
	 */
	public int getcheckErrorTotal(int pid, JSONArray deepCheckRules) throws Exception {
		int total = 0;
		String sql = "select n.RULEID from ni_val_exception n,ck_result_object c where n.MD5_CODE=c.MD5_CODE and c.PID = :1";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, pid);
			
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				if (deepCheckRules.contains(resultSet.getString("RULEID"))) {
					total += 1;
				}
			}
			
			return total;
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);

		}
	}
	
}
