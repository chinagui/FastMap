package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlaneconnexity.delete;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;

	private int pid;
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.RDLANECONNEXITY;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setProjectId(json.getInt("projectId"));

		this.pid = json.getInt("objId");
		
	}

}
