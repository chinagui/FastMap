package com.navinfo.dataservice.column.job;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
/**
 * poi月库管理字段批处理
 * 必传参数：subtaskId
 * @author 赵凯凯
 */
public class MonthPoiBatchSyncJobRequest extends AbstractJobRequest {


	private int taskId;

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "monthPoiBatchSync";
	}

	@Override
	public String getJobTypeName() {
		// TODO Auto-generated method stub
		return "poi月库管理字段批处理";
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
