package com.navinfo.dataservice.dao.glm.selector.poi.deep;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
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
	public List<Integer> getApplyPids(Subtask subtask, String firstWorkItem, String secondWorkItem, int type,int qcFlag,JSONObject conditions,long userId) throws Exception {
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
		//质检标示
		sb.append(" AND s.QC_FLAG =:3 ");
		//如果是质检，需要扩充质检条件
		if(qcFlag==1){
			String commenUserId =conditions.getString("commenUserId");
			String startTime =conditions.getString("startTime");
			String endTime =conditions.getString("endTime");
			if(!startTime.isEmpty()){
				sb.append(" and  s.apply_date >= to_date('"+startTime+"', 'yyyymmddhh24miss') ");
			}
			if(!endTime.isEmpty()){
				sb.append(" and s.apply_date <= to_date('"+endTime+"', 'yyyymmddhh24miss') ");
			}
			sb.append(" and s.COMMON_HANDLER<>"+userId+" ");
			if(!commenUserId.isEmpty()){
				sb.append(" and s.COMMON_HANDLER in ("+commenUserId+") ");
			}
			sb.append(" and NOT EXISTS ");
			sb.append(" (SELECT 1 FROM POI_COLUMN_STATUS PS, POI_COLUMN_WORKITEM_CONF PC ");
			sb.append(" WHERE PS.PID = S.PID ");
			sb.append(" AND PS.HANDLER <> 0 ");
			sb.append(" AND PC.TYPE = 1 ");
			sb.append(" AND PC.CHECK_FLAG IN (1, 3) ");
			sb.append(" AND PS.WORK_ITEM_ID = PC.WORK_ITEM_ID ");
			sb.append(" AND PC.FIRST_WORK_ITEM ='"+firstWorkItem+"') ");
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
			pstmt.setInt(3,qcFlag);

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
	public void dataSetLock(List<Integer> pids, List<String> workItemIds, long userId, int taskId, Timestamp timeStamp,int qcFlag) throws Exception {
		StringBuilder sb = new StringBuilder();
		if(qcFlag==1){
			sb.append("UPDATE POI_COLUMN_STATUS SET handler=:1,task_id=:2,apply_date=:3 WHERE work_item_id in (");
		}else{
			sb.append("UPDATE POI_COLUMN_STATUS SET handler=:1,COMMON_HANDLER="+userId+",task_id=:2,apply_date=:3 WHERE work_item_id in (");
		}
		
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
	 * @param subtaskId
	 * @return
	 * @throws Exception
	 */
	public int queryHandlerCount(String firstWorkItem, String secondWorkItem, long userId, int type, int subtaskId,int qcFlag) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(distinct s.pid) num");
		sb.append(" FROM POI_COLUMN_STATUS s,POI_COLUMN_WORKITEM_CONF w");
		sb.append(" WHERE s.work_item_id = w.work_item_id");
		sb.append(" AND s.handler = :1");
		sb.append(" AND w.type = :2");
		sb.append(" AND s.TASK_ID = :3 ");
		if(qcFlag==1){
			sb.append(" AND s.common_handler <> :4 ");
			sb.append(" AND s.QC_FLAG = 1 ");
		}else{
			sb.append(" AND s.common_handler = :4");
		}

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
			
			pstmt.setInt(3, subtaskId);
			
			pstmt.setLong(4, userId);

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
	 * 根据指定的poi列表，找到存在别名作业项的数据
	 * @param firstWorkItem
	 * @param secondWorkItem
	 * @return
	 * @throws Exception
	 */
	public List<Integer> getHasAliasItemPids(List<Integer> pids, String secondWorkItem,long userId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT w.pid");
		sb.append(" FROM POI_COLUMN_STATUS w");
		sb.append(" WHERE w.work_item_id='" + secondWorkItem + "'");
		sb.append(" AND w.handler=" + userId +" ");
		sb.append("   AND w.PID in (");
		String temp = "";
		for (int pid:pids) {
			sb.append(temp);
			sb.append(pid);
			temp = ",";
		}
		sb.append(")");


		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {

			pstmt = conn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			List<Integer> hasAliasItemPids = new ArrayList<Integer>();

			while (resultSet.next()) {
				hasAliasItemPids.add(resultSet.getInt("pid"));
			}

			return hasAliasItemPids;
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
	public JSONObject columnQuery(int status, String secondWorkItem, long userId,int taskId,Integer isQuality) throws Exception {
		//按group_id排序
		StringBuilder sb = new StringBuilder();
		sb.append("	SELECT COUNT(1) OVER(PARTITION BY 1) TOTAL, PID");
		sb.append("	FROM (SELECT IX.PID,");
		sb.append("	P.GROUP_ID AS PGROUP_ID,");
		sb.append("	C.GROUP_ID AS CGROUP_ID");
		sb.append("	FROM IX_POI IX, IX_POI_PARENT P, IX_POI_CHILDREN C");
		sb.append("	WHERE IX.PID IN");
		sb.append("	(SELECT DISTINCT S.PID");
		sb.append("	FROM POI_COLUMN_STATUS S,");
		sb.append("	POI_COLUMN_WORKITEM_CONF W");
		sb.append("	WHERE S.HANDLER =:1 ");
		sb.append("	AND S.SECOND_WORK_STATUS =:2");
		sb.append("	AND S.TASK_ID = :3");
		if(isQuality==0){//常规任务
			sb.append("	AND S.COMMON_HANDLER = "+userId);
		}else if(isQuality==1){//质检任务
			sb.append("	AND S.COMMON_HANDLER <> "+userId);
			sb.append("	AND S.QC_FLAG = 1");
		}
		sb.append("	AND S.WORK_ITEM_ID = W.WORK_ITEM_ID");
		sb.append("	AND W.SECOND_WORK_ITEM =:4)");
		
		sb.append("	AND IX.PID = P.PARENT_POI_PID(+)");
		sb.append("	AND IX.PID = C.CHILD_POI_PID(+)");
		sb.append("	ORDER BY P.GROUP_ID, C.GROUP_ID)");
		logger.info(sb.toString());
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		JSONObject data = new JSONObject();

		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setLong(1, userId);
			pstmt.setInt(2, status);
			pstmt.setInt(3, taskId);
			pstmt.setString(4, secondWorkItem);
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
	public JSONObject queryClassifyByPidSecondWorkItem(List<Integer> pids,String secondWorkItem,int status,long userId,Integer isQuality) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT P.PID,LISTAGG(P.WORK_ITEM_ID, ',') WITHIN GROUP(ORDER BY P.WORK_ITEM_ID) C_POI_PID ");
		sb.append("  FROM POI_COLUMN_STATUS P, POI_COLUMN_WORKITEM_CONF PC");
		sb.append(" WHERE P.WORK_ITEM_ID = PC.WORK_ITEM_ID");
		sb.append("   AND PC.SECOND_WORK_ITEM = :1");
		sb.append("   AND P.SECOND_WORK_STATUS = :2");
		sb.append("   AND P.HANDLER = :3");
		if(isQuality==0){//常规任务
			sb.append("	AND P.COMMON_HANDLER = "+userId);
		}else if(isQuality==1){//质检任务
			sb.append("	AND P.COMMON_HANDLER <> "+userId);
			sb.append("	AND P.QC_FLAG = 1");
		}
		
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
		sb.append(" WHERE W.SECOND_WORK_ITEM = :2");
//		if (firstWorkItem.equals("poi_deep")){
//			sb.append(" AND W.SECOND_WORK_ITEM = :3");
//		}
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
			pstmt.setString(2, secondWorkItem);
//			if (firstWorkItem.equals("poi_deep")){
//				pstmt.setString(3, secondWorkItem);
//			}
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
	 * 查詢POI问题列表
	 * 
	 * @param pids
	 * @param firstWorkItem
	 * @param tbNm
	 * @return
	 * @throws Exception
	 */
	public Map<Integer,JSONObject> queryIsProblemsByPids(List<Integer> pids,String secondWorkItem,int comSubTaskId) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT P.PID,P.IS_PROBLEM");
		sb.append(" FROM COLUMN_QC_PROBLEM P ");
		sb.append(" WHERE P.SECOND_WORK_ITEM = '"+secondWorkItem+"' ");
		sb.append(" AND P.PID IN (");
		String temp = "";
		for (int pid:pids) {
			sb.append(temp);
			sb.append(pid);
			temp = ",";
		}
		sb.append(")");
		sb.append(" AND P.SUBTASK_ID = "+comSubTaskId+" ");
		sb.append(" AND P.IS_VALID=0 ");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		Map<Integer,JSONObject> isProblemData = new HashMap<Integer,JSONObject>();
		try {
			pstmt = conn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				JSONObject value=new JSONObject();
				
				if(secondWorkItem.equals("namePinyin")){
					value.put("py",string2json(resultSet.getString("IS_PROBLEM")));	
				}else{
					value.put("other",resultSet.getString("IS_PROBLEM"));	
				}
				
				isProblemData.put(resultSet.getInt("PID"), value);
			}

			return isProblemData;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

	}
	private JSONObject string2json(String data){
		JSONObject newdata = new JSONObject();
		String[] strs=data.split("\\|");
		for(String str:strs){
			JSONObject js =JSONObject.fromObject("{\""+str.replace(":", "\":\"")+"\"}");
			Iterator<String> keys = js.keys();  
	        while (keys.hasNext()) {  
				String key=(String) keys.next();
				newdata.put(key, js.get(key));
			}
		}

		return newdata;
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
	 * 查询该任务下可提交数据的Pid
	 * 
	 * @param firstWorkItem
	 * @param secondWorkItem
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
public List<Integer> getPIdForSubmit(String firstWorkItem,String secondWorkItem,int taskId,int userId,boolean flag) throws Exception {
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT s.pid FROM poi_column_status s,poi_column_workitem_conf w WHERE s.work_item_id=w.work_item_id");
		sb.append(" AND s.handler=" + userId + " AND s.task_id=" + taskId);
		if(flag){
			sb.append(" AND S.QC_FLAG=1");
		}
		
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
	public JSONObject secondWorkStatistics(String firstWorkItem, long userId, int type, int taskId,Integer isQuality) throws Exception {

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
		if(!firstWorkItem.equals("poi_deep")){
			if(isQuality==0){//常规任务
				sql.append("	AND S.COMMON_HANDLER = "+userId);
			}else if(isQuality==1){//质检任务
				sql.append("	AND S.COMMON_HANDLER <> "+userId);
				sql.append("	AND S.QC_FLAG = 1");
			}
		}
		sql.append("         GROUP BY CC.SECOND_WORK_ITEM, S.SECOND_WORK_STATUS) TT,");
		sql.append("       (SELECT DISTINCT CF.SECOND_WORK_ITEM");
		sql.append("          FROM POI_COLUMN_WORKITEM_CONF CF");
		sql.append("         WHERE CF.SECOND_WORK_ITEM not in ('nonImportantEngAddress','netEngName','aliasOriEngName','aliasStdEngName') AND CF.FIRST_WORK_ITEM = '" + firstWorkItem + "'");
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
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public JSONObject getColumnCount(Subtask subtask,int deepTaskId,long userId,int isQuality) throws Exception{
		int taskId = subtask.getSubtaskId();
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(" with t1 as");
			sb.append("  (select s.pid, c.first_work_item, c.second_work_item,S.COMMON_HANDLER,s.QC_FLAG ");
			sb.append("  from poi_column_status s, poi_column_workitem_conf c, ix_poi p");
			sb.append(" where s.work_item_id = c.work_item_id");
			sb.append("   and s.work_item_id != 'FM-YW-20-017'");
			sb.append("   and s.pid = p.pid");
			sb.append("   and sdo_within_distance(p.geometry,sdo_geometry(:1,8307),'mask=anyinteract') = 'TRUE'");
			sb.append("   and ((c.first_work_item in ('poi_name', 'poi_address') and");
			sb.append("       s.first_work_status = 1) or");
			sb.append("       (c.first_work_item = 'poi_englishname' and s.first_work_status = 1 and");
			sb.append("       s.pid not in");
			sb.append("       (select ss.pid");
			sb.append("            from poi_column_status ss, poi_column_workitem_conf cc");
			sb.append("           where ss.work_item_id = cc.work_item_id");
			sb.append("             and cc.first_work_item = 'poi_name'");
			sb.append("             and ss.first_work_status <> 3)) or");
			sb.append("       (c.first_work_item = 'poi_englishaddress' and");
			sb.append("       s.first_work_status = 1 and");
			sb.append("       s.pid not in");
			sb.append("       (select ss.pid");
			sb.append("            from poi_column_status ss, poi_column_workitem_conf cc");
			sb.append("           where ss.work_item_id = cc.work_item_id");
			sb.append("             and cc.first_work_item = 'poi_address'");
			sb.append("             and ss.first_work_status <> 3)) or");
			sb.append("       (c.second_work_item in");
			sb.append("       ('deepDetail', 'deepParking', 'deepCarrental') and");
			sb.append("       s.second_work_status = 1))");
			sb.append("   and s.handler = 0)");
			sb.append(" select count (1) as num, p.first_work_item as type");
			sb.append("   from t1 p");
			sb.append("  where p.first_work_item in ('poi_name', 'poi_address', 'poi_englishname', 'poi_englishaddress')");
			if(isQuality==0){//常规任务
				sb.append("	AND p.COMMON_HANDLER =0 ");
			}else if(isQuality==1){//质检任务
				sb.append("	AND p.COMMON_HANDLER <> "+userId);
				sb.append("	AND p.QC_FLAG = 1");
				sb.append(" AND NOT exists (SELECT 1 FROM POI_COLUMN_STATUS PS, POI_COLUMN_WORKITEM_CONF PC ");
				sb.append(" WHERE PS.PID = p.pid AND PS.HANDLER <> 0 AND PC.TYPE = 1 AND PC.CHECK_FLAG IN (1, 3) ");
				sb.append(" AND PS.WORK_ITEM_ID = PC.WORK_ITEM_ID AND pc.first_work_item  = p.FIRST_WORK_ITEM)");
			}
			sb.append("  GROUP BY p.first_work_item");
			sb.append(" union all");
			sb.append(" select count(1) as num, p1.second_work_item as type");
			sb.append("   from t1 p1");
			sb.append("  where p1.second_work_item in ('deepDetail', 'deepParking', 'deepCarrental')");
			if(isQuality==0){//常规任务
				sb.append("	AND p1.COMMON_HANDLER =0 ");
			}else if(isQuality==1){//质检任务
				sb.append("	AND p1.COMMON_HANDLER <> "+userId);
				sb.append("	AND p1.QC_FLAG = 1");
				sb.append(" AND NOT exists (SELECT 1 FROM POI_COLUMN_STATUS PS, POI_COLUMN_WORKITEM_CONF PC ");
				sb.append(" WHERE PS.PID = p1.pid AND PS.HANDLER <> 0 AND PC.TYPE = 1 AND PC.CHECK_FLAG IN (1, 3) ");
				sb.append(" AND PS.WORK_ITEM_ID = PC.WORK_ITEM_ID AND pc.second_work_item  = p1.second_work_item)");
			}
			sb.append("  GROUP BY p1.second_work_item");


			pstmt = conn.prepareStatement(sb.toString());
			 
			Clob geoClob =ConnectionUtil.createClob(conn);
			geoClob.setString(1, subtask.getGeometry());
			pstmt.setClob(1, geoClob);
			
			resultSet = pstmt.executeQuery();
			
			String parameter = "{\"poi_name\":{\"kcLog\":0,\"flag\":0},\"poi_address\":{\"kcLog\":0,\"flag\":0},\"poi_englishname\":{\"kcLog\":0,\"flag\":0},\"poi_englishaddress\":{\"kcLog\":0,\"flag\":0},\"deepDetail\":{\"kcLog\":0,\"flag\":0},\"deepParking\":{\"kcLog\":0,\"flag\":0},\"deepCarrental\":{\"kcLog\":0,\"flag\":0}}";
			JSONObject result = JSONObject.fromObject(parameter);
			
			while (resultSet.next()) {
				String type = resultSet.getString("type");
				JSONObject json = result.getJSONObject(type);
				int num = resultSet.getInt("num");
				json.put("kcLog", num);
				
				if ("poi_name".equals(type)) {
					result.put("poi_name", json);
				} else if ("poi_address".equals(type)) {
					result.put("poi_address", json);
				} else if ("poi_englishname".equals(type)) {
					result.put("poi_englishname", json);
				} else if ("poi_englishaddress".equals(type)) {
					result.put("poi_englishaddress", json);
				} else if ("deepDetail".equals(type)) {
					result.put("deepDetail", json);
				} else if ("deepParking".equals(type)) {
					result.put("deepParking", json);
				} else if ("deepCarrental".equals(type)) {
					result.put("deepCarrental", json);
				}
			}
			JSONObject res = getKcLogFlag(result, taskId,deepTaskId, userId,isQuality);
			return res;
		} catch (Exception e){
			logger.error(e.getMessage());
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

	}
	
	/**
	 * 库存log统计接口变更-返回值增加是否有数据标记位
	 * @param result
	 * @param taskId
	 * @param userId
	 * @return
	 * @throws Exception 
	 */
	public JSONObject getKcLogFlag(JSONObject res,int taskId,int deepTaskId,long userId,Integer isQuality) throws Exception{
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select count(1) as num, c.first_work_item as type");
			sb.append("  from poi_column_status s, poi_column_workitem_conf c");
			sb.append(" where s.work_item_id = c.work_item_id");
			sb.append("  and c.first_work_item in");
			sb.append("  ('poi_name', 'poi_address', 'poi_englishname', 'poi_englishaddress')");
			sb.append("  and s.task_id = :1");
			sb.append("  and s.handler = :2");
			if(isQuality==0){//常规任务
				sb.append("	AND s.COMMON_HANDLER = "+userId);
			}else if(isQuality==1){//质检任务
				sb.append("	AND s.COMMON_HANDLER <> "+userId);
				sb.append("	AND s.QC_FLAG = 1");
			}
			sb.append(" GROUP BY c.first_work_item");
			sb.append(" union all");
			sb.append(" select count(1) as num, c1.second_work_item as type");
			sb.append("  from poi_column_status s1, poi_column_workitem_conf c1");
			sb.append(" where s1.work_item_id = c1.work_item_id");
			sb.append("  and c1.second_work_item in ('deepDetail', 'deepParking', 'deepCarrental')");
			sb.append("  and s1.task_id = :3");
			sb.append("  and s1.handler = :4");
			if(isQuality==0){//常规任务
				sb.append("	AND s1.COMMON_HANDLER = "+userId);
			}else if(isQuality==1){//质检任务
				sb.append("	AND s1.COMMON_HANDLER <> "+userId);
				sb.append("	AND s1.QC_FLAG = 1");
			}
			sb.append(" GROUP BY c1.second_work_item");

			pstmt = conn.prepareStatement(sb.toString());
			 
			pstmt.setInt(1, taskId);
			pstmt.setLong(2, userId);
			pstmt.setInt(3, deepTaskId);
			pstmt.setLong(4, userId);
			
			resultSet = pstmt.executeQuery();
			
			JSONObject result = res;
			
			while (resultSet.next()) {
				String type = resultSet.getString("type");
				int flag = 0;
				if (resultSet.getInt("num") != 0){
					flag = 1;
				}
				JSONObject json = result.getJSONObject(type);
				json.put("flag", flag);
				
				if ("poi_name".equals(type)) {
					result.put("poi_name", json);
				} else if ("poi_address".equals(type)) {
					result.put("poi_address", json);
				} else if ("poi_englishname".equals(type)) {
					result.put("poi_englishname", json);
				} else if ("poi_englishaddress".equals(type)) {
					result.put("poi_englishaddress", json);
				} else if ("deepDetail".equals(type)) {
					result.put("deepDetail", json);
				} else if ("deepParking".equals(type)) {
					result.put("deepParking", json);
				} else if ("deepCarrental".equals(type)) {
					result.put("deepCarrental", json);
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
	
	/**
	 * 根据传入质检子任务id，查询对应常规子任务下，已打质检标记qc_flag=1，且作业员非本人的作业员列表
	 * @param subtask
	 * @param status
	 * @param userid
	 * @param secondWorkItem
	 * @return
	 * @throws Exception
	 */
	public List<Long> getQueryWorkerList(int subtaskId ,long userid) throws Exception {
		List<Long> commonHandlerList = new ArrayList<Long>();
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT DISTINCT PS.COMMON_HANDLER");
		sb.append(" FROM POI_COLUMN_STATUS PS");
		sb.append(" WHERE PS.TASK_ID =  ?");
		sb.append(" AND PS.QC_FLAG = 1");
		sb.append(" AND PS.COMMON_HANDLER NOT IN (?, 0)");
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1,subtaskId);
			pstmt.setLong(2, userid);
			
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				commonHandlerList.add(resultSet.getLong("COMMON_HANDLER"));
			}
			
			return commonHandlerList;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 质检问题查询
	 * @param subtaskId
	 * @param pid
	 * @param firstWorkItem
	 * @param secondWorkItem
	 * @return
	 * @throws Exception
	 */
	public JSONArray queryQcProblem(int subtaskId ,Integer pid,String firstWorkItem,String secondWorkItem,String nameId,ManApi apiService) throws Exception {
		JSONArray jsonArray  = new JSONArray();
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT * FROM COLUMN_QC_PROBLEM");
		sb.append(" WHERE SUBTASK_ID = ?");
		sb.append(" AND IS_VALID = 0");
		if(pid!=null){
			sb.append(" AND PID = "+pid);
		}
		if(StringUtils.isNotEmpty(firstWorkItem)){
			sb.append(" AND FIRST_WORK_ITEM ='"+firstWorkItem+"'");
		}
		if(StringUtils.isNotEmpty(secondWorkItem)){
			sb.append(" AND SECOND_WORK_ITEM = '"+secondWorkItem+"'");
		}
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setInt(1,subtaskId);
			
			resultSet = pstmt.executeQuery();
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			boolean isNameIdNotEmpty = StringUtils.isNotEmpty(nameId);

			while (resultSet.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("pid", resultSet.getInt("PID"));
				jsonObject.put("firstWorkItem", resultSet.getString("FIRST_WORK_ITEM"));
				jsonObject.put("secondWorkItem", resultSet.getString("SECOND_WORK_ITEM"));
				jsonObject.put("subtaskId", resultSet.getInt("SUBTASK_ID"));
				jsonObject.put("workItemId", resultSet.getString("WORK_ITEM_ID"));
				
				
				
				String errorType = StringUtils.isEmpty(resultSet.getString("ERROR_TYPE"))?"":resultSet.getString("ERROR_TYPE");
				String errorLevel = StringUtils.isEmpty(resultSet.getString("ERROR_LEVEL"))?"":resultSet.getString("ERROR_LEVEL");
				String problemDesc = StringUtils.isEmpty(resultSet.getString("PROBLEM_DESC"))?"":resultSet.getString("PROBLEM_DESC");
				String techGuidance = StringUtils.isEmpty(resultSet.getString("TECH_GUIDANCE"))?"":resultSet.getString("TECH_GUIDANCE");
				String techScheme = StringUtils.isEmpty(resultSet.getString("TECH_SCHEME"))?"":resultSet.getString("TECH_SCHEME");
				String isProblem = StringUtils.isEmpty(resultSet.getString("IS_PROBLEM"))?"":resultSet.getString("IS_PROBLEM");
				String oldValue = StringUtils.isEmpty(resultSet.getString("OLD_VALUE"))?"":resultSet.getString("OLD_VALUE");
				String newValue = StringUtils.isEmpty(resultSet.getString("NEW_VALUE"))?"":resultSet.getString("NEW_VALUE");
				
				
				if(isNameIdNotEmpty){
					if(StringUtils.isEmpty(errorType)){
						errorType = "";
					}else{
						errorType = searchValueByNameId(errorType, nameId);
					}
					if(StringUtils.isEmpty(errorLevel)){
						errorLevel = "";
					}else{
						errorLevel = searchValueByNameId(errorLevel, nameId);
					}
					if(StringUtils.isEmpty(problemDesc)){
						problemDesc = "";
					}else{
						problemDesc = searchValueByNameId(problemDesc, nameId);
					}
					if(StringUtils.isEmpty(techGuidance)){
						techGuidance = "";
					}else{
						techGuidance = searchValueByNameId(techGuidance, nameId);
					}
					if(StringUtils.isEmpty(techScheme)){
						techScheme = "";
					}else{
						techScheme = searchValueByNameId(techScheme, nameId);
					}
					if(StringUtils.isEmpty(isProblem)){
						isProblem = "";
					}else{
						isProblem = searchValueByNameId(isProblem, nameId);
					}
					if(StringUtils.isEmpty(oldValue)){
						oldValue = "";
					}else{
						oldValue = searchValueByNameId(oldValue, nameId);
					}
					if(StringUtils.isEmpty(newValue)){
						newValue = "";
					}else{
						newValue = searchValueByNameId(newValue, nameId);
					}
				
				}
				
				
				jsonObject.put("errorType",errorType);
				jsonObject.put("errorLevel", errorLevel);
				jsonObject.put("problemDesc",problemDesc);
				jsonObject.put("techGuidance",techGuidance);
				jsonObject.put("techScheme",techScheme);
				jsonObject.put("isProblem",isProblem);
				jsonObject.put("oldValue",oldValue);
				jsonObject.put("newValue",newValue);
			
				
				
				jsonObject.put("workTime",resultSet.getTimestamp("WORK_TIME")==null?"":
								sdf.format(resultSet.getTimestamp("WORK_TIME")));
				jsonObject.put("qcTime",resultSet.getTimestamp("QC_TIME")==null?"":
								sdf.format(resultSet.getTimestamp("QC_TIME")));
				
				
				jsonObject.put("isValid", resultSet.getInt("IS_VALID"));
				
				UserInfo worker  = apiService.getUserInfoByUserId(resultSet.getInt("WORKER"));
				UserInfo qcWorker  = apiService.getUserInfoByUserId(resultSet.getInt("QC_WORKER"));
				
				jsonObject.put("worker", worker.getUserRealName()+resultSet.getInt("WORKER"));
				jsonObject.put("qcWorker",qcWorker.getUserRealName()+resultSet.getInt("QC_WORKER"));
				jsonObject.put("originalInfo", StringUtils.isEmpty(resultSet.getString("ORIGINAL_INFO"))?"":resultSet.getString("ORIGINAL_INFO"));
				jsonArray.add(jsonObject);
			}
			
			return jsonArray;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	
	/**
	 * 保存质检问题
	 * @param pid
	 * @param firstWorkItem
	 * @param secondWorkItem
	 * @param errorType
	 * @param errorLevel
	 * @param problemDesc
	 * @param techGuidance
	 * @param techScheme
	 * @param subtaskId
	 * @return
	 * @throws Exception
	 */
	public JSONObject saveQcProblem(Integer pid, String firstWorkItem, String secondWorkItem, String errorType,
			String errorLevel, String problemDesc,String techGuidance,String techScheme,Integer subtaskId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" UPDATE COLUMN_QC_PROBLEM c SET c.is_problem = '2', c.error_type = :1");
		sb.append(" ,c.error_level = :2 , c.problem_desc= :3 , c.tech_guidance = :4");
		sb.append(" , c.tech_scheme = :5 WHERE c.SUBTASK_ID = :6");
		sb.append(" AND c.IS_VALID = 0 AND c.pid = :7");
		sb.append(" AND c.first_work_item = :8  AND c.second_work_item = :9");
		
		PreparedStatement pstmt = null;

		try {

			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setString(1, errorType);
			pstmt.setString(2, errorLevel);
			pstmt.setString(3, problemDesc);
			pstmt.setString(4, techGuidance);
			pstmt.setString(5, techScheme);
			
			pstmt.setInt(6, subtaskId);
			pstmt.setInt(7, pid);
			pstmt.setString(8, firstWorkItem);
			pstmt.setString(9, secondWorkItem);
			
			int count =  pstmt.executeUpdate();

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("count", count);
			return jsonObject;
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
	}

	/**
	 * 针对拼音类，保存质检问题
	 * @param pid
	 * @param firstWorkItem
	 * @param secondWorkItem
	 * @param errorType
	 * @param errorLevel
	 * @param problemDesc
	 * @param techGuidance
	 * @param techScheme
	 * @param subtaskId
	 * @param jsonObject
	 * @param nameId
	 * @return
	 * @throws Exception
	 */
	public JSONObject saveQcProblemWithPinYin(Integer pid, String firstWorkItem, String secondWorkItem,
			String errorType, String errorLevel, String problemDesc, String techGuidance, String techScheme,
			Integer subtaskId, JSONObject jsonObject,String nameId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" UPDATE COLUMN_QC_PROBLEM c SET c.error_type = :1");
		sb.append(" ,c.error_level = :2 , c.problem_desc= :3 , c.tech_guidance = :4");
		sb.append(" , c.tech_scheme = :5 ,c.is_problem = :6 WHERE c.SUBTASK_ID = :7");
		sb.append(" AND c.IS_VALID = 0 AND c.pid = :8");
		sb.append(" AND c.first_work_item = :9  AND c.second_work_item = :10");
		
		PreparedStatement pstmt = null;

		try {

			pstmt = conn.prepareStatement(sb.toString());

			String oldErrorType = jsonObject.getString("errorType");
			String oldErrorLevel = jsonObject.getString("errorLevel");
			String oldProblemDesc = jsonObject.getString("problemDesc");
			String oldTechGuidance = jsonObject.getString("techGuidance");
			String oldTechScheme = jsonObject.getString("techScheme");
			String oldIsProblem = jsonObject.getString("isProblem");
			
			if(StringUtils.isNotEmpty(oldErrorType)){
				if(oldErrorType.contains(nameId)){
					pstmt.setString(1, replaceOldValueToNewWithPinYin(oldErrorType, nameId, errorType));				
				}else{
					pstmt.setString(1, oldErrorType+"|"+nameId+":"+errorType);
				}
			}else{
				pstmt.setString(1, nameId+":"+errorType);
			}
			
			if(StringUtils.isNotEmpty(oldErrorLevel)){
				if(oldErrorLevel.contains(nameId)){
					pstmt.setString(2, replaceOldValueToNewWithPinYin(oldErrorLevel, nameId, errorLevel));				
				}else{
					pstmt.setString(2, oldErrorLevel+"|"+nameId+":"+errorLevel);
				}
			}else{
				pstmt.setString(2, nameId+":"+errorLevel);
			}
			
			if(StringUtils.isNotEmpty(oldProblemDesc)){
				if(oldProblemDesc.contains(nameId)){
					pstmt.setString(3, replaceOldValueToNewWithPinYin(oldProblemDesc, nameId, problemDesc));				
				}else{
					pstmt.setString(3, oldProblemDesc+"|"+nameId+":"+problemDesc);
				}
			}else{
				pstmt.setString(3, nameId+":"+problemDesc);
			}
			
			if(StringUtils.isNotEmpty(oldTechGuidance)){
				if(oldTechGuidance.contains(nameId)){
					pstmt.setString(4, replaceOldValueToNewWithPinYin(oldTechGuidance, nameId, techGuidance));				
				}else{
					pstmt.setString(4, oldTechGuidance+"|"+nameId+":"+techGuidance);
				}
			}else{
				pstmt.setString(4, nameId+":"+techGuidance);
			}
			
			if(StringUtils.isNotEmpty(oldTechScheme)){
				if(oldTechScheme.contains(nameId)){
					pstmt.setString(5, replaceOldValueToNewWithPinYin(oldTechScheme, nameId, techScheme));				
				}else{
					pstmt.setString(5, oldTechScheme+"|"+nameId+":"+techScheme);
				}
			}else{
				pstmt.setString(5, nameId+":"+techScheme);
			}
			
			if(StringUtils.isNotEmpty(oldIsProblem)){
				if(oldIsProblem.contains(nameId)){
					pstmt.setString(6, replaceOldValueToNewWithPinYin(oldIsProblem, nameId, "2"));				
				}else{
					pstmt.setString(6, oldIsProblem+"|"+nameId+":"+"2");
				}
			}else{
				pstmt.setString(6, nameId+":2");
			}
			
			pstmt.setInt(7, subtaskId);
			pstmt.setInt(8, pid);
			pstmt.setString(9, firstWorkItem);
			pstmt.setString(10, secondWorkItem);
			
			int count =  pstmt.executeUpdate();

			JSONObject jsonObject1 = new JSONObject();
			jsonObject1.put("count", count);
			return jsonObject1;
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	
	/**
	 * 针对拼音类，把column_qc_problem中字段的旧值替换成新值
	 */
	public String replaceOldValueToNewWithPinYin(String oldTotalVal,String nameId,String newReplaceVal){
		String[] oldValArray = oldTotalVal.split("\\|");
		for (String oldVal : oldValArray) {
			String newVal = "";
			if(oldVal.contains(nameId)){
				newVal = nameId+":"+newReplaceVal;
				oldTotalVal = oldTotalVal.replace(oldVal, newVal);
				return oldTotalVal;
			}
		}
		return oldTotalVal;
	}
	
	
	/**
	 * 针对拼音类，通过NameId查询column_qc_problem中5个字段
	 */
	public String searchValueByNameId(String totalVal,String nameId){
		String[] valArray = totalVal.split("\\|");
		for (String val : valArray) {
			if(val.contains(nameId)){
				return val.substring(val.indexOf(":")+1,val.length());
			}
		}
		return "";
	}

	/**
	 * 获取抽检的pid
	 * @param subtask
	 * @param firstWorkItem
	 * @param secondWorkItem
	 * @param type
	 * @param userId
	 * @return
	 * @throws Exception 
	 */
	public List<Integer> getExtractPids(Subtask subtask, String firstWorkItem, String secondWorkItem, int type, long userId) throws Exception {
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT s.pid");
		sb.append(" FROM POI_COLUMN_STATUS s, POI_COLUMN_WORKITEM_CONF w, IX_POI p");
		sb.append(" WHERE s.work_item_id = w.work_item_id");
		sb.append(" AND s.pid = p.pid");
		sb.append(" AND s.first_work_status = 1 AND s.second_work_status = 1 AND s.handler = 0");
		sb.append(" AND w.type = :1");
		sb.append(" AND s.task_id = :2");
		sb.append(" AND w.first_work_item = :3");
		sb.append(" AND w.second_work_item = :4");
		sb.append(" AND s.qc_flag = 1 ");
		sb.append(" AND s.common_handler <> :5 ");

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			logger.info("getExtractPids sql:"+sb);
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.setInt(1, type);
			pstmt.setInt(2,subtask.getSubtaskId());
			pstmt.setString(3,firstWorkItem);
			pstmt.setString(4,secondWorkItem);
			pstmt.setLong(5,userId);

			resultSet = pstmt.executeQuery();

			List<Integer> pids = new ArrayList<Integer>();

			while (resultSet.next()) {
				int pid = resultSet.getInt("pid");
				pids.add(pid);
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
	 * 抽取数据是调用，更新ColumnStatus状态
	 * @param pids
	 * @param userId
	 * @param taskId
	 * @param timeStamp
	 * @throws Exception
	 */
	public void updateExtractColumnStatus(List<Integer> pids, long userId, int taskId, Timestamp timeStamp) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" UPDATE POI_COLUMN_STATUS SET handler=:1,task_id=:2,apply_date=:3 WHERE ");
		sb.append(" PID IN ("+org.apache.commons.lang.StringUtils.join(pids, ",")+")");

		PreparedStatement pstmt = null;

		try {

			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setLong(1, userId);
			pstmt.setInt(2, taskId);
			pstmt.setTimestamp(3, timeStamp);

			pstmt.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
		
	}

	
	
}
