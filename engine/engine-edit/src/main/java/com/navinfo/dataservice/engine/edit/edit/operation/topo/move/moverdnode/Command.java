package com.navinfo.dataservice.engine.edit.edit.operation.topo.move.moverdnode;

import java.util.List;

import org.json.JSONException;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

public class Command extends AbstractCommand {
	
	private int nodePid;
	
	private double longitude;
	
	private double latitude;
	
	private String requester;
	
	private List<RdLink> links;
	
	public List<RdLink> getLinks() {
		return links;
	}

	public void setLinks(List<RdLink> links) {
		this.links = links;
	}
	
	public Command(JSONObject json,String requester) throws JSONException{
		
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
		
		return ObjType.RDNODE;
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


	
}
