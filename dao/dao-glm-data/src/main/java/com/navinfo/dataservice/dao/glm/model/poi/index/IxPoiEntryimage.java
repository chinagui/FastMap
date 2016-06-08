package com.navinfo.dataservice.dao.glm.model.poi.index;

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

public class IxPoiEntryimage implements IRow {

	private int poiPid;//POI号码

	private String imageCode;//概略图号码

	private int xPixelR4=0;//R4像素坐标X

	private int yPixelR4=0;//R4像素坐标Y

	private int xPixelR5=0;//R5像素坐标X

	private int yPixelR5=0;//R5像素坐标Y
	
	private int xPixel35=0;//35像素坐标X

	private int yPixel35=0;//35像素坐标Y
	
	private String memo;
	
	private int mainPoiPid=0;//主点POI号码
	
	private String rowId;
	
	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public int getPoiPid() {
		return poiPid;
	}

	public void setPoiPid(int poiPid) {
		this.poiPid = poiPid;
	}

	public String getImageCode() {
		return imageCode;
	}

	public void setImageCode(String imageCode) {
		this.imageCode = imageCode;
	}

	public int getxPixelR4() {
		return xPixelR4;
	}

	public void setxPixelR4(int xPixelR4) {
		this.xPixelR4 = xPixelR4;
	}

	public int getyPixelR4() {
		return yPixelR4;
	}

	public void setyPixelR4(int yPixelR4) {
		this.yPixelR4 = yPixelR4;
	}

	public int getxPixelR5() {
		return xPixelR5;
	}

	public void setxPixelR5(int xPixelR5) {
		this.xPixelR5 = xPixelR5;
	}

	public int getyPixelR5() {
		return yPixelR5;
	}

	public void setyPixelR5(int yPixelR5) {
		this.yPixelR5 = yPixelR5;
	}

	public int getxPixel35() {
		return xPixel35;
	}

	public void setxPixel35(int xPixel35) {
		this.xPixel35 = xPixel35;
	}

	public int getyPixel35() {
		return yPixel35;
	}

	public void setyPixel35(int yPixel35) {
		this.yPixel35 = yPixel35;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public int getMainPoiPid() {
		return mainPoiPid;
	}

	public void setMainPoiPid(int mainPoiPid) {
		this.mainPoiPid = mainPoiPid;
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
		return "ix_poi_entryimage";
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
		return ObjType.IXPOIENTRYIMAGE;
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
		return "pid";
	}

	@Override
	public int parentPKValue() {
		return this.poiPid;
	}

	@Override
	public String parentTableName() {
		return "ix_poi";
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
