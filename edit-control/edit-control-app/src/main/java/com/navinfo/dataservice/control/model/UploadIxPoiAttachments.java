package com.navinfo.dataservice.control.model;


/** 
* @ClassName:  IxPoiChildren 
* @author zl
* @date 2017-06-20
* @Description: TODO
*/
public class UploadIxPoiAttachments{
	private String id;
	private int type;
	private String content;
	private String extContent;
	private int tag;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getExtContent() {
		return extContent;
	}
	public void setExtContent(String extContent) {
		this.extContent = extContent;
	}
	public int getTag() {
		return tag;
	}
	public void setTag(int tag) {
		this.tag = tag;
	}
	
	
}
