package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.create;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {
	
	private String requester;
	
	private double latitude;
	
	private double longitude;
	
	private int direct;
	
	private int linkPid;
	
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

	public int getDirect() {
		return direct;
	}

	public void setDirect(int direct) {
		this.direct = direct;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDELECTRONICEYE;
	}
	
	public Command(JSONObject json, String requester){
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		
		JSONObject data = json.getJSONObject("data");
		this.linkPid = data.getInt("linkPid");
		this.direct = data.getInt("direct");
		this.latitude = data.getDouble("latitude");
		this.longitude = data.getDouble("longitude");
	}

}
