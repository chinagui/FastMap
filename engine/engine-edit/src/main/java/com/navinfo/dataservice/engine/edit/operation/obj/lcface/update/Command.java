package com.navinfo.dataservice.engine.edit.operation.obj.lcface.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * @Title: Command.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月30日 上午9:46:50
 * @version: v1.0
 */
public class Command extends AbstractCommand {

	private String requester;

	private JSONObject content;
	
	private LcFace face;
	
	public LcFace getFace() {
		return face;
	}

	public void setFace(LcFace face) {
		this.face = face;
	}

	public JSONObject getContent() {
		return content;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));

		this.content = json.getJSONObject("data");
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
		return ObjType.LCFACE;
	}

}
