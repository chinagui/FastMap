/**
 * 
 */
package com.navinfo.dataservice.dao.glm.model.rd.crf;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.JsonUtils;
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
 * @ClassName: RdObject
 * @author Zhang Xiaolong
 * @date 2016年8月12日 下午3:27:54
 * @Description: TODO
 */
public class RdObject implements IObj {

	private int pid;

	// LANDMARK坐标
	private Geometry geometry;

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	private List<IRow> nodes = new ArrayList<>();

	private List<IRow> links = new ArrayList<>();

	private List<IRow> names = new ArrayList<>();

	private List<IRow> inters = new ArrayList<>();

	private List<IRow> roads = new ArrayList<>();
	
	public Map<String,RdObjectNode> nodeMap = new HashMap<>();
	
	public Map<String,RdObjectLink> linkMap = new HashMap<>();
	
	public Map<String,RdObjectName> nameMap = new HashMap<>();
	
	public Map<String,RdObjectInter> interMap = new HashMap<>();
	
	public Map<String,RdObjectRoad> roadMap = new HashMap<>();
	
	protected ObjStatus status;

	public List<IRow> getNodes() {
		return nodes;
	}

	public void setNodes(List<IRow> nodes) {
		this.nodes = nodes;
	}

	public List<IRow> getLinks() {
		return links;
	}

	public void setLinks(List<IRow> links) {
		this.links = links;
	}

	public List<IRow> getNames() {
		return names;
	}

	public void setNames(List<IRow> names) {
		this.names = names;
	}

	public List<IRow> getInters() {
		return inters;
	}

	public void setInters(List<IRow> inters) {
		this.inters = inters;
	}

	public List<IRow> getRoads() {
		return roads;
	}

	public void setRoads(List<IRow> roads) {
		this.roads = roads;
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
		return "RD_OBJECT";
	}

	@Override
	public ObjStatus status() {
		return status;
	}

	@Override
	public void setStatus(ObjStatus os) {
		status = os;
	}

	@Override
	public ObjType objType() {
		return ObjType.RDOBJECT;
	}

	@Override
	public void copy(IRow row) {
		RdObject object = (RdObject) row;

		this.pid = object.pid;

		this.geometry = object.geometry;

		this.links = new ArrayList<IRow>();

		for (IRow r : object.links) {

			RdObjectLink link = new RdObjectLink();

			link.copy(r);

			this.links.add(link);
		}

		this.nodes = new ArrayList<IRow>();

		for (IRow r : object.nodes) {

			RdObjectNode node = new RdObjectNode();

			node.copy(r);

			this.nodes.add(node);
		}

		this.inters = new ArrayList<IRow>();

		for (IRow r : object.inters) {

			RdObjectInter inter = new RdObjectInter();

			inter.copy(r);

			this.inters.add(inter);
		}

		this.roads = new ArrayList<IRow>();

		for (IRow r : object.roads) {

			RdObjectRoad road = new RdObjectRoad();

			road.copy(r);

			this.roads.add(road);
		}
	}

	@Override
	public Map<String, Object> changedFields() {
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		return "pid";
	}

	@Override
	public int parentPKValue() {
		return this.pid;
	}

	@Override
	public String parentTableName() {
		return "RD_OBJECT";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<>();

		children.add(this.nodes);

		children.add(this.links);

		children.add(this.roads);

		children.add(this.names);

		children.add(this.inters);

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
//		JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);
//
//		return JSONObject.fromObject(this, jsonConfig);
		JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);
		JSONObject json = JSONObject.fromObject(this, jsonConfig);

		if (objLevel == ObjLevel.HISTORY) {
			json.remove("status");
		}

		return json;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		return false;
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
		return "pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdObjectLink.class, links);
		childList.put(RdObjectNode.class, nodes);
		childList.put(RdObjectName.class, names);
		childList.put(RdObjectInter.class, inters);
		childList.put(RdObjectRoad.class, roads);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<Class<? extends IRow>,Map<String,?>>();
		childMap.put(RdObjectLink.class, linkMap);
		childMap.put(RdObjectNode.class, nodeMap);
		childMap.put(RdObjectName.class, nameMap);
		childMap.put(RdObjectInter.class, interMap);
		childMap.put(RdObjectRoad.class, roadMap);
		return childMap;
	}

}
