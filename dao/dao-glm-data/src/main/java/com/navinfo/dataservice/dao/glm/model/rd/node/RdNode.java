package com.navinfo.dataservice.dao.glm.model.rd.node;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.vividsolutions.jts.geom.Geometry;

public class RdNode implements IObj {
	
	private String rowId;

	private int pid;

	private int kind = 1;

	private int adasFlag = 2;

	private int editFlag = 1;

	private String difGroupid;

	private int srcFlag = 6;

	private int digitalLevel;

	private Geometry geometry;

	private String reserved;
	
	protected ObjStatus status;

	private List<IRow> forms = new ArrayList<IRow>();

	private List<IRow> meshes = new ArrayList<IRow>();

	private List<IRow> names = new ArrayList<IRow>();

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public Map<String, RdNodeForm> formMap = new HashMap<String, RdNodeForm>();

	public Map<String, RdNodeMesh> meshMap = new HashMap<String, RdNodeMesh>();

	public Map<String, RdNodeName> nameMap = new HashMap<String, RdNodeName>();
	
	public RdNode() {
	}
	
	public String getRowId() {
		return rowId;
	}

	public String getReserved() {
		return reserved;
	}

	public void setReserved(String reserved) {
		this.reserved = reserved;
	}

	public List<IRow> getForms() {
		return forms;
	}

	public void setForms(List<IRow> forms) {
		this.forms = forms;
	}

	public List<IRow> getMeshes() {
		return meshes;
	}

	public void setMeshes(List<IRow> meshes) {
		this.meshes = meshes;
	}

	public List<IRow> getNames() {
		return names;
	}

	public void setNames(List<IRow> names) {
		this.names = names;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int nodePid) {
		this.pid = nodePid;
	}

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int getAdasFlag() {
		return adasFlag;
	}

	public void setAdasFlag(int adasFlag) {
		this.adasFlag = adasFlag;
	}

	public int getEditFlag() {
		return editFlag;
	}

	public void setEditFlag(int editFlag) {
		this.editFlag = editFlag;
	}

	public String getDifGroupid() {
		return difGroupid;
	}

	public void setDifGroupid(String difGroupid) {
		this.difGroupid = difGroupid;
	}

	public int getSrcFlag() {
		return srcFlag;
	}

	public void setSrcFlag(int srcFlag) {
		this.srcFlag = srcFlag;
	}

	public int getDigitalLevel() {
		return digitalLevel;
	}

	public void setDigitalLevel(int digitalLevel) {
		this.digitalLevel = digitalLevel;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	@Override
	public String tableName() {

		return "rd_node";
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

		return ObjType.RDNODE;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {

		JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

		//JSONObject json = JSONObject.fromObject(this, jsonConfig);
		JSONObject json = JSONObject.fromObject(this, jsonConfig);
		if (objLevel == ObjLevel.HISTORY) {
			json.remove("status");
		}

		return json;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {

				switch (key) {
				case "forms":
					forms.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdNodeForm row = new RdNodeForm();

						row.Unserialize(jo);

						forms.add(row);
					}

					break;
				case "meshes":

					meshes.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdNodeMesh row = new RdNodeMesh();

						row.Unserialize(jo);

						meshes.add(row);
					}

					break;

				case "names":

					names.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdNodeName row = new RdNodeName();

						row.Unserialize(jo);

						names.add(row);
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
	public void copy(IRow row) {

		RdNode node = (RdNode) row;

		this.kind = node.kind;

		this.geometry = node.geometry;

		this.adasFlag = node.adasFlag;

		this.editFlag = node.editFlag;

		this.difGroupid = node.difGroupid;

		this.srcFlag = node.srcFlag;

		this.digitalLevel = node.digitalLevel;

		this.reserved = node.reserved;

		this.forms = new ArrayList<IRow>();

		for (IRow form : node.forms) {

			RdNodeForm formCopy = new RdNodeForm();

			formCopy.copy(form);
			
			formCopy.setNodePid(this.getPid());

			this.forms.add(formCopy);
		}

		this.names = new ArrayList<IRow>();

		for (IRow name : node.names) {

			RdNodeName nameCopy = new RdNodeName();
			
			nameCopy.copy(name);

			nameCopy.setNodePid(this.getPid());
			
			try {
				nameCopy.setPid(PidUtil.getInstance().applyNodeNameId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			this.names.add(nameCopy);
		}

		this.meshes = new ArrayList<IRow>();

		for (IRow mesh : node.meshes) {

			RdNodeMesh meshCopy = new RdNodeMesh();

			meshCopy.copy(mesh);
			
			meshCopy.setNodePid(this.getPid());

			this.meshes.add(meshCopy);
		}
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

		return "node_pid";
	}

	@Override
	public int parentPKValue() {

		return this.getPid();
	}

	@Override
	public List<List<IRow>> children() {

		List<List<IRow>> children = new ArrayList<List<IRow>>();

		children.add(this.getForms());

		children.add(this.getNames());

		children.add(this.getMeshes());

		return children;
	}

	@Override
	public String parentTableName() {

		return "rd_node";
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
			}  else if ("geometry".equals(key)) {
				
				JSONObject geojson = json.getJSONObject(key);
				
				String wkt = Geojson.geojson2Wkt(geojson.toString());
				
				String oldwkt = GeoTranslator.jts2Wkt(geometry, 0.00001, 5);
				
				if(!wkt.equals(oldwkt))
				{
					changedFields.put(key, json.getJSONObject(key));
				}
			}  else {
				if ( !"objStatus".equals(key)) {
					
					Field field = this.getClass().getDeclaredField(key);
					
					field.setAccessible(true);
					
					Object objValue = field.get(this);
					
					String oldValue = null;
					
					if (objValue == null){
						oldValue = "null";
					}else{
						oldValue = String.valueOf(objValue);
					}
					
					String newValue = json.getString(key);
					
					if (!newValue.equals(oldValue)){
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
		
		if (changedFields.size() >0){
			return true;
		}else{
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
	public String primaryKey() {
		return "node_pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdNodeName.class, names);
		childList.put(RdNodeMesh.class, meshes);
		childList.put(RdNodeForm.class, forms);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<Class<? extends IRow>,Map<String,?>>();
		childMap.put(RdNodeName.class, nameMap);
		childMap.put(RdNodeMesh.class, meshMap);
		childMap.put(RdNodeForm.class, formMap);
		return childMap;
	}
}
