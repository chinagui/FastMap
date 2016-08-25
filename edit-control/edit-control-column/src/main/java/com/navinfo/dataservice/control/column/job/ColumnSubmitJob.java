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
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiDeepStatusSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiOpConfSelector;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ColumnSubmitJob extends AbstractJob {
	
	public ColumnSubmitJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		
		ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
		
		List<String> rowIdList = new ArrayList<String>();
		
		Connection conn = null;
		
		try {
			ColumnSubmitJobRequest columnSubmitJobRequest = (ColumnSubmitJobRequest) this.request;
			
			int taskId = columnSubmitJobRequest.getTaskId();
			int userId = columnSubmitJobRequest.getUserId();
			String firstWorkItem = columnSubmitJobRequest.getFirstWorkItem();
			String secondWorkItem = columnSubmitJobRequest.getSecondWorkItem();
			
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			
			int dbId = subtask.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			// TODO 区分大陆/港澳
			int type = 1;
			
			// 查询可提交数据
			IxPoiDeepStatusSelector ixPoiDeepStatusSelector = new IxPoiDeepStatusSelector(conn);
			rowIdList = ixPoiDeepStatusSelector.getRowIdForSubmit(firstWorkItem, secondWorkItem, taskId);
			
			IxPoiOpConfSelector ixPoiOpConfSelector = new IxPoiOpConfSelector(conn);
			PoiDeepOpConf deepOpConf = ixPoiOpConfSelector.getDeepOpConf(firstWorkItem,secondWorkItem, type);
			
			// TODO 检查和批处理
			// rowIdList替换为无检查错误的list
			// rowIdList = TODO
			
			// 重分类
			if (deepOpConf.getSubmitExeclassify()==1) {
				HashMap<String,Object> classifyMap = new HashMap<String,Object>();
				classifyMap.put("userId", userId);
				classifyMap.put("ckRules", deepOpConf.getSaveCkrules());
				classifyMap.put("classifyRules", deepOpConf.getSaveClassifyrules());
				JSONArray dataArray = new JSONArray(); 
				for (String rowId:rowIdList) {
					JSONObject temp = new JSONObject();
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
			ColumnCoreControl control = new ColumnCoreControl();
			control.updateDeepStatus(rowIdList, conn, 3);
			
		} catch (Exception e) {
			throw new JobException(e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

}
