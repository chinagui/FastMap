package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdlink.create;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.OperType;
import com.navinfo.dataservice.commons.geom.GeoTranslator;

public class Command implements ICommand {

	private String requester;

	private int projectId;
	
	private JSONObject geometry;
	
	private int eNodePid;
	
	private int sNodePid;
	
	private int kind=7;
	
	private int laneNum=2;
	
	private JSONArray catchLinks;
	
	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int getLaneNum() {
		return laneNum;
	}

	public void setLaneNum(int laneNum) {
		this.laneNum = laneNum;
	}

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
		return ObjType.RDLINK;
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
		
		if(data.containsKey("kind")){
			this.kind= data.getInt("kind");
		}
		
		if(data.containsKey("laneNum")){
			this.laneNum = data.getInt("laneNum");
		}
		
		if (data.containsKey("catchLinks")){
			
			this.catchLinks = new JSONArray();
			
			JSONArray array = data.getJSONArray("catchLinks");
			
			for(int i=0;i<array.size();i++){
				JSONObject jo = array.getJSONObject(i);
				
				double lon = Math.round(jo.getDouble("lon")*100000)/100000.0;
				
				double lat = Math.round(jo.getDouble("lat")*100000)/100000.0;
				
				jo.put("lon",lon);
				
				jo.put("lat", lat);
				
				this.catchLinks.add(jo);
			}
		}else{
			this.catchLinks = new JSONArray();
		}
	}

}
