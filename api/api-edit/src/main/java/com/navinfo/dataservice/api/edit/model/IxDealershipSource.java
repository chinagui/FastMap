package com.navinfo.dataservice.api.edit.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/** 
* @ClassName:  IxDealershipSource 
* @author code generator
* @date 2017-05-27 03:28:25 
* @Description: TODO
*/
public class IxDealershipSource  {
	private int sourceId ;
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
	private int fbSource ;
	private String fbContent ;
	private String fbAuditRemark ;
	private String fbDate ;
	private String cfmPoiNum ;
	private String cfmMemo ;
	private String dealCfmDate ;
	private String poiKindCode ;
	private String poiChain ;
	private String poiName ;
	private String poiNameShort ;
	private String poiAddress ;
	private String poiPostCode ;
	private double poiXDisplay = 0 ;
	private double poiYDisplay = 0 ;
	private double poiXGuide = 0;
	private double poiYGuide = 0;
	private Geometry geometry ;
	private String poiTel ;
	
	public IxDealershipSource (){
	}
	
	public IxDealershipSource (int sourceId ,String province,String city,String project,String kindCode,String chain,String name,String nameShort,String address,String telSale,String telService,String telOther,String postCode,String nameEng,String addressEng,String provideDate,int isDeleted,int fbSource,String fbContent,String fbAuditRemark,String fbDate,String cfmPoiNum,String cfmMemo,String dealCfmDate,String poiKindCode,String poiChain,String poiName,String poiNameShort,String poiAddress,String poiPostCode,double poiXDisplay,double poiYDisplay,double poiXGuide,double poiYGuide,Geometry geometry,String poiTel){
		this.sourceId=sourceId ;
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
		this.fbSource=fbSource ;
		this.fbContent=fbContent ;
		this.fbAuditRemark=fbAuditRemark ;
		this.fbDate=fbDate ;
		this.cfmPoiNum=cfmPoiNum ;
		this.cfmMemo=cfmMemo ;
		this.dealCfmDate=dealCfmDate ;
		this.poiKindCode=poiKindCode ;
		this.poiChain=poiChain ;
		this.poiName=poiName ;
		this.poiNameShort=poiNameShort ;
		this.poiAddress=poiAddress ;
		this.poiPostCode=poiPostCode ;
		this.poiXDisplay=poiXDisplay ;
		this.poiYDisplay=poiYDisplay ;
		this.poiXGuide=poiXGuide ;
		this.poiYGuide=poiYGuide ;
		this.geometry=geometry ;
		this.poiTel=poiTel ;
	}
	public int getSourceId() {
		return sourceId;
	}
	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
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
		if(address==null){
			return "";
		}
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
	public String getPoiTel() {
		return poiTel;
	}
	public void setPoiTel(String poiTel) {
		this.poiTel = poiTel;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IxDealershipSource [sourceId=" + sourceId +",province="+province+",city="+city+",project="+project+",kindCode="+kindCode+",chain="+chain+",name="+name+",nameShort="+nameShort+",address="+address+",telSale="+telSale+",telService="+telService+",telOther="+telOther+",postCode="+postCode+",nameEng="+nameEng+",addressEng="+addressEng+",provideDate="+provideDate+",isDeleted="+isDeleted+",fbSource="+fbSource+",fbContent="+fbContent+",fbAuditRemark="+fbAuditRemark+",fbDate="+fbDate+",cfmPoiNum="+cfmPoiNum+",cfmMemo="+cfmMemo+",dealCfmDate="+dealCfmDate+",poiKindCode="+poiKindCode+",poiChain="+poiChain+",poiName="+poiName+",poiNameShort="+poiNameShort+",poiAddress="+poiAddress+",poiPostCode="+poiPostCode+",poiXDisplay="+poiXDisplay+",poiYDisplay="+poiYDisplay+",poiXGuide="+poiXGuide+",poiYGuide="+poiYGuide+",geometry="+geometry+",poiTel="+poiTel+"]";
	}

	/**
	 * @return
	 */
	public String getTelephone() {
		StringBuffer sb = new StringBuffer();
		String telephone = "";
		String splitChar = "|";
		if (this.telSale != null && !"".equals(this.telSale)) {
			sb.append(this.telSale);
			sb.append(splitChar);
		}
		if (this.telService != null && !"".equals(this.telService)) {
			sb.append(this.telService);
			sb.append(splitChar);
		}
		if (this.telOther != null && !"".equals(this.telOther)) {
			sb.append(this.telOther);
			sb.append(splitChar);
		}
		telephone = sb.toString();
		if (!"".equals(telephone)) {
			telephone = telephone.substring(0, telephone.length() - 1);
		}
		return telephone;
	}


}
