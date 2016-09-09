package com.navinfo.dataservice.engine.edit.operation.obj.poi.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand  implements ICommand{


	private String requester;


	private JSONObject content;
	
	private int pid;
	
	private boolean isLock = true;
	
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

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.IXPOI;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.content = json.getJSONObject("data");	
		
		this.pid = this.content.getInt("pid");
		
		if(json.containsKey("isLock"))
		{
			isLock = json.getBoolean("isLock");
		}
	}

	public boolean isLock() {
		return isLock;
	}

	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}
	
}
