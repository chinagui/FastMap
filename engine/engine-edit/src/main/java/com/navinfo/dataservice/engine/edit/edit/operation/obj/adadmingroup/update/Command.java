package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmingroup.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

public class Command extends AbstractCommand implements ICommand {

	private String requester;


	private JSONObject content;

	
	public JSONObject getContent() {
		return content;
	}

	public void setContent(JSONObject content) {
		this.content = content;
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDRESTRICTION;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setSubTaskId(json.getInt("subTaskId"));

		this.content = json.getJSONObject("data");
		
		
	}
}
