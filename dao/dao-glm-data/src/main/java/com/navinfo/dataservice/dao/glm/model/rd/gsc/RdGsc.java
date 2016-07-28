package com.navinfo.dataservice.dao.glm.model.rd.gsc;

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

public class RdGsc implements IObj  {
	private String rowId;
	
	private int pid;
	
	private Geometry geometry;
	
	private  int processFlag = 0 ;
	
	private Map<String, Object> changedFields = new HashMap<String, Object>();
	private List<IRow> links = new ArrayList<IRow>();
	
	public Map<String, RdGscLink> rdGscLinkMap = new HashMap<String, RdGscLink>();

	public List<IRow> getLinks() {
		return links;
	}

	public void setLinks(List<IRow> links) {
		this.links = links;
	}

	@Override
	public String rowId() {
		return rowId;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public int getProcessFlag() {
		return processFlag;
	}

	public void setProcessFlag(int processFlag) {
		this.processFlag = processFlag;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
		
	}

	@Override
	public String tableName() {
		return "rd_gsc";
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
		return ObjType.RDGSC;
	}

	@Override
	public void copy(IRow row) {
		RdGsc node = (RdGsc) row;
		this.setGeometry(node.getGeometry());
		this.setProcessFlag(node.getProcessFlag());
		this.links = new ArrayList<IRow>();
		for(IRow form :node.getLinks()){
			RdGscLink formCopy = new RdGscLink();
			formCopy.copy(form);
			formCopy.setPid(this.pid());
			this.links.add(formCopy);
		}
		
	}

	@Override
	public Map<String, Object> changedFields() {
		return changedFields;
	}

	@Override
	public String parentPKName() {
		return "pid";
	}

	@Override
	public int parentPKValue() {
		return this.pid();
	}

	@Override
	public String parentTableName() {
		return "rd_gsc";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();
		children.add(this.getLinks());
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
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		
		JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);
		
		if (objLevel == ObjLevel.FULL || objLevel == ObjLevel.HISTORY) {

			JSONObject json = JSONObject.fromObject(this, jsonConfig);
			
			return json;
		}
		return JSONObject.fromObject(this, jsonConfig);
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
		return pid;
	}

	@Override
	public String primaryKey() {
		return "pid";
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getRowId() {
		return rowId;
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdGscLink.class, links);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<Class<? extends IRow>,Map<String,?>>();
		childMap.put(RdGscLink.class, rdGscLinkMap);
		return childMap;
	}
	
}
