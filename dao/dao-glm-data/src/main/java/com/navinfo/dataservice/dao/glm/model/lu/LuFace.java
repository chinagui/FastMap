package com.navinfo.dataservice.dao.glm.model.lu;

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

public class LuFace implements IObj {

	private int pid;

	private String rowId;

	private int featurePid;

	private Geometry geometry;

	private int kind;

	private double area;

	private double perimeter;

	private int meshId;

	private int editFlag = 1;

	private int detailFlag;

	private Map<String, Object> changedFields = new HashMap<>();

	private List<IRow> faceTopos = new ArrayList<>();

	public Map<String, LuFaceTopo> luFaceTopoMap = new HashMap<>();

	private List<IRow> faceNames = new ArrayList<>();

	public Map<String, LuFaceName> luFaceNameMap = new HashMap<>();

	protected ObjStatus status;

	public LuFace() {
	}

	@Override
	public String rowId() {
		return this.rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	@Override
	public String tableName() {
		return "lu_face";
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
		return ObjType.LUFACE;
	}

	@Override
	public void copy(IRow row) {
		LuFace sourceFace = (LuFace) row;

		this.detailFlag = sourceFace.getDetailFlag();
		this.editFlag = sourceFace.getEditFlag();
		this.featurePid = sourceFace.getFeaturePid();
		this.geometry = sourceFace.getGeometry();
		this.kind = sourceFace.getKind();
		this.meshId = sourceFace.getMeshId();
		this.perimeter = sourceFace.getPerimeter();
		this.pid = sourceFace.getPid();
		this.rowId = sourceFace.getRowId();
		this.faceNames = new ArrayList<IRow>();
		for (IRow name : sourceFace.getFaceNames()) {
			LuFaceName fn = new LuFaceName();
			fn.copy(name);
			this.faceNames.add(fn);
		}
		this.faceTopos = new ArrayList<IRow>();
		for (IRow topo : sourceFace.getFaceTopos()) {
			LuFaceTopo ft = new LuFaceTopo();
			ft.copy(topo);
			this.faceTopos.add(ft);
		}
	}

	@Override
	public Map<String, Object> changedFields() {
		return changedFields;
	}

	@Override
	public String parentPKName() {
		return "feature_pid";
	}

	@Override
	public int parentPKValue() {
		return this.featurePid;
	}

	@Override
	public String parentTableName() {
		return "lu_feature";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();
		children.add(this.faceTopos);
		children.add(this.faceNames);

		return children;
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
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

		JSONObject json = JSONObject.fromObject(this, jsonConfig);

		return json;
	}

	@SuppressWarnings("unused")
	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		@SuppressWarnings("rawtypes")
		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

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

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
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

	public List<IRow> getFaceTopos() {
		return faceTopos;
	}

	public void setFaceTopos(List<IRow> faceTopos) {
		this.faceTopos = faceTopos;
	}

	public String getRowId() {
		return rowId;
	}

	public int getFeaturePid() {
		return featurePid;
	}

	public void setFeaturePid(int featurePid) {
		this.featurePid = featurePid;
	}

	public int getDetailFlag() {
		return detailFlag;
	}

	public void setDetailFlag(int detailFlag) {
		this.detailFlag = detailFlag;
	}

	public List<IRow> getFaceNames() {
		return faceNames;
	}

	public void setFaceNames(List<IRow> faceNames) {
		this.faceNames = faceNames;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>, List<IRow>> childMap = new HashMap<>();
		childMap.put(LuFaceTopo.class, faceTopos);
		childMap.put(LuFaceName.class, faceNames);
		return childMap;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		Map<Class<? extends IRow>, Map<String, ?>> childMap = new HashMap<>();
		childMap.put(LuFaceTopo.class, luFaceTopoMap);
		childMap.put(LuFaceName.class, luFaceNameMap);
		return childMap;
	}

	@Override
	public List<IRow> relatedRows() {
		return null;
	}

	@Override
	public int pid() {
		return this.pid;
	}

	@Override
	public String primaryKey() {
		return "face_pid";
	}

}
