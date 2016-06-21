package com.navinfo.dataservice.engine.edit.edit.operation.topo.delete.deleterwlink;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand{
	
	private String requester;

	private int linkPid;
	
	private RwLink link;
	
	private List<RwNode> nodes;
	
	public RwLink getLink() {
		return link;
	}

	public void setLink(RwLink link) {
		this.link = link;
	}

	public List<RwNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<RwNode> nodes) {
		this.nodes = nodes;
	}

	private boolean isCheckInfect = false;
	
	public boolean isCheckInfect() {
		return isCheckInfect;
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
		return ObjType.RWLINK;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}

}
