package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * @author zhaokk 创建ZONE点基础参数类
 */
public class Command extends AbstractCommand {
	private String requester;
	private List<ZoneFace> faces;
	private JSONArray breakNodes;
	private List<ZoneLink> newLinks = new ArrayList<ZoneLink>();
	private ZoneLink breakLink = new ZoneLink();

	public JSONArray getBreakNodes() {
		return breakNodes;
	}

	public void setBreakNodes(JSONArray breakNodes) {
		this.breakNodes = breakNodes;
	}

	public List<ZoneLink> getNewLinks() {
		return newLinks;
	}

	public void setNewLinks(List<ZoneLink> newLinks) {
		this.newLinks = newLinks;
	}

	public ZoneLink getBreakLink() {
		return breakLink;
	}

	public void setBreakLink(ZoneLink breakLink) {
		this.breakLink = breakLink;
	}

	private Point point;
	private int linkPid;
	private int breakNodePid = 0;

	private String operationType = "";

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	private ZoneNode breakNode;

	public int getBreakNodePid() {
		return breakNodePid;
	}

	public void setBreakNodePid(int breakNodePid) {
		this.breakNodePid = breakNodePid;
	}

	public List<ZoneFace> getFaces() {
		return faces;
	}

	public void setFaces(List<ZoneFace> faces) {
		this.faces = faces;
	}

	public ZoneNode getBreakNode() {
		return breakNode;
	}

	public void setBreakNode(ZoneNode breakNode) {
		this.breakNode = breakNode;
	}

	private GeometryFactory geometryFactory = new GeometryFactory();

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.ZONENODE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) throws Exception {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		this.linkPid = json.getInt("objId");
		JSONObject data = json.getJSONObject("data");

		if (data.containsKey("breakNodePid")) {
			this.setBreakNodePid(data.getInt("breakNodePid"));
		}
		if (data.containsKey("breakNodes")) {
			this.breakNodes = JSONArray.fromObject(data
					.getJSONArray("breakNodes"));

		} else {
			JSONObject geoPoint = new JSONObject();

			geoPoint.put("type", "Point");

			geoPoint.put(
					"coordinates",
					new double[] { data.getDouble("longitude"),
							data.getDouble("latitude") });

			Geometry geometry = GeoTranslator.geojson2Jts(geoPoint, 1, 5);
			Coordinate coord = new Coordinate(geometry.getCoordinate().x,
					geometry.getCoordinate().y);
			this.point = geometryFactory.createPoint(coord);
		}

	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

}
