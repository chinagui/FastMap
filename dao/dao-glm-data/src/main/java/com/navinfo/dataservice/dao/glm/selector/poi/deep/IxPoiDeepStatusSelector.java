package com.navinfo.dataservice.dao.glm.selector.poi.deep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

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
	public List<String> columnQuery(int status,String secondWorkItem,long userId) throws Exception {
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

}
