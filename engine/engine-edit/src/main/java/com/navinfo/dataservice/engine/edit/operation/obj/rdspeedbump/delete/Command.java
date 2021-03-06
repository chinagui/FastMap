package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * @Title: Command.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月8日 上午11:11:03
 * @version: v1.0
 */
public class Command extends AbstractCommand{
	
	private String requester;
	
	private int pid;
	
	private RdSpeedbump rdSpeedbump;

	public int getPid() {
		return pid;
	}

	public RdSpeedbump getRdSpeedbump() {
		return rdSpeedbump;
	}


	public void setRdSpeedbump(RdSpeedbump rdSpeedbump) {
		this.rdSpeedbump = rdSpeedbump;
	}


	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		this.pid = json.getInt("objId");
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
		return ObjType.RDSPEEDBUMP;
	}

}
