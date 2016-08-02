package com.navinfo.dataservice.dao.glm.model.poi.index;

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
 * POI名称语音语调表
 * 
 * @author zhangxiaolong
 *
 */
public class IxPoiNameTone implements IRow {

	private int nameId;

	private String toneA;// 带音调拼音一

	private String toneB;// 带音调拼音二

	private String lhA;// LH拼音一

	private String lhB;// LH拼音二

	private String jyutp;// 粤语拼音

	private String memo;// 备注信息

	private String rowId;

	// 更新时间
	private String uDate;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public int getNameId() {
		return nameId;
	}
	
	public String getuDate() {
		return uDate;
	}

	public void setuDate(String uDate) {
		this.uDate = uDate;
	}

	public void setNameId(int nameId) {
		this.nameId = nameId;
	}

	public String getToneA() {
		return toneA;
	}

	public void setToneA(String toneA) {
		this.toneA = toneA;
	}

	public String getToneB() {
		return toneB;
	}

	public void setToneB(String toneB) {
		this.toneB = toneB;
	}

	public String getLhA() {
		return lhA;
	}

	public void setLhA(String lhA) {
		this.lhA = lhA;
	}

	public String getLhB() {
		return lhB;
	}

	public void setLhB(String lhB) {
		this.lhB = lhB;
	}

	public String getJyutp() {
		return jyutp;
	}

	public void setJyutp(String jyutp) {
		this.jyutp = jyutp;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

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
		return "ix_poi_name_tone";
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
		return ObjType.IXPOINAMETONE;
	}

	@Override
	public void copy(IRow row) {
	}

	@Override
	public Map<String, Object> changedFields() {
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		return "name_id";
	}

	@Override
	public int parentPKValue() {
		return this.nameId;
	}

	@Override
	public String parentTableName() {
		return "ix_poi_name";
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
