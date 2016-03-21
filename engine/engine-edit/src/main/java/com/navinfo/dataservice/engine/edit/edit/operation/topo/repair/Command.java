package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.edit.edit.model.ObjType;
import com.navinfo.dataservice.engine.edit.edit.operation.ICommand;
import com.navinfo.dataservice.engine.edit.edit.operation.OperType;

public class Command implements ICommand {

	private String requester;
	
	private int linkPid;
	
	private JSONObject linkGeom;
	
	private JSONArray interLines;
	
	private JSONArray interNodes;
	
	private int projectId;

	public int getLinkPid() {
		return linkPid;
	}

	public JSONObject getLinkGeom() {
		return linkGeom;
	}

	public JSONArray getInterLines() {
		return interLines;
	}

	public JSONArray getInterNodes() {
		return interNodes;
	}

	@Override
	public OperType getOperType() {
		
		return OperType.REPAIR;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}

	@Override
	public ObjType getObjType() {
		
		return ObjType.RDLINK;
	}
	
	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public Command(JSONObject json,String requester){
		
		this.requester = requester;
		
		this.projectId = json.getInt("projectId");
		
		this.linkPid = json.getInt("objId");
		
		JSONObject data = json.getJSONObject("data");
		
		this.linkGeom = data.getJSONObject("geometry");
		
		this.interLines = data.getJSONArray("interLinks");
		
		this.interNodes = data.getJSONArray("interNodes");
		
		
	}

}
