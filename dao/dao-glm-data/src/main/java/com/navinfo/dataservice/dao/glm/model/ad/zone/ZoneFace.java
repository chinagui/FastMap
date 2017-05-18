package com.navinfo.dataservice.dao.glm.model.ad.zone;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
/**
 * 
 * 
 * @author zhaokk
 *ZONE:FACE 表
 */
public class ZoneFace implements IObj {

	private String rowId;

	private int pid;

	private int regionId;

	private Geometry geometry;

	private double area;

	private double perimeter;

	private int meshId;

	private int editFlag = 1;

	private Map<String, Object> changedFields = new HashMap<>();

	private List<IRow> faceTopos = new ArrayList<>();

	public Map<String, ZoneFaceTopo> zoneFaceTopoMap = new HashMap<>();

	protected ObjStatus status;

	public ZoneFace() {
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
		return "zone_face";
	}

	@Override
	public ObjStatus status() {
		return this.status;
	}

	@Override
	public void setStatus(ObjStatus os) {
        this.status = os;
	}

	@Override
	public ObjType objType() {

		return ObjType.ZONEFACE;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {

		JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

		JSONObject json = JSONObject.fromObject(this, jsonConfig);

		return json;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		@SuppressWarnings("rawtypes")
		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			if (json.get(key) instanceof JSONArray) {

			} else if ("geometry".equals(key)) {

				Geometry jts = GeoTranslator.geojson2Jts(json.getJSONObject(key), 100000, 0);

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

		children.add(faceTopos);
		return children;
	}

	@Override
	public String parentTableName() {

		return "ZONE_FACE";
	}

	@Override
	public String rowId() {

		return rowId;
	}

	public String getRowId() {
		return rowId;
	}

	@Override
	public void setRowId(String rowId) {

		this.rowId = rowId;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {

		@SuppressWarnings("rawtypes")
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
						Object value = json.get(key);

						if (value instanceof String) {
							changedFields.put(key, newValue.replace("'", "''"));
						} else {
							changedFields.put(key, value);
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
		return meshId;
	}

	@Override
	public void setMesh(int mesh) {
		this.meshId = mesh;
	}

	@Override
	public String primaryKey() {
		return "face_pid";
	}

	public List<IRow> getFaceTopos() {
		return faceTopos;
	}

	public void setFaceTopos(List<IRow> faceTopos) {
		this.faceTopos = faceTopos;
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<>();
		childList.put(ZoneFaceTopo.class, faceTopos);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<>();
		childMap.put(ZoneFaceTopo.class, zoneFaceTopoMap);
		return childMap;
	}

}
