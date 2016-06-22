package com.navinfo.dataservice.engine.edit.edit.operation.topo.move.movezonenode;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
/**
 * @author zhaokk
 * Zone点参数基础类 
 */
public class Command extends AbstractCommand {
	
	private int nodePid;
	
	private double longitude;
	
	private double latitude;
	
	private String requester;
	
	private List<ZoneLink> links;
	private List<ZoneFace> faces;
	private ZoneNode zoneNode;

	public ZoneNode getZoneNode() {
		return zoneNode;
	}

	public void setZoneNode(ZoneNode zoneNode) {
		this.zoneNode = zoneNode;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public List<ZoneLink> getLinks() {
		return links;
	}

	public void setLinks(List<ZoneLink> links) {
		this.links = links;
	}

	public List<ZoneFace> getFaces() {
		return faces;
	}

	public void setFaces(List<ZoneFace> faces) {
		this.faces = faces;
	}
	
	public Command(JSONObject json,String requester){
		
		this.nodePid = json.getInt("objId");
		
		this.longitude = Math.round(json.getJSONObject("data").getDouble("longitude")*100000)/100000.0;
		
		this.latitude = Math.round(json.getJSONObject("data").getDouble("latitude")*100000)/100000.0;
		
		this.setDbId(json.getInt("dbId"));
	}

	@Override
	public OperType getOperType() {
		
		return OperType.MOVE;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}

	@Override
	public ObjType getObjType() {
		
		return ObjType.ZONENODE;
	}

	public int getNodePid() {
		return nodePid;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}


	
}
