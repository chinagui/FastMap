package com.navinfo.dataservice.engine.edit.edit.operation.topo.depart.departrdnode;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private int linkPid;

	private String requester;

	private int eNodePid = -1;

	private int sNodePid = -1;

	private double slon;

	private double slat;

	private double elon;

	private double elat;

	private List<RdRestriction> restrictions;

	private List<RdLaneConnexity> lanes;

	private List<RdBranch> branches;

	public Command(JSONObject json, String requester) throws Exception {
		this.requester = requester;

		JSONObject data = json.getJSONObject("data");

		this.linkPid = data.getInt("linkPid");

		if (data.containsKey("sNodePid")) {
			this.sNodePid = data.getInt("sNodePid");

			this.slon = Math.round(data.getDouble("slon") * 100000) / 100000.0;

			this.slat = Math.round(data.getDouble("slat") * 100000) / 100000.0;
		}

		if (data.containsKey("eNodePid")) {
			this.sNodePid = data.getInt("eNodePid");

			this.elon = Math.round(data.getDouble("elon") * 100000) / 100000.0;

			this.elat = Math.round(data.getDouble("elat") * 100000) / 100000.0;
		}

		this.setDbId(json.getInt("dbId"));
		//createGlmList();
	}

	public int getLinkPid() {
		return linkPid;
	}

	public int geteNodePid() {
		return eNodePid;
	}

	public int getsNodePid() {
		return sNodePid;
	}

	public double getSlon() {
		return slon;
	}

	public double getSlat() {
		return slat;
	}

	public double getElon() {
		return elon;
	}

	public double getElat() {
		return elat;
	}

	public List<RdRestriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<RdRestriction> restrictions) {
		this.restrictions = restrictions;
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

	@Override
	public OperType getOperType() {
		return OperType.DEPART;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDLINK;
	}
//	public void createGlmList() throws Exception {
//		// TODO Auto-generated method stub
//		List<IRow> glmList=new ArrayList<IRow>();		
//		
//		RdLink linkObj=new RdLink();
//		RdNode eNode=new RdNode();
//		RdNode sNode=new RdNode();
//		linkObj.setPid(this.linkPid);
//		glmList.add(linkObj);
//		if(this.sNodePid!=-1){sNode.setPid(this.sNodePid);glmList.add(sNode);}
//		if(this.eNodePid!=-1){eNode.setPid(this.eNodePid);glmList.add(eNode);}	
//		
//		this.setGlmList(glmList);
//	}

}
