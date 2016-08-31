package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * @author zhaokk 创建行政区划点基础参数类
 */
public class Command extends AbstractCommand {
	private String requester;
	private AdLink sAdLink;
	private AdLink eAdLink;
	private List<AdFace> faces;
	private int breakNodePid = 0;

	private String operationType = "";

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	private AdNode breakNode;

	public int getBreakNodePid() {
		return breakNodePid;
	}

	public void setBreakNodePid(int breakNodePid) {
		this.breakNodePid = breakNodePid;
	}

	public List<AdFace> getFaces() {
		return faces;
	}

	public void setFaces(List<AdFace> faces) {
		this.faces = faces;
	}

	public AdLink getsAdLink() {
		return sAdLink;
	}

	public void setsAdLink(AdLink sAdLink) {
		this.sAdLink = sAdLink;
	}

	public AdLink geteAdLink() {
		return eAdLink;
	}

	public void seteAdLink(AdLink eAdLink) {
		this.eAdLink = eAdLink;
	}

	public AdNode getBreakNode() {
		return breakNode;
	}

	public void setBreakNode(AdNode breakNode) {
		this.breakNode = breakNode;
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
		return ObjType.ADNODE;
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

		geoPoint.put("type", "Point");

		geoPoint.put("coordinates", new double[] { data.getDouble("longitude"),
				data.getDouble("latitude") });

		Geometry geometry = GeoTranslator.geojson2Jts(geoPoint, 1, 5);

		if (data.containsKey("breakNodePid")) {
			this.setBreakNodePid(data.getInt("breakNodePid"));
		}
		Coordinate coord = new Coordinate(geometry.getCoordinate().x,
				geometry.getCoordinate().y);
		this.eAdLink = new AdLink();
		this.sAdLink = new AdLink();
		this.point = geometryFactory.createPoint(coord);
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
