package com.navinfo.dataservice.api.edit.model;

import java.io.Serializable;
import java.util.Date;

public class MultiSrcFmSync implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long sid;
	private int syncStatus;
	private Date syncTime;
	private long jobId;
	private String zipFile;
	private int dbType;
	

	public static final int STATUS_RECEIVED=1;
	public static final int STATUS_IMPORTING=2;
	public static final int STATUS_DOWNLOAD_SUCCESS=3;
	public static final int STATUS_DOWNLOAD_FAIL=4;
	public static final int STATUS_IMP_SUCCESS=5;
	public static final int STATUS_IMP_FAIL=6;
	public static final int STATUS_CREATE_RES_SUCCESS=7;
	public static final int STATUS_CREATE_RES_FAIL=8;
	public static final int STATUS_NOTIFY_SUCCESS=11;
	public static final int STATUS_NOTIFY_FAIL=12;
	public static final int DBTYPE_DAY=1;
	public static final int DBTYPE_MONTH=2;
	
	
	public long getSid() {
		return sid;
	}
	public void setSid(long sid) {
		this.sid = sid;
	}
	public int getSyncStatus() {
		return syncStatus;
	}
	public void setSyncStatus(int syncStatus) {
		this.syncStatus = syncStatus;
	}
	public Date getSyncTime() {
		return syncTime;
	}
	public void setSyncTime(Date syncTime) {
		this.syncTime = syncTime;
	}
	public long getJobId() {
		return jobId;
	}
	public void setJobId(long jobId) {
		this.jobId = jobId;
	}
	public String getZipFile() {
		return zipFile;
	}
	public void setZipFile(String zipFile) {
		this.zipFile = zipFile;
	}
	public int getDbType() {
		return dbType;
	}
	public void setDbType(int dbType) {
		this.dbType = dbType;
	}
	
	
	@Override
	public String toString() {
		return "MultiSrcFmSync [sid=" + sid + ", syncStatus=" + syncStatus + ", syncTime=" + syncTime + ", jobId="
				+ jobId + ", zipFile=" + zipFile + ", dbType=" + dbType + "]";
	}
	
	
	
}
