package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand {
	private String requester;
	private List<LcFace> faces;
	private int breakNodePid = 0;
	private LcNode breakNode;
	public LcNode getBreakNode() {
		return breakNode;
	}

	public void setBreakNode(LcNode breakNode) {
		this.breakNode = breakNode;
	}

	private LcLink breakLink = new LcLink();
	public List<LcLink> getNewLinks() {
		return newLinks;
	}

	public void setNewLinks(List<LcLink> newLinks) {
		this.newLinks = newLinks;
	}

	public LcLink getBreakLink() {
		return breakLink;
	}

	public void setBreakLink(LcLink breakLink) {
		this.breakLink = breakLink;
	}

	private List<LcLink> newLinks = new ArrayList<LcLink>();
	private JSONArray breakNodes;

	public JSONArray getBreakNodes() {
		return breakNodes;
	}

	public void setBreakNodes(JSONArray breakNodes) {
		this.breakNodes = breakNodes;
	}

	private List<RdGsc> rdGscs;

	public List<RdGsc> getRdGscs() {
		return rdGscs;
	}

	public void setRdGscs(List<RdGsc> rdGscs) {
		this.rdGscs = rdGscs;
	}

	public int getBreakNodePid() {
		return breakNodePid;
	}

	public void setBreakNodePid(int breakNodePid) {
		this.breakNodePid = breakNodePid;
	}

	public List<LcFace> getFaces() {
		return faces;
	}

	public void setFaces(List<LcFace> faces) {
		this.faces = faces;
	}
	private GeometryFactory geometryFactory = new GeometryFactory();
	private Point point;
	private int linkPid;

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.LCNODE;
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
		JSONObject geoPoint = new JSONObject();
		// 打断node有pid
		if (data.containsKey("breakNodePid")) {
			this.setBreakNodePid(data.getInt("breakNodePid"));
		}
		// 连续打断功能
		if (data.containsKey("breakNodes")) {
			this.breakNodes = JSONArray.fromObject(data
					.getJSONArray("breakNodes"));

		} else {
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
