package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdrestriction.create;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.OperType;

public class Command implements ICommand {

	private String requester;

	private int projectId;

	private int inLinkPid;

	private int nodePid;

	private List<Integer> outLinkPids;

	private List<Integer> restricInfos;

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

	public List<Integer> getRestricInfos() {
		return restricInfos;
	}

	public void setRestricInfos(List<Integer> restricInfos) {
		this.restricInfos = restricInfos;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDRESTRICTION;
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

		if (data.containsKey("outLinkPids")) {
			JSONArray array = data.getJSONArray("outLinkPids");

			outLinkPids = new ArrayList<Integer>();

			for (int i = 0; i < array.size(); i++) {

				int pid = array.getInt(i);

				if (!outLinkPids.contains(pid)) {
					outLinkPids.add(pid);
				}
			}
		} else {

			JSONArray array = data.getJSONArray("infos");

			restricInfos = new ArrayList<Integer>();

			for (int i = 0; i < array.size(); i++) {

				int info = array.getInt(i);

				restricInfos.add(info);
			}
		}
	}

}
