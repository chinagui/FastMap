package com.navinfo.dataservice.dao.plus.model.ixpointaddress;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPointaddressName 
* @author code generator
* @date 2017-09-15 08:59:40 
* @Description: TODO
*/
public class IxPointaddressName extends BasicRow {
	private long pid ;
	private long nameId ;
	private long nameGroupid = 1;
	private String langCode ;
	private int sumChar ;
	private String splitFlag ;
	private String fullname ;
	private String fullnamePhonetic ;
	private String roadname ;
	private String roadnamePhonetic ;
	private String addrname ;
	private String addrnamePhonetic ;
	private String province ;
	private String city ;
	private String county ;
	private String town ;
	private String place ;
	private String street ;
	private String landmark ;
	private String prefix ;
	private String housenum ;
	private String type ;
	private String subnum ;
	private String surfix ;
	private String estab ;
	private String building ;
	private String unit ;
	private String floor ;
	private String room ;
	private String addons ;
	private String provPhonetic ;
	private String cityPhonetic ;
	private String countyPhonetic ;
	private String townPhonetic ;
	private String streetPhonetic ;
	private String placePhonetic ;
	private String landmarkPhonetic ;
	private String prefixPhonetic ;
	private String housenumPhonetic ;
	private String typePhonetic ;
	private String subnumPhonetic ;
	private String surfixPhonetic ;
	private String estabPhonetic ;
	private String buildingPhonetic ;
	private String floorPhonetic ;
	private String unitPhonetic ;
	private String roomPhonetic ;
	private String addonsPhonetic ;
	
	public IxPointaddressName (long objPid){
		super(objPid);
		setPid(objPid);
	}
	
	public long getPid(){
		return pid;
	}
	public void setPid(long pid){
		if(this.checkValue("PID", this.pid, pid)){
			this.pid = pid;
		}
	}
	public long getNameId() {
		return nameId;
	}
	public void setNameId(long nameId) {
		if(this.checkValue("NAME_ID", this.nameId, nameId)){
			this.nameId = nameId;
		}
	}
	public long getNameGroupid() {
		return nameGroupid;
	}
	public void setNameGroupid(long nameGroupid) {
		if(this.checkValue("NAME_GROUPID", this.nameGroupid, nameGroupid)){
			this.nameGroupid = nameGroupid;
		}
	}
	public String getLangCode() {
		return langCode;
	}
	public void setLangCode(String langCode) {
		if(this.checkValue("LANG_CODE", this.langCode, langCode)){
			this.langCode = langCode;
		}
	}
	public int getSumChar() {
		return sumChar;
	}
	public void setSumChar(int sumChar) {
		if(this.checkValue("SUM_CHAR", this.sumChar, sumChar)){
			this.sumChar = sumChar;
		}
	}
	public String getSplitFlag() {
		return splitFlag;
	}
	public void setSplitFlag(String splitFlag) {
		if(this.checkValue("SPLIT_FLAG", this.splitFlag, splitFlag)){
			this.splitFlag = splitFlag;
		}
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		if(this.checkValue("FULLNAME", this.fullname, fullname)){
			this.fullname = fullname;
		}
	}
	public String getFullnamePhonetic() {
		return fullnamePhonetic;
	}
	public void setFullnamePhonetic(String fullnamePhonetic) {
		if(this.checkValue("FULLNAME_PHONETIC", this.fullnamePhonetic, fullnamePhonetic)){
			this.fullnamePhonetic = fullnamePhonetic;
		}
	}
	public String getRoadname() {
		return roadname;
	}
	public void setRoadname(String roadname) {
		if(this.checkValue("ROADNAME", this.roadname, roadname)){
			this.roadname = roadname;
		}
	}
	public String getRoadnamePhonetic() {
		return roadnamePhonetic;
	}
	public void setRoadnamePhonetic(String roadnamePhonetic) {
		if(this.checkValue("ROADNAME_PHONETIC", this.roadnamePhonetic, roadnamePhonetic)){
			this.roadnamePhonetic = roadnamePhonetic;
		}
	}
	public String getAddrname() {
		return addrname;
	}
	public void setAddrname(String addrname) {
		if(this.checkValue("ADDRNAME", this.addrname, addrname)){
			this.addrname = addrname;
		}
	}
	public String getAddrnamePhonetic() {
		return addrnamePhonetic;
	}
	public void setAddrnamePhonetic(String addrnamePhonetic) {
		if(this.checkValue("ADDRNAME_PHONETIC", this.addrnamePhonetic, addrnamePhonetic)){
			this.addrnamePhonetic = addrnamePhonetic;
		}
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		if(this.checkValue("PROVINCE", this.province, province)){
			this.province = province;
		}
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		if(this.checkValue("CITY", this.city, city)){
			this.city = city;
		}
	}
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		if(this.checkValue("COUNTY", this.county, county)){
			this.county = county;
		}
	}
	public String getTown() {
		return town;
	}
	public void setTown(String town) {
		if(this.checkValue("TOWN", this.town, town)){
			this.town = town;
		}
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		if(this.checkValue("PLACE", this.place, place)){
			this.place = place;
		}
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		if(this.checkValue("STREET", this.street, street)){
			this.street = street;
		}
	}
	public String getLandmark() {
		return landmark;
	}
	public void setLandmark(String landmark) {
		if(this.checkValue("LANDMARK", this.landmark, landmark)){
			this.landmark = landmark;
		}
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		if(this.checkValue("PREFIX", this.prefix, prefix)){
			this.prefix = prefix;
		}
	}
	public String getHousenum() {
		return housenum;
	}
	public void setHousenum(String housenum) {
		if(this.checkValue("HOUSENUM", this.housenum, housenum)){
			this.housenum = housenum;
		}
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		if(this.checkValue("TYPE", this.type, type)){
			this.type = type;
		}
	}
	public String getSubnum() {
		return subnum;
	}
	public void setSubnum(String subnum) {
		if(this.checkValue("SUBNUM", this.subnum, subnum)){
			this.subnum = subnum;
		}
	}
	public String getSurfix() {
		return surfix;
	}
	public void setSurfix(String surfix) {
		if(this.checkValue("SURFIX", this.surfix, surfix)){
			this.surfix = surfix;
		}
	}
	public String getEstab() {
		return estab;
	}
	public void setEstab(String estab) {
		if(this.checkValue("ESTAB", this.estab, estab)){
			this.estab = estab;
		}
	}
	public String getBuilding() {
		return building;
	}
	public void setBuilding(String building) {
		if(this.checkValue("BUILDING", this.building, building)){
			this.building = building;
		}
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		if(this.checkValue("UNIT", this.unit, unit)){
			this.unit = unit;
		}
	}
	public String getFloor() {
		return floor;
	}
	public void setFloor(String floor) {
		if(this.checkValue("FLOOR", this.floor, floor)){
			this.floor = floor;
		}
	}
	public String getRoom() {
		return room;
	}
	public void setRoom(String room) {
		if(this.checkValue("ROOM", this.room, room)){
			this.room = room;
		}
	}
	public String getAddons() {
		return addons;
	}
	public void setAddons(String addons) {
		if(this.checkValue("ADDONS", this.addons, addons)){
			this.addons = addons;
		}
	}
	public String getProvPhonetic() {
		return provPhonetic;
	}
	public void setProvPhonetic(String provPhonetic) {
		if(this.checkValue("PROV_PHONETIC", this.provPhonetic, provPhonetic)){
			this.provPhonetic = provPhonetic;
		}
	}
	public String getCityPhonetic() {
		return cityPhonetic;
	}
	public void setCityPhonetic(String cityPhonetic) {
		if(this.checkValue("CITY_PHONETIC", this.cityPhonetic, cityPhonetic)){
			this.cityPhonetic = cityPhonetic;
		}
	}
	public String getCountyPhonetic() {
		return countyPhonetic;
	}
	public void setCountyPhonetic(String countyPhonetic) {
		if(this.checkValue("COUNTY_PHONETIC", this.countyPhonetic, countyPhonetic)){
			this.countyPhonetic = countyPhonetic;
		}
	}
	public String getTownPhonetic() {
		return townPhonetic;
	}
	public void setTownPhonetic(String townPhonetic) {
		if(this.checkValue("TOWN_PHONETIC", this.townPhonetic, townPhonetic)){
			this.townPhonetic = townPhonetic;
		}
	}
	public String getStreetPhonetic() {
		return streetPhonetic;
	}
	public void setStreetPhonetic(String streetPhonetic) {
		if(this.checkValue("STREET_PHONETIC", this.streetPhonetic, streetPhonetic)){
			this.streetPhonetic = streetPhonetic;
		}
	}
	public String getPlacePhonetic() {
		return placePhonetic;
	}
	public void setPlacePhonetic(String placePhonetic) {
		if(this.checkValue("PLACE_PHONETIC", this.placePhonetic, placePhonetic)){
			this.placePhonetic = placePhonetic;
		}
	}
	public String getLandmarkPhonetic() {
		return landmarkPhonetic;
	}
	public void setLandmarkPhonetic(String landmarkPhonetic) {
		if(this.checkValue("LANDMARK_PHONETIC", this.landmarkPhonetic, landmarkPhonetic)){
			this.landmarkPhonetic = landmarkPhonetic;
		}
	}
	public String getPrefixPhonetic() {
		return prefixPhonetic;
	}
	public void setPrefixPhonetic(String prefixPhonetic) {
		if(this.checkValue("PREFIX_PHONETIC", this.prefixPhonetic, prefixPhonetic)){
			this.prefixPhonetic = prefixPhonetic;
		}
	}
	public String getHousenumPhonetic() {
		return housenumPhonetic;
	}
	public void setHousenumPhonetic(String housenumPhonetic) {
		if(this.checkValue("HOUSENUM_PHONETIC", this.housenumPhonetic, housenumPhonetic)){
			this.housenumPhonetic = housenumPhonetic;
		}
	}
	public String getTypePhonetic() {
		return typePhonetic;
	}
	public void setTypePhonetic(String typePhonetic) {
		if(this.checkValue("TYPE_PHONETIC", this.typePhonetic, typePhonetic)){
			this.typePhonetic = typePhonetic;
		}
	}
	public String getSubnumPhonetic() {
		return subnumPhonetic;
	}
	public void setSubnumPhonetic(String subnumPhonetic) {
		if(this.checkValue("SUBNUM_PHONETIC", this.subnumPhonetic, subnumPhonetic)){
			this.subnumPhonetic = subnumPhonetic;
		}
	}
	public String getSurfixPhonetic() {
		return surfixPhonetic;
	}
	public void setSurfixPhonetic(String surfixPhonetic) {
		if(this.checkValue("SURFIX_PHONETIC", this.surfixPhonetic, surfixPhonetic)){
			this.surfixPhonetic = surfixPhonetic;
		}
	}
	public String getEstabPhonetic() {
		return estabPhonetic;
	}
	public void setEstabPhonetic(String estabPhonetic) {
		if(this.checkValue("ESTAB_PHONETIC", this.estabPhonetic, estabPhonetic)){
			this.estabPhonetic = estabPhonetic;
		}
	}
	public String getBuildingPhonetic() {
		return buildingPhonetic;
	}
	public void setBuildingPhonetic(String buildingPhonetic) {
		if(this.checkValue("BUILDING_PHONETIC", this.buildingPhonetic, buildingPhonetic)){
			this.buildingPhonetic = buildingPhonetic;
		}
	}
	public String getFloorPhonetic() {
		return floorPhonetic;
	}
	public void setFloorPhonetic(String floorPhonetic) {
		if(this.checkValue("FLOOR_PHONETIC", this.floorPhonetic, floorPhonetic)){
			this.floorPhonetic = floorPhonetic;
		}
	}
	public String getUnitPhonetic() {
		return unitPhonetic;
	}
	public void setUnitPhonetic(String unitPhonetic) {
		if(this.checkValue("UNIT_PHONETIC", this.unitPhonetic, unitPhonetic)){
			this.unitPhonetic = unitPhonetic;
		}
	}
	public String getRoomPhonetic() {
		return roomPhonetic;
	}
	public void setRoomPhonetic(String roomPhonetic) {
		if(this.checkValue("ROOM_PHONETIC", this.roomPhonetic, roomPhonetic)){
			this.roomPhonetic = roomPhonetic;
		}
	}
	public String getAddonsPhonetic() {
		return addonsPhonetic;
	}
	public void setAddonsPhonetic(String addonsPhonetic) {
		if(this.checkValue("ADDONS_PHONETIC", this.addonsPhonetic, addonsPhonetic)){
			this.addonsPhonetic = addonsPhonetic;
		}
	}

	@Override
	public String tableName() {
		// TODO Auto-generated method stub
		return "IX_POINTADDRESS_NAME";
	}

	
	
}
