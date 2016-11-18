package com.navinfo.dataservice.api.edit.model;

import java.io.Serializable;

public class MultiSrcFmSync implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long sid;
	private Long syncStatus;
	private Object syncTime;
	private Long jobId;
	private String zipFile;
	private Long dbType;
	
	
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
	public Long getDbType() {
		return dbType;
	}
	public void setDbType(Long dbType) {
		this.dbType = dbType;
	}
	
	
	@Override
	public String toString() {
		return "MultiSrcFmSync [sid=" + sid + ", syncStatus=" + syncStatus + ", syncTime=" + syncTime + ", jobId="
				+ jobId + ", zipFile=" + zipFile + ", dbType=" + dbType + "]";
	}
	
	
	
}
