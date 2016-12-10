package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.create;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand{
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	private String requester;
	
	private int nodePid;
	
	private JSONArray nodeArray = new JSONArray();
	
	private JSONArray linkArray = new JSONArray();
	
	public JSONArray getNodeArray() {
		return nodeArray;
	}

	public void setNodeArray(JSONArray nodeArray) {
		this.nodeArray = nodeArray;
	}

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.RDINTER;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) throws Exception{
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));
		
		JSONObject data = json.getJSONObject("data");

		this.nodeArray = data.getJSONArray("nodes");
		
		if(data.containsKey("links"))
		{
			this.linkArray = data.getJSONArray("links");
		}
	}

	public JSONArray getLinkArray() {
		return linkArray;
	}

	public void setLinkArray(JSONArray linkArray) {
		this.linkArray = linkArray;
	}
}
