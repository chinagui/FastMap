package com.navinfo.dataservice.job.statics.model;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONObject;

/**
 * 
 * @Title:PoiCountTable
 * @Package:com.navinfo.dataservice.job.statics.model
 * @Description: 
 * @author:Jarvis 
 * @date: 2017年10月24日
 */
public class PoiCountTable implements IObj{
	private String fid = "null";
	private String level = "null";
	private String poiName = "null";
	private String meshId = "null";
	private String category = "null";
	private String nameSiteCount = "null";
	private String positionSiteCount = "null";
	private String categorySiteCount = "null";
	private String addressSiteCount = "null";
	private String photeSiteCount = "null";
	private String sideSiteCount = "null";
	private String fatherSonSiteCount = "null";
	private String deepSiteCount = "null";
	private String labelSiteCount = "null";
	private String resturantSiteCount = "null";
	private String linkSiteCount = "null";
	private String postcodeSiteCount = "null";
	private String levelSiteCount = "null";
	private String collectorUserid = "null";
	private String collectorTime = "null";
	private String inputUserid = "null";
	private String inputTime = "null";
	private String qcUserid = "null";
	private String qcTime = "null";
	private String qcSubTaskid = "null";
	private String vision = "null";
	private String memo = "null";
	private String type = "null";
	private String memoUserid = "null";

	public PoiCountTable() {
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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}


	public String getNameSiteCount() {
		return nameSiteCount;
	}

	public void setNameSiteCount(String nameSiteCount) {
		this.nameSiteCount = nameSiteCount;
	}


	public String getPositionSiteCount() {
		return positionSiteCount;
	}

	public void setPositionSiteCount(String positionSiteCount) {
		this.positionSiteCount = positionSiteCount;
	}



	public String getCategorySiteCount() {
		return categorySiteCount;
	}

	public void setCategorySiteCount(String categorySiteCount) {
		this.categorySiteCount = categorySiteCount;
	}



	public String getAddressSiteCount() {
		return addressSiteCount;
	}

	public void setAddressSiteCount(String addressSiteCount) {
		this.addressSiteCount = addressSiteCount;
	}


	public String getPhoteSiteCount() {
		return photeSiteCount;
	}




	public void setPhoteSiteCount(String photeSiteCount) {
		this.photeSiteCount = photeSiteCount;
	}





	public String getFatherSonSiteCount() {
		return fatherSonSiteCount;
	}




	public String getSideSiteCount() {
		return sideSiteCount;
	}


	public void setSideSiteCount(String sideSiteCount) {
		this.sideSiteCount = sideSiteCount;
	}


	public void setFatherSonSiteCount(String fatherSonSiteCount) {
		this.fatherSonSiteCount = fatherSonSiteCount;
	}




	public String getDeepSiteCount() {
		return deepSiteCount;
	}




	public void setDeepSiteCount(String deepSiteCount) {
		this.deepSiteCount = deepSiteCount;
	}




	public String getLabelSiteCount() {
		return labelSiteCount;
	}




	public void setLabelSiteCount(String labelSiteCount) {
		this.labelSiteCount = labelSiteCount;
	}




	public String getResturantSiteCount() {
		return resturantSiteCount;
	}




	public void setResturantSiteCount(String resturantSiteCount) {
		this.resturantSiteCount = resturantSiteCount;
	}




	public String getLinkSiteCount() {
		return linkSiteCount;
	}




	public void setLinkSiteCount(String linkSiteCount) {
		this.linkSiteCount = linkSiteCount;
	}




	public String getPostcodeSiteCount() {
		return postcodeSiteCount;
	}




	public void setPostcodeSiteCount(String postcodeSiteCount) {
		this.postcodeSiteCount = postcodeSiteCount;
	}




	public String getLevelSiteCount() {
		return levelSiteCount;
	}




	public void setLevelSiteCount(String levelSiteCount) {
		this.levelSiteCount = levelSiteCount;
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
