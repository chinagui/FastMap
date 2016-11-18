package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.editplus.model.AbstractIx;

/** 
* @ClassName:  IxPoi 
* @author code generator
* @date 2016-11-16 02:22:30 
* @Description: TODO
*/
public class IxPoi extends AbstractIx {
	protected String kindCode ;
	protected Integer side ;
	protected long nameGroupid ;
	protected Integer roadFlag ;
	protected Integer pmeshId ;
	protected Integer adminReal ;
	protected Integer importance ;
	protected String chain ;
	protected String airportCode ;
	protected Integer accessFlag ;
	protected Integer open24h ;
	protected String meshId5k ;
	protected long regionId ;
	protected String postCode ;
	protected Integer editFlag ;
	protected String difGroupid ;
	protected String reserved ;
	protected Integer state ;
	protected String fieldState ;
	protected String label ;
	protected Integer type ;
	protected Integer addressFlag ;
	protected String exPriority ;
	protected String editionFlag ;
	protected String poiMemo ;
	protected String oldBlockcode ;
	protected String oldName ;
	protected String oldAddress ;
	protected String oldKind ;
	protected String poiNum ;
	protected String log ;
	protected long taskId ;
	protected String dataVersion ;
	protected long fieldTaskId ;
	protected Integer verifiedFlag ;
	protected String collectTime ;
	protected Integer geoAdjustFlag ;
	protected Integer fullAttrFlag ;
	protected double oldXGuide ;
	protected double oldYGuide ;
	protected Integer truckFlag ;
	protected String level ;
	protected String sportsVenue ;
	protected Integer indoor ;
	protected String vipFlag ;
	
	public IxPoi (long objPid){
		super(objPid);
	}
	


	public String getKindCode() {
		return kindCode;
	}
	protected void setKindCode(String kindCode) {
		this.kindCode = kindCode;
	}
	public Integer getSide() {
		return side;
	}
	protected void setSide(Integer side) {
		this.side = side;
	}
	public long getNameGroupid() {
		return nameGroupid;
	}
	protected void setNameGroupid(long nameGroupid) {
		this.nameGroupid = nameGroupid;
	}
	public Integer getRoadFlag() {
		return roadFlag;
	}
	protected void setRoadFlag(Integer roadFlag) {
		this.roadFlag = roadFlag;
	}
	public Integer getPmeshId() {
		return pmeshId;
	}
	protected void setPmeshId(Integer pmeshId) {
		this.pmeshId = pmeshId;
	}
	public Integer getAdminReal() {
		return adminReal;
	}
	protected void setAdminReal(Integer adminReal) {
		this.adminReal = adminReal;
	}
	public Integer getImportance() {
		return importance;
	}
	protected void setImportance(Integer importance) {
		this.importance = importance;
	}
	public String getChain() {
		return chain;
	}
	protected void setChain(String chain) {
		this.chain = chain;
	}
	public String getAirportCode() {
		return airportCode;
	}
	protected void setAirportCode(String airportCode) {
		this.airportCode = airportCode;
	}
	public Integer getAccessFlag() {
		return accessFlag;
	}
	protected void setAccessFlag(Integer accessFlag) {
		this.accessFlag = accessFlag;
	}
	public Integer getOpen24h() {
		return open24h;
	}
	protected void setOpen24h(Integer open24h) {
		this.open24h = open24h;
	}
	public String getMeshId5k() {
		return meshId5k;
	}
	protected void setMeshId5k(String meshId5k) {
		this.meshId5k = meshId5k;
	}
	public long getRegionId() {
		return regionId;
	}
	protected void setRegionId(long regionId) {
		this.regionId = regionId;
	}
	public String getPostCode() {
		return postCode;
	}
	protected void setPostCode(String postCode) {
		this.postCode = postCode;
	}
	public Integer getEditFlag() {
		return editFlag;
	}
	protected void setEditFlag(Integer editFlag) {
		this.editFlag = editFlag;
	}
	public String getDifGroupid() {
		return difGroupid;
	}
	protected void setDifGroupid(String difGroupid) {
		this.difGroupid = difGroupid;
	}
	public String getReserved() {
		return reserved;
	}
	protected void setReserved(String reserved) {
		this.reserved = reserved;
	}
	public Integer getState() {
		return state;
	}
	protected void setState(Integer state) {
		this.state = state;
	}
	public String getFieldState() {
		return fieldState;
	}
	protected void setFieldState(String fieldState) {
		this.fieldState = fieldState;
	}
	public String getLabel() {
		return label;
	}
	protected void setLabel(String label) {
		this.label = label;
	}
	public Integer getType() {
		return type;
	}
	protected void setType(Integer type) {
		this.type = type;
	}
	public Integer getAddressFlag() {
		return addressFlag;
	}
	protected void setAddressFlag(Integer addressFlag) {
		this.addressFlag = addressFlag;
	}
	public String getExPriority() {
		return exPriority;
	}
	protected void setExPriority(String exPriority) {
		this.exPriority = exPriority;
	}
	public String getEditionFlag() {
		return editionFlag;
	}
	protected void setEditionFlag(String editionFlag) {
		this.editionFlag = editionFlag;
	}
	public String getPoiMemo() {
		return poiMemo;
	}
	protected void setPoiMemo(String poiMemo) {
		this.poiMemo = poiMemo;
	}
	public String getOldBlockcode() {
		return oldBlockcode;
	}
	protected void setOldBlockcode(String oldBlockcode) {
		this.oldBlockcode = oldBlockcode;
	}
	public String getOldName() {
		return oldName;
	}
	protected void setOldName(String oldName) {
		this.oldName = oldName;
	}
	public String getOldAddress() {
		return oldAddress;
	}
	protected void setOldAddress(String oldAddress) {
		this.oldAddress = oldAddress;
	}
	public String getOldKind() {
		return oldKind;
	}
	protected void setOldKind(String oldKind) {
		this.oldKind = oldKind;
	}
	public String getPoiNum() {
		return poiNum;
	}
	protected void setPoiNum(String poiNum) {
		this.poiNum = poiNum;
	}
	public String getLog() {
		return log;
	}
	protected void setLog(String log) {
		this.log = log;
	}
	public long getTaskId() {
		return taskId;
	}
	protected void setTaskId(long taskId) {
		this.taskId = taskId;
	}
	public String getDataVersion() {
		return dataVersion;
	}
	protected void setDataVersion(String dataVersion) {
		this.dataVersion = dataVersion;
	}
	public long getFieldTaskId() {
		return fieldTaskId;
	}
	protected void setFieldTaskId(long fieldTaskId) {
		this.fieldTaskId = fieldTaskId;
	}
	public Integer getVerifiedFlag() {
		return verifiedFlag;
	}
	protected void setVerifiedFlag(Integer verifiedFlag) {
		this.verifiedFlag = verifiedFlag;
	}
	public String getCollectTime() {
		return collectTime;
	}
	protected void setCollectTime(String collectTime) {
		this.collectTime = collectTime;
	}
	public Integer getGeoAdjustFlag() {
		return geoAdjustFlag;
	}
	protected void setGeoAdjustFlag(Integer geoAdjustFlag) {
		this.geoAdjustFlag = geoAdjustFlag;
	}
	public Integer getFullAttrFlag() {
		return fullAttrFlag;
	}
	protected void setFullAttrFlag(Integer fullAttrFlag) {
		this.fullAttrFlag = fullAttrFlag;
	}
	public double getOldXGuide() {
		return oldXGuide;
	}
	protected void setOldXGuide(double oldXGuide) {
		this.oldXGuide = oldXGuide;
	}
	public double getOldYGuide() {
		return oldYGuide;
	}
	protected void setOldYGuide(double oldYGuide) {
		this.oldYGuide = oldYGuide;
	}
	public Integer getTruckFlag() {
		return truckFlag;
	}
	protected void setTruckFlag(Integer truckFlag) {
		this.truckFlag = truckFlag;
	}
	public String getLevel() {
		return level;
	}
	protected void setLevel(String level) {
		this.level = level;
	}
	public String getSportsVenue() {
		return sportsVenue;
	}
	protected void setSportsVenue(String sportsVenue) {
		this.sportsVenue = sportsVenue;
	}
	public Integer getIndoor() {
		return indoor;
	}
	protected void setIndoor(Integer indoor) {
		this.indoor = indoor;
	}
	public String getVipFlag() {
		return vipFlag;
	}
	protected void setVipFlag(String vipFlag) {
		this.vipFlag = vipFlag;
	}

	
	@Override
	public String tableName() {
		return "IX_POI";
	}
	
//	@Override
//	public String colName2Getter(String colName){
//		//handler open_24h...
//		return null;
//	}


//	@Override
//	public String getObjType() {
//		return ObjectType.IX_POI;
//	}

//	@Override
//	public long getGeoPid() {
//		return getObjPid();
//	}

//	@Override
//	public String getGeoType() {
//		return getObjType();
//	}

//	@Override
//	public String primaryKey() {
//		return "PID";
//	}


//	@Override
//	public boolean isGeoChanged() {
//		if(opType==OperationType.INSERT
//			||opType==OperationType.DELETE
//			||(opType==OperationType.UPDATE&&oldValues!=null&&oldValues.keySet().contains("GEOMETRY"))){
//			return true;
//		}
//		return false;
//	}
}
