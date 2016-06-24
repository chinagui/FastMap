package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdbranch.create;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	private String requester;

//	private int projectId;

	private int inLinkPid;

	private int nodePid;

	private int outLinkPid;
	
	private int branchType = 0;
	
	private RdBranch branch;

	public RdBranch getBranch() {
		return branch;
	}

	public void setBranch(RdBranch branch) {
		this.branch = branch;
	}

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

//	public int getProjectId() {
//		return projectId;
//	}
//
//	public void setProjectId(int projectId) {
//		this.projectId = projectId;
//	}
	
	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	public int getBranchType() {
		return branchType;
	}

	public void setBranchType(int branchType) {
		this.branchType = branchType;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDBRANCH;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

//		this.projectId = json.getInt("projectId");
		this.setDbId(json.getInt("dbId"));

		JSONObject data = json.getJSONObject("data");

		this.inLinkPid = data.getInt("inLinkPid");

		this.nodePid = data.getInt("nodePid");

		this.outLinkPid = data.getInt("outLinkPid");
		
		if(data.containsKey("branchType"))
		{
			this.branchType = data.getInt("branchType");
		}
	}

}
