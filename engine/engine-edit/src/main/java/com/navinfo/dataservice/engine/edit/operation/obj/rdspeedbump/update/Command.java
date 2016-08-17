package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * @Title: Command.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月9日 下午4:28:30
 * @version: v1.0
 */
public class Command extends AbstractCommand {
	
	private String requester;
	
	private RdSpeedbump speedbump;
	
	private JSONObject content;

	public JSONObject getContent() {
		return content;
	}

	public RdSpeedbump getSpeedbump() {
		return speedbump;
	}

	public void setSpeedbump(RdSpeedbump speedbump) {
		this.speedbump = speedbump;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		content = json.getJSONObject("data");
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
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
