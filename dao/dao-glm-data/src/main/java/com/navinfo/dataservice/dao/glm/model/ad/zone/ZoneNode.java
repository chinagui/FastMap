package com.navinfo.dataservice.dao.glm.model.ad.zone;

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
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNodeMesh;
import com.vividsolutions.jts.geom.Geometry;


/**
 * ZoneNode表
 * @author luyao
 *
 */
public class ZoneNode implements IObj {
	
	private String rowId;
	
	private int pid;
	
	private int kind=1;//种别
	
	private int form=0;//形态
	
	private Geometry geometry;

	private int editFlag = 1;//编辑标识
	
	private Map<String, Object> changedFields = new HashMap<>();
	
	private List<IRow> meshes = new ArrayList<>();

	public Map<String, ZoneNodeMesh> meshMap = new HashMap<>();

	protected ObjStatus status;
	
	public ZoneNode() {
	}	
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
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

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public int getEditFlag() {
		return editFlag;
	}

	public void setEditFlag(int editFlag) {
		this.editFlag = editFlag;
	}

	public List<IRow> getMeshes() {
		return meshes;
	}

	public void setMeshes(List<IRow> meshes) {
		this.meshes = meshes;
	}

	public String getRowId() {
		return rowId;
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
	public String tableName() {
		return "zone_node";
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
		return ObjType.ZONENODE;
	}

	@Override
	public void copy(IRow row) {
        ZoneNode node = (ZoneNode) row;
        this.kind = node.getKind();
        this.form = node.getForm();
        this.geometry = node.getGeometry();
        List<IRow> meshes = new ArrayList<>();
        for (IRow m : node.getMeshes()) {
            ZoneNodeMesh mesh = new ZoneNodeMesh();
            mesh.copy(m);
            mesh.setNodePid(this.pid);
            meshes.add(mesh);
        }
        this.meshes = meshes;
	}

	@Override
	public Map<String, Object> changedFields() {
		return changedFields;
	}

	@Override
	public String parentPKName() {
		return "node_pid";
	}

	@Override
	public int parentPKValue() {
		return this.getPid();
	}

	@Override
	public String parentTableName() {
		return "zone_node";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();
		children.add(this.getMeshes());
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

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		@SuppressWarnings("rawtypes")
		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {

				switch (key) {
				case "meshes":

					meshes.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						ZoneNodeMesh row = new ZoneNodeMesh();

						row.Unserialize(jo);

						meshes.add(row);
					}
					break;
				default:
					break;
				}

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
	public int pid() {
		return this.getPid();
	}

	@Override
	public String primaryKey() {
		return "node_pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<>();
		childList.put(ZoneNodeMesh.class, meshes);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>>childMap = new HashMap<>();
		childMap.put(ZoneNodeMesh.class, meshMap);
		return childMap;
	}

}
