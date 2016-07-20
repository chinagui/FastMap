package com.navinfo.dataservice.dao.glm.model.rd.eleceye;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdEleceyePart implements IRow {

	private int groupId;

	private int eleceyePid;

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		return JSONObject.fromObject(this, JsonUtils.getStrConfig());
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		Iterator keys = json.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			Field f = this.getClass().getDeclaredField(key);
			f.setAccessible(true);
			f.set(this, json.get(key));
		}
		return true;
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
		return "rd_eleceye_part";
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
		return ObjType.RDELECEYEPART;
	}

	@Override
	public void copy(IRow row) {
		RdEleceyePart source = (RdEleceyePart) row;
		this.groupId = source.groupId;
		this.eleceyePid = source.eleceyePid;
	}

	@Override
	public Map<String, Object> changedFields() {
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		return "group_id";
	}

	@Override
	public int parentPKValue() {
		return this.groupId;
	}

	@Override
	public String parentTableName() {
		return "rd_eleceye_pair";
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

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getEleceyePid() {
		return eleceyePid;
	}

	public void setEleceyePid(int eleceyePid) {
		this.eleceyePid = eleceyePid;
	}

	public String getRowId() {
		return rowId;
	}

}
