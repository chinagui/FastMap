package com.navinfo.dataservice.api.metadata.model;

import java.io.Serializable;

/** 
 * @ClassName: Mesh4Partition
 * @author xiaoxiaowen4127
 * @date 2017年2月9日
 * @Description: Mesh4Partition.java
 */
public class Mesh4Partition implements Serializable {
	private int mesh;
	private int adminCode;
	private String province;
	private String provinceCode;
	private int action;//出品批次
	private int day2monSwitch;//日落月开关状态，0-关闭，1-打开
	
	public Mesh4Partition(){}
	public int getMesh() {
		return mesh;
	}
	public void setMesh(int mesh) {
		this.mesh = mesh;
	}
	public int getAdminCode() {
		return adminCode;
	}
	public void setAdminCode(int adminCode) {
		this.adminCode = adminCode;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getProvinceCode() {
		return provinceCode;
	}
	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}
	public int getAction() {
		return action;
	}
	public void setAction(int action) {
		this.action = action;
	}
	public int getDay2monSwitch() {
		return day2monSwitch;
	}
	public void setDay2monSwitch(int day2monSwitch) {
		this.day2monSwitch = day2monSwitch;
	}
}
