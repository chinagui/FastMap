package com.navinfo.dataservice.engine.edit.operation.obj.lunode.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;

	private JSONObject content;

	private int pid;

	private LuNode node;

	public LuNode getNode() {
		return node;
	}

	public void setNode(LuNode node) {
		this.node = node;
	}

	public JSONObject getContent() {
		return content;
	}

	public void setContent(JSONObject content) {
		this.content = content;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
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
		return ObjType.LUNODE;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.content = json.getJSONObject("data");

		this.pid = this.content.getInt("pid");
	}

	public Command(JSONObject json, String requester, LuNode node) {
		this(json, requester);
		this.node = node;

	}

}
