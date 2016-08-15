package com.navinfo.dataservice.engine.edit.operation.obj.rdlane.update;

import net.sf.json.JSONObject;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

/**
 * @author zhaokk 修改详细车道参数基础类
 */
public class Command extends AbstractCommand {

	private String requester;
	private RdLane rdLane;

	private JSONObject content;

	private int lanePid;

	public void setContent(JSONObject content) {
		this.content = content;
	}

	public RdLane getRdLane() {
		return rdLane;
	}

	public void setRdLane(RdLane rdLane) {
		this.rdLane = rdLane;
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDLANE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		this.content = json.getJSONObject("data");
		this.lanePid = this.content.getInt("pid");

	}

	public int getLanePid() {
		return lanePid;
	}

	public void setLanePid(int lanePid) {
		this.lanePid = lanePid;
	}

	public JSONObject getContent() {
		return content;
	}

}
