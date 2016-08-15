package com.navinfo.dataservice.engine.edit.operation.obj.rdobject.create;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand{
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	private String requester;
	
	private JSONArray interArray;
	
	private JSONArray linkArray;
	
	private JSONArray roadArray;
	
	public JSONArray getInterArray() {
		return interArray;
	}

	public void setInterArray(JSONArray interArray) {
		this.interArray = interArray;
	}

	public JSONArray getRoadArray() {
		return roadArray;
	}

	public void setRoadArray(JSONArray roadArray) {
		this.roadArray = roadArray;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.RDOBJECT;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) throws Exception{
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));
		
		JSONObject data = json.getJSONObject("data");
		
		if(data.containsKey("inters"))
		{
			this.interArray = data.getJSONArray("inters");
		}
		
		if(data.containsKey("roads"))
		{
			this.roadArray = data.getJSONArray("roads");
		}
		
		if(data.containsKey("links"))
		{
			this.linkArray = data.getJSONArray("links");
		}
	}

	public JSONArray getLinkArray() {
		return linkArray;
	}

	public void setLinkArray(JSONArray linkArray) {
		this.linkArray = linkArray;
	}
}
