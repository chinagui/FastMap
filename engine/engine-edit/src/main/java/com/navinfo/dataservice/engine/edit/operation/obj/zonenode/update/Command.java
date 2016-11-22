package com.navinfo.dataservice.engine.edit.operation.obj.zonenode.update;

import net.sf.json.JSONObject;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

/**
 * @author zhaokk 修改ZONE点参数基础类
 */
public class Command extends AbstractCommand {

	private String requester;

	private JSONObject content;

	private int pid;

	private ZoneNode zoneNode;

	public ZoneNode getZoneNode() {
		return zoneNode;
	}

	public void setZoneNode(ZoneNode zoneNode) {
		this.zoneNode = zoneNode;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

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
		return ObjType.ZONENODE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));

		this.content = json.getJSONObject("data");

		this.pid = this.content.getInt("pid");

	}

	public Command(JSONObject json, String requester, ZoneNode node) {
		this(json, requester);
		this.zoneNode = node;

	}

}
