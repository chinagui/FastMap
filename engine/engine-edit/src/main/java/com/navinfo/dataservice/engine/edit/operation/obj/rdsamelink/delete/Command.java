package com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.delete;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand implements ICommand {

	private String requester;

	private RdSameLink rdSameLink;

	private int pid;

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public RdSameLink getRdSameLink() {
		return rdSameLink;
	}

	public void setRdSameLink(RdSameLink rdSameLink) {
		this.rdSameLink = rdSameLink;
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDSAMELINK;
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
