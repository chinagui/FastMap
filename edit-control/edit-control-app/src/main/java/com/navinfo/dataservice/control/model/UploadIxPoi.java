package com.navinfo.dataservice.control.model;

import java.util.Set;

/** 
* @ClassName:  IxPoi 
* @author zl
* @date 2017-06-20
* @Description: TODO
*/
public class UploadIxPoi  {
	private String fid ;
	private String name ;
	private int pid ;
	private int meshid ;
	private String kindCode ;
	private UploadIxPoiGuide guide;
	private String address ;
	private String parentFid ;
	private String rawFields;
	private int t_lifecycle;
	private String geometry;
	private String t_operateDate;
	private int truck;
	private String sameFid;
	private String sourceName;
	
	private int side ;
	private long nameGroupid ;
	private int roadFlag ;
	private int pmeshId ;
	private int adminReal ;
	private int importance ;
	private String chain ;
	private String airportCode ;
	private int accessFlag ;
	private int open24h ;
	private String meshId5k ;
	private long regionId ;
	private String postCode ;
	private int editFlag =1 ;
	private String difGroupid ;
	private String reserved ;
	private int state ;
	private String fieldState ;
	private String label ;
	private int type ;
	private int addressFlag ;
	private String exPriority ;
	private String editionFlag ;
	private String poiMemo ;
	private String oldBlockcode ;
	
	private String oldKind ;
	private String log ;
	private long taskId ;
	private String dataVersion ;
	private long fieldTaskId ;
	private int verifiedFlag  = 9;
	private String collectTime ;
	private int geoAdjustFlag = 9;
	private int fullAttrFlag = 9;
	private int truckFlag ;
	private String level ;
	private String sportsVenue ;
	private String vipFlag ;
	
	private Set<UploadIxPoiRelateChildren> relateChildren;
	private Set<UploadIxPoiContacts> contacts;
	private UploadIxPoiFoodtypes foodtypes;
	private UploadIxPoiParkings parkings;
	private UploadIxPoiHotel hotel;
	private UploadIxPoiChargingStation chargingStation;
	private Set<UploadIxPoiChargingPole> chargingPole;
	private UploadIxPoiGasStation gasStation;
	private UploadIxPoiIndoor indoor;
	private Set<UploadIxPoiAttachments> attachments;
	
	
	public int getPid() {
		return pid;
	}
	public void setPid(int pid) {
		this.pid = pid;
	}
	
	public String getParentFid() {
		return parentFid;
	}
	public void setParentFid(String parentFid) {
		this.parentFid = parentFid;
	}
	public int getMeshid() {
		return meshid;
	}
	public void setMeshid(int meshid) {
		this.meshid = meshid;
	}
	
	public String getRawFields() {
		return rawFields;
	}
	public void setRawFields(String rawFields) {
		this.rawFields = rawFields;
	}
	
	public int getT_lifecycle() {
		return t_lifecycle;
	}
	public void setT_lifecycle(int t_lifecycle) {
		this.t_lifecycle = t_lifecycle;
	}
	
	public String getGeometry() {
		return geometry;
	}
	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}
	
	public String getSameFid() {
		return sameFid;
	}
	public void setSameFid(String sameFid) {
		this.sameFid = sameFid;
	}
	public UploadIxPoiGuide getGuide() {
		return guide;
	}
	
	public void setGuide(UploadIxPoiGuide guide) {
		this.guide = guide;
	}
	public String getT_operateDate() {
		return t_operateDate;
	}
	public void setT_operateDate(String t_operateDate) {
		this.t_operateDate = t_operateDate;
	}
	public int getTruck() {
		return truck;
	}
	public void setTruck(int truck) {
		this.truck = truck;
	}
	public UploadIxPoiIndoor getIndoor() {
		return indoor;
	}
	public void setIndoor(UploadIxPoiIndoor indoor) {
		this.indoor = indoor;
	}
	public Set<UploadIxPoiRelateChildren> getRelateChildren() {
		return relateChildren;
	}
	public void setRelateChildren(Set<UploadIxPoiRelateChildren> relateChildren) {
		this.relateChildren = relateChildren;
	}
	public Set<UploadIxPoiChargingPole> getChargingPole() {
		return chargingPole;
	}
	public void setChargingPole(Set<UploadIxPoiChargingPole> chargingPole) {
		this.chargingPole = chargingPole;
	}
	public Set<UploadIxPoiAttachments> getAttachments() {
		return attachments;
	}
	public void setAttachments(Set<UploadIxPoiAttachments> attachments) {
		this.attachments = attachments;
	}
	
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	public String getKindCode() {
		return kindCode;
	}
	public void setKindCode(String kindCode) {
			this.kindCode = kindCode;
	}
	public int getSide() {
		return side;
	}
	public void setSide(int side) {
			this.side = side;
	}
	public long getNameGroupid() {
		return nameGroupid;
	}
	public void setNameGroupid(long nameGroupid) {
			this.nameGroupid = nameGroupid;
	}
	public int getRoadFlag() {
		return roadFlag;
	}
	public void setRoadFlag(int roadFlag) {
			this.roadFlag = roadFlag;
	}
	public int getPmeshId() {
		return pmeshId;
	}
	public void setPmeshId(int pmeshId) {
			this.pmeshId = pmeshId;
	}
	public int getAdminReal() {
		return adminReal;
	}
	public void setAdminReal(int adminReal) {
			this.adminReal = adminReal;
	}
	public int getImportance() {
		return importance;
	}
	public void setImportance(int importance) {
			this.importance = importance;
	}
	public String getChain() {
		return chain;
	}
	public void setChain(String chain) {
			this.chain = chain;
	}
	public String getAirportCode() {
		return airportCode;
	}
	public void setAirportCode(String airportCode) {
			this.airportCode = airportCode;
	}
	public int getAccessFlag() {
		return accessFlag;
	}
	public void setAccessFlag(int accessFlag) {
			this.accessFlag = accessFlag;
	}
	public int getOpen24h() {
		return open24h;
	}
	public void setOpen24h(int open24h) {
			this.open24h = open24h;
	}
	public String getMeshId5k() {
		return meshId5k;
	}
	public void setMeshId5k(String meshId5k) {
			this.meshId5k = meshId5k;
	}
	public long getRegionId() {
		return regionId;
	}
	public void setRegionId(long regionId) {
			this.regionId = regionId;
	}
	public String getPostCode() {
		return postCode;
	}
	public void setPostCode(String postCode) {
			this.postCode = postCode;
	}
	public int getEditFlag() {
		return editFlag;
	}
	public void setEditFlag(int editFlag) {
			this.editFlag = editFlag;
	}
	public String getDifGroupid() {
		return difGroupid;
	}
	public void setDifGroupid(String difGroupid) {
			this.difGroupid = difGroupid;
	}
	public String getReserved() {
		return reserved;
	}
	public void setReserved(String reserved) {
			this.reserved = reserved;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
			this.state = state;
	}
	public String getFieldState() {
		return fieldState;
	}
	public void setFieldState(String fieldState) {
			this.fieldState = fieldState;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
			this.label = label;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
			this.type = type;
	}
	public int getAddressFlag() {
		return addressFlag;
	}
	public void setAddressFlag(int addressFlag) {
			this.addressFlag = addressFlag;
	}
	public String getExPriority() {
		return exPriority;
	}
	public void setExPriority(String exPriority) {
			this.exPriority = exPriority;
	}
	public String getEditionFlag() {
		return editionFlag;
	}
	public void setEditionFlag(String editionFlag) {
			this.editionFlag = editionFlag;
	}
	public String getPoiMemo() {
		return poiMemo;
	}
	public void setPoiMemo(String poiMemo) {
			this.poiMemo = poiMemo;
	}
	public String getOldBlockcode() {
		return oldBlockcode;
	}
	public void setOldBlockcode(String oldBlockcode) {
			this.oldBlockcode = oldBlockcode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
			this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
			this.address = address;
	}
	public String getOldKind() {
		return oldKind;
	}
	public void setOldKind(String oldKind) {
			this.oldKind = oldKind;
	}
	public String getFid() {
		return fid;
	}
	public void setFid(String fid) {
			this.fid = fid;
	}
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
			this.log = log;
	}
	public long getTaskId() {
		return taskId;
	}
	public void setTaskId(long taskId) {
			this.taskId = taskId;
	}
	public String getDataVersion() {
		return dataVersion;
	}
	public void setDataVersion(String dataVersion) {
			this.dataVersion = dataVersion;
	}
	public long getFieldTaskId() {
		return fieldTaskId;
	}
	public void setFieldTaskId(long fieldTaskId) {
			this.fieldTaskId = fieldTaskId;
	}
	public int getVerifiedFlag() {
		return verifiedFlag;
	}
	public void setVerifiedFlag(int verifiedFlag) {
			this.verifiedFlag = verifiedFlag;
	}
	public String getCollectTime() {
		return collectTime;
	}
	public void setCollectTime(String collectTime) {
			this.collectTime = collectTime;
	}
	public int getGeoAdjustFlag() {
		return geoAdjustFlag;
	}
	public void setGeoAdjustFlag(int geoAdjustFlag) {
			this.geoAdjustFlag = geoAdjustFlag;
	}
	public int getFullAttrFlag() {
		return fullAttrFlag;
	}
	public void setFullAttrFlag(int fullAttrFlag) {
			this.fullAttrFlag = fullAttrFlag;
	}
	public int getTruckFlag() {
		return truckFlag;
	}
	public void setTruckFlag(int truckFlag) {
			this.truckFlag = truckFlag;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
			this.level = level;
	}
	public String getSportsVenue() {
		return sportsVenue;
	}
	public void setSportsVenue(String sportsVenue) {
			this.sportsVenue = sportsVenue;
	}
	public String getVipFlag() {
		return vipFlag;
	}
	public void setVipFlag(String vipFlag) {
			this.vipFlag = vipFlag;
	}
	public Set<UploadIxPoiContacts> getContacts() {
		return contacts;
	}
	public void setContacts(Set<UploadIxPoiContacts> contacts) {
		this.contacts = contacts;
	}
	public UploadIxPoiFoodtypes getFoodtypes() {
		return foodtypes;
	}
	public void setFoodtypes(UploadIxPoiFoodtypes foodtypes) {
		this.foodtypes = foodtypes;
	}
	public UploadIxPoiParkings getParkings() {
		return parkings;
	}
	public void setParkings(UploadIxPoiParkings parkings) {
		this.parkings = parkings;
	}
	public UploadIxPoiHotel getHotel() {
		return hotel;
	}
	public void setHotel(UploadIxPoiHotel hotel) {
		this.hotel = hotel;
	}
	public UploadIxPoiChargingStation getChargingStation() {
		return chargingStation;
	}
	public void setChargingStation(UploadIxPoiChargingStation chargingStation) {
		this.chargingStation = chargingStation;
	}
	public UploadIxPoiGasStation getGasStation() {
		return gasStation;
	}
	public void setGasStation(UploadIxPoiGasStation gasStation) {
		this.gasStation = gasStation;
	}
	
	
	
}
