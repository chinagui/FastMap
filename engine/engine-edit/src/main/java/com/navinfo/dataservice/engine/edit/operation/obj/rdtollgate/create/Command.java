package com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.create;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * @Title: Command.java
 * @Description: 收费站新增参数类
 * @author zhangyt
 * @date: 2016年8月10日 下午2:01:07
 * @version: v1.0
 */
public class Command extends AbstractCommand {

	private String requester;

	private int inLinkPid;

	private int nodePid;

	private int outLinkPid;

	public int getInLinkPid() {
		return inLinkPid;
	}

	public int getNodePid() {
		return nodePid;
	}


	public int getOutLinkPid() {
		return outLinkPid;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		
		JSONObject data = json.getJSONObject("data");
		this.inLinkPid = data.getInt("inLinkPid");
		this.nodePid = data.getInt("nodePid");
		this.outLinkPid = data.getInt("outLinkPid");
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
		return ObjType.RDTOLLGATE;
	}

}
