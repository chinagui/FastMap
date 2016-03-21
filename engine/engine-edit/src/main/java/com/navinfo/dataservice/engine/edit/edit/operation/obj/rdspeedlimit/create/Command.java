package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdspeedlimit.create;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.edit.edit.model.ObjType;
import com.navinfo.dataservice.engine.edit.edit.operation.ICommand;
import com.navinfo.dataservice.engine.edit.edit.operation.OperType;

public class Command implements ICommand {

	private String requester;

	private int projectId;
	
	private int linkPid;
	
	private int direct;
	
	private double latitude;
	
	private double longitude;
	
	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public int getDirect() {
		return direct;
	}

	public void setDirect(int direct) {
		this.direct = direct;
	}

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
		return ObjType.RDSPEEDLIMIT;
	}

	@Override
	public String getRequester() {
		return requester;
	}
	
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.projectId = json.getInt("projectId");
		
		JSONObject data = json.getJSONObject("data");

		this.direct = data.getInt("direct");
		
		this.linkPid = data.getInt("linkPid");
		
		this.longitude = data.getDouble("longitude");
		
		this.latitude = data.getDouble("latitude");
	}

}
