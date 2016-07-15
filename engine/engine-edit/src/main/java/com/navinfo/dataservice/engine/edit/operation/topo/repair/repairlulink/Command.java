package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink;

import java.util.List;

import org.json.JSONException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;
	
	private int linkPid;
	
	private JSONObject linkGeom;
	
	private JSONArray interNodes;
	
	private JSONArray interLines;
	
	private LuLink updateLink;
	
	private List<LuFace> faces;
	
	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public JSONObject getLinkGeom() {
		return linkGeom;
	}

	public void setLinkGeom(JSONObject linkGeom) {
		this.linkGeom = linkGeom;
	}

	public JSONArray getInterNodes() {
		return interNodes;
	}

	public void setInterNodes(JSONArray interNodes) {
		this.interNodes = interNodes;
	}

	public JSONArray getInterLines() {
		return interLines;
	}

	public void setInterLines(JSONArray interLines) {
		this.interLines = interLines;
	}

	public LuLink getUpdateLink() {
		return updateLink;
	}

	public void setUpdateLink(LuLink updateLink) {
		this.updateLink = updateLink;
	}

	public List<LuFace> getFaces() {
		return faces;
	}

	public void setFaces(List<LuFace> faces) {
		this.faces = faces;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	@Override
	public OperType getOperType() {
		return OperType.REPAIR;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.LULINK;
	}

	public Command(JSONObject json, String requester) throws JSONException{
		this.requester = requester;
		
		this.setDbId(json.getInt("dbId"));
		
		this.linkPid = json.getInt("objId");
		
		JSONObject data = json.getJSONObject("data");
		
		JSONObject geometry = data.getJSONObject("geometry");
		
		this.linkGeom = GeoTranslator.jts2Geojson(GeoTranslator.geojson2Jts(geometry, 1, 5));
		//修行后挂接对应Lu_Link信息
		this.interLines = data.getJSONArray("interLinks");
		//修行后挂接对应的Lu_Node信息
		this.interNodes = data.getJSONArray("interNodes");
	}
}
