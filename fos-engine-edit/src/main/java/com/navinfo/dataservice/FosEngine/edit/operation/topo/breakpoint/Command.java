package com.navinfo.dataservice.FosEngine.edit.operation.topo.breakpoint;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranch;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch.RdBranchVia;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.OperType;
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
	
	//下面两个字段,在以已存在的node通过移动位置来打断LINK的记录
	private int breakNodePid;
	
	private RdNode breakNode;

	private List<RdRestriction> restrictions;

	private List<RdRestrictionDetail> restrictionDetails;

	private List<List<RdRestrictionVia>> listRestrictionVias;
	
	private List<RdLaneConnexity> laneConnextys;
	
	private List<RdLaneTopology> laneTopologys;

	private List<List<RdLaneVia>> laneVias;

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

	public List<List<RdLaneVia>> getLaneVias() {
		return laneVias;
	}

	public void setLaneVias(List<List<RdLaneVia>> laneVias) {
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

	public List<List<RdRestrictionVia>> geListRestrictVias() {
		return listRestrictionVias;
	}

	public void setRestrictListVias(List<List<RdRestrictionVia>> listVias) {
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

		double lng = json.getJSONObject("data")
				.getDouble("longitude");

		double lat = json.getJSONObject("data")
				.getDouble("latitude");
		
		this.projectId = json.getInt("projectId");
		
		if (json.containsKey("breakNodePid")){
			this.breakNodePid = json.getInt("breakNodePid");
		}

		Coordinate coord = new Coordinate(lng, lat);

		this.point = geometryFactory.createPoint(coord);

		this.link1 = new RdLink();

		this.link2 = new RdLink();
		
		if (json.containsKey("infect") && json.getInt("infect") == 1){
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


	public RdNode getBreakNode() {
		return breakNode;
	}

	public void setBreakNode(RdNode breakNode) {
		this.breakNode = breakNode;
	}

	
}
