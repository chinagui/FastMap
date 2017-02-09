package com.navinfo.dataservice.dao.glm.model.rd.branch;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.vividsolutions.jts.geom.Geometry;

public class RdSignboard implements IObj {

	private int pid;

	private int branchPid;

	private String arrowCode;
	
	private String backimageCode;
	
	private String rowId;
	private Geometry geometry;
	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	private Map<String, Object> changedFields = new HashMap<String, Object>();
	
	private List<IRow> names = new ArrayList<IRow>();
	
	public Map<Integer, RdSignboardName> nameMap = new HashMap<Integer, RdSignboardName>();
	
	protected  ObjStatus status;

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public RdSignboard() {

	}
	
	public String getRowId() {
		return rowId;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getArrowCode() {
		return arrowCode;
	}

	public void setArrowCode(String arrowCode) {
		this.arrowCode = arrowCode;
	}

	public String getBackimageCode() {
		return backimageCode;
	}

	public void setBackimageCode(String backimageCode) {
		this.backimageCode = backimageCode;
	}

	public List<IRow> getNames() {
		return names;
	}

	public void setNames(List<IRow> names) {
		this.names = names;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {

		return JSONObject.fromObject(this,Geojson.geoJsonConfig(0.00001, 5));
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {

				switch (key) {
				case "names":
					names.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdSignboardName row = new RdSignboardName();

						row.Unserialize(jo);

						names.add(row);
					}

					break;

				default:
					break;
				}

			} else {
				Field f = this.getClass().getDeclaredField(key);

				f.setAccessible(true);

				f.set(this, json.get(key));
			}
		}

		return true;
	}

	@Override
	public String tableName() {

		return "rd_signboard";
	}

	@Override
	public ObjStatus status() {

		return status;
	}

	@Override
	public void setStatus(ObjStatus os) {
		status=os;
	}

	@Override
	public ObjType objType() {

		return ObjType.RDSIGNBOARD;
	}


	@Override
	public void copy(IRow row) {

	}
	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public String parentPKName() {

		return "branch_pid";
	}

	@Override
	public int parentPKValue() {

		return this.getBranchPid();
	}

	@Override
	public List<List<IRow>> children() {
		
		List<List<IRow>> children = new ArrayList<List<IRow>>();

		children.add(this.getNames());

		return children;
	}

	@Override
	public String parentTableName() {

		return "rd_branch";
	}

	@Override
	public String rowId() {

		return this.rowId;
	}

	public int getBranchPid() {
		return branchPid;
	}

	public void setBranchPid(int branchPid) {
		this.branchPid = branchPid;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		
		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {
				continue;
			}  else {
				if (!"objStatus".equals(key)) {
					
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
	public int mesh() {
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
	}

	@Override
	public String primaryKey() {
		return "signboard_id";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdSignboardName.class, names);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		return null;
	}

}
