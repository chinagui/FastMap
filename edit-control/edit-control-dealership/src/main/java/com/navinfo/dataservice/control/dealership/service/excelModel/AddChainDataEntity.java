package com.navinfo.dataservice.control.dealership.service.excelModel;

import com.vividsolutions.jts.geom.Geometry;

import oracle.spatial.geometry.JGeometry;

/**
 * 增量更新数据
 * 
 * */
public class AddChainDataEntity {
	private Integer resultId ;
	private int workflowStatus ;
	private int dealStatus ;
	private int userId ;
	private String toInfoDate ;
	private String toClientDate ;
	private String province ;
	private String city ;
	private String project ;
	private String kindCode ;
	private String chain ;
	private String name ;
	private String nameShort ;
	private String address ;
	private String telSale ;
	private String telService ;
	private String telOther ;
	private String postCode ;
	private String nameEng ;
	private String addressEng ;
	private String provideDate ;
	private int isDeleted ;
	private int matchMethod ;
	private String poiNum1 ;
	private String poiNum2 ;
	private String poiNum3 ;
	private String poiNum4 ;
	private String poiNum5 ;
	private String similarity ;
	private int fbSource ;
	private String fbContent ;
	private String fbAuditRemark ;
	private String fbDate ;
	private int cfmStatus ;
	private int cfmIsAdopted ;
	private String cfmPoiNum ;
	private String cfmMemo ;
	private Integer sourceId ;
	private int dealSrcDiff ;
	private String dealCfmDate ;
	private String poiKindCode ;
	private String poiChain ;
	private String poiName ;
	private String poiNameShort ;
	private String poiAddress ;
	private String poiTel ;
	private String poiPostCode ;
	private double poiXDisplay ;
	private double poiYDisplay ;
	private double poiXGuide ;
	private double poiYGuide ;
	private Geometry geometry ;
	private Integer regionId ;
	public Integer getResultId() {
		return resultId;
	}
	public void setResultId(Integer resultId) {
		this.resultId = resultId;
	}
	public int getWorkflowStatus() {
		return workflowStatus;
	}
	public void setWorkflowStatus(int workflowStatus) {
		this.workflowStatus = workflowStatus;
	}
	public int getDealStatus() {
		return dealStatus;
	}
	public void setDealStatus(int dealStatus) {
		this.dealStatus = dealStatus;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getToInfoDate() {
		return toInfoDate;
	}
	public void setToInfoDate(String toInfoDate) {
		this.toInfoDate = toInfoDate;
	}
	public String getToClientDate() {
		return toClientDate;
	}
	public void setToClientDate(String toClientDate) {
		this.toClientDate = toClientDate;
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
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNameShort() {
		return nameShort;
	}
	public void setNameShort(String nameShort) {
		this.nameShort = nameShort;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getTelSale() {
		return telSale;
	}
	public void setTelSale(String telSale) {
		this.telSale = telSale;
	}
	public String getTelService() {
		return telService;
	}
	public void setTelService(String telService) {
		this.telService = telService;
	}
	public String getTelOther() {
		return telOther;
	}
	public void setTelOther(String telOther) {
		this.telOther = telOther;
	}
	public String getPostCode() {
		return postCode;
	}
	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}
	public String getNameEng() {
		return nameEng;
	}
	public void setNameEng(String nameEng) {
		this.nameEng = nameEng;
	}
	public String getAddressEng() {
		return addressEng;
	}
	public void setAddressEng(String addressEng) {
		this.addressEng = addressEng;
	}
	public String getProvideDate() {
		return provideDate;
	}
	public void setProvideDate(String provideDate) {
		this.provideDate = provideDate;
	}
	public int getIsDeleted() {
		return isDeleted;
	}
	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}
	public int getMatchMethod() {
		return matchMethod;
	}
	public void setMatchMethod(int matchMethod) {
		this.matchMethod = matchMethod;
	}
	public String getPoiNum1() {
		return poiNum1;
	}
	public void setPoiNum1(String poiNum1) {
		this.poiNum1 = poiNum1;
	}
	public String getPoiNum2() {
		return poiNum2;
	}
	public void setPoiNum2(String poiNum2) {
		this.poiNum2 = poiNum2;
	}
	public String getPoiNum3() {
		return poiNum3;
	}
	public void setPoiNum3(String poiNum3) {
		this.poiNum3 = poiNum3;
	}
	public String getPoiNum4() {
		return poiNum4;
	}
	public void setPoiNum4(String poiNum4) {
		this.poiNum4 = poiNum4;
	}
	public String getPoiNum5() {
		return poiNum5;
	}
	public void setPoiNum5(String poiNum5) {
		this.poiNum5 = poiNum5;
	}
	public String getSimilarity() {
		return similarity;
	}
	public void setSimilarity(String similarity) {
		this.similarity = similarity;
	}
	public int getFbSource() {
		return fbSource;
	}
	public void setFbSource(int fbSource) {
		this.fbSource = fbSource;
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
	public int getCfmStatus() {
		return cfmStatus;
	}
	public void setCfmStatus(int cfmStatus) {
		this.cfmStatus = cfmStatus;
	}
	public int getCfmIsAdopted() {
		return cfmIsAdopted;
	}
	public void setCfmIsAdopted(int cfmIsAdopted) {
		this.cfmIsAdopted = cfmIsAdopted;
	}
	public String getCfmPoiNum() {
		return cfmPoiNum;
	}
	public void setCfmPoiNum(String cfmPoiNum) {
		this.cfmPoiNum = cfmPoiNum;
	}
	public String getCfmMemo() {
		return cfmMemo;
	}
	public void setCfmMemo(String cfmMemo) {
		this.cfmMemo = cfmMemo;
	}
	public Integer getSourceId() {
		return sourceId;
	}
	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}
	public int getDealSrcDiff() {
		return dealSrcDiff;
	}
	public void setDealSrcDiff(int dealSrcDiff) {
		this.dealSrcDiff = dealSrcDiff;
	}
	public String getDealCfmDate() {
		return dealCfmDate;
	}
	public void setDealCfmDate(String dealCfmDate) {
		this.dealCfmDate = dealCfmDate;
	}
	public String getPoiKindCode() {
		return poiKindCode;
	}
	public void setPoiKindCode(String poiKindCode) {
		this.poiKindCode = poiKindCode;
	}
	public String getPoiChain() {
		return poiChain;
	}
	public void setPoiChain(String poiChain) {
		this.poiChain = poiChain;
	}
	public String getPoiName() {
		return poiName;
	}
	public void setPoiName(String poiName) {
		this.poiName = poiName;
	}
	public String getPoiNameShort() {
		return poiNameShort;
	}
	public void setPoiNameShort(String poiNameShort) {
		this.poiNameShort = poiNameShort;
	}
	public String getPoiAddress() {
		return poiAddress;
	}
	public void setPoiAddress(String poiAddress) {
		this.poiAddress = poiAddress;
	}
	public String getPoiTel() {
		return poiTel;
	}
	public void setPoiTel(String poiTel) {
		this.poiTel = poiTel;
	}
	public String getPoiPostCode() {
		return poiPostCode;
	}
	public void setPoiPostCode(String poiPostCode) {
		this.poiPostCode = poiPostCode;
	}
	public double getPoiXDisplay() {
		return poiXDisplay;
	}
	public void setPoiXDisplay(double poiXDisplay) {
		this.poiXDisplay = poiXDisplay;
	}
	public double getPoiYDisplay() {
		return poiYDisplay;
	}
	public void setPoiYDisplay(double poiYDisplay) {
		this.poiYDisplay = poiYDisplay;
	}
	public double getPoiXGuide() {
		return poiXGuide;
	}
	public void setPoiXGuide(double poiXGuide) {
		this.poiXGuide = poiXGuide;
	}
	public double getPoiYGuide() {
		return poiYGuide;
	}
	public void setPoiYGuide(double poiYGuide) {
		this.poiYGuide = poiYGuide;
	}
	public Geometry getGeometry() {
		return geometry;
	}
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	public Integer getRegionId() {
		return regionId;
	}
	public void setRegionId(Integer regionId) {
		this.regionId = regionId;
	}
	
}
