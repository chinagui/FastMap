package com.navinfo.dataservice.engine.edit.operation.obj.poi.upload;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class CommandForDelete extends AbstractCommand {

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
		return ObjType.IXPOI;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public CommandForDelete(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.pid = json.getInt("objId");
		
	}

}
