package com.navinfo.dataservice.control.dealership.service.excelModel;

/**
 * @Title: AdditionResultEntity
 * @Package: com.navinfo.dataservice.control.dealership.service.excelModel
 * @Description:
 * @Author: LittleDog
 * @Date: 2017年9月18日
 * @Version: V1.0
 */
public class AdditionResultEntity {

	private Integer id;// 序号
	private Integer sourceId;// 一览表ID
	private String cfmPoiNum;// IDCODE
	private String chainName;// 品牌
	private String kindCode;// 分类
	private String chain;// CHAIN
	private String province;// 省/直辖市
	private String city;// 市
	private String county;// 县/区------空
	private String name;// 厂商提供名称
	private String poiName;// 四维录入名称
	private String namePhonetic;// 名称拼音
	private String englishName;// 厂商提供英文名称
	private String navEnglishName;// 四维录入英文全称
	private String navEnglishShortName;// 四维录入英文简称
	private String address;// 厂商提供地址
	private String poiAddress;// 四维录入地址

	private String addressProvince;// 地址省名
	private String addressCity;// 地址市名
	private String addressCounty;// 地址区县名
	private String addressTown;// 地址乡镇街道办
	private String addressPlace;// 地址地名小区名
	private String addressStreet;// 地址街巷名
	private String addressLandMark;// 地址标志物名
	private String addressPrefix;// 地址前缀
	private String addressHouseNum;// 地址门牌号
	private String addressType;// 地址类型名
	private String addressSubNum;// 地址子号
	private String addressSurfix;// 地址后缀
	private String addressEstab;// 地址附属设施名
	private String addressBuilding;// 地址楼栋号
	private String addressFloor;// 地址楼层
	private String addressUnit;// 地址楼门号
	private String addressRoom;// 地址房间号
	private String addressAddons;// 地址附加信息

	private String addressProvincePhonetic;// 地址省名发音
	private String addressCityPhonetic;// 地址市名发音
	private String addressCountyPhonetic;// 地址区县名发音
	private String addressTownPhonetic;// 地址乡镇街道办发音
	private String addressPlacePhonetic;// 地址地名小区名发音
	private String addressStreetPhonetic;// 地址街巷名发音
	private String addressLandMarkPhonetic;// 地址标志物名发音
	private String addressPrefixPhonetic;// 地址前缀发音
	private String addressHouseNumPhonetic;// 地址门牌号发音
	private String addressTypePhonetic;// 地址类型名发音
	private String addressSubNumPhonetic;// 地址子号发音
	private String addressSurfixPhonetic;// 地址后缀发音
	private String addressEstabPhonetic;// 地址附属设施名发音
	private String addressBuildingPhonetic;// 地址楼栋号发音
	private String addressFloorPhonetic;// 地址楼层发音
	private String addressUnitPhonetic;// 地址楼门号发音
	private String addressRoomPhonetic;// 地址房间号发音
	private String addressAddonsPhonetic;// 地址附加信息发音

	private String englishAddress;// 厂商提供英文地址
	private String navFullName;// 四维录入英文地址
	private String telephone;// 厂商提供电话
	private String navPoiTel;// 四维录入电话
	private String telephonePriority;// 电话优先级
	private String telephoneType;// 电话类型
	private String postCode;// 厂商提供邮编
	private String navPoiChain;// 四维录入CHAIN
	private String navPoiPostCode;// 四维录入邮编
	private String nameShort;// 厂商提供别名
	private String navPoiNameShort;// 四维录入别名
	private String navOriginalName;// 四维录入别名原始英文
	private String navStandardName;// 四维录入别名标准化英文
	private String navAnotherName;// 别名拼音
	private Integer isDelete;// 是否删除记录
	private String project;// 项目
	private String personId;// 反馈人ID------空
	private String fbContent;// 负责人反馈结果
	private String fbAuditRemark;// 审核意见
	private String fbDate;// 反馈时间
	private String dealCfmDate;// 一览表确认时间
	private String cfmMemo;// 备注

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getSourceId() {
		return sourceId;
	}

	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}

	public String getCfmPoiNum() {
		return cfmPoiNum;
	}

	public void setCfmPoiNum(String cfmPoiNum) {
		this.cfmPoiNum = cfmPoiNum;
	}

	public String getChainName() {
		return chainName;
	}

	public void setChainName(String chainName) {
		this.chainName = chainName;
	}

	public String getKindCode() {
		return kindCode;
	}

	public void setKindCode(String kindCode) {
		this.kindCode = kindCode;
	}

	public String getChain() {
		return chain;
	}

	public void setChain(String chain) {
		this.chain = chain;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPoiName() {
		return poiName;
	}

	public void setPoiName(String poiName) {
		this.poiName = poiName;
	}

	public String getNamePhonetic() {
		return namePhonetic;
	}

	public void setNamePhonetic(String namePhonetic) {
		this.namePhonetic = namePhonetic;
	}

	public String getEnglishName() {
		return englishName;
	}

	public void setEnglishName(String englishName) {
		this.englishName = englishName;
	}

	public String getNavEnglishName() {
		return navEnglishName;
	}

	public void setNavEnglishName(String navEnglishName) {
		this.navEnglishName = navEnglishName;
	}

	public String getNavEnglishShortName() {
		return navEnglishShortName;
	}

	public void setNavEnglishShortName(String navEnglishShortName) {
		this.navEnglishShortName = navEnglishShortName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPoiAddress() {
		return poiAddress;
	}

	public void setPoiAddress(String poiAddress) {
		this.poiAddress = poiAddress;
	}

	public String getAddressProvince() {
		return addressProvince;
	}

	public void setAddressProvince(String addressProvince) {
		this.addressProvince = addressProvince;
	}

	public String getAddressCity() {
		return addressCity;
	}

	public void setAddressCity(String addressCity) {
		this.addressCity = addressCity;
	}

	public String getAddressCounty() {
		return addressCounty;
	}

	public void setAddressCounty(String addressCounty) {
		this.addressCounty = addressCounty;
	}

	public String getAddressTown() {
		return addressTown;
	}

	public void setAddressTown(String addressTown) {
		this.addressTown = addressTown;
	}

	public String getAddressPlace() {
		return addressPlace;
	}

	public void setAddressPlace(String addressPlace) {
		this.addressPlace = addressPlace;
	}

	public String getAddressStreet() {
		return addressStreet;
	}

	public void setAddressStreet(String addressStreet) {
		this.addressStreet = addressStreet;
	}

	public String getAddressLandMark() {
		return addressLandMark;
	}

	public void setAddressLandMark(String addressLandMark) {
		this.addressLandMark = addressLandMark;
	}

	public String getAddressPrefix() {
		return addressPrefix;
	}

	public void setAddressPrefix(String addressPrefix) {
		this.addressPrefix = addressPrefix;
	}

	public String getAddressHouseNum() {
		return addressHouseNum;
	}

	public void setAddressHouseNum(String addressHouseNum) {
		this.addressHouseNum = addressHouseNum;
	}

	public String getAddressType() {
		return addressType;
	}

	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}

	public String getAddressSubNum() {
		return addressSubNum;
	}

	public void setAddressSubNum(String addressSubNum) {
		this.addressSubNum = addressSubNum;
	}

	public String getAddressSurfix() {
		return addressSurfix;
	}

	public void setAddressSurfix(String addressSurfix) {
		this.addressSurfix = addressSurfix;
	}

	public String getAddressEstab() {
		return addressEstab;
	}

	public void setAddressEstab(String addressEstab) {
		this.addressEstab = addressEstab;
	}

	public String getAddressBuilding() {
		return addressBuilding;
	}

	public void setAddressBuilding(String addressBuilding) {
		this.addressBuilding = addressBuilding;
	}

	public String getAddressFloor() {
		return addressFloor;
	}

	public void setAddressFloor(String addressFloor) {
		this.addressFloor = addressFloor;
	}

	public String getAddressUnit() {
		return addressUnit;
	}

	public void setAddressUnit(String addressUnit) {
		this.addressUnit = addressUnit;
	}

	public String getAddressRoom() {
		return addressRoom;
	}

	public void setAddressRoom(String addressRoom) {
		this.addressRoom = addressRoom;
	}

	public String getAddressAddons() {
		return addressAddons;
	}

	public void setAddressAddons(String addressAddons) {
		this.addressAddons = addressAddons;
	}

	public String getAddressProvincePhonetic() {
		return addressProvincePhonetic;
	}

	public void setAddressProvincePhonetic(String addressProvincePhonetic) {
		this.addressProvincePhonetic = addressProvincePhonetic;
	}

	public String getAddressCityPhonetic() {
		return addressCityPhonetic;
	}

	public void setAddressCityPhonetic(String addressCityPhonetic) {
		this.addressCityPhonetic = addressCityPhonetic;
	}

	public String getAddressCountyPhonetic() {
		return addressCountyPhonetic;
	}

	public void setAddressCountyPhonetic(String addressCountyPhonetic) {
		this.addressCountyPhonetic = addressCountyPhonetic;
	}

	public String getAddressTownPhonetic() {
		return addressTownPhonetic;
	}

	public void setAddressTownPhonetic(String addressTownPhonetic) {
		this.addressTownPhonetic = addressTownPhonetic;
	}

	public String getAddressPlacePhonetic() {
		return addressPlacePhonetic;
	}

	public void setAddressPlacePhonetic(String addressPlacePhonetic) {
		this.addressPlacePhonetic = addressPlacePhonetic;
	}

	public String getAddressStreetPhonetic() {
		return addressStreetPhonetic;
	}

	public void setAddressStreetPhonetic(String addressStreetPhonetic) {
		this.addressStreetPhonetic = addressStreetPhonetic;
	}

	public String getAddressLandMarkPhonetic() {
		return addressLandMarkPhonetic;
	}

	public void setAddressLandMarkPhonetic(String addressLandMarkPhonetic) {
		this.addressLandMarkPhonetic = addressLandMarkPhonetic;
	}

	public String getAddressPrefixPhonetic() {
		return addressPrefixPhonetic;
	}

	public void setAddressPrefixPhonetic(String addressPrefixPhonetic) {
		this.addressPrefixPhonetic = addressPrefixPhonetic;
	}

	public String getAddressHouseNumPhonetic() {
		return addressHouseNumPhonetic;
	}

	public void setAddressHouseNumPhonetic(String addressHouseNumPhonetic) {
		this.addressHouseNumPhonetic = addressHouseNumPhonetic;
	}

	public String getAddressTypePhonetic() {
		return addressTypePhonetic;
	}

	public void setAddressTypePhonetic(String addressTypePhonetic) {
		this.addressTypePhonetic = addressTypePhonetic;
	}

	public String getAddressSubNumPhonetic() {
		return addressSubNumPhonetic;
	}

	public void setAddressSubNumPhonetic(String addressSubNumPhonetic) {
		this.addressSubNumPhonetic = addressSubNumPhonetic;
	}

	public String getAddressSurfixPhonetic() {
		return addressSurfixPhonetic;
	}

	public void setAddressSurfixPhonetic(String addressSurfixPhonetic) {
		this.addressSurfixPhonetic = addressSurfixPhonetic;
	}

	public String getAddressEstabPhonetic() {
		return addressEstabPhonetic;
	}

	public void setAddressEstabPhonetic(String addressEstabPhonetic) {
		this.addressEstabPhonetic = addressEstabPhonetic;
	}

	public String getAddressBuildingPhonetic() {
		return addressBuildingPhonetic;
	}

	public void setAddressBuildingPhonetic(String addressBuildingPhonetic) {
		this.addressBuildingPhonetic = addressBuildingPhonetic;
	}

	public String getAddressFloorPhonetic() {
		return addressFloorPhonetic;
	}

	public void setAddressFloorPhonetic(String addressFloorPhonetic) {
		this.addressFloorPhonetic = addressFloorPhonetic;
	}

	public String getAddressUnitPhonetic() {
		return addressUnitPhonetic;
	}

	public void setAddressUnitPhonetic(String addressUnitPhonetic) {
		this.addressUnitPhonetic = addressUnitPhonetic;
	}

	public String getAddressRoomPhonetic() {
		return addressRoomPhonetic;
	}

	public void setAddressRoomPhonetic(String addressRoomPhonetic) {
		this.addressRoomPhonetic = addressRoomPhonetic;
	}

	public String getAddressAddonsPhonetic() {
		return addressAddonsPhonetic;
	}

	public void setAddressAddonsPhonetic(String addressAddonsPhonetic) {
		this.addressAddonsPhonetic = addressAddonsPhonetic;
	}

	public String getEnglishAddress() {
		return englishAddress;
	}

	public void setEnglishAddress(String englishAddress) {
		this.englishAddress = englishAddress;
	}

	public String getNavFullName() {
		return navFullName;
	}

	public void setNavFullName(String navFullName) {
		this.navFullName = navFullName;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getNavPoiTel() {
		return navPoiTel;
	}

	public void setNavPoiTel(String navPoiTel) {
		this.navPoiTel = navPoiTel;
	}

	public String getTelephonePriority() {
		return telephonePriority;
	}

	public void setTelephonePriority(String telephonePriority) {
		this.telephonePriority = telephonePriority;
	}

	public String getTelephoneType() {
		return telephoneType;
	}

	public void setTelephoneType(String telephoneType) {
		this.telephoneType = telephoneType;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public String getNavPoiChain() {
		return navPoiChain;
	}

	public void setNavPoiChain(String navPoiChain) {
		this.navPoiChain = navPoiChain;
	}

	public String getNavPoiPostCode() {
		return navPoiPostCode;
	}

	public void setNavPoiPostCode(String navPoiPostCode) {
		this.navPoiPostCode = navPoiPostCode;
	}

	public String getNameShort() {
		return nameShort;
	}

	public void setNameShort(String nameShort) {
		this.nameShort = nameShort;
	}

	public String getNavPoiNameShort() {
		return navPoiNameShort;
	}

	public void setNavPoiNameShort(String navPoiNameShort) {
		this.navPoiNameShort = navPoiNameShort;
	}

	public String getNavOriginalName() {
		return navOriginalName;
	}

	public void setNavOriginalName(String navOriginalName) {
		this.navOriginalName = navOriginalName;
	}

	public String getNavStandardName() {
		return navStandardName;
	}

	public void setNavStandardName(String navStandardName) {
		this.navStandardName = navStandardName;
	}

	public String getNavAnotherName() {
		return navAnotherName;
	}

	public void setNavAnotherName(String navAnotherName) {
		this.navAnotherName = navAnotherName;
	}

	public Integer getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(Integer isDelete) {
		this.isDelete = isDelete;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getFbContent() {
		return fbContent;
	}

	public void setFbContent(String fbContent) {
		this.fbContent = fbContent;
	}

	public String getFbAuditRemark() {
		return fbAuditRemark;
	}

	public void setFbAuditRemark(String fbAuditRemark) {
		this.fbAuditRemark = fbAuditRemark;
	}

	public String getFbDate() {
		return fbDate;
	}

	public void setFbDate(String fbDate) {
		this.fbDate = fbDate;
	}

	public String getDealCfmDate() {
		return dealCfmDate;
	}

	public void setDealCfmDate(String dealCfmDate) {
		this.dealCfmDate = dealCfmDate;
	}

	public String getCfmMemo() {
		return cfmMemo;
	}

	public void setCfmMemo(String cfmMemo) {
		this.cfmMemo = cfmMemo;
	}

}
