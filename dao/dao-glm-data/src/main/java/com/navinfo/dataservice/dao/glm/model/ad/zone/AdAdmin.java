package com.navinfo.dataservice.dao.glm.model.ad.zone;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.JsonUtils;
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
	private   int capital = 0; 
	
	private String population ;
	private Geometry geometry;
	
	private int linkPid = 0;
	private int nameGroupId = 0;
	private int side = 0;
	private int pid;
	private int roadFlag =0 ;
	
	private int pMeshId = 0;
	
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatus(ObjStatus os) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjType objType() {
		return ObjType.ADADMIN;
	}

	@Override
	public void copy(IRow row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> changedFields() {
		// TODO Auto-generated method stub
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		// TODO Auto-generated method stub
		return "region_id";
	}

	@Override
	public int parentPKValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String parentTableName() {
		// TODO Auto-generated method stub
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

	public int getNameGroupId() {
		return nameGroupId;
	}

	public void setNameGroupId(int nameGroupId) {
		this.nameGroupId = nameGroupId;
	}

	public int getSide() {
		return side;
	}

	public void setSide(int side) {
		this.side = side;
	}

	public int getpMeshId() {
		return pMeshId;
	}

	public void setpMeshId(int pMeshId) {
		this.pMeshId = pMeshId;
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

	public Map<String, Object> getChangedFields() {
		return changedFields;
	}

	public void setChangedFields(Map<String, Object> changedFields) {
		this.changedFields = changedFields;
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
		return JSONObject.fromObject(this, JsonUtils.getStrConfig());
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<IRow> relatedRows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int pid() {
		// TODO Auto-generated method stub
		return this.getPid();
	}

	@Override
	public String primaryKey() {
		// TODO Auto-generated method stub
		return "region_id";
	}

}
