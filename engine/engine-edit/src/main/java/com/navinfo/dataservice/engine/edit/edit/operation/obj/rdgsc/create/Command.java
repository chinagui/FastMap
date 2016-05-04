package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.create;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;

	private Map<Integer,Integer> linkMap = new HashMap<Integer,Integer>();
	
	private JSONObject geoObject;

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDCROSS;
	}

	@Override
	public String getRequester() {
		return requester;
	}
	
	public Map<Integer, Integer> getLinkMap() {
		return linkMap;
	}

	public void setLinkMap(Map<Integer, Integer> linkMap) {
		this.linkMap = linkMap;
	}
	
	public JSONObject getGeoObject() {
		return geoObject;
	}

	public void setGeoObject(JSONObject geoObject) {
		this.geoObject = geoObject;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setProjectId(json.getInt("projectId"));
		JSONObject data = json.getJSONObject("data");
		
		this.geoObject = data.getJSONObject("geometry");
		
		if(data.getJSONArray("linkObjs") instanceof JSONArray)
		{
			JSONArray linkAttrArray = data.getJSONArray("linkObjs");
			
			for(int i = 0;i<linkAttrArray.size();i++)
			{
				JSONObject linkObj = linkAttrArray.getJSONObject(i);
				linkMap.put(linkObj.getInt("pid"), linkObj.getInt("level_index"));
			}
			
		}
		
	}
}
