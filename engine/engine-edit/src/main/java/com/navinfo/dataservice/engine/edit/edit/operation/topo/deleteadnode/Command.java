package com.navinfo.dataservice.engine.edit.edit.operation.topo.deleteadnode;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

/**
 * @author zhaokk
 * 删除行政区划点参数基础类 
 */
public class Command extends AbstractCommand {
	
	private String requester;

	private int nodePid;
	
	private AdNode node;
	
	private List<AdLink> links;
	
	private List<Integer> linkPids;
	
	private List<AdNode> nodes;
	
	private List<Integer> nodePids;
	
	private List<Integer> facePids;
	
	private List<AdFace> faces;
	
	
	public List<Integer> getFacePids() {
		return facePids;
	}

	public void setFacePids(List<Integer> facePids) {
		this.facePids = facePids;
	}

	public List<AdFace> getFaces() {
		return faces;
	}

	public void setFaces(List<AdFace> faces) {
		this.faces = faces;
	}

	public List<AdLink> getLinks() {
		return links;
	}

	public void setLinks(List<AdLink> links) {
		this.links = links;
	}

	public List<AdNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<AdNode> nodes) {
		this.nodes = nodes;
	}

	public List<Integer> getNodePids() {
		return nodePids;
	}

	public void setNodePids(List<Integer> nodePids) {
		this.nodePids = nodePids;
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

	public AdNode getNode() {
		return node;
	}

	public void setNode(AdNode node) {
		this.node = node;
	}

	@Override
	public OperType getOperType() {
		
		return OperType.DELETE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.ADNODE;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}
	
	public Command(JSONObject json,String requester) {
		this.requester = requester;
		
		this.nodePid = json.getInt("objId");
		
		this.setProjectId(json.getInt("projectId"));
		
	}

}
