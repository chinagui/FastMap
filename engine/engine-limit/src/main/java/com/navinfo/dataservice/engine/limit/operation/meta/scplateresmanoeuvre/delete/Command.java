package com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresManoeuvre;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand{
	private String requester;

	private JSONArray manoeuvreId;

	private JSONObject content;

	private List<ScPlateresManoeuvre> manoeuvre;
	
	private String groupId;

	public JSONArray getManoeuvreId() {
		return this.manoeuvreId;
	}

	public JSONObject getContent() {
		return this.content;
	}

	public List<ScPlateresManoeuvre> getManoeuvre() {
		return this.manoeuvre;
	}

	public void setManoeuvre(List<ScPlateresManoeuvre> value) {
		this.manoeuvre = value;
	}
	
	public String getGroupId(){
		return this.groupId;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.manoeuvreId = json.getJSONArray("objId");
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
