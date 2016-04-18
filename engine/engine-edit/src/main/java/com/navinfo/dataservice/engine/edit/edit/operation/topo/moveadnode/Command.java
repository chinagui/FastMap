package com.navinfo.dataservice.engine.edit.edit.operation.topo.moveadnode;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
/**
 * @author zhaokk
 * 移动行政区划点参数基础类 
 */
public class Command implements ICommand {
	
	private int nodePid;
	
	private double longitude;
	
	private double latitude;
	
	private String requester;
	
	private int projectId;
	
	private List<AdLink> links;
	private List<AdFace> faces;

	public List<AdLink> getLinks() {
		return links;
	}

	public void setLinks(List<AdLink> links) {
		this.links = links;
	}

	public List<AdFace> getFaces() {
		return faces;
	}

	public void setFaces(List<AdFace> faces) {
		this.faces = faces;
	}

	public int getProjectId() {
		return projectId;
	}
	
	public Command(JSONObject json,String requester){
		
		this.nodePid = json.getInt("objId");
		
		this.longitude = Math.round(json.getJSONObject("data").getDouble("longitude")*100000)/100000.0;
		
		this.latitude = Math.round(json.getJSONObject("data").getDouble("latitude")*100000)/100000.0;
		
		this.projectId = json.getInt("projectId");
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
		
		return ObjType.ADNODE;
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
