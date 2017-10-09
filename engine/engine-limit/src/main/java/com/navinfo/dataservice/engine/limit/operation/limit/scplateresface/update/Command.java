package com.navinfo.dataservice.engine.limit.operation.limit.scplateresface.update;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand{
	
	private String requester;

	private String geometryId;
	
	private JSONObject content;
	
	private ScPlateresFace face;
	
	public Command(JSONObject json, String requester){
		this.requester = requester;
		
		geometryId = json.getString("geomId");
		
		content = json.getJSONObject("data");
	}
	
	public String getGemetryId(){
		return this.geometryId;
	}
	
	public JSONObject getContent(){
		return this.content;
	}
	
	public ScPlateresFace getFace(){
		return this.face;
	}
	
	public void setLink(ScPlateresFace value){
		this.face = value;
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
		return LimitObjType.SCPLATERESFACE;
	}

}
