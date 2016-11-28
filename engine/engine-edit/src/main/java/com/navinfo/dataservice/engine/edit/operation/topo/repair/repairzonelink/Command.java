package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author zhaokk 修行ZONE线参数基础类
 */
public class Command extends AbstractCommand {

	private String requester;

	private int linkPid;

	private Geometry linkGeom;
	private JSONArray catchInfos;

	public Geometry getLinkGeom() {
		return linkGeom;
	}

	public void setLinkGeom(Geometry linkGeom) {
		this.linkGeom = linkGeom;
	}

	public JSONArray getCatchInfos() {
		return catchInfos;
	}

	public void setCatchInfos(JSONArray catchInfos) {
		this.catchInfos = catchInfos;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	private ZoneLink updateLink;

	private String operationType = "";

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public ZoneLink getUpdateLink() {
		return updateLink;
	}

	public void setUpdateLink(ZoneLink updateLink) {
		this.updateLink = updateLink;
	}

	public List<ZoneFace> getFaces() {
		return faces;
	}

	public void setFaces(List<ZoneFace> faces) {
		this.faces = faces;
	}

	private List<ZoneFace> faces;

	public int getLinkPid() {
		return linkPid;
	}

	@Override
	public OperType getOperType() {

		return OperType.REPAIR;
	}

	@Override
	public String getRequester() {

		return requester;
	}

	@Override
	public ObjType getObjType() {

		return ObjType.ZONELINK;
	}

	public Command(JSONObject json, String requester) throws JSONException {

		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.linkPid = json.getInt("objId");

		JSONObject data = json.getJSONObject("data");

		JSONObject geometry = data.getJSONObject("geometry");

		this.linkGeom = GeoTranslator.geojson2Jts(geometry, 1, 5);
		// 修行挂接信息
		if (data.containsKey("catchInfos")) {
			this.catchInfos = data.getJSONArray("catchInfos");
		}
	}

}
