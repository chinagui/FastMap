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
	private String reason;
	public ErrorLog(){}
	public ErrorLog(String fid,String reason){
		this.fid = fid;
		this.reason = reason;
	}
	public String getFid() {
		return fid;
	}
	public void setFid(String fid) {
		this.fid = fid;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
}
