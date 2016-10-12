package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	private String requester;

	private int linkPid;

	private RdLink link;

	private List<Integer> nodePids;

	private List<RdNode> nodes;

	private List<RdRestriction> restrictions;
	
	private List<RdRestrictionDetail> delDetailResList;

	private List<RdLaneConnexity> lanes;

	private List<RdBranch> branches;

	private List<RdCross> crosses;

	private List<RdSpeedlimit> limits;

	private List<AdAdmin> adAdmins;

	private List<RdElectroniceye> electroniceyes;
	
	private List<RdGate> rdGates;
	
	private List<RdInter> inters;
	
	private boolean isCheckInfect = false;

	public boolean isCheckInfect() {
		return isCheckInfect;
	}

	public List<AdAdmin> getAdAdmins() {
		return adAdmins;
	}

	public void setAdAdmins(List<AdAdmin> adAdmins) {
		this.adAdmins = adAdmins;
	}

	public RdLink getLink() {
		return link;
	}

	public void setLink(RdLink link) {
		this.link = link;
	}

	public List<Integer> getNodePids() {
		return nodePids;
	}

	public void setNodePids(List<Integer> nodePids) {
		this.nodePids = nodePids;
	}

	public List<RdSpeedlimit> getLimits() {
		return limits;
	}

	public void setLimits(List<RdSpeedlimit> limits) {
		this.limits = limits;
	}
	
	public List<RdRestrictionDetail> getDelDetailResList() {
		return delDetailResList;
	}

	public void setDelDetailResList(List<RdRestrictionDetail> delDetailResList) {
		this.delDetailResList = delDetailResList;
	}

	public List<RdNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<RdNode> nodes) {
		this.nodes = nodes;
	}

	public List<RdRestriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<RdRestriction> restrictions) {
		this.restrictions = restrictions;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.linkPid = json.getInt("objId");

		this.setDbId(json.getInt("dbId"));
		if (json.containsKey("infect") && json.getInt("infect") == 1) {
			this.isCheckInfect = true;
		}
		// 构造检查参数
		// this.createGlmList();

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

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	@Override
	public OperType getOperType() {

		return OperType.DELETE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDLINK;
	}

	@Override
	public String getRequester() {

		return requester;
	}

	public List<RdElectroniceye> getElectroniceyes() {
		return electroniceyes;
	}

	public void setElectroniceyes(List<RdElectroniceye> electroniceyes) {
		this.electroniceyes = electroniceyes;
	}

	public List<RdGate> getRdGates() {
		return rdGates;
	}

	public void setRdGates(List<RdGate> rdGates) {
		this.rdGates = rdGates;
	}

	public List<RdInter> getInters() {
		return inters;
	}

	public void setInters(List<RdInter> inters) {
		this.inters = inters;
	}
}
