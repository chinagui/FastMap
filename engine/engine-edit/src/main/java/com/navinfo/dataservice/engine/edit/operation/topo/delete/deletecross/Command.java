package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletecross;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {
	
	private String requester;

	private int pid;
	
	private RdCross cross;
	
	private List<RdRestriction> restricts;
	
	private List<RdLaneConnexity> lanes;
	
	private List<RdBranch> branches;
	
	private boolean isCheckInfect = false;
	
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

	public Command(JSONObject json,String requester) {
		this.requester = requester;
		
		this.pid = json.getInt("objId");
		
		this.setDbId(json.getInt("dbId"));
		
		if (json.containsKey("infect") && json.getInt("infect") == 1) {
			this.isCheckInfect = true;
		}
	}

	public boolean isCheckInfect() {
		return isCheckInfect;
	}

	public void setCheckInfect(boolean isCheckInfect) {
		this.isCheckInfect = isCheckInfect;
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
