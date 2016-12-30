package com.navinfo.dataservice.dao.glm.selector.poi.deep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;

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
	 * 深度信息 按条件查询poi
	 * 
	 * @param jsonReq
	 * @param subtaskId
	 * @param userId
	 * @return
	 * @throws Exception 
	 */
	public JSONObject loadDeepPoiByCondition(JSONObject jsonReq, int subtaskId, long userId) throws Exception{
		
		String type = jsonReq.getString("type");

		JSONArray deepCheckRules = getdeepCheckRules(type);
		
		int status = jsonReq.getInt("status");
		
		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		int total = 0;

		StringBuilder bufferCondition = new StringBuilder();
		
		bufferCondition.append("select COUNT(1) OVER(PARTITION BY 1) total, ipn.poi_pid pid, ipn.name, p.kind_code ");
		bufferCondition.append(" from ix_poi p,poi_column_status s,ix_poi_name ipn,poi_column_workitem_conf c");
		bufferCondition.append(" where ipn.name_class = 1 and ipn.name_type = 2 and (ipn.lang_code = 'CHI' or ipn.lang_code = 'CHT')");
		bufferCondition.append(" and p.pid = s.pid and p.pid = ipn.poi_pid");
		bufferCondition.append(" and s.work_item_id=c.work_item_id");
		bufferCondition.append(" and s.task_id=" + subtaskId );
		bufferCondition.append(" and c.second_work_item = '" + type + "'" + " and s.second_work_status = " + status + " and s.handler = " + userId );
		
		if (jsonReq.containsKey("poiName")){
			// poiName模糊查询
			bufferCondition.append(" and ipn.name like '%" + jsonReq.getString("poiName") + "%' ");
		} 
		if (jsonReq.containsKey("pid")) {
			// pid精确查询
			bufferCondition.append(" and p.pid = " + jsonReq.getInt("pid"));
		}
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(bufferCondition.toString());
			
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				if (total == 0) {
					total = resultSet.getInt("total");
				}
				JSONObject json = new JSONObject();
				json.put("pid", resultSet.getInt("pid"));
				json.put("name", resultSet.getString("name"));
				json.put("kindCode", resultSet.getString("kind_code"));
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
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public JSONArray getdeepCheckRules(String type) throws Exception{
		
		String subCategory = new String();
		if ("deepDetail".equals(type)){
			subCategory = "IX_POI_DETAIL";
		} else if ("deepParking".equals(type)){
			subCategory = "IX_POI_PARKING";
		}else if ("deepCarrental".equals(type)) {
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
//	public JSONArray getCheckRulesByCondition(List<String> fields,List<String> values) throws Exception{
//		
//		StringBuilder sb = new StringBuilder();
//		sb.append("select rule_code from ck_rule where rule_status = 'E' ");
//		
//		for(int i = 0; i < fields.size(); i++){
//			String field=fields.get(i);
//			String value=values.get(i);
//			sb.append(" and "+field+" = '"+value+"'");
//		}
//		
//		JSONArray CheckRules = new JSONArray();
//		
//		PreparedStatement pstmt = null;
//
//		ResultSet resultSet = null;
//		
//		Connection conn = null;
//		
//		try {
//			
//			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
//			pstmt = conn.prepareStatement(sb.toString());	
//			resultSet = pstmt.executeQuery();
//			
//			while (resultSet.next()) {
//				CheckRules.add(resultSet.getString("rule_code"));
//			}
//			
//			return CheckRules;
//		} catch (Exception e) {
//			throw e;
//		} finally {
//			DbUtils.closeQuietly(resultSet);
//			DbUtils.closeQuietly(pstmt);
//			DbUtils.closeQuietly(conn);
//		}
//		
//	}
	
	
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
