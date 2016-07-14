package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breaklupoint;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class Command extends AbstractCommand {
	private String requester;
	private LuLink sLuLink;
	private LuLink eLuLink;
	private List<LuFace> faces;

	private int breakNodePid = 0;

	public int getBreakNodePid() {
		return breakNodePid;
	}

	public void setBreakNodePid(int breakNodePid) {
		this.breakNodePid = breakNodePid;
	}

	private GeometryFactory geometryFactory = new GeometryFactory();
	private Point point;
	private int linkPid;
	private List<LuFaceTopo> luFaceTopos;

	public LuLink getsLuLink() {
		return sLuLink;
	}

	public void setsLuLink(LuLink sLuLink) {
		this.sLuLink = sLuLink;
	}

	public LuLink geteLuLink() {
		return eLuLink;
	}

	public void seteLuLink(LuLink eLuLink) {
		this.eLuLink = eLuLink;
	}

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
		double lng = Math.round(data.getDouble("longitude") * 100000) / 100000.0;
		double lat = Math.round(data.getDouble("latitude") * 100000) / 100000.0;
		if (data.containsKey("breakNodePid")) {
			this.setBreakNodePid(data.getInt("breakNodePid"));
		}
		Coordinate coord = new Coordinate(lng, lat);
		this.sLuLink = new LuLink();
		this.eLuLink = new LuLink();
		this.point = geometryFactory.createPoint(coord);
	}
}
