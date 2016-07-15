package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonenode;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

/**
 * @author zhaokk
 * 删除ZONE点参数基础类 
 */
public class Command extends AbstractCommand {
	
	private String requester;

	public List<ZoneLink> getLinks() {
		return links;
	}

	public void setLinks(List<ZoneLink> links) {
		this.links = links;
	}

	public List<ZoneNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<ZoneNode> nodes) {
		this.nodes = nodes;
	}

	public List<ZoneFace> getFaces() {
		return faces;
	}

	public void setFaces(List<ZoneFace> faces) {
		this.faces = faces;
	}

	private int nodePid;
	
	private ZoneNode node;
	
	private List<ZoneLink> links;
	
	private List<Integer> linkPids;
	
	private List<ZoneNode> nodes;
	
	private List<Integer> nodePids;
	
	private List<Integer> facePids;
	
	private List<ZoneFace> faces;
	
	
	public List<Integer> getFacePids() {
		return facePids;
	}

	public void setFacePids(List<Integer> facePids) {
		this.facePids = facePids;
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

	public ZoneNode getNode() {
		return node;
	}

	public void setNode(ZoneNode node) {
		this.node = node;
	}

	@Override
	public OperType getOperType() {
		
		return OperType.DELETE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.ZONENODE;
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
