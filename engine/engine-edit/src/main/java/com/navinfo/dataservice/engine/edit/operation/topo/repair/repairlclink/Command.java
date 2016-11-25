package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlclink;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

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

	private LcLink updateLink;

	private List<LcFace> faces;

	private List<RdGsc> gscList;

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public List<RdGsc> getGscList() {
		return gscList;
	}

	public void setGscList(List<RdGsc> gscList) {
		this.gscList = gscList;
	}

	public LcLink getUpdateLink() {
		return updateLink;
	}

	public void setUpdateLink(LcLink updateLink) {
		this.updateLink = updateLink;
	}

	public List<LcFace> getFaces() {
		return faces;
	}

	public void setFaces(List<LcFace> faces) {
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
		return requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.LCLINK;
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
