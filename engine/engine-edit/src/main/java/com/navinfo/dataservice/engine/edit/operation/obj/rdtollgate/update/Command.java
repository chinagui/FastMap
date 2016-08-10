package com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * @Title: Command.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月10日 下午2:38:49
 * @version: v1.0
 */
public class Command extends AbstractCommand {

	private String requester;
	
	private RdTollgate tollgate;

	private JSONObject content;
	
	public JSONObject getContent() {
		return content;
	}

	public RdTollgate getTollgate() {
		return tollgate;
	}


	public void setTollgate(RdTollgate tollgate) {
		this.tollgate = tollgate;
	}


	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		this.content = json.getJSONObject("data");
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
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
