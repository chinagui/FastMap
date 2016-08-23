package com.navinfo.dataservice.control.column.job;

import java.sql.Connection;
import java.util.HashMap;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.column.core.ColumnCoreControl;
import com.navinfo.dataservice.control.column.core.ColumnCoreOperation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.PoiDeepOpConf;
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
		
		Connection conn = null;
		try {
			ColumnSaveJobRequest columnSaveJobRequest = (ColumnSaveJobRequest) this.request;
			int taskId = columnSaveJobRequest.getTaskId();
			int userId = columnSaveJobRequest.getUserId();
			JSONArray data = columnSaveJobRequest.getData();
			String secondWorItem = columnSaveJobRequest.getSecondWorkItem();
			
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			
			int dbId = subtask.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			ColumnCoreControl control = new ColumnCoreControl();
			control.columnSave(dbId, data);
			
			// TODO 区分大陆/港澳
			int type = 1;
			
			PoiDeepOpConf deepOpConf = control.getDeepOpConf(secondWorItem, type, conn);
			
			// TODO 检查和批处理
			
			// 重分类
			HashMap<String,Object> classifyMap = new HashMap<String,Object>();
			classifyMap.put("userId", userId);
			classifyMap.put("ckRules", deepOpConf.getSaveCkrules());
			classifyMap.put("classifyRules", deepOpConf.getSaveClassifyrules());
			JSONArray dataArray = new JSONArray(); 
			for (int i=0;i<data.size();i++) {
				JSONObject temp = new JSONObject();
				temp.put("rowId", data.getJSONObject(i).getString("rowId"));
				temp.put("taskId", taskId);
				dataArray.add(temp);
			}
			classifyMap.put("data", dataArray);
			ColumnCoreOperation columnCoreOperation = new ColumnCoreOperation();
			columnCoreOperation.runClassify(classifyMap);
			
			
		} catch (Exception e) {
			throw new JobException(e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
		

	}

}
