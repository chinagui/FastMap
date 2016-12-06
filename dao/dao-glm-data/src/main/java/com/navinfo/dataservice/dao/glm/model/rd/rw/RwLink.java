package com.navinfo.dataservice.dao.glm.model.rd.rw;

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
 * 铁路:LINK表
 * 
 * @author zhangxiaolong
 *
 */
public class RwLink implements IObj {

	private int pid;

	// 要素号码
	private int featurePid;

	// 起点号码
	private int sNodePid;

	// 终点号码
	private int eNodePid;

	// LINK种别
	private int kind = 1;

	// LINK形态
	private int form;

	// LINK长度
	private double length;

	// LINK坐标
	private Geometry geometry;

	// 图幅号码
	private int meshId;

	// 比例尺
	private int scale;

	// 详细区域标识
	private int detailFlag;

	// 编辑标识
	private int editFlag = 1;

	// 线路渲染颜色
	private String color;

	// 行记录ID
	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	private List<IRow> nodes = new ArrayList<>();

	public Map<String, RwNode> nodeMap = new HashMap<>();

	private List<IRow> names = new ArrayList<>();

	public Map<String, RwLinkName> linkNameMap = new HashMap<>();

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

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
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

	public List<IRow> getNodes() {
		return nodes;
	}

	public void setNodes(List<IRow> nodes) {
		this.nodes = nodes;
	}

	public List<IRow> getNames() {
		return names;
	}

	public void setNames(List<IRow> names) {
		this.names = names;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getRowId() {
		return rowId;
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
		return "rw_link";
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
		return ObjType.RWLINK;
	}

	@Override
	public void copy(IRow row) {
		RwLink link = (RwLink) row;
		this.setColor(link.getColor());
		this.setDetailFlag(link.getDetailFlag());
		this.setEditFlag(link.getEditFlag());
		this.seteNodePid(link.geteNodePid());
		this.setsNodePid(link.getsNodePid());
		this.setFeaturePid(link.getFeaturePid());
		this.setMeshId(link.getMeshId());
		this.setForm(link.getForm());
		this.setGeometry(link.getGeometry());
		this.setKind(link.getKind());
		for (IRow name : link.names) {
			RwLinkName linkName = new RwLinkName();
			linkName.copy(name);
			linkName.setLinkPid(this.pid);
			this.names.add(linkName);
		}
	}

	@Override
	public Map<String, Object> changedFields() {
		return changedFields;
	}

	@Override
	public String parentPKName() {
		return "FEATURE_PID";
	}

	@Override
	public int parentPKValue() {
		return this.featurePid;
	}

	@Override
	public String parentTableName() {
		return "RW_FEATURE";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<>();

		children.add(this.nodes);

		children.add(this.names);

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
		this.meshId = mesh;
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
				case "nodes":
					nodes.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RwNode row = new RwNode();

						row.Unserialize(jo);

						nodes.add(row);
					}

					break;
				case "names":
					names.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RwLinkName row = new RwLinkName();

						row.Unserialize(jo);

						names.add(row);
					}

					break;
				}
			} else if ("geometry".equals(key)) {

				Geometry jts = GeoTranslator.geojson2Jts(json.getJSONObject(key), 100000, 0);

				this.setGeometry(jts);

			} else if (!"objStatus".equals(key)) {

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
		return this.pid;
	}

	@Override
	public String primaryKey() {
		return "link_pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>, List<IRow>> childList = new HashMap<>();

		childList.put(RwLinkName.class, names);

		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		
		Map<Class<? extends IRow>,Map<String ,?>> childMap = new HashMap<>();
		
		childMap.put(RwLinkName.class, linkNameMap);
		
		return childMap;
	}

}
