package com.navinfo.dataservice.engine.edit.operation.obj.poiparent.delete;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand implements ICommand {

	private String requester;

	private JSONObject content;
	
	private int objId;

	private ObjType objType = ObjType.IXPOIPARENT;
	
	public JSONObject getContent() {
		return content;
	}

	public void setContent(JSONObject content) {
		this.content = content;
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}

	@Override
	public ObjType getObjType() {
		return objType;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
	    this.requester = requester;
		this.setDbId(json.getInt("dbId"));

		this.objId = json.getInt("objId");
	}

	public int getObjId() {
		return objId;
	}

	public void setObjId(int objId) {
		this.objId = objId;
	}

	public void setObjType(ObjType objType) {
		this.objType = objType;
	}
}
