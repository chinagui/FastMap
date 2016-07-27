package com.navinfo.dataservice.engine.edit.operation.obj.luface.delete;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {
	
	private String requester;
	
	private int faceId;

	public int getFaceId() {
		return faceId;
	}

	public void setFaceId(int faceId) {
		this.faceId = faceId;
	}

	public void setRequester(String requester) {
		this.requester = requester;
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
		return ObjType.LUFACE;
	}

	public Command(JSONObject json, String requester) throws Exception{
		this.requester = requester;
        this.faceId = json.getInt("objId");
		this.setDbId(json.getInt("dbId"));
	}
}
