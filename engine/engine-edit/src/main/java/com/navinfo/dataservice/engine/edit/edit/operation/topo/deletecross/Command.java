package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletecross;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.edit.edit.model.ObjType;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.branch.RdBranch;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.cross.RdCross;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.engine.edit.edit.operation.ICommand;
import com.navinfo.dataservice.engine.edit.edit.operation.OperType;

public class Command implements ICommand {
	
	private String requester;

	private int pid;
	
	private int projectId;
	
	private RdCross cross;
	
	private List<RdRestriction> restricts;
	
	private List<RdLaneConnexity> lanes;
	
	private List<RdBranch> branches;
	
	public RdCross getCross() {
		return cross;
	}

	public void setCross(RdCross cross) {
		this.cross = cross;
	}

	public List<RdRestriction> getRestricts() {
		return restricts;
	}

	public void setRestricts(List<RdRestriction> restricts) {
		this.restricts = restricts;
	}

	public List<RdLaneConnexity> getLanes() {
		return lanes;
	}

	public void setLanes(List<RdLaneConnexity> lanes) {
		this.lanes = lanes;
	}

	public List<RdBranch> getBranches() {
		return branches;
	}

	public void setBranches(List<RdBranch> branches) {
		this.branches = branches;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public Command(JSONObject json,String requester) {
		this.requester = requester;
		
		this.pid = json.getInt("objId");
		
		this.projectId = json.getInt("projectId");
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

}
