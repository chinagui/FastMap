package com.navinfo.dataservice.dao.glm.model.rd.eleceye;

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
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossName;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdEleceyePair implements IObj {

	private int pid;

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	private List<IRow> parts = new ArrayList<IRow>();

	public Map<String, RdEleceyePart> partMap = new HashMap<String, RdEleceyePart>();

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		return JSONObject.fromObject(this, JsonUtils.getStrConfig());
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		Iterator keys = json.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			JSONArray ja = null;
			if (json.get(key) instanceof JSONArray) {
				switch (key) {
				case "parts":
					parts.clear();
					ja = json.getJSONArray(key);
					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);
						RdEleceyePart row = new RdEleceyePart();
						row.Unserialize(jo);
						parts.add(row);
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
	public ObjType objType() {
		return ObjType.RDELECEYEPAIR;
	}

	@Override
	public void copy(IRow row) {
		RdEleceyePair source = (RdEleceyePair) row;
		this.pid = source.pid;
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
		return "rd_eleceye_pair";
	}

	@Override
	public ObjStatus status() {
		return null;
	}

	@Override
	public void setStatus(ObjStatus os) {
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
		return this.pid;
	}

	@Override
	public String parentTableName() {
		return "rd_eleceye_pair";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();
		children.add(this.parts);
		return children;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		Iterator keys = json.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
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
		return "group_id";
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

	public List<IRow> getParts() {
		return parts;
	}

	public void setParts(List<IRow> parts) {
		this.parts = parts;
	}

}
