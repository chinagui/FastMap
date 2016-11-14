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
 * 索引:POI 深度信息(开放或营业时间)
 * 
 * @author zhaokk
 *
 */
public class IxPoiBusinessTime implements IRow {

	private int poiPid = 0;
	private String monSrt;// 营业开始月份
	private String monEnd;// 营业结束月份
	private String weekInYearSrt;// 一年中的起始周
	private String weekInYearEnd;// 一年中的结束周
	private String weekInMonthSrt;// 指定月份的营业起始周
	private String weekInMonthEnd;// 指定月份的营业结束周
	private String validWeek;// 周营业日
	private String daySrt;// 月营业起始日
	private String dayEnd;// 月营业结束日
	private String timeSrt;// 营业开始日期
	private String timeDur;// 营业时长
	private String reserved;// 预留字段
	private int uRecord=0;
	
	public int getuRecord(){
		return uRecord;
	}
	
	public void setuRecord(int uRecord){
		this.uRecord = uRecord;
	}
	
	// 更新时间
	private String uDate;

	public String getuDate() {
		return uDate;
	}

	public void setuDate(String uDate) {
		this.uDate = uDate;
	}

	public String getMonSrt() {
		return monSrt;
	}

	public void setMonSrt(String monSrt) {
		this.monSrt = monSrt;
	}

	public String getMonEnd() {
		return monEnd;
	}

	public void setMonEnd(String monEnd) {
		this.monEnd = monEnd;
	}

	public String getWeekInYearSrt() {
		return weekInYearSrt;
	}

	public void setWeekInYearSrt(String weekInYearSrt) {
		this.weekInYearSrt = weekInYearSrt;
	}

	public String getWeekInYearEnd() {
		return weekInYearEnd;
	}

	public void setWeekInYearEnd(String weekInYearEnd) {
		this.weekInYearEnd = weekInYearEnd;
	}

	public String getWeekInMonthSrt() {
		return weekInMonthSrt;
	}

	public void setWeekInMonthSrt(String weekInMonthSrt) {
		this.weekInMonthSrt = weekInMonthSrt;
	}

	public String getWeekInMonthEnd() {
		return weekInMonthEnd;
	}

	public void setWeekInMonthEnd(String weekInMonthEnd) {
		this.weekInMonthEnd = weekInMonthEnd;
	}

	public String getValidWeek() {
		return validWeek;
	}

	public void setValidWeek(String validWeek) {
		this.validWeek = validWeek;
	}

	public String getDaySrt() {
		return daySrt;
	}

	public void setDaySrt(String daySrt) {
		this.daySrt = daySrt;
	}

	public String getDayEnd() {
		return dayEnd;
	}

	public void setDayEnd(String dayEnd) {
		this.dayEnd = dayEnd;
	}

	public String getTimeSrt() {
		return timeSrt;
	}

	public void setTimeSrt(String timeSrt) {
		this.timeSrt = timeSrt;
	}

	public String getTimeDur() {
		return timeDur;
	}

	public void setTimeDur(String timeDur) {
		this.timeDur = timeDur;
	}

	public String getReserved() {
		return reserved;
	}

	public void setReserved(String reserved) {
		this.reserved = reserved;
	}

	private String rowId;

	public int getPoiPid() {
		return poiPid;
	}

	public void setPoiPid(int poiPid) {
		this.poiPid = poiPid;
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
		return "ix_poi_businesstime";
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
		return ObjType.IXPOIBUSINESSTIME;
	}

	@Override
	public void copy(IRow row) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Object> changedFields() {
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		return "poi_pid";
	}

	@Override
	public int parentPKValue() {
		return this.getPoiPid();
	}

	@Override
	public String parentTableName() {
		return "ix_poi";
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
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
