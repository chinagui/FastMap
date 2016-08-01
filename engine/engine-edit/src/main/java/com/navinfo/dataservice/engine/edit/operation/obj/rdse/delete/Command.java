package com.navinfo.dataservice.engine.edit.operation.obj.rdse.delete;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * @Title: Command.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月1日 下午2:40:30
 * @version: v1.0
 */
public class Command extends AbstractCommand {

	private String requester;
	
	private int pid;

	private RdSe rdSe;

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		this.pid = json.getInt("objId");
	}

	public RdSe getRdSe() {
		return rdSe;
	}

	public void setRdSe(RdSe rdSe) {
		this.rdSe = rdSe;
	}

	public int getPid() {
		return pid;
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
		return ObjType.RDSE;
	}

}
