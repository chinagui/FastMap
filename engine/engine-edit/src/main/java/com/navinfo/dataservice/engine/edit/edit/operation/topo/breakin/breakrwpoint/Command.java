package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breakrwpoint;

import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONObject;

/**
 * 铁路点基础参数类
 * 
 * @author zhangxiaolong
 *
 */
public class Command extends AbstractCommand {
	private String requester;
	private RwLink sRwLink;
	private RwLink eRwLink;
	private int breakNodePid = 0;
	private List<RdGsc> rdGscs;

	public int getBreakNodePid() {
		return breakNodePid;
	}

	public void setBreakNodePid(int breakNodePid) {
		this.breakNodePid = breakNodePid;
	}

	public RwLink getsRwLink() {
		return sRwLink;
	}

	public void setsRwLink(RwLink sRwLink) {
		this.sRwLink = sRwLink;
	}

	public RwLink geteRwLink() {
		return eRwLink;
	}

	public void seteRwLink(RwLink eRwLink) {
		this.eRwLink = eRwLink;
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
		JSONObject geoPoint = new JSONObject();

		geoPoint.put("type", "Point");

		geoPoint.put("coordinates", new double[] {data.getDouble("lon"),
				data.getDouble("lat") });
		
		Geometry geometry = GeoTranslator.geojson2Jts(geoPoint, 1, 5);
		
		if(data.containsKey("breakNodePid")){
			this.setBreakNodePid(data.getInt("breakNodePid"));
		}
		Coordinate coord = new Coordinate(geometry.getCoordinate().x, geometry.getCoordinate().y);
		this.eRwLink = new RwLink();
		this.sRwLink = new RwLink();
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
