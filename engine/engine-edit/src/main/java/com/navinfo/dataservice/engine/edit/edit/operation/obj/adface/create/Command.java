package com.navinfo.dataservice.engine.edit.edit.operation.obj.adface.create;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;

public class Command implements ICommand {

	private String requester;

	private int projectId;
	
	private JSONObject geometry;
	private List<Integer> linkPids;
	private List<AdLink> links;
	public List<AdLink> getLinks() {
		return links;
	}

	public void setLinks(List<AdLink> links) {
		this.links = links;
	}

	private String linkType;

	public String getLinkType() {
		return linkType;
	}

	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}

	public List<Integer> getLinkPids() {
		return linkPids;
	}

	public void setLinkPids(List<Integer> linkPids) {
		this.linkPids = linkPids;
	}

	public JSONObject getGeometry() {
		return geometry;
	}

	public void setGeometry(JSONObject geometry) {
		this.geometry = geometry;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.ADFACE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) throws Exception{
		this.requester = requester;

		this.projectId = json.getInt("projectId");
		JSONObject data = json.getJSONObject("data");
		if (data.containsKey("geometry")){
			this.geometry = data.getJSONObject("geometry");
	 
		}
		if (data.containsKey("linkPids")){
			this.linkType = json.getString("linkType");
			JSONArray array = data.getJSONArray("linkPids");
			linkPids = new ArrayList<Integer>();
			for( int i= 0 ;i < array.size();i++){
				int pid = array.getInt(i);
				if(!linkPids.contains(pid)){
					linkPids.add(pid);
				}
			}
		}


		
		
	}

}
