package com.navinfo.dataservice.dao.glm.model.poi.deep;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

/**
 * 索引:POI 深度信息(充电站类)
 * 
 * @author zhaokk
 *
 */
public class IxPoiChargingStation implements IObj {

	private int pid;
	private int poiPid = 0;
	private int chargingType = 3;// 充电站类型
	private String changeBrands;// CHANGE_BRANDS
	private String changeOpenType;// CHANGE_OPEN_TYPE
	private int chargingNum = 0;// 充电桩总数
	private String serviceProv;// 服务提供商
	private String memo;// 备注信息
	private String openHour;// 营业时间
	private String photoName;// 全景照片
	private int parkingFees = 0;// 停车收费
	private String parkingInfo;// 停 车 收 费 备注
	private int availableState = 0;// 可用状态
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

	public String getRowId() {
		return rowId;
	}

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
		return "ix_poi_chargingstation";
	}

	@Override
	public ObjStatus status() {
		return null;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	@Override
	public void setStatus(ObjStatus os) {

	}

	@Override
	public ObjType objType() {
		return ObjType.IXPOICHARGINGSTATION;
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

	public int getChargingType() {
		return chargingType;
	}

	public void setChargingType(int chargingType) {
		this.chargingType = chargingType;
	}

	public String getChangeBrands() {
		return changeBrands;
	}

	public void setChangeBrands(String changeBrands) {
		this.changeBrands = changeBrands;
	}

	public String getChangeOpenType() {
		return changeOpenType;
	}

	public void setChangeOpenType(String changeOpenType) {
		this.changeOpenType = changeOpenType;
	}

	public int getChargingNum() {
		return chargingNum;
	}

	public void setChargingNum(int chargingNum) {
		this.chargingNum = chargingNum;
	}

	public int getParkingFees() {
		return parkingFees;
	}

	public void setParkingFees(int parkingFees) {
		this.parkingFees = parkingFees;
	}

	public String getParkingInfo() {
		return parkingInfo;
	}

	public void setParkingInfo(String parkingInfo) {
		this.parkingInfo = parkingInfo;
	}

	public int getAvailableState() {
		return availableState;
	}

	public void setAvailableState(int availableState) {
		this.availableState = availableState;
	}

	@Override
	public List<List<IRow>> children() {
		return null;
	}

	public String getServiceProv() {
		return serviceProv;
	}

	public void setServiceProv(String serviceProv) {
		this.serviceProv = serviceProv;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getOpenHour() {
		return openHour;
	}

	public void setOpenHour(String openHour) {
		this.openHour = openHour;
	}

	public String getPhotoName() {
		return photoName;
	}

	public void setPhotoName(String photoName) {
		this.photoName = photoName;
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

	@Override
	public List<IRow> relatedRows() {
		return null;
	}

	@Override
	public int pid() {
		return this.getPid();
	}

	@Override
	public String primaryKey() {
		return "charging_id";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		return null;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		return null;
	}

}
