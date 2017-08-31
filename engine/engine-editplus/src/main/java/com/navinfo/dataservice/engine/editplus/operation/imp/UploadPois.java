package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: UploadPois
 * @author xiaoxiaowen4127
 * @date 2017年4月26日
 * @Description: UploadPois.java
 */
public abstract class UploadPois {

	protected int regionId = 0;
	protected int regionDayDbId = 0;
	
	
	protected Map<String,JSONObject> addPois = new HashMap<String,JSONObject>();//key:fid
	protected Map<String,JSONObject> deletePois = new HashMap<String,JSONObject>();//key:fid
	protected Map<String,JSONObject> updatePois = new HashMap<String,JSONObject>();//key:fid
	
	public void addJsonPoi(JSONObject jo){
		addJsonPoi(jo.getString("fid"),jo);
	}
	public abstract void addJsonPoi(String fid,JSONObject jo);
	
	public void addJsonPois(JSONArray ja){
		if(ja!=null&&ja.size()>0){
			for(Object jo:ja){
				addJsonPoi((JSONObject)jo);
			}
		}
	}
	public void addJsonPois(Map<String,JSONObject> ja){
		if(ja!=null){
			for(Entry<String,JSONObject> entry:ja.entrySet()){
				addJsonPoi(entry.getKey(),entry.getValue());
			}
		}
	}
	
	public Map<String, JSONObject> getAddPois() {
		return addPois;
	}
	public Map<String, JSONObject> getDeletePois() {
		return deletePois;
	}
	public Map<String, JSONObject> getUpdatePois() {
		return updatePois;
	}
	
	public List<ErrorLog> allFail(String reason){
		List<ErrorLog> logs = new ArrayList<ErrorLog>();
		if(addPois!=null){
			for(String f:addPois.keySet()){
				logs.add(new ErrorLog(f,reason));
			}
		}
		if(updatePois!=null){
			for(String f:updatePois.keySet()){
				logs.add(new ErrorLog(f,reason));
			}
		}
		if(deletePois!=null){
			for(String f:deletePois.keySet()){
				logs.add(new ErrorLog(f,reason));
			}
		}
		return logs;
	}
	public int getRegionId() {
		return regionId;
	}
	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}
	public int getRegionDayDbId() {
		return regionDayDbId;
	}
	public void setRegionDayDbId(int regionDayDbId) {
		this.regionDayDbId = regionDayDbId;
	}
}
