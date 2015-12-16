package com.navinfo.dataservice.FosEngine.edit.operation.topo.deletelink;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.restrict.RdRestriction;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.OperType;

public class Command implements ICommand {
	
	private String requester;

	private int linkPid;
	
	private RdLink link;
	
	private List<RdNode> nodes;
	
	private List<RdRestriction> restrictions;
	
	private int projectId;
	
	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public RdLink getLink() {
		return link;
	}

	public void setLink(RdLink link) {
		this.link = link;
	}

	public List<RdNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<RdNode> nodes) {
		this.nodes = nodes;
	}

	public List<RdRestriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<RdRestriction> restrictions) {
		this.restrictions = restrictions;
	}

	public Command(JSONObject json,String requester) {
		this.requester = requester;
		
		this.linkPid = json.getInt("objId");
		
		this.projectId = json.getInt("projectId");
		
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
		return ObjType.RDLINK;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}

}
