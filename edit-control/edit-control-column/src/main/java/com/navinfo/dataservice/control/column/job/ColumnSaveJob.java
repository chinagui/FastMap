package com.navinfo.dataservice.control.column.job;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.column.core.ColumnCoreControl;
import com.navinfo.dataservice.control.column.core.ColumnCoreOperation;
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
			
			ColumnCoreControl control = new ColumnCoreControl();
			control.columnSave(dbId, data);
			
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
				columnCoreOperation.runClassify(classifyMap);
			}
			
			// 修改poi_deep_status表作业项状态
			control.updateDeepStatus(rowIdList, conn, 2);
			
		} catch (Exception e) {
			throw new JobException(e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

}
