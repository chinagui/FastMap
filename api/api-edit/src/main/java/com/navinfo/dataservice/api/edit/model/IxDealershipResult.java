package com.navinfo.dataservice.api.edit.model;

import com.vividsolutions.jts.geom.Geometry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: IxDealershipResult
 * @author code generator
 * @date 2017-05-27 03:27:47
 * @Description: TODO
 */
public class IxDealershipResult implements IObj {
	private int resultId;
	private int workflowStatus;
	private int dealStatus;
	private int userId;
	private String toInfoDate;
	private String toClientDate;
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
	private String provideDate;
	private int isDeleted;
	private int matchMethod;
	private String poiNum1;
	private String poiNum2;
	private String poiNum3;
	private String poiNum4;
	private String poiNum5;
	private String similarity;
	private int fbSource;
	private String fbContent;
	private String fbAuditRemark;
	private String fbDate;
	private int cfmStatus;
	private int cfmIsAdopted;
	private String cfmPoiNum;
	private String cfmMemo;
	private int sourceId;
	private int dealSrcDiff;
	private String dealCfmDate;
	private String poiKindCode;
	private String poiChain;
	private String poiName;
	private String poiNameShort;
	private String poiAddress;
	private String poiTel;
	private String poiPostCode;
	private double poiXDisplay;
	private double poiYDisplay;
	private double poiXGuide;
	private double poiYGuide;
	private Geometry geometry;
	private int regionId;

	protected Map<String, Object> oldValues = null;// 存储变化字段的旧值，key:col_name,value：旧值
	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public boolean checkValue(String colName, int oldValue, int newValue) {
		if (newValue == oldValue)
			return false;
		if (oldValues == null) {
			oldValues = new HashMap<String, Object>();
			oldValues.put(colName, oldValue);
		} else {
			// old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
			if (!oldValues.containsKey(colName)) {
				oldValues.put(colName, oldValue);
			}
		}
		return true;
	}

	public boolean checkValue(String colName, double oldValue, double newValue) {
		if (newValue == oldValue)
			return false;
		if (oldValues == null) {
			oldValues = new HashMap<String, Object>();
			oldValues.put(colName, oldValue);
		} else {
			// old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
			if (!oldValues.containsKey(colName)) {
				oldValues.put(colName, oldValue);
			}
		}
		return true;
	}

	public boolean checkValue(String colName, float oldValue, float newValue) {
		if (newValue == oldValue)
			return false;
		if (oldValues == null) {
			oldValues = new HashMap<String, Object>();
			oldValues.put(colName, oldValue);
		} else {
			// old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
			if (!oldValues.containsKey(colName)) {
				oldValues.put(colName, oldValue);
			}
		}
		return true;
	}

	public boolean checkValue(String colName, boolean oldValue, boolean newValue) {
		if (newValue == oldValue)
			return false;
		if (oldValues == null) {
			oldValues = new HashMap<String, Object>();
			oldValues.put(colName, oldValue);
		} else {
			// old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
			if (!oldValues.containsKey(colName)) {
				oldValues.put(colName, oldValue);
			}
		}
		return true;
	}

	public boolean checkValue(String colName, long oldValue, long newValue) {
		if (newValue == oldValue)
			return false;
		if (oldValues == null) {
			oldValues = new HashMap<String, Object>();
			oldValues.put(colName, oldValue);
		} else {
			// old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
			if (!oldValues.containsKey(colName)) {
				oldValues.put(colName, oldValue);
			}
		}
		return true;
	}

	public boolean checkValue(String colName, Object oldValue, Object newValue) {
		if (oldValue == null && newValue == null)
			return false;
		if (oldValue != null && oldValue.equals(newValue))
			return false;// 所有Object类型都通用
		// 处理String的null和""的问题
		if ((oldValue == null && newValue.equals("")) || (newValue == null && oldValue.equals(""))) {
			return false;
		}

		if (oldValues == null) {
			oldValues = new HashMap<String, Object>();
			oldValues.put(colName, oldValue);
		} else {
			// old值已经保存下来的不要再覆盖，防止多次修改时丢失原始值
			if (!oldValues.containsKey(colName)) {
				oldValues.put(colName, oldValue);
			}
		}
		return true;
	}

	public IxDealershipResult() {
	}

	public IxDealershipResult(int resultId, int workflowStatus, int dealStatus, int userId, String toInfoDate,
			String toClientDate, String province, String city, String project, String kindCode, String chain,
			String name, String nameShort, String address, String telSale, String telService, String telOther,
			String postCode, String nameEng, String addressEng, String provideDate, int isDeleted, int matchMethod,
			String poiNum1, String poiNum2, String poiNum3, String poiNum4, String poiNum5, String similarity,
			int fbSource, String fbContent, String fbAuditRemark, String fbDate, int cfmStatus, String cfmPoiNum,
			String cfmMemo, int sourceId, int dealSrcDiff, String dealCfmDate, String poiKindCode, String poiChain,
			String poiName, String poiNameShort, String poiAddress, String poiTel, String poiPostCode,
			double poiXDisplay, double poiYDisplay, double poiXGuide, double poiYGuide, Geometry geometry,
			int regionId) {
		super();
		setResultId(resultId);
		setWorkflowStatus(workflowStatus);
		setDealStatus(dealStatus);
		setUserId(userId);
		setToInfoDate(toInfoDate);
		setToClientDate(toClientDate);
		setProvince(province);
		setCity(city);
		setProject(project);
		setKindCode(kindCode);
		setChain(chain);
		setName(name);
		setNameShort(nameShort);
		setAddress(address);
		setTelSale(telSale);
		setTelService(telService);
		setTelOther(telOther);
		setPostCode(postCode);
		setNameEng(nameEng);
		setAddressEng(addressEng);
		setProvideDate(provideDate);
		setIsDeleted(isDeleted);
		setMatchMethod(matchMethod);
		setPoiNum1(poiNum1);
		setPoiNum2(poiNum2);
		setPoiNum3(poiNum3);
		setPoiNum4(poiNum4);
		setPoiNum5(poiNum5);
		setSimilarity(similarity);
		setFbSource(fbSource);
		setFbContent(fbContent);
		setFbAuditRemark(fbAuditRemark);
		setFbDate(fbDate);
		setCfmStatus(cfmStatus);
		setCfmPoiNum(cfmPoiNum);
		setCfmMemo(cfmMemo);
		setSourceId(sourceId);
		setDealSrcDiff(dealSrcDiff);
		setDealCfmDate(dealCfmDate);
		setPoiKindCode(poiKindCode);
		setPoiChain(poiChain);
		setPoiName(poiName);
		setPoiNameShort(poiNameShort);
		setPoiAddress(poiAddress);
		setPoiTel(poiTel);
		setPoiPostCode(poiPostCode);
		setPoiXDisplay(poiXDisplay);
		setPoiYDisplay(poiYDisplay);
		setPoiXGuide(poiXGuide);
		setPoiYGuide(poiYGuide);
		setGeometry(geometry);
		setRegionId(regionId);
		// this.resultId = resultId;
		// this.workflowStatus = workflowStatus;
		// this.dealStatus = dealStatus;
		// this.userId = userId;
		// this.toInfoDate = toInfoDate;
		// this.toClientDate = toClientDate;
		// this.province = province;
		// this.city = city;
		// this.project = project;
		// this.kindCode = kindCode;
		// this.chain = chain;
		// this.name = name;
		// this.nameShort = nameShort;
		// this.address = address;
		// this.telSale = telSale;
		// this.telService = telService;
		// this.telOther = telOther;
		// this.postCode = postCode;
		// this.nameEng = nameEng;
		// this.addressEng = addressEng;
		// this.provideDate = provideDate;
		// this.isDeleted = isDeleted;
		// this.matchMethod = matchMethod;
		// this.poiNum1 = poiNum1;
		// this.poiNum2 = poiNum2;
		// this.poiNum3 = poiNum3;
		// this.poiNum4 = poiNum4;
		// this.poiNum5 = poiNum5;
		// this.similarity = similarity;
		// this.fbSource = fbSource;
		// this.fbContent = fbContent;
		// this.fbAuditRemark = fbAuditRemark;
		// this.fbDate = fbDate;
		// this.cfmStatus = cfmStatus;
		// this.cfmPoiNum = cfmPoiNum;
		// this.cfmMemo = cfmMemo;
		// this.sourceId = sourceId;
		// this.dealSrcDiff = dealSrcDiff;
		// this.dealCfmDate = dealCfmDate;
		// this.poiKindCode = poiKindCode;
		// this.poiChain = poiChain;
		// this.poiName = poiName;
		// this.poiNameShort = poiNameShort;
		// this.poiAddress = poiAddress;
		// this.poiTel = poiTel;
		// this.poiPostCode = poiPostCode;
		// this.poiXDisplay = poiXDisplay;
		// this.poiYDisplay = poiYDisplay;
		// this.poiXGuide = poiXGuide;
		// this.poiYGuide = poiYGuide;
		// this.geometry = geometry;
		// this.regionId = regionId;
	}

	/**
	 * @param i
	 */
	public IxDealershipResult(IxDealershipResult ixDealershipResult) {
		setResultId(ixDealershipResult.getResultId());
		setWorkflowStatus(ixDealershipResult.getWorkflowStatus());
		setDealStatus(ixDealershipResult.getDealStatus());
		setUserId(ixDealershipResult.getUserId());
		setToInfoDate(ixDealershipResult.getToInfoDate());
		setToClientDate(ixDealershipResult.getToClientDate());
		setProvince(ixDealershipResult.getProvince());
		setCity(ixDealershipResult.getCity());
		setProject(ixDealershipResult.getProject());
		setKindCode(ixDealershipResult.getKindCode());
		setChain(ixDealershipResult.getChain());
		setName(ixDealershipResult.getName());
		setNameShort(ixDealershipResult.getNameShort());
		setAddress(ixDealershipResult.getAddress());
		setTelSale(ixDealershipResult.getTelSale());
		setTelService(ixDealershipResult.getTelService());
		setTelOther(ixDealershipResult.getTelOther());
		setPostCode(ixDealershipResult.getPostCode());
		setNameEng(ixDealershipResult.getNameEng());
		setAddressEng(ixDealershipResult.getAddressEng());
		setProvideDate(ixDealershipResult.getProvideDate());
		setIsDeleted(ixDealershipResult.getIsDeleted());
		setMatchMethod(ixDealershipResult.getMatchMethod());
		setPoiNum1(ixDealershipResult.getPoiNum1());
		setPoiNum2(ixDealershipResult.getPoiNum2());
		setPoiNum3(ixDealershipResult.getPoiNum3());
		setPoiNum4(ixDealershipResult.getPoiNum4());
		setPoiNum5(ixDealershipResult.getPoiNum5());
		setSimilarity(ixDealershipResult.getSimilarity());
		setFbSource(ixDealershipResult.getFbSource());
		setFbContent(ixDealershipResult.getFbContent());
		setFbAuditRemark(ixDealershipResult.getFbAuditRemark());
		setFbDate(ixDealershipResult.getFbDate());
		setCfmStatus(ixDealershipResult.getCfmStatus());
		setCfmPoiNum(ixDealershipResult.getCfmPoiNum());
		setCfmMemo(ixDealershipResult.getCfmMemo());
		setSourceId(ixDealershipResult.getSourceId());
		setDealSrcDiff(ixDealershipResult.getDealSrcDiff());
		setDealCfmDate(ixDealershipResult.getDealCfmDate());
		setPoiKindCode(ixDealershipResult.getPoiKindCode());
		setPoiChain(ixDealershipResult.getPoiChain());
		setPoiName(ixDealershipResult.getPoiName());
		setPoiNameShort(ixDealershipResult.getPoiNameShort());
		setPoiAddress(ixDealershipResult.getPoiAddress());
		setPoiTel(ixDealershipResult.getPoiTel());
		setPoiPostCode(ixDealershipResult.getPoiPostCode());
		setPoiXDisplay(ixDealershipResult.getPoiXDisplay());
		setPoiYDisplay(ixDealershipResult.getPoiYDisplay());
		setPoiXGuide(ixDealershipResult.getPoiXGuide());
		setPoiYGuide(ixDealershipResult.getPoiYGuide());
		setGeometry(ixDealershipResult.getGeometry());
		setRegionId(ixDealershipResult.getRegionId());
		// this.resultId=ixDealershipResult.getResultId() ;
		// this.workflowStatus=ixDealershipResult.getWorkflowStatus() ;
		// this.dealStatus=ixDealershipResult.getDealStatus() ;
		// this.userId=ixDealershipResult.getUserId() ;
		// this.toInfoDate=ixDealershipResult.getToInfoDate() ;
		// this.toClientDate=ixDealershipResult.getToClientDate() ;
		// this.province=ixDealershipResult.getProvince() ;
		// this.city=ixDealershipResult.getCity() ;
		// this.project=ixDealershipResult.getProject() ;
		// this.kindCode=ixDealershipResult.getKindCode() ;
		// this.chain=ixDealershipResult.getChain() ;
		// this.name=ixDealershipResult.getName() ;
		// this.nameShort=ixDealershipResult.getNameShort() ;
		// this.address=ixDealershipResult.getAddress() ;
		// this.telSale=ixDealershipResult.getTelSale() ;
		// this.telService=ixDealershipResult.getTelService() ;
		// this.telOther=ixDealershipResult.getTelOther() ;
		// this.postCode=ixDealershipResult.getPostCode() ;
		// this.nameEng=ixDealershipResult.getNameEng() ;
		// this.addressEng=ixDealershipResult.getAddressEng() ;
		// this.provideDate=ixDealershipResult.getProvideDate() ;
		// this.isDeleted=ixDealershipResult.getIsDeleted() ;
		// this.matchMethod=ixDealershipResult.getMatchMethod() ;
		// this.poiNum1=ixDealershipResult.getPoiNum1() ;
		// this.poiNum2=ixDealershipResult.getPoiNum2() ;
		// this.poiNum3=ixDealershipResult.getPoiNum3() ;
		// this.poiNum4=ixDealershipResult.getPoiNum4() ;
		// this.poiNum5=ixDealershipResult.getPoiNum5() ;
		// this.similarity=ixDealershipResult.getSimilarity() ;
		// this.fbSource=ixDealershipResult.getFbSource() ;
		// this.fbContent=ixDealershipResult.getFbContent() ;
		// this.fbAuditRemark=ixDealershipResult.getFbAuditRemark() ;
		// this.fbDate=ixDealershipResult.getFbDate() ;
		// this.cfmStatus=ixDealershipResult.getCfmStatus() ;
		// this.cfmPoiNum=ixDealershipResult.getCfmPoiNum() ;
		// this.cfmMemo=ixDealershipResult.getCfmMemo() ;
		// this.sourceId=ixDealershipResult.getSourceId() ;
		// this.dealSrcDiff=ixDealershipResult.getDealSrcDiff() ;
		// this.dealCfmDate=ixDealershipResult.getDealCfmDate() ;
		// this.poiKindCode=ixDealershipResult.getPoiKindCode() ;
		// this.poiChain=ixDealershipResult.getPoiChain() ;
		// this.poiName=ixDealershipResult.getPoiName() ;
		// this.poiNameShort=ixDealershipResult.getPoiNameShort() ;
		// this.poiAddress=ixDealershipResult.getPoiAddress() ;
		// this.poiTel=ixDealershipResult.getPoiTel() ;
		// this.poiPostCode=ixDealershipResult.getPoiPostCode() ;
		// this.poiXDisplay=ixDealershipResult.getPoiXDisplay() ;
		// this.poiYDisplay=ixDealershipResult.getPoiYDisplay() ;
		// this.poiXGuide=ixDealershipResult.getPoiXGuide() ;
		// this.poiYGuide=ixDealershipResult.getPoiYGuide() ;
		// this.geometry=ixDealershipResult.getGeometry() ;
		// this.regionId=ixDealershipResult.getRegionId() ;
	}

	public int getResultId() {
		return resultId;
	}

	public void setResultId(int resultId) {
		if (this.checkValue("RESULT_ID", this.resultId, resultId)) {
			this.resultId = resultId;
		}
	}

	public int getWorkflowStatus() {
		return workflowStatus;
	}

	public void setWorkflowStatus(int workflowStatus) {
		if (this.checkValue("WORKFLOW_STATUS", this.workflowStatus, workflowStatus)) {
			this.workflowStatus = workflowStatus;
		}
	}

	public int getDealStatus() {
		return dealStatus;
	}

	public void setDealStatus(int dealStatus) {
		if (this.checkValue("DEAL_STATUS", this.dealStatus, dealStatus)) {
			this.dealStatus = dealStatus;
		}
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		if (this.checkValue("USER_ID", this.userId, userId)) {
			this.userId = userId;
		}
	}

	public String getToInfoDate() {
		return toInfoDate;
	}

	public void setToInfoDate(String toInfoDate) {
		if (this.checkValue("TO_INFO_DATE", this.toInfoDate, toInfoDate)) {
			this.toInfoDate = toInfoDate;
		}
	}

	public String getToClientDate() {
		return toClientDate;
	}

	public void setToClientDate(String toClientDate) {
		if (this.checkValue("TO_CLIENT_DATE", this.toClientDate, toClientDate)) {
			this.toClientDate = toClientDate;
		}
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		if (this.checkValue("PROVINCE", this.province, province)) {
			this.province = province;
		}
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		if (this.checkValue("CITY", this.city, city)) {
			this.city = city;
		}
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		if (this.checkValue("PROJECT", this.project, project)) {
			this.project = project;
		}
	}

	public String getKindCode() {
		return kindCode;
	}

	public void setKindCode(String kindCode) {
		if (this.checkValue("KIND_CODE", this.kindCode, kindCode)) {
			this.kindCode = kindCode;
		}
	}

	public String getChain() {
		return chain;
	}

	public void setChain(String chain) {
		if (this.checkValue("CHAIN", this.chain, chain)) {
			this.chain = chain;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (this.checkValue("NAME", this.name, name)) {
			this.name = name;
		}
	}

	public String getNameShort() {
		return nameShort;
	}

	public void setNameShort(String nameShort) {
		if (this.checkValue("NAME_SHORT", this.nameShort, nameShort)) {
			this.nameShort = nameShort;
		}
	}

	public String getAddress() {
		if (address == null) {
			return "";
		}
		return address;
	}

	public void setAddress(String address) {
		if (this.checkValue("ADDRESS", this.address, address)) {
			this.address = address;
		}
	}

	public String getTelSale() {
		return telSale;
	}

	public void setTelSale(String telSale) {
		if (this.checkValue("TEL_SALE", this.telSale, telSale)) {
			this.telSale = telSale;
		}
	}

	public String getTelService() {
		return telService;
	}

	public void setTelService(String telService) {
		if (this.checkValue("TEL_SERVICE", this.telService, telService)) {
			this.telService = telService;
		}
	}

	public String getTelOther() {
		return telOther;
	}

	public void setTelOther(String telOther) {
		if (this.checkValue("TEL_OTHER", this.telOther, telOther)) {
			this.telOther = telOther;
		}
	}

	public String getPostCode() {
		if (postCode == null) {
			return "";
		}
		return postCode;
	}

	public void setPostCode(String postCode) {
		if (this.checkValue("POST_CODE", this.postCode, postCode)) {
			this.postCode = postCode;
		}
	}

	public String getNameEng() {
		return nameEng;
	}

	public void setNameEng(String nameEng) {
		if (this.checkValue("NAME_ENG", this.nameEng, nameEng)) {
			this.nameEng = nameEng;
		}
	}

	public String getAddressEng() {
		return addressEng;
	}

	public void setAddressEng(String addressEng) {
		if (this.checkValue("ADDRESS_ENG", this.addressEng, addressEng)) {
			this.addressEng = addressEng;
		}
	}

	public String getProvideDate() {
		return provideDate;
	}

	public void setProvideDate(String provideDate) {
		if (this.checkValue("PROVIDE_DATE", this.provideDate, provideDate)) {
			this.provideDate = provideDate;
		}
	}

	public int getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(int isDeleted) {
		if (this.checkValue("IS_DELETED", this.isDeleted, isDeleted)) {
			this.isDeleted = isDeleted;
		}
	}

	public int getMatchMethod() {
		return matchMethod;
	}

	public void setMatchMethod(int matchMethod) {
		if (this.checkValue("MATCH_METHOD", this.matchMethod, matchMethod)) {
			this.matchMethod = matchMethod;
		}
	}

	public String getPoiNum1() {
		return poiNum1;
	}

	public void setPoiNum1(String poiNum1) {
		if (this.checkValue("POI_NUM_1", this.poiNum1, poiNum1)) {
			this.poiNum1 = poiNum1;
		}
	}

	public String getPoiNum2() {
		return poiNum2;
	}

	public void setPoiNum2(String poiNum2) {
		if (this.checkValue("POI_NUM_2", this.poiNum2, poiNum2)) {
			this.poiNum2 = poiNum2;
		}
	}

	public String getPoiNum3() {
		return poiNum3;
	}

	public void setPoiNum3(String poiNum3) {
		if (this.checkValue("POI_NUM_3", this.poiNum3, poiNum3)) {
			this.poiNum3 = poiNum3;
		}
	}

	public String getPoiNum4() {
		return poiNum4;
	}

	public void setPoiNum4(String poiNum4) {
		if (this.checkValue("POI_NUM_4", this.poiNum4, poiNum4)) {
			this.poiNum4 = poiNum4;
		}
	}

	public String getPoiNum5() {
		return poiNum5;
	}

	public void setPoiNum5(String poiNum5) {
		if (this.checkValue("POI_NUM_5", this.poiNum5, poiNum5)) {
			this.poiNum5 = poiNum5;
		}
	}

	public String getSimilarity() {
		return similarity;
	}

	public void setSimilarity(String similarity) {
		if (this.checkValue("SIMILARITY", this.similarity, similarity)) {
			this.similarity = similarity;
		}
	}

	public int getFbSource() {
		return fbSource;
	}

	public void setFbSource(int fbSource) {
		if (this.checkValue("FB_SOURCE", this.fbSource, fbSource)) {
			this.fbSource = fbSource;
		}
	}

	public String getFbContent() {
		return fbContent;
	}

	public void setFbContent(String fbContent) {
		if (this.checkValue("FB_CONTENT", this.fbContent, fbContent)) {
			this.fbContent = fbContent;
		}
	}

	public String getFbAuditRemark() {
		return fbAuditRemark;
	}

	public void setFbAuditRemark(String fbAuditRemark) {
		if (this.checkValue("FB_AUDIT_REMARK", this.fbAuditRemark, fbAuditRemark)) {
			this.fbAuditRemark = fbAuditRemark;
		}
	}

	public String getFbDate() {
		return fbDate;
	}

	public void setFbDate(String fbDate) {
		if (this.checkValue("FB_DATE", this.fbDate, fbDate)) {
			this.fbDate = fbDate;
		}
	}

	public int getCfmStatus() {
		return cfmStatus;
	}

	public void setCfmStatus(int cfmStatus) {
		if (this.checkValue("CFM_STATUS", this.cfmStatus, cfmStatus)) {
			this.cfmStatus = cfmStatus;
		}
	}

	public String getCfmPoiNum() {
		return cfmPoiNum;
	}

	public void setCfmPoiNum(String cfmPoiNum) {
		if (this.checkValue("CFM_POI_NUM", this.cfmPoiNum, cfmPoiNum)) {
			this.cfmPoiNum = cfmPoiNum;
		}
	}

	public String getCfmMemo() {
		return cfmMemo;
	}

	public void setCfmMemo(String cfmMemo) {
		if (this.checkValue("CFM_MEMO", this.cfmMemo, cfmMemo)) {
			this.cfmMemo = cfmMemo;
		}
	}

	public int getSourceId() {
		return sourceId;
	}

	public void setSourceId(int sourceId) {
		if (this.checkValue("SOURCE_ID", this.sourceId, sourceId)) {
			this.sourceId = sourceId;
		}
	}

	public int getDealSrcDiff() {
		return dealSrcDiff;
	}

	public void setDealSrcDiff(int dealSrcDiff) {
		if (this.checkValue("DEAL_SRC_DIFF", this.dealSrcDiff, dealSrcDiff)) {
			this.dealSrcDiff = dealSrcDiff;
		}
	}

	public String getDealCfmDate() {
		return dealCfmDate;
	}

	public void setDealCfmDate(String dealCfmDate) {
		if (this.checkValue("DEAL_CFM_DATE", this.dealCfmDate, dealCfmDate)) {
			this.dealCfmDate = dealCfmDate;
		}
	}

	public String getPoiKindCode() {
		return poiKindCode;
	}

	public void setPoiKindCode(String poiKindCode) {
		if (this.checkValue("POI_KIND_CODE", this.poiKindCode, poiKindCode)) {
			this.poiKindCode = poiKindCode;
		}
	}

	public String getPoiChain() {
		return poiChain;
	}

	public void setPoiChain(String poiChain) {
		if (this.checkValue("POI_CHAIN", this.poiChain, poiChain)) {
			this.poiChain = poiChain;
		}
	}

	public String getPoiName() {
		return poiName;
	}

	public void setPoiName(String poiName) {
		if (this.checkValue("POI_NAME", this.poiName, poiName)) {
			this.poiName = poiName;
		}
	}

	public String getPoiNameShort() {
		return poiNameShort;
	}

	public void setPoiNameShort(String poiNameShort) {
		if (this.checkValue("POI_NAME_SHORT", this.poiNameShort, poiNameShort)) {
			this.poiNameShort = poiNameShort;
		}
	}

	public String getPoiAddress() {
		return poiAddress;
	}

	public void setPoiAddress(String poiAddress) {
		if (this.checkValue("POI_ADDRESS", this.poiAddress, poiAddress)) {
			this.poiAddress = poiAddress;
		}
	}

	public String getPoiTel() {
		return poiTel;
	}

	public void setPoiTel(String poiTel) {
		if (this.checkValue("POI_TEL", this.poiTel, poiTel)) {
			this.poiTel = poiTel;
		}
	}

	public String getPoiPostCode() {
		return poiPostCode;
	}

	public void setPoiPostCode(String poiPostCode) {
		if (this.checkValue("POI_POST_CODE", this.poiPostCode, poiPostCode)) {
			this.poiPostCode = poiPostCode;
		}
	}

	public double getPoiXDisplay() {
		return poiXDisplay;
	}

	public void setPoiXDisplay(double poiXDisplay) {
		if (this.checkValue("POI_X_DISPLAY", this.poiXDisplay, poiXDisplay)) {
			this.poiXDisplay = poiXDisplay;
		}
	}

	public double getPoiYDisplay() {
		return poiYDisplay;
	}

	public void setPoiYDisplay(double poiYDisplay) {
		if (this.checkValue("POI_Y_DISPLAY", this.poiYDisplay, poiYDisplay)) {
			this.poiYDisplay = poiYDisplay;
		}
	}

	public double getPoiXGuide() {
		return poiXGuide;
	}

	public void setPoiXGuide(double poiXGuide) {
		if (this.checkValue("POI_X_GUIDE", this.poiXGuide, poiXGuide)) {
			this.poiXGuide = poiXGuide;
		}
	}

	public double getPoiYGuide() {
		return poiYGuide;
	}

	public void setPoiYGuide(double poiYGuide) {
		if (this.checkValue("POI_Y_GUIDE", this.poiYGuide, poiYGuide)) {
			this.poiYGuide = poiYGuide;
		}
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		if (this.checkValue("GEOMETRY", this.geometry, geometry)) {
			this.geometry = geometry;
		}
	}

	public int getRegionId() {
		return regionId;
	}

	public void setRegionId(int regionId) {
		if (this.checkValue("REGION_ID", this.regionId, regionId)) {
			this.regionId = regionId;
		}
	}

	public Map<String, Object> getOldValues() {
		return oldValues;
	}

	public void setOldValues(Map<String, Object> oldValues) {
		this.oldValues = oldValues;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IxDealershipResult [resultId=" + resultId + ",workflowStatus=" + workflowStatus + ",dealStatus="
				+ dealStatus + ",userId=" + userId + ",toInfoDate=" + toInfoDate + ",toClientDate=" + toClientDate
				+ ",province=" + province + ",city=" + city + ",project=" + project + ",kindCode=" + kindCode
				+ ",chain=" + chain + ",name=" + name + ",nameShort=" + nameShort + ",address=" + address + ",telSale="
				+ telSale + ",telService=" + telService + ",telOther=" + telOther + ",postCode=" + postCode
				+ ",nameEng=" + nameEng + ",addressEng=" + addressEng + ",provideDate=" + provideDate + ",isDeleted="
				+ isDeleted + ",matchMethod=" + matchMethod + ",poiNum1=" + poiNum1 + ",poiNum2=" + poiNum2
				+ ",poiNum3=" + poiNum3 + ",poiNum4=" + poiNum4 + ",poiNum5=" + poiNum5 + ",similarity=" + similarity
				+ ",fbSource=" + fbSource + ",fbContent=" + fbContent + ",fbAuditRemark=" + fbAuditRemark + ",fbDate="
				+ fbDate + ",cfmStatus=" + cfmStatus + ",cfmPoiNum=" + cfmPoiNum + ",cfmMemo=" + cfmMemo + ",sourceId="
				+ sourceId + ",dealSrcDiff=" + dealSrcDiff + ",dealCfmDate=" + dealCfmDate + ",poiKindCode="
				+ poiKindCode + ",poiChain=" + poiChain + ",poiName=" + poiName + ",poiNameShort=" + poiNameShort
				+ ",poiAddress=" + poiAddress + ",poiTel=" + poiTel + ",poiPostCode=" + poiPostCode + ",poiXDisplay="
				+ poiXDisplay + ",poiYDisplay=" + poiYDisplay + ",poiXGuide=" + poiXGuide + ",poiYGuide=" + poiYGuide
				+ ",geometry=" + geometry + ",regionId=" + regionId + "]";
	}

	public int getCfmIsAdopted() {
		return cfmIsAdopted;
	}

	public void setCfmIsAdopted(int cfmIsAdopted) {
		this.cfmIsAdopted = cfmIsAdopted;
	}

	/**
	 * @return
	 */
	public String getTelephone() {
		// TODO Auto-generated method stub
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

	@Override
	public String rowId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRowId(String rowId) {
		// TODO Auto-generated method stub

	}

	@Override
	public String tableName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjStatus status() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatus(ObjStatus os) {
		// TODO Auto-generated method stub

	}

	@Override
	public ObjType objType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void copy(IRow row) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, Object> changedFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String parentPKName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int parentPKValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String parentTableName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<List<IRow>> children() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		@SuppressWarnings("rawtypes")
		Iterator keys = json.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();

			if (json.get(key) instanceof JSONArray) {
				continue;
			} else if ("geometry".equals(key)) {

				JSONObject geojson = json.getJSONObject(key);

				String wkt = Geojson.geojson2Wkt(geojson.toString());

				String oldwkt = GeoTranslator.jts2Wkt(geometry, 0.00001, 5);

				if (!wkt.equals(oldwkt)) {
					// double length =
					// GeometryUtils.getLinkLength(GeoTranslator.geojson2Jts(geojson));
					//
					// changedFields.put("length", length);

					changedFields.put(key, json.getJSONObject(key));
				}
			}
		}
		return false;
	}

	@Override
	public int mesh() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
		// TODO Auto-generated method stub

	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<IRow> relatedRows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int pid() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String primaryKey() {
		return "resultId";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		// TODO Auto-generated method stub
		return null;
	}

}
