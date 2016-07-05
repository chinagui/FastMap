package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdgsc.delete;

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
		return ObjType.RDGSC;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public void createGlmList() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.pid = json.getInt("objId");
		
	}

}
