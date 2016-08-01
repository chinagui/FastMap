/**
 * 
 */
package com.navinfo.dataservice.engine.edit.operation.obj.rdse.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.se.RdSe;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * 
 * @Title: Command.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月1日 下午2:48:31
 * @version: v1.0
 */
public class Command extends AbstractCommand {

	private String requester;

	private RdSe rdSe;

	private JSONObject content;

	public JSONObject getContent() {
		return content;
	}

	public RdSe getRdSe() {
		return rdSe;
	}

	public void setRdSe(RdSe rdSe) {
		this.rdSe = rdSe;
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
		return ObjType.RDSE;
	}

}
