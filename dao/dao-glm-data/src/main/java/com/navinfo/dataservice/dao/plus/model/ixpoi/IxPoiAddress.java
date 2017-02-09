package com.navinfo.dataservice.dao.plus.model.ixpoi;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;

/** 
* @ClassName:  IxPoiAddress 
* @author code generator
* @date 2016-11-18 11:27:08 
* @Description: TODO
*/
public class IxPoiAddress extends BasicRow {
	protected long nameId ;
	protected long nameGroupid =1 ;
	protected long poiPid ;
	protected String langCode ;
	protected int srcFlag ;
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
	
	public IxPoiAddress (long objPid){
		super(objPid);
		setPoiPid(objPid);
	}
	
	public long getNameId() {
		return nameId;
	}
	public void setNameId(long nameId) {
		if(this.checkValue("NAME_ID",this.nameId,nameId)){
			this.nameId = nameId;
		}
	}
	public long getNameGroupid() {
		return nameGroupid;
	}
	public void setNameGroupid(long nameGroupid) {
		if(this.checkValue("NAME_GROUPID",this.nameGroupid,nameGroupid)){
			this.nameGroupid = nameGroupid;
		}
	}
	public long getPoiPid() {
		return poiPid;
	}
	public void setPoiPid(long poiPid) {
		if(this.checkValue("POI_PID",this.poiPid,poiPid)){
			this.poiPid = poiPid;
		}
	}
	public String getLangCode() {
		return langCode;
	}
	public void setLangCode(String langCode) {
		if(this.checkValue("LANG_CODE",this.langCode,langCode)){
			this.langCode = langCode;
		}
	}
	public int getSrcFlag() {
		return srcFlag;
	}
	public void setSrcFlag(int srcFlag) {
		if(this.checkValue("SRC_FLAG",this.srcFlag,srcFlag)){
			this.srcFlag = srcFlag;
		}
	}
	public String getFullname() {
		return fullname;
	}
	public void setFullname(String fullname) {
		if(this.checkValue("FULLNAME",this.fullname,fullname)){
			this.fullname = fullname;
		}
	}
	public String getFullnamePhonetic() {
		return fullnamePhonetic;
	}
	public void setFullnamePhonetic(String fullnamePhonetic) {
		if(this.checkValue("FULLNAME_PHONETIC",this.fullnamePhonetic,fullnamePhonetic)){
			this.fullnamePhonetic = fullnamePhonetic;
		}
	}
	public String getRoadname() {
		return roadname;
	}
	public void setRoadname(String roadname) {
		if(this.checkValue("ROADNAME",this.roadname,roadname)){
			this.roadname = roadname;
		}
	}
	public String getRoadnamePhonetic() {
		return roadnamePhonetic;
	}
	public void setRoadnamePhonetic(String roadnamePhonetic) {
		if(this.checkValue("ROADNAME_PHONETIC",this.roadnamePhonetic,roadnamePhonetic)){
			this.roadnamePhonetic = roadnamePhonetic;
		}
	}
	public String getAddrname() {
		return addrname;
	}
	public void setAddrname(String addrname) {
		if(this.checkValue("ADDRNAME",this.addrname,addrname)){
			this.addrname = addrname;
		}
	}
	public String getAddrnamePhonetic() {
		return addrnamePhonetic;
	}
	public void setAddrnamePhonetic(String addrnamePhonetic) {
		if(this.checkValue("ADDRNAME_PHONETIC",this.addrnamePhonetic,addrnamePhonetic)){
			this.addrnamePhonetic = addrnamePhonetic;
		}
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		if(this.checkValue("PROVINCE",this.province,province)){
			this.province = province;
		}
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		if(this.checkValue("CITY",this.city,city)){
			this.city = city;
		}
	}
	public String getCounty() {
		return county;
	}
	public void setCounty(String county) {
		if(this.checkValue("COUNTY",this.county,county)){
			this.county = county;
		}
	}
	public String getTown() {
		return town;
	}
	public void setTown(String town) {
		if(this.checkValue("TOWN",this.town,town)){
			this.town = town;
		}
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		if(this.checkValue("PLACE",this.place,place)){
			this.place = place;
		}
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		if(this.checkValue("STREET",this.street,street)){
			this.street = street;
		}
	}
	public String getLandmark() {
		return landmark;
	}
	public void setLandmark(String landmark) {
		if(this.checkValue("LANDMARK",this.landmark,landmark)){
			this.landmark = landmark;
		}
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		if(this.checkValue("PREFIX",this.prefix,prefix)){
			this.prefix = prefix;
		}
	}
	public String getHousenum() {
		return housenum;
	}
	public void setHousenum(String housenum) {
		if(this.checkValue("HOUSENUM",this.housenum,housenum)){
			this.housenum = housenum;
		}
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		if(this.checkValue("TYPE",this.type,type)){
			this.type = type;
		}
	}
	public String getSubnum() {
		return subnum;
	}
	public void setSubnum(String subnum) {
		if(this.checkValue("SUBNUM",this.subnum,subnum)){
			this.subnum = subnum;
		}
	}
	public String getSurfix() {
		return surfix;
	}
	public void setSurfix(String surfix) {
		if(this.checkValue("SURFIX",this.surfix,surfix)){
			this.surfix = surfix;
		}
	}
	public String getEstab() {
		return estab;
	}
	public void setEstab(String estab) {
		if(this.checkValue("ESTAB",this.estab,estab)){
			this.estab = estab;
		}
	}
	public String getBuilding() {
		return building;
	}
	public void setBuilding(String building) {
		if(this.checkValue("BUILDING",this.building,building)){
			this.building = building;
		}
	}
	public String getFloor() {
		return floor;
	}
	public void setFloor(String floor) {
		if(this.checkValue("FLOOR",this.floor,floor)){
			this.floor = floor;
		}
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		if(this.checkValue("UNIT",this.unit,unit)){
			this.unit = unit;
		}
	}
	public String getRoom() {
		return room;
	}
	public void setRoom(String room) {
		if(this.checkValue("ROOM",this.room,room)){
			this.room = room;
		}
	}
	public String getAddons() {
		return addons;
	}
	public void setAddons(String addons) {
		if(this.checkValue("ADDONS",this.addons,addons)){
			this.addons = addons;
		}
	}
	public String getProvPhonetic() {
		return provPhonetic;
	}
	public void setProvPhonetic(String provPhonetic) {
		if(this.checkValue("PROV_PHONETIC",this.provPhonetic,provPhonetic)){
			this.provPhonetic = provPhonetic;
		}
	}
	public String getCityPhonetic() {
		return cityPhonetic;
	}
	public void setCityPhonetic(String cityPhonetic) {
		if(this.checkValue("CITY_PHONETIC",this.cityPhonetic,cityPhonetic)){
			this.cityPhonetic = cityPhonetic;
		}
	}
	public String getCountyPhonetic() {
		return countyPhonetic;
	}
	public void setCountyPhonetic(String countyPhonetic) {
		if(this.checkValue("COUNTY_PHONETIC",this.countyPhonetic,countyPhonetic)){
			this.countyPhonetic = countyPhonetic;
		}
	}
	public String getTownPhonetic() {
		return townPhonetic;
	}
	public void setTownPhonetic(String townPhonetic) {
		if(this.checkValue("TOWN_PHONETIC",this.townPhonetic,townPhonetic)){
			this.townPhonetic = townPhonetic;
		}
	}
	public String getStreetPhonetic() {
		return streetPhonetic;
	}
	public void setStreetPhonetic(String streetPhonetic) {
		if(this.checkValue("STREET_PHONETIC",this.streetPhonetic,streetPhonetic)){
			this.streetPhonetic = streetPhonetic;
		}
	}
	public String getPlacePhonetic() {
		return placePhonetic;
	}
	public void setPlacePhonetic(String placePhonetic) {
		if(this.checkValue("PLACE_PHONETIC",this.placePhonetic,placePhonetic)){
			this.placePhonetic = placePhonetic;
		}
	}
	public String getLandmarkPhonetic() {
		return landmarkPhonetic;
	}
	public void setLandmarkPhonetic(String landmarkPhonetic) {
		if(this.checkValue("LANDMARK_PHONETIC",this.landmarkPhonetic,landmarkPhonetic)){
			this.landmarkPhonetic = landmarkPhonetic;
		}
	}
	public String getPrefixPhonetic() {
		return prefixPhonetic;
	}
	public void setPrefixPhonetic(String prefixPhonetic) {
		if(this.checkValue("PREFIX_PHONETIC",this.prefixPhonetic,prefixPhonetic)){
			this.prefixPhonetic = prefixPhonetic;
		}
	}
	public String getHousenumPhonetic() {
		return housenumPhonetic;
	}
	public void setHousenumPhonetic(String housenumPhonetic) {
		if(this.checkValue("HOUSENUM_PHONETIC",this.housenumPhonetic,housenumPhonetic)){
			this.housenumPhonetic = housenumPhonetic;
		}
	}
	public String getTypePhonetic() {
		return typePhonetic;
	}
	public void setTypePhonetic(String typePhonetic) {
		if(this.checkValue("TYPE_PHONETIC",this.typePhonetic,typePhonetic)){
			this.typePhonetic = typePhonetic;
		}
	}
	public String getSubnumPhonetic() {
		return subnumPhonetic;
	}
	public void setSubnumPhonetic(String subnumPhonetic) {
		if(this.checkValue("SUBNUM_PHONETIC",this.subnumPhonetic,subnumPhonetic)){
			this.subnumPhonetic = subnumPhonetic;
		}
	}
	public String getSurfixPhonetic() {
		return surfixPhonetic;
	}
	public void setSurfixPhonetic(String surfixPhonetic) {
		if(this.checkValue("SURFIX_PHONETIC",this.surfixPhonetic,surfixPhonetic)){
			this.surfixPhonetic = surfixPhonetic;
		}
	}
	public String getEstabPhonetic() {
		return estabPhonetic;
	}
	public void setEstabPhonetic(String estabPhonetic) {
		if(this.checkValue("ESTAB_PHONETIC",this.estabPhonetic,estabPhonetic)){
			this.estabPhonetic = estabPhonetic;
		}
	}
	public String getBuildingPhonetic() {
		return buildingPhonetic;
	}
	public void setBuildingPhonetic(String buildingPhonetic) {
		if(this.checkValue("BUILDING_PHONETIC",this.buildingPhonetic,buildingPhonetic)){
			this.buildingPhonetic = buildingPhonetic;
		}
	}
	public String getFloorPhonetic() {
		return floorPhonetic;
	}
	public void setFloorPhonetic(String floorPhonetic) {
		if(this.checkValue("FLOOR_PHONETIC",this.floorPhonetic,floorPhonetic)){
			this.floorPhonetic = floorPhonetic;
		}
	}
	public String getUnitPhonetic() {
		return unitPhonetic;
	}
	public void setUnitPhonetic(String unitPhonetic) {
		if(this.checkValue("UNIT_PHONETIC",this.unitPhonetic,unitPhonetic)){
			this.unitPhonetic = unitPhonetic;
		}
	}
	public String getRoomPhonetic() {
		return roomPhonetic;
	}
	public void setRoomPhonetic(String roomPhonetic) {
		if(this.checkValue("ROOM_PHONETIC",this.roomPhonetic,roomPhonetic)){
			this.roomPhonetic = roomPhonetic;
		}
	}
	public String getAddonsPhonetic() {
		return addonsPhonetic;
	}
	public void setAddonsPhonetic(String addonsPhonetic) {
		if(this.checkValue("ADDONS_PHONETIC",this.addonsPhonetic,addonsPhonetic)){
			this.addonsPhonetic = addonsPhonetic;
		}
	}
	/**
	 * 是否中文地址，langCode IN (CHI，CHT)就算
	 * @return
	 */
	public boolean isCH() {
		if(this.langCode.equals("CHI")||this.langCode.equals("CHT")){
			return true;
		}
		return false;
	}
	/**
	 * 是否英文地址，langCode IN (ENG)就算
	 * @return
	 */
	public boolean isEng() {
		if(this.langCode.equals("ENG")){
			return true;
		}
		return false;
	}
	/**
	 * 是否葡文地址，langCode IN (POR)就算
	 * @return
	 */
	public boolean isPor() {
		if(this.langCode.equals("POR")){
			return true;
		}
		return false;
	}
	
	/**
	 * 将中文地址拆分后的15个字段
	 * 按照“附加信息、房间号、楼层、楼门号、楼栋号、附属设施名、后缀、子号、类型名、门牌号、前缀、标志物名、街巷名、地名小区名、乡镇街道办”
	 * 进行合并
	 * @return splitAddStr
	 */
	public String getChiSplitAddr() {
		List<String> splitAddList = new ArrayList<String>();
		splitAddList.add(this.getAddons());
        splitAddList.add(this.getRoom());
        splitAddList.add(this.getFloor());
        splitAddList.add(this.getUnit());
        splitAddList.add(this.getBuilding());
        splitAddList.add(this.getEstab());
        splitAddList.add(this.getSurfix());
        splitAddList.add(this.getSubnum());
        splitAddList.add(this.getType());
        splitAddList.add(this.getHousenum());
        splitAddList.add(this.getPrefix());
        splitAddList.add(this.getLandmark());
        if (StringUtils.isNotEmpty(this.getRoadname())){
        	splitAddList.add(this.getStreet());
        	splitAddList.add(this.getPlace());
        	splitAddList.add(this.getTown());
        }
        
        String splitAddStr = splitAddList.toString().replace("[", "").replace("]", "").replaceAll(",","");
        return splitAddStr;
	}
	
	
	@Override
	public String tableName() {
		return "IX_POI_ADDRESS";
	}
	
	public static final String NAME_ID = "NAME_ID";
	public static final String NAME_GROUPID = "NAME_GROUPID";
	public static final String POI_PID = "POI_PID";
	public static final String LANG_CODE = "LANG_CODE";
	public static final String SRC_FLAG = "SRC_FLAG";
	public static final String FULLNAME = "FULLNAME";
	public static final String FULLNAME_PHONETIC = "FULLNAME_PHONETIC";
	public static final String ROADNAME = "ROADNAME";
	public static final String ROADNAME_PHONETIC = "ROADNAME_PHONETIC";
	public static final String ADDRNAME = "ADDRNAME";
	public static final String ADDRNAME_PHONETIC = "ADDRNAME_PHONETIC";
	public static final String PROVINCE = "PROVINCE";
	public static final String CITY = "CITY";
	public static final String COUNTY = "COUNTY";
	public static final String TOWN = "TOWN";
	public static final String PLACE = "PLACE";
	public static final String STREET = "STREET";
	public static final String LANDMARK = "LANDMARK";
	public static final String PREFIX = "PREFIX";
	public static final String HOUSENUM = "HOUSENUM";
	public static final String TYPE = "TYPE";
	public static final String SUBNUM = "SUBNUM";
	public static final String SURFIX = "SURFIX";
	public static final String ESTAB = "ESTAB";
	public static final String BUILDING = "BUILDING";
	public static final String FLOOR = "FLOOR";
	public static final String UNIT = "UNIT";
	public static final String ROOM = "ROOM";
	public static final String ADDONS = "ADDONS";
	public static final String PROV_PHONETIC = "PROV_PHONETIC";
	public static final String CITY_PHONETIC = "CITY_PHONETIC";
	public static final String COUNTY_PHONETIC = "COUNTY_PHONETIC";
	public static final String TOWN_PHONETIC = "TOWN_PHONETIC";
	public static final String STREET_PHONETIC = "STREET_PHONETIC";
	public static final String PLACE_PHONETIC = "PLACE_PHONETIC";
	public static final String LANDMARK_PHONETIC = "LANDMARK_PHONETIC";
	public static final String PREFIX_PHONETIC = "PREFIX_PHONETIC";
	public static final String HOUSENUM_PHONETIC = "HOUSENUM_PHONETIC";
	public static final String TYPE_PHONETIC = "TYPE_PHONETIC";
	public static final String SUBNUM_PHONETIC = "SUBNUM_PHONETIC";
	public static final String SURFIX_PHONETIC = "SURFIX_PHONETIC";
	public static final String ESTAB_PHONETIC = "ESTAB_PHONETIC";
	public static final String BUILDING_PHONETIC = "BUILDING_PHONETIC";
	public static final String FLOOR_PHONETIC = "FLOOR_PHONETIC";
	public static final String UNIT_PHONETIC = "UNIT_PHONETIC";
	public static final String ROOM_PHONETIC = "ROOM_PHONETIC";
	public static final String ADDONS_PHONETIC = "ADDONS_PHONETIC";
}
