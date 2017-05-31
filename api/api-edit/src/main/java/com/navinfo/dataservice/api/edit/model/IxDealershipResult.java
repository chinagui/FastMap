package com.navinfo.dataservice.api.edit.model;

/** 
* @ClassName:  IxDealershipResult 
* @author code generator
* @date 2017-05-27 03:27:47 
* @Description: TODO
*/
public class IxDealershipResult  {
	private int resultId ;
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
	private String cfmPoiNum ;
	private String cfmMemo ;
	private int sourceId ;
	private int dealSrcDiff ;
	private String dealCfmDate ;
	private String poiKindCode ;
	private String poiChain ;
	private String poiName ;
	private String poiNameShort ;
	private String poiAddress ;
	private String poiTel ;
	private String poiPostCode ;
	private int poiXDisplay ;
	private int poiYDisplay ;
	private int poiXGuide ;
	private int poiYGuide ;
	private Object geometry ;
	private int regionId ;
	
	public IxDealershipResult (){
	}
	
	public IxDealershipResult (int resultId ,int workflowStatus,int dealStatus,int userId,String toInfoDate,String toClientDate,String province,String city,String project,String kindCode,String chain,String name,String nameShort,String address,String telSale,String telService,String telOther,String postCode,String nameEng,String addressEng,String provideDate,int isDeleted,int matchMethod,String poiNum1,String poiNum2,String poiNum3,String poiNum4,String poiNum5,String similarity,int fbSource,String fbContent,String fbAuditRemark,String fbDate,int cfmStatus,String cfmPoiNum,String cfmMemo,int sourceId,int dealSrcDiff,String dealCfmDate,String poiKindCode,String poiChain,String poiName,String poiNameShort,String poiAddress,String poiTel,String poiPostCode,int poiXDisplay,int poiYDisplay,int poiXGuide,int poiYGuide,Object geometry,int regionId){
		this.resultId=resultId ;
		this.workflowStatus=workflowStatus ;
		this.dealStatus=dealStatus ;
		this.userId=userId ;
		this.toInfoDate=toInfoDate ;
		this.toClientDate=toClientDate ;
		this.province=province ;
		this.city=city ;
		this.project=project ;
		this.kindCode=kindCode ;
		this.chain=chain ;
		this.name=name ;
		this.nameShort=nameShort ;
		this.address=address ;
		this.telSale=telSale ;
		this.telService=telService ;
		this.telOther=telOther ;
		this.postCode=postCode ;
		this.nameEng=nameEng ;
		this.addressEng=addressEng ;
		this.provideDate=provideDate ;
		this.isDeleted=isDeleted ;
		this.matchMethod=matchMethod ;
		this.poiNum1=poiNum1 ;
		this.poiNum2=poiNum2 ;
		this.poiNum3=poiNum3 ;
		this.poiNum4=poiNum4 ;
		this.poiNum5=poiNum5 ;
		this.similarity=similarity ;
		this.fbSource=fbSource ;
		this.fbContent=fbContent ;
		this.fbAuditRemark=fbAuditRemark ;
		this.fbDate=fbDate ;
		this.cfmStatus=cfmStatus ;
		this.cfmPoiNum=cfmPoiNum ;
		this.cfmMemo=cfmMemo ;
		this.sourceId=sourceId ;
		this.dealSrcDiff=dealSrcDiff ;
		this.dealCfmDate=dealCfmDate ;
		this.poiKindCode=poiKindCode ;
		this.poiChain=poiChain ;
		this.poiName=poiName ;
		this.poiNameShort=poiNameShort ;
		this.poiAddress=poiAddress ;
		this.poiTel=poiTel ;
		this.poiPostCode=poiPostCode ;
		this.poiXDisplay=poiXDisplay ;
		this.poiYDisplay=poiYDisplay ;
		this.poiXGuide=poiXGuide ;
		this.poiYGuide=poiYGuide ;
		this.geometry=geometry ;
		this.regionId=regionId ;
	}
	public int getResultId() {
		return resultId;
	}
	public void setResultId(int resultId) {
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
	public int getSourceId() {
		return sourceId;
	}
	public void setSourceId(int sourceId) {
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
	public int getPoiXDisplay() {
		return poiXDisplay;
	}
	public void setPoiXDisplay(int poiXDisplay) {
		this.poiXDisplay = poiXDisplay;
	}
	public int getPoiYDisplay() {
		return poiYDisplay;
	}
	public void setPoiYDisplay(int poiYDisplay) {
		this.poiYDisplay = poiYDisplay;
	}
	public int getPoiXGuide() {
		return poiXGuide;
	}
	public void setPoiXGuide(int poiXGuide) {
		this.poiXGuide = poiXGuide;
	}
	public int getPoiYGuide() {
		return poiYGuide;
	}
	public void setPoiYGuide(int poiYGuide) {
		this.poiYGuide = poiYGuide;
	}
	public Object getGeometry() {
		return geometry;
	}
	public void setGeometry(Object geometry) {
		this.geometry = geometry;
	}
	public int getRegionId() {
		return regionId;
	}
	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IxDealershipResult [resultId=" + resultId +",workflowStatus="+workflowStatus+",dealStatus="+dealStatus+",userId="+userId+",toInfoDate="+toInfoDate+",toClientDate="+toClientDate+",province="+province+",city="+city+",project="+project+",kindCode="+kindCode+",chain="+chain+",name="+name+",nameShort="+nameShort+",address="+address+",telSale="+telSale+",telService="+telService+",telOther="+telOther+",postCode="+postCode+",nameEng="+nameEng+",addressEng="+addressEng+",provideDate="+provideDate+",isDeleted="+isDeleted+",matchMethod="+matchMethod+",poiNum1="+poiNum1+",poiNum2="+poiNum2+",poiNum3="+poiNum3+",poiNum4="+poiNum4+",poiNum5="+poiNum5+",similarity="+similarity+",fbSource="+fbSource+",fbContent="+fbContent+",fbAuditRemark="+fbAuditRemark+",fbDate="+fbDate+",cfmStatus="+cfmStatus+",cfmPoiNum="+cfmPoiNum+",cfmMemo="+cfmMemo+",sourceId="+sourceId+",dealSrcDiff="+dealSrcDiff+",dealCfmDate="+dealCfmDate+",poiKindCode="+poiKindCode+",poiChain="+poiChain+",poiName="+poiName+",poiNameShort="+poiNameShort+",poiAddress="+poiAddress+",poiTel="+poiTel+",poiPostCode="+poiPostCode+",poiXDisplay="+poiXDisplay+",poiYDisplay="+poiYDisplay+",poiXGuide="+poiXGuide+",poiYGuide="+poiYGuide+",geometry="+geometry+",regionId="+regionId+"]";
	}


	
}
