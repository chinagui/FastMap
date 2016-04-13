package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint;

import java.util.List;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class Command implements ICommand {

	private GeometryFactory geometryFactory = new GeometryFactory();

	private String requester;

	private int linkPid;

	private Point point;

	private RdLink link1;

	private RdLink link2;

	private RdNode sNode;

	private RdNode eNode;

	private int projectId;

	private int breakNodePid;//在以已存在的node通过移动位置来打断LINK的记录

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

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public void setLink1(RdLink link1) {
		this.link1 = link1;
	}

	public void setLink2(RdLink link2) {
		this.link2 = link2;
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

	public void setRestrictListVias(List<List<Entry<Integer, RdRestrictionVia>>> listVias) {
		this.listRestrictionVias = listVias;
	}

	public RdLink getLink1() {
		return link1;
	}

	public RdLink getLink2() {
		return link2;
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

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.linkPid = json.getInt("objId");

		JSONObject data = json.getJSONObject("data");

		double lng = Math.round(data.getDouble("longitude")*100000)/100000.0;

		double lat = Math.round(data.getDouble("latitude")*100000)/100000.0;

		this.projectId = json.getInt("projectId");

		if (data.containsKey("breakNodePid")) {
			this.breakNodePid = data.getInt("breakNodePid");
		}

		Coordinate coord = new Coordinate(lng, lat);

		this.point = geometryFactory.createPoint(coord);

		this.link1 = new RdLink();

		this.link2 = new RdLink();

		if (json.containsKey("infect") && json.getInt("infect") == 1) {
			this.isCheckInfect = true;
		}
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
		return ObjType.RDLINK;
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


}
