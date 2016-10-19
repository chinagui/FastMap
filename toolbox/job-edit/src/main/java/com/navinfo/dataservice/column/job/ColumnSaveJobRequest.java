package com.navinfo.dataservice.column.job;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

import net.sf.json.JSONArray;

/**
 * 
 * @author wangdongbin
 * 描述：ColumnSaveJob请求参数的解析处理类
 *
 */
public class ColumnSaveJobRequest extends AbstractJobRequest {
	
	private int taskId;
	private int userId;
	private JSONArray data;
	private String secondWorkItem;

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public JSONArray getData() {
		return data;
	}

	public void setData(JSONArray data) {
		this.data = data;
	}

	public String getSecondWorkItem() {
		return secondWorkItem;
	}

	public void setSecondWorkItem(String secondWorkItem) {
		this.secondWorkItem = secondWorkItem;
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getJobType() {
		return "columnSaveJob";
	}
	
	@Override
	public String getJobTypeName(){
		return "列编保存";
	}

	@Override
	protected int myStepCount() throws JobException {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub

	}

}
