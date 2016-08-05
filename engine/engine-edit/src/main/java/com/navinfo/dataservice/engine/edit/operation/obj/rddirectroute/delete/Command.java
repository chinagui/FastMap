package com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand{
	
	private String requester;
	
	private int pid;
	
	private RdDirectroute directroute;
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public RdDirectroute getDirectroute() {
		return directroute;
	}

	public void setDirectroute(RdDirectroute directroute) {
		this.directroute = directroute;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}

	@Override
	public String getRequester() {
	
		return requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDDIRECTROUTE;
	}
	
	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		if(json.containsKey("objId"))
		{
			this.pid = json.getInt("objId");
		}
	}

}
