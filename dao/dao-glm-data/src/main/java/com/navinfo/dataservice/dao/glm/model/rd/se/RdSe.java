package com.navinfo.dataservice.dao.glm.model.rd.se;

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

/**
 * @Title: RdSe.java
 * @Description: 分叉口提示
 * @author zhangyt
 * @date: 2016年8月1日 上午10:41:03
 * @version: v1.0
 */
public class RdSe implements IObj, Cloneable {

	public RdSe() {
	}

	private int pid;

	private String rowId;

	private int inLinkPid;

	private int nodePid;

	private int outLinkPid;
	
	protected ObjStatus status;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getInLinkPid() {
		return inLinkPid;
	}

	public void setInLinkPid(int inLinkPid) {
		this.inLinkPid = inLinkPid;
	}

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public int getOutLinkPid() {
		return outLinkPid;
	}

	public void setOutLinkPid(int outLinkPid) {
		this.outLinkPid = outLinkPid;
	}

	public String getRowId() {
		return rowId;
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
		return "rd_se";
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
		return ObjType.RDSE;
	}

	@Override
	public void copy(IRow row) {
		RdSe source = (RdSe) row;
		this.pid = source.pid;
		this.rowId = source.rowId;
		this.inLinkPid = source.inLinkPid;
		this.nodePid = source.nodePid;
		this.outLinkPid = source.outLinkPid;
	}

	@Override
	public Map<String, Object> changedFields() {
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		return "pid";
	}

	@Override
	public int parentPKValue() {
		return this.pid;
	}

	@Override
	public String parentTableName() {
		return "rd_se";
	}

	@Override
	public List<List<IRow>> children() {
		return new ArrayList<List<IRow>>();
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		@SuppressWarnings("rawtypes")
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
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		//JSONObject.fromObject(this, JsonUtils.getStrConfig());
		JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());
		if (objLevel == ObjLevel.HISTORY) {
			json.remove("status");
		}
		return json;
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
		return "pid";
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
