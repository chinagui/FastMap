package com.navinfo.dataservice.FosEngine.edit.operation.topo.movenode;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.OperType;

public class Command implements ICommand {
	
	private int nodePid;
	
	private double longitude;
	
	private double latitude;
	
	private int projectId;
	
	private String requester;
	
	private JSONArray mountLinks;
	
	public Command(JSONObject json,String requester,int projectId){
		
		this.nodePid = json.getInt("nodePid");
		
		this.longitude = json.getJSONObject("data").getDouble("longitude");
		
		this.latitude = json.getJSONObject("data").getDouble("latitude");
		
		this.projectId = projectId;
		
		this.mountLinks = json.getJSONArray("mountLinks");
		
	}

	@Override
	public OperType getOperType() {
		
		return OperType.MOVENODE;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}

	@Override
	public ObjType getObjType() {
		
		return ObjType.RDLINK;
	}

	public int getNodePid() {
		return nodePid;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public int getProjectId() {
		return projectId;
	}

	public JSONArray getMountLinks() {
		return mountLinks;
	}

	
}
