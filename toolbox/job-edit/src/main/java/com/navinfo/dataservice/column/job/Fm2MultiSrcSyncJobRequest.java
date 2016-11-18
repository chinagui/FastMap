package com.navinfo.dataservice.column.job;

import java.util.List;

import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;

/** 
 * @ClassName: Fm2MultiSrcSyncJobRequest
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: Fm2MultiSrcSyncJobRequest.java
 */
public class Fm2MultiSrcSyncJobRequest extends AbstractJobRequest {
	
	protected List<String> dbIds;
	protected String lastSyncTime;
	protected String syncTime;

	public List<String> getDbIds() {
		return dbIds;
	}

	public void setDbIds(List<String> dbIds) {
		this.dbIds = dbIds;
	}

	public String getLastSyncTime() {
		return lastSyncTime;
	}

	public void setLastSyncTime(String lastSyncTime) {
		this.lastSyncTime = lastSyncTime;
	}

	public String getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(String syncTime) {
		this.syncTime = syncTime;
	}

	@Override
	public void defineSubJobRequests() throws JobCreateException {
		
	}

	@Override
	public String getJobType() {
		// TODO Auto-generated method stub
		return "Fm2MultiSrcSync";
	}

	@Override
	public String getJobTypeName() {
		return "创建FM日库多源增量包";
	}

	@Override
	protected int myStepCount() throws JobException {
		return 3;
	}

	@Override
	public void validate() throws JobException {
		// TODO Auto-generated method stub

	}

}
