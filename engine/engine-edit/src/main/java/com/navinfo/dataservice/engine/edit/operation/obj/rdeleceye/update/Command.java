package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {
	
	private String requester;
	
	private int pid;
	
	private JSONObject content;
	
	private RdElectroniceye eleceye;

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

	public RdElectroniceye getEleceye() {
		return eleceye;
	}

	public void setEleceye(RdElectroniceye eleceye) {
		this.eleceye = eleceye;
	}

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
		return ObjType.RDELECTRONICEYE;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		
		this.content = json.getJSONObject("data");
		this.pid = content.getInt("pid");
	}

}
