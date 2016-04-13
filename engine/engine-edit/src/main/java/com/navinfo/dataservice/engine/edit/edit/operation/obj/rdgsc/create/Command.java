package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.create;

import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command implements ICommand {

	private String requester;

	private int projectId;

	private Map<Integer,Integer> linkMap = new HashMap<Integer,Integer>();
	
	private String wkt;

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}


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
	
	public String getWkt() {
		return wkt;
	}

	public void setWkt(String wkt) {
		this.wkt = wkt;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.projectId = json.getInt("projectId");

		JSONObject data = json.getJSONObject("data");
		
		this.wkt = data.getString("wkt");
		
		if(data.getJSONArray("linkObjs") instanceof JSONArray)
		{
			JSONArray linkAttrArray = data.getJSONArray("linkObjs");
			
			for(int i = 0;i<linkAttrArray.size();i++)
			{
				JSONObject linkObj = linkAttrArray.getJSONObject(i);
				linkMap.put(linkObj.getInt("pid"), linkObj.getInt("lever_index"));
			}
			
		}
		
	}
}
