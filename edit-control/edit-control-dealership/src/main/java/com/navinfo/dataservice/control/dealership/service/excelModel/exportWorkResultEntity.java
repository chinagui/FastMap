package com.navinfo.dataservice.control.dealership.service.excelModel;

/**
 * 客户确认结果导出实体
 * @author songhe
 * 
 * */
public class exportWorkResultEntity {
	
	private Integer id;
	private Integer sourceId;
	private String cfmPoiNum;
	private String chainName;
	private String kindCode;
	private String chain;
	private String province;
	private String city;
	private String block;
	private String name;
	private String poiName;
	private String englishName;
	private String navEnglishName;
	private String navEnglishShortName;
	private String address;
	private String poiAddress;
	private String addressEnglish;
	private String navAddressEnglish;
	private String venderTel;
	private String poiTel;
	private String postCode;
	private String poiChain;
	private String poiPostCode;
	private String shortName;
	private String poiNameShort;
	private String navOtherEngName;
	private String navStantardEngName;
	private Integer isDeleted;
	private String project;
	private String personId;
	private String fbContnt;
	private String auditRemark;
	private String fbDate;
	private String dealCfmDate;
	private String cfmMemo;
	
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
		nullToString(cfmPoiNum);
		this.cfmPoiNum = cfmPoiNum;
	}
	public String getChainName() {
		return chainName;
	}
	public void setChainName(String chainName) {
		nullToString(chainName);
		this.chainName = chainName;
	}
	public String getKindCode() {
		return kindCode;
	}
	public void setKindCode(String kindCode) {
		nullToString(kindCode);
		this.kindCode = kindCode;
	}
	public String getChain() {
		return chain;
	}
	public void setChain(String chain) {
		nullToString(chain);
		this.chain = chain;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		nullToString(province);
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		nullToString(city);
		this.city = city;
	}
	public String getBlock() {
		return block;
	}
	public void setBlock(String block) {
		nullToString(block);
		this.block = block;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		nullToString(name);
		this.name = name;
	}
	public String getPoiName() {
		return poiName;
	}
	public void setPoiName(String poiName) {
		nullToString(poiName);
		this.poiName = poiName;
	}
	public String getEnglishName() {
		return englishName;
	}
	public void setEnglishName(String englishName) {
		nullToString(englishName);
		this.englishName = englishName;
	}
	public String getNavEnglishName() {
		return navEnglishName;
	}
	public void setNavEnglishName(String navEnglishName) {
		nullToString(navEnglishName);
		this.navEnglishName = navEnglishName;
	}
	public String getNavEnglishShortName() {
		return navEnglishShortName;
	}
	public void setNavEnglishShortName(String navEnglishShortName) {
		nullToString(navEnglishShortName);
		this.navEnglishShortName = navEnglishShortName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		nullToString(address);
		this.address = address;
	}
	public String getPoiAddress() {
		return poiAddress;
	}
	public void setPoiAddress(String poiAddress) {
		nullToString(poiAddress);
		this.poiAddress = poiAddress;
	}
	public String getAddressEnglish() {
		return addressEnglish;
	}
	public void setAddressEnglish(String addressEnglish) {
		nullToString(addressEnglish);
		this.addressEnglish = addressEnglish;
	}
	public String getNavAddressEnglish() {
		return navAddressEnglish;
	}
	public void setNavAddressEnglish(String navAddressEnglish) {
		nullToString(navAddressEnglish);
		this.navAddressEnglish = navAddressEnglish;
	}
	public String getVenderTel() {
		return venderTel;
	}
	public void setVenderTel(String telephoneSale,String telephoneService, String telephoneOther) {
		nullToString(telephoneSale);
		nullToString(telephoneService);
		nullToString(telephoneOther);
		this.venderTel = telephoneSale+telephoneService+telephoneOther;
	}
	public String getPoiTel() {
		return poiTel;
	}
	public void setPoiTel(String poiTel) {
		nullToString(poiTel);
		this.poiTel = poiTel;
	}
	public String getPostCode() {
		return postCode;
	}
	public void setPostCode(String postCode) {
		nullToString(postCode);
		this.postCode = postCode;
	}
	public String getPoiChain() {
		return poiChain;
	}
	public void setPoiChain(String poiChain) {
		nullToString(poiChain);
		this.poiChain = poiChain;
	}
	public String getPoiPostCode() {
		return poiPostCode;
	}
	public void setPoiPostCode(String poiPostCode) {
		nullToString(poiPostCode);
		this.poiPostCode = poiPostCode;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		nullToString(shortName);
		this.shortName = shortName;
	}
	public String getPoiNameShort() {
		return poiNameShort;
	}
	public void setPoiNameShort(String poiNameShort) {
		nullToString(poiNameShort);
		this.poiNameShort = poiNameShort;
	}
	public String getNavOtherEngName() {
		return navOtherEngName;
	}
	public void setNavOtherEngName(String navOtherEngName) {
		nullToString(navOtherEngName);
		this.navOtherEngName = navOtherEngName;
	}
	public String getNavStantardEngName() {
		return navStantardEngName;
	}
	public void setNavStantardEngName(String navStantardEngName) {
		nullToString(navStantardEngName);
		this.navStantardEngName = navStantardEngName;
	}
	public Integer getIsDeleted() {
		return isDeleted;
	}
	public void setIsDeleted(Integer isDeleted) {
		this.isDeleted = isDeleted;
	}
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		nullToString(project);
		this.project = project;
	}
	public String getPersonId() {
		return personId;
	}
	public void setPersonId(String personId) {
		nullToString(personId);
		this.personId = personId;
	}
	public String getFbContnt() {
		return fbContnt;
	}
	public void setFbContnt(String fbContnt) {
		nullToString(fbContnt);
		this.fbContnt = fbContnt;
	}
	public String getAuditRemark() {
		return auditRemark;
	}
	public void setAuditRemark(String auditRemark) {
		nullToString(auditRemark);
		this.auditRemark = auditRemark;
	}
	public String getFbDate() {
		return fbDate;
	}
	public void setFbDate(String fbDate) {
		nullToString(fbDate);
		this.fbDate = fbDate;
	}
	public String getDealCfmDate() {
		return dealCfmDate;
	}
	public void setDealCfmDate(String dealCfmDate) {
		nullToString(dealCfmDate);
		this.dealCfmDate = dealCfmDate;
	}
	public String getCfmMemo() {
		return cfmMemo;
	}
	public void setCfmMemo(String cfmMemo) {
		nullToString(cfmMemo);
		this.cfmMemo = cfmMemo;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	private void nullToString(String toString){
		if(toString == null){
			toString = "";
		}
	}

}
