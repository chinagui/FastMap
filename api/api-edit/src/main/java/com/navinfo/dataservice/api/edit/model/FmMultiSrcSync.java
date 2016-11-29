package com.navinfo.dataservice.api.edit.model;

import java.io.Serializable;
import java.util.Date;

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
	private int syncStatus;
	private Date lastSyncTime;
	private Date syncTime;
	private Long jobId;
	private String zipFile;
	
	public static final int STATUS_START_CREATE=1;
	public static final int STATUS_CREATING=2;
	public static final int STATUS_CREATED_SUCCESS=8;
	public static final int STATUS_CREATED_FAIL=9;
	public static final int STATUS_SYNC_SUCCESS=18;
	public static final int STATUS_SYNC_FAIL=19;
	
	
	
	public Long getSid() {
		return sid;
	}
	public void setSid(Long sid) {
		this.sid = sid;
	}
	public int getSyncStatus() {
		return syncStatus;
	}
	public void setSyncStatus(int syncStatus) {
		this.syncStatus = syncStatus;
	}
	public Date getLastSyncTime() {
		return lastSyncTime;
	}
	public void setLastSyncTime(Date lastSyncTime) {
		this.lastSyncTime = lastSyncTime;
	}
	public Date getSyncTime() {
		return syncTime;
	}
	public void setSyncTime(Date syncTime) {
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
