package com.navinfo.dataservice.engine.edit.edit.operation.topo.delete.deletelulink;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

public class Command extends AbstractCommand{

	private String requester;
	
	private int linkPid;
	
	private LuLink link;
	
	private List<Integer> nodePids;
	
	private List<LuNode> nodes;
	
	private List<LuFace> faces;
	
	private boolean isCheckInfect = false;
	
	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public LuLink getLink() {
		return link;
	}

	public void setLink(LuLink link) {
		this.link = link;
	}

	public List<Integer> getNodePids() {
		return nodePids;
	}

	public void setNodePids(List<Integer> nodePids) {
		this.nodePids = nodePids;
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

	public void setRequester(String requester) {
		this.requester = requester;
	}

	public boolean isCheckInfect() {
		return isCheckInfect;
	}

	public void setCheckInfect(boolean isCheckInfect) {
		this.isCheckInfect = isCheckInfect;
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.LULINK;
	}
	
	public Command(JSONObject json,String requester) {
		this.requester = requester;
		
		this.linkPid = json.getInt("objId");
		
		this.setDbId(json.getInt("dbId"));
		
		if (json.containsKey("infect") && json.getInt("infect") == 1){
			this.isCheckInfect = true;
		}
		
	}

}
