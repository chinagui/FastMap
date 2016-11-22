package com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * @author zhaokk 移动行政区划点参数基础类
 */
public class Command extends AbstractCommand {

	private int nodePid;

	private double longitude;

	private double latitude;

	private String requester;

	private List<AdLink> links;

	private JSONObject json;

	private List<AdFace> faces;
	private AdNode node;

	public AdNode getNode() {
		return node;
	}

	public void setNode(AdNode node) {
		this.node = node;
	}

	public List<AdLink> getLinks() {
		return links;
	}

	public void setLinks(List<AdLink> links) {
		this.links = links;
	}

	public List<AdFace> getFaces() {
		return faces;
	}

	public void setFaces(List<AdFace> faces) {
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

	public Command(JSONObject json, AdLink adLink, AdNode node)
			throws JSONException {
		this(json, "");
		List<AdLink> links = new ArrayList<>();
		links.add(adLink);
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

		return ObjType.ADNODE;
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
