package com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	private int nodePid;

	private double longitude;

	private double latitude;

	private String requester;

	private List<RdLink> links;
	
	private JSONObject json;
	
	private RdNode node;

	public RdNode getNode() {
		return node;
	}

	public void setNode(RdNode node) {
		this.node = node;
	}

	public List<RdLink> getLinks() {
		return links;
	}

	public void setLinks(List<RdLink> links) {
		this.links = links;
	}

	public Command(JSONObject json, String requester) throws JSONException {

		this.nodePid = json.getInt("objId");

		this.json = json;

		JSONObject geoPoint = new JSONObject();

		geoPoint.put("type", "Point");

		geoPoint.put("coordinates", new double[] {
				json.getJSONObject("data").getDouble("longitude"),
				json.getJSONObject("data").getDouble("latitude") });

		Geometry geometry = GeoTranslator.geojson2Jts(geoPoint, 1, 5);

		this.longitude = geometry.getCoordinate().x;

		this.latitude = geometry.getCoordinate().y;

		this.setDbId(json.getInt("dbId"));
	}

	public Command(JSONObject json, RdLink rdLink,RdNode node) throws JSONException {
		this(json, "");
        List<RdLink> links = new ArrayList<>(); 
        links.add(rdLink);
		this.setLinks(links);
		this.node = node;

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

	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}

}
