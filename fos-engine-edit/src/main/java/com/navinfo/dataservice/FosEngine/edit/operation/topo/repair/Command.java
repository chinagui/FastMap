package com.navinfo.dataservice.FosEngine.edit.operation.topo.repair;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.OperType;

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
		
		this.linkPid = json.getInt("linkPid");
		
		this.linkGeom = json.getJSONObject("geometry");
		
		this.interLines = json.getJSONArray("interLines");
		
		this.interNodes = json.getJSONArray("interPoints");
		
		this.projectId = json.getInt("projectId");
	}

}
