package com.navinfo.dataservice.control.app.download;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.bizcommons.sys.SysLogConstant;
import com.navinfo.dataservice.bizcommons.sys.SysLogOperator;
import com.navinfo.dataservice.bizcommons.sys.SysLogStats;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
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
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiFlagMethod;
import com.navinfo.dataservice.dao.glm.search.batch.PoiGridIncreSearch;
import com.navinfo.dataservice.engine.editplus.operation.imp.ErrorLog;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

public class PoiDownloadOperation {
	private static final Logger logger = Logger.getLogger(PoiDownloadOperation.class);
	//*******2017.08.16 zl *****
	//
	private List<ErrorLog> errorLogs = null;
	
	/**
	 * 
	 * @param grids
	 * @return url
	 * @throws Exception
	 */
	public String generateZip(Map<String,String> gridDateMap,int subtaskId,long userId) throws Exception{
		
		try{
			errorLogs = new ArrayList<ErrorLog>();
			Date startTime = new Date();
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
			//初始化存储图片属性的map
			Map<String, Object> logMap=new HashMap<String, Object>();
			logMap.put("beginTime", DateUtils.getSysDateFormat());
			
			logger.info("export ix_poi to poi.txt--->start");
			export2Txt(gridDateMap, filePath, "poi.txt",logMap);
			logger.info("export ix_poi to poi.txt--->end");
			if(subtaskId > 0){
				logger.info("export pid to --->start");
				export2TxtBySubtaskId(filePath, subtaskId);
				logger.info("export pid--->end");
			}
			
			
			String zipFileName = uuid + ".zip";

			String zipFullName = parentPath + zipFileName;

			ZipUtils.zipFile(filePath, zipFullName);
			
			String serverUrl =  SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.serverUrl);
			
			String downloadUrlPath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.downloadUrlPathPoi);

			String url = serverUrl + downloadUrlPath +File.separator+ day + "/"
					+ zipFileName;
			
			Date endTime = new Date();
			logger.info("total time:"+ (endTime.getTime() - startTime.getTime())+"ms");
			
			logMap.put("endTime", DateUtils.getSysDateFormat());
			logMap.put("userId", userId);
			logMap.put("uuid", uuid);
			
			insertStatisticsInfoNoException(subtaskId, userId, logMap);
			return url;
		} catch (Exception e){
			throw e;
		}
	}
	

	/**
	 * @Title: insertStatisticsInfoNoException
	 * @Description: 记录poi下载日志
	 * @param subtaskId
	 * @param userId
	 * @param logMap  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年8月15日 下午3:05:07 
	 */
	private void insertStatisticsInfoNoException( int subtaskId,
			long userId, Map<String, Object> logMap)  {
		try{
			
			SysLogStats log = new SysLogStats();
			log.setLogType(SysLogConstant.POI_DOWNLOAD_TYPE);
			log.setLogDesc(SysLogConstant.POI_DOWNLOAD_DESC+",jobId :0,subtaskId:"+subtaskId+",downloadTime:"+logMap.get("endTime")+",uuid:"+logMap.get("uuid"));
			log.setFailureTotal(((int) logMap.get("total"))-((int) logMap.get("success")));
			log.setSuccessTotal((int) logMap.get("success"));  
			log.setTotal((int) logMap.get("total"));
			log.setBeginTime((String) logMap.get("beginTime"));
			log.setEndTime(DateUtils.getSysDateFormat());
			if(errorLogs != null && errorLogs.size() > 0 ){
				JSONArray jsonArrFail = JSONArray.fromObject(errorLogs);
				log.setErrorMsg(jsonArrFail.toString());
			}else{
				log.setErrorMsg("");
			}
			log.setUserId(String.valueOf(userId));
			
			SysLogOperator.getInstance().insertSysLog(log);
		
		}catch (Exception e) {
			logger.error("记录poi下载日志出错："+e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * @param grids
	 * @param folderName
	 * @param fileName
	 * @param errorLogs 
	 * @throws Exception
	 */
	public void export2Txt(Map<String,String> gridDateMap,  String folderName,
			String fileName,Map<String, Object> logMap) throws Exception {
		if (!folderName.endsWith("/")) {
			folderName += "/";
		}

		fileName = folderName + fileName;

		PrintWriter pw = new PrintWriter(fileName);
		try {
			logger.info("starting load data...");
			Collection<IxPoi> data = new PoiGridIncreSearch().getPoiByGrids(gridDateMap);
			logger.info("data total:"+data.size());
			logger.info("starting convert data...");
			JSONArray ja = changeData(data);
			logger.info("begin write json to file");
			String encoding = System.getProperty("file.encoding");
			logger.info("系统编码encoding:"+encoding);
			for (int j = 0; j < ja.size(); j++) {
				pw.println(ja.getJSONObject(j).toString());
			}
			logger.info("file write ok");
			logMap.put("total", data.size());
			logMap.put("success", ja.size());
		} catch (Exception e) {
			errorLogs.add(new ErrorLog("", "poi 下载报错:"+e.getMessage()));
			logger.error("poi 下载报错:"+e.getMessage());
//			throw e;
		} finally {
			pw.close();

		}
	}
	
	public void export2TxtBySubtaskId(String folderName, int subtaskId) throws Exception {
		if (!folderName.endsWith("/")) {
			folderName += "/";
		}
		logger.info("subtaskId : "+subtaskId);
		PoiGridIncreSearch seach = new PoiGridIncreSearch();
		int taskId = 0;
		int dbid = 0;
		Map<String,Integer> map = seach.getRegionIdTaskIdBySubtaskId(subtaskId);
		String fileName = "";
		if(map != null && map.size() > 0 ){
			fileName = map.get("regionId")+"_"+map.get("taskId")+"_"+subtaskId+".txt";
			fileName = folderName + fileName;
			logger.info("fileName : "+fileName);
			if(map.containsKey("taskId")){taskId = map.get("taskId");}
			if(map.containsKey("dayDbId")){dbid = map.get("dayDbId");}
			
			PrintWriter pw = new PrintWriter(fileName);
			try {
				if(taskId > 0 && dbid > 0){
					
				logger.info("starting load data...");
				Set<Integer> data = new PoiGridIncreSearch().getPidsByTaskId(taskId,dbid);
				logger.info("data total:"+data.size());
				logger.info("begin write json to file");
				for (int pid : data) {
					JSONObject jo = new JSONObject();
					jo.put("pid", pid);
					pw.println(jo.toString());
				}
				}
				logger.info("file write ok");
			} catch (Exception e) {
//				throw e;
				logger.error("生成"+fileName+"文件错误:"+e.getMessage());
				errorLogs.add(new ErrorLog("", "生成"+fileName+"文件错误:"+e.getMessage()));
			} finally {
				pw.close();

			}
		}
		
	}
	
	/**
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public JSONArray changeData(Collection<IxPoi> data) throws Exception{
		JSONArray retList = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		
		for (IxPoi poi:data){
			
			jsonObj.put("fid", poi.getPoiNum());
			
			IxPoiName poiName = new IxPoiName();
			List<IRow> poiNameList = poi.getNames();
			if (poiNameList.size()>0) {
				poiName = (IxPoiName)poi.getNames().get(0);
			}
			if (poiName.getName() == null) {
				jsonObj.put("name", "");
			} else {
				jsonObj.put("name", poiName.getName());
			}
			
			jsonObj.put("pid", poi.getPid());
			
			jsonObj.put("meshid", poi.getMeshId());
			
			if (poi.getKindCode() == null) {
				jsonObj.put("kindCode", "");
			} else {
				jsonObj.put("kindCode", poi.getKindCode());
			}
			
			if  (poi.getLinkPid()==0 && poi.getxGuide()==0 && poi.getyGuide()==0) {
				jsonObj.put("guide", JSONNull.getInstance());
			} else {
				JSONObject guide = new JSONObject();
				guide.put("linkPid", poi.getLinkPid());
				guide.put("longitude", poi.getxGuide());
				guide.put("latitude", poi.getyGuide());
				jsonObj.put("guide", guide);
			}
			
			IxPoiAddress address = new IxPoiAddress();
			List<IRow> addressList = poi.getAddresses();
			if (addressList.size()>0) {
				address = (IxPoiAddress)poi.getAddresses().get(0);
			}
			if (address.getFullname() == null) {
				jsonObj.put("address", "");
			} else {
				jsonObj.put("address", address.getFullname());
			}
			
			if (poi.getPostCode() == null) {
				jsonObj.put("postCode", "");
			} else {
				jsonObj.put("postCode", poi.getPostCode());
			}
			
			JSONObject indoor = new JSONObject();
			if (poi.getIndoor() == 1) {
				indoor.put("type", 3);
			} else {
				indoor.put("type", poi.getIndoor());
			}
			
			if (address.getFloor() == null) {
				indoor.put("floor", "");
			} else {
				indoor.put("floor", address.getFloor());
			}
			
			jsonObj.put("indoor", indoor);
			
			int open24H = poi.getOpen24h();
			if (open24H!=1) {
				open24H = 2;
			}
			jsonObj.put("open24H", open24H);
			
			if (poi.getLevel() == null) {
				jsonObj.put("level", "");
			} else {
				jsonObj.put("level", poi.getLevel());
			}
			
			if (poi.getSportsVenue() == null) {
				jsonObj.put("sportsVenues", "");
			} else {
				jsonObj.put("sportsVenues", poi.getSportsVenue());
			}
			
			if (poi.getVipFlag() == null) {
				jsonObj.put("vipFlag", "");
			} else {
				jsonObj.put("vipFlag", poi.getVipFlag());
			}
			
			// 增加卡车字段下载20161012
			jsonObj.put("truck", poi.getTruckFlag());
			
			IxPoiParentForAndroid parent = new IxPoiParentForAndroid();
			List<IRow> parentList = poi.getParents();
			if (parentList.size()>0) {
				parent = (IxPoiParentForAndroid)poi.getParents().get(0);
			}
			if (parent.getPoiNum() == null) {
				jsonObj.put("parentFid", "");
			} else {
				jsonObj.put("parentFid", parent.getPoiNum());
			}
			
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
				if (poiContact.getContact() == null) {
					contact.put("number", "");
				} else {
					contact.put("number", poiContact.getContact());
				}
				
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
				if (restaurant.getFoodType() == null) {
					foodtype.put("foodtype", "");
				} else {
					foodtype.put("foodtype", restaurant.getFoodType());
				}
				
				if (restaurant.getCreditCard() == null) {
					foodtype.put("creditCards", "");
				} else {
					foodtype.put("creditCards", restaurant.getCreditCard());
				}
				
				foodtype.put("parking", restaurant.getParking());
				
				if (restaurant.getOpenHour() == null) {
					foodtype.put("openHour", "");
				} else {
					foodtype.put("openHour", restaurant.getOpenHour());
				}
				
				foodtype.put("avgCost", restaurant.getAvgCost());
				foodtype.put("rowId", restaurant.getRowId());
				jsonObj.put("foodtypes", foodtype);
			} else {
				jsonObj.put("foodtypes", JSONNull.getInstance());
			}
			
			List<IRow> parkingsList = poi.getParkings();
			if (parkingsList.size()>0) {
				IxPoiParking parking = (IxPoiParking) parkingsList.get(0);
				JSONObject parkings = new JSONObject();
				if (parking.getTollStd() == null) {
					parkings.put("tollStd", "");
				} else {
					parkings.put("tollStd", parking.getTollStd());
				}
				
				if (parking.getTollDes() == null) {
					parkings.put("tollDes", "");
				} else {
					parkings.put("tollDes", parking.getTollDes());
				}
				
				if (parking.getTollWay() == null) {
					parkings.put("tollWay", "");
				} else {
					parkings.put("tollWay", parking.getTollWay());
				}
				if (parking.getOpenTiime() == null) {
					parkings.put("openTime", "");
				} else {
					parkings.put("openTime", parking.getOpenTiime());
				}
				
				parkings.put("totalNum", parking.getTotalNum());
				
				if (parking.getPayment() == null) {
					parkings.put("payment", "");
				} else {
					parkings.put("payment", parking.getPayment());
				}
				
				if (parking.getRemark() == null) {
					parkings.put("remark", "");
				} else {
					parkings.put("remark", parking.getRemark());
				}
				
				if (parking.getParkingType() == null) {
					parkings.put("buildingType", "");
				} else {
					parkings.put("buildingType", parking.getParkingType());
				}
				
				parkings.put("resHigh", parking.getResHigh());
				parkings.put("resWidth", parking.getResWidth());
				parkings.put("resWeigh", parking.getResWeigh());
				parkings.put("certificate", parking.getCertificate());
				parkings.put("vehicle", parking.getVehicle());
				
				if (parking.getHaveSpecialplace() == null) {
					parkings.put("haveSpecialPlace", "");
				} else {
					parkings.put("haveSpecialPlace", parking.getHaveSpecialplace());
				}
				
				parkings.put("womenNum", parking.getWomenNum());
				parkings.put("handicapNum", parking.getHandicapNum());
				parkings.put("miniNum", parking.getMiniNum());
				parkings.put("vipNum", parking.getVipNum());
				parkings.put("rowId", parking.getRowId());
				jsonObj.put("parkings", parkings);
			} else {
				jsonObj.put("parkings", JSONNull.getInstance());
			}
			
			List<IRow> hotelList = poi.getHotels();
			if (hotelList.size()>0) {
				IxPoiHotel hotel = (IxPoiHotel) hotelList.get(0);
				JSONObject hotelObj = new JSONObject();
				hotelObj.put("rating", hotel.getRating());
				
				if (hotel.getCreditCard() == null) {
					hotelObj.put("creditCards", "");
				} else {
					hotelObj.put("creditCards", hotel.getCreditCard());
				}
				
				if (hotel.getLongDescription() == null) {
					hotelObj.put("description", "");
				} else {
					hotelObj.put("description", hotel.getLongDescription());
				}
				
				if (hotel.getCheckinTime() == null) {
					hotelObj.put("checkInTime", "");
				} else {
					hotelObj.put("checkInTime", hotel.getCheckinTime());
				}
				
				if (hotel.getCheckoutTime() == null) {
					hotelObj.put("checkOutTime", "");
				} else {
					hotelObj.put("checkOutTime", hotel.getCheckoutTime());
				}
				
				hotelObj.put("roomCount", hotel.getRoomCount());
				
				if (hotel.getRoomType() == null) {
					hotelObj.put("roomType", "");
				} else {
					hotelObj.put("roomType", hotel.getRoomType());
				}
				
				if (hotel.getRoomPrice() == null) {
					hotelObj.put("roomPrice", "");
				} else {
					hotelObj.put("roomPrice", hotel.getRoomPrice());
				}
				
				hotelObj.put("breakfast", hotel.getBreakfast());
				
				if (hotel.getService() == null) {
					hotelObj.put("service", "");
				} else {
					hotelObj.put("service", hotel.getService());
				}
				
				hotelObj.put("parking", hotel.getParking());
				
				if (hotel.getOpenHour() == null) {
					hotelObj.put("openHour", "");
				} else {
					hotelObj.put("openHour", hotel.getOpenHour());
				}
				
				hotelObj.put("rowId", hotel.getRowId());
				jsonObj.put("hotel", hotelObj);
			} else {
				jsonObj.put("hotel", JSONNull.getInstance());
			}
			
			List<IRow> chargingStationList = poi.getChargingstations();
			if (chargingStationList.size()>0) {
				IxPoiChargingStation chargingStation = (IxPoiChargingStation) chargingStationList.get(0);
				JSONObject chargingStationObj = new JSONObject();
				chargingStationObj.put("type", chargingStation.getChargingType());
				chargingStationObj.put("changeBrands", chargingStation.getChangeBrands());
				chargingStationObj.put("changeOpenType", chargingStation.getChangeOpenType());
				chargingStationObj.put("servicePro", chargingStation.getServiceProv());
				chargingStationObj.put("chargingNum", chargingStation.getChargingNum());
				chargingStationObj.put("openHour", chargingStation.getOpenHour());
				chargingStationObj.put("parkingFees", chargingStation.getParkingFees());
				chargingStationObj.put("parkingInfo", chargingStation.getParkingInfo());
				chargingStationObj.put("availableState", chargingStation.getAvailableState());
				chargingStationObj.put("rowId", chargingStation.getRowId());
				jsonObj.put("chargingStation", chargingStationObj);
			} else {
				jsonObj.put("chargingStation", JSONNull.getInstance());
			}
			
			List<IRow> chargingPole = poi.getChargingplots();
			if (chargingPole.size()>0) {
				JSONArray chargingPoleArray = new JSONArray();
				for (IRow charging:chargingPole) {
					IxPoiChargingPlot ixPoiChargingplot = (IxPoiChargingPlot) charging;
					JSONObject chargingPoleObj = new JSONObject();
					chargingPoleObj.put("groupId", ixPoiChargingplot.getGroupId());
					chargingPoleObj.put("acdc", ixPoiChargingplot.getAcdc());
					chargingPoleObj.put("plugType", ixPoiChargingplot.getPlugType());
					chargingPoleObj.put("power", ixPoiChargingplot.getPower());
					chargingPoleObj.put("voltage", ixPoiChargingplot.getVoltage());
					chargingPoleObj.put("current", ixPoiChargingplot.getCurrent());
					chargingPoleObj.put("mode", ixPoiChargingplot.getMode());
					chargingPoleObj.put("count", ixPoiChargingplot.getCount());
					chargingPoleObj.put("plugNum", ixPoiChargingplot.getPlugNum());
					chargingPoleObj.put("prices", ixPoiChargingplot.getPrices());
					chargingPoleObj.put("openType", ixPoiChargingplot.getOpenType());
					chargingPoleObj.put("availableState", ixPoiChargingplot.getAvailableState());
					chargingPoleObj.put("manufacturer", ixPoiChargingplot.getManufacturer());
					chargingPoleObj.put("factoryNum", ixPoiChargingplot.getFactoryNum());
					chargingPoleObj.put("plotNum", ixPoiChargingplot.getPlotNum());
					chargingPoleObj.put("productNum", ixPoiChargingplot.getProductNum());
					chargingPoleObj.put("parkingNum", ixPoiChargingplot.getParkingNum());
					chargingPoleObj.put("floor", ixPoiChargingplot.getFloor());
					chargingPoleObj.put("locationType", ixPoiChargingplot.getLocationType());
					chargingPoleObj.put("payment", ixPoiChargingplot.getPayment());
					chargingPoleObj.put("rowId", ixPoiChargingplot.getRowId());
					chargingPoleArray.add(chargingPoleObj);
				}
				jsonObj.put("chargingPole", chargingPoleArray);
			} else {
				jsonObj.put("chargingPole", new ArrayList<Object>());
			}
			
			List<IRow> gasStationList = poi.getGasstations();
			if (gasStationList.size()>0) {
				IxPoiGasstation gas = (IxPoiGasstation) gasStationList.get(0);
				JSONObject gasStation = new JSONObject();
				if (gas.getFuelType() == null) {
					gasStation.put("fuelType", "");
				} else {
					gasStation.put("fuelType", gas.getFuelType());
				}
				
				if (gas.getOilType() == null) {
					gasStation.put("oilType", "");
				} else {
					gasStation.put("oilType", gas.getOilType());
				}
				
				if (gas.getEgType() == null) {
					gasStation.put("egType", "");
				} else {
					gasStation.put("egType", gas.getEgType());
				}
				
				if (gas.getMgType() == null) {
					gasStation.put("mgType", "");
				} else {
					gasStation.put("mgType", gas.getMgType());
				}
				
				if (gas.getPayment() == null) {
					gasStation.put("payment", "");
				} else {
					gasStation.put("payment", gas.getPayment());
				}
				
				if (gas.getService() == null) {
					gasStation.put("service", "");
				} else {
					gasStation.put("service", gas.getService());
				}
				
				if (gas.getServiceProv() == null) {
					gasStation.put("servicePro", "");
				} else {
					gasStation.put("servicePro", gas.getServiceProv());
				}
				
				if (gas.getOpenHour() == null) {
					gasStation.put("openHour", "");
				} else {
					gasStation.put("openHour", gas.getOpenHour());
				}
				
				gasStation.put("rowId", gas.getRowId());
				jsonObj.put("gasStation", gasStation);
			} else {
				jsonObj.put("gasStation", JSONNull.getInstance());
			}
			
			jsonObj.put("attachments", new ArrayList<Object>());
			if (poi.getChain() == null) {
				jsonObj.put("chain", "");
			} else {
				jsonObj.put("chain", poi.getChain());
			}
			
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
			
			
			//************zl 2016.12.05*****************************
			//处理统一关系 获取  samefid
			jsonObj.put("sameFid", poi.getSameFid());
			//处理编辑状态  获取  poiEditStatus
			jsonObj.put("commitHisStatus", poi.getPoiEditStatus());
				
			//*****************************************
			
			jsonObj.put("evaluPlan", poi.getEvaluPlan());
			
			IxPoiFlagMethod poiFlag = new IxPoiFlagMethod();
			List<IRow> poiFlagList = poi.getIxPoiFlagMethod();
			if (poiFlagList.size()>0) {
				poiFlag = (IxPoiFlagMethod)poiFlagList.get(0);
			}
			jsonObj.put("srcRecord",poiFlag.getSrcRecord());
			jsonObj.put("fdVerified",poiFlag.getFieldVerified());
			
			
			
			
			retList.add(jsonObj);
		}
		return retList;
	}

}
