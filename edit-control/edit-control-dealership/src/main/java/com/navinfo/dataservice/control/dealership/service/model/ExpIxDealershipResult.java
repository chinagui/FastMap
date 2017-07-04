package com.navinfo.dataservice.control.dealership.service.model;

/** 
* @ClassName:  ExpIxDealershipResult 
* @author zl
* @date 2017-06-01 03:27:47 
* @Description: 用于表表差分结果表导出Excel的javabean 类
*/
public class ExpIxDealershipResult  {
	private String resultId ;
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
	
	private String  oldSourceId;
	private String  oldProvince;
	private String  oldCity;
	private String  oldProject;
	private String  oldKindCode;
	private String  oldChain;
	private String  oldName;
	private String  oldNameShort;
	private String  oldAddress;
	private String  oldTelSale;
	private String  oldTelService;
	private String  oldTelOther;
	private String  oldPostCode;
	private String  oldNameEng;
	private String  oldAddressEng;
	private String  dealSrcDiff;

	public ExpIxDealershipResult (){
		
	}

	public ExpIxDealershipResult(String resultId, String province, String city, String project, String kindCode,
			String chain, String name, String nameShort, String address, String telSale, String telService,
			String telOther, String postCode, String nameEng, String addressEng, String oldSourceId, String oldProvince,
			String oldCity, String oldProject, String oldKindCode, String oldChain, String oldName, String oldNameShort,
			String oldAddress, String oldTelSale, String oldTelService, String oldTelOther, String oldPostCode,
			String oldNameEng, String oldAddressEng, String dealSrcDiff) {
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
		this.oldSourceId = oldSourceId;
		this.oldProvince = oldProvince;
		this.oldCity = oldCity;
		this.oldProject = oldProject;
		this.oldKindCode = oldKindCode;
		this.oldChain = oldChain;
		this.oldName = oldName;
		this.oldNameShort = oldNameShort;
		this.oldAddress = oldAddress;
		this.oldTelSale = oldTelSale;
		this.oldTelService = oldTelService;
		this.oldTelOther = oldTelOther;
		this.oldPostCode = oldPostCode;
		this.oldNameEng = oldNameEng;
		this.oldAddressEng = oldAddressEng;
		this.dealSrcDiff = dealSrcDiff;
	}


	@Override
	public String toString() {
		return "ExpIxDealershipResult [resultId=" + resultId + ", province=" + province + ", city=" + city
				+ ", project=" + project + ", kindCode=" + kindCode + ", chain=" + chain + ", name=" + name
				+ ", nameShort=" + nameShort + ", address=" + address + ", telSale=" + telSale + ", telService="
				+ telService + ", telOther=" + telOther + ", postCode=" + postCode + ", nameEng=" + nameEng
				+ ", addressEng=" + addressEng + ", oldSourceId=" + oldSourceId + ", oldProvince=" + oldProvince
				+ ", oldCity=" + oldCity + ", oldProject=" + oldProject + ", oldKindCode=" + oldKindCode + ", oldChain="
				+ oldChain + ", oldName=" + oldName + ", oldNameShort=" + oldNameShort + ", oldAddress=" + oldAddress
				+ ", oldTelSale=" + oldTelSale + ", oldTelService=" + oldTelService + ", oldTelOther=" + oldTelOther
				+ ", oldPostCode=" + oldPostCode + ", oldNameEng=" + oldNameEng + ", oldAddressEng=" + oldAddressEng
				+ ", dealSrcDiff=" + dealSrcDiff + "]";
	}


	public String getResultId() {
		return resultId;
	}


	public void setResultId(String resultId) {
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


	public String getOldSourceId() {
		return oldSourceId;
	}


	public void setOldSourceId(String oldSourceId) {
		this.oldSourceId = oldSourceId;
	}


	public String getOldProvince() {
		return oldProvince;
	}


	public void setOldProvince(String oldProvince) {
		this.oldProvince = oldProvince;
	}


	public String getOldCity() {
		return oldCity;
	}


	public void setOldCity(String oldCity) {
		this.oldCity = oldCity;
	}


	public String getOldProject() {
		return oldProject;
	}


	public void setOldProject(String oldProject) {
		this.oldProject = oldProject;
	}


	public String getOldKindCode() {
		return oldKindCode;
	}


	public void setOldKindCode(String oldKindCode) {
		this.oldKindCode = oldKindCode;
	}


	public String getOldChain() {
		return oldChain;
	}


	public void setOldChain(String oldChain) {
		this.oldChain = oldChain;
	}


	public String getOldName() {
		return oldName;
	}


	public void setOldName(String oldName) {
		this.oldName = oldName;
	}


	public String getOldNameShort() {
		return oldNameShort;
	}


	public void setOldNameShort(String oldNameShort) {
		this.oldNameShort = oldNameShort;
	}


	public String getOldAddress() {
		return oldAddress;
	}


	public void setOldAddress(String oldAddress) {
		this.oldAddress = oldAddress;
	}


	public String getOldTelSale() {
		return oldTelSale;
	}


	public void setOldTelSale(String oldTelSale) {
		this.oldTelSale = oldTelSale;
	}


	public String getOldTelService() {
		return oldTelService;
	}


	public void setOldTelService(String oldTelService) {
		this.oldTelService = oldTelService;
	}


	public String getOldTelOther() {
		return oldTelOther;
	}


	public void setOldTelOther(String oldTelOther) {
		this.oldTelOther = oldTelOther;
	}


	public String getOldPostCode() {
		return oldPostCode;
	}


	public void setOldPostCode(String oldPostCode) {
		this.oldPostCode = oldPostCode;
	}


	public String getOldNameEng() {
		return oldNameEng;
	}


	public void setOldNameEng(String oldNameEng) {
		this.oldNameEng = oldNameEng;
	}


	public String getOldAddressEng() {
		return oldAddressEng;
	}


	public void setOldAddressEng(String oldAddressEng) {
		this.oldAddressEng = oldAddressEng;
	}


	public String getDealSrcDiff() {
		return dealSrcDiff;
	}


	public void setDealSrcDiff(String dealSrcDiff) {
		this.dealSrcDiff = dealSrcDiff;
	}

	
}
