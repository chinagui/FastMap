/**
 * 
 */
package com.navinfo.dataservice.dao.glm.model.rd.variablespeed;

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
 * @ClassName: RdVariableSpeed
 * @author Zhang Xiaolong
 * @date 2016年8月15日 下午5:08:47
 * @Description: TODO
 */
public class RdVariableSpeed implements IObj {

	private int pid;

	private int inLinkPid;

	private int nodePid;

	private int outLinkPid;

	private int location = 2;

	private int speedValue;

	private int speedType;

	private int speedDependent;

	private String timeDomain;

	private long vehicle = 0;

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public Map<String, RdVariableSpeedVia> viaMap = new HashMap<String, RdVariableSpeedVia>();

	private List<IRow> vias = new ArrayList<IRow>();

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

	public int getOutLinkPid() {
		return outLinkPid;
	}

	public void setOutLinkPid(int outLinkPid) {
		this.outLinkPid = outLinkPid;
	}

	public int getSpeedValue() {
		return speedValue;
	}

	public void setSpeedValue(int speedValue) {
		this.speedValue = speedValue;
	}

	public int getSpeedType() {
		return speedType;
	}

	public void setSpeedType(int speedType) {
		this.speedType = speedType;
	}

	public int getSpeedDependent() {
		return speedDependent;
	}

	public void setSpeedDependent(int speedDependent) {
		this.speedDependent = speedDependent;
	}

	public String getTimeDomain() {
		return timeDomain;
	}

	public void setTimeDomain(String timeDomain) {
		this.timeDomain = timeDomain;
	}

	public long getVehicle() {
		return vehicle;
	}

	public void setVehicle(long vehicle) {
		this.vehicle = vehicle;
	}

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int location) {
		this.location = location;
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
		return "rd_variable_speed";
	}

	@Override
	public ObjStatus status() {
		return null;
	}

	public List<IRow> getVias() {
		return vias;
	}

	public void setVias(List<IRow> vias) {
		this.vias = vias;
	}

	@Override
	public void setStatus(ObjStatus os) {
	}

	@Override
	public ObjType objType() {
		return ObjType.RDVARIABLESPEED;
	}

	@Override
	public void copy(IRow row) {
		RdVariableSpeed variableSpeed = (RdVariableSpeed) row;
		this.setInLinkPid(variableSpeed.getInLinkPid());
		this.setOutLinkPid(variableSpeed.getOutLinkPid());
		this.setSpeedDependent(variableSpeed.getSpeedDependent());
		this.setSpeedType(variableSpeed.getSpeedType());
		this.setSpeedValue(variableSpeed.getSpeedValue());
		this.setLocation(variableSpeed.getLocation());
		this.setNodePid(variableSpeed.getNodePid());
		this.setRowId(variableSpeed.getRowId());
		this.setPid(variableSpeed.getPid());
	}

	@Override
	public Map<String, Object> changedFields() {
		return changedFields;
	}

	@Override
	public String parentPKName() {
		return "vspeed_pid";
	}

	@Override
	public int parentPKValue() {
		return this.pid;
	}

	@Override
	public String parentTableName() {
		return "rd_variable_speed";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();
		children.add(this.getVias());
		return children;
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
		JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());

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
		return "vspeed_pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>, List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdVariableSpeedVia.class, vias);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		Map<Class<? extends IRow>, Map<String, ?>> childMap = new HashMap<Class<? extends IRow>, Map<String, ?>>();
		childMap.put(RdVariableSpeedVia.class, viaMap);
		return childMap;
	}

}
