package com.navinfo.dataservice.engine.editplus.model.ixpoi;

import com.navinfo.dataservice.engine.editplus.model.AbstractIx;
/** 
* @ClassName:  IxPoi 
* @author code generator
* @date 2016-11-18 11:18:40 
* @Description: TODO
*/
public class IxPoi extends AbstractIx {
	protected String kindCode ;
	protected int side ;
	protected long nameGroupid ;
	protected int roadFlag ;
	protected int pmeshId ;
	protected int adminReal ;
	protected int importance ;
	protected String chain ;
	protected String airportCode ;
	protected int accessFlag ;
	protected int open24h ;
	protected String meshId5k ;
	protected long regionId ;
	protected String postCode ;
	protected int editFlag ;
	protected String difGroupid ;
	protected String reserved ;
	protected int state ;
	protected String fieldState ;
	protected String label ;
	protected int type ;
	protected int addressFlag ;
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
	protected int verifiedFlag ;
	protected String collectTime ;
	protected int geoAdjustFlag ;
	protected int fullAttrFlag ;
	protected double oldXGuide ;
	protected double oldYGuide ;
	protected int truckFlag ;
	protected String level ;
	protected String sportsVenue ;
	protected int indoor ;
	protected String vipFlag ;
	
	public IxPoi (long objPid){
		super(objPid);
	}
	
	public String getKindCode() {
		return kindCode;
	}
	public void setKindCode(String kindCode) {
		if(this.checkValue("KIND_CODE",this.kindCode,kindCode)){
			this.kindCode = kindCode;
		}
	}
	public int getSide() {
		return side;
	}
	public void setSide(int side) {
		if(this.checkValue("SIDE",this.side,side)){
			this.side = side;
		}
	}
	public long getNameGroupid() {
		return nameGroupid;
	}
	public void setNameGroupid(long nameGroupid) {
		if(this.checkValue("NAME_GROUPID",this.nameGroupid,nameGroupid)){
			this.nameGroupid = nameGroupid;
		}
	}
	public int getRoadFlag() {
		return roadFlag;
	}
	public void setRoadFlag(int roadFlag) {
		if(this.checkValue("ROAD_FLAG",this.roadFlag,roadFlag)){
			this.roadFlag = roadFlag;
		}
	}
	public int getPmeshId() {
		return pmeshId;
	}
	public void setPmeshId(int pmeshId) {
		if(this.checkValue("PMESH_ID",this.pmeshId,pmeshId)){
			this.pmeshId = pmeshId;
		}
	}
	public int getAdminReal() {
		return adminReal;
	}
	public void setAdminReal(int adminReal) {
		if(this.checkValue("ADMIN_REAL",this.adminReal,adminReal)){
			this.adminReal = adminReal;
		}
	}
	public int getImportance() {
		return importance;
	}
	public void setImportance(int importance) {
		if(this.checkValue("IMPORTANCE",this.importance,importance)){
			this.importance = importance;
		}
	}
	public String getChain() {
		return chain;
	}
	public void setChain(String chain) {
		if(this.checkValue("CHAIN",this.chain,chain)){
			this.chain = chain;
		}
	}
	public String getAirportCode() {
		return airportCode;
	}
	public void setAirportCode(String airportCode) {
		if(this.checkValue("AIRPORT_CODE",this.airportCode,airportCode)){
			this.airportCode = airportCode;
		}
	}
	public int getAccessFlag() {
		return accessFlag;
	}
	public void setAccessFlag(int accessFlag) {
		if(this.checkValue("ACCESS_FLAG",this.accessFlag,accessFlag)){
			this.accessFlag = accessFlag;
		}
	}
	public int getOpen24h() {
		return open24h;
	}
	public void setOpen24h(int open24h) {
		if(this.checkValue("OPEN_24H",this.open24h,open24h)){
			this.open24h = open24h;
		}
	}
	public String getMeshId5k() {
		return meshId5k;
	}
	public void setMeshId5k(String meshId5k) {
		if(this.checkValue("MESH_ID_5K",this.meshId5k,meshId5k)){
			this.meshId5k = meshId5k;
		}
	}
	public int getMeshId() {
		return meshId;
	}
	public void setMeshId(int meshId) {
		if(this.checkValue("MESH_ID",this.meshId,meshId)){
			this.meshId = meshId;
		}
	}
	public long getRegionId() {
		return regionId;
	}
	public void setRegionId(long regionId) {
		if(this.checkValue("REGION_ID",this.regionId,regionId)){
			this.regionId = regionId;
		}
	}
	public String getPostCode() {
		return postCode;
	}
	public void setPostCode(String postCode) {
		if(this.checkValue("POST_CODE",this.postCode,postCode)){
			this.postCode = postCode;
		}
	}
	public int getEditFlag() {
		return editFlag;
	}
	public void setEditFlag(int editFlag) {
		if(this.checkValue("EDIT_FLAG",this.editFlag,editFlag)){
			this.editFlag = editFlag;
		}
	}
	public String getDifGroupid() {
		return difGroupid;
	}
	public void setDifGroupid(String difGroupid) {
		if(this.checkValue("DIF_GROUPID",this.difGroupid,difGroupid)){
			this.difGroupid = difGroupid;
		}
	}
	public String getReserved() {
		return reserved;
	}
	public void setReserved(String reserved) {
		if(this.checkValue("RESERVED",this.reserved,reserved)){
			this.reserved = reserved;
		}
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		if(this.checkValue("STATE",this.state,state)){
			this.state = state;
		}
	}
	public String getFieldState() {
		return fieldState;
	}
	public void setFieldState(String fieldState) {
		if(this.checkValue("FIELD_STATE",this.fieldState,fieldState)){
			this.fieldState = fieldState;
		}
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		if(this.checkValue("LABEL",this.label,label)){
			this.label = label;
		}
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		if(this.checkValue("TYPE",this.type,type)){
			this.type = type;
		}
	}
	public int getAddressFlag() {
		return addressFlag;
	}
	public void setAddressFlag(int addressFlag) {
		if(this.checkValue("ADDRESS_FLAG",this.addressFlag,addressFlag)){
			this.addressFlag = addressFlag;
		}
	}
	public String getExPriority() {
		return exPriority;
	}
	public void setExPriority(String exPriority) {
		if(this.checkValue("EX_PRIORITY",this.exPriority,exPriority)){
			this.exPriority = exPriority;
		}
	}
	public String getEditionFlag() {
		return editionFlag;
	}
	public void setEditionFlag(String editionFlag) {
		if(this.checkValue("EDITION_FLAG",this.editionFlag,editionFlag)){
			this.editionFlag = editionFlag;
		}
	}
	public String getPoiMemo() {
		return poiMemo;
	}
	public void setPoiMemo(String poiMemo) {
		if(this.checkValue("POI_MEMO",this.poiMemo,poiMemo)){
			this.poiMemo = poiMemo;
		}
	}
	public String getOldBlockcode() {
		return oldBlockcode;
	}
	public void setOldBlockcode(String oldBlockcode) {
		if(this.checkValue("OLD_BLOCKCODE",this.oldBlockcode,oldBlockcode)){
			this.oldBlockcode = oldBlockcode;
		}
	}
	public String getOldName() {
		return oldName;
	}
	public void setOldName(String oldName) {
		if(this.checkValue("OLD_NAME",this.oldName,oldName)){
			this.oldName = oldName;
		}
	}
	public String getOldAddress() {
		return oldAddress;
	}
	public void setOldAddress(String oldAddress) {
		if(this.checkValue("OLD_ADDRESS",this.oldAddress,oldAddress)){
			this.oldAddress = oldAddress;
		}
	}
	public String getOldKind() {
		return oldKind;
	}
	public void setOldKind(String oldKind) {
		if(this.checkValue("OLD_KIND",this.oldKind,oldKind)){
			this.oldKind = oldKind;
		}
	}
	public String getPoiNum() {
		return poiNum;
	}
	public void setPoiNum(String poiNum) {
		if(this.checkValue("POI_NUM",this.poiNum,poiNum)){
			this.poiNum = poiNum;
		}
	}
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
		if(this.checkValue("LOG",this.log,log)){
			this.log = log;
		}
	}
	public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long taskId) {
		if(this.checkValue("TASK_ID",this.taskId,taskId)){
			this.taskId = taskId;
		}
	}
	public String getDataVersion() {
		return dataVersion;
	}
	public void setDataVersion(String dataVersion) {
		if(this.checkValue("DATA_VERSION",this.dataVersion,dataVersion)){
			this.dataVersion = dataVersion;
		}
	}
	public long getFieldTaskId() {
		return fieldTaskId;
	}
	public void setFieldTaskId(long fieldTaskId) {
		if(this.checkValue("FIELD_TASK_ID",this.fieldTaskId,fieldTaskId)){
			this.fieldTaskId = fieldTaskId;
		}
	}
	public int getVerifiedFlag() {
		return verifiedFlag;
	}
	public void setVerifiedFlag(int verifiedFlag) {
		if(this.checkValue("VERIFIED_FLAG",this.verifiedFlag,verifiedFlag)){
			this.verifiedFlag = verifiedFlag;
		}
	}
	public String getCollectTime() {
		return collectTime;
	}
	public void setCollectTime(String collectTime) {
		if(this.checkValue("COLLECT_TIME",this.collectTime,collectTime)){
			this.collectTime = collectTime;
		}
	}
	public int getGeoAdjustFlag() {
		return geoAdjustFlag;
	}
	public void setGeoAdjustFlag(int geoAdjustFlag) {
		if(this.checkValue("GEO_ADJUST_FLAG",this.geoAdjustFlag,geoAdjustFlag)){
			this.geoAdjustFlag = geoAdjustFlag;
		}
	}
	public int getFullAttrFlag() {
		return fullAttrFlag;
	}
	public void setFullAttrFlag(int fullAttrFlag) {
		if(this.checkValue("FULL_ATTR_FLAG",this.fullAttrFlag,fullAttrFlag)){
			this.fullAttrFlag = fullAttrFlag;
		}
	}
	public double getOldXGuide() {
		return oldXGuide;
	}
	public void setOldXGuide(double oldXGuide) {
		if(this.checkValue("OLD_X_GUIDE",this.oldXGuide,oldXGuide)){
			this.oldXGuide = oldXGuide;
		}
	}
	public double getOldYGuide() {
		return oldYGuide;
	}
	public void setOldYGuide(double oldYGuide) {
		if(this.checkValue("OLD_Y_GUIDE",this.oldYGuide,oldYGuide)){
			this.oldYGuide = oldYGuide;
		}
	}
	public int getTruckFlag() {
		return truckFlag;
	}
	public void setTruckFlag(int truckFlag) {
		if(this.checkValue("TRUCK_FLAG",this.truckFlag,truckFlag)){
			this.truckFlag = truckFlag;
		}
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		if(this.checkValue("LEVEL",this.level,level)){
			this.level = level;
		}
	}
	public String getSportsVenue() {
		return sportsVenue;
	}
	public void setSportsVenue(String sportsVenue) {
		if(this.checkValue("SPORTS_VENUE",this.sportsVenue,sportsVenue)){
			this.sportsVenue = sportsVenue;
		}
	}
	public int getIndoor() {
		return indoor;
	}
	public void setIndoor(int indoor) {
		if(this.checkValue("INDOOR",this.indoor,indoor)){
			this.indoor = indoor;
		}
	}
	public String getVipFlag() {
		return vipFlag;
	}
	public void setVipFlag(String vipFlag) {
		if(this.checkValue("VIP_FLAG",this.vipFlag,vipFlag)){
			this.vipFlag = vipFlag;
		}
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
