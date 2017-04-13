package com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.create;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
/**
 * 语音引导 创建参数
 * @author zhaokk
 *
 */
public class Command extends AbstractCommand {

	private String requester;

	private int inLinkPid;

	public JSONArray getArray() {
		return array;
	}

	public void setArray(JSONArray array) {
		this.array = array;
	}

	private int nodePid;

	private JSONArray array;

	public int getInLinkPid() {
		return inLinkPid;
	}

	public void setInLinkPid(int inLinkPid) {
		this.inLinkPid = inLinkPid;
	}

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDVOICEGUIDE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		JSONObject data = json.getJSONObject("data");
		//进入点
		this.nodePid = data.getInt("nodePid");
		//进入线
		this.inLinkPid = data.getInt("inLinkPid");
		//退出线信息
		this.setArray(data.getJSONArray("infos"));

	}
}
