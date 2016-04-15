package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlaneconnexity.create;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;

public class Command implements ICommand {

	private String requester;

	private int projectId;

	private int inLinkPid;

	private int nodePid;

	private List<Integer> outLinkPids;

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

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public List<Integer> getOutLinkPids() {
		return outLinkPids;
	}

	public void setOutLinkPids(List<Integer> outLinkPids) {
		this.outLinkPids = outLinkPids;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
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

		this.projectId = json.getInt("projectId");

		JSONObject data = json.getJSONObject("data");

		this.nodePid = data.getInt("nodePid");

		this.inLinkPid = data.getInt("inLinkPid");
		
		JSONArray array = data.getJSONArray("outLinkPids");

		outLinkPids = new ArrayList<Integer>();

		for (int i = 0; i < array.size(); i++) {

			int pid = array.getInt(i);

			if (!outLinkPids.contains(pid)) {
				outLinkPids.add(pid);
			}
		}

		laneInfo = data.getString("laneInfo");

	}

}
