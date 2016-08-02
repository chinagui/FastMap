package com.navinfo.dataservice.engine.edit.operation.obj.rdse.create;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * @Title: Command.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月1日 下午2:28:24
 * @version: v1.0
 */
public class Command extends AbstractCommand {

	private String requester;

	private JSONObject content;

	public JSONObject getContent() {
		return content;
	}

	public void setContent(JSONObject content) {
		this.content = content;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		this.content = json.getJSONObject("data");
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDSE;
	}

}
