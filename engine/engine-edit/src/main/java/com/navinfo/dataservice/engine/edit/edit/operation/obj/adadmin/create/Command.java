package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmin.create;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;

import net.sf.json.JSONObject;

public class Command implements ICommand {

	private String requester;

	private int projectId;

	/**
	 * 引导线RDLink的pid
	 */
	private int linkPid;

	/**
	 * 经度
	 */
	private double longitude;

	/**
	 * 维度
	 */
	private double latitude;

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDBRANCH;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.projectId = json.getInt("projectId");

		JSONObject data = json.getJSONObject("data");

		this.linkPid = data.getInt("linkPid");

		this.longitude = data.getDouble("longitude");

		this.latitude = data.getDouble("latitude");
	}

}
