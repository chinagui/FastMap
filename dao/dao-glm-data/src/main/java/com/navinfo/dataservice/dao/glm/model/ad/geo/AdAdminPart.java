package com.navinfo.dataservice.dao.glm.model.ad.geo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.util.StringUtils;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

public class AdAdminPart implements IRow {

	private int groupId;
	private int regionIdDown;
    private String rowId;
    protected String objType;
    public String getRowId() {
		return rowId;
	}

	private Map<String, Object> changedFields = new HashMap<String, Object>();
	@Override
	public String rowId() {
		return this.getRowId();
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
		
	}
	
	public String getObjType() {
		return objType;
	}

	public void setObjType(String objType) {
		this.objType = objType;
	}

	@Override
	public String tableName() {
		return "ad_admin_part";
	}

	@Override
	public ObjStatus status() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatus(ObjStatus os) {
		
	}

	@Override
	public ObjType objType() {
		return ObjType.ADADMINGPART;
	}

	@Override
	public void copy(IRow row) {
		
	}

	@Override
	public Map<String, Object> changedFields() {
		// TODO Auto-generated method stub
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		// TODO Auto-generated method stub
		return "group_id";
	}

	@Override
	public int parentPKValue() {
		// TODO Auto-generated method stub
		return this.getGroupId();
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getRegionIdDown() {
		return regionIdDown;
	}

	public void setRegionIdDown(int regionIdDown) {
		this.regionIdDown = regionIdDown;
	}

	@Override
	public String parentTableName() {
		// TODO Auto-generated method stub
		return "ad_admin_group";
	}
	@Override
	public List<List<IRow>> children() {
		return null;
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
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		if (objLevel == ObjLevel.FULL || objLevel == ObjLevel.HISTORY) {

			JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());

			return json;
		}
		else if (objLevel == ObjLevel.BRIEF) {
			JSONObject json = new JSONObject();

			json.put("groupId", groupId);
			
			json.put("regionIdDown", regionIdDown);
			
			json.put("rowId", rowId);
			
			if(!StringUtils.isEmpty(objType))
			{
				json.put("objType", objType);
			}

			return json;
		}
		return null;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		return false;
	}

}
