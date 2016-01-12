package com.navinfo.dataservice.FosEngine.edit.operation.topo.departnode;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.OperType;

public class Command implements ICommand {
	
	private int linkPid;
	
	private String requester;
	
	private int nodePid;
	
	private double longitude;
	
	private double latitude;
	
	private int projectId;
	
	public int getProjectId() {
		return projectId;
	}


	public Command(JSONObject json,String requester){
		this.requester = requester;
		
		this.linkPid = json.getInt("linkPid");
		
		this.nodePid = json.getInt("nodePid");
		
		this.longitude = json.getJSONObject("data").getDouble("longitude")*100000;
		
		this.latitude = json.getJSONObject("data").getDouble("latitude")*100000;
		
		this.projectId = json.getInt("projectId");
	}
	

	public int getLinkPid() {
		return linkPid;
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

	@Override
	public OperType getOperType() {
		// TODO Auto-generated method stub
		return OperType.DEPARTNODE;
	}

	@Override
	public String getRequester() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjType getObjType() {
		// TODO Auto-generated method stub
		return ObjType.RDLINK;
	}


}
