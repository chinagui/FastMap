package com.navinfo.dataservice.engine.edit.operation.obj.samepoi.create;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @Title: Command.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月29日 上午10:14:11
 * @version: v1.0
 */
public class Command extends AbstractCommand {

	private String requester;

	private JSONArray pidArray;

	public JSONArray getPidArray() {
		return pidArray;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));

		this.pidArray = json.getJSONArray("poiPids");
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
		return ObjType.IXSAMEPOI;
	}

}
