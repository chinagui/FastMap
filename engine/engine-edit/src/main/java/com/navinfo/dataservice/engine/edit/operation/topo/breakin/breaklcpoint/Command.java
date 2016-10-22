package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand {
	private String requester;
	private LcLink  sLcLink;
	private LcLink  eLcLink;
	private List<LcFace> faces;
	private int breakNodePid = 0;
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

	public LcLink getsLcLink() {
		return sLcLink;
	}

	public void setsLcLink(LcLink sLcLink) {
		this.sLcLink = sLcLink;
	}

	public LcLink geteLcLink() {
		return eLcLink;
	}

	public void seteLcLink(LcLink eLcLink) {
		this.eLcLink = eLcLink;
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

	public Command(JSONObject json, String requester) throws Exception{
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		this.linkPid = json.getInt("objId");
		JSONObject data = json.getJSONObject("data");
		
		JSONObject geoPoint = new JSONObject();

		geoPoint.put("type", "Point");

		geoPoint.put("coordinates", new double[] {data.getDouble("longitude"),
				data.getDouble("latitude") });
		
		Geometry geometry = GeoTranslator.geojson2Jts(geoPoint, 1, 5);
		
		if(data.containsKey("breakNodePid")){
			this.setBreakNodePid(data.getInt("breakNodePid"));
		}
		Coordinate coord = new Coordinate(geometry.getCoordinate().x, geometry.getCoordinate().y);
		this.eLcLink = new LcLink();
		this.sLcLink = new LcLink();
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
