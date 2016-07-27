package com.navinfo.dataservice.engine.edit.operation.obj.rdcross.delete;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	private String requester;
	
	private List<IRow> trafficsignals;

	private int pid;
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public List<IRow> getTrafficsignals() {
		return trafficsignals;
	}

	public void setTrafficsignals(List<IRow> trafficsignals) {
		this.trafficsignals = trafficsignals;
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.RDCROSS;
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
