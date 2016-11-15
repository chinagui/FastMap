package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import org.json.JSONException;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	private GeometryFactory geometryFactory = new GeometryFactory();
	private String requester;
	private List<RdLink> newLinks = new ArrayList<RdLink>();
	private int linkPid;

	private Point point;

	public List<RdLink> getNewLinks() {
		return newLinks;
	}

	public void setNewLinks(List<RdLink> newLinks) {
		this.newLinks = newLinks;
	}

	private RdNode sNode;

	private RdNode eNode;

	private int breakNodePid;// 在以已存在的node通过移动位置来打断LINK的记录
	private JSONArray breakNodes;

	private RdNode breakNode;

	private String operationType = "";

	private RdLink breakLink;

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public RdLink getBreakLink() {
		return breakLink;
	}

	public void setBreakLink(RdLink breakLink) {
		this.breakLink = breakLink;
	}

	private List<RdRestriction> restrictions;

	private List<RdRestrictionDetail> restrictionDetails;

	private List<List<Entry<Integer, RdRestrictionVia>>> listRestrictionVias;

	private List<RdLaneConnexity> laneConnextys;

	private List<RdLaneTopology> laneTopologys;

	private List<List<Entry<Integer, RdLaneVia>>> laneVias;

	private List<RdSpeedlimit> speedlimits;

	private List<RdBranch> inBranchs;

	private List<RdBranch> outBranchs;

	private List<List<RdBranchVia>> branchVias;

	private List<RdGsc> rdGscs;

	private List<AdAdmin> adAdmins;

	private List<RdElectroniceye> eleceyes;

	private List<RdGate> gates;

	private List<RdSe> rdSes;

	private List<RdSpeedbump> rdSpeedbumps;

	private List<RdTollgate> rdTollgates;

	private List<RdHgwgLimit> rdHgwgLimits;

	private boolean isCheckInfect = false;

	public boolean isCheckInfect() {
		return isCheckInfect;
	}

	public List<RdBranch> getInBranchs() {
		return inBranchs;
	}

	public void setInBranchs(List<RdBranch> inBranchs) {
		this.inBranchs = inBranchs;
	}

	public List<RdBranch> getOutBranchs() {
		return outBranchs;
	}

	public void setOutBranchs(List<RdBranch> outBranchs) {
		this.outBranchs = outBranchs;
	}

	public List<List<RdBranchVia>> getBranchVias() {
		return branchVias;
	}

	public void setBranchVias(List<List<RdBranchVia>> branchVias) {
		this.branchVias = branchVias;
	}

	public List<RdSpeedlimit> getSpeedlimits() {
		return speedlimits;
	}

	public void setSpeedlimits(List<RdSpeedlimit> speedlimits) {
		this.speedlimits = speedlimits;
	}

	public List<RdLaneConnexity> getLaneConnextys() {
		return laneConnextys;
	}

	public void setLaneConnexitys(List<RdLaneConnexity> laneConnextys) {
		this.laneConnextys = laneConnextys;
	}

	public List<RdLaneTopology> getLaneTopologys() {
		return laneTopologys;
	}

	public void setLaneTopologys(List<RdLaneTopology> laneTopologys) {
		this.laneTopologys = laneTopologys;
	}

	public List<List<Entry<Integer, RdLaneVia>>> getLaneVias() {
		return laneVias;
	}

	public void setLaneVias(List<List<Entry<Integer, RdLaneVia>>> laneVias) {
		this.laneVias = laneVias;
	}

	public List<RdRestriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<RdRestriction> restrictions) {
		this.restrictions = restrictions;
	}

	public List<RdRestrictionDetail> getRestrictionDetails() {
		return restrictionDetails;
	}

	public void setRestrictionDetails(
			List<RdRestrictionDetail> restrictionDetails) {
		this.restrictionDetails = restrictionDetails;
	}

	public List<List<Entry<Integer, RdRestrictionVia>>> geListRestrictVias() {
		return listRestrictionVias;
	}

	public void setRestrictListVias(
			List<List<Entry<Integer, RdRestrictionVia>>> listVias) {
		this.listRestrictionVias = listVias;
	}

	public List<RdHgwgLimit> getRdHgwgLimits() {
		return rdHgwgLimits;
	}

	public void setRdHgwgLimits(List<RdHgwgLimit> rdHgwgLimits) {
		this.rdHgwgLimits = rdHgwgLimits;
	}

	public RdNode getsNode() {
		return sNode;
	}

	public void setsNode(RdNode sNode) {
		this.sNode = sNode;
	}

	public RdNode geteNode() {
		return eNode;
	}

	public void seteNode(RdNode eNode) {
		this.eNode = eNode;
	}

	public RdNode getBreakNode() {
		return breakNode;
	}

	public void setBreakNode(RdNode breakNode) {
		this.breakNode = breakNode;
	}

	public List<AdAdmin> getAdAdmins() {
		return adAdmins;
	}

	public void setAdAdmins(List<AdAdmin> adAdmins) {
		this.adAdmins = adAdmins;
	}

	public List<RdElectroniceye> getEleceyes() {
		return eleceyes;
	}

	public void setEleceyes(List<RdElectroniceye> eleceyes) {
		this.eleceyes = eleceyes;
	}

	public List<RdSe> getRdSes() {
		return rdSes;
	}

	public void setRdSes(List<RdSe> rdSes) {
		this.rdSes = rdSes;
	}

	public List<RdSpeedbump> getRdSpeedbumps() {
		return rdSpeedbumps;
	}

	public void setRdSpeedbumps(List<RdSpeedbump> rdSpeedbumps) {
		this.rdSpeedbumps = rdSpeedbumps;
	}

	public List<RdTollgate> getRdTollgates() {
		return rdTollgates;
	}

	public void setRdTollgates(List<RdTollgate> rdTollgates) {
		this.rdTollgates = rdTollgates;
	}

	public Command(JSONObject json, String requester) throws JSONException {
		this.requester = requester;

		this.linkPid = json.getInt("objId");

		JSONObject data = json.getJSONObject("data");

		if (data.containsKey("breakNodePid")) {
			this.setBreakNodePid(data.getInt("breakNodePid"));
		}

		this.setDbId(json.getInt("dbId"));

		if (data.containsKey("breakNodes")) {
			this.breakNodes = JSONArray.fromObject(data
					.getJSONArray("breakNodes"));

		} else {
			Coordinate coord = new Coordinate(data.getDouble("longitude"),
					data.getDouble("latitude"));

			this.point = geometryFactory.createPoint(coord);
		}

		if (json.containsKey("infect") && json.getInt("infect") == 1) {
			this.isCheckInfect = true;
		}
	}

	public JSONArray getBreakNodes() {
		return breakNodes;
	}

	public void setBreakNodes(JSONArray breakNodes) {
		this.breakNodes = breakNodes;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	@Override
	public OperType getOperType() {

		return OperType.BREAK;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDNODE;
	}

	@Override
	public String getRequester() {

		return requester;
	}

	public int getBreakNodePid() {
		return breakNodePid;
	}

	public void setBreakNodePid(int breakNodePid) {
		this.breakNodePid = breakNodePid;
	}

	public List<RdGsc> getRdGscs() {
		return rdGscs;
	}

	public void setRdGscs(List<RdGsc> rdGscs) {
		this.rdGscs = rdGscs;
	}

	public List<RdGate> getGates() {
		return gates;
	}

	public void setGates(List<RdGate> gates) {
		this.gates = gates;
	}
}
