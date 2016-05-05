package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdlink.create;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

public class Command extends AbstractCommand{

	private String requester;
	
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

		this.setProjectId(json.getInt("projectId"));
		
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
		//构造检查对象
		this.createGlmList();
	}

	public void createGlmList() throws Exception {
		RdLink rdLinkObj=new RdLink();
		rdLinkObj.setsNodePid(this.sNodePid);
		rdLinkObj.seteNodePid(this.eNodePid);
		rdLinkObj.setGeometry(GeoTranslator.geojson2Jts(this.geometry));
		rdLinkObj.setLaneNum(this.laneNum);
		rdLinkObj.setKind(this.kind);
		List<IRow> glmList=new ArrayList<IRow>();
		glmList.add(rdLinkObj);
		this.setGlmList(glmList);
	}

}
