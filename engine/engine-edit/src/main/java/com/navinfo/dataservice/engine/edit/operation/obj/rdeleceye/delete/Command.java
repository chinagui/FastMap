package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	private String requester;
	
	private int pid;
	
	private RdElectroniceye eleceye;
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public RdElectroniceye getEleceye() {
		return eleceye;
	}

	public void setEleceye(RdElectroniceye eleceye) {
		this.eleceye = eleceye;
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDELECTRONICEYE;
	}
	
	public Command(JSONObject json, String requester){
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		
		JSONObject data = json.getJSONObject("data");
		this.pid = data.getInt("pid");
	}

}
