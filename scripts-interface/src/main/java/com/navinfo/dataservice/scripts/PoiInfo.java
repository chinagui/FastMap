package com.navinfo.dataservice.scripts;

public class PoiInfo {
	/**
	 * poi的Pid
	 */
	private int pid;
	
	public int getPid(){
		return pid;
	}
	
	public void setPid(int value){
		this.pid = value;
	}
	
	/**
	 * poi所在图幅号
	 */
	private String meshId;
	
	public String getMeshId(){
		return this.meshId;
	}
	
	public void setMeshId(String value){
		this.meshId =value;
	}
	
	/**
	 * poi的verifyFlags.record
	 */
	private int verifyRecord;
	
	public int getVerifyRecord(){
		return this.verifyRecord;
	}
	
	public void setVerifyRecord(int value){
		this.verifyRecord = value;
	}
	
	/**
	 * poi的sourceFlags.record 
	 */
	private int sourceRecord;
	
	public int getSourceRecord(){
		return this.sourceRecord;
	}
	
	public void setSourceRecord(int value){
		this.sourceRecord = value;
	}
	
	/**
	 * poi的fieldVerification
	 */
	private int fieldVerification;
	
	public int getFieldVerification(){
		return this.fieldVerification;
	}
	
	public void setFieldVerification(int value){
		this.fieldVerification = value;
	}
}
