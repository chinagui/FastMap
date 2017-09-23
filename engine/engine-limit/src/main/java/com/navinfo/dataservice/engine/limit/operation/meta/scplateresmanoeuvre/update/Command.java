package com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.update;

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
	
	public int getManoeuvreId(){
		return this.manoeuvreId;
	}
	
	public JSONObject getContent(){
		return this.content;
	}
	
	public ScPlateresManoeuvre getManoeuvre(){
		return this.manoeuvre;
	}
	
	public void setManoeuvre(ScPlateresManoeuvre value){
		this.manoeuvre = value;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		
		JSONObject data = json.getJSONObject("data");
		this.manoeuvreId = data.getInt("manoeuvreId");
		this.content = data.getJSONObject("data");
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
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
