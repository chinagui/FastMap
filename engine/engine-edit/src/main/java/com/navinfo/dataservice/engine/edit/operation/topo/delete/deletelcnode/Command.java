package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelcnode;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

/**
 * @author zhaokk
 * 删除行政区划点参数基础类 
 */
public class Command extends AbstractCommand {
	
	private String requester;

	private int nodePid;
	
	private LcNode node;
	
	private List<LcLink> links;
	
	private List<Integer> linkPids;
	
	private List<LcNode> nodes;
	
	private List<Integer> nodePids;
	
	private List<Integer> facePids;
	
	private List<LcFace> faces;

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public LcNode getNode() {
		return node;
	}

	public void setNode(LcNode node) {
		this.node = node;
	}

	public List<LcLink> getLinks() {
		return links;
	}

	public void setLinks(List<LcLink> links) {
		this.links = links;
	}

	public List<Integer> getLinkPids() {
		return linkPids;
	}

	public void setLinkPids(List<Integer> linkPids) {
		this.linkPids = linkPids;
	}

	public List<LcNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<LcNode> nodes) {
		this.nodes = nodes;
	}

	public List<Integer> getNodePids() {
		return nodePids;
	}

	public void setNodePids(List<Integer> nodePids) {
		this.nodePids = nodePids;
	}

	public List<Integer> getFacePids() {
		return facePids;
	}

	public void setFacePids(List<Integer> facePids) {
		this.facePids = facePids;
	}

	public List<LcFace> getFaces() {
		return faces;
	}

	public void setFaces(List<LcFace> faces) {
		this.faces = faces;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	@Override
	public OperType getOperType() {
		
		return OperType.DELETE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.LCNODE;
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
