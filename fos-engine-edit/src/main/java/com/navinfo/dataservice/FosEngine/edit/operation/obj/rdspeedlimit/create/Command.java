package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdspeedlimit.create;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.OperType;

public class Command implements ICommand {

	private String requester;

	private int projectId;
	
	private JSONObject geometry;
	
	private int linkPid;
	
	private int direct;
	
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

	public JSONObject getGeometry() {
		return geometry;
	}

	public void setGeometry(JSONObject geometry) {
		this.geometry = geometry;
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
	
	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.projectId = json.getInt("projectId");
		
		JSONObject data = json.getJSONObject("data");

		this.direct = data.getInt("direct");
		
		this.linkPid = data.getInt("linkPid");
		
		this.geometry = data.getJSONObject("geometry");
	}

}
