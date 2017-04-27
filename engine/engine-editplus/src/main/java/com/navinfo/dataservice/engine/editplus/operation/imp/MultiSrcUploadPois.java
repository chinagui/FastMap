package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

public class MultiSrcUploadPois extends UploadPois{
	public void addJsonPoi(JSONObject jo){
		addJsonPoi(jo.getString("fid"),jo);
	}
	public void addJsonPoi(String fid,JSONObject jo){
		if(jo.getInt("addFlag")==1){
			if(addPois==null){
				addPois=new HashMap<String,JSONObject>();
			}
			addPois.put(fid, jo);
		}else if(jo.getInt("delFlag")==1){
			if(deletePois==null){
				deletePois=new HashMap<String,JSONObject>();
			}
			deletePois.put(fid, jo);
		}else{
			if(updatePois==null){
				updatePois= new HashMap<String,JSONObject>();
			}
			updatePois.put(fid, jo);
		}
	}
}
