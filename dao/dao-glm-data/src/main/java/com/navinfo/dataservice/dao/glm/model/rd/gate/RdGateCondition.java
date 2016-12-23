package com.navinfo.dataservice.dao.glm.model.rd.gate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdGateCondition implements IRow {
	
	private String rowId;
	private int pid;
	private int validObj = 0;
	private String timeDomain = "";
	public Map<String, Object> changedFields = new HashMap<String, Object>();
	private ObjStatus status;
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getValidObj() {
		return validObj;
	}

	public void setValidObj(int validObj) {
		this.validObj = validObj;
	}

	public String getTimeDomain() {
		return timeDomain;
	}

	public void setTimeDomain(String timeDomain) {
		this.timeDomain = timeDomain;
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
		return "rd_gate_condition";
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
		return ObjType.RDGATECONDITION;
	}

	@Override
	public void copy(IRow row) {
		RdGateCondition node = (RdGateCondition) row;
		this.validObj = node.getValidObj();
		this.timeDomain = node.getTimeDomain();

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
	public String parentTableName() {
		return "rd_gate";
	}

	@Override
	public List<List<IRow>> children() {
		return null;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		@SuppressWarnings("rawtypes")
		Iterator keys = json.keys();
		
		while (keys.hasNext()) {
			String key = (String) keys.next();

			if (json.get(key) instanceof JSONArray) {
				continue;
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

		JSONObject json = JSONObject.fromObject(this);

		return json;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		return false;
	}

	public String getRowId() {
		return rowId;
	}

	@Override
	public int parentPKValue() {
		return this.getPid();
	}
}
