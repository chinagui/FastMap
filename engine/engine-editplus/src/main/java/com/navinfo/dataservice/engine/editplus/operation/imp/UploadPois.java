package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.HashMap;
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

	protected Map<String,JSONObject> addPois;//key:fid
	protected Map<String,JSONObject> deletePois;//key:fid
	protected Map<String,JSONObject> updatePois;//key:fid
	
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
}
