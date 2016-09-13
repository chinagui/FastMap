package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	private String requester;

	private int linkPid;

	private RdLink link;

	private List<Integer> nodePids;

	private List<RdNode> nodes;

	private List<RdRestriction> inLinkRestrictions;

	private List<RdRestriction> outDeleteLinkRestrictions;

	private List<RdRestriction> outUpdateLinkRestrictions;

	private List<RdLaneConnexity> inLinkRdLaneConnexitys;

	private List<RdLaneConnexity> outLinkDeleteRdLaneConnexitys;

	private List<RdLaneConnexity> outLinkUpdateRdLaneConnexitys;

	private List<RdBranch> inLinkBranchs;

	private List<RdBranch> outLinkDeleteBranchs;

	private List<RdRestriction> restrictions;

	private List<RdLaneConnexity> lanes;

	private List<RdBranch> branches;

	private List<RdCross> crosses;

	private List<RdSpeedlimit> limits;

	private List<RdGsc> rdGscs;

	private List<AdAdmin> adAdmins;

	private List<RdElectroniceye> electroniceyes;
	
	private List<RdGate> rdGates;
	
	private List<RdInter> inters;
	
	private List<RdInter> updateInters;
	
	private List<RdInter> deleteInters;

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

	public List<RdGsc> getRdGscs() {
		return rdGscs;
	}

	public void setRdGscs(List<RdGsc> rdGscs) {
		this.rdGscs = rdGscs;
	}

	public List<RdElectroniceye> getElectroniceyes() {
		return electroniceyes;
	}

	public void setElectroniceyes(List<RdElectroniceye> electroniceyes) {
		this.electroniceyes = electroniceyes;
	}

	public List<RdRestriction> getInLinkRestrictions() {
		return inLinkRestrictions;
	}

	public void setInLinkRestrictions(List<RdRestriction> inLinkRestrictions) {
		this.inLinkRestrictions = inLinkRestrictions;
	}

	public List<RdRestriction> getOutDeleteLinkRestrictions() {
		return outDeleteLinkRestrictions;
	}

	public void setOutDeleteLinkRestrictions(List<RdRestriction> outDeleteLinkRestrictions) {
		this.outDeleteLinkRestrictions = outDeleteLinkRestrictions;
	}

	public List<RdRestriction> getOutUpdateLinkRestrictions() {
		return outUpdateLinkRestrictions;
	}

	public void setOutUpdateLinkRestrictions(List<RdRestriction> outUpdateLinkRestrictions) {
		this.outUpdateLinkRestrictions = outUpdateLinkRestrictions;
	}

	public List<RdBranch> getInLinkBranchs() {
		return inLinkBranchs;
	}

	public void setInLinkBranchs(List<RdBranch> inLinkBranchs) {
		this.inLinkBranchs = inLinkBranchs;
	}

	public List<RdBranch> getOutLinkDeleteBranchs() {
		return outLinkDeleteBranchs;
	}

	public void setOutLinkDeleteBranchs(List<RdBranch> outLinkDeleteBranchs) {
		this.outLinkDeleteBranchs = outLinkDeleteBranchs;
	}

	public List<RdLaneConnexity> getInLinkRdLaneConnexitys() {
		return inLinkRdLaneConnexitys;
	}

	public void setInLinkRdLaneConnexitys(List<RdLaneConnexity> inLinkRdLaneConnexitys) {
		this.inLinkRdLaneConnexitys = inLinkRdLaneConnexitys;
	}

	public List<RdLaneConnexity> getOutLinkDeleteRdLaneConnexitys() {
		return outLinkDeleteRdLaneConnexitys;
	}

	public void setOutLinkDeleteRdLaneConnexitys(List<RdLaneConnexity> outLinkDeleteRdLaneConnexitys) {
		this.outLinkDeleteRdLaneConnexitys = outLinkDeleteRdLaneConnexitys;
	}

	public List<RdLaneConnexity> getOutLinkUpdateRdLaneConnexitys() {
		return outLinkUpdateRdLaneConnexitys;
	}

	public void setOutLinkUpdateRdLaneConnexitys(List<RdLaneConnexity> outLinkUpdateRdLaneConnexitys) {
		this.outLinkUpdateRdLaneConnexitys = outLinkUpdateRdLaneConnexitys;
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

	public List<RdInter> getUpdateInters() {
		return updateInters;
	}

	public void setUpdateInters(List<RdInter> updateInters) {
		this.updateInters = updateInters;
	}

	public List<RdInter> getDeleteInters() {
		return deleteInters;
	}

	public void setDeleteInters(List<RdInter> deleteInters) {
		this.deleteInters = deleteInters;
	}
}
