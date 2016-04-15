package com.navinfo.dataservice.engine.edit.edit.operation.obj.adlink.create;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;

public class Command implements ICommand {

	private String requester;

	private int projectId;
	
	private JSONObject geometry;
	
	private int eNodePid;
	
	private int sNodePid;
	
	private JSONArray catchLinks;

	public int geteNodePid() {
		return eNodePid;
	}

	public void seteNodePid(int eNodePid) {
		this.eNodePid = eNodePid;
	}

	public int getsNodePid() {
		return sNodePid;
	}

	public void setsNodePid(int sNodePid) {
		this.sNodePid = sNodePid;
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
		return ObjType.ADLINK;
	}

	@Override
	public String getRequester() {
		return requester;
	}
	
	
	
	public JSONArray getCatchLinks() {
		return catchLinks;
	}

	public Command(JSONObject json, String requester) throws Exception{
		this.requester = requester;

		this.projectId = json.getInt("projectId");
		
		JSONObject data = json.getJSONObject("data");

		this.eNodePid = data.getInt("eNodePid");
		
		this.sNodePid = data.getInt("sNodePid");
		
		this.geometry = data.getJSONObject("geometry");
		
		this.geometry = GeoTranslator.jts2Geojson(GeoTranslator.geojson2Jts(geometry, 1, 5));
	
		if (data.containsKey("catchLinks")){
			
			this.catchLinks = new JSONArray();
			
			JSONArray array = data.getJSONArray("catchLinks");
			
			for(int i=0;i<array.size();i++){
				JSONObject jo = array.getJSONObject(i);
				
				double lon = Math.round(jo.getDouble("lon")*100000)/100000.0;
				
				double lat = Math.round(jo.getDouble("lat")*100000)/100000.0;
				double linkPid = jo.getInt("linkPid");
				jo.put("lon",lon);
				
				jo.put("lat", lat);
				
				jo.put("linkPid", linkPid);
				
				this.catchLinks.add(jo);
			}
		}else{
			this.catchLinks = new JSONArray();
		}
	}

}
