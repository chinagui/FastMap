package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink;

import java.util.List;

import org.json.JSONException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

public class Command extends AbstractCommand {

	private String requester;

	private int linkPid;

	private Geometry linkGeom;

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

	private LuLink updateLink;

	private JSONArray catchInfos;

	private List<LuFace> faces;

	private String operationType = "";

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public LuLink getUpdateLink() {
		return updateLink;
	}

	public void setUpdateLink(LuLink updateLink) {
		this.updateLink = updateLink;
	}

	public List<LuFace> getFaces() {
		return faces;
	}

	public void setFaces(List<LuFace> faces) {
		this.faces = faces;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	@Override
	public OperType getOperType() {
		return OperType.REPAIR;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.LULINK;
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
