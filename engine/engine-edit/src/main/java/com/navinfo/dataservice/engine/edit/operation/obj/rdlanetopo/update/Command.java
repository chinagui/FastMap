package com.navinfo.dataservice.engine.edit.operation.obj.rdlanetopo.update;

import net.sf.json.JSONObject;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

/**
 * @author zhaokk 修改详细车道联通参数基础类
 */
public class Command extends AbstractCommand {

	private String requester;
	private RdLaneTopoDetail detail;

	private JSONObject content;

	private int topoId;

	public void setContent(JSONObject content) {
		this.content = content;
	}

	public RdLaneTopoDetail getDetail() {
		return detail;
	}

	public void setDetail(RdLaneTopoDetail detail) {
		this.detail = detail;
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDLANETOPODETAIL;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		this.content = json.getJSONObject("data");
		this.setTopoId(this.content.getInt("pid"));

	}


	public int getTopoId() {
		return topoId;
	}

	public void setTopoId(int topoId) {
		this.topoId = topoId;
	}

	public JSONObject getContent() {
		return content;
	}

}
