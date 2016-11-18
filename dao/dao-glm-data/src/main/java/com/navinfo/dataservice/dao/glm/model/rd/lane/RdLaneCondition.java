package com.navinfo.dataservice.dao.glm.model.rd.lane;

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

/**
 * 详细车道的时间段和车辆限制表
 * 
 * @author zhaokaikai
 *
 */
public class RdLaneCondition implements IRow {

	private int lanePid; // 车道号码
	/*
	 * ①当车道为潮汐车道时，记录某时间段内车道的 通行方向，如果与 LINK 画线方向相同为顺方 向，反之逆方向
	 */
	private int direction = 1;// 车道方向1 无 2 顺方向3 逆方向
	private String directionTime;// 方向时间段

	private String rowId;

	private long vehicle = 0;// 车辆类型

	private String vehicleTime;// 车辆时间段

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public String getRowId() {
		return rowId;
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
	public String rowId() {
		return this.rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	@Override
	public String tableName() {
		return "rd_lane_condition";
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
		return ObjType.RDLANECONDITION;
	}

	@Override
	public void copy(IRow row) {
		RdLaneCondition condition = (RdLaneCondition)row;
		this.setDirection(condition.getDirection());
		this.setDirectionTime(condition.getDirectionTime());
		this.setVehicle(condition.getVehicle());
		this.setVehicleTime(condition.getVehicleTime());
	}

	@Override
	public Map<String, Object> changedFields() {
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		return "lane_pid";
	}

	@Override
	public int parentPKValue() {
		return this.getLanePid();
	}

	public int getLanePid() {
		return lanePid;
	}

	public void setLanePid(int lanePid) {
		this.lanePid = lanePid;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public String getDirectionTime() {
		return directionTime;
	}

	public void setDirectionTime(String directionTime) {
		this.directionTime = directionTime;
	}

	public long getVehicle() {
		return vehicle;
	}

	public void setVehicle(long vehicle) {
		this.vehicle = vehicle;
	}

	public String getVehicleTime() {
		return vehicleTime;
	}

	public void setVehicleTime(String vehicleTime) {
		this.vehicleTime = vehicleTime;
	}

	@Override
	public String parentTableName() {
		return "rd_lane";
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

}
