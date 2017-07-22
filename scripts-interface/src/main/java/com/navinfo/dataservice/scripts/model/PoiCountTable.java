package com.navinfo.dataservice.scripts.model;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONObject;

/**
 * @ClassName: IxDealershipSource
 * @author code generator
 * @date 2017-05-27 03:28:25
 * @Description: TODO
 */
public class PoiCountTable implements IObj{
	private String fid;
	private String level;
	private String poiName;
	private String meshId;
	private String area;
	private String category;
	private String fullMatch;
	private String partialMatch;
	private String extra;
	private String missing;
	private String nameDbCount;
	private String nameSiteCount;
	private String nameModify;
	private String nameDataUnmodified;
	private String nameDataModified;
	private String positionDbCount;
	private String positionSiteCount;
	private String positionModify;
	private String positionDataUnmodified;
	private String positionDataModified;
	private String categoryDbCount;
	private String categorySiteCount;
	private String categoryModify;
	private String categoryDataUnmodified;
	private String categoryDataModified;
	private String addressDbCount;
	private String addressSiteCount;
	private String addressModify;
	private String addressDataUnmodified;
	private String addressDataModified;
	private String photeDbCount;
	private String photeSiteCount;
	private String photeModify;
	private String photeDataUnmodified;
	private String photeDataModified;
	private String siteDbCount;
	private String siteSiteCount;
	private String siteModify;
	private String siteDataUnmodified;
	private String siteDataModified;
	private String fatherSonDbCount;
	private String fatherSonSiteCount;
	private String fatherSonModify;
	private String fatherSonDataUnmodified;
	private String fatherSonDataModified;
	private String deepDbCount;
	private String deepSiteCount;
	private String deepModify;
	private String deepDataUnmodified;
	private String deepDataModified;
	private String labelDbCount;
	private String labelSiteCount;
	private String labelModify;
	private String labelDataUnmodified;
	private String labelDataModified;
	private String resturantDbCount;
	private String resturantSiteCount;
	private String resturantModify;
	private String resturantDataUnmodified;
	private String resturantDataModified;
	private String linkDbCount;
	private String linkSiteCount;
	private String linkModify;
	private String linkDataUnmodified;
	private String linkDataModified;
	private String postcodeDbCount;
	private String postcodeSiteCount;
	private String postcodeModify;
	private String postcodeDataUnmodified;
	private String postcodeDataModified;
	private String levelDbCount;
	private String levelSiteCount;
	private String levelModify;
	private String levelDataUnmodified;
	private String levelDataModified;
	private String collectorUserid;
	private String collectorTime;
	private String inputUserid;
	private String inputTime;
	private String qcUserid;
	private String qcTime;
	private String qcSubTaskid;
	private String vision;
	private String memo;
	private String type;
	private String memoUserid;
	private String hasExport;

	public PoiCountTable() {
	}

	public PoiCountTable(String fid, String level, String poiName, String meshId, String area, String category,
			String fullMatch, String partialMatch, String extra, String missing, String nameDbCount,
			String nameSiteCount, String nameModify, String nameDataUnmodified, String nameDataModified,
			String positionDbCount, String positionSiteCount, String positionModify, String positionDataUnmodified,
			String positionDataModified, String categoryDbCount, String categorySiteCount, String categoryModify,
			String categoryDataUnmodified, String categoryDataModified, String addressDbCount, String addressSiteCount,
			String addressModify, String addressDataUnmodified, String addressDataModified, String photeDbCount,
			String photeSiteCount, String photeModify, String photeDataUnmodified, String photeDataModified,
			String siteDbCount, String siteSiteCount, String siteModify, String siteDataUnmodified,
			String siteDataModified, String fatherSonDbCount, String fatherSonSiteCount, String fatherSonModify,
			String fatherSonDataUnmodified, String fatherSonDataModified, String deepDbCount, String deepSiteCount,
			String deepModify, String deepDataUnmodified, String deepDataModified, String labelDbCount,
			String labelSiteCount, String labelModify, String labelDataUnmodified, String labelDataModified,
			String resturantDbCount, String resturantSiteCount, String resturantModify, String resturantDataUnmodified,
			String resturantDataModified, String linkDbCount, String linkSiteCount, String linkModify,
			String linkDataUnmodified, String linkDataModified, String postcodeDbCount, String postcodeSiteCount,
			String postcodeModify, String postcodeDataUnmodified, String postcodeDataModified, String levelDbCount,
			String levelSiteCount, String levelModify, String levelDataUnmodified, String levelDataModified,
			String collectorUserid, String collectorTime, String inputUserid, String inputTime, String qcUserid,
			String qcTime, String qcSubTaskid, String vision, String memo, String type, String memoUserid,
			String hasExport) {
		super();
		this.fid = fid;
		this.level = level;
		this.poiName = poiName;
		this.meshId = meshId;
		this.area = area;
		this.category = category;
		this.fullMatch = fullMatch;
		this.partialMatch = partialMatch;
		this.extra = extra;
		this.missing = missing;
		this.nameDbCount = nameDbCount;
		this.nameSiteCount = nameSiteCount;
		this.nameModify = nameModify;
		this.nameDataUnmodified = nameDataUnmodified;
		this.nameDataModified = nameDataModified;
		this.positionDbCount = positionDbCount;
		this.positionSiteCount = positionSiteCount;
		this.positionModify = positionModify;
		this.positionDataUnmodified = positionDataUnmodified;
		this.positionDataModified = positionDataModified;
		this.categoryDbCount = categoryDbCount;
		this.categorySiteCount = categorySiteCount;
		this.categoryModify = categoryModify;
		this.categoryDataUnmodified = categoryDataUnmodified;
		this.categoryDataModified = categoryDataModified;
		this.addressDbCount = addressDbCount;
		this.addressSiteCount = addressSiteCount;
		this.addressModify = addressModify;
		this.addressDataUnmodified = addressDataUnmodified;
		this.addressDataModified = addressDataModified;
		this.photeDbCount = photeDbCount;
		this.photeSiteCount = photeSiteCount;
		this.photeModify = photeModify;
		this.photeDataUnmodified = photeDataUnmodified;
		this.photeDataModified = photeDataModified;
		this.siteDbCount = siteDbCount;
		this.siteSiteCount = siteSiteCount;
		this.siteModify = siteModify;
		this.siteDataUnmodified = siteDataUnmodified;
		this.siteDataModified = siteDataModified;
		this.fatherSonDbCount = fatherSonDbCount;
		this.fatherSonSiteCount = fatherSonSiteCount;
		this.fatherSonModify = fatherSonModify;
		this.fatherSonDataUnmodified = fatherSonDataUnmodified;
		this.fatherSonDataModified = fatherSonDataModified;
		this.deepDbCount = deepDbCount;
		this.deepSiteCount = deepSiteCount;
		this.deepModify = deepModify;
		this.deepDataUnmodified = deepDataUnmodified;
		this.deepDataModified = deepDataModified;
		this.labelDbCount = labelDbCount;
		this.labelSiteCount = labelSiteCount;
		this.labelModify = labelModify;
		this.labelDataUnmodified = labelDataUnmodified;
		this.labelDataModified = labelDataModified;
		this.resturantDbCount = resturantDbCount;
		this.resturantSiteCount = resturantSiteCount;
		this.resturantModify = resturantModify;
		this.resturantDataUnmodified = resturantDataUnmodified;
		this.resturantDataModified = resturantDataModified;
		this.linkDbCount = linkDbCount;
		this.linkSiteCount = linkSiteCount;
		this.linkModify = linkModify;
		this.linkDataUnmodified = linkDataUnmodified;
		this.linkDataModified = linkDataModified;
		this.postcodeDbCount = postcodeDbCount;
		this.postcodeSiteCount = postcodeSiteCount;
		this.postcodeModify = postcodeModify;
		this.postcodeDataUnmodified = postcodeDataUnmodified;
		this.postcodeDataModified = postcodeDataModified;
		this.levelDbCount = levelDbCount;
		this.levelSiteCount = levelSiteCount;
		this.levelModify = levelModify;
		this.levelDataUnmodified = levelDataUnmodified;
		this.levelDataModified = levelDataModified;
		this.collectorUserid = collectorUserid;
		this.collectorTime = collectorTime;
		this.inputUserid = inputUserid;
		this.inputTime = inputTime;
		this.qcUserid = qcUserid;
		this.qcTime = qcTime;
		this.qcSubTaskid = qcSubTaskid;
		this.vision = vision;
		this.memo = memo;
		this.type = type;
		this.memoUserid = memoUserid;
		this.hasExport = hasExport;
	}



	public String getFid() {
		return fid;
	}

	public void setFid(String fid) {
		this.fid = fid;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getPoiName() {
		return poiName;
	}

	public void setPoiName(String poiName) {
		this.poiName = poiName;
	}

	public String getMeshId() {
		return meshId;
	}

	public void setMeshId(String meshId) {
		this.meshId = meshId;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getFullMatch() {
		return fullMatch;
	}

	public void setFullMatch(String fullMatch) {
		this.fullMatch = fullMatch;
	}

	public String getPartialMatch() {
		return partialMatch;
	}

	public void setPartialMatch(String partialMatch) {
		this.partialMatch = partialMatch;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public String getMissing() {
		return missing;
	}

	public void setMissing(String missing) {
		this.missing = missing;
	}

	public String getNameDbCount() {
		return nameDbCount;
	}

	public void setNameDbCount(String nameDbCount) {
		this.nameDbCount = nameDbCount;
	}

	public String getNameSiteCount() {
		return nameSiteCount;
	}

	public void setNameSiteCount(String nameSiteCount) {
		this.nameSiteCount = nameSiteCount;
	}

	public String getNameModify() {
		return nameModify;
	}

	public void setNameModify(String nameModify) {
		this.nameModify = nameModify;
	}

	public String getNameDataUnmodified() {
		return nameDataUnmodified;
	}

	public void setNameDataUnmodified(String nameDataUnmodified) {
		this.nameDataUnmodified = nameDataUnmodified;
	}

	public String getNameDataModified() {
		return nameDataModified;
	}

	public void setNameDataModified(String nameDataModified) {
		this.nameDataModified = nameDataModified;
	}

	public String getPositionDbCount() {
		return positionDbCount;
	}

	public void setPositionDbCount(String positionDbCount) {
		this.positionDbCount = positionDbCount;
	}

	public String getPositionSiteCount() {
		return positionSiteCount;
	}

	public void setPositionSiteCount(String positionSiteCount) {
		this.positionSiteCount = positionSiteCount;
	}

	public String getPositionModify() {
		return positionModify;
	}

	public void setPositionModify(String positionModify) {
		this.positionModify = positionModify;
	}

	public String getPositionDataUnmodified() {
		return positionDataUnmodified;
	}

	public void setPositionDataUnmodified(String positionDataUnmodified) {
		this.positionDataUnmodified = positionDataUnmodified;
	}

	public String getPositionDataModified() {
		return positionDataModified;
	}

	public void setPositionDataModified(String positionDataModified) {
		this.positionDataModified = positionDataModified;
	}

	public String getCategoryDbCount() {
		return categoryDbCount;
	}

	public void setCategoryDbCount(String categoryDbCount) {
		this.categoryDbCount = categoryDbCount;
	}

	public String getCategorySiteCount() {
		return categorySiteCount;
	}

	public void setCategorySiteCount(String categorySiteCount) {
		this.categorySiteCount = categorySiteCount;
	}

	public String getCategoryModify() {
		return categoryModify;
	}

	public void setCategoryModify(String categoryModify) {
		this.categoryModify = categoryModify;
	}

	public String getCategoryDataUnmodified() {
		return categoryDataUnmodified;
	}

	public void setCategoryDataUnmodified(String categoryDataUnmodified) {
		this.categoryDataUnmodified = categoryDataUnmodified;
	}

	public String getCategoryDataModified() {
		return categoryDataModified;
	}

	public void setCategoryDataModified(String categoryDataModified) {
		this.categoryDataModified = categoryDataModified;
	}

	public String getAddressDbCount() {
		return addressDbCount;
	}

	public void setAddressDbCount(String addressDbCount) {
		this.addressDbCount = addressDbCount;
	}

	public String getAddressSiteCount() {
		return addressSiteCount;
	}

	public void setAddressSiteCount(String addressSiteCount) {
		this.addressSiteCount = addressSiteCount;
	}

	public String getAddressModify() {
		return addressModify;
	}

	public void setAddressModify(String addressModify) {
		this.addressModify = addressModify;
	}

	public String getAddressDataUnmodified() {
		return addressDataUnmodified;
	}

	public void setAddressDataUnmodified(String addressDataUnmodified) {
		this.addressDataUnmodified = addressDataUnmodified;
	}

	public String getAddressDataModified() {
		return addressDataModified;
	}

	public void setAddressDataModified(String addressDataModified) {
		this.addressDataModified = addressDataModified;
	}

	public String getPhoteDbCount() {
		return photeDbCount;
	}

	public void setPhoteDbCount(String photeDbCount) {
		this.photeDbCount = photeDbCount;
	}

	public String getPhoteSiteCount() {
		return photeSiteCount;
	}

	public void setPhoteSiteCount(String photeSiteCount) {
		this.photeSiteCount = photeSiteCount;
	}

	public String getPhoteModify() {
		return photeModify;
	}

	public void setPhoteModify(String photeModify) {
		this.photeModify = photeModify;
	}

	public String getPhoteDataUnmodified() {
		return photeDataUnmodified;
	}

	public void setPhoteDataUnmodified(String photeDataUnmodified) {
		this.photeDataUnmodified = photeDataUnmodified;
	}

	public String getPhoteDataModified() {
		return photeDataModified;
	}

	public void setPhoteDataModified(String photeDataModified) {
		this.photeDataModified = photeDataModified;
	}

	public String getSiteDbCount() {
		return siteDbCount;
	}

	public void setSiteDbCount(String siteDbCount) {
		this.siteDbCount = siteDbCount;
	}

	public String getSiteSiteCount() {
		return siteSiteCount;
	}

	public void setSiteSiteCount(String siteSiteCount) {
		this.siteSiteCount = siteSiteCount;
	}

	public String getSiteModify() {
		return siteModify;
	}

	public void setSiteModify(String siteModify) {
		this.siteModify = siteModify;
	}

	public String getSiteDataUnmodified() {
		return siteDataUnmodified;
	}

	public void setSiteDataUnmodified(String siteDataUnmodified) {
		this.siteDataUnmodified = siteDataUnmodified;
	}

	public String getSiteDataModified() {
		return siteDataModified;
	}

	public void setSiteDataModified(String siteDataModified) {
		this.siteDataModified = siteDataModified;
	}

	public String getFatherSonDbCount() {
		return fatherSonDbCount;
	}

	public void setFatherSonDbCount(String fatherSonDbCount) {
		this.fatherSonDbCount = fatherSonDbCount;
	}

	public String getFatherSonSiteCount() {
		return fatherSonSiteCount;
	}

	public void setFatherSonSiteCount(String fatherSonSiteCount) {
		this.fatherSonSiteCount = fatherSonSiteCount;
	}

	public String getFatherSonModify() {
		return fatherSonModify;
	}

	public void setFatherSonModify(String fatherSonModify) {
		this.fatherSonModify = fatherSonModify;
	}

	public String getFatherSonDataUnmodified() {
		return fatherSonDataUnmodified;
	}

	public void setFatherSonDataUnmodified(String fatherSonDataUnmodified) {
		this.fatherSonDataUnmodified = fatherSonDataUnmodified;
	}

	public String getFatherSonDataModified() {
		return fatherSonDataModified;
	}

	public void setFatherSonDataModified(String fatherSonDataModified) {
		this.fatherSonDataModified = fatherSonDataModified;
	}

	public String getDeepDbCount() {
		return deepDbCount;
	}

	public void setDeepDbCount(String deepDbCount) {
		this.deepDbCount = deepDbCount;
	}

	public String getDeepSiteCount() {
		return deepSiteCount;
	}

	public void setDeepSiteCount(String deepSiteCount) {
		this.deepSiteCount = deepSiteCount;
	}

	public String getDeepModify() {
		return deepModify;
	}

	public void setDeepModify(String deepModify) {
		this.deepModify = deepModify;
	}

	public String getDeepDataUnmodified() {
		return deepDataUnmodified;
	}

	public void setDeepDataUnmodified(String deepDataUnmodified) {
		this.deepDataUnmodified = deepDataUnmodified;
	}

	public String getDeepDataModified() {
		return deepDataModified;
	}

	public void setDeepDataModified(String deepDataModified) {
		this.deepDataModified = deepDataModified;
	}

	public String getLabelDbCount() {
		return labelDbCount;
	}

	public void setLabelDbCount(String labelDbCount) {
		this.labelDbCount = labelDbCount;
	}

	public String getLabelSiteCount() {
		return labelSiteCount;
	}

	public void setLabelSiteCount(String labelSiteCount) {
		this.labelSiteCount = labelSiteCount;
	}

	public String getLabelModify() {
		return labelModify;
	}

	public void setLabelModify(String labelModify) {
		this.labelModify = labelModify;
	}

	public String getLabelDataUnmodified() {
		return labelDataUnmodified;
	}

	public void setLabelDataUnmodified(String labelDataUnmodified) {
		this.labelDataUnmodified = labelDataUnmodified;
	}

	public String getLabelDataModified() {
		return labelDataModified;
	}

	public void setLabelDataModified(String labelDataModified) {
		this.labelDataModified = labelDataModified;
	}

	public String getResturantDbCount() {
		return resturantDbCount;
	}

	public void setResturantDbCount(String resturantDbCount) {
		this.resturantDbCount = resturantDbCount;
	}

	public String getResturantSiteCount() {
		return resturantSiteCount;
	}

	public void setResturantSiteCount(String resturantSiteCount) {
		this.resturantSiteCount = resturantSiteCount;
	}

	public String getResturantModify() {
		return resturantModify;
	}

	public void setResturantModify(String resturantModify) {
		this.resturantModify = resturantModify;
	}

	public String getResturantDataUnmodified() {
		return resturantDataUnmodified;
	}

	public void setResturantDataUnmodified(String resturantDataUnmodified) {
		this.resturantDataUnmodified = resturantDataUnmodified;
	}

	public String getResturantDataModified() {
		return resturantDataModified;
	}

	public void setResturantDataModified(String resturantDataModified) {
		this.resturantDataModified = resturantDataModified;
	}

	public String getLinkDbCount() {
		return linkDbCount;
	}

	public void setLinkDbCount(String linkDbCount) {
		this.linkDbCount = linkDbCount;
	}

	public String getLinkSiteCount() {
		return linkSiteCount;
	}

	public void setLinkSiteCount(String linkSiteCount) {
		this.linkSiteCount = linkSiteCount;
	}

	public String getLinkModify() {
		return linkModify;
	}

	public void setLinkModify(String linkModify) {
		this.linkModify = linkModify;
	}

	public String getLinkDataUnmodified() {
		return linkDataUnmodified;
	}

	public void setLinkDataUnmodified(String linkDataUnmodified) {
		this.linkDataUnmodified = linkDataUnmodified;
	}

	public String getLinkDataModified() {
		return linkDataModified;
	}

	public void setLinkDataModified(String linkDataModified) {
		this.linkDataModified = linkDataModified;
	}

	public String getPostcodeDbCount() {
		return postcodeDbCount;
	}

	public void setPostcodeDbCount(String postcodeDbCount) {
		this.postcodeDbCount = postcodeDbCount;
	}

	public String getPostcodeSiteCount() {
		return postcodeSiteCount;
	}

	public void setPostcodeSiteCount(String postcodeSiteCount) {
		this.postcodeSiteCount = postcodeSiteCount;
	}

	public String getPostcodeModify() {
		return postcodeModify;
	}

	public void setPostcodeModify(String postcodeModify) {
		this.postcodeModify = postcodeModify;
	}

	public String getPostcodeDataUnmodified() {
		return postcodeDataUnmodified;
	}

	public void setPostcodeDataUnmodified(String postcodeDataUnmodified) {
		this.postcodeDataUnmodified = postcodeDataUnmodified;
	}

	public String getPostcodeDataModified() {
		return postcodeDataModified;
	}

	public void setPostcodeDataModified(String postcodeDataModified) {
		this.postcodeDataModified = postcodeDataModified;
	}

	public String getLevelDbCount() {
		return levelDbCount;
	}

	public void setLevelDbCount(String levelDbCount) {
		this.levelDbCount = levelDbCount;
	}

	public String getLevelSiteCount() {
		return levelSiteCount;
	}

	public void setLevelSiteCount(String levelSiteCount) {
		this.levelSiteCount = levelSiteCount;
	}

	public String getLevelModify() {
		return levelModify;
	}

	public void setLevelModify(String levelModify) {
		this.levelModify = levelModify;
	}

	public String getLevelDataUnmodified() {
		return levelDataUnmodified;
	}

	public void setLevelDataUnmodified(String levelDataUnmodified) {
		this.levelDataUnmodified = levelDataUnmodified;
	}

	public String getLevelDataModified() {
		return levelDataModified;
	}

	public void setLevelDataModified(String levelDataModified) {
		this.levelDataModified = levelDataModified;
	}

	public String getCollectorUserid() {
		return collectorUserid;
	}

	public void setCollectorUserid(String collectorUserid) {
		this.collectorUserid = collectorUserid;
	}

	public String getCollectorTime() {
		return collectorTime;
	}

	public void setCollectorTime(String collectorTime) {
		this.collectorTime = collectorTime;
	}

	public String getInputUserid() {
		return inputUserid;
	}

	public void setInputUserid(String inputUserid) {
		this.inputUserid = inputUserid;
	}

	public String getInputTime() {
		return inputTime;
	}

	public void setInputTime(String inputTime) {
		this.inputTime = inputTime;
	}

	public String getQcUserid() {
		return qcUserid;
	}

	public void setQcUserid(String qcUserid) {
		this.qcUserid = qcUserid;
	}

	public String getQcTime() {
		return qcTime;
	}

	public void setQcTime(String qcTime) {
		this.qcTime = qcTime;
	}

	public String getQcSubTaskid() {
		return qcSubTaskid;
	}

	public void setQcSubTaskid(String qcSubTaskid) {
		this.qcSubTaskid = qcSubTaskid;
	}

	public String getVision() {
		return vision;
	}

	public void setVision(String vision) {
		this.vision = vision;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMemoUserid() {
		return memoUserid;
	}

	public void setMemoUserid(String memoUserid) {
		this.memoUserid = memoUserid;
	}

	public String getHasExport() {
		return hasExport;
	}

	public void setHasExport(String hasExport) {
		this.hasExport = hasExport;
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return "";
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
