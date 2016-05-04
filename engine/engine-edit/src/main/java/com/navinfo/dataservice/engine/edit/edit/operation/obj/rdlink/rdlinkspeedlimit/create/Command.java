package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlink.rdlinkspeedlimit.create;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {
	
	private String requester;
	private int pid;
	
	private JSONArray linkPids;

	public int getPid() {
		return pid;
	}

	public JSONArray getLinkPids() {
		return linkPids;
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
		
		return ObjType.RDLINKSPEEDLIMIT;
	}
	
	public Command(JSONObject json,String requester){
		this.requester = requester;

		this.setProjectId(json.getInt("projectId"));
		
		JSONObject data = json.getJSONObject("data");
		
		this.pid = data.getInt("pid");
		
		this.linkPids = data.getJSONArray("linkPids");
	}

}
