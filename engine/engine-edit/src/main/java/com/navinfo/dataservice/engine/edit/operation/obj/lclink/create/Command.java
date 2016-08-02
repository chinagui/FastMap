package com.navinfo.dataservice.engine.edit.operation.obj.lclink.create;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

public class Command extends AbstractCommand  {

	private String requester;

	
	private Geometry geometry;
	
	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

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


	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.LCLINK;
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
		this.setDbId(json.getInt("dbId"));
		JSONObject data = json.getJSONObject("data");

		this.eNodePid = data.getInt("eNodePid");
		
		this.sNodePid = data.getInt("sNodePid");
		this.geometry = GeoTranslator.geojson2Jts(data.getJSONObject("geometry"),1,5);
	    //获取行政区划线挂接的LCLINK 和LCNODE
		if (data.containsKey("catchLinks")){
			
			this.catchLinks = new JSONArray();
			
			JSONArray array = data.getJSONArray("catchLinks");
			
			for(int i=0;i<array.size();i++){
				JSONObject jo = array.getJSONObject(i);

				JSONObject geoPoint = new JSONObject();

				geoPoint.put("type", "Point");

				geoPoint.put("coordinates", new double[] {jo.getDouble("lon"),
						jo.getDouble("lat") });
				
				Geometry geometry = GeoTranslator.geojson2Jts(geoPoint, 1, 5);
				
				jo.put("lon",geometry.getCoordinate().x);
				
				jo.put("lat", geometry.getCoordinate().y);
				
				this.catchLinks.add(jo);
			}
		}else{
			this.catchLinks = new JSONArray();
		}
	}

}
