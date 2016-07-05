package com.navinfo.dataservice.engine.meta.rdname;

import java.util.Map;

/**
 *用于在检查结果查看，包含道路名的字段和检查规则，检查信息和检查补充信息扩展字段
 * @author XXW
 *
 */
public class RdNameExtend4Teilen extends RdName {

	
	protected String userName;  //登陆用户名
	protected String startTime; // 查询开始时间，用于查询
	protected String endTime; // 查询结束时间，用于查询
	protected String time;//拆分时间，用于返回结果
	
	public void setSearchParameter(Map<String, Object> data){
		super.setSearchParameter(data);
		this.setUserName(toString(data.get("userName")));
		this.setStartTime(toString(data.get("startTime")));
		this.setEndTime(toString(data.get("endTime")));
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}


}
