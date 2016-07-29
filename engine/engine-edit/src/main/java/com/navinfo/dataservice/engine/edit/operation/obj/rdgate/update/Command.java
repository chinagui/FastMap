package com.navinfo.dataservice.engine.edit.operation.obj.rdgate.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {
	
	private String requester;
	
	private int pid;
	
	private JSONObject content;
	
	private RdGate rdGate;

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

	public RdGate getRdGate() {
		return rdGate;
	}

	public void setRdGate(RdGate rdGate) {
		this.rdGate = rdGate;
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDGATE;
	}
	
	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		
		this.content = json.getJSONObject("data");
		this.pid = content.getInt("pid");
	}

}
