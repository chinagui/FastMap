package com.navinfo.dataservice.control.model;


/** 
* @ClassName:  IxPoiChildren 
* @author zl
* @date 2017-06-20
* @Description: TODO
*/
public class UploadIxPoiContacts{
	private String number;
	private int type;
	private String linkman;
	private int priority;
	private String rowId;
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getLinkman() {
		return linkman;
	}
	public void setLinkman(String linkman) {
		this.linkman = linkman;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public String getRowId() {
		return rowId;
	}
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
	
}
