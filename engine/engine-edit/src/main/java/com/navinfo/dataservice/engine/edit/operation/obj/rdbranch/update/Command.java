package com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	private String requester;
	
	//分歧主表pid
	private int objId;

	private JSONObject content;
	
	//具体分歧的pid
	private int pid;
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

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
	
	public int getObjId() {
		return objId;
	}

	public void setObjId(int objId) {
		this.objId = objId;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.content = json.getJSONObject("data");
		
		this.pid = this.content.getInt("pid");
		
		this.objId = json.getInt("objId");

	}

}
