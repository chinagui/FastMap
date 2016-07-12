package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breakzonepoint;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * @author  zhaokk 
 * 创建ZONE点基础参数类 
 */
public class Command extends AbstractCommand {
	private String requester;
	private ZoneLink  sZoneLink;
	private ZoneLink  eZoneLink;
	private List<ZoneFace> faces;
	private Point point;
	private int linkPid;
	private int breakNodePid = 0;


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

	public Command(JSONObject json, String requester) throws Exception{
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		this.linkPid = json.getInt("objId");
		JSONObject data = json.getJSONObject("data");
		double lng = Math.round(data.getDouble("longitude")*100000)/100000.0;
		double lat = Math.round(data.getDouble("latitude")*100000)/100000.0;
		if(data.containsKey("breakNodePid")){
			this.setBreakNodePid(data.getInt("breakNodePid"));
		}
		Coordinate coord = new Coordinate(lng, lat);
		this.eZoneLink = new ZoneLink();
		this.sZoneLink = new ZoneLink();
		this.point = geometryFactory.createPoint(coord);
	}

	public ZoneLink getsZoneLink() {
		return sZoneLink;
	}

	public void setsZoneLink(ZoneLink sZoneLink) {
		this.sZoneLink = sZoneLink;
	}

	public ZoneLink geteZoneLink() {
		return eZoneLink;
	}

	public void seteZoneLink(ZoneLink eZoneLink) {
		this.eZoneLink = eZoneLink;
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
