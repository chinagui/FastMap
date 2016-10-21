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
 * ZONE:LINK 表
 * @author zhaokk
 *
 */
public class ZoneLink implements IObj {

	private int pid;

	private int sNodePid;//起始节点

	private int eNodePid;//终止节点

	private Geometry geometry;//LINK 坐标

	private double length;//LINK 长度

	private int scale;

	private int editFlag = 1;

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	private List<IRow> meshes = new ArrayList<IRow>();
	private List<IRow> kinds = new ArrayList<IRow>();

	public Map<String, ZoneNode> nodeMap = new HashMap<String, ZoneNode>();

	public Map<String, ZoneLinkMesh> meshMap = new HashMap<String, ZoneLinkMesh>();

	public Map<String, ZoneLinkKind> kindMap = new HashMap<String, ZoneLinkKind>();

	public ZoneLink() {
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
		return "zone_link";
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
		return ObjType.ZONELINK;
	}

	@Override
	public void copy(IRow row) {
		ZoneLink zoneLink = (ZoneLink) row;

		this.editFlag = zoneLink.getEditFlag();

		this.eNodePid = zoneLink.geteNodePid();

		this.geometry = zoneLink.getGeometry();

		this.length = zoneLink.getLength();

		this.scale = zoneLink.getScale();

		this.sNodePid = zoneLink.getsNodePid();

		for (IRow mesh : zoneLink.meshes) {

			ZoneLinkMesh adLinkMesh = new ZoneLinkMesh();

			adLinkMesh.copy(mesh);

			adLinkMesh.setLinkPid(this.getPid());

			this.meshes.add(adLinkMesh);
		}
		for(IRow kind:zoneLink.kinds){
			ZoneLinkKind zoneLinkKind = new ZoneLinkKind();
			zoneLinkKind.copy(kind);
			zoneLinkKind.setLinkPid(this.getPid());
			this.kinds.add(zoneLinkKind);
			
		}
	}

	@Override
	public Map<String, Object> changedFields() {
		return changedFields;
	}

	@Override
	public String parentPKName() {
		return "link_pid";
	}

	@Override
	public int parentPKValue() {
		return this.getPid();
	}

	@Override
	public String parentTableName() {
		return "zone_link";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();

		children.add(this.getMeshes());
        children.add(this.getKinds());
		return children;
	}

	public List<IRow> getKinds() {
		return kinds;
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

	public void setMeshes(List<IRow> meshes) {
		this.meshes = meshes;
	}

	public void setKinds(List<IRow> kinds) {
		this.kinds = kinds;
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

						ZoneLinkMesh row= new ZoneLinkMesh();

						row.Unserialize(jo);

						meshes.add(row);
					}

					break;
				case "kinds":

					kinds.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						ZoneLinkKind row = new ZoneLinkKind();

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
		return "link_pid";
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getsNodePid() {
		return sNodePid;
	}

	public void setsNodePid(int sNodePid) {
		this.sNodePid = sNodePid;
	}

	public int geteNodePid() {
		return eNodePid;
	}

	public void seteNodePid(int eNodePid) {
		this.eNodePid = eNodePid;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
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
	public String getRowId() {
		return rowId;
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<>();
		childList.put(ZoneLinkMesh.class, meshes);
		childList.put(ZoneLinkKind.class, kinds);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<>();
		childMap.put(ZoneLinkMesh.class, meshMap);
		childMap.put(ZoneLinkKind.class, kindMap);
		return childMap;
	}
}
