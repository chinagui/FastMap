package com.navinfo.dataservice.engine.edit.operation.obj.rdwarninginfo.create;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand implements ICommand{

	private String requester;

	/**
	 * RDLink的pid
	 */
	private Integer linkPid;

	/**
	 * RDNode的pid
	 */
	private Integer nodePid;	
	
	
	public Integer getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(Integer linkPid) {
		this.linkPid = linkPid;
	}

	public Integer getNodePid() {
		return nodePid;
	}

	public void setNodePid(Integer nodePid) {
		this.nodePid = nodePid;
	}
	
	
	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDWARNINGINFO;
	}
	
	public Command(JSONObject json, String requester) {
		
		this.requester = requester;
		
		this.setDbId(json.getInt("dbId"));
		
		JSONObject data = json.getJSONObject("data");
		
		if (data.containsKey("linkPid")) {
			this.setLinkPid(data.getInt("linkPid"));
		}
		else
		{
			this.setLinkPid(0);
		}
		
		if (data.containsKey("nodePid")) {
			this.setLinkPid(data.getInt("nodePid"));
		}
		else
		{
			this.setNodePid(0);
		}
		
	}

}
