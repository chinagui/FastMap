package com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.download;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildrenForAndroid;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParentForAndroid;
import com.navinfo.dataservice.dao.glm.search.PoiGridSearch;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DownloadOperation {
	
	/**
	 * 
	 * @param gridDateList
	 * @return url
	 * @throws Exception
	 */
	public String getPoiUrl(JSONArray gridDateList) throws Exception{
		try{
			String day = StringUtils.getCurrentDay();

			String uuid = UuidUtils.genUuid();
			
			String downloadFilePath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.downloadFilePathPoi);

			String parentPath = downloadFilePath +File.separator+ day + "/";

			String filePath = parentPath + uuid + "/";

			File file = new File(filePath);
			
			if (!file.exists()) {
				file.mkdirs();
			}
			
			export(gridDateList, filePath, "poi.txt");
			
			String zipFileName = uuid + ".zip";

			String zipFullName = parentPath + zipFileName;

			ZipUtils.zipFile(filePath, zipFullName);
			
			String serverUrl =  SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.serverUrl);
			
			String downloadUrlPath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.downloadUrlPathPoi);

			String url = serverUrl + downloadUrlPath +File.separator+ day + "/"
					+ zipFileName;
			
			return url;
		} catch (Exception e){
			throw e;
		}
	}
	
	/**
	 * 
	 * @param gridDateList
	 * @param folderName
	 * @param fileName
	 * @throws Exception
	 */
	public void export(JSONArray gridDateList,  String folderName,
			String fileName) throws Exception {
		if (!folderName.endsWith("/")) {
			folderName += "/";
		}

		fileName = folderName + fileName;

		PrintWriter pw = new PrintWriter(fileName);
		try {
			
			List<IRow> data = new PoiGridSearch().getPoiByGrids(gridDateList);

			JSONArray ja = changeData(data);

			for (int j = 0; j < ja.size(); j++) {
				pw.println(ja.getJSONObject(j).toString());
			}
		} catch (Exception e) {
			throw e;
		} finally {
			pw.close();

		}
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public JSONArray changeData(List<IRow> data) throws Exception{
		JSONArray retList = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		for (IRow row:data){
			IxPoi poi = (IxPoi) row;
			
			jsonObj.put("fid", poi.getPoiNum());
			IxPoiName poiName = (IxPoiName)poi.getNames().get(0);
			jsonObj.put("name", poiName.getName());
			jsonObj.put("pid", poi.getPid());
			jsonObj.put("meshid", poi.getMeshId());
			jsonObj.put("kindCode", poi.getKindCode());
			
			JSONObject guide = new JSONObject();
			if  (poi.getLinkPid()==0 && poi.getxGuide()==0 && poi.getyGuide()==0) {
				guide = null;
			} else {
				guide.put("linkPid", poi.getLinkPid());
				guide.put("longitude", poi.getxGuide());
				guide.put("latitude", poi.getyGuide());
			}
			jsonObj.put("guide", guide);
			
			IxPoiAddress address = (IxPoiAddress)poi.getAddresses().get(0);
			jsonObj.put("address", address.getFullname());
			jsonObj.put("postCode", poi.getPostCode());
			
			JSONObject indoor = new JSONObject();
			indoor.put("type", poi.getIndoor());
			indoor.put("floor", address.getFloor());
			jsonObj.put("indoor", indoor);
			
			int open24H = poi.getOpen24h();
			if (open24H!=1) {
				open24H = 2;
			}
			jsonObj.put("open24H", open24H);
			
			jsonObj.put("level", poi.getLevel());
			jsonObj.put("sportsVenues", poi.getSportsVenue());
			jsonObj.put("vipFlag", poi.getVipFlag());
			
			IxPoiParentForAndroid parent = (IxPoiParentForAndroid)poi.getParents().get(0);
			jsonObj.put("parentFid", parent.getPoiNum());
			
			List<IRow> childrenList = poi.getChildren();
			List<JSONObject> childrenArray = new ArrayList<JSONObject>();
			for (IRow children:childrenList) {
				JSONObject child = new JSONObject();
				IxPoiChildrenForAndroid poiChild = (IxPoiChildrenForAndroid)children;
				child.put("type", poiChild.getRelationType());
				child.put("childPid", poiChild.getChildPoiPid());
				child.put("childFid", poiChild.getPoiNum());
				child.put("rowId", poiChild.getRowId());
				childrenArray.add(child);
			}
			jsonObj.put("relateChildren", childrenArray);
			
			List<IRow> contactsList = poi.getContacts();
			List<JSONObject> contactsArray = new ArrayList<JSONObject>();
			for (IRow contacts:contactsList) {
				JSONObject contact = new JSONObject();
				IxPoiContact poiContact = (IxPoiContact) contacts;
				contact.put("number", poiContact.getContact());
				contact.put("type", poiContact.getContactType());
				int linkman = poiContact.getContactDepart();
				if (linkman == 0) {
					contact.put("linkman", "");
				} else {
					String linkmanStr = Integer.toBinaryString(linkman);
					String retStr = "";
					for (int i=0;i<linkmanStr.length();i++) {
						char linkmanChar = linkmanStr.charAt(linkmanStr.length()-1-i);
						if  (linkmanChar == '1') {
							switch (i) {
							case 0:
								retStr += "总机-";
								break;
							case 1:
								retStr += "客服-";
								break;
							case 2:
								retStr += "预订-";
								break;
							case 3:
								retStr += "销售-";
								break;
							case 4:
								retStr += "维修-";
								break;
							case 5:
								retStr += "其他-";
								break;
							}
						}
					}
					retStr = retStr.substring(0, retStr.length()-1);
					contact.put("linkman", retStr);
				}
				contact.put("priority", poiContact.getPriority());
				contact.put("rowId", poiContact.getRowId());
				contactsArray.add(contact);
			}
			jsonObj.put("contacts", contactsArray);
			
			List<IRow> restaurantList = poi.getRestaurants();
			if (restaurantList.size()>0) {
				IxPoiRestaurant restaurant = (IxPoiRestaurant) restaurantList.get(0);
				JSONObject foodtype = new JSONObject();
				foodtype.put("foodtype", restaurant.getFoodType());
				foodtype.put("creditCards", restaurant.getCreditCard());
				foodtype.put("parking", restaurant.getParking());
				foodtype.put("openHour", restaurant.getOpenHour());
				foodtype.put("avgCost", restaurant.getAvgCost());
				foodtype.put("rowId", restaurant.getRowId());
				jsonObj.put("foodtypes", foodtype);
			} else {
				jsonObj.put("foodtypes", null);
			}
			
			List<IRow> parkingsList = poi.getParkings();
			if (parkingsList.size()>0) {
				IxPoiParking parking = (IxPoiParking) parkingsList.get(0);
				JSONObject parkings = new JSONObject();
				parkings.put("tollStd", parking.getTollStd());
				parkings.put("tollDes", parking.getTollDes());
				parkings.put("tollWay", parking.getTollWay());
				parkings.put("openTime", parking.getOpenTiime());
				parkings.put("totalNum", parking.getTotalNum());
				parkings.put("payment", parking.getPayment());
				parkings.put("remark", parking.getRemark());
				parkings.put("buildingType", parking.getParkingType());
				parkings.put("resHigh", parking.getResHigh());
				parkings.put("resWidth", parking.getResWidth());
				parkings.put("resWeigh", parking.getResWeigh());
				parkings.put("certificate", parking.getCertificate());
				parkings.put("vehicle", parking.getVehicle());
				parkings.put("haveSpecialPlace", parking.getHaveSpecialplace());
				parkings.put("womenNum", parking.getWomenNum());
				parkings.put("handicapNum", parking.getHandicapNum());
				parkings.put("miniNum", parking.getMiniNum());
				parkings.put("vipNum", parking.getVipNum());
				parkings.put("rowId", parking.getRowId());
				jsonObj.put("parkings", parkings);
			} else {
				jsonObj.put("parkings", null);
			}
			
			List<IRow> hotelList = poi.getHotels();
			if (hotelList.size()>0) {
				IxPoiHotel hotel = (IxPoiHotel) hotelList.get(0);
				JSONObject hotelObj = new JSONObject();
				hotelObj.put("rating", hotel.getRating());
				hotelObj.put("creditCards", hotel.getCreditCard());
				hotelObj.put("description", hotel.getLongDescription());
				hotelObj.put("checkInTime", hotel.getCheckinTime());
				hotelObj.put("checkOutTime", hotel.getCheckoutTime());
				hotelObj.put("roomCount", hotel.getRoomCount());
				hotelObj.put("roomType", hotel.getRoomType());
				hotelObj.put("roomPrice", hotel.getRoomPrice());
				hotelObj.put("breakfast", hotel.getBreakfast());
				hotelObj.put("service", hotel.getService());
				hotelObj.put("parking", hotel.getParking());
				hotelObj.put("openHour", hotel.getOpenHour());
				hotelObj.put("rowId", hotel.getRowId());
				jsonObj.put("hotel", hotelObj);
			} else {
				jsonObj.put("hotel", null);
			}
			
			jsonObj.put("chargingStation", null);
			jsonObj.put("chargingPole", new ArrayList<Object>());
			
			List<IRow> gasStationList = poi.getGasstations();
			if (gasStationList.size()>0) {
				IxPoiGasstation gas = (IxPoiGasstation) gasStationList.get(0);
				JSONObject gasStation = new JSONObject();
				gasStation.put("fuelType", gas.getFuelType());
				gasStation.put("oilType", gas.getOilType());
				gasStation.put("egType", gas.getEgType());
				gasStation.put("mgType", gas.getMgType());
				gasStation.put("payment", gas.getPayment());
				gasStation.put("service", gas.getService());
				gasStation.put("servicePro", gas.getServiceProv());
				gasStation.put("openHour", gas.getOpenHour());
				gasStation.put("rowId", gas.getRowId());
				jsonObj.put("gasStation", gasStation);
			} else {
				jsonObj.put("gasStation", null);
			}
			
			jsonObj.put("attachments", new ArrayList<Object>());
			jsonObj.put("chain", poi.getChain());
			jsonObj.put("rawFields", "");
			
			switch (poi.getuRecord()) {
			case 0:
				jsonObj.put("t_lifecycle", 0);
				break;
			case 1:
				jsonObj.put("t_lifecycle", 3);
				break;
			case 2:
				jsonObj.put("t_lifecycle", 1);
				break;
			case 3:
				jsonObj.put("t_lifecycle", 2);
				break;
			}
			
			GeoTranslator trans = new GeoTranslator();
			String geometry = trans.jts2Wkt(poi.getGeometry(),1,5);
			jsonObj.put("geometry", geometry);
			
			jsonObj.put("t_operateDate", "");
			
			retList.add(jsonObj);
		}
		return retList;
	}

}
