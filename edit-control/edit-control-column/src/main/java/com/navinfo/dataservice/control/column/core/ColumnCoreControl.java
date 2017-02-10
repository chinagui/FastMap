package com.navinfo.dataservice.control.column.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.search.IxPoiSearch;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiColumnStatusSelector;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 精编业务处理类
 * 
 * @author wangdongbin
 *
 */
public class ColumnCoreControl {

	public int applyData(JSONObject jsonReq, long userId) throws Exception {
		// TODO
		Connection conn = null;
		try {

			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");

			int taskId = jsonReq.getInt("taskId");
			Subtask subtask = apiService.queryBySubtaskId(taskId);

			int dbId = subtask.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);

			int totalCount = 0;
			int hasApply = 0;
			
			// 默认type为1常规大陆
			int type = 1;

			IxPoiColumnStatusSelector columnSelector = new IxPoiColumnStatusSelector(conn);

			String firstWorkItem = jsonReq.getString("firstWorkItem");
			if (StringUtils.isEmpty(firstWorkItem)){
				throw new Exception("一级作业项不能为空，请确认");
			}
			String secondWorkItem = jsonReq.getString("secondWorkItem");

			// 月编专项作业和后期专项
			List<String> columnFirstWorkItems = Arrays.asList("poi_name", "poi_address", "poi_englishname",
					"poi_englishaddress", "poi_postwork");
			List<String> columnSecondWorkItems = Arrays.asList("postViewLevel", "postAirportCode", "postImportance",
					"postAdminReal", "");

			if (columnFirstWorkItems.contains(firstWorkItem)) {

				if (columnSecondWorkItems.contains(secondWorkItem)) {

					hasApply = columnSelector.queryHandlerCount(firstWorkItem, secondWorkItem, userId, type);
					// 可申请数据条数
					int canApply = 100 - hasApply;
					if (canApply == 0) {
						throw new Exception("该作业员名下已存在100条数据，不可继续申请");
					}

					// 申请数据
					List<Integer> pids = columnSelector.getApplyPids(subtask, firstWorkItem, secondWorkItem, type);
					if (pids.size() == 0) {
						// 库中未查到可以申请的数据，返回0
						return 0;
					}

					// 实际申请到的数据pids
					List<Integer> applyDataPids = new ArrayList<Integer>();
					if (pids.size() >= canApply) {
						applyDataPids = pids.subList(0, canApply);
					} else {
						// 库里面查询出的数据量小于当前用户可申请的量，即锁定库中查询出的数据
						applyDataPids = pids;
					}

					// 数据加锁， 赋值handler，task_id,apply_date
					Timestamp timeStamp = new Timestamp(new Date().getTime());
					List<String> workItemIds = columnSelector.getWorkItemIds(firstWorkItem, secondWorkItem);
					columnSelector.dataSetLock(applyDataPids, workItemIds, userId, taskId, timeStamp);
					totalCount += applyDataPids.size();
				}
			}

			// 行政区划关联要素作业, 点位调整作业
			List<String> locSecondWorkItems = Arrays.asList("locationIcon", "locationLandMark");
			if (("poi_postwork".equals(firstWorkItem) && "postAdminArea".equals(secondWorkItem))
					|| ("poi_location".equals(firstWorkItem) && locSecondWorkItems.contains(secondWorkItem))) {
				// 申请数据
				// TODO
			}

			return totalCount;

		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.commitAndClose(conn);
		}
	}

	/**
	 * 作业数据查询
	 * 
	 * @param userId
	 * @param jsonReq
	 * @return
	 * @throws Exception
	 */
//	public JSONArray columnQuery(long userId, JSONObject jsonReq) throws Exception {
//
//		Connection conn = null;
//
//		try {
//			// int taskId= jsonReq.getInt("taskId");
//
//			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
//
//			String type = jsonReq.getString("type");
//			int status = jsonReq.getInt("status");
//			String firstWordItem = jsonReq.getString("firstWorkItem");
//			String secondWorkItem = jsonReq.getString("secondWorkItem");
//
//			// Subtask subtask = apiService.queryBySubtaskId(taskId);
//			// int dbId = subtask.getDbId();
//
//			// 获取未提交数据的rowId
//			conn = DBConnector.getInstance().getConnectionById(17);
//			IxPoiColumnStatusSelector selector = new IxPoiColumnStatusSelector(conn);
//			List<String> rowIdList = selector.columnQuery(status, secondWorkItem, userId);
//
//			IxPoiSearch poiSearch = new IxPoiSearch(conn);
//
//			JSONArray datas = poiSearch.searchColumnPoiByRowId(firstWordItem, secondWorkItem, rowIdList, type, "CHI");
//
//			return datas;
//		} catch (Exception e) {
//			throw e;
//		} finally {
//			DbUtils.closeQuietly(conn);
//		}
//	}
	/**
	 * 作业数据查询
	 * 
	 * @param userId
	 * @param jsonReq
	 * @return
	 * @throws Exception
	 */
	public JSONObject columnQuery(long userId, JSONObject jsonReq) throws Exception {

		Connection conn = null;
		JSONObject result = new JSONObject();

		try {
			// int taskId= jsonReq.getInt("taskId");

			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");

			int status = jsonReq.getInt("status");
			String firstWordItem = jsonReq.getString("firstWorkItem");
			String secondWorkItem = jsonReq.getString("secondWorkItem");
			int taskId = jsonReq.getInt("taskId");
//			int pageSize = jsonReq.getInt("pageSize");
//			int pageNo = jsonReq.getInt("pageNo");
			
//			int startRow = (pageNo - 1) * pageSize + 1;
//			int endRow = pageNo * pageSize;

			Subtask subtask = apiService.queryBySubtaskId(taskId);
			int dbId = subtask.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
			IxPoiColumnStatusSelector selector = new IxPoiColumnStatusSelector(conn);
			// 获取未提交数据的pid以及总数
			JSONObject data= selector.columnQuery(status, secondWorkItem, userId,taskId);
			List<Integer> pidList =new ArrayList<Integer>();
			if(data.get("pidList") instanceof List){ 
				pidList = (List) data.get("pidList"); 
			}
			int total =(Integer) data.get("total");
			JSONArray datas=new JSONArray();
			if (total==0){
				result.put("total", 0);
				result.put("rows", datas);
				return result;
			}
			//获取数据详细字段
			JSONObject classifyRules= selector.queryClassifyByPidSecondWorkItem(pidList,secondWorkItem,status,userId);
			JSONObject ckRules= selector.queryCKLogByPidfirstWorkItem(pidList,firstWordItem,secondWorkItem,"IX_POI");
			IxPoiSearch poiSearch = new IxPoiSearch(conn);
			datas = poiSearch.searchColumnPoiByPid(firstWordItem, secondWorkItem, pidList,userId,status,classifyRules,ckRules);

			result.put("total", total);
			result.put("rows", datas);
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	/**
	 * 精编任务的统计查询
	 * 
	 * @param jsonReq
	 * @return
	 * @throws Exception
	 */
	public JSONObject taskStatistics(JSONObject jsonReq) throws Exception {

		Connection conn = null;

		try {
			int taskId = jsonReq.getInt("taskId");

			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			int dbId = subtask.getDbId();

			conn = DBConnector.getInstance().getConnectionById(dbId);

			IxPoiColumnStatusSelector ixPoiColumnStatusSelector = new IxPoiColumnStatusSelector(conn);

			return ixPoiColumnStatusSelector.taskStatistics(taskId);
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	/**
	 * 查询二级作业项的统计信息
	 * 
	 * @param jsonReq
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public JSONObject secondWorkStatistics(JSONObject jsonReq, long userId) throws Exception {

		Connection conn = null;

		try {
			int taskId = jsonReq.getInt("taskId");
			String firstWorkItem = jsonReq.getString("firstWorkItem");
			// 默认为大陆
			int type = 1;

			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			int dbId = subtask.getDbId();

			conn = DBConnector.getInstance().getConnectionById(dbId);

			IxPoiColumnStatusSelector ixPoiColumnStatusSelector = new IxPoiColumnStatusSelector(conn);

			return ixPoiColumnStatusSelector.secondWorkStatistics(firstWorkItem, userId, type, taskId);
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	/**
	 * 根据作业组，查询精编任务列表
	 * 
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	public List<Object> queryTaskList(long userId, JSONObject jsonReq) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			// 持久化
			int type = jsonReq.getInt("type");
			// 获取用户组下所有区域库及子任务
			List<Object> subtaskList = new ArrayList<Object>();
			Map<Integer, Map<Integer, String>> dbIdAndSubtaskInfo = getDbIdAndSubtaskListByUser(userId, type);

			// 获取所有子任务列表信息
			Iterator<Entry<Integer, Map<Integer, String>>> iter = dbIdAndSubtaskInfo.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Integer, Map<Integer, String>> entry = iter.next();
				int dbId = (int) entry.getKey();
				Map<Integer, String> subtasks = entry.getValue();

				// 获取区域库下子任务信息
				List<Object> subtasksWithItems = getSubtaskInfoList(dbId, subtasks);
				// subtaskList.add(subtasksWithItems);
				subtaskList.addAll(subtasksWithItems);
			}
			return subtaskList;

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("查询失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}

	/**
	 * @param dbId
	 * @param subtasks
	 * @return
	 * @throws ServiceException
	 */
	private List<Object> getSubtaskInfoList(int dbId, Map<Integer, String> subtasks) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			Set<Integer> subtaskIds = subtasks.keySet();
			String taskIds = "(" + StringUtils.join(subtaskIds.toArray(), ",") + ")";
			// 查询作业信息
			conn = DBConnector.getInstance().getConnectionById(dbId);
			String sql = "SELECT PDS.TASK_ID, PDW.FIRST_WORK_ITEM,COUNT(PDS.ROW_ID) AS NUM"
					+ " FROM POI_COLUMN_STATUS PDS, POI_COLUMN_WORKITEM_CONF PDW" + " WHERE PDS.TASK_ID IN " + taskIds
					+ " AND PDS.WORK_ITEM_ID = PDW.WORK_ITEM_ID" + " GROUP BY PDS.TASK_ID, PDW.FIRST_WORK_ITEM";

			ResultSetHandler<Map<Integer, Object>> rsHandler = new ResultSetHandler<Map<Integer, Object>>() {
				public Map<Integer, Object> handle(ResultSet rs) throws SQLException {
					Map<Integer, Object> result = new HashMap<Integer, Object>();
					while (rs.next()) {
						int subtaskId = rs.getInt("TASK_ID");
						String firstWorkItem = rs.getString("FIRST_WORK_ITEM");
						int num = rs.getInt("NUM");
						if (result.containsKey(subtaskId)) {
							Map<String, Integer> firstWorkItems = (Map<String, Integer>) result.get(subtaskId);
							firstWorkItems.put(firstWorkItem, num);
							result.put(subtaskId, firstWorkItems);
						} else {
							Map<String, Integer> firstWorkItems = new HashMap<String, Integer>();
							firstWorkItems.put(firstWorkItem, num);
							result.put(subtaskId, firstWorkItems);
						}
					}
					return result;
				}
			};

			Map<?, ?> temp = run.query(conn, sql, rsHandler);
			List<Object> result = new ArrayList<Object>();
			Iterator<?> iter = temp.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				int subtaskId = (int) entry.getKey();
				Map<String, Integer> firstWorkItems = (Map<String, Integer>) entry.getValue();
				String name = (String) subtasks.get(subtaskId);
				Map<String, Object> subtask = new HashMap<String, Object>();
				subtask.put("subtaskId", subtaskId);
				subtask.put("name", name);
				subtask.put("dbId", dbId);
				subtask.put("firstWorkItems", firstWorkItems);
				result.add(subtask);
			}
			return result;
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("查询失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 根据用户获取用户组下精编子任务列表及精编库
	 * 
	 * @param userId
	 * @return
	 * @throws ServiceException
	 */
	private Map<Integer, Map<Integer, String>> getDbIdAndSubtaskListByUser(long userId, int type)
			throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			// 用户所在组
			String temp = "(SELECT UG.GROUP_ID" + " FROM USER_GROUP UG, GROUP_USER_MAPPING GUM"
					+ " WHERE UG.GROUP_ID = GUM.GROUP_ID" + " AND UG.GROUP_TYPE = 2" + " AND GUM.USER_ID = " + userId
					+ ")";

			String blockSql = "SELECT S.SUBTASK_ID, S.NAME,R.MONTHLY_DB_ID FROM BLOCK_MAN BM, SUBTASK S, REGION R, BLOCK B"
					+ "," + temp + "TEMP" + " WHERE BM.MONTH_EDIT_GROUP_ID = TEMP.GROUP_ID"
					+ " AND BM.BLOCK_MAN_ID = S.BLOCK_MAN_ID" + " AND BM.LATEST = 1" + " AND BM.BLOCK_ID = B.BLOCK_ID"
					+ " AND B.REGION_ID = R.REGION_ID" + " AND S.TYPE = " + type;

			String taskSql = "SELECT S.SUBTASK_ID, S.NAME,R.MONTHLY_DB_ID FROM TASK T,SUBTASK S,REGION R,CITY C" + ","
					+ temp + "TEMP" + " WHERE T.MONTH_EDIT_GROUP_ID = TEMP.GROUP_ID" + " AND T.TASK_ID = S.TASK_ID"
					+ " AND T.LATEST = 1" + " AND T.CITY_ID = C.CITY_ID" + " AND C.REGION_ID = R.REGION_ID"
					+ " AND S.TYPE = " + type;
			String querySql = blockSql + " union " + taskSql;

			ResultSetHandler<Map<Integer, Map<Integer, String>>> rsHandler = new ResultSetHandler<Map<Integer, Map<Integer, String>>>() {
				public Map<Integer, Map<Integer, String>> handle(ResultSet rs) throws SQLException {
					Map<Integer, Map<Integer, String>> result = new HashMap<Integer, Map<Integer, String>>();
					while (rs.next()) {
						int dbId = rs.getInt("MONTHLY_DB_ID");
						int subtaskId = rs.getInt("SUBTASK_ID");
						String name = rs.getString("NAME");
						if (result.containsKey(dbId)) {
							Map<Integer, String> subtasks = result.get(dbId);
							subtasks.put(subtaskId, name);
							result.put(dbId, subtasks);
						} else {
							Map<Integer, String> subtasks = new HashMap<Integer, String>();
							subtasks.put(subtaskId, name);
							result.put(dbId, subtasks);
						}
					}
					return result;
				}
			};

			return run.query(conn, querySql, rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException("查询失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	
	/**
	 * 月编专项获取库存总量
	 * @param subtaskId
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public JSONObject getLogCount(int subtaskId, long userId) throws Exception {
		Connection conn = null;
		try {
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			
			if (subtask == null) {
				throw new Exception("subtaskid未找到数据");
			}
			
			int dbId = subtask.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			IxPoiColumnStatusSelector columnStatusSelector = new IxPoiColumnStatusSelector(conn);
			JSONObject result = columnStatusSelector.getColumnCount(subtask, userId);
			
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
