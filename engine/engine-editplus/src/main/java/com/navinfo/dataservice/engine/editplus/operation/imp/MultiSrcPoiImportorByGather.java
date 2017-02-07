package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import com.navinfo.dataservice.api.edit.upload.UploadPois;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ISerializable;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiPhoto;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjFactory;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

/** 
 * @ClassName: MultiSrcPoiImportorByGather
 * @author xiaoxiaowen4127
 * @date 2016年11月17日
 * @Description: MultiSrcPoiImportorByGather.java
 */
public class MultiSrcPoiImportorByGather extends AbstractOperation {

	protected Map<String,String> errLog = new HashMap<String,String>();
	protected List<PoiRelation> parentPid = new ArrayList<PoiRelation>();
	protected Map<Long,String> sourceTypes = new HashMap<Long,String>();
	//protected List<PoiRelation> samePoiRel = new ArrayList<PoiRelation>();
	
	public Map<Long, String> getSourceTypes() {
		return sourceTypes;
	}

	public MultiSrcPoiImportorByGather(Connection conn,OperationResult preResult) {
		super(conn,preResult);
	}

	public Map<String, String> getErrLog() {
		return errLog;
	}

	public List<PoiRelation> getParentPid() {
		return parentPid;
	}

	@Override
	public void operate(AbstractCommand cmd) throws Exception {
		// 获取当前做业季
		String version = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		UploadPois pois = ((MultiSrcPoiDayImportorCommand)cmd).getPois();
		if(pois!=null){
			//新增
			Map<String, JSONObject> addPois = pois.getAddPois();
			if(addPois!=null&&addPois.size()>0){
				List<IxPoiObj> ixPoiObjAdd = this.improtAdd(conn, addPois,version);
				result.putAll(ixPoiObjAdd);
			}
			//修改
			Map<String, JSONObject> updatePois = pois.getUpdatePois();
			if(updatePois!=null&&updatePois.size()>0){
				List<IxPoiObj> ixPoiObjUpdate = this.improtUpdate(conn,updatePois,version);
				result.putAll(ixPoiObjUpdate);
			}
			//删除
			Map<String, JSONObject> deletePois = pois.getDeletePois();
			if(deletePois!=null&&deletePois.size()>0){
				List<IxPoiObj> ixPoiObjDelete = this.improtDelete(conn, deletePois);
				result.putAll(ixPoiObjDelete);
			}
			
		}
	}
	
	/**
	 * 新增数据解析
	 * @author Han Shaoming
	 * @param conn
	 * @param version 
	 * @param jo
	 * @return
	 * @throws Exception
	 */
	public List<IxPoiObj> improtAdd(Connection conn,Map<String, JSONObject> addPois, String version)throws Exception{
		List<IxPoiObj> ixPoiObjList = new ArrayList<IxPoiObj>();
		//排除fid已存在的
		filterAddedPoi(addPois);
		
		for (Map.Entry<String, JSONObject> entry : addPois.entrySet()) {
			JSONObject jo = entry.getValue();
			//日志
			log.info("采集端上传新增json数据"+jo.toString());
			try {
				IxPoiObj poiObj = (IxPoiObj) ObjFactory.getInstance().create(ObjectName.IX_POI);
				importAddByJson(poiObj, jo,version);
				ixPoiObjList.add(poiObj);
			} catch (Exception e) {
				log.error(e.getMessage(),e);
				errLog.put(jo.getString("fid"), StringUtils.isEmpty(e.getMessage())?"新增执行成功":e.getMessage());
			}
		}
		return ixPoiObjList;
	}
	
	/**
	 * 修改数据解析
	 * @author Han Shaoming
	 * @param conn
	 * @param jso
	 * @param updatePois 
	 * @param version 
	 * @return
	 * @throws Exception
	 */
	public List<IxPoiObj> improtUpdate(Connection conn,Map<String, JSONObject> updatePois, String version)throws Exception{
		List<IxPoiObj> ixPoiObjList = new ArrayList<IxPoiObj>();
		//获取所需的子表
		Set<String> tabNames = this.getTabNames();
		Map<String,BasicObj> objs = IxPoiSelector.selectByFids(conn,tabNames,updatePois.keySet(),true,true);

		//排除有变更的数据
	//	filterUpdatePoi(updatePois);
		//开始导入
		for (Map.Entry<String, JSONObject> jo : updatePois.entrySet()) {
			//日志
			log.info("采集端上传修改json数据"+jo.getValue().toString());
			BasicObj obj = objs.get(jo.getKey());
			if(obj==null){
				errLog.put(jo.getKey(), "库中没有查到相应的数据");
			}else{
				try{
					IxPoiObj ixPoiObj = (IxPoiObj)obj;
					if(ixPoiObj.isDeleted()){
						throw new Exception("该数据已经逻辑删除");
					}else{
						this.importUpdateByJson(ixPoiObj, jo.getValue(),version);
						ixPoiObjList.add(ixPoiObj);
					}
				} catch (Exception e) {
					log.error(e.getMessage(),e);
					errLog.put(jo.getKey(), StringUtils.isEmpty(e.getMessage())?"修改执行出现空指针错误":e.getMessage());
				}
			}
		}
		return ixPoiObjList;
	}
	
	/**
	 * 删除数据解析
	 * @author Han Shaoming
	 * @param conn
	 * @param jo
	 * @return
	 * @throws Exception
	 */
	public List<IxPoiObj> improtDelete(Connection conn,Map<String,JSONObject> deletePois)throws Exception{
		List<IxPoiObj> ixPoiObjList = new ArrayList<IxPoiObj>();
		//获取所需的子表
		Set<String> tabNames = this.getTabNames();
		Map<String,BasicObj> objs = IxPoiSelector.selectByFids(conn,tabNames,deletePois.keySet(),true,true);
		
		//开始导入
		for (Map.Entry<String, JSONObject> jo : deletePois.entrySet()) {
			//日志
			log.info("采集端上传删除json数据"+jo.getValue().toString());
			BasicObj obj = objs.get(jo.getKey());
			if(obj==null){
				errLog.put(jo.getKey(), "没有查到相应的数据");
			}else{
				try{
					IxPoiObj ixPoiObj = (IxPoiObj)obj;
					if(ixPoiObj.isDeleted()){
						throw new Exception("该数据已经逻辑删除");
					}else{
						this.importDeleteByJson(ixPoiObj, jo.getValue());
						ixPoiObjList.add(ixPoiObj);
					}
				} catch (Exception e) {
					log.error(e.getMessage(),e);
					errLog.put(jo.getKey(), StringUtils.isEmpty(e.getMessage())?"删除执行出现空指针错误":e.getMessage());
				}
			}
		}
		return ixPoiObjList;
	}
	
	/**
	 * @Title: importAddByJson
	 * @Description: 上传新增数据解析
	 * @param poi
	 * @param jo
	 * @param version
	 * @return
	 * @throws Exception  boolean
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年1月6日 下午2:57:58 
	 */
	public boolean importAddByJson(IxPoiObj poi,JSONObject jo, String version)throws Exception {
		if(poi!=null&&jo!=null){
			if(poi instanceof IxPoiObj){
				//*************zl 2016.12.20*******************
				//POI主表
				IxPoi ixPoi = (IxPoi) poi.getMainrow();
				// geometry按SDO_GEOMETRY格式原值转出
				Geometry geometry = new WKTReader().read(jo.getString("geometry"));
				//ixPoi.setGeometry(GeoTranslator.transform(geometry, 100000, 5));
				ixPoi.setGeometry(geometry);
				if (jo.getJSONObject("guide").size() > 0) {
					ixPoi.setXGuide(jo.getJSONObject("guide").getDouble("longitude"));
					ixPoi.setYGuide(jo.getJSONObject("guide").getDouble("latitude"));
					ixPoi.setLinkPid(jo.getJSONObject("guide").getInt("linkPid"));
				} else {
					ixPoi.setXGuide(0);
					ixPoi.setYGuide(0);
					ixPoi.setLinkPid(0);
				}
				ixPoi.setChain(jo.getString("chain"));
				ixPoi.setOpen24h(jo.getInt("open24H"));
				// meshid非0时原值转出；为0时根据几何计算；
				int meshId = jo.getInt("meshid");
				if (meshId == 0) {
					String[] meshIds = MeshUtils.point2Meshes(geometry.getCoordinate().x, geometry.getCoordinate().y);
					meshId = Integer.parseInt(meshIds[0]);
				}
				ixPoi.setMeshId(meshId);
				ixPoi.setPostCode(jo.getString("postCode"));
				// 如果KIND_CODE有修改，则追加“改种别代码”；
				// 如果CHAIN有修改，则追加“改连锁品牌”；
				// 如果IX_POI_HOTEL.RATING有修改,则追加“改酒店星级”；
				String fieldState = "";
				if (!jo.getString("kindCode").isEmpty()) {
					fieldState += "改种别代码|";
				}
				if (!jo.getString("chain").isEmpty()) {
					fieldState += "改连锁品牌|";
				}
				if (jo.has("hotel")) {

					JSONObject hotel = jo.getJSONObject("hotel");

					if (!hotel.isNullObject() && hotel.has("rating")) {
						fieldState += "改酒店星级|";
					}
				}
				if (fieldState.length() > 0) {
					fieldState = fieldState.substring(0, fieldState.length() - 1);
				}
				ixPoi.setFieldState(fieldState);
				
				ixPoi.setOldName(jo.getString("name"));
				ixPoi.setOldAddress(jo.getString("address"));
				ixPoi.setOldKind(jo.getString("kindCode"));
				ixPoi.setPoiNum(jo.getString("fid"));
				ixPoi.setDataVersion(version);
				ixPoi.setCollectTime(jo.getString("t_operateDate"));
				ixPoi.setLevel(jo.getString("level"));
				
				String outDoorLog = "";
				if (!ixPoi.getOldName().isEmpty()) {
					outDoorLog += "改名称|";
				}
				if (!ixPoi.getOldAddress().isEmpty()) {
					outDoorLog += "改地址|";
				}
				if (!ixPoi.getOldKind().isEmpty()) {
					outDoorLog += "改分类|";
				}
				if (!ixPoi.getLevel().isEmpty()) {
					outDoorLog += "改POI_LEVEL|";
				}
				if (!ixPoi.getGeometry().isEmpty()) {
					outDoorLog += "改RELATION|";
				}
				if (outDoorLog.length() > 0) {
					outDoorLog = outDoorLog.substring(0, outDoorLog.length() - 1);
				}
				ixPoi.setLog(outDoorLog);
				ixPoi.setSportsVenue(jo.getString("sportsVenues"));
				
				JSONObject indoor = jo.getJSONObject("indoor");
				if (!indoor.isNullObject() && indoor.has("type")) {
					if (indoor.getInt("type") == 3) {
						ixPoi.setIndoor(1);
					} else {
						ixPoi.setIndoor(0);
					}
					
				} else {
					ixPoi.setIndoor(0);
				}
				
				ixPoi.setVipFlag(jo.getString("vipFlag"));

				// 新增卡车标识20160927
				ixPoi.setTruckFlag(jo.getInt("truck"));
				
				//名称
				if(!JSONUtils.isNull(jo.get("name")) && StringUtils.isNotEmpty(jo.getString("name"))){
					String name = jo.getString("name");
					//IX_POI_NAME表
					IxPoiName ixPoiName = poi.createIxPoiName();
					ixPoiName.setPoiPid(ixPoi.getPid());
					ixPoiName.setName(name);
					ixPoiName.setNameClass(1);
					ixPoiName.setNameType(2);
					ixPoiName.setLangCode("CHI");
				}
				
				//地址
				if(!JSONUtils.isNull(jo.get("address")) && StringUtils.isNotEmpty(jo.getString("address"))){
					if(StringUtils.isNotEmpty(jo.getString("address"))){
						String address = jo.getString("address");
						//IX_POI_ADDRESS表
						IxPoiAddress ixPoiAddress = poi.createIxPoiAddress();
						ixPoiAddress.setPoiPid(ixPoi.getPid());
						ixPoiAddress.setFullname(address);
						ixPoiAddress.setLangCode("CHI");
					}
				}
				//[集合]联系方式
				if(!JSONUtils.isNull(jo.get("contacts")) ){
					String contacts = jo.getString("contacts");
					if(!"[]".equals(contacts)){
						JSONArray ja = JSONArray.fromObject(contacts);
						for (int i=0;i<ja.size();i++) {
							JSONObject jso = ja.getJSONObject(i);
							//号码number
							String number = null;
							if(!JSONUtils.isNull(jso.get("number"))){
								number = jso.getString("number");
							}else{
								throw new Exception("号码number字段名不存在");
							}
							//联系方式类型type
							int type = jso.getInt("type");
							
							String linkman = jso.getString("linkman");
							String linkmanNum = "";
							if (linkman.indexOf("总机") >= 0) {
								linkmanNum = "1" + linkmanNum;
							} else {
								linkmanNum = "0" + linkmanNum;
							}
							if (linkman.indexOf("客服") >= 0) {
								linkmanNum = "1" + linkmanNum;
							} else {
								linkmanNum = "0" + linkmanNum;
							}
							if (linkman.indexOf("预订") >= 0) {
								linkmanNum = "1" + linkmanNum;
							} else {
								linkmanNum = "0" + linkmanNum;
							}
							if (linkman.indexOf("销售") >= 0) {
								linkmanNum = "1" + linkmanNum;
							} else {
								linkmanNum = "0" + linkmanNum;
							}
							if (linkman.indexOf("维修") >= 0) {
								linkmanNum = "1" + linkmanNum;
							} else {
								linkmanNum = "0" + linkmanNum;
							}
							if (linkman.indexOf("其他") >= 0) {
								linkmanNum = "1" + linkmanNum;
							} else {
								linkmanNum = "0" + linkmanNum;
							}
							int contactInt = Integer.parseInt(linkmanNum, 2);
							
							//IX_POI_CONTACT表
							IxPoiContact ixPoiContact = poi.createIxPoiContact();
							ixPoiContact.setPoiPid(ixPoi.getPid());
							ixPoiContact.setContact(number);
							ixPoiContact.setContactType(type);
							ixPoiContact.setContactDepart(contactInt);
							if(!JSONUtils.isNull(jso.get("priority"))){
								ixPoiContact.setPriority(jso.getInt("priority"));
							}
							
						}
					}
				}
				
				// 新增POI_MEMO录入20160927
				String poiMemo = "";
				
				// 照片
				JSONArray attachments = jo.getJSONArray("attachments");
				if(!JSONUtils.isNull(jo.get("attachments"))){
					String phones = jo.getString("attachments");
					if(!"[]".equals(phones)){
						JSONArray ja = JSONArray.fromObject(phones);
						for (int i=0;i<ja.size();i++) {
							JSONObject photo = ja.getJSONObject(i);
							//IX_POI_PHOTO表
							int type = photo.getInt("type");
							if(type == 1) {
								String fccpid = photo.getString("id");
								IxPoiPhoto ixPoiPhoto = poi.createIxPoiPhoto();
								if(fccpid != null && StringUtils.isNotEmpty(fccpid)){
									ixPoiPhoto.setPid(fccpid);
								}
								ixPoiPhoto.setPoiPid(ixPoi.getPid());
								ixPoiPhoto.setTag(photo.getInt("tag"));
							}else if (type == 3) {
								poiMemo = photo.getString("content");
							}
						}
						ixPoi.setPoiMemo(poiMemo);
					}
					
				}
				// 父子关系
				//处理父子关系
				String fatherson = null;
				if(!JSONUtils.isNull(jo.get("parentFid")) && StringUtils.isNotEmpty(jo.getString("parentFid"))){
					fatherson = jo.getString("parentFid");
				}
				PoiRelation pr = new PoiRelation();
				pr.setFatherFid(fatherson);
				pr.setPid(ixPoi.getPid());
				pr.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
				parentPid.add(pr);
				
				//********************************
				if (!JSONUtils.isNull(jo.get("relateChildren")) && jo.getJSONArray("relateChildren").size() > 0) {//上传的poi 存在 子poi
					//此poi是父,判断IxPoiParent 是否存在
					IxPoiParent ixPoiParent = null;
					List<IxPoiParent> ixPoiParentList =poi.getIxPoiParents();
					if(ixPoiParentList != null && ixPoiParentList.size() >0){
						ixPoiParent = ixPoiParentList.get(0);
					}else{//新建 ixPoiParent
							ixPoiParent = poi.createIxPoiParent();
							ixPoiParent.setParentPoiPid(ixPoi.getPid());
					}
					
					JSONArray childrenpoiList = jo.getJSONArray("relateChildren");
					for (int i=0;i<childrenpoiList.size();i++) {
						JSONObject relateChildrenObj = childrenpoiList.getJSONObject(i);
						//*******zl 2017.01.20 **********
						//如果此子poi 的 childPid == 0 需要放到PoiRelation 中后续处理
						if(relateChildrenObj.getInt("childPid") == 0 && relateChildrenObj.getString("childFid") != null && StringUtils.isNotEmpty(relateChildrenObj.getString("childFid"))){
							//创建子的 PoiRelation
							PoiRelation pr2 = new PoiRelation();
							pr2.setFatherPid(ixPoi.getPid());
							pr2.setFid(relateChildrenObj.getString("childFid"));
							pr2.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
							parentPid.add(pr2);
						}else{
							IxPoiChildren newIxPoiChildren;
							newIxPoiChildren = poi.createIxPoiChildren(ixPoiParent.getGroupId());
							newIxPoiChildren.setChildPoiPid(relateChildrenObj.getInt("childPid"));
							newIxPoiChildren.setRelationType(relateChildrenObj.getInt("type"));
							newIxPoiChildren.setRowId(relateChildrenObj.getString("rowId"));
						}
						
					}
				}
				//********************************
				
				// 同一关系
				//处理同一关系
				String sameFid = null;
				if(!JSONUtils.isNull(jo.get("sameFid")) && StringUtils.isNotEmpty(jo.getString("sameFid"))){
					sameFid = jo.getString("sameFid");
				}
				PoiRelation sr = new PoiRelation();
				sr.setSameFid(sameFid);
				sr.setPid(ixPoi.getPid());
				sr.setPoiRelationType(PoiRelationType.SAME_POI);
				//samePoiRel.add(sr);
				parentPid.add(sr);
				
				// 加油站
				if (!JSONUtils.isNull(jo.get("gasStation")) && jo.getJSONObject("gasStation").size() > 0) {
					JSONObject gasObj = jo.getJSONObject("gasStation");
					IxPoiGasstation gasStation = poi.createIxPoiGasstation();	
					gasStation.setPoiPid(ixPoi.getPid());
					gasStation.setServiceProv(gasObj.getString("servicePro"));
					gasStation.setFuelType(gasObj.getString("fuelType"));
					gasStation.setOilType(gasObj.getString("oilType"));
					gasStation.setEgType(gasObj.getString("egType"));
					gasStation.setMgType(gasObj.getString("mgType"));
					gasStation.setPayment(gasObj.getString("payment"));
					gasStation.setService(gasObj.getString("service"));
					gasStation.setOpenHour(gasObj.getString("openHour"));
				}
				// 停车场
				if (!JSONUtils.isNull(jo.get("parkings")) && jo.getJSONObject("parkings").size() > 0) {
					JSONObject parkingsObj = jo.getJSONObject("parkings");
					IxPoiParking parkings = poi.createIxPoiParking();
					parkings.setPoiPid(ixPoi.getPid());
					parkings.setParkingType(parkingsObj.getString("buildingType"));
					parkings.setTollStd(parkingsObj.getString("tollStd"));
					parkings.setTollDes(parkingsObj.getString("tollDes"));
					parkings.setTollWay(parkingsObj.getString("tollWay"));
					parkings.setPayment(parkingsObj.getString("payment"));
					parkings.setRemark(parkingsObj.getString("remark"));
					parkings.setOpenTiime(parkingsObj.getString("openTime"));
					parkings.setTotalNum(parkingsObj.getInt("totalNum"));
					parkings.setResHigh(parkingsObj.getInt("resHigh"));
					parkings.setResWidth(parkingsObj.getInt("resWidth"));
					parkings.setResWeigh(parkingsObj.getInt("resWeigh"));
					parkings.setCertificate(parkingsObj.getInt("certificate"));
					parkings.setVehicle(parkingsObj.getInt("vehicle"));
					parkings.setHaveSpecialplace(parkingsObj.getString("haveSpecialPlace"));
					parkings.setWomenNum(parkingsObj.getInt("womenNum"));
					parkings.setHandicapNum(parkingsObj.getInt("handicapNum"));
					parkings.setMiniNum(parkingsObj.getInt("miniNum"));
					parkings.setVipNum(parkingsObj.getInt("vipNum"));
				}
				// 酒店
				if (!JSONUtils.isNull(jo.get("hotel")) && jo.getJSONObject("hotel").size() > 0) {
					JSONObject hotelObj = jo.getJSONObject("hotel");
					IxPoiHotel hotel = poi.createIxPoiHotel();
					hotel.setPoiPid(ixPoi.getPid());
					hotel.setCreditCard(hotelObj.getString("creditCards"));
					hotel.setRating(hotelObj.getInt("rating"));
					hotel.setCheckinTime(hotelObj.getString("checkInTime"));
					hotel.setCheckoutTime(hotelObj.getString("checkOutTime"));
					hotel.setRoomCount(hotelObj.getInt("roomCount"));
					hotel.setRoomType(hotelObj.getString("roomType"));
					hotel.setRoomPrice(hotelObj.getString("roomPrice"));
					hotel.setBreakfast(hotelObj.getInt("breakfast"));
					hotel.setService(hotelObj.getString("service"));
					hotel.setParking(hotelObj.getInt("parking"));
					hotel.setLongDescription(hotelObj.getString("description"));
					hotel.setOpenHour(hotelObj.getString("openHour"));
				}
				
				// 餐馆
				if (!JSONUtils.isNull(jo.get("foodtypes")) && jo.getJSONObject("foodtypes").size() > 0) {
					JSONObject foodtypesObj = jo.getJSONObject("foodtypes");
					IxPoiRestaurant foodtypes = poi.createIxPoiRestaurant();
					foodtypes.setPoiPid(ixPoi.getPid());
					foodtypes.setFoodType(foodtypesObj.getString("foodtype"));
					foodtypes.setCreditCard(foodtypesObj.getString("creditCards"));
					foodtypes.setAvgCost(foodtypesObj.getInt("avgCost"));
					foodtypes.setParking(foodtypesObj.getInt("parking"));
					foodtypes.setOpenHour(foodtypesObj.getString("openHour"));
				}
				// 充电站
				if (!JSONUtils.isNull(jo.get("chargingStation")) && jo.getJSONObject("chargingStation").size() > 0) {
					JSONObject chargingStationObj = jo.getJSONObject("chargingStation");
					IxPoiChargingstation chargingStation = poi.createIxPoiChargingstation();
					chargingStation.setPoiPid(ixPoi.getPid());
					chargingStation.setChargingType(chargingStationObj.getInt("type"));
					chargingStation.setChangeBrands(chargingStationObj.getString("changeBrands"));
					chargingStation.setChangeOpenType(chargingStationObj.getString("changeOpenType"));
					chargingStation.setChargingNum(chargingStationObj.getInt("chargingNum"));
					chargingStation.setServiceProv(chargingStationObj.getString("servicePro"));
					chargingStation.setOpenHour(chargingStationObj.getString("openHour"));
					chargingStation.setParkingFees(chargingStationObj.getInt("parkingFees"));
					chargingStation.setParkingInfo(chargingStationObj.getString("parkingInfo"));
					chargingStation.setAvailableState(chargingStationObj.getInt("availableState"));
				}
				
				// 充电桩
				if (!JSONUtils.isNull(jo.get("chargingPole")) && jo.getJSONArray("chargingPole").size() > 0) {
					JSONArray chargingPoleArray = jo.getJSONArray("chargingPole");
					for (int i=0;i<chargingPoleArray.size();i++) {
						JSONObject chargingPoleObj = chargingPoleArray.getJSONObject(i);
						IxPoiChargingplot chargingPole = poi.createIxPoiChargingplot();
						chargingPole.setPoiPid(ixPoi.getPid());
						chargingPole.setGroupId(chargingPoleObj.getInt("groupId"));
						chargingPole.setCount(chargingPoleObj.getInt("count"));
						chargingPole.setAcdc(chargingPoleObj.getInt("acdc"));
						chargingPole.setPlugType(chargingPoleObj.getString("plugType"));
						chargingPole.setPower(chargingPoleObj.getString("power"));
						chargingPole.setVoltage(chargingPoleObj.getString("voltage"));
						chargingPole.setCurrent(chargingPoleObj.getString("current"));
						chargingPole.setMode(chargingPoleObj.getInt("mode"));
						chargingPole.setPlugNum(chargingPoleObj.getInt("plugNum"));
						chargingPole.setPrices(chargingPoleObj.getString("prices"));
						chargingPole.setOpenType(chargingPoleObj.getString("openType"));
						chargingPole.setAvailableState(chargingPoleObj.getInt("availableState"));
						chargingPole.setManufacturer(chargingPoleObj.getString("manufacturer"));
						chargingPole.setFactoryNum(chargingPoleObj.getString("factoryNum"));
						chargingPole.setPlotNum(chargingPoleObj.getString("plotNum"));
						chargingPole.setProductNum(chargingPoleObj.getString("productNum"));
						chargingPole.setParkingNum(chargingPoleObj.getString("parkingNum"));
						chargingPole.setFloor(chargingPoleObj.getInt("floor"));
						chargingPole.setLocationType(chargingPoleObj.getInt("locationType"));
						chargingPole.setPayment(chargingPoleObj.getString("payment"));
					}
				}

				return true;
				//*********************************
			}else{
				throw new ImportException("不支持的对象类型");
			}
		}
		return false;
	}

	/**
	 * @Title: importUpdateByJson
	 * @Description: 更新数据的数据解析
	 * @param poi
	 * @param jo
	 * @param version
	 * @return
	 * @throws ImportException
	 * @throws Exception  boolean
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年1月6日 下午2:58:41 
	 */
	public boolean importUpdateByJson(IxPoiObj poi,JSONObject jo, String version) throws ImportException, Exception {
		if(poi!=null&&jo!=null){
			if(poi instanceof IxPoiObj){
				//查询的POI主表
				IxPoi ixPoi = (IxPoi) poi.getMainrow();
				long pid = ixPoi.getPid();
				//************zl 2016.12.20********
				//改分类
				if(!JSONUtils.isNull(jo.get("kindCode"))){
					String kind = jo.getString("kindCode");
					ixPoi.setKindCode(kind);
				}
				// geometry按SDO_GEOMETRY格式原值转出
				Geometry geometry = new WKTReader().read(jo.getString("geometry"));
				//ixPoi.setGeometry(GeoTranslator.transform(geometry, 100000, 5));
				//JSONObject geometryObj = GeoTranslator.jts2Geojson(geometry);
				ixPoi.setGeometry(geometry);
				if (jo.getJSONObject("guide").size() > 0) {
					ixPoi.setXGuide(jo.getJSONObject("guide").getDouble("longitude"));
					ixPoi.setYGuide(jo.getJSONObject("guide").getDouble("latitude"));
					ixPoi.setLinkPid(jo.getJSONObject("guide").getInt("linkPid"));
				} else {
					ixPoi.setXGuide(0);
					ixPoi.setYGuide(0);
					ixPoi.setLinkPid(0);
				}
				ixPoi.setChain(jo.getString("chain"));
				ixPoi.setOpen24h(jo.getInt("open24H"));
				// meshid非0时原值转出；为0时根据几何计算；
				int meshId = jo.getInt("meshid");
				if (meshId == 0) {
					String[] meshIds = MeshUtils.point2Meshes(geometry.getCoordinate().x, geometry.getCoordinate().y);
					meshId = Integer.parseInt(meshIds[0]);
				}
				ixPoi.setMeshId(meshId);
				ixPoi.setPostCode(jo.getString("postCode"));
				// 如果KIND_CODE有修改，则追加“改种别代码”；
				// 如果CHAIN有修改，则追加“改连锁品牌”；
				// 如果IX_POI_HOTEL.RATING有修改,则追加“改酒店星级”；
				String fieldState = "";
				if (!jo.getString("kindCode").isEmpty()) {
					fieldState += "改种别代码|";
				}
				if (!jo.getString("chain").isEmpty()) {
					fieldState += "改连锁品牌|";
				}
				if (jo.has("hotel")) {

					JSONObject hotel = jo.getJSONObject("hotel");

					if (!hotel.isNullObject() && hotel.has("rating")) {
						fieldState += "改酒店星级|";
					}
				}
				if (fieldState.length() > 0) {
					fieldState = fieldState.substring(0, fieldState.length() - 1);
				}
				ixPoi.setFieldState(fieldState);
				ixPoi.setOldName(jo.getString("name"));
				ixPoi.setOldAddress(jo.getString("address"));
				ixPoi.setOldKind(jo.getString("kindCode"));
				ixPoi.setPoiNum(jo.getString("fid"));
				ixPoi.setDataVersion(version);
				ixPoi.setCollectTime(jo.getString("t_operateDate"));
				ixPoi.setLevel(jo.getString("level"));
				
				String outDoorLog = "";
				if (!ixPoi.getOldName().isEmpty()) {
					outDoorLog += "改名称|";
				}
				if (!ixPoi.getOldAddress().isEmpty()) {
					outDoorLog += "改地址|";
				}
				if (!ixPoi.getOldKind().isEmpty()) {
					outDoorLog += "改分类|";
				}
				if (!ixPoi.getLevel().isEmpty()) {
					outDoorLog += "改POI_LEVEL|";
				}
				if (!ixPoi.getGeometry().isEmpty()) {
					outDoorLog += "改RELATION|";
				}
				if (outDoorLog.length() > 0) {
					outDoorLog = outDoorLog.substring(0, outDoorLog.length() - 1);
				}
				ixPoi.setLog(outDoorLog);
				ixPoi.setSportsVenue(jo.getString("sportsVenues"));
				
				JSONObject indoor = jo.getJSONObject("indoor");
				if (!indoor.isNullObject() && indoor.has("type")) {
					if (indoor.getInt("type") == 3) {
						ixPoi.setIndoor(1);
					} else {
						ixPoi.setIndoor(0);
					}
					
				} else {
					ixPoi.setIndoor(0);
				}
				
				ixPoi.setVipFlag(jo.getString("vipFlag"));
				// 新增卡车标识20160927
				ixPoi.setTruckFlag(jo.getInt("truck"));
				
				// 照片
				if(jo.getJSONArray("attachments").size() > 0){
				// 新增POI_MEMO录入20160927
				String poiMemo = "";
				
				// 照片
				List<IxPoiPhoto> ixPoiPhotosOld = poi.getIxPoiPhotos();
				List<Long> photoIdList = new ArrayList<Long>();
				if(ixPoiPhotosOld != null && ixPoiPhotosOld.size() > 0){
					for (IxPoiPhoto ixPoiPhotoOld : ixPoiPhotosOld) {
						photoIdList.add(ixPoiPhotoOld.getPhotoId());
					}
				}
				if(!JSONUtils.isNull(jo.get("attachments"))){
					String phones = jo.getString("attachments");
					if(!"[]".equals(phones)){
						JSONArray ja = JSONArray.fromObject(phones);
						for (int i=0;i<ja.size();i++) {
							JSONObject photo = ja.getJSONObject(i);
							//IX_POI_PHOTO表
							int type = photo.getInt("type");
							if(type == 1) {
								String fccpid = photo.getString("id");
								IxPoiPhoto ixPoiPhoto = poi.createIxPoiPhoto();
								if(fccpid != null && StringUtils.isNotEmpty(fccpid)){
									//Long photoIdl = Long.parseLong(fccpid);
									ixPoiPhoto.setPid(fccpid);
								}
								ixPoiPhoto.setPoiPid(ixPoi.getPid());
								ixPoiPhoto.setTag(photo.getInt("tag"));
							}else if (type == 3) {
								poiMemo = photo.getString("content");
							}
						}
						ixPoi.setPoiMemo(poiMemo);
					}
					
				}
				}
				//改名称
				if(!JSONUtils.isNull(jo.get("name")) && StringUtils.isNotEmpty(jo.getString("name"))){
					this.usdateName(poi, jo, "CHI");
				}
				//改电话
				if(!JSONUtils.isNull(jo.get("contacts")) && jo.getJSONArray("contacts").size() > 0){
					this.usdateContact(poi, jo);
				}
				//改地址
				if(!JSONUtils.isNull(jo.get("address")) && StringUtils.isNotEmpty(jo.getString("address"))){
					this.usdateAddress(poi, jo, "CHI");
				}
				
				// 父子关系
				//1.处理父
				String fatherson = null;
				if(!JSONUtils.isNull(jo.get("parentFid")) && StringUtils.isNotEmpty((String) jo.get("parentFid"))){
					fatherson = jo.getString("parentFid");
				}
				PoiRelation pr = new PoiRelation();
				pr.setFatherFid(fatherson);
				pr.setPid(poi.objPid());
				pr.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
				parentPid.add(pr);
				
				//2.处理子
				if(jo.containsKey("relateChildren")){
					this.usdateChildren(poi, jo, pid);
				}
				
				
				// 同一关系
				//处理同一关系
				String sameFid = null;
				if(!JSONUtils.isNull(jo.get("sameFid")) && StringUtils.isNotEmpty(jo.getString("sameFid"))){
					sameFid = jo.getString("sameFid");
				}
				PoiRelation sr = new PoiRelation();
				sr.setSameFid(sameFid);
				sr.setPid(ixPoi.getPid());
				sr.setPoiRelationType(PoiRelationType.SAME_POI);
				//samePoiRel.add(sr);
				parentPid.add(sr);
				
				//改加油站
				//if(!JSONUtils.isNull(jo.get("gasStation")) && jo.getJSONObject("gasStation").size() > 0){
				if(jo.containsKey("gasStation")){
					this.usdateGasStation(poi, jo, pid);
				}
				//改停车场
				//if(!JSONUtils.isNull(jo.get("parkings")) && jo.getJSONObject("parkings").size() > 0){
				if(jo.containsKey("parkings")){
					this.usdateIxPoiParking(poi, jo, pid);
				}
				//改酒店
				//if(!JSONUtils.isNull(jo.get("hotel")) && jo.getJSONObject("hotel").size() > 0){
				if(jo.containsKey("hotels")){
					this.usdateIxPoiHotel(poi, jo, pid);
				}
				//改餐馆
				//if(!JSONUtils.isNull(jo.get("foodtypes")) && jo.getJSONObject("foodtypes").size() > 0){
				if(jo.containsKey("foodtypes")){
					this.usdateIxPoiFoodtypes(poi, jo, pid);
				}
				//改充电站
				//if(!JSONUtils.isNull(jo.get("chargingStation")) && jo.getJSONObject("chargingStation").size() > 0){
				if(jo.containsKey("chargingStation")){
					this.usdateIxPoiChargingStation(poi, jo, pid);
				}
				//改充电桩
				//if(!JSONUtils.isNull(jo.get("chargingPole")) && jo.getJSONArray("chargingPole").size() > 0){
				if(jo.containsKey("chargingPole")){
					this.usdateIxPoiChargingPlot(poi, jo, pid);
				}
				//*********************************
				return true;
			}else{
				throw new ImportException("不支持的对象类型");
			}
		}
		return false;
	}
	
	/**
	 * @Title: usdateChildren
	 * @Description: 解析处理 子 IxPoiChildren 的数据
	 * @param poi
	 * @param jo
	 * @param pid  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年1月6日 下午2:59:13 
	 */
	private void usdateChildren(IxPoiObj poi, JSONObject jo, long pid) {
		List<IxPoiChildren> oldChildrenpoiList = poi.getIxPoiChildrens();
		JSONArray childrenpoiList = jo.getJSONArray("relateChildren");
		JSONArray oldArray = new JSONArray();
		
		//此poi是父,判断IxPoiParent 是否存在
		IxPoiParent ixPoiParent = null;
		List<IxPoiParent> ixPoiParentList =poi.getIxPoiParents();
		if(ixPoiParentList != null && ixPoiParentList.size() >0){
			ixPoiParent = ixPoiParentList.get(0);
		}else{//新建 ixPoiParent
			try {
				ixPoiParent = poi.createIxPoiParent();
				ixPoiParent.setParentPoiPid(pid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(oldChildrenpoiList != null && oldChildrenpoiList.size() > 0 ){//原本就已经存在 子poi
			for (IxPoiChildren oldChildren : oldChildrenpoiList) {
			
				//JSONObject oldChildrenObj = new JSONObject();
				try {
					JSONObject oldChildrenObj2 = ((ISerializable) oldChildren).Serialize(null);
//					oldChildrenObj.put("childPoiPid", oldChildren.getChildPoiPid());
//					oldChildrenObj.put("relationType", oldChildren.getRelationType());
//					oldChildrenObj.put("rowId", oldChildren.getRowId());
					oldArray.add(oldChildrenObj2);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			if (!JSONUtils.isNull(childrenpoiList) && childrenpoiList.size() > 0) {//上传的poi 存在 子poi
				
				List<String> newRowIdList = new ArrayList<String>();//存储上传poi的所有子的 row_id
				for (int i=0;i<childrenpoiList.size();i++) {
					JSONObject relateChildrenObj = childrenpoiList.getJSONObject(i);
					newRowIdList.add(relateChildrenObj.getString("rowId").toUpperCase());
					//差分子poi
					JSONObject newChildrenObj = new JSONObject();
					newChildrenObj.put("childPoiPid", relateChildrenObj.getInt("childPid"));
					newChildrenObj.put("relationType", relateChildrenObj.getInt("type"));
					newChildrenObj.put("rowId", relateChildrenObj.getString("rowId"));
					int ret;
					try {
						ret = getDifferent(oldArray,newChildrenObj);
						if (ret == 0) {//新增
							//********zl 2017.01.20*******
							//如果此子poi 的 childPid == 0 需要放到PoiRelation 中后续处理
							if(relateChildrenObj.getInt("childPid") == 0 && relateChildrenObj.getString("childFid") != null && StringUtils.isNotEmpty(relateChildrenObj.getString("childFid"))){
								//创建子的 PoiRelation
								PoiRelation pr2 = new PoiRelation();
								pr2.setFatherPid(pid);
								pr2.setFid(relateChildrenObj.getString("childFid"));
								pr2.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
								parentPid.add(pr2);
							}else{
								IxPoiChildren newIxPoiChildren = poi.createIxPoiChildren(ixPoiParent.getGroupId());
								newIxPoiChildren.setChildPoiPid(relateChildrenObj.getInt("childPid"));
								newIxPoiChildren.setRelationType(relateChildrenObj.getInt("type"));
								newIxPoiChildren.setRowId(relateChildrenObj.getString("rowId"));
							}
							
							
						} else if (ret == 1) {//修改
							for (IxPoiChildren oldChildren : oldChildrenpoiList) {
								if (oldChildren.getRowId().equals(relateChildrenObj.getString("rowId").toUpperCase())) {
									oldChildren.setChildPoiPid(relateChildrenObj.getInt("childPid"));
									oldChildren.setGroupId(ixPoiParent.getGroupId());
									oldChildren.setRelationType(relateChildrenObj.getInt("type"));
									break;
								}
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//差分删除
				for (IxPoiChildren oldChildren : oldChildrenpoiList) {
					if(!newRowIdList.contains(oldChildren.getRowId().toUpperCase())){//原poi中存在的子,上传的poi中不存在,则删除此子
						poi.deleteSubrow(oldChildren);
					}
				}
				
			}else{//上传的poi 不存在 子poi
				for(IxPoiChildren ixPoiChildren : oldChildrenpoiList){//删除所有子
					poi.deleteSubrow(ixPoiChildren);
				}
				poi.deleteSubrow(ixPoiParent);//删除父
			}
		}else{//原本就已经不存在 子poi
			if (!JSONUtils.isNull(childrenpoiList) && childrenpoiList.size() > 0) {//上传的poi 存在 子poi
				//JSONArray relateChildren = jo.getJSONArray("relateChildren");
				for (int i=0;i<childrenpoiList.size();i++) {
					JSONObject relateChildrenObj = childrenpoiList.getJSONObject(i);
					//********zl 2017.01.20*******
					//如果此子poi 的 childPid == 0 需要放到PoiRelation 中后续处理
					if(relateChildrenObj.getInt("childPid") == 0 && relateChildrenObj.getString("childFid") != null && StringUtils.isNotEmpty(relateChildrenObj.getString("childFid"))){
						//创建子的 PoiRelation
						PoiRelation pr2 = new PoiRelation();
						pr2.setFatherPid(pid);
						pr2.setFid(relateChildrenObj.getString("childFid"));
						pr2.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
						parentPid.add(pr2);
					}else{
						IxPoiChildren newIxPoiChildren;
						try {
							newIxPoiChildren = poi.createIxPoiChildren(ixPoiParent.getGroupId());
							newIxPoiChildren.setChildPoiPid(relateChildrenObj.getInt("childPid"));
							newIxPoiChildren.setRelationType(relateChildrenObj.getInt("type"));
							newIxPoiChildren.setRowId(relateChildrenObj.getString("rowId"));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
	}

	/**
	 * @Title: usdateGasStation
	 * @Description: 解析处理加油站 数据
	 * @param poi
	 * @param jo
	 * @param pid
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年1月6日 下午3:02:08 
	 */
	private void usdateGasStation(IxPoiObj poi, JSONObject jo,long pid) throws Exception {
		//查询的IX_POI_GASSTATION表
		List<IxPoiGasstation> gasList = poi.getIxPoiGasstations();
		JSONObject gasObj = jo.getJSONObject("gasStation");
		JSONArray oldArray = new JSONArray();
		if(gasList != null && gasList.size() > 0){
		if (gasObj != null && !gasObj.isEmpty()) {
			for (IxPoiGasstation oldGas : gasList) {
				JSONObject oldPoiGasObj = ((ISerializable) oldGas).Serialize(null);
				oldArray.add(oldPoiGasObj);
			}
			JSONObject newGasStation = new JSONObject();
			newGasStation.put("poiPid", pid);
			newGasStation.put("serviceProv", gasObj.getString("servicePro"));
			newGasStation.put("fuelType", gasObj.getString("fuelType"));
			newGasStation.put("oilType", gasObj.getString("oilType"));
			newGasStation.put("egType", gasObj.getString("egType"));
			newGasStation.put("mgType", gasObj.getString("mgType"));
			newGasStation.put("payment", gasObj.getString("payment"));
			newGasStation.put("service", gasObj.getString("service"));
			newGasStation.put("openHour", gasObj.getString("openHour"));
			newGasStation.put("rowId", gasObj.getString("rowId").toUpperCase());
			// 差分,区分新增修改
			int ret = getDifferent(oldArray, newGasStation);
			if (ret == 0) {//新增
				IxPoiGasstation newIxPoiGasstation = poi.createIxPoiGasstation();
				newIxPoiGasstation.setPoiPid(pid);
				newIxPoiGasstation.setServiceProv(gasObj.getString("servicePro"));
				newIxPoiGasstation.setFuelType(gasObj.getString("fuelType"));
				newIxPoiGasstation.setOilType(gasObj.getString("oilType"));
				newIxPoiGasstation.setEgType(gasObj.getString("egType"));
				newIxPoiGasstation.setMgType(gasObj.getString("mgType"));
				newIxPoiGasstation.setPayment(gasObj.getString("payment"));
				newIxPoiGasstation.setService(gasObj.getString("service"));
				newIxPoiGasstation.setOpenHour(gasObj.getString("openHour"));
				newIxPoiGasstation.setRowId(gasObj.getString("rowId").toUpperCase());
				
				// 鲜度验证
				//freshFlag = false;
			} else if (ret == 1) {//修改
				for (IxPoiGasstation oldGas : gasList) {
					if (oldGas.getRowId().equals(gasObj.getString("rowId").toUpperCase())) {
						oldGas.setPoiPid(pid);
						oldGas.setServiceProv(gasObj.getString("servicePro"));
						oldGas.setFuelType(gasObj.getString("fuelType"));
						oldGas.setOilType(gasObj.getString("oilType"));
						oldGas.setEgType(gasObj.getString("egType"));
						oldGas.setMgType(gasObj.getString("mgType"));
						oldGas.setPayment(gasObj.getString("payment"));
						oldGas.setService(gasObj.getString("service"));
						oldGas.setOpenHour(gasObj.getString("openHour"));
						
						break;
					}
				}
			}
		}{
			// 删除的数据
			for (IxPoiGasstation oldGas : gasList) {
				poi.deleteSubrow(oldGas);
			}
		}
		
	}else{
		if (gasObj != null && !gasObj.isEmpty()) {
		IxPoiGasstation newIxPoiGasstation = poi.createIxPoiGasstation();
		newIxPoiGasstation.setPoiPid(pid);
		newIxPoiGasstation.setServiceProv(gasObj.getString("servicePro"));
		newIxPoiGasstation.setFuelType(gasObj.getString("fuelType"));
		newIxPoiGasstation.setOilType(gasObj.getString("oilType"));
		newIxPoiGasstation.setEgType(gasObj.getString("egType"));
		newIxPoiGasstation.setMgType(gasObj.getString("mgType"));
		newIxPoiGasstation.setPayment(gasObj.getString("payment"));
		newIxPoiGasstation.setService(gasObj.getString("service"));
		newIxPoiGasstation.setOpenHour(gasObj.getString("openHour"));
		newIxPoiGasstation.setRowId(gasObj.getString("rowId").toUpperCase());
		}
	}

	}

	/**
	 * @Title: usdateIxPoiParking
	 * @Description: 解析处理停车场数据
	 * @param poi
	 * @param jo
	 * @param pid
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年1月6日 下午3:02:46 
	 */
	private void usdateIxPoiParking(IxPoiObj poi, JSONObject jo, long pid) throws Exception {
		//查询的IX_POI_PARKING表
		List<IxPoiParking> parkList = poi.getIxPoiParkings();
		JSONObject parkObj = jo.getJSONObject("parkings");
		JSONArray oldArray = new JSONArray();
		if(parkList != null && parkList.size() > 0){
			if (parkObj != null && !parkObj.isEmpty()) {
				for (IxPoiParking oldPark : parkList) {
					JSONObject oldPoiGasObj = ((ISerializable) oldPark).Serialize(null);
					oldArray.add(oldPoiGasObj);
			}
				JSONObject newParkings = new JSONObject();
				newParkings.put("poiPid", pid);
				newParkings.put("parkingType", parkObj.getString("buildingType"));
				newParkings.put("tollStd", parkObj.getString("tollStd"));
				newParkings.put("tollDes", parkObj.getString("tollDes"));
				newParkings.put("tollWay", parkObj.getString("tollWay"));
				newParkings.put("payment", parkObj.getString("payment"));
				newParkings.put("remark", parkObj.getString("remark"));
				newParkings.put("openTiime", parkObj.getString("openTime"));
				newParkings.put("totalNum", parkObj.getInt("totalNum"));
				newParkings.put("resHigh", parkObj.getInt("resHigh"));
				newParkings.put("resWidth", parkObj.getInt("resWidth"));
				newParkings.put("resWeigh", parkObj.getInt("resWeigh"));
				newParkings.put("certificate", parkObj.getInt("certificate"));
				newParkings.put("vehicle", parkObj.getInt("vehicle"));
				newParkings.put("haveSpecialplace", parkObj.getString("haveSpecialPlace"));
				newParkings.put("womenNum", parkObj.getInt("womenNum"));
				newParkings.put("handicapNum", parkObj.getInt("handicapNum"));
				newParkings.put("miniNum", parkObj.getInt("miniNum"));
				newParkings.put("vipNum", parkObj.getInt("vipNum"));
				newParkings.put("rowId", parkObj.getString("rowId").toUpperCase());
				// 差分,区分新增修改
				int ret = getDifferent(oldArray, newParkings);
				if (ret == 0) {//新增
					IxPoiParking newIxPoiParking = poi.createIxPoiParking();
					newIxPoiParking.setPoiPid(pid);
					newIxPoiParking.setParkingType(parkObj.getString("buildingType"));
					newIxPoiParking.setTollStd(parkObj.getString("tollStd"));
					newIxPoiParking.setTollDes(parkObj.getString("tollDes"));
					newIxPoiParking.setTollWay(parkObj.getString("tollWay"));
					newIxPoiParking.setPayment(parkObj.getString("payment"));
					newIxPoiParking.setRemark(parkObj.getString("remark"));
					newIxPoiParking.setOpenTiime(parkObj.getString("openTime"));
					newIxPoiParking.setTotalNum(parkObj.getLong("totalNum"));
					newIxPoiParking.setResHigh(parkObj.getDouble("resHigh"));
					newIxPoiParking.setResWidth(parkObj.getDouble("resWidth"));
					newIxPoiParking.setResWeigh(parkObj.getDouble("resWeigh"));
					newIxPoiParking.setCertificate(parkObj.getInt("certificate"));
					newIxPoiParking.setVehicle(parkObj.getInt("vehicle"));
					newIxPoiParking.setHaveSpecialplace(parkObj.getString("haveSpecialPlace"));
					newIxPoiParking.setWomenNum(parkObj.getInt("womenNum"));
					newIxPoiParking.setHandicapNum(parkObj.getInt("handicapNum"));
					newIxPoiParking.setMiniNum(parkObj.getInt("miniNum"));
					newIxPoiParking.setVipNum(parkObj.getInt("vipNum"));
					newIxPoiParking.setRowId(parkObj.getString("rowId").toUpperCase());
					
				} else if (ret == 1) {//修改
					
					for (IxPoiParking oldpark : parkList) {
						if (oldpark.getRowId().equals(parkObj.getString("rowId").toUpperCase())) {
							oldpark.setPoiPid(pid);
							oldpark.setParkingType(parkObj.getString("buildingType"));
							oldpark.setTollStd(parkObj.getString("tollStd"));
							oldpark.setTollDes(parkObj.getString("tollDes"));
							oldpark.setTollWay(parkObj.getString("tollWay"));
							oldpark.setPayment(parkObj.getString("payment"));
							oldpark.setRemark(parkObj.getString("remark"));
							oldpark.setOpenTiime(parkObj.getString("openTime"));
							oldpark.setTotalNum(parkObj.getLong("totalNum"));
							oldpark.setResHigh(parkObj.getDouble("resHigh"));
							oldpark.setResWidth(parkObj.getDouble("resWidth"));
							oldpark.setResWeigh(parkObj.getDouble("resWeigh"));
							oldpark.setCertificate(parkObj.getInt("certificate"));
							oldpark.setVehicle(parkObj.getInt("vehicle"));
							oldpark.setHaveSpecialplace(parkObj.getString("haveSpecialPlace"));
							oldpark.setWomenNum(parkObj.getInt("womenNum"));
							oldpark.setHandicapNum(parkObj.getInt("handicapNum"));
							oldpark.setMiniNum(parkObj.getInt("miniNum"));
							oldpark.setVipNum(parkObj.getInt("vipNum"));
							
							break;
						}
					}
				}
			} else{
				// 删除的数据
				for (IxPoiParking oldpark : parkList) {
					poi.deleteSubrow(oldpark);
				}
			}
		}else{
			if (parkObj != null && !parkObj.isEmpty()) {
				IxPoiParking newIxPoiParking = poi.createIxPoiParking();
				newIxPoiParking.setPoiPid(pid);
				newIxPoiParking.setParkingType(parkObj.getString("buildingType"));
				newIxPoiParking.setTollStd(parkObj.getString("tollStd"));
				newIxPoiParking.setTollDes(parkObj.getString("tollDes"));
				newIxPoiParking.setTollWay(parkObj.getString("tollWay"));
				newIxPoiParking.setPayment(parkObj.getString("payment"));
				newIxPoiParking.setRemark(parkObj.getString("remark"));
				newIxPoiParking.setOpenTiime(parkObj.getString("openTime"));
				newIxPoiParking.setTotalNum(parkObj.getLong("totalNum"));
				newIxPoiParking.setResHigh(parkObj.getDouble("resHigh"));
				newIxPoiParking.setResWidth(parkObj.getDouble("resWidth"));
				newIxPoiParking.setResWeigh(parkObj.getDouble("resWeigh"));
				newIxPoiParking.setCertificate(parkObj.getInt("certificate"));
				newIxPoiParking.setVehicle(parkObj.getInt("vehicle"));
				newIxPoiParking.setHaveSpecialplace(parkObj.getString("haveSpecialPlace"));
				newIxPoiParking.setWomenNum(parkObj.getInt("womenNum"));
				newIxPoiParking.setHandicapNum(parkObj.getInt("handicapNum"));
				newIxPoiParking.setMiniNum(parkObj.getInt("miniNum"));
				newIxPoiParking.setVipNum(parkObj.getInt("vipNum"));
				newIxPoiParking.setRowId(parkObj.getString("rowId").toUpperCase());
			}
		}
	}
	
	
	/**
	 * @Title: usdateIxPoiHotel
	 * @Description: 解析处理 酒店数据
	 * @param poi
	 * @param jo
	 * @param pid
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年1月6日 下午3:03:39 
	 */
	private void usdateIxPoiHotel(IxPoiObj poi, JSONObject jo, long pid) throws Exception {
		//查询的IX_POI_Hotel表
		List<IxPoiHotel> hotelList = poi.getIxPoiHotels();
		JSONObject hotelObj = jo.getJSONObject("hotels");
		JSONArray oldArray = new JSONArray();
		if(hotelList != null && hotelList.size() > 0){//数据库存在原始数据
			
			if (hotelObj != null && !hotelObj.isEmpty()) {//poi上传的数据不为空
				for (IxPoiHotel oldHotel : hotelList) {
					JSONObject oldPoiGasObj = ((ISerializable) oldHotel).Serialize(null);
					oldArray.add(oldPoiGasObj);
				}
				JSONObject newHotel = new JSONObject();
				newHotel.put("poiPid", pid);
				newHotel.put("creditCard", hotelObj.getString("creditCards"));
				newHotel.put("rating", hotelObj.getInt("rating"));
				newHotel.put("checkinTime", hotelObj.getString("checkInTime"));
				newHotel.put("checkoutTime", hotelObj.getString("checkOutTime"));
				newHotel.put("roomCount", hotelObj.getInt("roomCount"));
				newHotel.put("roomType", hotelObj.getString("roomType"));
				newHotel.put("roomPrice", hotelObj.getString("roomPrice"));
				newHotel.put("breakfast", hotelObj.getInt("breakfast"));
				newHotel.put("service", hotelObj.getString("service"));
				newHotel.put("parking", hotelObj.getInt("parking"));
				newHotel.put("longDescription", hotelObj.getString("description"));
				newHotel.put("openHour", hotelObj.getString("openHour"));
				newHotel.put("rowId", hotelObj.getString("rowId").toUpperCase());
				// 差分,区分新增修改
				int ret = getDifferent(oldArray, newHotel);
				if (ret == 0) {//新增
					IxPoiHotel newIxPoiHotel = poi.createIxPoiHotel();
					newIxPoiHotel.setPoiPid(pid);
					newIxPoiHotel.setCreditCard(hotelObj.getString("creditCards"));
					newIxPoiHotel.setRating(hotelObj.getInt("rating"));
					newIxPoiHotel.setCheckinTime(hotelObj.getString("checkInTime"));
					newIxPoiHotel.setCheckoutTime(hotelObj.getString("checkOutTime"));
					newIxPoiHotel.setRoomCount(hotelObj.getInt("roomCount"));
					newIxPoiHotel.setRoomType(hotelObj.getString("roomType"));
					newIxPoiHotel.setRoomPrice(hotelObj.getString("roomPrice"));
					newIxPoiHotel.setBreakfast(hotelObj.getInt("breakfast"));
					newIxPoiHotel.setService(hotelObj.getString("service"));
					newIxPoiHotel.setParking(hotelObj.getInt("parking"));
					newIxPoiHotel.setLongDescription(hotelObj.getString("description"));
					newIxPoiHotel.setOpenHour(hotelObj.getString("openHour"));
					newIxPoiHotel.setRowId(hotelObj.getString("rowId").toUpperCase());
				} else if (ret == 1) {//修改
					for (IxPoiHotel oldhotel : hotelList) {
						if (oldhotel.getRowId().equals(hotelObj.getString("rowId").toUpperCase())) {
							oldhotel.setPoiPid(pid);
							oldhotel.setCreditCard(hotelObj.getString("creditCards"));
							oldhotel.setRating(hotelObj.getInt("rating"));
							oldhotel.setCheckinTime(hotelObj.getString("checkInTime"));
							oldhotel.setCheckoutTime(hotelObj.getString("checkOutTime"));
							oldhotel.setRoomCount(hotelObj.getInt("roomCount"));
							oldhotel.setRoomType(hotelObj.getString("roomType"));
							oldhotel.setRoomPrice(hotelObj.getString("roomPrice"));
							oldhotel.setBreakfast(hotelObj.getInt("breakfast"));
							oldhotel.setService(hotelObj.getString("service"));
							oldhotel.setParking(hotelObj.getInt("parking"));
							oldhotel.setLongDescription(hotelObj.getString("description"));
							oldhotel.setOpenHour(hotelObj.getString("openHour"));
							break;
						}
					}
				}
			} else if (hotelList.size() > 0) {
				// 删除的数据
				for (IxPoiHotel oldhotel : hotelList) {
					poi.deleteSubrow(oldhotel);
				}
			}
	}else{
		if (hotelObj != null && !hotelObj.isEmpty()) {//poi上传的数据不为空
			IxPoiHotel newIxPoiHotel = poi.createIxPoiHotel();
			newIxPoiHotel.setPoiPid(pid);
			newIxPoiHotel.setCreditCard(hotelObj.getString("creditCards"));
			newIxPoiHotel.setRating(hotelObj.getInt("rating"));
			newIxPoiHotel.setCheckinTime(hotelObj.getString("checkInTime"));
			newIxPoiHotel.setCheckoutTime(hotelObj.getString("checkOutTime"));
			newIxPoiHotel.setRoomCount(hotelObj.getInt("roomCount"));
			newIxPoiHotel.setRoomType(hotelObj.getString("roomType"));
			newIxPoiHotel.setRoomPrice(hotelObj.getString("roomPrice"));
			newIxPoiHotel.setBreakfast(hotelObj.getInt("breakfast"));
			newIxPoiHotel.setService(hotelObj.getString("service"));
			newIxPoiHotel.setParking(hotelObj.getInt("parking"));
			newIxPoiHotel.setLongDescription(hotelObj.getString("description"));
			newIxPoiHotel.setOpenHour(hotelObj.getString("openHour"));
			newIxPoiHotel.setRowId(hotelObj.getString("rowId").toUpperCase());
		}
	}
}
	
	/**
	 * @Title: usdateIxPoiFoodtypes
	 * @Description: 解析处理餐馆数据
	 * @param poi
	 * @param jo
	 * @param pid
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年1月6日 下午3:04:16 
	 */
	private void usdateIxPoiFoodtypes(IxPoiObj poi, JSONObject jo, long pid) throws Exception {
		//查询的IX_POI_Restaurant表
		List<IxPoiRestaurant> foodtypeList = poi.getIxPoiRestaurants();
		JSONObject foodtypesObj = jo.getJSONObject("foodtypes");
		JSONArray oldArray = new JSONArray();
		if(foodtypeList != null && foodtypeList.size() > 0 ){//数据库存在原始数据
					
			if (foodtypesObj != null && !foodtypesObj.isEmpty()) {//poi 上传的数据不为空
				for (IxPoiRestaurant oldFoodtype : foodtypeList) {
					JSONObject oldPoiGasObj = ((ISerializable) oldFoodtype).Serialize(null);
					oldArray.add(oldPoiGasObj);
				}
				JSONObject newFoodtype = new JSONObject();
				newFoodtype.put("poiPid", pid);
				newFoodtype.put("foodType", foodtypesObj.getString("foodtype"));
				newFoodtype.put("creditCard", foodtypesObj.getString("creditCards"));
				newFoodtype.put("avgCost", foodtypesObj.getInt("avgCost"));
				newFoodtype.put("parking", foodtypesObj.getInt("parking"));
				newFoodtype.put("openHour", foodtypesObj.getString("openHour"));
				newFoodtype.put("rowId", foodtypesObj.getString("rowId").toUpperCase());
				// 差分,区分新增修改
				int ret = getDifferent(oldArray, newFoodtype);
				if (ret == 0) {//新增
					IxPoiRestaurant newIxPoiRestaurant = poi.createIxPoiRestaurant();
					newIxPoiRestaurant.setPoiPid(pid);
					newIxPoiRestaurant.setFoodType(foodtypesObj.getString("foodtype"));
					newIxPoiRestaurant.setCreditCard(foodtypesObj.getString("creditCards"));
					newIxPoiRestaurant.setAvgCost(foodtypesObj.getInt("avgCost"));
					newIxPoiRestaurant.setParking(foodtypesObj.getInt("parking"));
					newIxPoiRestaurant.setOpenHour(foodtypesObj.getString("openHour"));
					newIxPoiRestaurant.setRowId(foodtypesObj.getString("rowId").toUpperCase());
					// 鲜度验证
					//freshFlag = false;
				} else if (ret == 1) {//修改
					for (IxPoiRestaurant oldfoodtype : foodtypeList) {
						if (oldfoodtype.getRowId().equals(foodtypesObj.getString("rowId").toUpperCase())) {
							oldfoodtype.setPoiPid(pid);
							oldfoodtype.setFoodType(foodtypesObj.getString("foodtype"));
							oldfoodtype.setCreditCard(foodtypesObj.getString("creditCards"));
							oldfoodtype.setAvgCost(foodtypesObj.getInt("avgCost"));
							oldfoodtype.setParking(foodtypesObj.getInt("parking"));
							oldfoodtype.setOpenHour(foodtypesObj.getString("openHour"));
							break;
						}
					}
				}
			} else{
				// 删除的数据
				for (IxPoiRestaurant oldfoodtype : foodtypeList) {
					poi.deleteSubrow(oldfoodtype);
				}
			}
		}else{
			if (foodtypesObj != null && !foodtypesObj.isEmpty()) {//poi 上传的数据不为空
				IxPoiRestaurant newIxPoiRestaurant = poi.createIxPoiRestaurant();
				newIxPoiRestaurant.setPoiPid(pid);
				newIxPoiRestaurant.setFoodType(foodtypesObj.getString("foodtype"));
				newIxPoiRestaurant.setCreditCard(foodtypesObj.getString("creditCards"));
				newIxPoiRestaurant.setAvgCost(foodtypesObj.getInt("avgCost"));
				newIxPoiRestaurant.setParking(foodtypesObj.getInt("parking"));
				newIxPoiRestaurant.setOpenHour(foodtypesObj.getString("openHour"));
				newIxPoiRestaurant.setRowId(foodtypesObj.getString("rowId").toUpperCase());
			}
		}
	}


	/**
	 * @Title: usdateIxPoiChargingStation
	 * @Description: 解析处理充电站数据
	 * @param poi
	 * @param jo
	 * @param pid
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年1月6日 下午3:05:14 
	 */
	private void usdateIxPoiChargingStation(IxPoiObj poi, JSONObject jo, long pid) throws Exception {
		//查询的IX_POI_ChargingStation表
		List<IxPoiChargingstation> chargingStationList = poi.getIxPoiChargingstations();
		JSONObject chargingStationObj = jo.getJSONObject("chargingStation");
		JSONArray oldArray = new JSONArray();
		if(chargingStationList != null && chargingStationList.size() > 0 ){//数据库中存在原始数据
			if (chargingStationObj != null && !chargingStationObj.isEmpty()) {//poi 上传的数据不为空
				for (IxPoiChargingstation oldChargingStation : chargingStationList) {
					JSONObject oldPoiGasObj = ((ISerializable) oldChargingStation).Serialize(null);
					oldArray.add(oldPoiGasObj);
				}
				JSONObject newChargingStation = new JSONObject();
				newChargingStation.put("poiPid", pid);
				newChargingStation.put("chargingType", chargingStationObj.getInt("type"));
				newChargingStation.put("changeBrands", chargingStationObj.getString("changeBrands"));
				newChargingStation.put("changeOpenType", chargingStationObj.getString("changeOpenType"));
				newChargingStation.put("chargingNum", chargingStationObj.getInt("chargingNum"));
				newChargingStation.put("serviceProv", chargingStationObj.getString("servicePro"));
				newChargingStation.put("openHour", chargingStationObj.getString("openHour"));
				newChargingStation.put("parkingFees", chargingStationObj.getInt("parkingFees"));
				newChargingStation.put("parkingInfo", chargingStationObj.getString("parkingInfo"));
				newChargingStation.put("availableState", chargingStationObj.getInt("availableState"));
				newChargingStation.put("rowId", chargingStationObj.getString("rowId").toUpperCase());
				// 差分,区分新增修改
				int ret = getDifferent(oldArray, newChargingStation);
				if (ret == 0) {//新增
					IxPoiChargingstation newIxPoiChargingstation = poi.createIxPoiChargingstation();
					newIxPoiChargingstation.setPoiPid(pid);
					newIxPoiChargingstation.setChargingType(chargingStationObj.getInt("type"));
					newIxPoiChargingstation.setChangeBrands(chargingStationObj.getString("changeBrands"));
					newIxPoiChargingstation.setChangeOpenType(chargingStationObj.getString("changeOpenType"));
					newIxPoiChargingstation.setChargingNum(chargingStationObj.getInt("chargingNum"));
					newIxPoiChargingstation.setServiceProv(chargingStationObj.getString("servicePro"));
					newIxPoiChargingstation.setOpenHour(chargingStationObj.getString("openHour"));
					newIxPoiChargingstation.setParkingFees(chargingStationObj.getInt("parkingFees"));
					newIxPoiChargingstation.setParkingInfo(chargingStationObj.getString("parkingInfo"));
					newIxPoiChargingstation.setAvailableState(chargingStationObj.getInt("availableState"));
					newIxPoiChargingstation.setRowId(chargingStationObj.getString("rowId").toUpperCase());
				} else if (ret == 1) {//修改
					for (IxPoiChargingstation oldChargingstation : chargingStationList) {
						if (oldChargingstation.getRowId().equals(chargingStationObj.getString("rowId").toUpperCase())) {
							oldChargingstation.setPoiPid(pid);
							oldChargingstation.setChargingType(chargingStationObj.getInt("type"));
							oldChargingstation.setChangeBrands(chargingStationObj.getString("changeBrands"));
							oldChargingstation.setChangeOpenType(chargingStationObj.getString("changeOpenType"));
							oldChargingstation.setChargingNum(chargingStationObj.getInt("chargingNum"));
							oldChargingstation.setServiceProv(chargingStationObj.getString("servicePro"));
							oldChargingstation.setOpenHour(chargingStationObj.getString("openHour"));
							oldChargingstation.setParkingFees(chargingStationObj.getInt("parkingFees"));
							oldChargingstation.setParkingInfo(chargingStationObj.getString("parkingInfo"));
							oldChargingstation.setAvailableState(chargingStationObj.getInt("availableState"));
							break;
						}
					}
				}
			} else{
				// 删除的数据
				for (IxPoiChargingstation oldChargingstation : chargingStationList) {
					poi.deleteSubrow(oldChargingstation);
				}
			}
		}else{
			if (chargingStationObj != null && !chargingStationObj.isEmpty()) {//poi 上传的数据不为空
				IxPoiChargingstation newIxPoiChargingstation = poi.createIxPoiChargingstation();
				newIxPoiChargingstation.setPoiPid(pid);
				newIxPoiChargingstation.setChargingType(chargingStationObj.getInt("type"));
				newIxPoiChargingstation.setChangeBrands(chargingStationObj.getString("changeBrands"));
				newIxPoiChargingstation.setChangeOpenType(chargingStationObj.getString("changeOpenType"));
				newIxPoiChargingstation.setChargingNum(chargingStationObj.getInt("chargingNum"));
				newIxPoiChargingstation.setServiceProv(chargingStationObj.getString("servicePro"));
				newIxPoiChargingstation.setOpenHour(chargingStationObj.getString("openHour"));
				newIxPoiChargingstation.setParkingFees(chargingStationObj.getInt("parkingFees"));
				newIxPoiChargingstation.setParkingInfo(chargingStationObj.getString("parkingInfo"));
				newIxPoiChargingstation.setAvailableState(chargingStationObj.getInt("availableState"));
				newIxPoiChargingstation.setRowId(chargingStationObj.getString("rowId").toUpperCase());
			}
		}
	}

	/**
	 * @Title: usdateIxPoiChargingPlot
	 * @Description: 解析处理充电桩数据
	 * @param poi
	 * @param jo
	 * @param pid
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年1月6日 下午3:06:11 
	 */
	private void usdateIxPoiChargingPlot(IxPoiObj poi, JSONObject jo, long pid) throws Exception {
		//查询的IX_POI_Chargingplot表
		List<IxPoiChargingplot> oldchargingplotList = poi.getIxPoiChargingplots();
		JSONArray chargingPoleList = jo.getJSONArray("chargingPole");
		JSONArray oldArray = new JSONArray();
		if(oldchargingplotList != null && oldchargingplotList.size() > 0 ){//数据库有原始值
					
			if (chargingPoleList != null && chargingPoleList.size() > 0) {//poi 上传数据中有值
				for (IxPoiChargingplot oldChargingplot : oldchargingplotList) {
					JSONObject oldPoiGasObj = ((ISerializable) oldChargingplot).Serialize(null);
					oldArray.add(oldPoiGasObj);
				}
				
				List<String> newRowIdList = new ArrayList<String>();
				for (int k = 0; k < chargingPoleList.size(); k++) {
					JSONObject chargingPoleObj = chargingPoleList.getJSONObject(k);
					newRowIdList.add(chargingPoleObj.getString("rowId").toUpperCase());
					
					JSONObject newChargingPole = new JSONObject();
					newChargingPole.put("poiPid", pid);
					newChargingPole.put("groupId", chargingPoleObj.getInt("groupId"));
					newChargingPole.put("count", chargingPoleObj.getInt("count"));
					newChargingPole.put("acdc", chargingPoleObj.getInt("acdc"));
					newChargingPole.put("plugType", chargingPoleObj.getString("plugType"));
					newChargingPole.put("power", chargingPoleObj.getString("power"));
					newChargingPole.put("voltage", chargingPoleObj.getString("voltage"));
					newChargingPole.put("current", chargingPoleObj.getString("current"));
					newChargingPole.put("mode", chargingPoleObj.getInt("mode"));
					newChargingPole.put("plugNum", chargingPoleObj.getInt("plugNum"));
					newChargingPole.put("prices", chargingPoleObj.getString("prices"));
					newChargingPole.put("openType", chargingPoleObj.getString("openType"));
					newChargingPole.put("availableState", chargingPoleObj.getInt("availableState"));
					newChargingPole.put("manufacturer", chargingPoleObj.getString("manufacturer"));
					newChargingPole.put("factoryNum", chargingPoleObj.getString("factoryNum"));
					newChargingPole.put("plotNum", chargingPoleObj.getString("plotNum"));
					newChargingPole.put("productNum", chargingPoleObj.getString("productNum"));
					newChargingPole.put("parkingNum", chargingPoleObj.getString("parkingNum"));
					newChargingPole.put("floor", chargingPoleObj.getInt("floor"));
					newChargingPole.put("locationType", chargingPoleObj.getInt("locationType"));
					newChargingPole.put("payment", chargingPoleObj.getString("payment"));
					newChargingPole.put("rowId", chargingPoleObj.getString("rowId").toUpperCase());
				
					// 差分,区分新增修改
					int ret = getDifferent(oldArray, newChargingPole);
					if (ret == 0) {//新增
						IxPoiChargingplot newIxPoiChargingplot = poi.createIxPoiChargingplot();
						newIxPoiChargingplot.setPoiPid(pid);
						newIxPoiChargingplot.setGroupId(chargingPoleObj.getInt("groupId"));
						newIxPoiChargingplot.setCount(chargingPoleObj.getInt("count"));
						newIxPoiChargingplot.setAcdc(chargingPoleObj.getInt("acdc"));
						newIxPoiChargingplot.setPlugType(chargingPoleObj.getString("plugType"));
						newIxPoiChargingplot.setPower(chargingPoleObj.getString("power"));
						newIxPoiChargingplot.setVoltage(chargingPoleObj.getString("voltage"));
						newIxPoiChargingplot.setCurrent(chargingPoleObj.getString("current"));
						newIxPoiChargingplot.setMode(chargingPoleObj.getInt("mode"));
						newIxPoiChargingplot.setPlugNum(chargingPoleObj.getInt("plugNum"));
						newIxPoiChargingplot.setPrices(chargingPoleObj.getString("prices"));
						newIxPoiChargingplot.setOpenType(chargingPoleObj.getString("openType"));
						newIxPoiChargingplot.setAvailableState(chargingPoleObj.getInt("availableState"));
						newIxPoiChargingplot.setManufacturer(chargingPoleObj.getString("manufacturer"));
						newIxPoiChargingplot.setFactoryNum(chargingPoleObj.getString("factoryNum"));
						newIxPoiChargingplot.setPlotNum(chargingPoleObj.getString("plotNum"));
						newIxPoiChargingplot.setProductNum(chargingPoleObj.getString("productNum"));
						newIxPoiChargingplot.setParkingNum(chargingPoleObj.getString("parkingNum"));
						newIxPoiChargingplot.setFloor(chargingPoleObj.getInt("floor"));
						newIxPoiChargingplot.setLocationType(chargingPoleObj.getInt("locationType"));
						newIxPoiChargingplot.setPayment(chargingPoleObj.getString("payment"));
						newIxPoiChargingplot.setRowId(chargingPoleObj.getString("rowId"));
						// 鲜度验证
						//freshFlag = false;
					} else if (ret == 1) {//修改
						//long oldPid = 0;
						for (IxPoiChargingplot oldChargingplot : oldchargingplotList) {
							if (oldChargingplot.getRowId().equals(chargingPoleObj.getString("rowId").toUpperCase())) {
								oldChargingplot.setPoiPid(pid);
								oldChargingplot.setGroupId(chargingPoleObj.getInt("groupId"));
								oldChargingplot.setCount(chargingPoleObj.getInt("count"));
								oldChargingplot.setAcdc(chargingPoleObj.getInt("acdc"));
								oldChargingplot.setPlugType(chargingPoleObj.getString("plugType"));
								oldChargingplot.setPower(chargingPoleObj.getString("power"));
								oldChargingplot.setVoltage(chargingPoleObj.getString("voltage"));
								oldChargingplot.setCurrent(chargingPoleObj.getString("current"));
								oldChargingplot.setMode(chargingPoleObj.getInt("mode"));
								oldChargingplot.setPlugNum(chargingPoleObj.getInt("plugNum"));
								oldChargingplot.setPrices(chargingPoleObj.getString("prices"));
								oldChargingplot.setOpenType(chargingPoleObj.getString("openType"));
								oldChargingplot.setAvailableState(chargingPoleObj.getInt("availableState"));
								oldChargingplot.setManufacturer(chargingPoleObj.getString("manufacturer"));
								oldChargingplot.setFactoryNum(chargingPoleObj.getString("factoryNum"));
								oldChargingplot.setPlotNum(chargingPoleObj.getString("plotNum"));
								oldChargingplot.setProductNum(chargingPoleObj.getString("productNum"));
								oldChargingplot.setParkingNum(chargingPoleObj.getString("parkingNum"));
								oldChargingplot.setFloor(chargingPoleObj.getInt("floor"));
								oldChargingplot.setLocationType(chargingPoleObj.getInt("locationType"));
								oldChargingplot.setPayment(chargingPoleObj.getString("payment"));
								break;
							}
						}
					}
				}
				//差分删除
				for (IxPoiChargingplot oldChargingplot : oldchargingplotList) {
					if(!newRowIdList.contains(oldChargingplot.getRowId().toUpperCase())){//原poi中存在的子,上传的poi中不存在,则删除此子
						poi.deleteSubrow(oldChargingplot);
					}
				}
			} else{
				// 删除的原始数据
				for (IxPoiChargingplot oldChargingplot : oldchargingplotList) {
					poi.deleteSubrow(oldChargingplot);
				}
			}
		}else{//数据库中没有原始值,直接新增
			if (chargingPoleList != null && chargingPoleList.size() > 0) {
				for (int k = 0; k < chargingPoleList.size(); k++) {
					JSONObject chargingPoleObj = chargingPoleList.getJSONObject(k);
					IxPoiChargingplot newIxPoiChargingplot = poi.createIxPoiChargingplot();
					newIxPoiChargingplot.setPoiPid(pid);
					newIxPoiChargingplot.setGroupId(chargingPoleObj.getInt("groupId"));
					newIxPoiChargingplot.setCount(chargingPoleObj.getInt("count"));
					newIxPoiChargingplot.setAcdc(chargingPoleObj.getInt("acdc"));
					newIxPoiChargingplot.setPlugType(chargingPoleObj.getString("plugType"));
					newIxPoiChargingplot.setPower(chargingPoleObj.getString("power"));
					newIxPoiChargingplot.setVoltage(chargingPoleObj.getString("voltage"));
					newIxPoiChargingplot.setCurrent(chargingPoleObj.getString("current"));
					newIxPoiChargingplot.setMode(chargingPoleObj.getInt("mode"));
					newIxPoiChargingplot.setPlugNum(chargingPoleObj.getInt("plugNum"));
					newIxPoiChargingplot.setPrices(chargingPoleObj.getString("prices"));
					newIxPoiChargingplot.setOpenType(chargingPoleObj.getString("openType"));
					newIxPoiChargingplot.setAvailableState(chargingPoleObj.getInt("availableState"));
					newIxPoiChargingplot.setManufacturer(chargingPoleObj.getString("manufacturer"));
					newIxPoiChargingplot.setFactoryNum(chargingPoleObj.getString("factoryNum"));
					newIxPoiChargingplot.setPlotNum(chargingPoleObj.getString("plotNum"));
					newIxPoiChargingplot.setProductNum(chargingPoleObj.getString("productNum"));
					newIxPoiChargingplot.setParkingNum(chargingPoleObj.getString("parkingNum"));
					newIxPoiChargingplot.setFloor(chargingPoleObj.getInt("floor"));
					newIxPoiChargingplot.setLocationType(chargingPoleObj.getInt("locationType"));
					newIxPoiChargingplot.setPayment(chargingPoleObj.getString("payment"));
					newIxPoiChargingplot.setRowId(chargingPoleObj.getString("rowId"));
			}
		}
		}
	}
	
	// 差分,区分新增修改
		@SuppressWarnings("unchecked")
		private int getDifferent(JSONArray oldArray, JSONObject newObj) throws Exception {
			try {
				int ret = 0;
				boolean theSame = false;
				boolean change = false;
				for (int i = 0; i < oldArray.size(); i++) {
					String newRowid = newObj.getString("rowId").toUpperCase();
					JSONObject old = oldArray.getJSONObject(i);
					if (old.getString("rowId").equals(newRowid)) {
						theSame = true;
						// rowid相同，有其他字段不同的情况下，为修改，返回1；
						// 没有rowid相同的，是新增，返回0
						Iterator<String> it = newObj.keySet().iterator();
						while (it.hasNext()) {
							String key = it.next();
							if (!key.equals("uRecord") && !key.equals("uDate") && !key.equals("pid") 
									&& !key.equals("log") && !key.equals("opType") && !key.equals("objPid") 
									&& !key.equals("oldValues") && !key.equals("hisChangeLogs")
									) {
								boolean flag = old.getString(key).equals(newObj.getString(key));
								if (flag) {
									continue;
								} else {
									// 修改
									ret = 1;
									change = true;
									break;
								}
							}

						}
					}
				}
				// rowid相同,但无修改字段，不处理，返回2
				if (theSame && !change) {
					ret = 2;
				}
				return ret;
			} catch (Exception e) {
				throw e;
			}
		}

		// 差分，区分删除的数据
		private JSONArray getOldDel(JSONArray oldArray, List<String> newRowIdList) throws Exception {
			try {
				JSONArray retArray = new JSONArray();
				for (int i = 0; i < oldArray.size(); i++) {
					boolean flag = true;
					JSONObject jsonObj = oldArray.getJSONObject(i);
					for (String rowid : newRowIdList) {
						rowid = rowid.toUpperCase();
						if (rowid.equals(jsonObj.getString("rowId"))) {
							flag = false;
							break;
						}
					}
					// 未找到rowid，为删除数据
					if (flag) {
						jsonObj.put("objStatus", ObjStatus.DELETE.toString());
						retArray.add(jsonObj);
					}
				}
				return retArray;
			} catch (Exception e) {
				throw e;
			}
		}
	
	/**
	 * 改地址
	 * @author Han Shaoming
	 * @param poi
	 * @param jo
	 * @param langCode
	 * @throws Exception 
	 */
	public void usdateAddress(IxPoiObj poi,JSONObject jo,String langCode) throws Exception{
		//查询的IX_POI_ADDRESS表
		List<IxPoiAddress> ixPoiAddresses = poi.getIxPoiAddresses();
		if(!JSONUtils.isNull(jo.get("address"))){
			if(StringUtils.isNotEmpty(jo.getString("address"))){
				String address = jo.getString("address");
				boolean flag = true;
				if(ixPoiAddresses !=  null && ixPoiAddresses.size() > 0){
					for (IxPoiAddress ixPoiAddress : ixPoiAddresses) {
						//多源address不为空，赋值给IX_POI_ADDRESS.FULLNAME(中文地址)
						if("CHI".equals(ixPoiAddress.getLangCode())){
							ixPoiAddress.setFullname(address);
							flag = false;
						}else if("CHT".equals(ixPoiAddress.getLangCode())){
							ixPoiAddress.setFullname(address);
							flag = false;
						}
					}
				}
				
				if(flag){
					//不存在IX_POI_ADDRESS记录，则增加一条记录
					//IX_POI_ADDRESS表
					IxPoiAddress ixPoiAddress = poi.createIxPoiAddress();
					ixPoiAddress.setFullname(address);
					ixPoiAddress.setLangCode(langCode);
				}
			}else{
				//逻辑删除日库中所有地址记录
				for (IxPoiAddress ixPoiAddress : ixPoiAddresses) {
					poi.deleteSubrow(ixPoiAddress);
				}
			}
		}
	}
	
	/**
	 * 改电话
	 * @author Han Shaoming
	 * @param poi
	 * @param jo
	 * @throws Exception
	 */
	public void usdateContact(IxPoiObj poi,JSONObject jo) throws Exception{
		//[集合]联系方式
		String contacts = null;
		if(!JSONUtils.isNull(jo.get("contacts"))){
			contacts = jo.getString("contacts");
		}else{
			throw new Exception("联系方式contacts字段名不存在");
		}
		//查询IX_POI_RESTAURANT表
		List<IxPoiContact> ixPoiContacts = poi.getIxPoiContacts();
		if(!"[]".equals(contacts)){
			JSONArray ja = JSONArray.fromObject(contacts);
			//保存查询IX_POI_RESTAURANT表
			List<IxPoiContact> contactList = new ArrayList<IxPoiContact>();
			//多源中不存在，但是日库中存在的电话逻辑删除
			if(ixPoiContacts != null && ixPoiContacts.size() > 0){
				for (IxPoiContact contact : ixPoiContacts) {
					for (int i=0;i<ja.size();i++) {
						JSONObject jso = ja.getJSONObject(i);
						//号码number
						if(!JSONUtils.isNull(jso.get("number"))){
							String number = jso.getString("number");
							if(!number.equals(contact.getContact())){
								poi.deleteSubrow(contact);
							}
						}else{
							throw new Exception("号码number字段名不存在");
						}
					}
					contactList.add(contact);
				}
			}
			
			for (int i=0;i<ja.size();i++) {
				JSONObject jso = ja.getJSONObject(i);
				//号码number
				String number = null;
				if(!JSONUtils.isNull(jso.get("number"))){
					number = jso.getString("number");
				}else{
					throw new Exception("号码number字段名不存在");
				}
				//联系方式类型type
				int type = jso.getInt("type");
				for (IxPoiContact contact : contactList) {
					if(!number.equals(contact.getContact())){
						//不一致，则将多源中电话和电话类型追加到日库中
						//IX_POI_CONTACT表
						IxPoiContact ixPoiContact = poi.createIxPoiContact();
						ixPoiContact.setContact(number);
						ixPoiContact.setContactType(type);
					}else if(number.equals(contact.getContact())
							&&type!=contact.getContactType()){
						//不一致，则更新电话和电话类型追加到日库中
						//IX_POI_CONTACT表
						IxPoiContact ixPoiContact = poi.createIxPoiContact();
						ixPoiContact.setContact(number);
						ixPoiContact.setContactType(type);
					}else if(number.equals(contact.getContact())
							&&type == contact.getContactType()){
						//一致，则不处理
					}
				}
			}
		}else if("[]".equals(contacts)){
			//逻辑删除日库中所有电话记录
			for (IxPoiContact ixPoiContact : ixPoiContacts) {
				poi.deleteSubrow(ixPoiContact);
			}
		}
	}
	
	/**
	 * 改名称
	 * @author Han Shaoming
	 * @param poi
	 * @param jo
	 * @param langCode
	 * @throws Exception
	 */
	public void usdateName(IxPoiObj poi,JSONObject jo,String langCode) throws Exception{
		//名称
		String name = null;
		if(!JSONUtils.isNull(jo.get("name"))){
			name = jo.getString("name");
		}else{
			throw new Exception("名称name字段名不存在");
		}
		//IX_POI_NAME表
		List<IxPoiName> ixPoiNames = poi.getIxPoiNames();
		boolean flag = true;
		if(ixPoiNames != null && ixPoiNames.size()>0){
			for (IxPoiName ixPoiName : ixPoiNames) {
				if(ixPoiName.getNameClass()==1 &&
						ixPoiName.getNameType()==2 &&
						"CHI".equals(ixPoiName.getLangCode())){
					ixPoiName.setName(name);
					flag = false;
				}else if(ixPoiName.getNameClass()==1 &&
						ixPoiName.getNameType()==2 &&
						"CHT".equals(ixPoiName.getLangCode())){
					ixPoiName.setName(name);
					flag = false;
				}
			}
			if(flag){
				
				//IX_POI_NAME表
				IxPoiName ixPoiName = poi.createIxPoiName();
				ixPoiName.setName(name);
				ixPoiName.setNameClass(1);
				ixPoiName.setNameType(2);
				ixPoiName.setLangCode(langCode);
			}
			
		}
	}
	
	/**
	 * 该风味类型
	 * @author Han Shaoming
	 * @param poi
	 * @param jo
	 * @throws Exception
	 */
	public void usdateFoodType(IxPoiObj poi,JSONObject jo) throws Exception{
		//查询的IX_POI_RESTAURANT表
		List<IxPoiRestaurant> ixPoiRestaurants = poi.getIxPoiRestaurants();
		//[集合]风味类型
		String foodType = null;
		if(!JSONUtils.isNull(jo.get("foodType"))){
			foodType = jo.getString("foodType");
		}else{
			throw new Exception("风味类型foodType字段名不存在");
		}
		if(StringUtils.isNotEmpty(foodType)){
			if(ixPoiRestaurants != null && ixPoiRestaurants.size()>0){
				ixPoiRestaurants.get(0).setFoodType(foodType);
			}else{
				//IX_POI_RESTAURANT表
				IxPoiRestaurant ixPoiRestaurant = poi.createIxPoiRestaurant();
				ixPoiRestaurant.setFoodType(foodType);
			}
		}else{
			//逻辑删除日库中所有风味类型记录
			IxPoiRestaurant ixPoiRestaurant = ixPoiRestaurants.get(0);
			poi.deleteSubrow(ixPoiRestaurant);
		}
	}

	
	public boolean importDeleteByJson(IxPoiObj poi,JSONObject jo)throws Exception {
		if(poi!=null&&jo!=null){
			if(poi instanceof IxPoiObj){
				//判断是否已逻辑删除
				if(poi.isDeleted()){
					//已逻辑删除
					throw new Exception("该数据已经逻辑删除");
				}else{
					//该对象逻辑删除
					poi.deleteObj();
					
					//处理父子关系
					PoiRelation pr = new PoiRelation();
					pr.setPid(poi.objPid());
					pr.setPoiRelationType(PoiRelationType.FATHER_AND_SON);
					parentPid.add(pr);
					
					//处理同一关系
					PoiRelation sr = new PoiRelation();
					sr.setPid(poi.objPid());
					sr.setPoiRelationType(PoiRelationType.SAME_POI);
					//samePoiRel.add(sr);
					parentPid.add(sr);
				}
				return true;
			}else{
				throw new ImportException("不支持的对象类型");
			}
		}
		return false;
		
	}
	
	/**
	 * 获取查询所需子表
	 * @author Han Shaoming
	 * @return
	 */
	public Set<String> getTabNames(){
		//添加所需的子表
		Set<String> tabNames = new HashSet<>();
		tabNames.add("IX_POI_NAME");
		tabNames.add("IX_POI_CONTACT");
		tabNames.add("IX_POI_ADDRESS");
		tabNames.add("IX_POI_RESTAURANT");
		tabNames.add("IX_POI_CHILDREN");
		tabNames.add("IX_POI_PARENT");
		tabNames.add("IX_POI_DETAIL");
		tabNames.add("IX_POI_PHOTO");
		tabNames.add("IX_POI_GASSTATION");
		tabNames.add("IX_POI_PARKING");
		tabNames.add("IX_POI_HOTEL");
		tabNames.add("IX_POI_CHARGINGSTATION");
		tabNames.add("IX_POI_CHARGINGPLOT");
		return tabNames;
	}
	
	

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "MultiSrcPoiImportorByGather";
	}
	
	private void filterAddedPoi(Map<String, JSONObject> addPois)throws Exception{
		Map<String,Long> exists = IxPoiSelector.getPidByFids(conn,addPois.keySet());
		if(exists==null)return;
		for(String fid:exists.keySet()){
			errLog.put(fid, "fid库中已存在");
			addPois.remove(fid);
		}
	}
	
}
