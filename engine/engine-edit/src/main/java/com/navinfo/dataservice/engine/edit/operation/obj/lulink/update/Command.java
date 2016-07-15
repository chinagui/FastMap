package com.navinfo.dataservice.engine.edit.operation.obj.lulink.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;
	
	private JSONObject updateContent;
	
	private int linkPid;
	
	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.LULINK;
	}
	
	public Command(JSONObject json, String requester){
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));
		
		this.updateContent = json.getJSONObject("data");
		
		this.linkPid = this.updateContent.getInt("pid");
	}

	public JSONObject getUpdateContent() {
		return updateContent;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

}
