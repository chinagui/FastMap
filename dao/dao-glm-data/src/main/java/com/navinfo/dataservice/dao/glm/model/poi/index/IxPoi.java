package com.navinfo.dataservice.dao.glm.model.poi.index;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.SerializeUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAdvertisement;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiAttraction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBuilding;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBusinessTime;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiCarrental;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlotPh;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiEvent;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiIntroduction;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiTourroute;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * POI基础信息表
 * 
 * @author zhangxiaolong
 * 
 */
public class IxPoi implements IObj {

	// POI号码
	private int pid;

	// 种别代码
	private String kindCode;

	// 显示坐标
	private Geometry geometry;

	// 引导X坐标
	private double xGuide = 0;

	// 引导Y坐标
	private double yGuide = 0;

	// 引导LINK
	private int linkPid = 0;

	// 位置关系
	private int side = 0;

	// 引导LINK名称
	private int nameGroupid = 0;

	// 道路标志
	private int roadFlag = 0;

	// 永久图幅号
	private int pmeshId = 0;

	// 真实城市
	private int adminReal = 0;

	// 重要度
	private int importance = 0;

	// 连锁品牌
	private String chain;

	// 机场代码
	private String airportCode;

	// 出入口标识
	private int accessFlag = 0;

	// 全天营业
	private int open24h = 0;

	// 详细图幅
	private String meshId5k;

	// MESH_ID
	private int meshId = 0;

	// 区划号码
	private int regionId = 0;

	// 邮政编码
	private String postCode;

	// 差分产品ID
	private String difGroupid;

	// 编辑标识
	private int editFlag = 1;

	// 预留信息
	private String reserved;

	// 记录状态
	private int state = 0;

	// 字段状态
	private String fieldState;

	// 标记
	private String label;

	// POI类型
	private int type = 0;

	// 地址标志
	private int addressFlag = 0;

	// 提取优先级
	private String exPriority;

	// 作业季标识
	private String editionFlag;

	// 备注信息
	private String poiMemo;

	// OLD乡镇
	private String oldBlockcode;

	// OLD名称
	private String oldName;

	// OLD地址
	private String oldAddress;

	// OLD种别
	private String oldKind;

	// POI编号
	private String poiNum;

	// 外业LOG
	private String log;

	// 任务编号
	private int taskId = 0;

	// 数据采集版本
	private String dataVersion;

	// 外业任务编号
	private int fieldTaskId = 0;

	// 验证标识
	private int verifiedFlag = 9;

	// 采集更新时间
	private String collectTime;

	// 几何调整标识
	private int geoAdjustFlag = 9;

	// 精编标识
	private int fullAttrFlag = 9;

	// 外业采集 引导X坐标
	private double oldXGuide = 0;

	// 外业采集 引导Y坐标
	private double oldYGuide = 0;

	// 行记录ID
	private String rowId;

	// 更新记录*
	private int uRecord;
	// POI等级

	private String level;
	// 运动场馆
	private String sportsVenue;
	// 内部标示
	private int indoor = 0;
	// VIP表示
	private String vipFlag;
	// 更新时间
	private String uDate;

	// POI状态：1：待作业,2：已作业,3：已提交
	private int status;
	
	//卡车标识
	private int truckFlag = 0;
	
	//app修改标识
	protected String rawFields;

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	

	public String getSportsVenue() {
		return sportsVenue;
	}

	public void setSportsVenue(String sportsVenue) {
		this.sportsVenue = sportsVenue;
	}

	public int getIndoor() {
		return indoor;
	}

	public void setIndoor(int indoor) {
		this.indoor = indoor;
	}

	public String getVipFlag() {
		return vipFlag;
	}

	public void setVipFlag(String vipFlag) {
		this.vipFlag = vipFlag;
	}

	public String getuDate() {
		return uDate;
	}

	public void setuDate(String uDate) {
		this.uDate = uDate;
	}

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	private List<IRow> addresses = new ArrayList<IRow>();

	public Map<String, IxPoiAddress> addressMap = new HashMap<String, IxPoiAddress>();

	private List<IRow> audioes = new ArrayList<IRow>();

	public Map<String, IxPoiAudio> audioMap = new HashMap<String, IxPoiAudio>();

	private List<IRow> contacts = new ArrayList<IRow>();

	public Map<String, IxPoiContact> contactMap = new HashMap<String, IxPoiContact>();

	private List<IRow> entryImages = new ArrayList<IRow>();

	public Map<String, IxPoiEntryimage> entryImageMap = new HashMap<String, IxPoiEntryimage>();

	private List<IRow> flags = new ArrayList<IRow>();

	public Map<String, IxPoiFlag> flagMap = new HashMap<String, IxPoiFlag>();

	private List<IRow> icons = new ArrayList<IRow>();

	public Map<String, IxPoiIcon> iconMap = new HashMap<String, IxPoiIcon>();

	private List<IRow> names = new ArrayList<IRow>();

	public Map<String, IxPoiName> nameMap = new HashMap<String, IxPoiName>();

	private List<IRow> parents = new ArrayList<IRow>();

	public Map<String, IxPoiParent> parentMap = new HashMap<String, IxPoiParent>();

	private List<IRow> children = new ArrayList<IRow>();

	public Map<String, IxPoiChildren> childrenMap = new HashMap<String, IxPoiChildren>();

	private List<IRow> photos = new ArrayList<IRow>();

	public Map<String, IxPoiPhoto> photoMap = new HashMap<String, IxPoiPhoto>();

	private List<IRow> videoes = new ArrayList<IRow>();

	public Map<String, IxPoiVideo> videoMap = new HashMap<String, IxPoiVideo>();

	private List<IRow> parkings = new ArrayList<IRow>();

	public Map<String, IxPoiParking> parkingMap = new HashMap<String, IxPoiParking>();

	private List<IRow> tourroutes = new ArrayList<IRow>();

	public Map<String, IxPoiTourroute> tourrouteMap = new HashMap<String, IxPoiTourroute>();

	private List<IRow> events = new ArrayList<IRow>();

	public Map<String, IxPoiEvent> eventMap = new HashMap<String, IxPoiEvent>();

	private List<IRow> details = new ArrayList<IRow>();

	public Map<String, IxPoiDetail> detailMap = new HashMap<String, IxPoiDetail>();

	private List<IRow> businesstimes = new ArrayList<IRow>();

	public Map<String, IxPoiBusinessTime> businesstimeMap = new HashMap<String, IxPoiBusinessTime>();

	private List<IRow> chargingstations = new ArrayList<IRow>();

	public Map<String, IxPoiChargingStation> chargingstationMap = new HashMap<String, IxPoiChargingStation>();

	private List<IRow> chargingplots = new ArrayList<IRow>();

	public Map<String, IxPoiChargingPlot> chargingplotMap = new HashMap<String, IxPoiChargingPlot>();

	private List<IRow> chargingplotPhs = new ArrayList<IRow>();

	public Map<String, IxPoiChargingPlotPh> chargingplotPhMap = new HashMap<String, IxPoiChargingPlotPh>();

	private List<IRow> buildings = new ArrayList<IRow>();

	public Map<String, IxPoiBuilding> buildingMap = new HashMap<String, IxPoiBuilding>();

	private List<IRow> advertisements = new ArrayList<IRow>();

	public Map<String, IxPoiAdvertisement> advertisementMap = new HashMap<String, IxPoiAdvertisement>();

	private List<IRow> gasstations = new ArrayList<IRow>();

	public Map<String, IxPoiGasstation> gasstationMap = new HashMap<String, IxPoiGasstation>();

	private List<IRow> introductions = new ArrayList<IRow>();

	public Map<String, IxPoiIntroduction> introductionMap = new HashMap<String, IxPoiIntroduction>();

	private List<IRow> attractions = new ArrayList<IRow>();

	public Map<String, IxPoiAttraction> attractionMap = new HashMap<String, IxPoiAttraction>();

	private List<IRow> hotels = new ArrayList<IRow>();

	public Map<String, IxPoiHotel> hotelMap = new HashMap<String, IxPoiHotel>();

	private List<IRow> restaurants = new ArrayList<IRow>();

	public Map<String, IxPoiRestaurant> restaurantMap = new HashMap<String, IxPoiRestaurant>();

	private List<IRow> carrentals = new ArrayList<IRow>();

	public Map<String, IxPoiCarrental> carrentalMap = new HashMap<String, IxPoiCarrental>();

	private List<IRow> samepoiParts = new ArrayList<IRow>();

	public Map<String, IxSamepoiPart> samepoiMap = new HashMap<String, IxSamepoiPart>();

	private List<IRow> operateRefs = new ArrayList<IRow>();

	public List<IRow> getOperateRefs() {
		return operateRefs;
	}

	public void setOperateRefs(List<IRow> operateRefs) {
		this.operateRefs = operateRefs;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getKindCode() {
		return kindCode;
	}

	public void setKindCode(String kindCode) {
		this.kindCode = kindCode;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public double getxGuide() {
		return xGuide;
	}

	public void setxGuide(double xGuide) {
		this.xGuide = xGuide;
	}

	public double getyGuide() {
		return yGuide;
	}

	public void setyGuide(double yGuide) {
		this.yGuide = yGuide;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public int getSide() {
		return side;
	}

	public void setSide(int side) {
		this.side = side;
	}

	public int getNameGroupid() {
		return nameGroupid;
	}

	public void setNameGroupid(int nameGroupid) {
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

	public int getMeshId() {
		return meshId;
	}

	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}

	public int getRegionId() {
		return regionId;
	}

	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public String getDifGroupid() {
		return difGroupid;
	}

	public void setDifGroupid(String difGroupid) {
		this.difGroupid = difGroupid;
	}

	public int getEditFlag() {
		return editFlag;
	}

	public void setEditFlag(int editFlag) {
		this.editFlag = editFlag;
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

	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public String getOldAddress() {
		return oldAddress;
	}

	public void setOldAddress(String oldAddress) {
		this.oldAddress = oldAddress;
	}

	public String getOldKind() {
		return oldKind;
	}

	public void setOldKind(String oldKind) {
		this.oldKind = oldKind;
	}

	public String getPoiNum() {
		return poiNum;
	}

	public void setPoiNum(String poiNum) {
		this.poiNum = poiNum;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public String getDataVersion() {
		return dataVersion;
	}

	public void setDataVersion(String dataVersion) {
		this.dataVersion = dataVersion;
	}

	public int getFieldTaskId() {
		return fieldTaskId;
	}

	public void setFieldTaskId(int fieldTaskId) {
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

	public double getOldXGuide() {
		return oldXGuide;
	}

	public void setOldXGuide(double oldXGuide) {
		this.oldXGuide = oldXGuide;
	}

	public double getOldYGuide() {
		return oldYGuide;
	}

	public void setOldYGuide(double oldYGuide) {
		this.oldYGuide = oldYGuide;
	}

	public List<IRow> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<IRow> addresses) {
		this.addresses = addresses;
	}

	public List<IRow> getAudioes() {
		return audioes;
	}

	public void setAudioes(List<IRow> audioes) {
		this.audioes = audioes;
	}

	public List<IRow> getContacts() {
		return contacts;
	}

	public void setContacts(List<IRow> contacts) {
		this.contacts = contacts;
	}

	public List<IRow> getEntryImages() {
		return entryImages;
	}

	public void setEntryImages(List<IRow> entryImages) {
		this.entryImages = entryImages;
	}

	public List<IRow> getFlags() {
		return flags;
	}

	public void setFlags(List<IRow> flags) {
		this.flags = flags;
	}

	public List<IRow> getIcons() {
		return icons;
	}

	public void setIcons(List<IRow> icons) {
		this.icons = icons;
	}

	public List<IRow> getNames() {
		return names;
	}

	public void setNames(List<IRow> names) {
		this.names = names;
	}

	public List<IRow> getParents() {
		return parents;
	}

	public void setParents(List<IRow> parents) {
		this.parents = parents;
	}

	public List<IRow> getPhotos() {
		return photos;
	}

	public void setPhotos(List<IRow> photos) {
		this.photos = photos;
	}

	public List<IRow> getVideoes() {
		return videoes;
	}

	public void setVideoes(List<IRow> videoes) {
		this.videoes = videoes;
	}

	public List<IRow> getParkings() {
		return parkings;
	}

	public void setParkings(List<IRow> parkings) {
		this.parkings = parkings;
	}

	public List<IRow> getTourroutes() {
		return tourroutes;
	}

	public void setTourroutes(List<IRow> tourroutes) {
		this.tourroutes = tourroutes;
	}

	public List<IRow> getEvents() {
		return events;
	}

	public void setEvents(List<IRow> events) {
		this.events = events;
	}

	public List<IRow> getDetails() {
		return details;
	}

	public void setDetails(List<IRow> details) {
		this.details = details;
	}

	public List<IRow> getBusinesstimes() {
		return businesstimes;
	}

	public void setBusinesstimes(List<IRow> businesstimes) {
		this.businesstimes = businesstimes;
	}

	public List<IRow> getChargingstations() {
		return chargingstations;
	}

	public void setChargingstations(List<IRow> chargingstations) {
		this.chargingstations = chargingstations;
	}

	public List<IRow> getChargingplots() {
		return chargingplots;
	}

	public void setChargingplots(List<IRow> chargingplots) {
		this.chargingplots = chargingplots;
	}

	public List<IRow> getChargingplotPhs() {
		return chargingplotPhs;
	}

	public void setChargingplotPhs(List<IRow> chargingplotPhs) {
		this.chargingplotPhs = chargingplotPhs;
	}

	public List<IRow> getBuildings() {
		return buildings;
	}

	public void setBuildings(List<IRow> buildings) {
		this.buildings = buildings;
	}

	public List<IRow> getAdvertisements() {
		return advertisements;
	}

	public void setAdvertisements(List<IRow> advertisements) {
		this.advertisements = advertisements;
	}

	public List<IRow> getGasstations() {
		return gasstations;
	}

	public void setGasstations(List<IRow> gasstations) {
		this.gasstations = gasstations;
	}

	public List<IRow> getIntroductions() {
		return introductions;
	}

	public void setIntroductions(List<IRow> introductions) {
		this.introductions = introductions;
	}

	public List<IRow> getAttractions() {
		return attractions;
	}

	public void setAttractions(List<IRow> attractions) {
		this.attractions = attractions;
	}

	public List<IRow> getHotels() {
		return hotels;
	}

	public void setHotels(List<IRow> hotels) {
		this.hotels = hotels;
	}

	public List<IRow> getRestaurants() {
		return restaurants;
	}

	public void setRestaurants(List<IRow> restaurants) {
		this.restaurants = restaurants;
	}

	public List<IRow> getCarrentals() {
		return carrentals;
	}

	public void setCarrentals(List<IRow> carrentals) {
		this.carrentals = carrentals;
	}

	public String getRowId() {
		return rowId;
	}

	public int getuRecord() {
		return uRecord;
	}

	public void setuRecord(int uRecord) {
		this.uRecord = uRecord;
	}

	public List<IRow> getChildren() {
		return children;
	}

	public void setChildren(List<IRow> children) {
		this.children = children;
	}

	public List<IRow> getSamepoiParts() {
		return samepoiParts;
	}

	public void setSamepoiParts(List<IRow> samepoiParts) {
		this.samepoiParts = samepoiParts;
	}

	@Override
	public String rowId() {
		return this.rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	@Override
	public String tableName() {
		return "ix_poi";
	}

	@Override
	public ObjStatus status() {
		return null;
	}

	@Override
	public void setStatus(ObjStatus os) {
	}

	@Override
	public ObjType objType() {
		return ObjType.IXPOI;
	}

	@Override
	public void copy(IRow row) {
	}

	@Override
	public Map<String, Object> changedFields() {
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		return "pid";
	}

	@Override
	public int parentPKValue() {
		return this.pid;
	}

	@Override
	public String parentTableName() {
		return "ix_poi";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();
		// index
		children.add(this.getAddresses());
		children.add(this.getAudioes());
		children.add(this.getContacts());
		children.add(this.getEntryImages());
		children.add(this.getFlags());
		children.add(this.getIcons());
		children.add(this.getNames());
		children.add(this.getParents());
		children.add(this.getPhotos());
		children.add(this.getVideoes());
		children.add(this.getChildren());

		// deep
		children.add(this.getParkings());
		children.add(this.getTourroutes());
		children.add(this.getEvents());
		children.add(this.getDetails());
		children.add(this.getBusinesstimes());
		children.add(this.getChargingstations());
		children.add(this.getChargingplots());
		children.add(this.getChargingplotPhs());
		children.add(this.getBuildings());
		children.add(this.getAdvertisements());
		children.add(this.getGasstations());
		children.add(this.getIntroductions());
		children.add(this.getAttractions());
		children.add(this.getHotels());
		children.add(this.getRestaurants());
		children.add(this.getCarrentals());

		return children;
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
			} else {
				if (!"objStatus".equals(key)) {

					Field field = this.getClass().getDeclaredField(key);

					field.setAccessible(true);

					Object objValue = field.get(this);
					String newValue = json.getString(key);
					if("null".equalsIgnoreCase(newValue))newValue=null;
					if (!isEqualsString(objValue,newValue)) {
						Object value = json.get(key);

						if (value instanceof String) {
							changedFields.put(key, newValue.replace("'", "''"));
						} else {
							changedFields.put(key, value);
						}
					}
				}
			}
		}

		if (changedFields.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	private static boolean isEqualsString(Object oldValue,Object newValue){
		if(null==oldValue&&null==newValue)
			return true;
		if(StringUtils.isEmpty(oldValue)&&StringUtils.isEmpty(newValue)){
			return true;
		}
		if(oldValue==null&&newValue!=null){
			return false;
		}
		if(oldValue!=null&&newValue==null){
			return false;
		}
		return oldValue.equals(newValue);
	}
	
	@Override
	public int mesh() {
		return this.meshId;
	}

	@Override
	public void setMesh(int mesh) {
		this.meshId = mesh;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

		JSONObject json = JSONObject.fromObject(this, jsonConfig);

		return json;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		@SuppressWarnings("rawtypes")
		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {

				switch (key) {
				case "addresses":
					addresses.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiAddress row = new IxPoiAddress();

						row.Unserialize(jo);

						addresses.add(row);
					}

					break;
				case "contacts":
					contacts.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiContact row = new IxPoiContact();

						row.Unserialize(jo);

						contacts.add(row);
					}

					break;
				case "audioes":

					audioes.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiAudio row = new IxPoiAudio();

						row.Unserialize(jo);

						audioes.add(row);
					}

					break;

				case "entryImages":

					entryImages.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiEntryimage row = new IxPoiEntryimage();

						row.Unserialize(jo);

						entryImages.add(row);
					}

					break;
				case "flags":

					flags.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiFlag row = new IxPoiFlag();

						row.Unserialize(jo);

						flags.add(row);
					}

					break;
				case "icons":

					icons.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiIcon row = new IxPoiIcon();

						row.Unserialize(jo);

						icons.add(row);
					}

					break;
				case "names":

					names.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiName row = new IxPoiName();

						row.Unserialize(jo);

						names.add(row);
					}

					break;
				case "parents":

					parents.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiParent row = new IxPoiParent();

						row.Unserialize(jo);

						parents.add(row);
					}

					break;
				case "children":
					children.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiChildren row = new IxPoiChildren();

						row.Unserialize(jo);

						children.add(row);
					}

					break;
				case "photos":

					photos.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiPhoto row = new IxPoiPhoto();

						row.Unserialize(jo);

						photos.add(row);
					}

					break;
				case "parkings":

					parkings.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiParking row = new IxPoiParking();

						row.Unserialize(jo);

						parkings.add(row);
					}
					break;
				case "tourroutes":

					tourroutes.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiTourroute row = new IxPoiTourroute();

						row.Unserialize(jo);

						tourroutes.add(row);
					}
					break;
				case "events":

					events.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiEvent row = new IxPoiEvent();

						row.Unserialize(jo);

						events.add(row);
					}
					break;
				case "details":

					details.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiDetail row = new IxPoiDetail();

						row.Unserialize(jo);

						details.add(row);
					}
					break;
				case "businesstimes":

					businesstimes.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiBusinessTime row = new IxPoiBusinessTime();

						row.Unserialize(jo);

						businesstimes.add(row);
					}
					break;
				case "chargingstations":

					chargingstations.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiChargingStation row = new IxPoiChargingStation();

						row.Unserialize(jo);

						chargingstations.add(row);
					}
					break;
				case "chargingplots":

					chargingplots.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiChargingPlot row = new IxPoiChargingPlot();

						row.Unserialize(jo);

						chargingplots.add(row);
					}
					break;
				case "chargingplotPhs":

					chargingplotPhs.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiChargingPlotPh row = new IxPoiChargingPlotPh();

						row.Unserialize(jo);

						chargingplotPhs.add(row);
					}
					break;
				case "buildings":

					buildings.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiBuilding row = new IxPoiBuilding();

						row.Unserialize(jo);

						buildings.add(row);
					}
					break;
				case "advertisements":

					advertisements.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiAdvertisement row = new IxPoiAdvertisement();

						row.Unserialize(jo);

						advertisements.add(row);
					}
					break;
				case "gasstations":

					gasstations.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiGasstation row = new IxPoiGasstation();

						row.Unserialize(jo);

						gasstations.add(row);
					}
					break;
				case "introductions":

					introductions.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiIntroduction row = new IxPoiIntroduction();

						row.Unserialize(jo);

						introductions.add(row);
					}
					break;
				case "attractions":

					attractions.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiAttraction row = new IxPoiAttraction();

						row.Unserialize(jo);

						attractions.add(row);
					}
					break;
				case "hotels":

					hotels.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiHotel row = new IxPoiHotel();

						row.Unserialize(jo);

						hotels.add(row);
					}
					break;
				case "carrentals":

					carrentals.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiCarrental row = new IxPoiCarrental();

						row.Unserialize(jo);

						carrentals.add(row);
					}
					break;
				case "restaurants":

					restaurants.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						IxPoiRestaurant row = new IxPoiRestaurant();

						row.Unserialize(jo);

						restaurants.add(row);
					}
					break;
				default:
					break;
				}

			} else if ("geometry".equals(key)) {

				Geometry jts = GeoTranslator.geojson2Jts(json.getJSONObject(key), 100000, 0);

				this.setGeometry(jts);

			} else {

				if (!"objStatus".equals(key)) {
					Field f = this.getClass().getDeclaredField(key);

					f.setAccessible(true);

					f.set(this, SerializeUtils.convert(json.get(key), f.getType()));

				}
			}
		}

		return true;
	}

	@Override
	public List<IRow> relatedRows() {
		return null;
	}

	@Override
	public int pid() {
		return this.pid;
	}

	@Override
	public String primaryKey() {
		return "pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>, List<IRow>> childMap = new HashMap<>();
		// 设置子表IX_POI_NAME
		childMap.put(IxPoiName.class, names);

		// 设置POI_EDIT_STATUS
		childMap.put(IxPoiEditStatus.class, names);

		// 设置子表IX_POI_ADDRESS
		childMap.put(IxPoiAddress.class, addresses);

		// 设置子表IX_POI_CONTACT
		childMap.put(IxPoiContact.class, contacts);

		// 设置子表IX_POI_FLAG
		childMap.put(IxPoiFlag.class, flags);

		// 设置子表IX_POI_ENTRYIMAGE
		childMap.put(IxPoiEntryimage.class, entryImages);

		// 设置子表IX_POI_ICON
		childMap.put(IxPoiIcon.class, icons);

		// 设置子表IX_POI_PHOTO*
		childMap.put(IxPoiPhoto.class, photos);

		// 设置子表IX_POI_AUDIO*
		childMap.put(IxPoiAudio.class, audioes);

		// 设置子表IX_POI_VIDEO*
		childMap.put(IxPoiVideo.class, videoes);

		// 设置子表IX_POI_PARENT
		childMap.put(IxPoiParent.class, parents);

		// 设置poi的子
		childMap.put(IxPoiChildren.class, children);

		// 设置子表IX_POI_PARKING
		childMap.put(IxPoiParking.class, parkings);

		// 设置子表IX_POI_DETAIL
		childMap.put(IxPoiDetail.class, details);

		// 设置子表IX_POI_BUSINESSTIME
		childMap.put(IxPoiBusinessTime.class, businesstimes);

		// 设置子表IX_POI_CHARGINGSTATION
		childMap.put(IxPoiChargingStation.class, chargingstations);

		// 设置子表IX_POI_CHARGINGPLOT
		childMap.put(IxPoiChargingPlot.class, chargingplots);

		// 设置子表IX_POI_CHARGINGPLOT_PH
		childMap.put(IxPoiChargingPlotPh.class, chargingplotPhs);

		// 设置子表IX_POI_BUILDING
		childMap.put(IxPoiBuilding.class, buildings);

		// 设置子表IX_POI_ADVERTISEMENT
		childMap.put(IxPoiAdvertisement.class, advertisements);

		// 设置子表IX_POI_GASSTATION
		childMap.put(IxPoiGasstation.class, gasstations);

		// 设置子表IX_POI_INTRODUCTION
		childMap.put(IxPoiIntroduction.class, introductions);

		// 设置子表IX_POI_ATTRACTION
		childMap.put(IxPoiAttraction.class, attractions);

		// 设置子表IX_POI_HOTEL
		childMap.put(IxPoiHotel.class, hotels);

		// 设置子表IX_POI_RESTAURANT
		childMap.put(IxPoiRestaurant.class, restaurants);

		// 设置子表IX_POI_CARRENTAL
		childMap.put(IxPoiCarrental.class, carrentals);

//		childMap.put(IxSamepoi.class, samepois);

		return childMap;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		Map<Class<? extends IRow>, Map<String, ?>> childMap = new HashMap<Class<? extends IRow>, Map<String, ?>>();

		// 设置子表IX_POI_NAME
		childMap.put(IxPoiName.class, nameMap);

		// 设置POI_EDIT_STATUS,模型无此表特殊处理
		childMap.put(IxPoiEditStatus.class, nameMap);

		// 设置子表IX_POI_ADDRESS
		childMap.put(IxPoiAddress.class, addressMap);

		// 设置子表IX_POI_CONTACT
		childMap.put(IxPoiContact.class, contactMap);

		// 设置子表IX_POI_FLAG
		childMap.put(IxPoiFlag.class, flagMap);

		// 设置子表IX_POI_ENTRYIMAGE
		childMap.put(IxPoiEntryimage.class, entryImageMap);

		// 设置子表IX_POI_ICON
		childMap.put(IxPoiIcon.class, iconMap);

		// 设置子表IX_POI_PHOTO*
		childMap.put(IxPoiPhoto.class, photoMap);

		// 设置子表IX_POI_AUDIO*
		childMap.put(IxPoiAudio.class, audioMap);

		// 设置子表IX_POI_VIDEO*
		childMap.put(IxPoiVideo.class, videoMap);

		// 设置子表IX_POI_PARENT
		childMap.put(IxPoiParent.class, parentMap);

		// 设置poi的子
		childMap.put(IxPoiChildren.class, childrenMap);

		// 设置子表IX_POI_PARKING
		childMap.put(IxPoiParking.class, parkingMap);

		// 设置子表IX_POI_DETAIL
		childMap.put(IxPoiDetail.class, detailMap);

		// 设置子表IX_POI_BUSINESSTIME
		childMap.put(IxPoiBusinessTime.class, businesstimeMap);

		// 设置子表IX_POI_CHARGINGSTATION
		childMap.put(IxPoiChargingStation.class, chargingstationMap);

		// 设置子表IX_POI_CHARGINGPLOT
		childMap.put(IxPoiChargingPlot.class, chargingplotMap);

		// 设置子表IX_POI_CHARGINGPLOT_PH
		childMap.put(IxPoiChargingPlotPh.class, chargingplotPhMap);

		// 设置子表IX_POI_BUILDING
		childMap.put(IxPoiBuilding.class, buildingMap);

		// 设置子表IX_POI_ADVERTISEMENT
		childMap.put(IxPoiAdvertisement.class, advertisementMap);

		// 设置子表IX_POI_GASSTATION
		childMap.put(IxPoiGasstation.class, gasstationMap);

		// 设置子表IX_POI_INTRODUCTION
		childMap.put(IxPoiIntroduction.class, introductionMap);

		// 设置子表IX_POI_ATTRACTION
		childMap.put(IxPoiAttraction.class, attractionMap);

		// 设置子表IX_POI_HOTEL
		childMap.put(IxPoiHotel.class, hotelMap);

		// 设置子表IX_POI_RESTAURANT
		childMap.put(IxPoiRestaurant.class, restaurantMap);

		// 设置子表IX_POI_CARRENTAL
		childMap.put(IxPoiCarrental.class, carrentalMap);

//		childMap.put(IxSamepoi.class, samepoiMap);

		return childMap;
	}

	public int getTruckFlag() {
		return truckFlag;
	}

	public void setTruckFlag(int truckFlag) {
		this.truckFlag = truckFlag;
	}

	public String getRawFields() {
		return rawFields;
	}

	public void setRawFields(String rawFields) {
		this.rawFields = rawFields;
	}
	public static void main(String[] args){
		System.out.println(isEqualsString(null,""));
		System.out.println(isEqualsString(null,"1"));
		System.out.println(null instanceof String);
	}
}
