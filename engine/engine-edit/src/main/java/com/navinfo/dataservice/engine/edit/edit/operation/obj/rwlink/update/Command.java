package com.navinfo.dataservice.engine.edit.edit.operation.obj.rwlink.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;
	
	private int linkPid;
	
	private JSONObject updateContent;

	public int getLinkPid() {
		return linkPid;
	}


	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
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
		return ObjType.RWLINK;
	}

	@Override
	public String getRequester() {
		return requester;
	}
	
	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));
		
		this.updateContent = json.getJSONObject("data");
		
		this.linkPid = this.updateContent.getInt("objId");
	}

}
