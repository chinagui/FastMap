package com.navinfo.dataservice.engine.edit.edit.operation.topo.deleteadlink;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;

public class Command implements ICommand {
	
	private String requester;

	private int linkPid;
	
	private AdLink link;
	
	private List<Integer> nodePids;
	
	private List<AdNode> nodes;
	
	private List<AdFace> faces;
	
	public AdLink getLink() {
		return link;
	}

	public void setLink(AdLink link) {
		this.link = link;
	}

	public List<AdNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<AdNode> nodes) {
		this.nodes = nodes;
	}

	public List<AdFace> getFaces() {
		return faces;
	}

	public void setFaces(List<AdFace> faces) {
		this.faces = faces;
	}

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
	public List<Integer> getNodePids() {
		return nodePids;
	}

	public void setNodePids(List<Integer> nodePids) {
		this.nodePids = nodePids;
	}
	public Command(JSONObject json,String requester) {
		this.requester = requester;
		
		this.linkPid = json.getInt("objId");
		
		this.projectId = json.getInt("projectId");
		
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

	@Override
	public OperType getOperType() {
		
		return OperType.DELETE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.ADLINK;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}

}
