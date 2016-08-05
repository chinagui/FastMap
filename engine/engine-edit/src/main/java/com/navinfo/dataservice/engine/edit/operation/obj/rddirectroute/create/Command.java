package com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.create;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;
	
	private int inLinkPid;

	private int nodePid;

	private int outLinkPid;	
	
	private RdDirectroute directroute;
	
	public int getInLinkPid() {
		return inLinkPid;
	}

	public void setInLinkPid(int inLinkPid) {
		this.inLinkPid = inLinkPid;
	}

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public int getOutLinkPid() {
		return outLinkPid;
	}

	public void setOutLinkPid(int outLinkPid) {
		this.outLinkPid = outLinkPid;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	public RdDirectroute getDirectroute() {
		return directroute;
	}

	public void setDirectroute(RdDirectroute directroute) {
		this.directroute = directroute;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
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

		JSONObject data = json.getJSONObject("data");

		this.inLinkPid = data.getInt("inLinkPid");

		this.nodePid = data.getInt("nodePid");

		this.outLinkPid = data.getInt("outLinkPid");
	}

}
