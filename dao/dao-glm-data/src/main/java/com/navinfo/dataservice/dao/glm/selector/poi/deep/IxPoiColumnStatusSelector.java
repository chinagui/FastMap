package com.navinfo.dataservice.dao.glm.selector.poi.deep;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.log.LogReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class IxPoiColumnStatusSelector extends AbstractSelector {
	private static final Logger logger = Logger.getLogger(IxPoiColumnStatusSelector.class);
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
			logger.info("getRowIdByTaskId sql:"+sb);
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
		
		
		// 后期专项 + 深度信息
		if (StringUtils.isNotEmpty(secondWorkItem)) {
			sb.append(" AND w.second_work_item='" + secondWorkItem + "'");
			sb.append(" AND s.second_work_status != 3");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			logger.info("getApplyPids sql:"+sb);
			logger.info("subtask.getGeometry():"+subtask.getGeometry());
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.setInt(1, type);
			Clob geom = ConnectionUtil.createClob(conn);
			geom.setString(1, subtask.getGeometry());
			pstmt.setClob(2,geom);

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
	 * @param timeStamp
	 * @throws Exception
	 */
	public void dataSetLock(List<Integer> pids, List<String> workItemIds, long userId, int taskId, Timestamp timeStamp) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE POI_COLUMN_STATUS SET handler=:1,task_id=:2,apply_date=:3 WHERE work_item_id in (");
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
			pstmt.setTimestamp(3, timeStamp);

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
	public JSONObject columnQuery(int status, String secondWorkItem, long userId,int taskId) throws Exception {
		//按group_id排序
		StringBuilder sb = new StringBuilder();
		sb.append("	SELECT COUNT(1) OVER(PARTITION BY 1) TOTAL, PID");
		sb.append("	FROM (SELECT IX.PID,");
		sb.append("	P.GROUP_ID AS PGROUP_ID,");
		sb.append("	C.GROUP_ID AS CGROUP_ID");
		sb.append("	FROM IX_POI IX, IX_POI_PARENT P, IX_POI_CHILDREN C");
		sb.append("	WHERE IX.PID = P.PARENT_POI_PID(+)");
		sb.append("	AND IX.PID = C.CHILD_POI_PID(+)");
		sb.append("	AND IX.PID IN");
		sb.append("	(SELECT DISTINCT S.PID");
		sb.append("	FROM POI_COLUMN_STATUS S,");
		sb.append("	POI_COLUMN_WORKITEM_CONF W");
		sb.append("	WHERE S.WORK_ITEM_ID = W.WORK_ITEM_ID");
		sb.append("	AND S.HANDLER =:1");
		sb.append("	AND W.SECOND_WORK_ITEM =:2");
		sb.append("	AND S.SECOND_WORK_STATUS =:3");
		sb.append("	AND S.TASK_ID = :4)");
		sb.append("	ORDER BY P.GROUP_ID, C.GROUP_ID)");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		JSONObject data = new JSONObject();

		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setLong(1, userId);
			pstmt.setString(2, secondWorkItem);
			pstmt.setInt(3, status);
			pstmt.setInt(4, taskId);
			resultSet = pstmt.executeQuery();

			List<Integer> pidList = new ArrayList<Integer>();
			int total = 0;
			while (resultSet.next()) {
				pidList.add(resultSet.getInt("pid"));
				total = resultSet.getInt("total");
			}
			data.put("pidList", pidList);
			data.put("total", total);
			return data;
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
	 * @param pids
	 * @param secondWorkItem
	 * @param status
	 * @return
	 * @throws Exception
	 */
	public JSONObject queryClassifyByPidSecondWorkItem(List<Integer> pids,String secondWorkItem,int status,long userId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT P.PID,LISTAGG(P.WORK_ITEM_ID, ',') WITHIN GROUP(ORDER BY P.WORK_ITEM_ID) C_POI_PID ");
		sb.append("  FROM POI_COLUMN_STATUS P, POI_COLUMN_WORKITEM_CONF PC");
		sb.append(" WHERE P.WORK_ITEM_ID = PC.WORK_ITEM_ID");
		sb.append("   AND PC.SECOND_WORK_ITEM = :1");
		sb.append("   AND P.SECOND_WORK_STATUS = :2");
		sb.append("   AND P.HANDLER = :3");
		sb.append("   AND P.PID in (");
		
		String temp = "";
		for (int pid:pids) {
			sb.append(temp);
			sb.append(pid);
			temp = ",";
		}
		sb.append(")");
		sb.append(" GROUP BY P.PID");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		JSONObject poiWorkItem = new JSONObject();

		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.setString(1, secondWorkItem);
			pstmt.setInt(2, status);
			pstmt.setLong(3, userId);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				poiWorkItem.put(resultSet.getInt("pid"), resultSet.getString("C_POI_PID"));
			}

			return poiWorkItem;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

	}
	/**
	 * 查詢POI子在当前一级项下的错误LOG
	 * 
	 * @param pids
	 * @param firstWorkItem
	 * @param tbNm
	 * @return
	 * @throws Exception
	 */
	public JSONObject queryCKLogByPidfirstWorkItem(List<Integer> pids,String firstWorkItem,String secondWorkItem,String tbNm) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT CO.PID,NE.RULEID,CO.MD5_CODE,NE.INFORMATION,NE.\"LEVEL\"");
		//sb.append(" LISTAGG('RULEID:' || NE.RULEID || ',' || 'log:' || NE.INFORMATION ||'LEVEL:' || NE.\"LEVEL\",'__|__') WITHIN GROUP(ORDER BY CO.PID) LOGMSG");
		sb.append(" FROM CK_RESULT_OBJECT CO, NI_VAL_EXCEPTION NE");
		sb.append(" WHERE CO.MD5_CODE = NE.MD5_CODE");
		sb.append(" AND CO.TABLE_NAME = :1");
		sb.append(" AND NE.RULEID IN (SELECT W.WORK_ITEM_ID");
		sb.append(" FROM POI_COLUMN_WORKITEM_CONF W");
		sb.append(" WHERE W.FIRST_WORK_ITEM = :2");
		if (firstWorkItem.equals("poi_deep")){
			sb.append(" AND W.SECOND_WORK_ITEM = :3");
		}
		sb.append(" AND W.TYPE = 1)");
		sb.append("   AND CO.PID in (");
		
		String temp = "";
		for (int pid:pids) {
			sb.append(temp);
			sb.append(pid);
			temp = ",";
		}
		sb.append(")");
		//sb.append(" GROUP BY CO.PID");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		JSONObject poiWorkItem = new JSONObject();
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.setString(1, tbNm);
			pstmt.setString(2, firstWorkItem);
			if (firstWorkItem.equals("poi_deep")){
				pstmt.setString(3, secondWorkItem);
			}
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				String keyPid=String.valueOf(resultSet.getInt("PID"));
				JSONObject ckLog=new JSONObject();
				ckLog.put("ruleId", resultSet.getString("RULEID"));
				ckLog.put("log", resultSet.getString("INFORMATION"));
				ckLog.put("level", resultSet.getString("LEVEL"));
				ckLog.put("md5Code", resultSet.getString("MD5_CODE"));
				
				if(poiWorkItem.containsKey(keyPid)){
					List<JSONObject> oldValue= (List<JSONObject>) poiWorkItem.get(keyPid);
					oldValue.add(ckLog);
					poiWorkItem.put(keyPid, oldValue);
				}else{
					List<JSONObject> newValue=new ArrayList<JSONObject>();
					newValue.add(ckLog);
					poiWorkItem.put(keyPid, newValue);
				}
			}

			return poiWorkItem;
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
	public List<String> queryClassifyByPid(int pid,List classifyRules) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT work_item_id FROM poi_column_status s where s.pid=:1 and s.first_work_status in (1,2) and work_item_id in (");
		for (Object rules:classifyRules) {
			sb.append("'" + rules + "'");
			sb.append(",");
		}
		sb.setLength(sb.length()-1);
		sb.append(")");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		List<String> workItemList = new ArrayList<String>();

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setInt(1, pid);

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
public List<Integer> getRowIdForSubmit(String firstWorkItem,String secondWorkItem,int taskId) throws Exception {
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT s.pid FROM poi_column_status s,poi_column_workitem_conf w WHERE s.work_item_id=w.work_item_id");
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			List<Integer> pidList = new ArrayList<Integer>();

			if (!secondWorkItem.isEmpty()) {
				sb.append(" AND s.second_work_status=2 AND w.second_work_item='" + secondWorkItem + "'");
			} else if (!firstWorkItem.isEmpty()) {
				sb.append(" AND s.first_work_status=2 AND w.first_work_item='" + firstWorkItem + "'");
			}

			pstmt = conn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				pidList.add(resultSet.getInt("pid"));
			}
			
			return pidList;
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
	 * @param taskId
	 * @return
	 * @throws Exception
	 * 
	 *             return model: {"data": {"total": {"count": 2, "check": 2},
	 *             "details": [{"count": 2, "id": "addrSplit", "check": 2},
	 *             {"count": 0, "id": "addrPinyin", "check": 0}]}, "errcode": 0,
	 *             "errmsg": "success"}
	 */
	@SuppressWarnings("rawtypes")
	public JSONObject secondWorkStatistics(String firstWorkItem, long userId, int type, int taskId) throws Exception {

		JSONObject result = new JSONObject();

		StringBuilder sql = new StringBuilder();
//		sql.append("SELECT count(1) num,s.second_work_status,w.second_work_item");
//		sql.append(" FROM poi_column_status s, poi_column_workitem_conf w");
//		sql.append(" WHERE s.work_item_id = w.work_item_id");
//		sql.append(" AND w.first_work_item='" + firstWorkItem + "'");
//		sql.append(" AND s.handler=" + userId);
//		sql.append(" AND w.type=" + type);
//		sql.append(" AND s.task_id=" + taskId);
//		sql.append(" AND s.second_work_status in (1,2)");
//		sql.append(" group by s.second_work_status,w.second_work_item");
//		sql.append(" order by w.second_work_item");
		sql.append("SELECT AA.SECOND_WORK_ITEM,");
		sql.append("       NVL(TT.SECOND_WORK_STATUS, 0) SECOND_WORK_STATUS,");
		sql.append("       NVL(TT.CT, 0) NUM");
		sql.append("  FROM (SELECT CC.SECOND_WORK_ITEM, S.SECOND_WORK_STATUS, COUNT(1) CT");
		sql.append("          FROM POI_COLUMN_STATUS S,");
		sql.append("               (SELECT DISTINCT CF.SECOND_WORK_ITEM, CF.WORK_ITEM_ID");
		sql.append("                  FROM POI_COLUMN_WORKITEM_CONF CF");
		sql.append("                 WHERE CF.FIRST_WORK_ITEM = '"+ firstWorkItem +"'");
		sql.append("                   AND CF.CHECK_FLAG IN (1, 3)");
		sql.append("                   AND CF.TYPE = " + type + ") CC");
		sql.append("         WHERE S.WORK_ITEM_ID = CC.WORK_ITEM_ID");
		sql.append("           AND S.SECOND_WORK_STATUS IN (1, 2)");
		sql.append("           AND S.HANDLER = " + userId);
		sql.append("           AND S.TASK_ID = " + taskId);
		sql.append("         GROUP BY CC.SECOND_WORK_ITEM, S.SECOND_WORK_STATUS) TT,");
		sql.append("       (SELECT DISTINCT CF.SECOND_WORK_ITEM");
		sql.append("          FROM POI_COLUMN_WORKITEM_CONF CF");
		sql.append("         WHERE CF.SECOND_WORK_ITEM <> 'nonImportantEngAddress' AND CF.FIRST_WORK_ITEM = '" + firstWorkItem + "'");
		sql.append("           AND CF.CHECK_FLAG IN (1, 3)) AA");
		sql.append(" WHERE TT.SECOND_WORK_ITEM(+) = AA.SECOND_WORK_ITEM");

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			logger.info("secondWorkStatistics:"+sql);
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

				// 对于查询在库中没有的二级项的POI，赋second_work_status默认值为0，所以查询出的总量和待提交量都是0
				if (status == 0){
					data.put("check", 0);
					data.put("work", 0);
				}
				
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
				check += data.getInt("work");

				JSONObject secondObj = new JSONObject();
				secondObj.put("count", count);
				secondObj.put("id", id);
				secondObj.put("check", data.getInt("work"));
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

	
	
	/**
	 * 根据任务号和handler获取pids
	 * @param taskId
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public List<Integer> getPids(int taskId, long userId)  throws Exception{
		
		PreparedStatement pstmt = null;
		
		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct s.pid from poi_column_status s where TASK_ID=:1 and HANDLER=:2");
			
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.setInt(1, taskId);
			pstmt.setLong(2, userId);
			
			resultSet = pstmt.executeQuery();
			List<Integer> pids = new ArrayList<Integer>();
			
			while(resultSet.next()) {
				pids.add(resultSet.getInt("pid"));
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
	 * 统计精编库存log量统计
	 * @param subtask
	 * @return
	 * @throws Exception
	 */
	public JSONObject getColumnCount(Subtask subtask) throws Exception{
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select count(1) as num, c.first_work_item as type");
			sb.append("  from poi_column_status s, poi_column_workitem_conf c,ix_poi p");
			sb.append(" where s.work_item_id = c.work_item_id");
			sb.append("  and s.pid=p.pid");
			sb.append("  and sdo_within_distance(p.geometry, sdo_geometry(    :1  , 8307), 'mask=anyinteract') = 'TRUE'");
			sb.append("  and c.first_work_item in");
			sb.append("  ('poi_name', 'poi_address', 'poi_englishname', 'poi_englishaddress')");
			sb.append("  and s.first_work_status = 1");
			sb.append("  and s.handler = 0");
			sb.append(" GROUP BY c.first_work_item");
			sb.append(" union all");
			sb.append(" select count(1) as num, c1.second_work_item as type");
			sb.append("  from poi_column_status s1, poi_column_workitem_conf c1,ix_poi p1");
			sb.append(" where s1.work_item_id = c1.work_item_id");
			sb.append("  and s1.pid=p1.pid");
			sb.append("  and sdo_within_distance(p1.geometry, sdo_geometry(    :2  , 8307), 'mask=anyinteract') = 'TRUE'");
			sb.append("  and c1.second_work_item in ('deepDetail', 'deepParking', 'deepCarrental')");
			sb.append("  and s1.second_work_status = 1");
			sb.append("  and s1.handler = 0");
			sb.append(" GROUP BY c1.second_work_item");

			pstmt = conn.prepareStatement(sb.toString());
			 
			Clob geoClob =ConnectionUtil.createClob(conn);
			geoClob.setString(1, subtask.getGeometry());
			pstmt.setClob(1, geoClob);
			pstmt.setClob(2, geoClob);
			
			resultSet = pstmt.executeQuery();
			
			String parameter = "{\"poi_name\":0,\"poi_address\":0,\"poi_englishname\":0,\"poi_englishaddress\":0,\"deepDetail\":0,\"deepParking\":0,\"deepCarrental\":0}";
			JSONObject result = JSONObject.fromObject(parameter);
			
			while (resultSet.next()) {
				String type = resultSet.getString("type");
				if ("poi_name".equals(type)) {
					result.put("poi_name", resultSet.getInt("num"));
				} else if ("poi_address".equals(type)) {
					result.put("poi_address", resultSet.getInt("num"));
				} else if ("poi_englishname".equals(type)) {
					result.put("poi_englishname", resultSet.getInt("num"));
				} else if ("poi_englishaddress".equals(type)) {
					result.put("poi_englishaddress", resultSet.getInt("num"));
				} else if ("deepDetail".equals(type)) {
					result.put("deepDetail", resultSet.getInt("num"));
				} else if ("deepParking".equals(type)) {
					result.put("deepParking", resultSet.getInt("num"));
				} else if ("deepCarrental".equals(type)) {
					result.put("deepCarrental", resultSet.getInt("num"));
				}
			}
			
			return result;
		} catch (Exception e){
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

	}
	/**
	 * 根据status,userid,secondWorkItem,subtask 获取可提交的数据rowIds
	 * @param subtask
	 * @param status
	 * @param userid
	 * @param secondWorkItem
	 * @return
	 * @throws Exception
	 */
	public List<Integer> getpidsForRelease(int subtaskId ,int status, long userid, String secondWorkItem) throws Exception {
		List<Integer> pids = new ArrayList<Integer>();
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT S.PID");
		sb.append(" FROM POI_COLUMN_STATUS S");
		sb.append(" WHERE S.TASK_ID = ?");
		sb.append(" AND S.WORK_ITEM_ID IN");
		sb.append(" (SELECT CF.WORK_ITEM_ID");
		sb.append(" FROM POI_COLUMN_WORKITEM_CONF CF");
		sb.append(" WHERE CF.SECOND_WORK_ITEM = ?)");
		sb.append(" AND S.HANDLER = ?");
		sb.append(" AND S.SECOND_WORK_STATUS = ?");
		sb.append(" AND NOT EXISTS (SELECT C.PID");
		sb.append(" FROM NI_VAL_EXCEPTION N, CK_RESULT_OBJECT C");
		sb.append(" WHERE N.MD5_CODE = C.MD5_CODE");
		sb.append(" AND C.PID = s.PID)");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1,subtaskId);
			pstmt.setString(2, secondWorkItem);
			pstmt.setLong(3, userid);
			pstmt.setInt(4, status);
			
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				pids.add(resultSet.getInt("pid"));
			}
			
			return pids;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
}
