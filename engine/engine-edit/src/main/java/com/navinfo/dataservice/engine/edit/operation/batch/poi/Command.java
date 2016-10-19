package com.navinfo.dataservice.engine.edit.operation.batch.poi;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand implements ICommand {

	private String requester;
	private int pid;
	private IxPoi poi;

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
		try {
			this.requester = requester;
			this.setPid(json.getInt("pid"));
			this.setDbId(json.getInt("dbId"));
			IxPoi poi = new IxPoi();
			poi.Unserialize(json.getJSONObject("poi"));
			this.setPoi(poi);
		} catch (Exception e) {
			throw e;
		}
		
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public IxPoi getPoi() {
		return poi;
	}

	public void setPoi(IxPoi poi) {
		this.poi = poi;
	}

}
