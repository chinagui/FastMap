package com.navinfo.dataservice.FosEngine.edit.model.bean.ad;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import com.navinfo.dataservice.FosEngine.edit.model.IObj;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.vividsolutions.jts.geom.Geometry;

public class AdFace implements IObj {

	private String rowId;

	private int pid;

	private int regionId;

	private Geometry geometry;

	private double area;

	private double perimeter;

	private int meshId;

	private int editFlag;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public AdFace() {

	}

	public int getPid() {
		return pid;
	}

	public void setPid(int nodePid) {
		this.pid = nodePid;
	}

	public int getRegionId() {
		return regionId;
	}

	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public double getPerimeter() {
		return perimeter;
	}

	public void setPerimeter(double perimeter) {
		this.perimeter = perimeter;
	}

	public int getMeshId() {
		return meshId;
	}

	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}

	public int getEditFlag() {
		return editFlag;
	}

	public void setEditFlag(int editFlag) {
		this.editFlag = editFlag;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	@Override
	public String tableName() {

		return "AD_FACE";
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

		return ObjType.ADFACE;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {

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

			if (json.get(key) instanceof JSONArray) {

			} else if ("geometry".equals(key)) {

				Geometry jts = GeoTranslator.geojson2Jts(
						json.getJSONObject(key), 100000, 0);

				this.setGeometry(jts);

			} else {
				Field f = this.getClass().getDeclaredField(key);

				f.setAccessible(true);

				f.set(this, json.get(key));
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

		return "face_pid";
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

		return "AD_FACE";
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

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			if (json.get(key) instanceof JSONArray) {
				continue;
			} else if ("geometry".equals(key)) {

				JSONObject geojson = json.getJSONObject(key);

				String wkt = Geojson.geojson2Wkt(geojson.toString());

				String oldwkt = GeoTranslator.jts2Wkt(geometry, 0.00001, 5);

				if (!wkt.equals(oldwkt)) {
					changedFields.put(key, json.getJSONObject(key));
				}
			} else {
				if (!"objStatus".equals(key)) {

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
						changedFields.put(key,newValue.replace("'","''"));

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
		// TODO Auto-generated method stub
		this.meshId = mesh;
	}

	@Override
	public String primaryKey() {
		// TODO Auto-generated method stub
		return "face_pid";
	}
}
