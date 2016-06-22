package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair.repairrwlink;

import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 修行铁路线基础类 
 * @author zhangxiaolong
 *
 */
public class Command extends AbstractCommand {

	private String requester;
	
	private int linkPid;
	
	private JSONObject linkGeom;
	
	private JSONArray interLines;
	
	private RwLink updateLink;
	
	public RwLink getUpdateLink() {
		return updateLink;
	}

	public void setUpdateLink(RwLink updateLink) {
		this.updateLink = updateLink;
	}

	private JSONArray interNodes;

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
		
		return ObjType.RWLINK;
	}

	public Command(JSONObject json,String requester) throws JSONException{
		
		this.requester = requester;
		
		this.setDbId(json.getInt("dbId"));
		
		this.linkPid = json.getInt("objId");
		
		JSONObject data = json.getJSONObject("data");
		
		JSONObject geometry = data.getJSONObject("geometry");
		
		this.linkGeom = GeoTranslator.jts2Geojson(GeoTranslator.geojson2Jts(geometry, 1, 5));
		//修行后挂接对应RW_LINK信息
		this.interLines = data.getJSONArray("interLinks");
		//修行后挂接对应的RW_NODE信息
		this.interNodes = data.getJSONArray("interNodes");
	}

}
