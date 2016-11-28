package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author zhaokk 修行行政区划线参数基础类
 */
public class Command extends AbstractCommand {

	private String requester;

	private int linkPid;

	private Geometry linkGeom;

	private AdLink updateLink;
	private JSONArray catchInfos;

	public JSONArray getCatchInfos() {
		return catchInfos;
	}

	public void setCatchInfos(JSONArray catchInfos) {
		this.catchInfos = catchInfos;
	}

	private String operationType = "";

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public AdLink getUpdateLink() {
		return updateLink;
	}

	public void setUpdateLink(AdLink updateLink) {
		this.updateLink = updateLink;
	}

	public List<AdFace> getFaces() {
		return faces;
	}

	public void setFaces(List<AdFace> faces) {
		this.faces = faces;
	}

	private List<AdFace> faces;

	public int getLinkPid() {
		return linkPid;
	}

	public Geometry getLinkGeom() {
		return linkGeom;
	}

	public void setLinkGeom(Geometry linkGeom) {
		this.linkGeom = linkGeom;
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

		return ObjType.ADLINK;
	}

	public Command(JSONObject json, String requester) throws JSONException {

		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.linkPid = json.getInt("objId");

		JSONObject data = json.getJSONObject("data");

		JSONObject geometry = data.getJSONObject("geometry");

		this.linkGeom = (GeoTranslator.geojson2Jts(geometry, 1, 5));
		// 修行挂接信息
		if (data.containsKey("catchInfos")) {
			this.catchInfos = data.getJSONArray("catchInfos");
		}

	}

}
