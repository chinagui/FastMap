package com.navinfo.dataservice.dao.glm.model.rd.speedlimit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.vividsolutions.jts.geom.Geometry;

public class RdSpeedlimit implements IObj {

	private int pid;

	private int linkPid;

	private int direct;

	private int speedValue;

	private int speedType;

	private int speedDependent;

	private int speedFlag;

	private int limitSrc = 1;

	private String timeDomain;

	private int captureFlag;

	private String descript;

	private int meshId;

	private int status = 7;

	private int ckStatus = 6;

	private int adjaFlag;

	private int recStatusIn;

	private int recStatusOut;

	private String timeDescript;

	private Geometry geometry;

	private String laneSpeedValue;

	private String rowId;
	
	private int tollgateFlag;
	
	public int getTollgateFlag() {
		return tollgateFlag;
	}

	public void setTollgateFlag(int tollgateFlag) {
		this.tollgateFlag = tollgateFlag;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public int getDirect() {
		return direct;
	}

	public void setDirect(int direct) {
		this.direct = direct;
	}

	public int getSpeedValue() {
		return speedValue;
	}

	public void setSpeedValue(int speedValue) {
		this.speedValue = speedValue;
	}

	public int getSpeedType() {
		return speedType;
	}

	public void setSpeedType(int speedType) {
		this.speedType = speedType;
	}

	public int getSpeedDependent() {
		return speedDependent;
	}

	public void setSpeedDependent(int speedDependent) {
		this.speedDependent = speedDependent;
	}

	public int getSpeedFlag() {
		return speedFlag;
	}

	public void setSpeedFlag(int speedFlag) {
		this.speedFlag = speedFlag;
	}

	public int getLimitSrc() {
		return limitSrc;
	}

	public void setLimitSrc(int limitSrc) {
		this.limitSrc = limitSrc;
	}

	public String getTimeDomain() {
		return timeDomain;
	}

	public void setTimeDomain(String timeDomain) {
		this.timeDomain = timeDomain;
	}

	public int getCaptureFlag() {
		return captureFlag;
	}

	public void setCaptureFlag(int captureFlag) {
		this.captureFlag = captureFlag;
	}

	public String getDescript() {
		return descript;
	}

	public void setDescript(String descript) {
		this.descript = descript;
	}

	public int getMeshId() {
		return meshId;
	}

	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getCkStatus() {
		return ckStatus;
	}

	public void setCkStatus(int ckStatus) {
		this.ckStatus = ckStatus;
	}

	public int getAdjaFlag() {
		return adjaFlag;
	}

	public void setAdjaFlag(int adjaFlag) {
		this.adjaFlag = adjaFlag;
	}

	public int getRecStatusIn() {
		return recStatusIn;
	}

	public void setRecStatusIn(int recStatusIn) {
		this.recStatusIn = recStatusIn;
	}

	public int getRecStatusOut() {
		return recStatusOut;
	}

	public void setRecStatusOut(int recStatusOut) {
		this.recStatusOut = recStatusOut;
	}

	public String getTimeDescript() {
		return timeDescript;
	}

	public void setTimeDescript(String timeDescript) {
		this.timeDescript = timeDescript;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public String getLaneSpeedValue() {
		return laneSpeedValue;
	}

	public void setLaneSpeedValue(String laneSpeedValue) {
		this.laneSpeedValue = laneSpeedValue;
	}

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public RdSpeedlimit() {

	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	@Override
	public String tableName() {

		return "rd_speedlimit";
	}

	@Override
	public ObjStatus status() {

		return null;
	}

	@Override
	public void setStatus(ObjStatus os) {

	}

	@Override
	public ObjType objType() {

		return ObjType.RDSPEEDLIMIT;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {

		if (objLevel == ObjLevel.FULL) {
			speedValue /= 10;
			
			if(laneSpeedValue != null)
			{
				laneSpeedValue = StringUtils.laneSpeedValue2KM(laneSpeedValue);
			}
		}

		JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

		JSONObject json = JSONObject.fromObject(this, jsonConfig);

		return json;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if ("geometry".equals(key)) {

				Geometry jts = GeoTranslator.geojson2Jts(
						json.getJSONObject(key), 100000, 0);

				this.setGeometry(jts);

			} else {
				Field f = this.getClass().getDeclaredField(key);

				f.setAccessible(true);

				if ("speedValue".equals(key)) {
					int value = json.getInt(key);

					f.set(this, value * 10);
				}else if ("laneSpeedValue".equals(key)){
					String value = json.getString(key);
					
					f.set(this, StringUtils.laneSpeedValue2M(value));
				}
				else {
					f.set(this, json.get(key));
				}
			}
		}

		return true;
	}

	@Override
	public List<IRow> relatedRows() {

		return null;
	}

	@Override
	public void copy(IRow row) {

	}

	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public int pid() {

		return this.getPid();
	}

	@Override
	public String parentPKName() {

		return "pid";
	}

	@Override
	public int parentPKValue() {

		return this.getPid();
	}

	@Override
	public List<List<IRow>> children() {

		List<List<IRow>> children = new ArrayList<List<IRow>>();

		return children;
	}

	@Override
	public String parentTableName() {

		return "rd_speedlimit";
	}

	@Override
	public String rowId() {

		return rowId;
	}

	@Override
	public void setRowId(String rowId) {

		this.rowId = rowId;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {

		Iterator<?> keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			if (json.get(key) instanceof JSONArray) {
				continue;
			} else if ("longitude".equals(key)) {
				
				JSONObject geojson = new JSONObject();
				
				double longitude = json.getDouble("longitude");
				
				double latitude = json.getDouble("latitude");
				
				geojson.put("type", "Point");
				
				geojson.put("coordinates", new double[] { longitude, latitude });
				
				String wkt = Geojson.geojson2Wkt(geojson.toString());

				String oldwkt = GeoTranslator.jts2Wkt(geometry, 0.00001, 5);

				if (!wkt.equals(oldwkt)) {
					changedFields.put("geometry", geojson);
				}
			} else {
				if (!"objStatus".equals(key) && !"latitude".equals(key)) {

					Field field = this.getClass().getDeclaredField(key);

					field.setAccessible(true);

					Object objValue = field.get(this);

					String oldValue = null;

					if (objValue == null) {
						oldValue = "null";
					} else {
						oldValue = String.valueOf(objValue);
					}

					String newValue = json.getString(key);

					if (!newValue.equals(oldValue)) {
						if (key.equals("speedValue")) {

							newValue = String.valueOf(json.getInt(key) * 10);

							if (!newValue.equals(oldValue)) {
								changedFields.put(key, json.getInt(key) * 10);
							}
						}else if(key.equals("laneSpeedValue")){
							
							newValue = StringUtils.laneSpeedValue2M(json.getString(key));
							
							if (!newValue.equals(oldValue)) {
								changedFields.put(key, newValue);
							}
						}
						else {
							Object value = json.get(key);
							
							if(value instanceof String){
								changedFields.put(key, newValue.replace("'", "''"));
							}
							else{
								changedFields.put(key, value);
							}

						}
					}

				}
			}
		}

		if (changedFields.size() > 0) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public int mesh() {
		// TODO Auto-generated method stub
		return meshId;
	}

	@Override
	public void setMesh(int mesh) {
		this.meshId = mesh;
	}

	@Override
	public String primaryKey() {
		return "pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		return null;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		return null;
	}

}
