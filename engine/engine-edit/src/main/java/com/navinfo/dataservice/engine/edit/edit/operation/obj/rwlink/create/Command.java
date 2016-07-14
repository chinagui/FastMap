package com.navinfo.dataservice.engine.edit.edit.operation.obj.rwlink.create;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

public class Command extends AbstractCommand{
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	private String requester;
	
	private Geometry geometry;
	
	private int eNodePid;
	
	private int sNodePid;
	
	private int kind=1;
	
	private int form;
	
	private JSONArray catchLinks;
	
	public int getKind() {
		return kind;
	}
	
	public int getForm() {
		return form;
	}

	public void setForm(int form) {
		this.form = form;
	}


	public void setKind(int kind) {
		this.kind = kind;
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

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.RWLINK;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
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
		
		try {
			this.geometry = GeoTranslator.geojson2Jts(data.getJSONObject("geometry"), 1, 5);
		} catch (Exception e) {
			String msg = e.getLocalizedMessage();
			
			log.error(e.getMessage(),e);
			
			if(msg.contains("found 1 - must be 0 or >= 2"))
			{
				throw new Exception("线至少包含两个点");
			}
			else
			{
				throw new Exception(msg);
			}
		}
		
		if(data.containsKey("kind")){
			this.kind= data.getInt("kind");
		}
		
		if(data.containsKey("form")){
			this.form = data.getInt("form");
		}
		
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
	
	public static void main(String[] args) throws JSONException {
		double lon1 = 116.45236587696671;
		double lat1 = 40.05121688373498;
		double lon = Math.round(lon1*100000)/100000.0;
		
		double lat = Math.round(lat1*100000)/100000.0;
		
		System.out.println(lon+":"+lat);
		
		JSONObject data = new JSONObject();
		
		data.put("geometry", "{\"type\":\"LineString\",\"coordinates\":[[116.45119428634644,40.051738918700195],[116.45236587696671,40.05121288373498],[116.45455241203307,40.05076162010255]]}");
		
		Geometry geometry = GeoTranslator.geojson2Jts(data.getJSONObject("geometry"), 1, 5);
		
		System.out.println(geometry);
	}
}
