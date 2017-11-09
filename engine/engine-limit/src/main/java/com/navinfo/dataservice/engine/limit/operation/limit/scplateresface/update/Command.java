package com.navinfo.dataservice.engine.limit.operation.limit.scplateresface.update;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Command extends AbstractCommand{
	
	private String requester;

	private String geometryId;
	
	private JSONObject content;
	
	private ScPlateresFace face;

	private List<String> ids = null;

	public List<String> getIds() {
		return ids;
	}

	private String boundaryLink;

	public String getBoundaryLink() {
		return boundaryLink;
	}

	private List<ScPlateresFace> faces = null;

	public List<ScPlateresFace> getFaces() {
		return faces;
	}

	public void setFaces(List<ScPlateresFace> faces) {
		this.faces = faces;
	}

	public Command(JSONObject json, String requester){
		this.requester = requester;

		this.content = json.getJSONObject("data");

		if (json.containsKey("objIds")) {

			ids = new ArrayList<>();

			if (this.content.containsKey("boundaryLink")) {

				boundaryLink = this.content.getString("boundaryLink");

				ids = new ArrayList<>(JSONArray.toCollection(json.getJSONArray("objIds")));
			}

			return;
		}
		
		geometryId = json.getString("geomId");
		

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
	
	public void setFace(ScPlateresFace value){
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
