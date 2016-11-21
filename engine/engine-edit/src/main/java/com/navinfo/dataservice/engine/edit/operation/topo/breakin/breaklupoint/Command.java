package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class Command extends AbstractCommand {
	private String requester;
	private List<LuFace> faces;
	private JSONArray breakNodes;
	private List<LuLink> newLinks = new ArrayList<LuLink>();
	private LuLink breakLink = new LuLink();

	public List<LuLink> getNewLinks() {
		return newLinks;
	}

	public void setNewLinks(List<LuLink> newLinks) {
		this.newLinks = newLinks;
	}

	public LuLink getBreakLink() {
		return breakLink;
	}

	public void setBreakLink(LuLink breakLink) {
		this.breakLink = breakLink;
	}

	public JSONArray getBreakNodes() {
		return breakNodes;
	}

	public void setBreakNodes(JSONArray breakNodes) {
		this.breakNodes = breakNodes;
	}

	private LuNode breakNode;

	private String operationType = "";

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	private int breakNodePid = 0;

	public int getBreakNodePid() {
		return breakNodePid;
	}

	public void setBreakNodePid(int breakNodePid) {
		this.breakNodePid = breakNodePid;
	}

	public LuNode getBreakNode() {
		return breakNode;
	}

	public void setBreakNode(LuNode breakNode) {
		this.breakNode = breakNode;
	}

	private GeometryFactory geometryFactory = new GeometryFactory();
	private Point point;
	private int linkPid;
	private List<LuFaceTopo> luFaceTopos;

	public List<LuFace> getFaces() {
		return faces;
	}

	public void setFaces(List<LuFace> faces) {
		this.faces = faces;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public List<LuFaceTopo> getLuFaceTopos() {
		return luFaceTopos;
	}

	public void setLuFaceTopos(List<LuFaceTopo> luFaceTopos) {
		this.luFaceTopos = luFaceTopos;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.LUNODE;
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
}
