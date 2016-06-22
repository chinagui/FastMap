package com.navinfo.dataservice.engine.edit.edit.operation.topo.delete.deleterwnode;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;

	private int nodePid;

	private List<RwLink> links;

	private List<RdGsc> rdGscs;

	private List<RwNode> nodes;

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public List<RwLink> getLinks() {
		return links;
	}

	public void setLinks(List<RwLink> links) {
		this.links = links;
	}

	public List<RdGsc> getRdGscs() {
		return rdGscs;
	}

	public void setRdGscs(List<RdGsc> rdGscs) {
		this.rdGscs = rdGscs;
	}

	public List<RwNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<RwNode> nodes) {
		this.nodes = nodes;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.nodePid = json.getInt("objId");

		this.setDbId(json.getInt("dbId"));

	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RWNODE;
	}

}
