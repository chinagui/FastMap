package com.navinfo.dataservice.scripts.model;

import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

/** 
 * @ClassName: City4Imp
 * @author xiaoxiaowen4127
 * @date 2017年2月26日
 * @Description: City4Imp.java
 */
public class Block4Imp {
	int cityId=0;
	String cityName;
	String blockName;
	Geometry geometry;
	Geometry originGeomtry;
	String wrokProperty;
	Set<String> grids;
	public int getCityId() {
		return cityId;
	}
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getBlockName() {
		return blockName;
	}
	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	public Geometry getOriginGeomtry() {
		return originGeomtry;
	}
	public void setOriginGeomtry(Geometry originGeomtry) {
		this.originGeomtry = originGeomtry;
	}
	public String getWrokProperty() {
		return wrokProperty;
	}
	public void setWrokProperty(String wrokProperty) {
		this.wrokProperty = wrokProperty;
	}
	public Set<String> getGrids() {
		return grids;
	}
	public void setGrids(Set<String> grids) {
		this.grids = grids;
	}
}
