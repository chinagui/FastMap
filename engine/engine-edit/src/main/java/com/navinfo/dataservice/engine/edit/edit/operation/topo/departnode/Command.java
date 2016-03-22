package com.navinfo.dataservice.engine.edit.edit.operation.topo.departnode;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;

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
		
		JSONObject data = json.getJSONObject("data");
		
		this.linkPid = data.getInt("linkPid");
		
		this.nodePid = data.getInt("nodePid");
		
		this.longitude = Math.round(data.getDouble("longitude")*100000)/100000.0;
		
		this.latitude = Math.round(data.getDouble("latitude")*100000)/100000.0;
		
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
		return OperType.DEPART;
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
