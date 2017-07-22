package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.io.Serializable;

/** 
 * @ClassName: ErrorLog
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: ErrorLog.java
 */
public class ErrorLog implements Serializable {
	private String fid;
	private int errorCode;
	private String reason;
	public ErrorLog(){}
	public ErrorLog(String fid,String reason){
		this.fid = fid;
		this.reason = reason;
	}
	public ErrorLog(String fid,int errorCode,String reason){
		this.fid = fid;
		this.errorCode = errorCode;
		this.reason = reason;
	}
	public String getFid() {
		return fid;
	}
	public void setFid(String fid) {
		this.fid = fid;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
}