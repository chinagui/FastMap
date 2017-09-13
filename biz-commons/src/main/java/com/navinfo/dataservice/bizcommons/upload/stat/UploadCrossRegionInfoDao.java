package com.navinfo.dataservice.bizcommons.upload.stat;

/** 
 * @ClassName: UploadCrossRegionInfoDao
 * @author xiaoxiaowen4127
 * @date 2017年8月24日
 * @Description: UploadCrossRegionInfoDao.java
 */
public class UploadCrossRegionInfoDao {
	protected long userId;
	protected int fromSubtaskId;
	protected String uploadTime;
	protected int uploadType;
	protected int outRegionId;
	protected int outGridId;
	protected int outGridNumber;
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public int getFromSubtaskId() {
		return fromSubtaskId;
	}
	public void setFromSubtaskId(int fromSubtaskId) {
		this.fromSubtaskId = fromSubtaskId;
	}
	public String getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(String uploadTime) {
		this.uploadTime = uploadTime;
	}
	public int getUploadType() {
		return uploadType;
	}
	public void setUploadType(int uploadType) {
		this.uploadType = uploadType;
	}
	public int getOutRegionId() {
		return outRegionId;
	}
	public void setOutRegionId(int outRegionId) {
		this.outRegionId = outRegionId;
	}
	public int getOutGridId() {
		return outGridId;
	}
	public void setOutGridId(int outGridId) {
		this.outGridId = outGridId;
	}
	public int getOutGridNumber() {
		return outGridNumber;
	}
	public void setOutGridNumber(int outGridNumber) {
		this.outGridNumber = outGridNumber;
	}
	public Object[] attrArray(){
		Object[] cols = new Object[6];
		cols[0] = userId;
		cols[1] = fromSubtaskId;
		cols[2] = uploadType;
		cols[3] = outRegionId;
		cols[4] = outGridId;
		cols[5] = outGridNumber;
		return cols;
	}
}
