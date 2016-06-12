package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmin.move;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

public class Command extends AbstractCommand   implements ICommand {
	
	private int pid;
	
	private double longitude;
	
	private double latitude;
	
	private String requester;
	
	/**
	 * 引导线RDLink的pid
	 */
	private int linkPid;

	
	public Command(JSONObject json,String requester){
		
		JSONObject data = json.getJSONObject("data");
		
		this.longitude = Math.round(data.getDouble("longitude")*100000)/100000.0;
		
		this.latitude = Math.round(data.getDouble("latitude")*100000)/100000.0;
		
		this.setDbId(json.getInt("subTaskId"));
		
		if(data.containsKey("linkPid") && !data.get("linkPid").equals("null"))
		{
			this.linkPid = data.getInt("linkPid");
		}
		
		this.pid = json.getInt("objId");
	}

	@Override
	public OperType getOperType() {
		
		return OperType.MOVE;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}

	@Override
	public ObjType getObjType() {
		
		return ObjType.RDNODE;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}
	
}
