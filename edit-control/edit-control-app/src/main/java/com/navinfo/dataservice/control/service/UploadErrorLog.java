package com.navinfo.dataservice.control.service;

import java.io.Serializable;

/** 
 * @ClassName: UploadErrorLog
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: UploadErrorLog.java
 */
public class UploadErrorLog implements Serializable {
	private String fid;
	private String reason;
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
