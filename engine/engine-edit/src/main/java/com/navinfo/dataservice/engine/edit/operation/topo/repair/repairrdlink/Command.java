package com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrdlink;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;

	private int linkPid;

	private JSONObject linkGeom;

	private JSONArray interLines;

	private JSONArray interNodes;

	private RdLink updateLink;

	private String operationType = "";

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	private List<RdGsc> gscList;

	public List<RdGsc> getGscList() {
		return gscList;
	}

	public void setGscList(List<RdGsc> gscList) {
		this.gscList = gscList;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public JSONObject getLinkGeom() {
		return linkGeom;
	}

	public JSONArray getInterLines() {
		return interLines;
	}

	public JSONArray getInterNodes() {
		return interNodes;
	}

	public RdLink getUpdateLink() {
		return updateLink;
	}

	public void setUpdateLink(RdLink updateLink) {
		this.updateLink = updateLink;
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

		return ObjType.RDLINK;
	}

	public Command(JSONObject json, String requester) throws JSONException {

		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.linkPid = json.getInt("objId");

		JSONObject data = json.getJSONObject("data");

		JSONObject geometry = data.getJSONObject("geometry");

		this.linkGeom = GeoTranslator.jts2Geojson(GeoTranslator.geojson2Jts(
				geometry, 1, 5));

		this.interLines = data.getJSONArray("interLinks");

		this.interNodes = data.getJSONArray("interNodes");
	}

}
