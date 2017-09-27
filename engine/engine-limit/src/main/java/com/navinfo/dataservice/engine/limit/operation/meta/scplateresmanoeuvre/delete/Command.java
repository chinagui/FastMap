package com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresManoeuvre;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand{
	private String requester;

	private int manoeuvreId = 0;

	private JSONObject content;

	private ScPlateresManoeuvre manoeuvre;
	
	private String groupId;

	public int getManoeuvreId() {
		return this.manoeuvreId;
	}

	public JSONObject getContent() {
		return this.content;
	}

	public ScPlateresManoeuvre getManoeuvre() {
		return this.manoeuvre;
	}

	public void setManoeuvre(ScPlateresManoeuvre value) {
		this.manoeuvre = value;
	}
	
	public String getGroupId(){
		return this.groupId;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.manoeuvreId = json.getInt("objId");
		this.groupId = json.getString("groupId");
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}

	@Override
	public DbType getDbType() {
		return DbType.LIMITDB;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public LimitObjType getObjType() {
		return LimitObjType.SCPLATERESMANOEUVRE;
	}
}
