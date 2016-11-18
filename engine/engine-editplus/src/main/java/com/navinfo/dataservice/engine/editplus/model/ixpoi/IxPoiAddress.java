package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;

/** 
* @ClassName:  IxPoiAddress 
* @author code generator
* @date 2016-11-16 02:30:36 
* @Description: TODO
*/
public class IxPoiAddress extends BasicRow {
	protected long nameId ;
	protected long nameGroupid ;
	protected long poiPid ;
	protected String langCode ;
	protected Integer srcFlag ;
	protected String fullname ;
	protected String fullnamePhonetic ;
	protected String roadname ;
	protected String roadnamePhonetic ;
	protected String addrname ;
	protected String addrnamePhonetic ;
	protected String province ;
	protected String city ;
	protected String county ;
	protected String town ;
	protected String place ;
	protected String street ;
	protected String landmark ;
	protected String prefix ;
	protected String housenum ;
	protected String type ;
	protected String subnum ;
	protected String surfix ;
	protected String estab ;
	protected String building ;
	protected String floor ;
	protected String unit ;
	protected String room ;
	protected String addons ;
	protected String provPhonetic ;
	protected String cityPhonetic ;
	protected String countyPhonetic ;
	protected String townPhonetic ;
	protected String streetPhonetic ;
	protected String placePhonetic ;
	protected String landmarkPhonetic ;
	protected String prefixPhonetic ;
	protected String housenumPhonetic ;
	protected String typePhonetic ;
	protected String subnumPhonetic ;
	protected String surfixPhonetic ;
	protected String estabPhonetic ;
	protected String buildingPhonetic ;
	protected String floorPhonetic ;
	protected String unitPhonetic ;
	protected String roomPhonetic ;
	protected String addonsPhonetic ;
//	protected Integer uRecord ;
//	protected String uFields ;
//	protected String uDate ;
	
	public IxPoiAddress (long objPid){
		super(objPid);
	}
	
	public long getNameId() {
		return nameId;
	}
	protected void setNameId(long nameId) {
		this.nameId = nameId;
	}
	public long getNameGroupid() {
		return nameGroupid;
	}
	protected void setNameGroupid(long nameGroupid) {
		this.nameGroupid = nameGroupid;
	}
	public long getPoiPid() {
		return poiPid;
	}
	protected void setPoiPid(long poiPid) {
		this.poiPid = poiPid;
	}
	public String getLangCode() {
		return langCode;
	}
	protected void setLangCode(String langCode) {
		this.langCode = langCode;
	}
	public Integer getSrcFlag() {
		return srcFlag;
	}
	protected void setSrcFlag(Integer srcFlag) {
		this.srcFlag = srcFlag;
	}
	public String getFullname() {
		return fullname;
	}
	protected void setFullname(String fullname) {
		this.fullname = fullname;
	}
	public String getFullnamePhonetic() {
		return fullnamePhonetic;
	}
	protected void setFullnamePhonetic(String fullnamePhonetic) {
		this.fullnamePhonetic = fullnamePhonetic;
	}
	public String getRoadname() {
		return roadname;
	}
	protected void setRoadname(String roadname) {
		this.roadname = roadname;
	}
	public String getRoadnamePhonetic() {
		return roadnamePhonetic;
	}
	protected void setRoadnamePhonetic(String roadnamePhonetic) {
		this.roadnamePhonetic = roadnamePhonetic;
	}
	public String getAddrname() {
		return addrname;
	}
	protected void setAddrname(String addrname) {
		this.addrname = addrname;
	}
	public String getAddrnamePhonetic() {
		return addrnamePhonetic;
	}
	protected void setAddrnamePhonetic(String addrnamePhonetic) {
		this.addrnamePhonetic = addrnamePhonetic;
	}
	public String getProvince() {
		return province;
	}
	protected void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	protected void setCity(String city) {
		this.city = city;
	}
	public String getCounty() {
		return county;
	}
	protected void setCounty(String county) {
		this.county = county;
	}
	public String getTown() {
		return town;
	}
	protected void setTown(String town) {
		this.town = town;
	}
	public String getPlace() {
		return place;
	}
	protected void setPlace(String place) {
		this.place = place;
	}
	public String getStreet() {
		return street;
	}
	protected void setStreet(String street) {
		this.street = street;
	}
	public String getLandmark() {
		return landmark;
	}
	protected void setLandmark(String landmark) {
		this.landmark = landmark;
	}
	public String getPrefix() {
		return prefix;
	}
	protected void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getHousenum() {
		return housenum;
	}
	protected void setHousenum(String housenum) {
		this.housenum = housenum;
	}
	public String getType() {
		return type;
	}
	protected void setType(String type) {
		this.type = type;
	}
	public String getSubnum() {
		return subnum;
	}
	protected void setSubnum(String subnum) {
		this.subnum = subnum;
	}
	public String getSurfix() {
		return surfix;
	}
	protected void setSurfix(String surfix) {
		this.surfix = surfix;
	}
	public String getEstab() {
		return estab;
	}
	protected void setEstab(String estab) {
		this.estab = estab;
	}
	public String getBuilding() {
		return building;
	}
	protected void setBuilding(String building) {
		this.building = building;
	}
	public String getFloor() {
		return floor;
	}
	protected void setFloor(String floor) {
		this.floor = floor;
	}
	public String getUnit() {
		return unit;
	}
	protected void setUnit(String unit) {
		this.unit = unit;
	}
	public String getRoom() {
		return room;
	}
	protected void setRoom(String room) {
		this.room = room;
	}
	public String getAddons() {
		return addons;
	}
	protected void setAddons(String addons) {
		this.addons = addons;
	}
	public String getProvPhonetic() {
		return provPhonetic;
	}
	protected void setProvPhonetic(String provPhonetic) {
		this.provPhonetic = provPhonetic;
	}
	public String getCityPhonetic() {
		return cityPhonetic;
	}
	protected void setCityPhonetic(String cityPhonetic) {
		this.cityPhonetic = cityPhonetic;
	}
	public String getCountyPhonetic() {
		return countyPhonetic;
	}
	protected void setCountyPhonetic(String countyPhonetic) {
		this.countyPhonetic = countyPhonetic;
	}
	public String getTownPhonetic() {
		return townPhonetic;
	}
	protected void setTownPhonetic(String townPhonetic) {
		this.townPhonetic = townPhonetic;
	}
	public String getStreetPhonetic() {
		return streetPhonetic;
	}
	protected void setStreetPhonetic(String streetPhonetic) {
		this.streetPhonetic = streetPhonetic;
	}
	public String getPlacePhonetic() {
		return placePhonetic;
	}
	protected void setPlacePhonetic(String placePhonetic) {
		this.placePhonetic = placePhonetic;
	}
	public String getLandmarkPhonetic() {
		return landmarkPhonetic;
	}
	protected void setLandmarkPhonetic(String landmarkPhonetic) {
		this.landmarkPhonetic = landmarkPhonetic;
	}
	public String getPrefixPhonetic() {
		return prefixPhonetic;
	}
	protected void setPrefixPhonetic(String prefixPhonetic) {
		this.prefixPhonetic = prefixPhonetic;
	}
	public String getHousenumPhonetic() {
		return housenumPhonetic;
	}
	protected void setHousenumPhonetic(String housenumPhonetic) {
		this.housenumPhonetic = housenumPhonetic;
	}
	public String getTypePhonetic() {
		return typePhonetic;
	}
	protected void setTypePhonetic(String typePhonetic) {
		this.typePhonetic = typePhonetic;
	}
	public String getSubnumPhonetic() {
		return subnumPhonetic;
	}
	protected void setSubnumPhonetic(String subnumPhonetic) {
		this.subnumPhonetic = subnumPhonetic;
	}
	public String getSurfixPhonetic() {
		return surfixPhonetic;
	}
	protected void setSurfixPhonetic(String surfixPhonetic) {
		this.surfixPhonetic = surfixPhonetic;
	}
	public String getEstabPhonetic() {
		return estabPhonetic;
	}
	protected void setEstabPhonetic(String estabPhonetic) {
		this.estabPhonetic = estabPhonetic;
	}
	public String getBuildingPhonetic() {
		return buildingPhonetic;
	}
	protected void setBuildingPhonetic(String buildingPhonetic) {
		this.buildingPhonetic = buildingPhonetic;
	}
	public String getFloorPhonetic() {
		return floorPhonetic;
	}
	protected void setFloorPhonetic(String floorPhonetic) {
		this.floorPhonetic = floorPhonetic;
	}
	public String getUnitPhonetic() {
		return unitPhonetic;
	}
	protected void setUnitPhonetic(String unitPhonetic) {
		this.unitPhonetic = unitPhonetic;
	}
	public String getRoomPhonetic() {
		return roomPhonetic;
	}
	protected void setRoomPhonetic(String roomPhonetic) {
		this.roomPhonetic = roomPhonetic;
	}
	public String getAddonsPhonetic() {
		return addonsPhonetic;
	}
	protected void setAddonsPhonetic(String addonsPhonetic) {
		this.addonsPhonetic = addonsPhonetic;
	}
//	public Integer getURecord() {
//		return uRecord;
//	}
//	protected void setURecord(Integer uRecord) {
//		this.uRecord = uRecord;
//	}
//	public String getUFields() {
//		return uFields;
//	}
//	protected void setUFields(String uFields) {
//		this.uFields = uFields;
//	}
//	public String getUDate() {
//		return uDate;
//	}
//	protected void setUDate(String uDate) {
//		this.uDate = uDate;
//	}
	
	@Override
	public String tableName() {
		return "IX_POI_ADDRESS";
	}
}
