package com.navinfo.dataservice.engine.edit.operation.obj.lcnode.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;

import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;

	private JSONObject content;

	private LcNode node;

	public LcNode getNode() {
		return node;
	}

	public void setNode(LcNode node) {
		this.node = node;
	}

	private int pid;

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

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.LCNODE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.content = json.getJSONObject("data");

		this.pid = this.content.getInt("pid");

	}

	public Command(JSONObject json, String requester, LcNode node) {
		this(json, requester);
		this.node = node;

	}

}
