package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlink.rdlinkspeedlimit.create;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.edit.edit.model.ObjType;
import com.navinfo.dataservice.engine.edit.edit.operation.ICommand;
import com.navinfo.dataservice.engine.edit.edit.operation.OperType;

public class Command implements ICommand {
	
	private String requester;
	
	private int projectId;
	
	private int pid;
	
	private JSONArray linkPids;

	public int getProjectId() {
		return projectId;
	}

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

		this.projectId = json.getInt("projectId");
		
		JSONObject data = json.getJSONObject("data");
		
		this.pid = data.getInt("pid");
		
		this.linkPids = data.getJSONArray("linkPids");
	}

}
