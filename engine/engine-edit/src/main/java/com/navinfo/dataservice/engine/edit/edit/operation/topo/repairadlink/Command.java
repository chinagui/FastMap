package com.navinfo.dataservice.engine.edit.edit.operation.topo.repairadlink;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
/**
 * @author zhaokk
 * 修行行政区划线参数基础类 
 */
public class Command extends AbstractCommand {

	private String requester;
	
	private int linkPid;
	
	private JSONObject linkGeom;
	
	private JSONArray interLines;
	
	private AdLink updateLink;
	
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

	private JSONArray interNodes;
	
	private List<AdFace> faces;

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

	public Command(JSONObject json,String requester) throws JSONException{
		
		this.requester = requester;
		
		this.setDbId(json.getInt("subTaskId"));
		
		this.linkPid = json.getInt("objId");
		
		JSONObject data = json.getJSONObject("data");
		
		JSONObject geometry = data.getJSONObject("geometry");
		
		this.linkGeom = GeoTranslator.jts2Geojson(GeoTranslator.geojson2Jts(geometry, 1, 5));
		//修行后挂接对应AD_LINK信息
		this.interLines = data.getJSONArray("interLinks");
		//修行后挂接对应的AD_NODE信息
		this.interNodes = data.getJSONArray("interNodes");
		
		
	}

}
