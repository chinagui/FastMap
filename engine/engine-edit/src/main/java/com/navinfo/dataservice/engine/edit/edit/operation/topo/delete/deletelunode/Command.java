package com.navinfo.dataservice.engine.edit.edit.operation.topo.delete.deletelunode;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

/**
 * 删除土地利用点参数基础类 
 */
public class Command extends AbstractCommand {
	
	private String requester;

	private int nodePid;
	
	private LuNode node;
	
	private List<LuLink> links;
	
	private List<Integer> linkPids;
	
	private List<LuNode> nodes;
	
	private List<Integer> nodePids;
	
	private List<Integer> facePids;
	
	private List<LuFace> faces;
	
	
	public List<Integer> getFacePids() {
		return facePids;
	}

	public void setFacePids(List<Integer> facePids) {
		this.facePids = facePids;
	}

	public List<LuLink> getLinks() {
		return links;
	}

	public void setLinks(List<LuLink> links) {
		this.links = links;
	}

	public List<LuNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<LuNode> nodes) {
		this.nodes = nodes;
	}

	public List<LuFace> getFaces() {
		return faces;
	}

	public void setFaces(List<LuFace> faces) {
		this.faces = faces;
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

	public LuNode getNode() {
		return node;
	}

	public void setNode(LuNode node) {
		this.node = node;
	}

	@Override
	public OperType getOperType() {
		
		return OperType.DELETE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.LUNODE;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}
	
	public Command(JSONObject json,String requester) {
		this.requester = requester;
		
		this.nodePid = json.getInt("objId");
		
		this.setDbId(json.getInt("dbId"));
		
	}

}
