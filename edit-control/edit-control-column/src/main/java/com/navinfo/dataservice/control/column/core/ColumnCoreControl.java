package com.navinfo.dataservice.control.column.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
 * @author wangdongbin
 *
 */
public class ColumnCoreControl {
	
	public void applyData(int groupId,String firstWorkItem,long userId) throws Exception{
		// 根据组号，查出子任务号
		List<Integer> subTaskIds = new ArrayList<Integer>();
		// TODO
		Connection conn = null;
		try {
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			int oldDbId = 0;
			int totalCount = 0;
			int hasApply = 0;
			
			// 查询该作业员名下已申请的数据数量
//			for (int taskId:subTaskIds) {
				
				// 区分大陆，港澳任务
				
//				Subtask subtask = apiService.queryBySubtaskId(taskId);
//				int dbId = subtask.getDbId();
//				if (dbId != oldDbId) {
//					DbUtils.closeQuietly(conn);
//					oldDbId = dbId;
			conn = DBConnector.getInstance().getConnectionById(17);
//				}
			IxPoiColumnStatusSelector columnSelector = new IxPoiColumnStatusSelector(conn);
			int tempCount = columnSelector.queryHandlerCount(firstWorkItem,userId);
			hasApply += tempCount;
//			}
			
			// 可申请数据条数
			int canApply = 100 - hasApply;
			
			if (canApply == 0) {
				throw new Exception("该作业员名下已存在100条数据，不可继续申请");
			}
			
			// 查询可申请数据
//			for (int taskId:subTaskIds) {
//				Subtask subtask = apiService.queryBySubtaskId(taskId);
//				int dbId = subtask.getDbId();
//				if (dbId != oldDbId) {
//					DbUtils.closeQuietly(conn);
//					oldDbId = dbId;
//					conn = DBConnector.getInstance().getConnectionById(oldDbId);
//				}
				IxPoiColumnStatusSelector selector = new IxPoiColumnStatusSelector(conn);
				List<String> rowIds = selector.getRowIdByTaskId(20160906, firstWorkItem);
				
				// 判断是否已申请够
				if (totalCount < canApply) {
					int leftCount = canApply - totalCount;
					if (rowIds.size()>leftCount) {
						List<String> subList = rowIds.subList(0, leftCount-1);
						// 申请数据
						updateHandler(subList,userId,conn);
						totalCount += subList.size();
					} else if (rowIds.size() > 0){
						// 申请数据
						updateHandler(rowIds,userId,conn);
						totalCount += rowIds.size();
					}
				} else {
//					break;
				}
//			}

		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.commitAndClose(conn);
		}
	}
	
	/**
	 * 更新handler
	 * @param rowIds
	 * @param conn
	 * @throws Exception
	 */
	public void updateHandler(List<String> rowIds,Long userId,Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE poi_deep_status SET handler=:1 WHERE row_id in (");
		String temp = "";
		for (String rowId:rowIds) {
			sb.append(temp);
			sb.append("'"+rowId+"'");
			temp = ",";
		}
		sb.append(")");
		
		PreparedStatement pstmt = null;

		try {
			
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setLong(1, userId);
			
			pstmt.execute();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt); 
		}
	}
	
	/**
	 * 作业数据查询
	 * @param userId
	 * @param jsonReq
	 * @return
	 * @throws Exception
	 */
	public JSONArray columnQuery(long userId ,JSONObject jsonReq) throws Exception {
		
		Connection conn = null;
		
		try {
//			int taskId= jsonReq.getInt("taskId");
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			
			String type = jsonReq.getString("type");
			int status = jsonReq.getInt("status");
			String firstWordItem = jsonReq.getString("firstWorkItem");
			String secondWorkItem = jsonReq.getString("secondWorkItem");
			
//			Subtask subtask = apiService.queryBySubtaskId(taskId);
//			int dbId = subtask.getDbId();
			
			// 获取未提交数据的rowId
			conn = DBConnector.getInstance().getConnectionById(17);
			IxPoiColumnStatusSelector selector = new IxPoiColumnStatusSelector(conn);
			List<String> rowIdList = selector.columnQuery(status,secondWorkItem,userId);
			
			IxPoiSearch poiSearch = new IxPoiSearch(conn);
			
			JSONArray datas = poiSearch.searchColumnPoiByRowId(firstWordItem, secondWorkItem,rowIdList, type, "CHI");
			
			return datas;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * 精编任务的统计查询
	 * @param jsonReq
	 * @return
	 * @throws Exception
	 */
	public JSONObject taskStatistics(JSONObject jsonReq)  throws Exception {
		
		Connection conn = null;
		
		try {
			int taskId = jsonReq.getInt("taskId");
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
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
	 * @param jsonReq
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public JSONObject secondWorkStatistics(JSONObject jsonReq,long userId)  throws Exception {
		
		Connection conn = null;
		
		try {
			int taskId = jsonReq.getInt("taskId");
			String firstWorkItem = jsonReq.getString("firstWorkItem");
			int type = jsonReq.getInt("taskType");
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			int dbId = subtask.getDbId();
			
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			IxPoiColumnStatusSelector ixPoiColumnStatusSelector = new IxPoiColumnStatusSelector(conn);
			
			return ixPoiColumnStatusSelector.secondWorkStatistics(firstWorkItem, userId, type);
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	

	/**
	 * 根据作业组，查询精编任务列表
	 * @param userId
	 * @return
	 * @throws ServiceException 
	 */
	public List<Object> queryTaskList(long userId,JSONObject jsonReq) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			// 持久化
			int type = jsonReq.getInt("type");
			//获取用户组下所有区域库及子任务
			List<Object> subtaskList = new ArrayList<Object>();
			Map<Integer, Map<Integer, String>> dbIdAndSubtaskInfo = getDbIdAndSubtaskListByUser(userId,type);
			
			//获取所有子任务列表信息
			Iterator<Entry<Integer, Map<Integer, String>>> iter = dbIdAndSubtaskInfo.entrySet().iterator();
			while (iter.hasNext()){
				Entry<Integer, Map<Integer, String>> entry = iter.next();
				int dbId = (int) entry.getKey();
				Map<Integer, String> subtasks =  entry.getValue();
				
				//获取区域库下子任务信息
				List<Object> subtasksWithItems = getSubtaskInfoList(dbId,subtasks);
//				subtaskList.add(subtasksWithItems);
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
	private List<Object> getSubtaskInfoList(int dbId,Map<Integer, String> subtasks) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			Set<Integer> subtaskIds = subtasks.keySet();
			String taskIds = "(" + StringUtils.join(subtaskIds.toArray(),",") + ")";
			//查询作业信息
			conn = DBConnector.getInstance().getConnectionById(dbId);
			String sql = "SELECT PDS.TASK_ID, PDW.FIRST_WORK_ITEM,COUNT(PDS.ROW_ID) AS NUM"
					+ " FROM POI_DEEP_STATUS PDS, POI_DEEP_WORKITEM_CONF PDW"
					+ " WHERE PDS.TASK_ID IN " + taskIds
					+ " AND PDS.WORK_ITEM_ID = PDW.WORK_ITEM_ID"
					+ " GROUP BY PDS.TASK_ID, PDW.FIRST_WORK_ITEM";
	
			ResultSetHandler<Map<Integer,Object>> rsHandler = new ResultSetHandler<Map<Integer,Object>>() {
				public Map<Integer,Object> handle(ResultSet rs) throws SQLException {
					Map<Integer,Object> result = new HashMap<Integer,Object>();
					while (rs.next()) {
						int subtaskId = rs.getInt("TASK_ID");
						String firstWorkItem = rs.getString("FIRST_WORK_ITEM");
						int num = rs.getInt("NUM");
						if(result.containsKey(subtaskId)){
							Map<String,Integer> firstWorkItems =  (Map<String,Integer>) result.get(subtaskId);
							firstWorkItems.put(firstWorkItem,num);
							result.put(subtaskId, firstWorkItems);
						}else{
							Map<String,Integer> firstWorkItems = new HashMap<String,Integer>();
							firstWorkItems.put(firstWorkItem,num);
							result.put(subtaskId, firstWorkItems);
						}
					}
					return result;
				}
			};
			
			Map<?, ?> temp = run.query(conn, sql, rsHandler);
			List<Object> result = new ArrayList<Object>();
			Iterator<?> iter = temp.entrySet().iterator();
			while (iter.hasNext()){
				Map.Entry entry = (Map.Entry) iter.next();
				int subtaskId = (int) entry.getKey();
				Map<String,Integer> firstWorkItems = (Map<String,Integer>) entry.getValue();
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
	 * @param userId
	 * @return
	 * @throws ServiceException 
	 */
	private Map<Integer, Map<Integer, String>> getDbIdAndSubtaskListByUser(long userId,int type) throws ServiceException {
		// TODO Auto-generated method stub
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			//用户所在组
			String temp = "(SELECT UG.GROUP_ID"
					+ " FROM USER_GROUP UG, GROUP_USER_MAPPING GUM"
					+ " WHERE UG.GROUP_ID = GUM.GROUP_ID"
					+ " AND UG.GROUP_TYPE = 2"
					+ " AND GUM.USER_ID = " + userId + ")";
	
			String blockSql = "SELECT S.SUBTASK_ID, S.NAME,R.MONTHLY_DB_ID FROM BLOCK_MAN BM, SUBTASK S, REGION R, BLOCK B"
					+ "," + temp + "TEMP"
					+ " WHERE BM.MONTH_EDIT_GROUP_ID = TEMP.GROUP_ID"
					+ " AND BM.BLOCK_MAN_ID = S.BLOCK_MAN_ID"
					+ " AND BM.LATEST = 1"
					+ " AND BM.BLOCK_ID = B.BLOCK_ID"
					+ " AND B.REGION_ID = R.REGION_ID"
					+ " AND S.TYPE = " + type;
			
			String taskSql = "SELECT S.SUBTASK_ID, S.NAME,R.MONTHLY_DB_ID FROM TASK T,SUBTASK S,REGION R,CITY C"
					+ "," + temp + "TEMP"
					+ " WHERE T.MONTH_EDIT_GROUP_ID = TEMP.GROUP_ID"
					+ " AND T.TASK_ID = S.TASK_ID"
					+ " AND T.LATEST = 1"
					+ " AND T.CITY_ID = C.CITY_ID"
					+ " AND C.REGION_ID = R.REGION_ID"
					+ " AND S.TYPE = " + type;
			String querySql = blockSql + " union " + taskSql;
	
			ResultSetHandler<Map<Integer, Map<Integer, String>>> rsHandler = new ResultSetHandler<Map<Integer, Map<Integer, String>>>() {
				public Map<Integer, Map<Integer, String>> handle(ResultSet rs) throws SQLException {
					Map<Integer, Map<Integer, String>> result = new HashMap<Integer, Map<Integer, String>>();
					while (rs.next()) {
						int dbId = rs.getInt("MONTHLY_DB_ID");
						int subtaskId = rs.getInt("SUBTASK_ID");
						String name = rs.getString("NAME");
						if(result.containsKey(dbId)){
							Map<Integer, String> subtasks = result.get(dbId);
							subtasks.put(subtaskId, name);
							result.put(dbId, subtasks);
						}else{
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
	
}
