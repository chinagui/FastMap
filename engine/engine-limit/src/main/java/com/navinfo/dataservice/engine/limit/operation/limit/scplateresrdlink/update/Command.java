package com.navinfo.dataservice.engine.limit.operation.limit.scplateresrdlink.update;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresLink;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand{
	
	private String requester;

	private String geometryId;
	
	private JSONObject content;
	
	private ScPlateresLink link;
	
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
	
	public ScPlateresLink getLink(){
		return this.link;
	}
	
	public void setLink(ScPlateresLink value){
		this.link = value;
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
		return LimitObjType.SCPLATERESLINK;
	}

	
}
