package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdbranch.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;

//	private int projectId;

	private JSONObject content;
	
	private int pid;
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

//	public int getProjectId() {
//		return projectId;
//	}
//
//	public void setProjectId(int projectId) {
//		this.projectId = projectId;
//	}

	public JSONObject getContent() {
		return content;
	}

	public void setContent(JSONObject content) {
		this.content = content;
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
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

//		this.projectId = json.getInt("projectId");
		this.setSubTaskId(json.getInt("subTaskId"));

		this.content = json.getJSONObject("data");
		
		this.pid = this.content.getInt("pid");

	}

}
