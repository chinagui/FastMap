package com.navinfo.dataservice.engine.edit.edit.operation.topo.deletenode;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.edit.edit.model.ObjType;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.branch.RdBranch;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.cross.RdCross;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.engine.edit.edit.operation.ICommand;
import com.navinfo.dataservice.engine.edit.edit.operation.OperType;

public class Command implements ICommand {
	
	private String requester;

	private int nodePid;
	
	private RdNode node;
	
	private List<RdLink> links;
	
	private List<Integer> linkPids;
	
	private List<RdNode> nodes;
	
	private List<Integer> nodePids;
	
	private List<RdRestriction> restrictions;
	
	private List<RdLaneConnexity> lanes;
	
	private List<RdBranch> branches;
	
	private List<RdCross> crosses;
	
	private List<RdSpeedlimit> limits;
	
	private int projectId;
	
	private boolean isCheckInfect = false;
	
	public boolean isCheckInfect() {
		return isCheckInfect;
	}
	
	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public List<RdLink> getLinks() {
		return links;
	}

	public void setLinks(List<RdLink> links) {
		this.links = links;
	}

	public List<RdSpeedlimit> getLimits() {
		return limits;
	}

	public void setLimits(List<RdSpeedlimit> limits) {
		this.limits = limits;
	}

	public List<RdNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<RdNode> nodes) {
		this.nodes = nodes;
	}

	public List<Integer> getNodePids() {
		return nodePids;
	}

	public void setNodePids(List<Integer> nodePids) {
		this.nodePids = nodePids;
	}

	public List<RdRestriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<RdRestriction> restrictions) {
		this.restrictions = restrictions;
	}

	public List<RdBranch> getBranches() {
		return branches;
	}

	public void setBranches(List<RdBranch> branches) {
		this.branches = branches;
	}

	public List<RdLaneConnexity> getLanes() {
		return lanes;
	}

	public void setLanes(List<RdLaneConnexity> lanes) {
		this.lanes = lanes;
	}

	public List<RdCross> getCrosses() {
		return crosses;
	}

	public void setCrosses(List<RdCross> crosses) {
		this.crosses = crosses;
	}
	
	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public List<Integer> getLinkPids() {
		return linkPids;
	}

	public void setLinkPids(List<Integer> linkPids) {
		this.linkPids = linkPids;
	}

	public RdNode getNode() {
		return node;
	}

	public void setNode(RdNode node) {
		this.node = node;
	}

	@Override
	public OperType getOperType() {
		
		return OperType.DELETE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.RDNODE;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}
	
	public Command(JSONObject json,String requester) {
		this.requester = requester;
		
		this.nodePid = json.getInt("objId");
		
		this.projectId = json.getInt("projectId");
		
		if (json.containsKey("infect") && json.getInt("infect") == 1){
			this.isCheckInfect = true;
		}
		
	}

}
