package com.navinfo.dataservice.engine.edit.operation.obj.rdroad.delete;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;

	private int pid;

	private RdRoad road;

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public RdRoad getRoad() {
		return road;
	}

	public void setRoad(RdRoad road) {
		this.road = road;
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDROAD;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.pid = json.getInt("objId");

	}

}
