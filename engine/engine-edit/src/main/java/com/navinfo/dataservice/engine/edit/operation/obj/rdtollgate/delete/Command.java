package com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * @Title: Command.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月10日 下午2:13:18
 * @version: v1.0
 */
public class Command extends AbstractCommand {

	private String requester;

	private int pid;

	private RdTollgate rdTollgate;

	public RdTollgate getRdTollgate() {
		return rdTollgate;
	}

	public void setRdTollgate(RdTollgate rdTollgate) {
		this.rdTollgate = rdTollgate;
	}

	public int getPid() {
		return pid;
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
		return ObjType.RDTOLLGATE;
	}

}
