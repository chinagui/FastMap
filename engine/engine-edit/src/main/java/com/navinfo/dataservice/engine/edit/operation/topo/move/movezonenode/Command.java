package com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;
/**
 * @author zhaokk
 * Zone点参数基础类 
 */
public class Command extends AbstractCommand {
	
	private int nodePid;
	
	private double longitude;
	
	private double latitude;
	
	private String requester;
	
	private List<ZoneLink> links;
	private List<ZoneFace> faces;
	private ZoneNode zoneNode;
	
	private JSONObject json;

	public ZoneNode getZoneNode() {
		return zoneNode;
	}

	public void setZoneNode(ZoneNode zoneNode) {
		this.zoneNode = zoneNode;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public List<ZoneLink> getLinks() {
		return links;
	}

	public void setLinks(List<ZoneLink> links) {
		this.links = links;
	}

	public List<ZoneFace> getFaces() {
		return faces;
	}

	public void setFaces(List<ZoneFace> faces) {
		this.faces = faces;
	}
	
	public Command(JSONObject json,String requester) throws JSONException{
		
		this.json = json;
		
		this.nodePid = json.getInt("objId");
		
		JSONObject geoPoint = new JSONObject();

		geoPoint.put("type", "Point");

		geoPoint.put("coordinates", new double[] {json.getJSONObject("data").getDouble("longitude"),
				json.getJSONObject("data").getDouble("latitude") });
		
		Geometry geometry = GeoTranslator.geojson2Jts(geoPoint, 1, 5);
		
		this.longitude = geometry.getCoordinate().x;
		
		this.latitude = geometry.getCoordinate().y;
		
		this.setDbId(json.getInt("dbId"));
	}
	public Command(JSONObject json, ZoneLink zoneLink, ZoneNode node)
			throws JSONException {
		this(json, "");
		List<ZoneLink> links = new ArrayList<>();
		links.add(zoneLink);
		this.setLinks(links);
		this.zoneNode = node;

	}


	@Override
	public OperType getOperType() {
		
		return OperType.MOVE;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}

	@Override
	public ObjType getObjType() {
		
		return ObjType.ZONENODE;
	}

	public int getNodePid() {
		return nodePid;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}
	
	
}
