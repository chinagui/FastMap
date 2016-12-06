package com.navinfo.dataservice.api.edit.upload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class EditJson {
	protected List<Map<String,JSONObject>> addJsons;//key:type
	protected MultiMap updateJsons;//Map<String,List<Map<String,JSONObject>>>key:type,value:key-pid
	protected MultiMap deleteJsons;//Map<String,List<Map<String,JSONObject>>>key:type,value:key-pid
	public void addJsonPoi(Object jo){
		if(jo instanceof JSONObject){
			JSONObject jso = (JSONObject) jo;
			this.handle(jso);
		}else if(jo instanceof JSONArray){
			JSONArray jsa = (JSONArray) jo;
			if(jsa.size()>0){
				for (int i = 0; i < jsa.size(); i++) {
					this.handle(jsa.getJSONObject(i));
				}
			}
		}
		
	}
	
	private void handle(JSONObject jo){
		if("INSERT".equals(jo.getString("command"))){
			if(addJsons==null){
				addJsons=new ArrayList<Map<String,JSONObject>>();
			}
			Map<String,JSONObject> addMap = new HashMap<String,JSONObject>();
			addMap.put(jo.getString("type"), jo.getJSONObject("data"));
			addJsons.add(addMap);
		}else if("UPDATE".equals(jo.getString("command"))){
			if(updateJsons==null){
				updateJsons= new MultiValueMap();
			}
			Map<Long,JSONObject> updateMap = new HashMap<Long,JSONObject>();
			updateMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
			updateJsons.put(jo.getString("type"), updateMap);
		}else if("DELETE".equals(jo.getString("command"))){
			if(deleteJsons==null){
				deleteJsons=new MultiValueMap();
			}
			Map<Long,JSONObject> deleteMap = new HashMap<Long,JSONObject>();
			deleteMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
			updateJsons.put(jo.getString("type"), deleteMap);
		}
	}
	
}
