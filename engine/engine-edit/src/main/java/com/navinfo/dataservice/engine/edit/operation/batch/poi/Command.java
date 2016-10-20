package com.navinfo.dataservice.engine.edit.operation.batch.poi;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand implements ICommand {

	private String requester;
	private int pid;
	private JSONObject content;
	private boolean isLock = true;

	@Override
	public OperType getOperType() {
		return OperType.BATCH;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.IXPOI;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) throws Exception {
		this.requester = requester;
		this.pid = json.getInt("pid");
		this.setDbId(json.getInt("dbId"));
		this.content = json.getJSONObject("change");
		if(json.containsKey("isLock"))
		{
			isLock = json.getBoolean("isLock");
		}
		
	}

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
	
	public boolean isLock() {
		return isLock;
	}

	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}

}
