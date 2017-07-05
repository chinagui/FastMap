package com.navinfo.dataservice.api.man.model;

import java.io.Serializable;
import java.util.Set;

/** 
 * @ClassName: RegionMesh
 * @author xiaoxiaowen4127
 * @date 2017年4月25日
 * @Description: RegionMesh.java
 */
public class RegionMesh implements Serializable{
	int regionId=0;
	int dailyDbId=0;
	int monthlyDbId=0;
	Set<String> meshes;

	public int getRegionId() {
		return regionId;
	}
	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}
	public int getDailyDbId() {
		return dailyDbId;
	}
	public void setDailyDbId(int dailyDbId) {
		this.dailyDbId = dailyDbId;
	}
	public int getMonthlyDbId() {
		return monthlyDbId;
	}
	public void setMonthlyDbId(int monthlyDbId) {
		this.monthlyDbId = monthlyDbId;
	}
	public Set<String> getMeshes() {
		return meshes;
	}
	public void setMeshes(Set<String> meshes) {
		this.meshes = meshes;
	}
	public boolean meshContains(String meshId){
		if(meshes!=null&&meshes.contains(meshId)){
			return true;
		}
		return false;
	}
}
