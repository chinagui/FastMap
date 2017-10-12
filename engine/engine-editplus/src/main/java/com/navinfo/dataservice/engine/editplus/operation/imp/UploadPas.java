package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: UploadPas
 * @author zl
 * @date 2017年10月09日
 * @Description: UploadPas.java
 */
public abstract class UploadPas {

	protected int regionId = 0;
	protected int regionDayDbId = 0;
	
	
	protected Map<String,JSONObject> addPas = new HashMap<String,JSONObject>();//key:fid
	protected Map<String,JSONObject> deletePas = new HashMap<String,JSONObject>();//key:fid
	protected Map<String,JSONObject> updatePas = new HashMap<String,JSONObject>();//key:fid
	
	public void addJsonPa(JSONObject jo){
		addJsonPa(jo.getString("fid"),jo);
	}
	public abstract void addJsonPa(String fid,JSONObject jo);
	
	public void addJsonPas(JSONArray ja){
		if(ja!=null&&ja.size()>0){
			for(Object jo:ja){
				addJsonPa((JSONObject)jo);
			}
		}
	}
	public void addJsonPas(Map<String,JSONObject> ja){
		if(ja!=null){
			for(Entry<String,JSONObject> entry:ja.entrySet()){
				addJsonPa(entry.getKey(),entry.getValue());
			}
		}
	}
	
	public Map<String, JSONObject> getAddPas() {
		return addPas;
	}
	public Map<String, JSONObject> getDeletePas() {
		return deletePas;
	}
	public Map<String, JSONObject> getUpdatePas() {
		return updatePas;
	}
	
	public List<ErrorLog> allFail(String reason){
		List<ErrorLog> logs = new ArrayList<ErrorLog>();
		if(addPas!=null){
			for(String f:addPas.keySet()){
				logs.add(new ErrorLog(f,reason));
			}
		}
		if(updatePas!=null){
			for(String f:updatePas.keySet()){
				logs.add(new ErrorLog(f,reason));
			}
		}
		if(deletePas!=null){
			for(String f:deletePas.keySet()){
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
