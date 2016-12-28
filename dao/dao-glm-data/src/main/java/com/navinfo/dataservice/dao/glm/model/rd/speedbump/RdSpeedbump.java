package com.navinfo.dataservice.dao.glm.model.rd.speedbump;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @Title: RdSpeedbump.java
 * @Description: 减速带模型
 * @author zhangyt
 * @date: 2016年8月5日 下午1:33:30
 * @version: v1.0
 */
public class RdSpeedbump implements IObj {

	public RdSpeedbump() {
	}

	private int pid;

	private String rowId;

	private int linkPid;

	private int nodePid;

	private String memo;

	private String reserved;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getRowId() {
		return rowId;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getReserved() {
		return reserved;
	}

	public void setReserved(String reserved) {
		this.reserved = reserved;
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
		return "rd_speedbump";
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
		return ObjType.RDSPEEDBUMP;
	}

	@Override
	public void copy(IRow row) {
		RdSpeedbump source = new RdSpeedbump();
		this.pid = source.pid;
		this.rowId = source.rowId;
		this.linkPid = source.linkPid;
		this.nodePid = source.nodePid;
		this.memo = source.memo;
		this.reserved = source.reserved;
	}

	@Override
	public Map<String, Object> changedFields() {
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		return "bump_pid";
	}

	@Override
	public int parentPKValue() {
		return this.pid;
	}

	@Override
	public String parentTableName() {
		return "rd_speedbump";
	}

	@Override
	public List<List<IRow>> children() {
		return null;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		Iterator<?> keys = json.keys();
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
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		return JSONObject.fromObject(this);
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		@SuppressWarnings("rawtypes")
		Iterator keys = json.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (!"objStatus".equals(key)) {
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
	public int pid() {
		return this.pid;
	}

	@Override
	public String primaryKey() {
		return "bump_pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		return null;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		return null;
	}

}
