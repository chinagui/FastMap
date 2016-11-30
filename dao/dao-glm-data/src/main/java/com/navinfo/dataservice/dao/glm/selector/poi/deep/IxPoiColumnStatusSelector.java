package com.navinfo.dataservice.dao.glm.selector.poi.deep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.log.LogReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class IxPoiColumnStatusSelector extends AbstractSelector {

	private Connection conn;

	public IxPoiColumnStatusSelector(Connection conn) {
		super(conn);
		this.conn = conn;
	}

	/**
	 * 查询可申请的rowId
	 * 
	 * @param taskId
	 * @param first_work_item
	 * @return
	 * @throws Exception
	 */
	public List<String> getRowIdByTaskId(int taskId, String firstWorkItem) throws Exception {
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
				count++;
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
	 * 根据sutask,firstWorkItem,secondWorkItem获取可以申请的pids
	 * @param subtask
	 * @param firstWorkItem
	 * @param secondWorkItem
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public List<Integer> getApplyPids(Subtask subtask, String firstWorkItem, String secondWorkItem, int type) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT s.pid");
		sb.append(" FROM POI_COLUMN_STATUS s, POI_COLUMN_WORKITEM_CONF w, IX_POI p");
		sb.append(" WHERE s.work_item_id=w.work_item_id");
		sb.append(" AND s.pid=p.pid");
		sb.append(" AND s.handler=0");
		sb.append(" AND w.type=:1");
		sb.append(" AND sdo_within_distance(p.geometry, sdo_geometry(    :2  , 8307), 'mask=anyinteract') = 'TRUE'");
		sb.append(" AND w.first_work_item='" + firstWorkItem + "'");

		// 月编专项作业
		List<String> columnFirstWorkItems = Arrays.asList("poi_name", "poi_address", "poi_englishname",
				"poi_englishaddress");
		if (columnFirstWorkItems.contains(firstWorkItem)) {
			sb.append(" AND s.first_work_status != 3");
		}

		// 申请英文名称/英文地址时，必须中文名称/中文地址作业完成
		// 英文名称申请时，WORK_ITEM_ID为FM-YW-20-017的数据不作业，不参与申请
		if (firstWorkItem.equals("poi_englishname")) {
			sb.append(" AND s.work_item_id != 'FM-YW-20-017'");
			sb.append(" AND s.pid not in (SELECT d.pid FROM POI_COLUMN_STATUS d,POI_COLUMN_WORKITEM_CONF c WHERE");
			sb.append(" d.work_item_id=c.work_item_id AND c.first_work_item='poi_name'");
			sb.append(" AND d.first_work_status != 3)");
		} else if (firstWorkItem.equals("poi_englishaddress")) {
			sb.append(" AND s.pid not in (SELECT d.pid FROM POI_COLUMN_STATUS d,POI_COLUMN_WORKITEM_CONF c WHERE");
			sb.append(" d.work_item_id=c.work_item_id AND c.first_work_item='poi_address'");
			sb.append(" AND d.first_work_status != 3)");
		}
		
		
		// 后期专项
		if (StringUtils.isNotEmpty(secondWorkItem)) {
			sb.append(" AND w.second_work_item='" + secondWorkItem + "'");
			sb.append(" AND s.second_work_status != 3");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {

			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.setInt(1, type);

			pstmt.setString(2, subtask.getGeometry());

			resultSet = pstmt.executeQuery();

			int count = 0;
			List<Integer> pids = new ArrayList<Integer>();

			while (resultSet.next()) {
				int pid = resultSet.getInt("pid");
				// 判断state为非删除的poi
				LogReader logRead = new LogReader(conn);
				int poiState = logRead.getObjectState(pid, "IX_POI");
				if (poiState != 2) {
					pids.add(pid);
					count ++;
				}
				if (count == 100) {
					break;
				}
			}

			return pids;

		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

	}

	/**
	 * 月编申请数据-数据加锁
	 * 
	 * @param pids
	 * @param workItemIds
	 * @param userId
	 * @param taskId
	 * @throws Exception
	 */
	public void dataSetLock(List<Integer> pids, List<String> workItemIds, long userId, int taskId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE POI_COLUMN_STATUS SET handler=:1,task_id=:2 WHERE work_item_id in (");
		String temp = "";
		for (String workItemId : workItemIds) {
			sb.append(temp);
			sb.append("'" + workItemId + "'");
			temp = ",";
		}
		sb.append(")");
		
		sb.append(" AND pid in (");
		String pid_temp = "";
		for (int pid : pids) {
			sb.append(pid_temp);
			sb.append(pid);
			pid_temp = ",";
		}
		sb.append(")");

		PreparedStatement pstmt = null;

		try {

			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setLong(1, userId);
			pstmt.setInt(2, taskId);

			pstmt.execute();

		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
	}

	/**
	 * 查询作业员名下已申请未提交的数据量
	 * 
	 * @param firstWorkItem
	 * @param secondWorkItem
	 * @param userId
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public int queryHandlerCount(String firstWorkItem, String secondWorkItem, long userId, int type) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(distinct s.pid) num");
		sb.append(" FROM POI_COLUMN_STATUS s,POI_COLUMN_WORKITEM_CONF w");
		sb.append(" WHERE s.work_item_id = w.work_item_id");
		sb.append(" AND s.handler = :1");
		sb.append(" AND w.type = :2");

		if (StringUtils.isNotEmpty(firstWorkItem)) {
			sb.append(" AND w.FIRST_WORK_ITEM='" + firstWorkItem + "'");
		}

		if (StringUtils.isNotEmpty(secondWorkItem)) {
			sb.append(" AND w.SECOND_WORK_ITEM='" + secondWorkItem + "'");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setLong(1, userId);

			pstmt.setInt(2, type);

			resultSet = pstmt.executeQuery();

			int count = 0;

			if (resultSet.next()) {
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
	 * 根据一级项和二级项加载workItemId
	 * @param firstWorkItem
	 * @param secondWorkItem
	 * @return
	 * @throws Exception
	 */
	public List<String> getWorkItemIds(String firstWorkItem, String secondWorkItem) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT w.work_item_id");
		sb.append(" FROM POI_COLUMN_WORKITEM_CONF w");
		sb.append(" WHERE w.FIRST_WORK_ITEM='" + firstWorkItem + "'");

		if (StringUtils.isNotEmpty(secondWorkItem)) {
			sb.append(" AND w.SECOND_WORK_ITEM='" + secondWorkItem + "'");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			List<String> workItemIds = new ArrayList<String>();

			while (resultSet.next()) {
				workItemIds.add(resultSet.getString("work_item_id"));
			}

			return workItemIds;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	
	/**
	 * 查询该作业员名下未提交数据的rowId
	 * 
	 * @param status
	 * @param secondWorkItem
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public List<String> columnQuery(int status, String secondWorkItem, long userId) throws Exception {
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
	 * 通过rowId获取一级作业项状态和作业标记 用于精编查询
	 * 
	 * @param rowId
	 * @return
	 * @throws Exception
	 */
	public JSONObject getStatus(String rowId, String secondWorkItem) throws Exception {
		String sql = "SELECT s.work_item_id,s.first_work_status FROM poi_deep_status s,poi_deep_workitem_conf w WHERE s.work_item_id=w.work_item_id AND s.row_id=:1 AND w.second_work_item=:2";

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, rowId);
			pstmt.setString(2, secondWorkItem);
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
	 * 
	 * @param object
	 * @return
	 * @throws Exception
	 */
	public List<String> queryClassifyByRowid(Object rowId, Object taskId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"SELECT work_item_id,handler FROM poi_deep_status s where s.row_id=:1 and s.first_work_status=1 and s.task_id=:2 ");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		List<String> workItemList = new ArrayList<String>();

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
	 * 
	 * @param firstWorkItem
	 * @param secondWorkItem
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
	public List<String> getRowIdForSubmit(String firstWorkItem, String secondWorkItem, int taskId) throws Exception {

		StringBuilder sb = new StringBuilder();
		sb.append(
				"SELECT s.row_id FROM poi_deep_status s,poi_deep_workitem_conf w WHERE s.work_item_id=w.work_item_id");

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			List<String> rowIdList = new ArrayList<String>();

			if (!firstWorkItem.isEmpty()) {
				sb.append(" AND s.first_work_status=2 AND w.first_work_item='" + firstWorkItem + "'");
			}
			if (!secondWorkItem.isEmpty()) {
				sb.append(" AND s.second_work_status=2 AND w.second_work_item='" + secondWorkItem + "'");
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
	 * 
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
	public JSONObject taskStatistics(int taskId) throws Exception {

		JSONObject result = new JSONObject();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT s.second_work_status,count(1) num");
		sql.append(" FROM poi_deep_status s, poi_deep_workitem_conf w");
		sql.append(" WHERE s.work_item_id = w.work_item_id");
		sql.append(" AND s.second_work_status in (1,2)");
		sql.append(" AND task_id=" + taskId);
		sql.append(" group by s.second_work_status");

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql.toString());

			resultSet = pstmt.executeQuery();

			JSONObject data = new JSONObject();

			int needWork = 0;
			int finished = 0;

			while (resultSet.next()) {
				if (resultSet.getInt("second_work_status") == 1) {
					needWork = resultSet.getInt("num");
					data.put("needWork", needWork);
				} else if (resultSet.getInt("second_work_status") == 2) {
					finished = resultSet.getInt("num");
					data.put("finished", finished);
				}
			}

			data.put("all", needWork + finished);

			result.put("poi", data);

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
	 * 
	 * @param secondWorkItem
	 * @param userId
	 * @param type
	 * @return
	 * @throws Exception
	 * 
	 *             return model: {"data": {"total": {"count": 2, "check": 2},
	 *             "details": [{"count": 2, "id": "addrSplit", "check": 2},
	 *             {"count": 0, "id": "addrPinyin", "check": 0}]}, "errcode": 0,
	 *             "errmsg": "success"}
	 */
	@SuppressWarnings("rawtypes")
	public JSONObject secondWorkStatistics(String firstWorkItem, long userId, int type) throws Exception {

		JSONObject result = new JSONObject();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT count(1) num,s.second_work_status,w.second_work_item");
		sql.append(" FROM poi_deep_status s, poi_deep_workitem_conf w");
		sql.append(" WHERE s.work_item_id = w.work_item_id");
		sql.append(" AND w.first_work_item='" + firstWorkItem + "'");
		sql.append(" AND s.handler=" + userId);
		sql.append(" AND w.type=" + type);
		sql.append(" AND s.second_work_status in (1,2)");
		sql.append(" group by s.second_work_status,w.second_work_item");
		sql.append(" order by w.second_work_item");

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql.toString());

			resultSet = pstmt.executeQuery();

			int total = 0;
			int check = 0;

			JSONArray details = new JSONArray();

			JSONObject secondWork = new JSONObject();

			while (resultSet.next()) {

				String secondId = resultSet.getString("second_work_item");

				JSONObject data = new JSONObject();
				data.put("check", 0);
				data.put("work", 0);

				if (secondWork.containsKey(secondId)) {
					data = secondWork.getJSONObject(secondId);
				}

				int status = resultSet.getInt("second_work_status");

				if (status == 1) {
					data.put("check", resultSet.getInt("num"));
				} else if (status == 2) {
					data.put("work", resultSet.getInt("num"));
				}

				secondWork.put(secondId, data);
			}

			for (Iterator iter = secondWork.keys(); iter.hasNext();) {
				String id = (String) iter.next();
				JSONObject data = secondWork.getJSONObject(id);
				int count = data.getInt("check") + data.getInt("work");

				total += count;
				check += data.getInt("check");

				JSONObject secondObj = new JSONObject();
				secondObj.put("count", count);
				secondObj.put("id", id);
				secondObj.put("check", data.getInt("check"));
				details.add(secondObj);
			}

			JSONObject totalObj = new JSONObject();
			totalObj.put("count", total);
			totalObj.put("check", check);

			result.put("total", totalObj);
			result.put("details", details);

			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}

}
