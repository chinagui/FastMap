package com.navinfo.dataservice.api.edit.upload;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

public class EditJson {
	protected List<JSONObject> addPois;//
	protected Map<Long,JSONObject> deletePois;//key:pid
	protected Map<Long,JSONObject> updatePois;//key:pid
	public void addJsonPoi(JSONObject jo){
//		if(jo.getInt("addFlag")==1){
//			if(addPois==null){
//				addPois=new ArrayList<JSONObject>();
//			}
//			addPois.add(jo);
//		}else if(jo.getInt("delFlag")==1){
//			if(deletePois==null){
//				deletePois=new HashMap<Long,JSONObject>();
//			}
//			deletePois.put(jo.getString("fid"), jo);
//		}else{
//			if(updatePois==null){
//				updatePois= new HashMap<String,JSONObject>();
//			}
//			updatePois.put(jo.getString("fid"), jo);
//		}
	}
	public List<JSONObject> getAddPois() {
		return addPois;
	}
	public Map<Long, JSONObject> getDeletePois() {
		return deletePois;
	}
	public Map<Long, JSONObject> getUpdatePois() {
		return updatePois;
	}
	
}
