package com.navinfo.dataservice.control.model;


/** 
* @ClassName:  IxPoiChildren 
* @author zl
* @date 2017-06-20
* @Description: TODO
*/
public class UploadIxPoiRelateChildren{
	private int type ;
	private int childPid;
	private String childFid;
	private String rowId;
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getChildPid() {
		return childPid;
	}
	public void setChildPid(int childPid) {
		this.childPid = childPid;
	}
	public String getChildFid() {
		return childFid;
	}
	public void setChildFid(String childFid) {
		this.childFid = childFid;
	}
	public String getRowId() {
		return rowId;
	}
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	
}
