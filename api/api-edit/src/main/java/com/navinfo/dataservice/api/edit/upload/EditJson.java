package com.navinfo.dataservice.api.edit.upload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class EditJson {
	protected List<Map<String,JSONObject>> addJsons;//key:type
	protected Map<String,Map<Long,JSONObject>> updateJsons;//key:type,value:key-pid
	protected Map<String,Map<Long,JSONObject>> deleteJsons;//key:type,value:key-pid
	public void addJsonPoi(Object jo){
		if(addJsons==null){
			addJsons=new ArrayList<Map<String,JSONObject>>();
		}
		if(updateJsons==null){
			updateJsons= new HashMap<String,Map<Long,JSONObject>>();
		}
		if(deleteJsons==null){
			deleteJsons=new HashMap<String,Map<Long,JSONObject>>();
		}
		if(jo instanceof JSONObject){
			JSONObject jso = (JSONObject) jo;
			this.handle(jso);
		}else if(jo instanceof JSONArray){
			JSONArray jsa = (JSONArray) jo;
			this.handles(jsa);
		}
	}
	
	private void handle(JSONObject jo){
		if("INSERT".equals(jo.getString("command"))){
			Map<String,JSONObject> addMap = new HashMap<String,JSONObject>();
			addMap.put(jo.getString("type"), jo.getJSONObject("data"));
			addJsons.add(addMap);
		}else if("UPDATE".equals(jo.getString("command"))){
			Map<Long,JSONObject> updateMap = new HashMap<Long,JSONObject>();
			updateMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
			updateJsons.put(jo.getString("type"), updateMap);
		}else if("DELETE".equals(jo.getString("command"))){
			Map<Long,JSONObject> deleteMap = new HashMap<Long,JSONObject>();
			deleteMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
			deleteJsons.put(jo.getString("type"), deleteMap);
		}
	}
	
	private void handles(JSONArray jsa){
		if(jsa.size()>0){
			Map<Long,JSONObject> ixPoiUpdateMap = new HashMap<Long,JSONObject>();
			Map<Long,JSONObject> ixHamletUpdateMap = new HashMap<Long,JSONObject>();
			Map<Long,JSONObject> adFaceUpdateMap = new HashMap<Long,JSONObject>();
			Map<Long,JSONObject> adLinkUpdateMap = new HashMap<Long,JSONObject>();
			Map<Long,JSONObject> adNodeUpdateMap = new HashMap<Long,JSONObject>();
			Map<Long,JSONObject> ixPoiDeleteMap = new HashMap<Long,JSONObject>();
			Map<Long,JSONObject> ixHamletDeleteMap = new HashMap<Long,JSONObject>();
			Map<Long,JSONObject> adFaceDeleteMap = new HashMap<Long,JSONObject>();
			Map<Long,JSONObject> adLinkDeleteMap = new HashMap<Long,JSONObject>();
			Map<Long,JSONObject> adNodeDeleteMap = new HashMap<Long,JSONObject>();
			for (int i = 0; i < jsa.size(); i++) {
				JSONObject jo = jsa.getJSONObject(i);
				if("INSERT".equals(jo.getString("command"))){
					Map<String,JSONObject> addMap = new HashMap<String,JSONObject>();
					addMap.put(jo.getString("type"), jo.getJSONObject("data"));
					addJsons.add(addMap);
				}else if("UPDATE".equals(jo.getString("command"))){
					String type = jo.getString("type");
					if("IXPOI".equals(type)){
						ixPoiUpdateMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("IXHAMLET".equals(type)){
						ixHamletUpdateMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("ADFACE".equals(type)){
						adFaceUpdateMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("ADLINK".equals(type)){
						adLinkUpdateMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("ADNODE".equals(type)){
						adNodeUpdateMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}
				}else if("DELETE".equals(jo.getString("command"))){
					String type = jo.getString("type");
					if("IXPOI".equals(type)){
						ixPoiDeleteMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("IXHAMLET".equals(type)){
						ixHamletDeleteMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("ADFACE".equals(type)){
						adFaceDeleteMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("ADLINK".equals(type)){
						adLinkDeleteMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("ADNODE".equals(type)){
						adNodeDeleteMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}
				}
			}
			updateJsons.put("IXPOI",ixPoiUpdateMap);
			updateJsons.put("IXHAMLET",ixHamletUpdateMap);
			updateJsons.put("ADFACE",adFaceUpdateMap);
			updateJsons.put("ADLINK",adLinkUpdateMap);
			updateJsons.put("ADNODE",adNodeUpdateMap);
			deleteJsons.put("IXPOI",ixPoiDeleteMap);
			deleteJsons.put("IXHAMLET",ixHamletDeleteMap);
			deleteJsons.put("ADFACE",adFaceDeleteMap);
			deleteJsons.put("ADLINK",adLinkDeleteMap);
			deleteJsons.put("ADNODE",adNodeDeleteMap);
		}
	}

	public List<Map<String, JSONObject>> getAddJsons() {
		return addJsons;
	}
	public Map<String,Map<Long,JSONObject>> getUpdateJsons() {
		return updateJsons;
	}
	public Map<String,Map<Long,JSONObject>> getDeleteJsons() {
		return deleteJsons;
	}
	
}
