package com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

public class Command extends AbstractCommand {

	private int nodePid;

	private double longitude;

	private double latitude;

	private String requester;

	private List<RwLink> links;

	private RwNode updateNode;

	private JSONObject json;

	public List<RwLink> getLinks() {
		return links;
	}

	public void setLinks(List<RwLink> links) {
		this.links = links;
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

	public RwNode getUpdateNode() {
		return updateNode;
	}

	public void setUpdateNode(RwNode updateNode) {
		this.updateNode = updateNode;
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

	public Command(JSONObject json, RwLink rwLink, RwNode node)
			throws JSONException {
		this(json, "");
		List<RwLink> links = new ArrayList<>();
		links.add(rwLink);
		this.setLinks(links);
		this.updateNode = node;

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

		return ObjType.RWNODE;
	}

	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}
}
