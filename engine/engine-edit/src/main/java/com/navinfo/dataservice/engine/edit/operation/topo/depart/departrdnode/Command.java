package com.navinfo.dataservice.engine.edit.operation.topo.depart.departrdnode;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class Command extends AbstractCommand {

	private int linkPid;

	private String requester;
	private int nodePid;
	private RdLink rdLink;
	private List<RdLink> links;
	private RdNode node;
	public RdNode getNode() {
		return node;
	}

	public void setNode(RdNode node) {
		this.node = node;
	}

	public List<RdLink> getLinks() {
		return links;
	}

	public void setLinks(List<RdLink> links) {
		this.links = links;
	}

	private int catchNodePid;
	private Point point;

	public RdLink getRdLink() {
		return rdLink;
	}

	public void setRdLink(RdLink rdLink) {
		this.rdLink = rdLink;
	}

	public int getCatchNodePid() {
		return catchNodePid;
	}

	public void setCatchNodePid(int catchNodePid) {
		this.catchNodePid = catchNodePid;
	}

	private Geometry geometry;

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public Command(JSONObject json, String requester) throws Exception {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		JSONObject data = json.getJSONObject("data");
		this.nodePid = data.getInt("objId");
		this.catchNodePid = data.getInt("catchNodePid");
		this.setLinkPid(data.getInt("linkPid"));
		GeoTranslator.point2Jts(
				data.getJSONObject("data").getDouble("longitude"), data
						.getJSONObject("data").getDouble("latitude"));
		this.geometry = GeoTranslator.geojson2Jts(
				data.getJSONObject("geometry"), 1, 5);

	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public int getLinkPid() {
		return linkPid;
	}

	@Override
	public OperType getOperType() {
		return OperType.DEPART;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDLINK;
	}
}
