package com.navinfo.dataservice.control.service;

import java.io.Serializable;

/** 
 * @ClassName: RegionUploadResult
 * @author xiaoxiaowen4127
 * @date 2017年8月24日
 * @Description: RegionUploadResult.java
 */
public class RegionUploadResult implements Serializable {
	
	private int regionId = 0;
	private int subtaskId = 0;
	private int success = 0;
	private int fail =0 ;
	public RegionUploadResult(int regionId){
		this.regionId=regionId;
	}
	public int getRegionId() {
		return regionId;
	}
	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}
	public int getSubtaskId() {
		return subtaskId;
	}
	public void setSubtaskId(int subtaskId) {
		this.subtaskId = subtaskId;
	}
	public void addResult(int success,int fail){
		this.success=success;
		this.fail=fail;
	}
	public int getSuccess() {
		return success;
	}
	public void setSuccess(int success) {
		this.success = success;
	}
	public int getFail() {
		return fail;
	}
	public void setFail(int fail) {
		this.fail = fail;
	}
}
