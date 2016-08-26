package com.navinfo.dataservice.control.column.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.search.IxPoiSearch;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiDeepStatusSelector;

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
			for (int taskId:subTaskIds) {
				
				// 区分大陆，港澳任务
				
				Subtask subtask = apiService.queryBySubtaskId(taskId);
				int dbId = subtask.getDbId();
				if (dbId != oldDbId) {
					DbUtils.closeQuietly(conn);
					oldDbId = dbId;
					conn = DBConnector.getInstance().getConnectionById(oldDbId);
				}
				IxPoiDeepStatusSelector selector = new IxPoiDeepStatusSelector(conn);
				int tempCount = selector.queryHandlerCount(firstWorkItem,userId);
				hasApply += tempCount;
			}
			
			// 可申请数据条数
			int canApply = 100 - hasApply;
			
			if (canApply == 0) {
				throw new Exception("该作业员名下已存在100条数据，不可继续申请");
			}
			
			// 查询可申请数据
			for (int taskId:subTaskIds) {
				Subtask subtask = apiService.queryBySubtaskId(taskId);
				int dbId = subtask.getDbId();
				if (dbId != oldDbId) {
					DbUtils.closeQuietly(conn);
					oldDbId = dbId;
					conn = DBConnector.getInstance().getConnectionById(oldDbId);
				}
				IxPoiDeepStatusSelector selector = new IxPoiDeepStatusSelector(conn);
				List<String> rowIds = selector.getRowIdByTaskId(taskId, firstWorkItem);
				
				// 判断是否已申请够
				if (totalCount < canApply) {
					int leftCount = canApply - totalCount;
					if (rowIds.size()>leftCount) {
						List<String> subList = rowIds.subList(0, leftCount-1);
						// 申请数据
						updateHandler(subList,userId,conn);
						totalCount += subList.size();
					} else {
						// 申请数据
						updateHandler(rowIds,userId,conn);
						totalCount += rowIds.size();
					}
				} else {
					break;
				}
			}

		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
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
			int taskId= jsonReq.getInt("taskId");
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			
			String type = jsonReq.getString("type");
			int status = jsonReq.getInt("status");
			String firstWordItem = jsonReq.getString("firstWorkItem");
			String secondWorkItem = jsonReq.getString("secondWorkItem");
			
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			int dbId = subtask.getDbId();
			
			// 获取未提交数据的rowId
			conn = DBConnector.getInstance().getConnectionById(dbId);
			IxPoiDeepStatusSelector selector = new IxPoiDeepStatusSelector(conn);
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
	 * 保存精编数据
	 * @param dbId
	 * @param data
	 * @throws Exception
	 */
	public void columnSave(int dbId,JSONArray data) throws Exception {
		try {
			for (int i=0;i<data.size();i++) {
				JSONObject poiObj = new JSONObject();
				poiObj.put("dbId", dbId);
				poiObj.put("data", data.getJSONObject(i));
				EditApi apiEdit=(EditApi) ApplicationContextUtil.getBean("editApi");
				apiEdit.run(poiObj);
			}
			
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 更新配置表状态
	 * @param rowIdList
	 * @param conn
	 * @throws Exception
	 */
	public void updateDeepStatus(List<String> rowIdList,Connection conn,int status) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE poi_deep_status SET firstWorkStatus="+status+",secondWorkStatus="+status+" WHERE row_id in(");
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			String temp="";
			for (String rowId:rowIdList) {
				sb.append(temp);
				sb.append("'"+rowId+"'");
				temp = ",";
			}
			sb.append(")");
			
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 精编任务的统计查询
	 * @param jsonReq
	 * @return
	 * @throws Exception
	 */
	public JSONArray taskStatistics(JSONObject jsonReq)  throws Exception {
		
		Connection conn = null;
		
		try {
			int taskId = jsonReq.getInt("taskId");
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			int dbId = subtask.getDbId();
			
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			IxPoiDeepStatusSelector ixPoiDeepStatusSelector = new IxPoiDeepStatusSelector(conn);
			
			return ixPoiDeepStatusSelector.taskStatistics(taskId);
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
			String secondWorkItem = jsonReq.getString("secondWorkItem");
			int type = jsonReq.getInt("taskType");
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			int dbId = subtask.getDbId();
			
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			IxPoiDeepStatusSelector ixPoiDeepStatusSelector = new IxPoiDeepStatusSelector(conn);
			
			return ixPoiDeepStatusSelector.secondWorkStatistics(secondWorkItem, userId, type);
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
}
