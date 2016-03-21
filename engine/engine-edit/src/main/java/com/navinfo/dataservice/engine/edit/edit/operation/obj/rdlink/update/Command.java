package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlink.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.edit.edit.model.ObjType;
import com.navinfo.dataservice.engine.edit.edit.operation.ICommand;
import com.navinfo.dataservice.engine.edit.edit.operation.OperType;

public class Command implements ICommand {

	private String requester;

	private int projectId;
	
	private int linkPid;
	
	private JSONObject updateContent;

	public int getLinkPid() {
		return linkPid;
	}


	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}


	public int getProjectId() {
		return projectId;
	}


	public JSONObject getUpdateContent() {
		return updateContent;
	}


	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.RDLINK;
	}

	@Override
	public String getRequester() {
		return requester;
	}
	
	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.projectId = json.getInt("projectId");
		
		this.updateContent = json.getJSONObject("data");
		
		this.linkPid = this.updateContent.getInt("pid");
	}

}
