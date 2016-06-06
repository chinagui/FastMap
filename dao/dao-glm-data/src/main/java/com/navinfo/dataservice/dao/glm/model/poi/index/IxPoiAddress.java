package com.navinfo.dataservice.dao.glm.model.poi.index;

import java.lang.reflect.Field;
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
 * POI地址表
 * @author zhangxiaolong
 *
 */
public class IxPoiAddress implements IObj {

	private int pid;
	
	private int poiPid;
	
	private int nameGroupid;
	
	private String langCode;
	
	private int srcFlag;
	
	private String fullName;
	
	private String rodeName;
	
	private String addrname;
	
	private String province;
	
	private String city;
	
	private String county;
	
	private String town;
	
	private String place;
	
	private String street;
	
	private String landmark;
	
	private String prefix;
	
	private String housesum;
	
	private String type;
	
	private String subnum;
	
	private String surfix;
	
	private String estab;
	
	private String building;
	
	private String floor;
	
	private String unit;
	
	private String room;
	
	private String addons;
	
	private String fullnamePhonetic;
	
	private String roadnamePhonetic;
	
	private String addrnamePhonetic;
	
	private String provPhonetic;
	
	private String cityPhonetic;
	
	private String countyPhonetic;
	
	private String townPhonetic;
	
	private String streetPhonetic;
	
	private String placePhonetic;
	
	private String landmarkPhonetic;
	
	private String prefixPhonetic;
	
	private String housenumPhonetic;
	
	private String typePhonetic;
	
	private String subsumPhonetic;
	
	private String surfixPhonetic;
	
	private String estabPhonetic;
	
	private String buildingPhonetic;
	
	private String floorPhonetic;
	
	private String unitPhonetic;
	
	private String roomPhonetic;
	
	private String addonsPhonetic;
	
	private String rowId;
	
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

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getRodeName() {
		return rodeName;
	}

	public void setRodeName(String rodeName) {
		this.rodeName = rodeName;
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

	public String getHousesum() {
		return housesum;
	}

	public void setHousesum(String housesum) {
		this.housesum = housesum;
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

	public String getSubsumPhonetic() {
		return subsumPhonetic;
	}

	public void setSubsumPhonetic(String subsumPhonetic) {
		this.subsumPhonetic = subsumPhonetic;
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
		return "name_id";
	}

	@Override
	public int parentPKValue() {
		return this.pid;
	}

	@Override
	public String parentTableName() {
		return "ix_poi_address";
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

}
