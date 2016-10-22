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

public class Command extends AbstractCommand {

	private String requester;
	
	private int linkPid;
	
	private JSONObject linkGeom;
	
	private JSONArray interLines;
	
	private LcLink updateLink;

	private JSONArray interNodes;
	
	private List<LcFace> faces;
	
	private List<RdGsc> gscList;

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public JSONObject getLinkGeom() {
		return linkGeom;
	}

	public void setLinkGeom(JSONObject linkGeom) {
		this.linkGeom = linkGeom;
	}

	public JSONArray getInterLines() {
		return interLines;
	}

	public void setInterLines(JSONArray interLines) {
		this.interLines = interLines;
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

	public JSONArray getInterNodes() {
		return interNodes;
	}

	public void setInterNodes(JSONArray interNodes) {
		this.interNodes = interNodes;
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

	public Command(JSONObject json,String requester) throws JSONException{
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		this.linkPid = json.getInt("objId");
		JSONObject data = json.getJSONObject("data");
		JSONObject geometry = data.getJSONObject("geometry");
		this.linkGeom = GeoTranslator.jts2Geojson(GeoTranslator.geojson2Jts(geometry, 1, 5));
		//修行后挂接对应LC_LINK信息
		this.interLines = data.getJSONArray("interLinks");
		//修行后挂接对应的LC_NODE信息
		this.interNodes = data.getJSONArray("interNodes");
	}

}
