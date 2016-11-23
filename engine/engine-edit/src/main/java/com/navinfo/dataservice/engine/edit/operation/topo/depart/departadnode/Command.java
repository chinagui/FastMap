package com.navinfo.dataservice.engine.edit.operation.topo.depart.departadnode;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;

import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Point;

/***
 * 
 * @author zhaokk 节点分离参数设置类
 * 
 */
public class Command extends AbstractCommand {

	private int linkPid;

	private String requester;
	private int nodePid;// 分离点的nodePid
	private AdLink adLink;// 分离线的pid
	private List<AdLink> links;// 分离点挂接的link
	private AdNode node;
	private int catchLinkPid;// 分离挂接的link

	public int getCatchLinkPid() {
		return catchLinkPid;
	}

	public void setCatchLinkPid(int catchLinkPid) {
		this.catchLinkPid = catchLinkPid;
	}

	public AdNode getNode() {
		return node;
	}

	public void setNode(AdNode node) {
		this.node = node;
	}

	public List<AdLink> getLinks() {
		return links;
	}

	public void setLinks(List<AdLink> links) {
		this.links = links;
	}

	private int catchNodePid;// 分离挂接的node
	private Point point;

	public AdLink getAdLink() {
		return adLink;
	}

	public void setAdLink(AdLink adLink) {
		this.adLink = adLink;
	}

	public int getCatchNodePid() {
		return catchNodePid;
	}

	public void setCatchNodePid(int catchNodePid) {
		this.catchNodePid = catchNodePid;
	}

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
		this.nodePid = json.getInt("objId");
		JSONObject data = json.getJSONObject("data");
		this.catchNodePid = data.getInt("catchNodePid");// 挂接的nodePid
		this.catchLinkPid = data.getInt("catchLinkPid");// 挂接的linkPid
		this.setLinkPid(data.getInt("linkPid"));
		if (data.containsKey("longitude") && data.containsKey("longitude")) {
			this.setPoint((Point) GeoTranslator.transform(
					GeoTranslator.point2Jts(data.getDouble("longitude"),
							data.getDouble("latitude")), 1, 5));
		}
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
		return ObjType.ADLINK;
	}
}
