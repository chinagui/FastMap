package com.navinfo.dataservice.dao.glm.selector.poi.deep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.log.LogReader;

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

		List<String> deepCheckRules = getDeepCheckRules(type);
		 
		int status = jsonReq.getInt("status");
		
		JSONObject result = new JSONObject();
		JSONArray array = new JSONArray();
		int total = 0;

		StringBuilder bufferCondition = new StringBuilder();
		
		bufferCondition.append("select COUNT(1) OVER(PARTITION BY 1) total, ipn.poi_pid pid, ipn.name, p.kind_code,p.poi_num,p.poi_memo ");
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
			
			List<Long> pidList = new ArrayList<>();
			
			while (resultSet.next()) {
				if (total == 0) {
					total = resultSet.getInt("total");
				}
				JSONObject json = new JSONObject();
				json.put("pid", resultSet.getInt("pid"));
				json.put("name", resultSet.getString("name"));
				json.put("kindCode", resultSet.getString("kind_code"));
				json.put("fid",  StringUtils.isBlank(resultSet.getString("poi_num"))?"":resultSet.getString("poi_num"));
				json.put("memo", StringUtils.isBlank(resultSet.getString("poi_memo"))?"":resultSet.getString("poi_memo"));
				json.put("status", status);
				int pid = resultSet.getInt("pid");
				/*//获取state
				LogReader logRead = new LogReader(conn);
				int poiState = logRead.getObjectState(pid, "IX_POI");
				
				json.put("state", poiState);
				//获取checkErrorTotal
				int checkErrorTotal = getcheckErrorTotal(pid, deepCheckRules);
				
				json.put("checkErrorTotal", checkErrorTotal);
				//获取photoTotal
				int photoTotal = getPoiPhotoTotal(pid);
				
				json.put("photoTotal", photoTotal);
				
				*/
				
				pidList.add((long)pid);
				array.add(json);
			}
			result.put("total", total);
			
			LogReader logRead = new LogReader(conn);
			Map<Long,Integer> stateResult  = logRead.getObjectState(pidList,"IX_POI");
			Map<Integer,Integer> checkErrorResult  = getCheckErrorTotal(deepCheckRules, pidList);
			Map<Integer,Integer> photoTotalResult  = getPoiPhotoTotal(pidList);
			for(int i=0;i<array.size();i++){
			    JSONObject json = array.getJSONObject(i);  
			    Integer pid = (Integer)json.get("pid");
			    json.put("state",(stateResult.get(pid.longValue())==null?0:stateResult.get(pid.longValue())));
			    json.put("checkErrorTotal",checkErrorResult==null||checkErrorResult.isEmpty()?0:(checkErrorResult.get(pid)==null?0:checkErrorResult.get(pid)));
			    json.put("photoTotal", photoTotalResult==null||photoTotalResult.isEmpty()?0:(photoTotalResult.get(pid)==null?0:photoTotalResult.get(pid)));
		    }
			result.put("rows", array);
			
			System.out.println(array);
			
			return result;
			
		}  catch (Exception e) {

			throw e;

		} finally {

			DbUtils.closeQuietly(resultSet);

			DbUtils.closeQuietly(pstmt);

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
	public List<String> getDeepCheckRules(String type) throws Exception{
				
		List<String> deepCheckRules =  new ArrayList<>();
		String sql = "select work_item_id from POI_COLUMN_WORKITEM_CONF where first_work_item='poi_deep' and second_work_item=:1 ";
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		try {
			
			//conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, type);
			
			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				deepCheckRules.add(resultSet.getString("work_item_id"));
			}
			
			return deepCheckRules;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
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
	public Map<Integer,Integer> getPoiPhotoTotal(List<Long> pidList) throws Exception {
		if(pidList==null||pidList.isEmpty()){
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("select poi_pid,count(1) total from ix_poi_photo where  U_RECORD !=2 and poi_pid IN (");
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			
			String pid_temp = "";
			for (long pid : pidList) {
				sb.append(pid_temp);
				sb.append(pid);
				pid_temp = ",";
			}
			sb.append(") GROUP BY poi_pid");
			
			pstmt = conn.prepareStatement(sb.toString());
			
			resultSet = pstmt.executeQuery();
			Map<Integer,Integer> countMap = new HashMap<>();
			while (resultSet.next()) {
				countMap.put(resultSet.getInt(1), resultSet.getInt(2));
			}
			
			return countMap;
		} catch (Exception e) {

			throw e;

		} finally {

			DbUtils.closeQuietly(resultSet);

			DbUtils.closeQuietly(pstmt);

		}
	}
	
	/**
	 * 获取poi的检查项错误个数
	 * @param pidList
	 * @param deepCheckRules
	 * @return
	 * @throws Exception
	 */
	public Map<Integer,Integer> getCheckErrorTotal(List<String> deepCheckRules,List<Long> pidList) throws Exception {
		if(pidList==null||pidList.isEmpty()){
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select C.PID,COUNT(1) from ni_val_exception n,ck_result_object c where n.MD5_CODE=c.MD5_CODE and RULEID IN (");
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			
			String temp = "";
			for (String deepCheckRule : deepCheckRules) {
				sb.append(temp);
				sb.append("'" + deepCheckRule + "'");
				temp = ",";
			}
			sb.append(")");
			
			sb.append(" AND pid in (");
			String pid_temp = "";
			for (long pid : pidList) {
				sb.append(pid_temp);
				sb.append(pid);
				pid_temp = ",";
			}
			sb.append(") GROUP BY C.PID");
			
			pstmt = conn.prepareStatement(sb.toString());
			
			resultSet = pstmt.executeQuery();
			
			Map<Integer,Integer> countMap = new HashMap<>();
			while (resultSet.next()) {
				countMap.put(resultSet.getInt(1), resultSet.getInt(2));
			}
			
			return countMap;
		} catch (Exception e) {

			throw e;

		} finally {

			DbUtils.closeQuietly(resultSet);

			DbUtils.closeQuietly(pstmt);

		}
	}
	
}
