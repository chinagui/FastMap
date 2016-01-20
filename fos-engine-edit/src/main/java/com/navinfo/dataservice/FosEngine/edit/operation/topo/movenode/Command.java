package com.navinfo.dataservice.FosEngine.edit.operation.topo.movenode;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.OperType;

public class Command implements ICommand {
	
	private int nodePid;
	
	private double longitude;
	
	private double latitude;
	
	private String requester;
	
	private int projectId;
	
	private List<RdLink> links;
	
	public List<RdLink> getLinks() {
		return links;
	}

	public void setLinks(List<RdLink> links) {
		this.links = links;
	}

	public int getProjectId() {
		return projectId;
	}
	
	public Command(JSONObject json,String requester){
		
		this.nodePid = json.getInt("objId");
		
		this.longitude = json.getJSONObject("data").getDouble("longitude");
		
		this.latitude = json.getJSONObject("data").getDouble("latitude");
		
		this.projectId = json.getInt("projectId");
	}

	@Override
	public OperType getOperType() {
		
		return OperType.MOVE;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}

	@Override
	public ObjType getObjType() {
		
		return ObjType.RDNODE;
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


	
}
