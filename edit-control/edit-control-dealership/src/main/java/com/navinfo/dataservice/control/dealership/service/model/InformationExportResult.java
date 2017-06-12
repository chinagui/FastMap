package com.navinfo.dataservice.control.dealership.service.model;

public class InformationExportResult {
	public InformationExportResult() {
	}

	private String uuid;
	private String infoId;
	private String province;
	private String city;
	private String project;
	private String kindCode;
	private String chain;
	private String name;
	private String nameShort;
	private String address;
	private String telSale;
	private String telService;
	private String telOther;
	private String postCode;
	private String nameEng;
	private String addressEng;
	private String sourceId;
	private String dealSrcDiff;
	private String matchMethod;
	private String poiNum1;
	private String poiNum2;
	private String poiNum3;
	private String poiNum4;
	private String poiNum5;
	private String similarity;
	private String xLocate;
	private String yLocate;
	private String cfmPoiNum;
	private String cfmMemo;
	private String expectTime;
	private String infoType;
	private String infoLevel;
	private String regionId;

	// UUID
	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	// 情报ID
	public String getInfoId() {
		return this.infoId;
	}

	public void setInfoId(String infoId) {
		this.infoId = infoId;
	}

	// 省份
	public String getProvince() {
		return this.province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	// 城市
	public String getCity() {
		return this.city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	// 项目
	public String getProject() {
		return this.project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	// 代理店分类
	public String getKindCode() {
		return this.kindCode;
	}

	public void setKindCode(String kindCode) {
		this.kindCode = kindCode;
	}

	// 代理店品牌
	public String getChain() {
		return this.chain;
	}

	public void setChain(String chain) {
		this.chain = chain;
	}

	// 厂商提供名称
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// 厂商提供简称
	public String getNameShort() {
		return this.nameShort;
	}

	public void setNameShort(String nameShort) {
		this.nameShort = nameShort;
	}

	// 厂商提供地址
	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	// 厂商提供电话（销售）
	public String getTelSale() {
		return this.telSale;
	}

	public void setTelSale(String telSale) {
		this.telSale = telSale;
	}

	//厂商提供电话（服务）
	public String getTelService() {
		return this.telService;
	}

	public void setTelService(String telService) {
		this.telService = telService;
	}

	//厂商提供电话（其他）
	public String getTelOther() {
		return this.telOther;
	}

	public void setTelOther(String telOther) {
		this.telOther = telOther;
	}

	//厂商提供邮编
	public String getPostCode() {
		return this.postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	//厂商提供英文名称
	public String getNameEng() {
		return nameEng;
	}

	public void setNameEng(String nameEng) {
		this.nameEng = nameEng;
	}

	//厂商提供英文地址
	public String getAddressEng() {
		return this.addressEng;
	}

	public void setAddressEng(String addressEng) {
		this.addressEng = addressEng;
	}

	//旧一览表ID
	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	//新旧一览表差分结果
	public String getDealSrcDiff() {
		return this.dealSrcDiff;
	}

	public void setDealSrcDiff(String dealSrcDiff) {
		this.dealSrcDiff = dealSrcDiff;
	}

	//与POI的匹配方式
	public String getMatchMethod() {
		return this.matchMethod;
	}

	public void setMatchMethod(String matchMethod) {
		this.matchMethod = matchMethod;
	}

	//POI1_NUM
	public String getPoiNum1() {
		return this.poiNum1;
	}

	public void setPoiNum1(String poiNum1) {
		this.poiNum1 = poiNum1;
	}

	//POI2_NUM
	public String getPoiNum2() {
		return this.poiNum2;
	}

	public void setPoiNum2(String poiNum2) {
		this.poiNum2 = poiNum2;
	}

	//POI3_NUM
	public String getPoiNum3() {
		return this.poiNum3;
	}

	public void setPoiNum3(String poiNum3) {
		this.poiNum3 = poiNum3;
	}

	//POI4_NUM
	public String getPoiNum4() {
		return poiNum4;
	}

	public void setPoiNum4(String poiNum4) {
		this.poiNum4 = poiNum4;
	}

	//POI5_NUM
	public String getPoiNum5() {
		return this.poiNum5;
	}

	public void setPoiNum5(String poiNum5) {
		this.poiNum5 = poiNum5;
	}

	//匹配度
	public String getSimilarity() {
		return this.similarity;
	}

	public void setSimilarity(String similarity) {
		this.similarity = similarity;
	}

	//代理店显示坐标X
	public String getxLocate() {
		return this.xLocate;
	}

	public void setxLocate(String xLocate) {
		this.xLocate = xLocate;
	}

	//代理店显示坐标Y
	public String getyLocate() {
		return yLocate;
	}

	public void setyLocate(String yLocate) {
		this.yLocate = yLocate;
	}

	//待采纳POI外业采集号码
	public String getCfmPoiNum() {
		return cfmPoiNum;
	}

	public void setCfmPoiNum(String cfmPoiNum) {
		this.cfmPoiNum = cfmPoiNum;
	}

	//四维确认备注
	public String getCfmMemo() {
		return this.cfmMemo;
	}

	public void setCfmMemo(String cfmMemo) {
		this.cfmMemo = cfmMemo;
	}

	//期望时间
	public String getExpectTime() {
		return this.expectTime;
	}

	public void setExpectTime(String expectTime) {
		this.expectTime = expectTime;
	}

	//情报类型
	public String getInfoType() {
		return this.infoType;
	}

	public void setInfoType(String infoType) {
		this.infoType = infoType;
	}

	//情报等级
	public String getInfoLevel() {
		return this.infoLevel;
	}

	public void setInfoLevel(String infoLevel) {
		this.infoLevel = infoLevel;
	}

	//大区ID
	public String getRegionId() {
		return this.regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}
}
