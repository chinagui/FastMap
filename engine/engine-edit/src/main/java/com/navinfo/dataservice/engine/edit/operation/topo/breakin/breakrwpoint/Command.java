package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 铁路点基础参数类
 * 
 * @author zhangxiaolong
 * 
 */
public class Command extends AbstractCommand {
	private String requester;
	private int breakNodePid = 0;
	private JSONArray breakNodes;
	private RwNode breakNode;
	public RwNode getBreakNode() {
		return breakNode;
	}

	public void setBreakNode(RwNode breakNode) {
		this.breakNode = breakNode;
	}

	private List<RwLink> newLinks = new ArrayList<RwLink>();
	private RwLink breakLink = new RwLink();

	public JSONArray getBreakNodes() {
		return breakNodes;
	}

	public void setBreakNodes(JSONArray breakNodes) {
		this.breakNodes = breakNodes;
	}

	public List<RwLink> getNewLinks() {
		return newLinks;
	}

	public void setNewLinks(List<RwLink> newLinks) {
		this.newLinks = newLinks;
	}

	public RwLink getBreakLink() {
		return breakLink;
	}

	public void setBreakLink(RwLink breakLink) {
		this.breakLink = breakLink;
	}

	private List<RdGsc> rdGscs;

	public int getBreakNodePid() {
		return breakNodePid;
	}

	public void setBreakNodePid(int breakNodePid) {
		this.breakNodePid = breakNodePid;
	}

	public List<AdFaceTopo> getAdFaceTopos() {
		return adFaceTopos;
	}

	public void setAdFaceTopos(List<AdFaceTopo> adFaceTopos) {
		this.adFaceTopos = adFaceTopos;
	}

	private GeometryFactory geometryFactory = new GeometryFactory();
	private Point point;
	private int linkPid;
	private List<AdFaceTopo> adFaceTopos;

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RWNODE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public List<RdGsc> getRdGscs() {
		return rdGscs;
	}

	public void setRdGscs(List<RdGsc> rdGscs) {
		this.rdGscs = rdGscs;
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
