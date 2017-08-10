package com.navinfo.dataservice.service.monitor.model;

import java.io.Serializable;

/** 
 * @ClassName: FosLog
 * @author xiaoxiaowen4127
 * @date 2017年8月10日
 * @Description: FosLog.java
 */
public class FosLog implements Serializable{
	protected String id;
	protected String url;
	protected long sizeKb=0;
	public FosLog(String id){
		this.id=id;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getSizeKb() {
		return sizeKb;
	}
	public void setSizeKb(long sizeKb) {
		this.sizeKb = sizeKb;
	}
}
