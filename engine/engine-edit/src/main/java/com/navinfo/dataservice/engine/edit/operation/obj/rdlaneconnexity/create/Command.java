package com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.create;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;

	private int inLinkPid;

	private int nodePid;

	private JSONArray topos = new JSONArray();

	public JSONArray getTopos() {
		return topos;
	}

	public void setTopos(JSONArray topos) {
		this.topos = topos;
	}

	private String laneInfo;

	public int getInLinkPid() {
		return inLinkPid;
	}

	public void setInLinkPid(int inLinkPid) {
		this.inLinkPid = inLinkPid;
	}

	public int getNodePid() {
		return nodePid;
	}

	public String getLaneInfo() {
		return laneInfo;
	}

	public void setLaneInfo(String laneInfo) {
		this.laneInfo = laneInfo;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDLANECONNEXITY;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		JSONObject data = json.getJSONObject("data");

		this.nodePid = data.getInt("nodePid");

		this.inLinkPid = data.getInt("inLinkPid");

		this.setTopos(data.getJSONArray("topos"));

		laneInfo = data.getString("laneInfo");

	}

}
