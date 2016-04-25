package com.navinfo.dataservice.dao.glm.model.ad.zone;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class AdAdminGroup implements IObj {
	private int regionIdUp;
	private int meshId = 0;
	private int pid;
    private String rowId;
    private Map<String, Object> changedFields = new HashMap<String, Object>();
    private List<IRow> parts = new ArrayList<IRow>();
    public Map<String, AdAdminPart> adAdminPartMap = new HashMap<String, AdAdminPart>();
    
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
		return "ad_admin_group";
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
		return ObjType.ADADMINGROUP;
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
		return "region_id_up";
	}

	@Override
	public int parentPKValue() {
		// TODO Auto-generated method stub
		return this.getRegionIdUp();
	}

	public int getRegionIdUp() {
		return regionIdUp;
	}

	public void setRegionIdUp(int regionIdUp) {
		this.regionIdUp = regionIdUp;
	}

	@Override
	public String parentTableName() {
		// TODO Auto-generated method stub
		return "ad_admin";
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
		children.add(this.parts);
		return children;
	}
	public List<IRow> getParts() {
		return parts;
	}

	public void setParts(List<IRow> parts) {
		this.parts = parts;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {
				continue;
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

		if (changedFields.size() > 0) {
			return true;
		} else {
			return false;
		}


	}

	@Override
	public int mesh() {
		return this.meshId;
	}

	@Override
	public void setMesh(int mesh) {
		this.meshId= mesh;
		
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		if (objLevel == ObjLevel.FULL || objLevel == ObjLevel.HISTORY) {

			JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());

			return json;
		}
		else if (objLevel == ObjLevel.BRIEF) {
			JSONObject json = new JSONObject();
			
			json.put("groupId", pid);
			
			json.put("regionIdUp", regionIdUp);
			
			json.put("rowId", rowId);

			return json;
		}
		return null;
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

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	@Override
	public String primaryKey() {
		// TODO Auto-generated method stub
		return "region_id";
	}

}