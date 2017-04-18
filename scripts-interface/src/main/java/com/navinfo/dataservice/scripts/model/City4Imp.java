package com.navinfo.dataservice.scripts.model;

import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: City4Imp
 * @author xiaoxiaowen4127
 * @date 2017年2月26日
 * @Description: City4Imp.java
 */
public class City4Imp {
	String cityName;
	int adminId;
	String provName;
	Geometry geometry;
	int regionId;
	Set<String> meshes;
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public int getAdminId() {
		return adminId;
	}
	public void setAdminId(int adminId) {
		this.adminId = adminId;
	}
	public String getProvName() {
		return provName;
	}
	public void setProvName(String provName) {
		this.provName = provName;
	}
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	public int getRegionId() {
		return regionId;
	}
	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}
	public Set<String> getMeshes() {
		return meshes;
	}
	public void setMeshes(Set<String> meshes) {
		this.meshes = meshes;
	}
}
