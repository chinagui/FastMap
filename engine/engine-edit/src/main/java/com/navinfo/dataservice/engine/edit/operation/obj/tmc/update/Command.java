package com.navinfo.dataservice.engine.edit.operation.obj.tmc.update;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	protected Logger log = Logger.getLogger(this.getClass());

	private String requester;
	
	private int pid;

	private RdTmclocation rdTmclocation;	
	
	private JSONObject updateContent;
	
	public JSONObject getUpdateContent() {
		return updateContent;
	}

	public void setUpdateContent(JSONObject updateContent) {
		this.updateContent = updateContent;
	}

	public int getPid() {
		return pid;
	}
	
	public RdTmclocation getRdTmclocation() {
		return rdTmclocation;
	}

	public void setRdTmclocation(RdTmclocation rdTmclocation) {
		this.rdTmclocation = rdTmclocation;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDTMCLOCATION;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) throws Exception {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		updateContent = json.getJSONObject("data");
		
		this.pid = updateContent.getInt("pid");
	}
}
