package com.navinfo.dataservice.dao.glm.model.lc;

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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Title: LcFace.java
 * @Description: 土地覆盖面
 * @author zhangyt
 * @date: 2016年7月27日 上午9:29:43
 * @version: v1.0
 */
public class LcFace implements IObj {

	private int pid;

	private String rowId;

	private int featurePid;

	private Geometry geometry;

	private int meshId;

	private int kind;

	private int form;

	private int displayClass;

	private double area;

	private double perimeter;

	private int scale;

	private int detailFlag;

	private int editFlag = 1;

	private Map<String, Object> changedFields = new HashMap<>();

	private List<IRow> topos = new ArrayList<>();

	private List<IRow> names = new ArrayList<>();

	public Map<String, LcFaceName> lcFaceNameMap = new HashMap<>();

	public Map<String, LcFaceTopo> lcFaceTopoMap = new HashMap<>();

	protected ObjStatus status;

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getFeaturePid() {
		return featurePid;
	}

	public void setFeaturePid(int featurePid) {
		this.featurePid = featurePid;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public int getMeshId() {
		return meshId;
	}

	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int getForm() {
		return form;
	}

	public void setForm(int form) {
		this.form = form;
	}

	public int getDisplayClass() {
		return displayClass;
	}

	public void setDisplayClass(int displayClass) {
		this.displayClass = displayClass;
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

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getDetailFlag() {
		return detailFlag;
	}

	public void setDetailFlag(int detailFlag) {
		this.detailFlag = detailFlag;
	}

	public int getEditFlag() {
		return editFlag;
	}

	public void setEditFlag(int editFlag) {
		this.editFlag = editFlag;
	}

	public List<IRow> getTopos() {
		return topos;
	}

	public void setTopos(List<IRow> topos) {
		this.topos = topos;
	}

	public List<IRow> getNames() {
		return names;
	}

	public void setNames(List<IRow> names) {
		this.names = names;
	}

	public String getRowId() {
		return rowId;
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

			@SuppressWarnings("unused")
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
		return "lc_face";
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
		return ObjType.LCFACE;
	}

	@Override
	public void copy(IRow row) {
		LcFace source = (LcFace) row;
		this.pid = source.pid;
		this.featurePid = source.featurePid;
		this.geometry = source.geometry;
		this.meshId = source.meshId;
		this.kind = source.kind;
		this.form = source.form;
		this.displayClass = source.displayClass;
		this.area = source.area;
		this.perimeter = source.perimeter;
		this.scale = source.scale;
		this.detailFlag = source.detailFlag;
		this.editFlag = source.editFlag;
		this.names = new ArrayList<IRow>();
		for (IRow r : source.names) {
			LcFaceName name = new LcFaceName();
			name.copy(r);
			this.names.add(name);
		}
		this.topos = new ArrayList<IRow>();
		for (IRow r : source.topos) {
			LcFaceTopo topo = new LcFaceTopo();
			topo.copy(r);
			this.topos.add(topo);
		}
	}

	@Override
	public Map<String, Object> changedFields() {
		return this.changedFields;
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
		return "lc_feature";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();
		children.add(this.names);
		children.add(this.topos);
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
		return this.meshId;
	}

	@Override
	public void setMesh(int mesh) {
		this.meshId = mesh;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		Map<Class<? extends IRow>, Map<String, ?>> childMap = new HashMap<Class<? extends IRow>, Map<String, ?>>();
		childMap.put(LcFaceName.class, lcFaceNameMap);
		childMap.put(LcFaceTopo.class, lcFaceTopoMap);
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

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(LcFaceName.class, names);
		childList.put(LcFaceTopo.class, topos);
		return childList;
	}

}
