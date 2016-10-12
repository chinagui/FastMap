package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {
	
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
	
	private boolean isCheckInfect = false;
	
	private List<AdAdmin> adAdmins;
	
	public boolean isCheckInfect() {
		return isCheckInfect;
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

	public List<AdAdmin> getAdAdmins() {
		return adAdmins;
	}

	public void setAdAdmins(List<AdAdmin> adAdmins) {
		this.adAdmins = adAdmins;
	}

	public Command(JSONObject json,String requester) {
		this.requester = requester;
		
		this.nodePid = json.getInt("objId");
		
		this.setDbId(json.getInt("dbId"));
		
		if (json.containsKey("infect") && json.getInt("infect") == 1){
			this.isCheckInfect = true;
		}
		
	}

}
