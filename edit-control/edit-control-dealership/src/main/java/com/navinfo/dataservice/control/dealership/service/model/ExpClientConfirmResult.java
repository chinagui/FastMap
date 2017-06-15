package com.navinfo.dataservice.control.dealership.service.model;

/**
 * 客户确认导出
 * ExpClientConfirmResult
 * @author test
 *
 */
public class ExpClientConfirmResult  {
	private Integer resultId ;
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
	private Integer poiPid;
	private String fid;
	private String poiName;
	private String poiAliasName;
	private String poiKindCode;
	private String poiChain;
	private String poiAddress;
	private String poiContact;
	private String poiPostCode;
	private String workFlowStatus;
	private String dealSrcDiff;
	private String cfmMemo;
	
	
	public ExpClientConfirmResult(){
		
	}


	public ExpClientConfirmResult(Integer resultId, String province, String city, String project, String kindCode,
			String chain, String name, String nameShort, String address, String telSale, String telService,
			String telOther, String postCode, String nameEng, String addressEng, Integer poiPid, String fid,
			String poiName, String poiAliasName, String poiKindCode, String poiChain, String poiAddress,
			String poiContact, String poiPostCode, String workFlowStatus, String dealSrcDiff, String cfmMemo) {
		super();
		this.resultId = resultId;
		this.province = province;
		this.city = city;
		this.project = project;
		this.kindCode = kindCode;
		this.chain = chain;
		this.name = name;
		this.nameShort = nameShort;
		this.address = address;
		this.telSale = telSale;
		this.telService = telService;
		this.telOther = telOther;
		this.postCode = postCode;
		this.nameEng = nameEng;
		this.addressEng = addressEng;
		this.poiPid = poiPid;
		this.fid = fid;
		this.poiName = poiName;
		this.poiAliasName = poiAliasName;
		this.poiKindCode = poiKindCode;
		this.poiChain = poiChain;
		this.poiAddress = poiAddress;
		this.poiContact = poiContact;
		this.poiPostCode = poiPostCode;
		this.workFlowStatus = workFlowStatus;
		this.dealSrcDiff = dealSrcDiff;
		this.cfmMemo = cfmMemo;
	}


	public Integer getResultId() {
		return resultId;
	}


	public void setResultId(Integer resultId) {
		this.resultId = resultId;
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


	public Integer getPoiPid() {
		return poiPid;
	}


	public void setPoiPid(Integer poiPid) {
		this.poiPid = poiPid;
	}


	public String getFid() {
		return fid;
	}


	public void setFid(String fid) {
		this.fid = fid;
	}


	public String getPoiName() {
		return poiName;
	}


	public void setPoiName(String poiName) {
		this.poiName = poiName;
	}


	public String getPoiAliasName() {
		return poiAliasName;
	}


	public void setPoiAliasName(String poiAliasName) {
		this.poiAliasName = poiAliasName;
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


	public String getPoiAddress() {
		return poiAddress;
	}


	public void setPoiAddress(String poiAddress) {
		this.poiAddress = poiAddress;
	}


	public String getPoiContact() {
		return poiContact;
	}


	public void setPoiContact(String poiContact) {
		this.poiContact = poiContact;
	}


	public String getPoiPostCode() {
		return poiPostCode;
	}


	public void setPoiPostCode(String poiPostCode) {
		this.poiPostCode = poiPostCode;
	}


	public String getWorkFlowStatus() {
		return workFlowStatus;
	}


	public void setWorkFlowStatus(String workFlowStatus) {
		this.workFlowStatus = workFlowStatus;
	}


	public String getDealSrcDiff() {
		return dealSrcDiff;
	}


	public void setDealSrcDiff(String dealSrcDiff) {
		this.dealSrcDiff = dealSrcDiff;
	}


	public String getCfmMemo() {
		return cfmMemo;
	}


	public void setCfmMemo(String cfmMemo) {
		this.cfmMemo = cfmMemo;
	}
	
	


	
}
