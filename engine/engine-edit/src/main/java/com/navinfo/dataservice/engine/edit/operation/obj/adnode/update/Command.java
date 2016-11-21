package com.navinfo.dataservice.engine.edit.operation.obj.adnode.update;

import net.sf.json.JSONObject;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

/**
 * @author zhaokk 修改行政区划点参数基础类
 */
public class Command extends AbstractCommand {

	public AdNode getNode() {
		return node;
	}

	public void setNode(AdNode node) {
		this.node = node;
	}

	private String requester;

	private AdNode node;

	private JSONObject content;

	private int pid;

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
		return ObjType.ADNODE;
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

	public Command(JSONObject json, String requester, AdNode node) {
		this(json, requester);
		this.node = node;

	}

}
