package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonelink;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand{
	
	private String requester;

	private int linkPid;
	
	private ZoneLink link;
	
	private List<Integer> nodePids;
	
	private List<ZoneNode> nodes;
	
	private List<ZoneFace> faces;
	
	public ZoneLink getLink() {
		return link;
	}

	public void setLink(ZoneLink link) {
		this.link = link;
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

	
	private boolean isCheckInfect = false;
	
	public boolean isCheckInfect() {
		return isCheckInfect;
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
		
		this.setDbId(json.getInt("dbId"));
		
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
		return ObjType.ZONELINK;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}

}
