package com.navinfo.dataservice.engine.edit.operation.obj.rdspeedbump.create;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * @Title: Command.java
 * @Description: 创建减速带参数
 * @author zhangyt
 * @date: 2016年8月8日 上午10:45:26
 * @version: v1.0
 */
public class Command extends AbstractCommand {

	private String requester;
	
	private int inLinkPid;
	
	private int inNodePid;

	public int getInLinkPid() {
		return inLinkPid;
	}

	public int getInNodePid() {
		return inNodePid;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		
		JSONObject data = json.getJSONObject("data");
		this.inLinkPid = data.getInt("inLinkPid");
		this.inNodePid = data.getInt("inNodePid");

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
		return ObjType.RDSPEEDBUMP;
	}

}
