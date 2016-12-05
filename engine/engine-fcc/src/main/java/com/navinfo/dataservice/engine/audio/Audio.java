package com.navinfo.dataservice.engine.audio;

import java.util.List;

/**
 * 音频模型
 */
public class Audio {
	


	private String rowkey;

	private String a_uuid;

	private int a_uploadUser;

	private String a_uploadDate;

	private List<String> a_tag;
	

	

	public String getRowkey() {
		return rowkey;
	}

	public void setRowkey(String rowkey) {
		this.rowkey = rowkey;
	}

	public String getA_uuid() {
		return a_uuid;
	}

	public void setA_uuid(String a_uuid) {
		this.a_uuid = a_uuid;
	}

	public int getA_uploadUser() {
		return a_uploadUser;
	}

	public void setA_uploadUser(int a_uploadUser) {
		this.a_uploadUser = a_uploadUser;
	}

	public String getA_uploadDate() {
		return a_uploadDate;
	}

	public void setA_uploadDate(String a_uploadDate) {
		this.a_uploadDate = a_uploadDate;
	}


	public List<String> getA_tag() {
		return a_tag;
	}

	public void setA_tag(List<String> a_tag) {
		this.a_tag = a_tag;
	}


	
}
