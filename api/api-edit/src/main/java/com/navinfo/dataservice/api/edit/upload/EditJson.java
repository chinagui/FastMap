package com.navinfo.dataservice.api.edit.upload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class EditJson {
	protected List<Map<String,JSONObject>> addJsons;//key:type
	protected Map<String,Map<Long,JSONObject>> updateJsons;//Map<String,Map<Long,JSONObject>>key:type,value:key-pid
	protected Map<String,Map<Long,JSONObject>> deleteJsons;//Map<String,Map<Long,JSONObject>>key:type,value:key-pid
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
					if("IX_POI".equals(type)){
						ixPoiUpdateMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("IX_HAMLET".equals(type)){
						ixHamletUpdateMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("AD_FACE".equals(type)){
						adFaceUpdateMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("AD_LINK".equals(type)){
						adLinkUpdateMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("AD_NODE".equals(type)){
						adNodeUpdateMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}
				}else if("DELETE".equals(jo.getString("command"))){
					String type = jo.getString("type");
					if("IX_POI".equals(type)){
						ixPoiDeleteMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("IX_HAMLET".equals(type)){
						ixHamletDeleteMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("AD_FACE".equals(type)){
						adFaceDeleteMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("AD_LINK".equals(type)){
						adLinkDeleteMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}else if("AD_NODE".equals(type)){
						adNodeDeleteMap.put(jo.getLong("objId"), jo.getJSONObject("data"));
					}
				}
			}
			updateJsons.put("IX_POI",ixPoiUpdateMap);
			updateJsons.put("IX_HAMLET",ixHamletUpdateMap);
			updateJsons.put("AD_FACE",adFaceUpdateMap);
			updateJsons.put("AD_LINK",adLinkUpdateMap);
			updateJsons.put("AD_NODE",adNodeUpdateMap);
			deleteJsons.put("IX_POI",ixPoiDeleteMap);
			deleteJsons.put("IX_HAMLET",ixHamletDeleteMap);
			deleteJsons.put("AD_FACE",adFaceDeleteMap);
			deleteJsons.put("AD_LINK",adLinkDeleteMap);
			deleteJsons.put("AD_NODE",adNodeDeleteMap);
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
