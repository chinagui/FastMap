package com.navinfo.dataservice.control.dealership.service.model;

/** 
 * @ClassName: ExpDbDiffResult
 * @author songdongyan
 * @date 2017年6月15日
 * @Description: ExpDbDiffResult.java
 */
public class ExpDbDiffResult {

	
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
	private String  dbDiff;
	private String  matchMethod;
	
	private String  poi1Num;
	private String  poi1Name;
	private String  poi1AliasName;
	private String  poi1KindCode;
	private String  poi1Chain;
	private String  poi1Address;
	private String  poi1Tel;
	private String  poi1PostCode;
	private String  poi1Diff;	
	
	private String  poi2Num;
	private String  poi2Name;
	private String  poi2AliasName;
	private String  poi2KindCode;
	private String  poi2Chain;
	private String  poi2Address;
	private String  poi2Tel;
	private String  poi2PostCode;
	private String  poi2Diff;
	
	private String  poi3Num;
	private String  poi3Name;
	private String  poi3AliasName;
	private String  poi3KindCode;
	private String  poi3Chain;
	private String  poi3Address;
	private String  poi3Tel;
	private String  poi3PostCode;
	private String  poi3Diff;
	
	private String  poi4Num;
	private String  poi4Name;
	private String  poi4AliasName;
	private String  poi4KindCode;
	private String  poi4Chain;
	private String  poi4Address;
	private String  poi4Tel;
	private String  poi4PostCode;
	private String  poi4Diff;

	private String  poi5Num;
	private String  poi5Name;
	private String  poi5AliasName;
	private String  poi5KindCode;
	private String  poi5Chain;
	private String  poi5Address;
	private String  poi5Tel;
	private String  poi5PostCode;
	private String  poi5Diff;
	
	//代理店 - 表库差分结果导出原则变更(6882)
	private String cfmPoiNum;
	private String cfmPoiNumName;
	private String cfmPoiNumAliasName;
	private String cfmPoiNumKindCode;
	private String cfmPoiNumChain;
	private String cfmPoiNumAddress;
	private String cfmPoiNumTel;
	private String cfmPoiNumPostCode;
	private String cfmPoiNumDiff;
	private String cfmIsAdopted;

	private String  similarity;
	private String  dealCfmDate;
	private int  regionId;

	
	

	public ExpDbDiffResult (){
		
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






	public String getDbDiff() {
		return dbDiff;
	}






	public void setDbDiff(String dbDiff) {
		this.dbDiff = dbDiff;
	}






	public String getMatchMethod() {
		return matchMethod;
	}






	public void setMatchMethod(String matchMethod) {
		this.matchMethod = matchMethod;
	}






	public String getPoi1Num() {
		return poi1Num;
	}






	public void setPoi1Num(String poi1Num) {
		this.poi1Num = poi1Num;
	}






	public String getPoi1Name() {
		return poi1Name;
	}






	public void setPoi1Name(String poi1Name) {
		this.poi1Name = poi1Name;
	}






	public String getPoi1AliasName() {
		return poi1AliasName;
	}






	public void setPoi1AliasName(String poi1AliasName) {
		this.poi1AliasName = poi1AliasName;
	}






	public String getPoi1KindCode() {
		return poi1KindCode;
	}






	public void setPoi1KindCode(String poi1KindCode) {
		this.poi1KindCode = poi1KindCode;
	}






	public String getPoi1Chain() {
		return poi1Chain;
	}






	public void setPoi1Chain(String poi1Chain) {
		this.poi1Chain = poi1Chain;
	}






	public String getPoi1Address() {
		return poi1Address;
	}






	public void setPoi1Address(String poi1Address) {
		this.poi1Address = poi1Address;
	}






	public String getPoi1Tel() {
		return poi1Tel;
	}






	public void setPoi1Tel(String poi1Tel) {
		this.poi1Tel = poi1Tel;
	}






	public String getPoi1PostCode() {
		return poi1PostCode;
	}






	public void setPoi1PostCode(String poi1PostCode) {
		this.poi1PostCode = poi1PostCode;
	}






	public String getPoi1Diff() {
		return poi1Diff;
	}






	public void setPoi1Diff(String poi1Diff) {
		this.poi1Diff = poi1Diff;
	}






	public String getPoi2Num() {
		return poi2Num;
	}






	public void setPoi2Num(String poi2Num) {
		this.poi2Num = poi2Num;
	}






	public String getPoi2Name() {
		return poi2Name;
	}






	public void setPoi2Name(String poi2Name) {
		this.poi2Name = poi2Name;
	}






	public String getPoi2AliasName() {
		return poi2AliasName;
	}






	public void setPoi2AliasName(String poi2AliasName) {
		this.poi2AliasName = poi2AliasName;
	}






	public String getPoi2KindCode() {
		return poi2KindCode;
	}






	public void setPoi2KindCode(String poi2KindCode) {
		this.poi2KindCode = poi2KindCode;
	}






	public String getPoi2Chain() {
		return poi2Chain;
	}






	public void setPoi2Chain(String poi2Chain) {
		this.poi2Chain = poi2Chain;
	}






	public String getPoi2Address() {
		return poi2Address;
	}






	public void setPoi2Address(String poi2Address) {
		this.poi2Address = poi2Address;
	}






	public String getPoi2Tel() {
		return poi2Tel;
	}






	public void setPoi2Tel(String poi2Tel) {
		this.poi2Tel = poi2Tel;
	}






	public String getPoi2PostCode() {
		return poi2PostCode;
	}






	public void setPoi2PostCode(String poi2PostCode) {
		this.poi2PostCode = poi2PostCode;
	}






	public String getPoi2Diff() {
		return poi2Diff;
	}






	public void setPoi2Diff(String poi2Diff) {
		this.poi2Diff = poi2Diff;
	}






	public String getPoi3Num() {
		return poi3Num;
	}






	public void setPoi3Num(String poi3Num) {
		this.poi3Num = poi3Num;
	}






	public String getPoi3Name() {
		return poi3Name;
	}






	public void setPoi3Name(String poi3Name) {
		this.poi3Name = poi3Name;
	}






	public String getPoi3AliasName() {
		return poi3AliasName;
	}






	public void setPoi3AliasName(String poi3AliasName) {
		this.poi3AliasName = poi3AliasName;
	}






	public String getPoi3KindCode() {
		return poi3KindCode;
	}






	public void setPoi3KindCode(String poi3KindCode) {
		this.poi3KindCode = poi3KindCode;
	}






	public String getPoi3Chain() {
		return poi3Chain;
	}






	public void setPoi3Chain(String poi3Chain) {
		this.poi3Chain = poi3Chain;
	}






	public String getPoi3Address() {
		return poi3Address;
	}






	public void setPoi3Address(String poi3Address) {
		this.poi3Address = poi3Address;
	}






	public String getPoi3Tel() {
		return poi3Tel;
	}






	public void setPoi3Tel(String poi3Tel) {
		this.poi3Tel = poi3Tel;
	}






	public String getPoi3PostCode() {
		return poi3PostCode;
	}






	public void setPoi3PostCode(String poi3PostCode) {
		this.poi3PostCode = poi3PostCode;
	}






	public String getPoi3Diff() {
		return poi3Diff;
	}






	public void setPoi3Diff(String poi3Diff) {
		this.poi3Diff = poi3Diff;
	}






	public String getPoi4Num() {
		return poi4Num;
	}






	public void setPoi4Num(String poi4Num) {
		this.poi4Num = poi4Num;
	}






	public String getPoi4Name() {
		return poi4Name;
	}






	public void setPoi4Name(String poi4Name) {
		this.poi4Name = poi4Name;
	}






	public String getPoi4AliasName() {
		return poi4AliasName;
	}






	public void setPoi4AliasName(String poi4AliasName) {
		this.poi4AliasName = poi4AliasName;
	}






	public String getPoi4KindCode() {
		return poi4KindCode;
	}






	public void setPoi4KindCode(String poi4KindCode) {
		this.poi4KindCode = poi4KindCode;
	}






	public String getPoi4Chain() {
		return poi4Chain;
	}






	public void setPoi4Chain(String poi4Chain) {
		this.poi4Chain = poi4Chain;
	}






	public String getPoi4Address() {
		return poi4Address;
	}






	public void setPoi4Address(String poi4Address) {
		this.poi4Address = poi4Address;
	}






	public String getPoi4Tel() {
		return poi4Tel;
	}






	public void setPoi4Tel(String poi4Tel) {
		this.poi4Tel = poi4Tel;
	}






	public String getPoi4PostCode() {
		return poi4PostCode;
	}






	public void setPoi4PostCode(String poi4PostCode) {
		this.poi4PostCode = poi4PostCode;
	}






	public String getPoi4Diff() {
		return poi4Diff;
	}






	public void setPoi4Diff(String poi4Diff) {
		this.poi4Diff = poi4Diff;
	}






	public String getPoi5Num() {
		return poi5Num;
	}






	public void setPoi5Num(String poi5Num) {
		this.poi5Num = poi5Num;
	}






	public String getPoi5Name() {
		return poi5Name;
	}






	public void setPoi5Name(String poi5Name) {
		this.poi5Name = poi5Name;
	}






	public String getPoi5AliasName() {
		return poi5AliasName;
	}






	public void setPoi5AliasName(String poi5AliasName) {
		this.poi5AliasName = poi5AliasName;
	}






	public String getPoi5KindCode() {
		return poi5KindCode;
	}






	public void setPoi5KindCode(String poi5KindCode) {
		this.poi5KindCode = poi5KindCode;
	}






	public String getPoi5Chain() {
		return poi5Chain;
	}






	public void setPoi5Chain(String poi5Chain) {
		this.poi5Chain = poi5Chain;
	}






	public String getPoi5Address() {
		return poi5Address;
	}






	public void setPoi5Address(String poi5Address) {
		this.poi5Address = poi5Address;
	}






	public String getPoi5Tel() {
		return poi5Tel;
	}






	public void setPoi5Tel(String poi5Tel) {
		this.poi5Tel = poi5Tel;
	}






	public String getPoi5PostCode() {
		return poi5PostCode;
	}






	public void setPoi5PostCode(String poi5PostCode) {
		this.poi5PostCode = poi5PostCode;
	}






	public String getPoi5Diff() {
		return poi5Diff;
	}






	public void setPoi5Diff(String poi5Diff) {
		this.poi5Diff = poi5Diff;
	}






	public String getSimilarity() {
		return similarity;
	}






	public void setSimilarity(String similarity) {
		this.similarity = similarity;
	}






	public String getDealCfmDate() {
		return dealCfmDate;
	}






	public void setDealCfmDate(String dealCfmDate) {
		this.dealCfmDate = dealCfmDate;
	}






	public int getRegionId() {
		return regionId;
	}






	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}






	public String getCfmPoiNum() {
		return cfmPoiNum;
	}






	public void setCfmPoiNum(String cfmPoiNum) {
		this.cfmPoiNum = cfmPoiNum;
	}






	public String getCfmPoiNumName() {
		return cfmPoiNumName;
	}






	public void setCfmPoiNumName(String cfmPoiNumName) {
		this.cfmPoiNumName = cfmPoiNumName;
	}






	public String getCfmPoiNumAliasName() {
		return cfmPoiNumAliasName;
	}






	public void setCfmPoiNumAliasName(String cfmPoiNumAliasName) {
		this.cfmPoiNumAliasName = cfmPoiNumAliasName;
	}






	public String getCfmPoiNumKindCode() {
		return cfmPoiNumKindCode;
	}






	public void setCfmPoiNumKindCode(String cfmPoiNumKindCode) {
		this.cfmPoiNumKindCode = cfmPoiNumKindCode;
	}






	public String getCfmPoiNumChain() {
		return cfmPoiNumChain;
	}






	public void setCfmPoiNumChain(String cfmPoiNumChain) {
		this.cfmPoiNumChain = cfmPoiNumChain;
	}






	public String getCfmPoiNumAddress() {
		return cfmPoiNumAddress;
	}






	public void setCfmPoiNumAddress(String cfmPoiNumAddress) {
		this.cfmPoiNumAddress = cfmPoiNumAddress;
	}






	public String getCfmPoiNumTel() {
		return cfmPoiNumTel;
	}






	public void setCfmPoiNumTel(String cfmPoiNumTel) {
		this.cfmPoiNumTel = cfmPoiNumTel;
	}






	public String getCfmPoiNumPostCode() {
		return cfmPoiNumPostCode;
	}






	public void setCfmPoiNumPostCode(String cfmPoiNumPostCode) {
		this.cfmPoiNumPostCode = cfmPoiNumPostCode;
	}






	public String getCfmPoiNumDiff() {
		return cfmPoiNumDiff;
	}






	public void setCfmPoiNumDiff(String cfmPoiNumDiff) {
		this.cfmPoiNumDiff = cfmPoiNumDiff;
	}






	public String getCfmIsAdopted() {
		return cfmIsAdopted;
	}






	public void setCfmIsAdopted(String cfmIsAdopted) {
		this.cfmIsAdopted = cfmIsAdopted;
	}
	
}
