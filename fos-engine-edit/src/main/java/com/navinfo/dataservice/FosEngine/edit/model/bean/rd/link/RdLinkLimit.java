package com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.comm.util.JsonUtils;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.ObjType;

public class RdLinkLimit implements IRow {

	private String rowId;

	private int type = 3;

	private int limitDir;

	private String timeDomain;

	private int linkPid;
	
	private int vehicle;
	
	public int getVehicle() {
		return vehicle;
	}

	public void setVehicle(int vehicle) {
		this.vehicle = vehicle;
	}

	public int getTollType() {
		return tollType;
	}

	public void setTollType(int tollType) {
		this.tollType = tollType;
	}

	public int getWeather() {
		return weather;
	}

	public void setWeather(int weather) {
		this.weather = weather;
	}

	public String getInputTime() {
		return inputTime;
	}

	public void setInputTime(String inputTime) {
		this.inputTime = inputTime;
	}

	public int getProcessFlag() {
		return processFlag;
	}

	public void setProcessFlag(int processFlag) {
		this.processFlag = processFlag;
	}

	private int tollType;
	
	private int weather;
	
	private String inputTime;
	
	private int processFlag;
	
	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public RdLinkLimit() {

	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLimitDir() {
		return limitDir;
	}

	public void setLimitDir(int limitDir) {
		this.limitDir = limitDir;
	}

	public String getTimeDomain() {
		return timeDomain;
	}

	public void setTimeDomain(String timeDomain) {
		this.timeDomain = timeDomain;
	}

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {

		return JSONObject.fromObject(this,JsonUtils.getStrConfig());
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

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
	public String tableName() {

		return "rd_link_limit";
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

		return ObjType.RDLINKLIMIT;
	}

	@Override
	public void copy(IRow row) {

		RdLinkLimit sourceLimit = (RdLinkLimit) row;

		this.setLimitDir(sourceLimit.getLimitDir());

		this.setRowId(sourceLimit.getRowId());

		this.setTimeDomain(sourceLimit.getTimeDomain());

		this.setType(sourceLimit.getType());
	}

	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public String primaryKey() {

		return "link_pid";
	}

	@Override
	public int primaryValue() {

		return this.getLinkPid();
	}

	@Override
	public List<List<IRow>> children() {

		return null;
	}

	@Override
	public String primaryTableName() {

		return "rd_link";
	}

	@Override
	public String rowId() {

		return rowId;
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
						changedFields.put(key, json.get(key));

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

}
