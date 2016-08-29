package com.navinfo.dataservice.engine.edit.operation.obj.samepoi.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxSamepoi;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

/**
 * @Title: Command.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月29日 上午10:35:42
 * @version: v1.0
 */
public class Command extends AbstractCommand {

	private String requester;

	private JSONObject content;
	
	private IxSamepoi samepoi;
	
	public IxSamepoi getSamepoi() {
		return samepoi;
	}

	public void setSamepoi(IxSamepoi samepoi) {
		this.samepoi = samepoi;
	}

	public JSONObject getContent() {
		return content;
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
		return ObjType.IXSAMEPOI;
	}

}
