package com.navinfo.dataservice.column.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.model.poi.deep.PoiDeepOpConf;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiOpConfSelector;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ColumnSaveJob extends AbstractJob {
	
	public ColumnSaveJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		
		ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
		
		List<String> rowIdList = new ArrayList<String>();
		
		Connection conn = null;
		try {
			ColumnSaveJobRequest columnSaveJobRequest = (ColumnSaveJobRequest) this.request;
			int taskId = columnSaveJobRequest.getTaskId();
			int userId = columnSaveJobRequest.getUserId();
			JSONArray data = columnSaveJobRequest.getData();
			String secondWorkItem = columnSaveJobRequest.getSecondWorkItem();
			
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			
			int dbId = subtask.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			columnSave(dbId, data);
			
			// TODO 区分大陆/港澳
			int type = 1;
			
			IxPoiOpConfSelector ixPoiOpConfSelector = new IxPoiOpConfSelector(conn);
			PoiDeepOpConf deepOpConf = ixPoiOpConfSelector.getDeepOpConf("",secondWorkItem, type);
			
			// TODO 检查和批处理
			
			// 重分类
			if (deepOpConf.getSaveExeclassify()==1) {
				HashMap<String,Object> classifyMap = new HashMap<String,Object>();
				classifyMap.put("userId", userId);
				classifyMap.put("ckRules", deepOpConf.getSaveCkrules());
				classifyMap.put("classifyRules", deepOpConf.getSaveClassifyrules());
				JSONArray dataArray = new JSONArray(); 
				for (int i=0;i<data.size();i++) {
					JSONObject temp = new JSONObject();
					String rowId = data.getJSONObject(i).getString("rowId");
					rowIdList.add(rowId);
					temp.put("rowId", rowId);
					temp.put("taskId", taskId);
					dataArray.add(temp);
				}
				classifyMap.put("data", dataArray);
				ColumnCoreOperation columnCoreOperation = new ColumnCoreOperation();
				columnCoreOperation.runClassify(classifyMap,conn);
			}
			
			// 修改poi_deep_status表作业项状态
			updateDeepStatus(rowIdList, conn, 2);
			
		} catch (Exception e) {
			throw new JobException(e);
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

}
