package com.navinfo.dataservice.dao.glm.model.ad.geo;

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
import com.vividsolutions.jts.geom.Geometry;

public class AdAdmin implements IObj {

	private int adminId = 0;
	private int extendId = 0;
	private double adminType = 0;
	public String getRowId() {
		return rowId;
	}

	private   int capital = 0; 
	
	private String population ;
	private Geometry geometry;
	
	private int linkPid = 0;
	private int nameGroupid = 0;
	private int side = 0;
	private int pid;
	private int roadFlag =0 ;
	
	private int pmeshId = 0;
	private int jisCode = 0;
	private int meshId = 0;
    public int getMeshId() {
		return meshId;
	}

	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}

	private int editFlag = 1;
    private String memo;
    private String rowId;
    private Map<String, Object> changedFields = new HashMap<String, Object>();
    private List<IRow> groups = new ArrayList<IRow>();
    private List<IRow> names = new ArrayList<IRow>();
    private List<IRow> details = new ArrayList<IRow>();
    
    public Map<String, AdAdminName> adAdminNameMap = new HashMap<String, AdAdminName>();
    
    public Map<String, AdAdminGroup> adAdminGroupMap = new HashMap<String, AdAdminGroup>();
    
    public Map<String, AdAdminDetail> adAdminDetailMap = new HashMap<String, AdAdminDetail>();

    protected ObjStatus status;
    
	@Override
	public String rowId() {
		return rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
		
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	@Override
	public String tableName() {
		return "ad_admin";
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
		return ObjType.ADADMIN;
	}

	@Override
	public void copy(IRow row) {
	}

	@Override
	public Map<String, Object> changedFields() {
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		return "region_id";
	}

	@Override
	public int parentPKValue() {
		return pid;
	}

	@Override
	public String parentTableName() {
		return "ad_admin";
	}

	public int getAdminId() {
		return adminId;
	}

	public void setAdminId(int adminId) {
		this.adminId = adminId;
	}

	public int getExtendId() {
		return extendId;
	}

	public void setExtendId(int extendId) {
		this.extendId = extendId;
	}

	public double getAdminType() {
		return adminType;
	}

	public void setAdminType(double adminType) {
		this.adminType = adminType;
	}

	public int getCapital() {
		return capital;
	}

	public void setCapital(int capital) {
		this.capital = capital;
	}

	public String getPopulation() {
		return population;
	}

	public void setPopulation(String population) {
		this.population = population;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}
	
	public int getNameGroupid() {
		return nameGroupid;
	}

	public void setNameGroupid(int nameGroupid) {
		this.nameGroupid = nameGroupid;
	}

	public int getSide() {
		return side;
	}

	public void setSide(int side) {
		this.side = side;
	}

	public int getPmeshId() {
		return pmeshId;
	}

	public void setPmeshId(int pmeshId) {
		this.pmeshId = pmeshId;
	}

	public int getJisCode() {
		return jisCode;
	}

	public void setJisCode(int jisCode) {
		this.jisCode = jisCode;
	}
	public int getRoadFlag() {
		return roadFlag;
	}

	public void setRoadFlag(int roadFlag) {
		this.roadFlag = roadFlag;
	}

	public int getEditFlag() {
		return editFlag;
	}

	public void setEditFlag(int editFlag) {
		this.editFlag = editFlag;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}


	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();
		children.add(this.getGroups());
		children.add(this.getNames());
		children.add(this.getDetails());
		return children;
	}

	public List<IRow> getGroups() {
		return groups;
	}

	public void setGroups(List<IRow> groups) {
		this.groups = groups;
	}

	public List<IRow> getNames() {
		return names;
	}

	public void setNames(List<IRow> names) {
		this.names = names;
	}

	public List<IRow> getDetails() {
		return details;
	}

	public void setDetails(List<IRow> details) {
		this.details = details;
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
		return this.getMeshId();
	}

	@Override
	public void setMesh(int mesh) {
		this.meshId= mesh;
		
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);
		
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
		return this.getPid();
	}

	@Override
	public String primaryKey() {
		return "region_id";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<>();
		childList.put(AdAdminGroup.class, groups);
		childList.put(AdAdminName.class, names);
		childList.put(AdAdminDetail.class, details);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<>();
		childMap.put(AdAdminGroup.class, adAdminGroupMap);
		childMap.put(AdAdminName.class, adAdminNameMap);
		childMap.put(AdAdminDetail.class, adAdminDetailMap);
		return childMap;
	}
}
