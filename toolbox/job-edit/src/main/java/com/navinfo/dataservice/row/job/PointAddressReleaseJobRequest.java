package com.navinfo.dataservice.row.job;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

public class PointAddressReleaseJobRequest extends AbstractJobRequest {
	private int dbId;
	private int subtaskId;

	@Override
	public String getJobType() {
		return "pointAddressRelease";
	}
	public int getDbId() {
		return dbId;
	}
	public void setDbId(int dbId) {
		this.dbId = dbId;
	}
	public long getSubtaskId() {
		return subtaskId;
	}
	public void setSubtaskId(int subtaskId) {
		this.subtaskId = subtaskId;
	}
	@Override
	public String getJobTypeName(){
		return "点门牌提交";
	}

	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void defineSubJobRequests() throws JobCreateException {
	}

	@Override
	protected int myStepCount() throws JobException {
		return 1;
	}

}
