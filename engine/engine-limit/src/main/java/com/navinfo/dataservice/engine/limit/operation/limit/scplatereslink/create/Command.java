package com.navinfo.dataservice.engine.limit.operation.limit.scplatereslink.create;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand{

	private String requester;
	
	private int seq = 0;
	
	private Geometry geo;
	
	private String groupId;
	
	private JSONArray links;
	
	private int dbId;

	public Command(JSONObject json, String requester) {

		this.requester = requester;
		
		JSONObject data = json.getJSONObject("data");
		
		this.dbId = json.getInt("dbId");
		
		this.groupId = data.getString("groupId");
		
		this.links = data.getJSONArray("links");
	}
	
	public int getDbId(){
		return this.dbId;
	}
	
	public int getSeq(){
		return this.seq;
	}
	
	public Geometry getGeo(){
		return this.geo;
	} 
	
	public String getGroupId(){
		return this.groupId;
	}
	
	public JSONArray getLinks(){
		return this.links;
	}

	@Override
	public OperType getOperType() {
		// TODO Auto-generated method stub
		return OperType.CREATE;
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
		return LimitObjType.SCPLATERESLINK;
	}
}
