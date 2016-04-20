package com.navinfo.dataservice.engine.edit.edit.operation.topo.repairadlink;

import org.json.JSONException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
/**
 * @author zhaokk
 * 修行行政区划线参数基础类 
 */
public class Command implements ICommand {

	private String requester;
	
	private int linkPid;
	
	private JSONObject linkGeom;
	
	private JSONArray interLines;
	
	private JSONArray interNodes;
	
	private int projectId;

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
	
	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public Command(JSONObject json,String requester) throws JSONException{
		
		this.requester = requester;
		
		this.projectId = json.getInt("projectId");
		
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
