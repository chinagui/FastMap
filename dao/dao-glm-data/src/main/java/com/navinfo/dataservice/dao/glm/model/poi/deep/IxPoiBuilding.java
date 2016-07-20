package com.navinfo.dataservice.dao.glm.model.poi.deep;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

/**
 * 索引:POI 深度信息(建筑物和租户的楼层信息)
 * 
 * @author zhaokk
 *
 */
public class IxPoiBuilding implements IRow {

	private int poiPid = 0;
	private String floorUsed;// 大厦或租户可用的楼层
	private String floorEmpty;// 大厦或租户空缺的楼层
	private String rowId;
	// 更新时间
	private String uDate;
	
	public String getuDate() {
		return uDate;
	}

	public void setuDate(String uDate) {
		this.uDate = uDate;
	}

	public int getPoiPid() {
		return poiPid;
	}

	public void setPoiPid(int poiPid) {
		this.poiPid = poiPid;
	}

	public String getFloorUsed() {
		return floorUsed;
	}

	public void setFloorUsed(String floorUsed) {
		this.floorUsed = floorUsed;
	}

	public String getFloorEmpty() {
		return floorEmpty;
	}

	public void setFloorEmpty(String floorEmpty) {
		this.floorEmpty = floorEmpty;
	}

	public String getRowId() {
		return rowId;
	}

	private String memo;
	private Map<String, Object> changedFields = new HashMap<String, Object>();

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
		return "ix_poi_building";
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
		return ObjType.IXPOIBUILDING;
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
		return "pid";
	}

	@Override
	public int parentPKValue() {
		// TODO Auto-generated method stub
		return this.getPoiPid();
	}

	@Override
	public String parentTableName() {
		// TODO Auto-generated method stub
		return "ix_poi";
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Map<String, Object> getChangedFields() {
		return changedFields;
	}

	public void setChangedFields(Map<String, Object> changedFields) {
		this.changedFields = changedFields;
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
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		return JSONObject.fromObject(this, JsonUtils.getStrConfig());
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
	public int mesh() {
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
	}

}
