package com.navinfo.dataservice.api.edit.model;

import java.io.Serializable;

/**
 * 
 * @ClassName FmMultiSrcSync
 * @author Han Shaoming
 * @date 2016年11月18日 上午9:46:43
 * @Description TODO
 */
public class FmMultiSrcSync implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long sid;
	private Long syncStatus;
	private Object lastSyncTime;
	private Object syncTime;
	private Long jobId;
	private String zipFile;
	
	
	public Long getSid() {
		return sid;
	}
	public void setSid(Long sid) {
		this.sid = sid;
	}
	public Long getSyncStatus() {
		return syncStatus;
	}
	public void setSyncStatus(Long syncStatus) {
		this.syncStatus = syncStatus;
	}
	public Object getLastSyncTime() {
		return lastSyncTime;
	}
	public void setLastSyncTime(Object lastSyncTime) {
		this.lastSyncTime = lastSyncTime;
	}
	public Object getSyncTime() {
		return syncTime;
	}
	public void setSyncTime(Object syncTime) {
		this.syncTime = syncTime;
	}
	public Long getJobId() {
		return jobId;
	}
	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}
	public String getZipFile() {
		return zipFile;
	}
	public void setZipFile(String zipFile) {
		this.zipFile = zipFile;
	}
	
	
	@Override
	public String toString() {
		return "FmMultiSrcSync [sid=" + sid + ", syncStatus=" + syncStatus + ", lastSyncTime=" + lastSyncTime
				+ ", syncTime=" + syncTime + ", jobId=" + jobId + ", zipFile=" + zipFile + "]";
	}

	
	
}
