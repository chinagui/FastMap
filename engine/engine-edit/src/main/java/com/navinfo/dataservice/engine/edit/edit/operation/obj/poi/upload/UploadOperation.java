package com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.upload;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiPhoto;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class UploadOperation {
	
	/**
	 * 读取txt，解析，入库
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public JSONObject importPoi(String fileName)throws Exception{
		JSONObject retObj = new JSONObject();
		Scanner importPois = new Scanner(new FileInputStream(fileName));
		JSONArray ja = new JSONArray();
		while (importPois.hasNextLine()) {
			try {
				String line = importPois.nextLine();
				JSONObject json = JSONObject.fromObject(line);
				ja.add(json);
			} catch (Exception e) {
				throw e;
			}
		}
		retObj = changeData(ja);
		return retObj;
	}
	
	/**
	 * 数据解析分类
	 * @param line
	 * @return
	 */
	@SuppressWarnings("static-access")
	private JSONObject changeData(JSONArray ja) throws Exception{
		JSONObject retObj = new JSONObject();
		List<String> errList = new ArrayList<String>();
		// 获取当前做业季
		String version = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		try {
			Map<String,List<JSONObject>> data = new HashMap<String,List<JSONObject>>();
			Connection manConn = null;
			manConn = DBConnector.getInstance().getManConnection();
			for (int i=0;i<ja.size();i++) {
				JSONObject jo = ja.getJSONObject(i);
				
				// 坐标确定grid，grid确定区库ID
				String wkt = jo.getString("geometry");
				Geometry point = new WKTReader().read(wkt);
				Coordinate[] coordinate =point.getCoordinates();
				CompGridUtil gridUtil = new CompGridUtil();
				String grid = gridUtil.point2Grids(coordinate[0].x, coordinate[0].y)[0];
				String manQuery = "SELECT region_id FROM grid WHERE grid_id=:1";
				PreparedStatement pstmt = null;
				pstmt = manConn.prepareStatement(manQuery);
				pstmt.setString(1, grid);
				ResultSet resultSet = pstmt.executeQuery();
				String dbId = "";
				if (resultSet.next()){
					dbId = resultSet.getString("region_id");
				} else {
					String errstr = "fid:" + jo.getString("fid") + "不在已知grid内";
					errList.add(errstr);
				}
				
				if (!data.containsKey(dbId)) {
					List<JSONObject> dataList = new ArrayList<JSONObject>();
					dataList.add(jo);
					data.put(dbId, dataList);
				} else {
					List<JSONObject> dataList = data.get(dbId);
					dataList.add(jo);
					data.put(dbId, dataList);
				}
			}
			
			// 确认每个点属于新增、修改还是删除
			// fid在IX_POI.POI_NUM中查找不到，并且待上传数据lifecycle不为1，为新增
			// fid能找到，并且待上传数据lifecycle不为1且对应的IX_POI.U_RECORD不为2（删除），即为修改
			// fid能找到，并且待上传数据lifecycle为1，即为删除
			String subQuery = "SELECT poi_num,u_record FROM ix_poi WHERE poi_num=':1'";
			JSONObject insertObj= new JSONObject();
			JSONObject updateObj = new JSONObject();
			JSONObject deleteObj = new JSONObject();
			for (Iterator<String> iter = data.keySet().iterator();iter.hasNext();){
				// 创建连接，查找FID是否存在
				String dbId = iter.next();
				Connection conn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dbId));
				List<JSONObject> dataList = data.get(dbId);
				List<JSONObject> insertList = new ArrayList<JSONObject>();
				List<JSONObject> updateList = new ArrayList<JSONObject>();
				List<JSONObject> deleteList = new ArrayList<JSONObject>();
				for (int i=0;i<dataList.size();i++) {
					JSONObject poi = dataList.get(i);
					String fid = poi.getString("fid");
					int lifecycle = poi.getInt("lifecycle");
					PreparedStatement pstmt = null;
					pstmt = conn.prepareStatement(subQuery);
					pstmt.setString(1, fid);
					ResultSet resultSet = pstmt.executeQuery();
					// 判断每一条数据是新增、修改还是删除
					if (resultSet.next()) {
						// 能找到，判断lifecycle和u_record
						if (lifecycle == 1) {
							deleteList.add(poi);
						} else {
							int uRecord = resultSet.getInt("u_record");
							if (uRecord == 2) {
								String errstr = "fid:" + fid + "为修改数据，但库中u_record为2";
								errList.add(errstr);
							} else {
								updateList.add(poi);
							}
						}
					} else {
						// 找不到，判断lifecycle是否为1
						if (lifecycle == 1) {
							String errstr = "fid:" + fid + "在库中找不到对应数据，但lifecycle为1";
							errList.add(errstr);
						} else {
							insertList.add(poi);
						}
					}
				}
				
				// 将数据分为新增、修改和删除三组，key值为区库ID
				insertObj.put(dbId, insertList);
				updateObj.put(dbId, updateList);
				deleteObj.put(dbId, deleteList);
				
			}
			
			// 数据入库处理
			JSONObject insertRet = insertData(insertObj,version);
			JSONObject updateRet = updateData(updateObj,version);
			JSONObject deleteRet = deleteData(deleteObj,version);
			
			int insertCount = insertRet.getInt("success");
			int updateCount = updateRet.getInt("success");
			int deleteCount = deleteRet.getInt("success");
			
			int total = insertCount + updateCount + deleteCount;
			
			@SuppressWarnings("unchecked")
			List<String> insertFail = (List<String>)insertRet.get("fail");
			@SuppressWarnings("unchecked")
			List<String> updateFail = (List<String>)updateRet.get("fail");
			@SuppressWarnings("unchecked")
			List<String> deleteFail = (List<String>)deleteRet.get("fail");
			
			errList.addAll(insertFail);
			errList.addAll(updateFail);
			errList.addAll(deleteFail);
			
			retObj.put("success", total);
			retObj.put("fail", errList);
			
		} catch (Exception e) {
			throw e;
		}
		return retObj;
	}
	
	// 处理新增数据
	@SuppressWarnings({ "unchecked", "static-access" })
	private JSONObject insertData(JSONObject insertObj,String version) {
		try {
			JSONObject retObj = new JSONObject();
			for (Iterator<String> iter = insertObj.keySet().iterator();iter.hasNext();){
				String dbId = iter.next();
				List<JSONObject> poiList = (List<JSONObject>) insertObj.get(dbId);
				List<IxPoi> poiRetList = new ArrayList<IxPoi>();
				for (int i=0;i<poiList.size();i++) {
					// POI主表
					JSONObject jo = poiList.get(i);
					IxPoi poi = new IxPoi();
					int pid = PidService.getInstance().applyPoiPid();
					poi.setPid(pid);
					poi.setKindCode(jo.getString("kindCode"));
					// geometry按SDO_GEOMETRY格式原值转出
					Geometry geometry = new WKTReader().read(jo.getString("geometry"));
					poi.setGeometry(geometry);
					poi.setxGuide(jo.getJSONObject("guide").getInt("longitude"));
					poi.setyGuide(jo.getJSONObject("guide").getInt("latitude"));
					poi.setLinkPid(jo.getJSONObject("guide").getInt("linkPid"));
					poi.setChain(jo.getString("chain"));
					poi.setOpen24h(jo.getInt("open24H"));
					// meshid非0时原值转出；为0时根据几何计算；
					int meshId = jo.getInt("meshid");
					if (meshId == 0) {
						String[] meshIds = MeshUtils.point2Meshes(geometry.getCoordinate().x, geometry.getCoordinate().y);
						meshId = Integer.parseInt(meshIds[0]);
					poi.setMesh(meshId);
					}
					poi.setPostCode(jo.getString("postCode"));
					//如果KIND_CODE有修改，则追加“改种别代码”；
					//如果CHAIN有修改，则追加“改连锁品牌”；
					//如果IX_POI_HOTEL.RATING有修改,则追加“改酒店星级”；
					String fieldState = "";
					if (jo.getString("kindCode") != "") {
						fieldState += "改种别代码|";
					}
					if (jo.getString("chain") != "") {
						fieldState += "改连锁品牌|";
					}
					if (jo.getString("rating") != "") {
						fieldState += "改酒店星级|";
					}
					if (fieldState.length()>0) {
						fieldState = fieldState.substring(0, fieldState.length()-1);
					}
					poi.setFieldState(fieldState);
					poi.setOldName(jo.getString("name"));
					poi.setOldAddress(jo.getString("address"));
					poi.setOldKind(jo.getString("kindCode"));
					poi.setPoiNum(jo.getString("fid"));
					poi.setDataVersion(version);
					poi.setCollectTime(jo.getString("t_operateDate"));
					poi.setLevel(jo.getString("level"));
					String outDoorLog = "";
					if (poi.getOldName() != "") {
						outDoorLog += "改名称|";
					}
					if (poi.getOldAddress() != "") {
						outDoorLog += "改地址|";
					}
					if (poi.getOldKind() != "") {
						outDoorLog += "改分类|";
					}
					if (poi.getLevel() != "") {
						outDoorLog += "改POI_LEVEL|";
					}
					if (!poi.getGeometry().isEmpty()) {
						outDoorLog += "改RELATION|";
					}
					if (outDoorLog.length()>0) {
						outDoorLog = outDoorLog.substring(0,outDoorLog.length()-1);
					}
					poi.setLog(outDoorLog);
					poi.setSportsVenue(jo.getString("sportsVenues"));
					poi.setIndoor(jo.getJSONObject("indoor").getInt("type"));
					poi.setVipFlag(jo.getString("vipFlag"));
					poi.setuRecord(1);
					Date sysDate = new Date();
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
					String dateStr = df.format(sysDate);
					poi.setuDate(dateStr);
					UuidUtils uuid = new UuidUtils();
					poi.setRowId(uuid.genUuid());
					
					// 名称
					if (poi.getOldName() != "") {
						List<IRow> nameList = new ArrayList<IRow>();
						IxPoiName poiName = new IxPoiName();
						int nameId = PidService.getInstance().applyPoiNameId();
						poiName.setPid(nameId);
						poiName.setPoiPid(pid);
						poiName.setNameGroupid(1);
						poiName.setLangCode("CHI");
						poiName.setNameClass(1);
						poiName.setNameType(2);
						poiName.setName(poi.getOldName());
						poiName.setuRecord(1);
						poiName.setuDate(dateStr);
						poiName.setRowId(uuid.genUuid());
						nameList.add(poiName);
						poi.setNames(nameList);
					}
					
					// 地址
					if (poi.getOldAddress() != "") {
						List<IRow> addressList = new ArrayList<IRow>();
						IxPoiAddress poiAddress = new IxPoiAddress();
						int addressId = PidService.getInstance().applyPoiAddressId();
						poiAddress.setPid(addressId);
						poiAddress.setPoiPid(pid);
						poiAddress.setNameGroupid(1);
						poiAddress.setLangCode("CHI");
						poiAddress.setFullname(poi.getOldAddress());
						poiAddress.setuRecord(1);
						poiAddress.setuDate(dateStr);
						poiAddress.setRowId(uuid.genUuid());
						addressList.add(poiAddress);
						poi.setAddresses(addressList);
					}
					
					// 联系方式
					if (jo.getJSONArray("contacts").size()>0) {
						List<JSONObject> contactsList = jo.getJSONArray("contacts");
						List<IRow> contacts = new ArrayList<IRow>();
						for (JSONObject contactObj:contactsList) {
							IxPoiContact contact = new IxPoiContact();
							contact.setPoiPid(pid);
							contact.setContactType(contactObj.getInt("type"));
							contact.setContact(contactObj.getString("number"));
							String linkman = contactObj.getString("linkman");
							String linkmanNum = "";
							if (linkman.indexOf("总机")>=0){
								linkmanNum = "1" + linkmanNum ;
							} else {
								linkmanNum = "0" + linkmanNum ;
							}
							if (linkman.indexOf("客服")>=0){
								linkmanNum = "1" + linkmanNum ;
							} else {
								linkmanNum = "0" + linkmanNum ;
							}
							if (linkman.indexOf("预订")>=0){
								linkmanNum = "1" + linkmanNum ;
							} else {
								linkmanNum = "0" + linkmanNum ;
							}
							if (linkman.indexOf("销售")>=0){
								linkmanNum = "1" + linkmanNum ;
							} else {
								linkmanNum = "0" + linkmanNum ;
							}
							if (linkman.indexOf("维修")>=0){
								linkmanNum = "1" + linkmanNum ;
							} else {
								linkmanNum = "0" + linkmanNum ;
							}
							if (linkman.indexOf("其他")>=0){
								linkmanNum = "1" + linkmanNum ;
							} else {
								linkmanNum = "0" + linkmanNum ;
							}
							int contactInt = Integer.parseInt(linkmanNum, 2);
							contact.setContactDepart(contactInt);
							contact.setPriority(contactObj.getInt("priority"));
							contact.setuRecord(1);
							contact.setuDate(dateStr);
							contact.setRowId(contactObj.getString("rowId"));
							contacts.add(contact);
						}
						poi.setContacts(contacts);
					}
					
					// 照片
					List<JSONObject> attachments = jo.getJSONArray("attachments");
					List<String> photoIdList = new ArrayList<String>();
					List<IRow> photoList = new ArrayList<IRow>();
					for (JSONObject photo:attachments) {
						int type = photo.getInt("type");
						if (type==1) {
							String photoId = photo.getString("id");
							if (!photoIdList.contains(photoId)) {
								photoIdList.add(photoId);
								IxPoiPhoto poiPhoto = new IxPoiPhoto();
								poiPhoto.setPoiPid(pid);
								poiPhoto.setPhotoId(photoId);
								// TODO tag
								poiPhoto.setuRecord(1);
								poiPhoto.setuDate(dateStr);
								poiPhoto.setRowId(photoId);
								photoList.add(poiPhoto);
							}
						}
					}
					if (photoList.size()>0) {
						poi.setPhotos(photoList);
					}
					
					// 父子关系
					int groupId = PidService.getInstance().applyPoiGroupId();
					if (jo.getJSONArray("relateChildren").size()>0) {
						IxPoiParent parent = new IxPoiParent();
						List<IRow> parentList = new ArrayList<IRow>();
						parent.setGroupId(groupId);
						parent.setParentPoiPid(pid);
						parent.setuRecord(1);
						parent.setuDate(dateStr);
						parent.setRowId(uuid.genUuid());
						parentList.add(parent);
						poi.setParents(parentList);
						
						List<JSONObject> relateChildren = jo.getJSONArray("relateChildren");
						List<IRow> childrenList = new ArrayList<IRow>();
						for (JSONObject children:relateChildren) {
							IxPoiChildren poiChildren = new IxPoiChildren();
							poiChildren.setGroupId(groupId);
							poiChildren.setChildPoiPid(children.getInt("childPid"));
							poiChildren.setRelationType(children.getInt("type"));
							poiChildren.setuRecord(1);
							poiChildren.setuDate(dateStr);
							poiChildren.setRowId(children.getString("rowId"));
							childrenList.add(poiChildren);
						}
						poi.setChildren(childrenList);
					}
					
					// 加油站
					if (jo.getJSONObject("gasStation").size()>0) {
						JSONObject gasObj = jo.getJSONObject("gasStation");
						IxPoiGasstation gasStation = new IxPoiGasstation();
						List<IRow> gasList = new ArrayList<IRow>();
						gasStation.setPid(PidService.getInstance().applyPoiGasstationId());
						gasStation.setPoiPid(pid);
						gasStation.setServiceProv(gasObj.getString("servicePro"));
						gasStation.setFuelType(gasObj.getString("fuelType"));
						gasStation.setOilType(gasObj.getString("oilType"));
						gasStation.setEgType(gasObj.getString("egType"));
						gasStation.setMgType(gasObj.getString("mgType"));
						gasStation.setPayment(gasObj.getString("payment"));
						gasStation.setService(gasObj.getString("service"));
						gasStation.setOpenHour(gasObj.getString("openHour"));
						gasStation.setuRecord(1);
						gasStation.setuDate(dateStr);
						gasStation.setRowId(gasObj.getString("rowId"));
						gasList.add(gasStation);
						poi.setGasstations(gasList);
					}
					
					// 停车场
					if (jo.getJSONObject("parkings").size()>0) {
						JSONObject parkingsObj = jo.getJSONObject("parkings");
						IxPoiParking parkings = new IxPoiParking();
						List<IRow> parkingsList = new ArrayList<IRow>();
						parkings.setPid(PidService.getInstance().applyPoiParkingsId());
						parkings.setPoiPid(pid);
						parkings.setParkingType(parkingsObj.getString("buildingType"));
						parkings.setTollStd(parkingsObj.getString("tollStd"));
						parkings.setTollDes(parkingsObj.getString("tollDes"));
						parkings.setTollWay(parkingsObj.getString("tollWay"));
						parkings.setPayment(parkingsObj.getString("payment"));
						parkings.setRemark(parkingsObj.getString("remark"));
						parkings.setOpenTime(parkingsObj.getString("openTime"));
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
						parkings.setuRecord(1);
						parkings.setuDate(dateStr);
						parkings.setRowId(parkingsObj.getString("rowId"));
						parkingsList.add(parkings);
						poi.setParkings(parkingsList);
					}
					
					// 酒店
					if (jo.getJSONObject("hotel").size()>0) {
						JSONObject hotelObj = jo.getJSONObject("hotel");
						IxPoiHotel hotel = new IxPoiHotel();
						List<IRow> hotelList = new ArrayList<IRow>();
						hotel.setPid(PidService.getInstance().applyPoiHotelId());
						hotel.setPoiPid(pid);
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
						hotel.setuRecord(1);
						hotel.setuDate(dateStr);
						hotel.setRowId(hotelObj.getString("rowId"));
						hotelList.add(hotel);
						poi.setHotels(hotelList);
					}
					
					// 餐馆
					if (jo.getJSONObject("foodtypes").size()>0) {
						JSONObject foodtypesObj = jo.getJSONObject("foodtypes");
						IxPoiRestaurant foodtypes = new IxPoiRestaurant();
						List<IRow> foodtypesList = new ArrayList<IRow>();
						foodtypes.setPid(PidService.getInstance().applyPoiFoodId());
						foodtypes.setPoiPid(pid);
						foodtypes.setFoodType(foodtypesObj.getString("foodtype"));
						foodtypes.setCreditCard(foodtypesObj.getString("creditCards"));
						foodtypes.setAvgCost(foodtypesObj.getInt("avgCost"));
						foodtypes.setParking(foodtypesObj.getInt("parking"));
						foodtypes.setOpenHour(foodtypesObj.getString("openHour"));
						foodtypes.setuRecord(1);
						foodtypes.setuDate(dateStr);
						foodtypes.setRowId(foodtypesObj.getString("rowId"));
						foodtypesList.add(foodtypes);
						poi.setRestaurants(foodtypesList);
					}
					
					// TODO IX_POI_OPERATE_REF
					
					poiRetList.add(poi);
				}
				retObj.put(dbId, poiRetList);
			}
			
		} catch (Exception e) {
			
		}
		return null;
	}
	
	// 处理更新数据
	private JSONObject updateData(JSONObject updateObj,String version){
		return null;
	}
	
	// 处理删除数据
	private JSONObject deleteData(JSONObject deleteObj,String version){
		return null;
	}
}
