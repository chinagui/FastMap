package com.navinfo.dataservice.engine.limit.operation.limit.scplateresrdface.delete;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand{
private String requester;
	
	private JSONArray geometryIds;
	
	private List<ScPlateresFace> scplateresfaces = new ArrayList<>();
	
	public Command(JSONObject json,String requester){
		this.requester = requester;
		
		this.geometryIds = json.getJSONArray("objId");
	}
	
	public List<ScPlateresFace> getscplateresFaces(){
		return this.scplateresfaces;
	}
	
	public void setscplateresFaces(List<ScPlateresFace> values){
		this.scplateresfaces = values;
	}
	
	public JSONArray getGeometryIds(){
		return this.geometryIds;
	}
	
	@Override
	public OperType getOperType() {
		// TODO Auto-generated method stub
		return OperType.DELETE;
	}

	@Override
	public DbType getDbType() {
		// TODO Auto-generated method stub
		return DbType.LIMITDB;
	}

	@Override
	public String getRequester() {
		// TODO Auto-generated method stub
		return this.requester;
	}

	@Override
	public LimitObjType getObjType() {
		// TODO Auto-generated method stub
		return LimitObjType.SCPLATERESFACE;
	}
	
}
