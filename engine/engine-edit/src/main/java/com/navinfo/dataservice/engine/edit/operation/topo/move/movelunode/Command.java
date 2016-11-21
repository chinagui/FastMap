package com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

public class Command extends AbstractCommand {

	private int nodePid;

	private JSONObject json;

	private double longitude;

	public LuNode getNode() {
		return node;
	}

	public void setNode(LuNode node) {
		this.node = node;
	}

	private double latitude;

	private String requester;
	private LuNode node;

	private List<LuLink> links;
	private List<LuFace> faces;

	public List<LuLink> getLinks() {
		return links;
	}

	public void setLinks(List<LuLink> links) {
		this.links = links;
	}

	public List<LuFace> getFaces() {
		return faces;
	}

	public void setFaces(List<LuFace> faces) {
		this.faces = faces;
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

	public Command(JSONObject json, LuLink luLink, LuNode node)
			throws JSONException {
		this(json, "");
		List<LuLink> links = new ArrayList<>();
		links.add(luLink);
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

		return ObjType.LUNODE;
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
