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
 * 索引:POI 深度信息(充电桩类)
 * 
 * @author zhaokk
 *
 */
public class IxPoiChargingPlot implements IRow {

	private int poiPid = 0;
	private int groupId = 1;// 充电桩分组号
	private int count = 1;// 组内充电桩数
	private int acdc = 0;// 交直流电充电
	private String plugType = "9";// 插座类型
	private String power; // 充电功率
	private String voltage;// 充电电压
	private String current;// 充电电流
	private int mode = 0;// 充电模式
	private int plugNum = 1;// 单个充电桩插数量
	private String prices;// 充电价格
	private String openType = "1";// 开放状态
	private int availableState = 0;// 可用状态
	private String manufacturer;// 设备生产商
	private String factoryNum;// 出厂编号
	private String plotNum;// 充电桩编号
	private String productNum;// 产品型号
	private String parkingNum;// 电动车泊位号码
	private int floor = 1;// 楼层
	private int locationType = 0;// 充电桩位置类型
	private String payment = "4";// 支付方式
	private String memo;
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
		return "ix_poi_chargingplot";
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getAcdc() {
		return acdc;
	}

	public void setAcdc(int acdc) {
		this.acdc = acdc;
	}

	public String getPlugType() {
		return plugType;
	}

	public void setPlugType(String plugType) {
		this.plugType = plugType;
	}

	public String getPower() {
		return power;
	}

	public void setPower(String power) {
		this.power = power;
	}

	public String getVoltage() {
		return voltage;
	}

	public void setVoltage(String voltage) {
		this.voltage = voltage;
	}

	public String getCurrent() {
		return current;
	}

	public void setCurrent(String current) {
		this.current = current;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getPlugNum() {
		return plugNum;
	}

	public void setPlugNum(int plugNum) {
		this.plugNum = plugNum;
	}

	public String getPrices() {
		return prices;
	}

	public void setPrices(String prices) {
		this.prices = prices;
	}

	public String getOpenType() {
		return openType;
	}

	public void setOpenType(String openType) {
		this.openType = openType;
	}

	public int getAvailableState() {
		return availableState;
	}

	public void setAvailableState(int availableState) {
		this.availableState = availableState;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getFactoryNum() {
		return factoryNum;
	}

	public void setFactoryNum(String factoryNum) {
		this.factoryNum = factoryNum;
	}

	public String getPlotNum() {
		return plotNum;
	}

	public void setPlotNum(String plotNum) {
		this.plotNum = plotNum;
	}

	public String getProductNum() {
		return productNum;
	}

	public void setProductNum(String productNum) {
		this.productNum = productNum;
	}

	public String getParkingNum() {
		return parkingNum;
	}

	public void setParkingNum(String parkingNum) {
		this.parkingNum = parkingNum;
	}

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;
	}

	public int getLocationType() {
		return locationType;
	}

	public void setLocationType(int locationType) {
		this.locationType = locationType;
	}

	public String getPayment() {
		return payment;
	}

	public void setPayment(String payment) {
		this.payment = payment;
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
		return ObjType.IXPOICHARGINGPLOT;
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
