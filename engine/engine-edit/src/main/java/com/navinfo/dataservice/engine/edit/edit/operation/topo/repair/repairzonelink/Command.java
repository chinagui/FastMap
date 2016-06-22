package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair.repairzonelink;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
/**
 * @author zhaokk
 * 修行ZONE线参数基础类 
 */
public class Command extends AbstractCommand {

	private String requester;
	
	private int linkPid;
	
	private JSONObject linkGeom;
	
	private JSONArray interLines;
	
	private ZoneLink updateLink;
	
	public ZoneLink getUpdateLink() {
		return updateLink;
	}

	public void setUpdateLink(ZoneLink updateLink) {
		this.updateLink = updateLink;
	}

	public List<ZoneFace> getFaces() {
		return faces;
	}

	public void setFaces(List<ZoneFace> faces) {
		this.faces = faces;
	}

	private JSONArray interNodes;
	
	private List<ZoneFace> faces;

	public int getLinkPid() {
		return linkPid;
	}

	public JSONObject getLinkGeom() {
		return linkGeom;
	}

	public JSONArray getInterLines() {
		return interLines;
	}

	public JSONArray getInterNodes() {
		return interNodes;
	}

	@Override
	public OperType getOperType() {
		
		return OperType.REPAIR;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}

	@Override
	public ObjType getObjType() {
		
		return ObjType.ZONELINK;
	}

	public Command(JSONObject json,String requester) throws JSONException{
		
		this.requester = requester;
		
		this.setDbId(json.getInt("dbId"));
		
		this.linkPid = json.getInt("objId");
		
		JSONObject data = json.getJSONObject("data");
		
		JSONObject geometry = data.getJSONObject("geometry");
		
		this.linkGeom = GeoTranslator.jts2Geojson(GeoTranslator.geojson2Jts(geometry, 1, 5));
		//修行后挂接对应ZONE_LINK信息
		this.interLines = data.getJSONArray("interLinks");
		//修行后挂接对应的ZONE_NODE信息
		this.interNodes = data.getJSONArray("interNodes");
		
		
	}

}
