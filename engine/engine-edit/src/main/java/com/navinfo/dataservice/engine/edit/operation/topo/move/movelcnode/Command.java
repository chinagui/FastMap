package com.navinfo.dataservice.engine.edit.operation.topo.move.movelcnode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	private int nodePid;

	private double longitude;

	private double latitude;
	
	private LcNode node;

	public LcNode getNode() {
		return node;
	}

	public void setNode(LcNode node) {
		this.node = node;
	}

	private String requester;

	private List<LcLink> links;
	private List<LcFace> faces;

	public List<LcLink> getLinks() {
		return links;
	}

	public void setLinks(List<LcLink> links) {
		this.links = links;
	}

	public List<LcFace> getFaces() {
		return faces;
	}

	public void setFaces(List<LcFace> faces) {
		this.faces = faces;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	@Override
	public String toString() {
		return "Command [nodePid=" + nodePid + ", longitude=" + longitude + ", latitude=" + latitude + ", requester="
				+ requester + ", links=" + links + ", faces=" + faces + "]";
	}

	public Command(JSONObject json, String requester) throws JSONException {
		this.nodePid = json.getInt("objId");
		JSONObject geoPoint = new JSONObject();
		geoPoint.put("type", "Point");
		geoPoint.put("coordinates", new double[] { json.getJSONObject("data").getDouble("longitude"),
				json.getJSONObject("data").getDouble("latitude") });
		Geometry geometry = GeoTranslator.geojson2Jts(geoPoint, 1, 5);
		this.longitude = geometry.getCoordinate().x;
		this.latitude = geometry.getCoordinate().y;
		this.setDbId(json.getInt("dbId"));
	}
	public Command(JSONObject json, LcLink lcLink, LcNode node)
			throws JSONException {
		this(json, "");
		List<LcLink> links = new ArrayList<>();
		links.add(lcLink);
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
		return ObjType.LCNODE;
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
