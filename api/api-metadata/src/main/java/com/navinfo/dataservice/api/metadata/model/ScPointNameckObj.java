package com.navinfo.dataservice.api.metadata.model;

import java.io.Serializable;

public class ScPointNameckObj implements Serializable{
	private String preKey;
	private String resultKey;
	private int type;
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getPreKey() {
		return preKey;
	}
	public void setPreKey(String preKey) {
		this.preKey = preKey;
	}
	public String getResultKey() {
		return resultKey;
	}
	public void setResultKey(String resultKey) {
		this.resultKey = resultKey;
	}
}
