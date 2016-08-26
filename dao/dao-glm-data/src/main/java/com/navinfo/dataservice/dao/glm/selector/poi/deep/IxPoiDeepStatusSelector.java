package com.navinfo.dataservice.dao.glm.selector.poi.deep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class IxPoiDeepStatusSelector extends AbstractSelector {
	
	private Connection conn;

	public IxPoiDeepStatusSelector(Connection conn) {
		super(conn);
		this.conn = conn;
	}
	
	/**
	 * 查询可申请的rowId
	 * @param taskId
	 * @param first_work_item
	 * @return
	 * @throws Exception
	 */
	public List<String> getRowIdByTaskId(int taskId,String firstWorkItem) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT s.row_id");
		sb.append(" FROM poi_deep_status s,poi_deep_workitem_conf w");
		sb.append(" WHERE s.work_item_id=w.work_item_id");
		sb.append(" AND s.handler=0");
		sb.append(" AND s.task_id=:1");
		sb.append(" AND w.first_work_item=:2");
		
		if (firstWorkItem.equals("poi_englishname")) {
			sb.append(" AND s.work_item_id != 'FM-YW-20-017'");
			sb.append(" AND s.row_id not in (SELECT d.row_id FROM poi_deep_status d,poi_deep_workitem_conf c WHERE");
			sb.append(" d.work_item_id=c.work_item_id AND c.first_work_item='poi_name'");
			sb.append(" AND d.first_work_status != 3)");
		} else if (firstWorkItem.equals("poi_englishaddress")) {
			sb.append(" AND s.row_id not in (SELECT d.row_id FROM poi_deep_status d,poi_deep_workitem_conf c WHERE");
			sb.append(" d.work_item_id=c.work_item_id AND c.first_work_item='poi_address'");
			sb.append(" AND d.first_work_status != 3)");
		}
		
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, taskId);
			
			pstmt.setString(2, firstWorkItem);

			resultSet = pstmt.executeQuery();
			
			int count = 0;
			
			List<String> rowIdList = new ArrayList<String>();
			
			while (resultSet.next()) {
				rowIdList.add(resultSet.getString("row_id"));
				count ++;
				if (count == 100) {
					break;
				}
			}
			
			return rowIdList;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt); 
		}
	}
	
	/**
	 * 查询作业员名下已申请未提交的数据量
	 * @param firstWorkItem
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public int queryHandlerCount(String firstWorkItem,long userId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(1) num");
		sb.append(" FROM poi_deep_status s,poi_deep_workitem_conf w");
		sb.append(" WHERE s.work_item_id=w.work_item_id");
		sb.append(" AND s.handler=:1");
		sb.append(" AND w.first_work_item=:2");
		sb.append(" AND s.first_work_status != 3");
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		
		try {
			
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setLong(1, userId);
			
			pstmt.setString(2, firstWorkItem);

			resultSet = pstmt.executeQuery();
			
			int count = 0;
			
			if (resultSet.next()) {
				count= resultSet.getInt("num");
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
	 * 查询该作业员名下未提交数据的rowId
	 * @param status
	 * @param secondWorkItem
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public List<String> columnQuery(int status, String secondWorkItem,long userId) throws Exception {
		String sql = "SELECT s.row_id FROM poi_deep_status s,poi_deep_workitem_conf w "
				+ "WHERE s.work_item_id=w.work_item_id AND s.handler=:1 AND w.second_work_item=:2 "
				+ "AND s.second_work_status=:3";
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, userId);
			pstmt.setString(2, secondWorkItem);
			pstmt.setInt(3, status);
			resultSet = pstmt.executeQuery();
			
			List<String> rowIdList = new ArrayList<String>();
			
			while (resultSet.next()) {
				rowIdList.add(resultSet.getString("row_id"));
			}
			
			return rowIdList;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt); 
		}
	}
	
	/**
	 * 通过rowId获取一级作业项状态和作业标记
	 * 用于精编查询
	 * @param rowId
	 * @return
	 * @throws Exception
	 */
	public JSONObject getStatus(String rowId) throws Exception {
		String sql = "SELECT work_item_id,first_work_status FROM poi_deep_status WHERE row_id=:1";
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, rowId);
			resultSet = pstmt.executeQuery();
			
			JSONObject result = new JSONObject();
			
			if (resultSet.next()) {
				result.put("workItemId", resultSet.getString("work_item_id"));
				result.put("firstWorkStatus", resultSet.getInt("first_work_status"));
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt); 
		}
	}

/**
	 * 查詢当前poi已打作业标记
	 * @param object
	 * @return
	 * @throws Exception
	 */
	public List<String> queryClassifyByRowid(Object rowId,Object taskId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT work_item_id,handler FROM poi_deep_status s where s.row_id=:1 and s.first_work_status=1 and s.task_id=:2 ");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		List<String> workItemList=new ArrayList<String>();
		
		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setString(1, (String) rowId);
			pstmt.setInt(2, (int) taskId);
			
			resultSet = pstmt.executeQuery();
			
			if (resultSet.next()) {
				workItemList.add(resultSet.getString("work_item_id"));
			}
			
			return workItemList;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt); 
		}
		
	}
	
	/**
	 * 查询该任务下可提交数据的rowId
	 * @param firstWorkItem
	 * @param secondWorkItem
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
	public List<String> getRowIdForSubmit(String firstWorkItem,String secondWorkItem,int taskId) throws Exception {
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT s.row_id FROM poi_deep_status s,poi_deep_workitem_conf w WHERE s.work_item_id=w.work_item_id");
		
		PreparedStatement pstmt = null;
		
		ResultSet resultSet = null;
		
		try {
			List<String> rowIdList = new ArrayList<String>();
			
			if (!firstWorkItem.isEmpty()) {
				sb.append(" AND s.first_work_status=2 AND w.first_work_item='"+firstWorkItem+"'");
			}
			if (!secondWorkItem.isEmpty()) {
				sb.append(" AND s.second_work_status=2 AND w.second_work_item='"+secondWorkItem+"'");
			}
			
			pstmt = conn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				rowIdList.add(resultSet.getString("row_id"));
			}
			
			return rowIdList;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt); 
		}
	}
	
	/**
	 * 精编任务的统计查询
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
	public JSONArray taskStatistics(int taskId) throws Exception {
		
		JSONArray result = new JSONArray();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT w.second_work_item,s.second_work_status,count(1) num");
		sql.append(" FROM poi_deep_status s, poi_deep_workitem_conf w");
		sql.append(" WHERE s.work_item_id = w.work_item_id");
		sql.append(" AND s.second_work_status in (1,2)");
		sql.append(" AND task_id="+taskId);
		sql.append(" group by w.second_work_item,s.second_work_status");
		
		PreparedStatement pstmt = null;
		
		ResultSet resultSet = null;
		
		try {
			pstmt = conn.prepareStatement(sql.toString());

			resultSet = pstmt.executeQuery();
			
			while (resultSet.next()) {
				JSONObject data = new JSONObject();
				data.put("secondWorkItem", resultSet.getString("second_work_item"));
				data.put("secondWorkStatus", resultSet.getInt("second_work_status"));
				data.put("total", resultSet.getInt("num"));
				result.add(data);
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt); 
		}
	}
	
	/**
	 * 查询二级作业项的统计信息
	 * @param secondWorkItem
	 * @param userId
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public JSONObject secondWorkStatistics(String secondWorkItem,long userId,int type) throws Exception {
		
		JSONObject result = new JSONObject();
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT count(1) num,s.second_work_status");
		sql.append(" FROM poi_deep_status s, poi_deep_workitem_conf w");
		sql.append(" WHERE s.work_item_id = w.work_item_id");
		sql.append(" AND w.second_work_item='"+secondWorkItem+"'");
		sql.append(" AND s.handler="+userId);
		sql.append(" AND w.type="+type);
		sql.append(" group by s.second_work_status");
		
		PreparedStatement pstmt = null;
		
		ResultSet resultSet = null;
		
		try {
			pstmt = conn.prepareStatement(sql.toString());

			resultSet = pstmt.executeQuery();
			
			int total = 0;
			int worked = 0;
			
			while (resultSet.next()) {
				int tempcount = resultSet.getInt("num");
				total += tempcount;
				int status = resultSet.getInt("second_work_status");
				if (status == 2) {
					worked = tempcount;
				}
			}
			
			result.put("total", total);
			result.put("worked", worked);
			
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt); 
		}
	}
	
}
