package com.navinfo.dataservice.dao.glm.model.poi.index;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

/**
 * POI地址表
 * @author zhangxiaolong
 *
 */
public class IxPoiAddress implements IObj {
	private Logger logger = Logger.getLogger(IxPoiAddress.class);

	private int pid;
	
	private int poiPid;//POI号码
	
	private int nameGroupid=1;//名称组号
	
	private String langCode;//语言代码
	
	private int srcFlag=0;//名称来源
	
	private String fullname;//地址全称
	
	private String roadname;//地址道路名
	
	private String addrname;//地址门牌号
	
	private String province;//省名
	
	private String city;//市名
	
	private String county;//区县名
	
	private String town;//乡镇街道办
	
	private String place;//地名小区名
	
	private String street;//街巷名
	
	private String landmark;//标志物名
	
	private String prefix;//前缀
	
	private String housenum;//门牌号
	
	private String type;//类型名
	
	private String subnum;//子号
	
	private String surfix;//后缀
	
	private String estab;//附属设施名
	
	private String building;//楼栋号
	
	private String floor;//楼层
	
	private String unit;//楼门号
	
	private String room;//房间号
	
	private String addons;//附加信息
	
	private String fullnamePhonetic;//地址全称发音
	
	private String roadnamePhonetic;//地址道路名发音
	
	private String addrnamePhonetic;//地址门牌号发音
	
	private String provPhonetic;//省名发音
	
	private String cityPhonetic;//市名发音
	
	private String countyPhonetic;//区县名发音
	
	private String townPhonetic;//乡镇街道办发音
	
	private String streetPhonetic;//街巷名发音
	
	private String placePhonetic;//地名小区名发音
	
	private String landmarkPhonetic;//标志物名发音
	
	private String prefixPhonetic;//前缀名发音
	
	private String housenumPhonetic;//门牌号发音
	
	private String typePhonetic;//类型名发音
	
	private String subnumPhonetic;//子号发音
	
	private String surfixPhonetic;//后缀名发音
	
	private String estabPhonetic;//附属设施名发音
	
	private String buildingPhonetic;//楼栋号发音
	
	private String floorPhonetic;//楼层发音
	
	private String unitPhonetic;//楼门号发音
	
	private String roomPhonetic;//房间号发音
	
	private String addonsPhonetic;//附加信息发音
	
	private String rowId;
	
	private int uRecord=0;
	
	private String uDate;
	
	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getPoiPid() {
		return poiPid;
	}

	public void setPoiPid(int poiPid) {
		this.poiPid = poiPid;
	}

	public int getNameGroupid() {
		return nameGroupid;
	}

	public void setNameGroupid(int nameGroupid) {
		this.nameGroupid = nameGroupid;
	}

	public String getLangCode() {
		return langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	public int getSrcFlag() {
		return srcFlag;
	}

	public void setSrcFlag(int srcFlag) {
		this.srcFlag = srcFlag;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getRoadname() {
		return roadname;
	}

	public void setRoadname(String roadname) {
		this.roadname = roadname;
	}

	public String getAddrname() {
		return addrname;
	}

	public void setAddrname(String addrname) {
		this.addrname = addrname;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getTown() {
		return town;
	}

	public void setTown(String town) {
		this.town = town;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getLandmark() {
		return landmark;
	}

	public void setLandmark(String landmark) {
		this.landmark = landmark;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubnum() {
		return subnum;
	}

	public void setSubnum(String subnum) {
		this.subnum = subnum;
	}

	public String getSurfix() {
		return surfix;
	}

	public void setSurfix(String surfix) {
		this.surfix = surfix;
	}

	public String getEstab() {
		return estab;
	}

	public void setEstab(String estab) {
		this.estab = estab;
	}

	public String getBuilding() {
		return building;
	}

	public void setBuilding(String building) {
		this.building = building;
	}

	public String getFloor() {
		return floor;
	}

	public void setFloor(String floor) {
		this.floor = floor;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getAddons() {
		return addons;
	}

	public void setAddons(String addons) {
		this.addons = addons;
	}

	public String getFullnamePhonetic() {
		return fullnamePhonetic;
	}

	public void setFullnamePhonetic(String fullnamePhonetic) {
		this.fullnamePhonetic = fullnamePhonetic;
	}

	public String getRoadnamePhonetic() {
		return roadnamePhonetic;
	}

	public void setRoadnamePhonetic(String roadnamePhonetic) {
		this.roadnamePhonetic = roadnamePhonetic;
	}

	public String getAddrnamePhonetic() {
		return addrnamePhonetic;
	}

	public void setAddrnamePhonetic(String addrnamePhonetic) {
		this.addrnamePhonetic = addrnamePhonetic;
	}

	public String getProvPhonetic() {
		return provPhonetic;
	}

	public void setProvPhonetic(String provPhonetic) {
		this.provPhonetic = provPhonetic;
	}

	public String getCityPhonetic() {
		return cityPhonetic;
	}

	public void setCityPhonetic(String cityPhonetic) {
		this.cityPhonetic = cityPhonetic;
	}

	public String getCountyPhonetic() {
		return countyPhonetic;
	}

	public void setCountyPhonetic(String countyPhonetic) {
		this.countyPhonetic = countyPhonetic;
	}

	public String getTownPhonetic() {
		return townPhonetic;
	}

	public void setTownPhonetic(String townPhonetic) {
		this.townPhonetic = townPhonetic;
	}

	public String getStreetPhonetic() {
		return streetPhonetic;
	}

	public void setStreetPhonetic(String streetPhonetic) {
		this.streetPhonetic = streetPhonetic;
	}

	public String getPlacePhonetic() {
		return placePhonetic;
	}

	public void setPlacePhonetic(String placePhonetic) {
		this.placePhonetic = placePhonetic;
	}

	public String getLandmarkPhonetic() {
		return landmarkPhonetic;
	}

	public void setLandmarkPhonetic(String landmarkPhonetic) {
		this.landmarkPhonetic = landmarkPhonetic;
	}

	public String getPrefixPhonetic() {
		return prefixPhonetic;
	}

	public void setPrefixPhonetic(String prefixPhonetic) {
		this.prefixPhonetic = prefixPhonetic;
	}

	public String getHousenumPhonetic() {
		return housenumPhonetic;
	}

	public void setHousenumPhonetic(String housenumPhonetic) {
		this.housenumPhonetic = housenumPhonetic;
	}

	public String getTypePhonetic() {
		return typePhonetic;
	}

	public void setTypePhonetic(String typePhonetic) {
		this.typePhonetic = typePhonetic;
	}

	public String getHousenum() {
		return housenum;
	}

	public void setHousenum(String housenum) {
		this.housenum = housenum;
	}

	public String getSubnumPhonetic() {
		return subnumPhonetic;
	}

	public void setSubnumPhonetic(String subnumPhonetic) {
		this.subnumPhonetic = subnumPhonetic;
	}

	public String getSurfixPhonetic() {
		return surfixPhonetic;
	}

	public void setSurfixPhonetic(String surfixPhonetic) {
		this.surfixPhonetic = surfixPhonetic;
	}

	public String getEstabPhonetic() {
		return estabPhonetic;
	}

	public void setEstabPhonetic(String estabPhonetic) {
		this.estabPhonetic = estabPhonetic;
	}

	public String getBuildingPhonetic() {
		return buildingPhonetic;
	}

	public void setBuildingPhonetic(String buildingPhonetic) {
		this.buildingPhonetic = buildingPhonetic;
	}

	public String getFloorPhonetic() {
		return floorPhonetic;
	}

	public void setFloorPhonetic(String floorPhonetic) {
		this.floorPhonetic = floorPhonetic;
	}

	public String getUnitPhonetic() {
		return unitPhonetic;
	}

	public void setUnitPhonetic(String unitPhonetic) {
		this.unitPhonetic = unitPhonetic;
	}

	public String getRoomPhonetic() {
		return roomPhonetic;
	}

	public void setRoomPhonetic(String roomPhonetic) {
		this.roomPhonetic = roomPhonetic;
	}

	public String getAddonsPhonetic() {
		return addonsPhonetic;
	}

	public void setAddonsPhonetic(String addonsPhonetic) {
		this.addonsPhonetic = addonsPhonetic;
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

	public int getuRecord() {
		return uRecord;
	}

	public void setuRecord(int uRecord) {
		this.uRecord = uRecord;
	}

	public String getuDate() {
		return uDate;
	}

	public void setuDate(String uDate) {
		this.uDate = uDate;
	}

	@Override
	public String tableName() {
		return "ix_poi_address";
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
		return ObjType.IXPOIADDRESS;
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
		return "POI_PID";
	}

	@Override
	public int parentPKValue() {
		return this.getPoiPid();
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
					String newValue = json.getString(key);
					if("null".equalsIgnoreCase(newValue))newValue=null;
					logger.info("objValue:"+objValue);
					logger.info("newValue:"+newValue);
					if (!isEqualsString(objValue,newValue)) {
						logger.info("isEqualsString:false");
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
	
	private static boolean isEqualsString(Object oldValue,Object newValue){
		if(null==oldValue&&null==newValue)
			return true;
		if(StringUtils.isEmpty(oldValue)&&StringUtils.isEmpty(newValue)){
			return true;
		}
		if(oldValue==null&&newValue!=null){
			return false;
		}
		if(oldValue!=null&&newValue==null){
			return false;
		}
		return oldValue.toString().equals(newValue.toString());
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
		return "name_id";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IObj#childMap()
	 */
	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		// TODO Auto-generated method stub
		return null;
	}

}
