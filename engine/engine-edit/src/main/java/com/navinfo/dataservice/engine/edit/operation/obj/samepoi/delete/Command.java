package com.navinfo.dataservice.engine.edit.operation.obj.samepoi.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoi;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * @Title: Command.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月29日 上午10:29:46
 * @version: v1.0
 */
public class Command extends AbstractCommand {

	private String requester;

	private IxSamepoi samepoi;

	private int pid;

	public int getPid() {
		return pid;
	}

	public IxSamepoi getSamepoi() {
		return samepoi;
	}

	public void setSamepoi(IxSamepoi samepoi) {
		this.samepoi = samepoi;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));

		this.pid = json.getInt("objId");
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
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
