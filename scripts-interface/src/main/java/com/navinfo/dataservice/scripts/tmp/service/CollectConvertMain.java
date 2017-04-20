<<<<<<< HEAD
package com.navinfo.dataservice.scripts.tmp.service;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import oracle.net.aso.n;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

public class CollectConvertMain {
	private static Logger log = LogManager.getLogger(CollectConvertMain.class);
	
	/**
	 * json转换模块
	 * 文件：com.navinfo.dataservice.scripts.tmp.service.CollectConvertMain.java
	 * 实现方式：java
	 * 输入：JSONObject 线上格式
	 * 输出：JSONObject 一体化格式
	 * 原则：
	 * 1.json格式转换，具体原则参照excel
	 * 2.照片转换时，可调用照片重命名模块
	 * 3.根据一体化格式geo获取dbid模块
	 * @param dbId 可选参数前端脚本可能会传入，非0则用，否则自行计算
	 * @param inPath data/12/
	 * @param oldPoi
	 * @return
	 * @throws Exception 
	 */
	public static JSONObject convertMain(int dbId, String inPath, JSONObject oldPoi) throws Exception{
		JSONObject newPoi=new JSONObject();
		Connection conn = null;
		try{
			/*
			 * 线上 geometry: "POINT (120.1733200000000039 32.3584900000000033)"
			 * 一体化geometry: "POINT (116.47958 40.01322)",	
			 * 原则：四舍五入截取小数点后5位赋值
			 */
			Geometry oldGeo = new WKTReader().read(oldPoi.getString("geometry"));
			String newGeoStr=GeoTranslator.jts2Wkt(oldGeo,0.00001, 5);
			newPoi.put("geometry", newGeoStr);
			//获取poi所在大区连接
			if(dbId==0){
				dbId=CollectConvertUtils.getDbidByGeo(newGeoStr);}
//			else{
//				log.info("");}
			String fid=oldPoi.getString("fid");
			newPoi.put("fid", fid);
//			if(fid.equals("0010080605XJ300066")||fid.equals("0010130518WMH00081")){
//				log.info("");
//			}
			log.info("采集成果格式转换：fid="+fid+",dbId="+dbId);
			
			conn = DBConnector.getInstance().getConnectionById(dbId);
			/*线上 fid: "0523081102MFY00291",	
			 *一体化fid: "00166420170303213016",	原则：直接赋值
			 *		pid: 0,	原则：根据fid查IX_POI表，若有则，更新pid；否则为0
		     */
			
			IxPoiObj oldPoiObj =null;
			//lifecycle: "2"	t_lifecycle: 3,	字符串转数字后赋值
			//0 无； 1 删除；2 更新；3 新增；
			int lifecycle=CollectConvertUtils.convertInt(oldPoi.getString("lifecycle"));			
			oldPoiObj = loadPoiByFid(conn, fid,false);
			if(oldPoiObj==null){
				if(lifecycle==2){lifecycle=3;}
				else if(lifecycle==1){throw new Exception("数据fid="+fid+"在大区"+dbId+"中不存在:lifecycle="+lifecycle);}
			}else{
				//row.setOpType(OperationType.PRE_DELETED);
				if(!oldPoiObj.getMainrow().getOpType().equals(OperationType.PRE_DELETED)&&lifecycle==3){
					lifecycle=2;
				}
				if(oldPoiObj.getMainrow().getOpType().equals(OperationType.PRE_DELETED)&&(lifecycle==3||lifecycle==2)){
					throw new Exception("数据fid="+fid+"在大区"+dbId+"中存在,且为删除记录:lifecycle="+lifecycle);
				}
			}
			newPoi.put("t_lifecycle", lifecycle);
			convertPid(lifecycle,newPoi,oldPoi,oldPoiObj);
			/*线上meshid: "486041",	一体化meshid: 0,	原则：字符串转数字后赋值*/
			newPoi.put("meshid",CollectConvertUtils.convertInt(oldPoi.getString("meshid")));
			//name: "泰州市顺旺铸造有限公司",	name: "Ｉｉｉｉ",	"null"转成""；否则，直接赋值
			newPoi.put("name", CollectConvertUtils.convertStr(oldPoi.getString("name")));
			//kindCode: "220100",	kindCode: "110101",	直接赋值
			newPoi.put("kindCode", CollectConvertUtils.convertStr(oldPoi.getString("kindCode")));
			//vipFlag: "null",	vipFlag: "",	"null"转成""；否则，直接赋值
			newPoi.put("vipFlag", CollectConvertUtils.convertStr(oldPoi.getString("vipFlag")));
			//truck: "0",	truck: 0,	字符串转数字后赋值
			newPoi.put("truck", CollectConvertUtils.convertInt(oldPoi.getString("truck")));
			/* 线上guide: "{\"latitude\":32.35849986671124,\"longitude\":120.17348080519618,\"linkPid\":0}",	
			 * 一体化guide: {linkPid: 208002607,longitude: 116.47924,latitude: 40.01292}
			 * 原则：guide的字符串转json格式；linkPid，直接赋值；longitude，latitude四舍五入截取小数点后5位赋值
			 */
			JSONObject oldGuide = JSONObject.fromObject(oldPoi.getString("guide"));
			JSONObject newGuide=new JSONObject();
			newGuide.put("linkPid", oldGuide.getLong("linkPid"));
			newGuide.put("longitude", CollectConvertUtils.convertDouble(oldGuide.getDouble("longitude"),5));
			newGuide.put("latitude", CollectConvertUtils.convertDouble(oldGuide.getDouble("latitude"),5));
			newPoi.put("guide", newGuide);
			//address: "null",	address: "",	"null"转成""；否则，直接赋值
			newPoi.put("address", CollectConvertUtils.convertStr(oldPoi.getString("address")));
			//postCode: "null",	postCode: "",	"null"转成""；否则，直接赋值
			newPoi.put("postCode", CollectConvertUtils.convertStr(oldPoi.getString("postCode")));
			//level: "B3",	level: "B3",	"null"转成""；否则，直接赋值
			newPoi.put("level", CollectConvertUtils.convertStr(oldPoi.getString("level")));
			//open24H: "2",	open24H: 0,	字符串转数字后赋值
			newPoi.put("open24H", CollectConvertUtils.convertInt(oldPoi.getString("open24H")));
			/* 线上relateParent: "null",（或者"relateParent":"{\"parentFid\":\"00366620161022153845\",\"parentRowkey\":null}"）
			 * 一体化parentFid: "",（或者"parentFid":"00000220170306135753"）	
			 * 原则："null"转成""；relateParent的字符串转json格式；取parentFid直接赋值
			 */
			String oldParentStr=CollectConvertUtils.convertStr(oldPoi.getString("relateParent"));
			if(oldParentStr.isEmpty()){
				newPoi.put("parentFid", "");
			}else{
				newPoi.put("parentFid", JSONObject.fromObject(oldParentStr).getString("parentFid"));
			}
			/* 线上：relateChildren: "[]",（或者"relateChildren":"[{\"childFid\":\"00366620161022153619\",\"type\":2,
			 * \"childPid\":94592396,\"childRowkey\":null}）	
			 * 一体化：relateChildren: [],（或者[{"childFid":"00000220170306114342","childPid":0,"type":2,
			 * "rowId":"58C6E1A005104DC1B152205EBD810FBA"}]）	
			 * 原则：字符串转json；若为[]，则直接赋值；childFid，type直接赋值；childPid,通过childFid在ix_poi表中查找，
			 * 若有则赋对应pid，同时若当前记录的poi的pid有值，则通过父，子共同查找非删除关系，若有，则赋对应rowId,否则生成新rowId
			 */
			convertRelateChildren(conn,lifecycle,newPoi,oldPoi,oldPoiObj);
			/* 线上：contacts: "[]",（或者"contacts":"[{\"number\":\"021-55277040\",\"linkman\":null,\"type\":1,
			 * \"weChatUrl\":null,\"priority\":1}]"）	
			 * 一体化：contacts: [],（或者"contacts":[{"linkman":"","number":"010-95105888","priority":1,"type":1,
			 * "rowId":"48F7237652530C9CE0530100007F0FE7"}]）	
			 * 原则：字符串转json。Linkman为null，赋值"";number，type，priority直接赋值；weChatUrl不处理;rowId,全字段查询IX_POI_CONTACT表，有非删除记录，则赋rowId，否则生成rowId
			 */
			convertContacts(lifecycle,newPoi,oldPoi,oldPoiObj);
			/*foodtypes: "null",	foodtypes: {	字符串转json；为null，则直接赋值（"foodtypes":null）
				  foodtype: "2016",	foodtype对应赋值
				  rowId: "488FC0E51C314C76A14E285D1B11E656",	全字段查询IX_POI_RESTAURANT表，有非删除记录，则赋rowId，否则生成rowId
				  openHour: "",	openHour对应赋值
				  parking: 0,	parking对应赋值
				  avgCost: 0,	avgCost对应赋值
				  creditCards: ""	creditCards对应赋值
				},	*/
			convertFoodtypes(lifecycle,newPoi,oldPoi,oldPoiObj);
			/*
			 * parkings: "null",（包含字段tollStd,tollDes,tollWay,openTime,totalNum,payment,remark,buildingType,
			 * resHigh,resWidth,resWeigh,certificate,vehicle,haveSpecialPlace,womenNum,handicapNum,miniNum,vipNum）	
			 * parkings: null,（包含字段tollStd,tollDes,tollWay,openTime,totalNum,payment,remark,buildingType,
			 * resHigh,resWidth,resWeigh,certificate,vehicle,haveSpecialPlace,womenNum,handicapNum,miniNum,vipNum,rowId）
			 * 字符串转json，直接赋值；然后全字段在IX_POI_PARKING中查找，存在非删除记录，则赋rowId，否则生成rowId
			 */
			convertParkings(lifecycle,newPoi,oldPoi,oldPoiObj);
			//hotel: "null",	hotel: null,	字符串转json，直接赋值；然后全字段在IX_POI_HOTEL中查找，存在非删除记录，则赋rowId，否则生成rowId
			converHotel(lifecycle,newPoi,oldPoi,oldPoiObj);
			/* 线上：sportsVenues: "null",(包含字段buildingType,sports)
			 * 一体化：sportsVenues: "",	
			 * 原则："null"转成""；否则转json，将buildingType赋给sportsVenues
			 */
			String oldSportsVenues=CollectConvertUtils.convertStr(oldPoi.getString("sportsVenues"));
			if(oldSportsVenues.isEmpty()){
				newPoi.put("sportsVenues", "");
			}else{
				newPoi.put("sportsVenues", CollectConvertUtils.convertStr(JSONObject.fromObject(oldSportsVenues).getString("buildingType")));
			}
			//chargingStation: "null",	chargingStation: null,(或者type,changeBrands,changeOpenType,servicePro,chargingNum,openHour,parkingFees,parkingInfo,availableState,rowId)	取oracle库中对应记录生成，没有则赋值""
			convertChargingStation(lifecycle,newPoi,oldPoi,oldPoiObj);
			//chargingPole: ""	chargingPole: [],(或者groupId,acdc,plugType,power,voltage,current,mode,count,plugNum,prices,openType,availableState,manufacturer,factoryNum,plotNum,productNum,parkingNum,floor,locationType,payment,rowId)	取oracle库中对应记录生成，没有则赋值""
			convertChargingPole(lifecycle,newPoi,oldPoi,oldPoiObj);
			//gasStation: "null",（包含字段fuelType,oilType,egType,mgType,payment,service,servicePro,openHour）	gasStation: null,（包含字段fuelType,oilType,egType,mgType,payment,service,servicePro,openHour,rowId）	字符串转json，直接赋值；然后全字段在IX_POI_GASSTATION中查找，存在非删除记录，则赋rowId，否则生成rowId
			convertGasStation(lifecycle, newPoi, oldPoi, oldPoiObj);
			//indoor: "{\"floor\":null,\"type\":0,\"open\":1}",	indoor: {type: 0,floor: ""}	indoor的字符串转json格式；type直接赋值；floor，null转"",否则直接赋值
			String oldIndoor=CollectConvertUtils.convertStr(oldPoi.getString("indoor"));
			if(oldIndoor.isEmpty()){
				newPoi.put("indoor", JSONNull.getInstance());
			}else{
				JSONObject newIndoor=new JSONObject();
				JSONObject oldIndoorJson=JSONObject.fromObject(oldIndoor);
				newIndoor.put("type", oldIndoorJson.getInt("type"));
				newIndoor.put("floor",CollectConvertUtils.convertStr(oldIndoorJson.getString("floor")));
				newPoi.put("indoor", newIndoor);
			}
			//brands: "[]"(或者包含code),	chain: "",（"chain":"304D"）	brands的字符串转json格式；为[]，赋值"";若有值，取第一个的code赋值chain
			JSONArray oldBrands=JSONArray.fromObject(oldPoi.getString("brands"));
			if(oldBrands==null||oldBrands.size()==0){
				newPoi.put("chain", "");
			}else{
				newPoi.put("chain", CollectConvertUtils.convertStr(JSONObject.fromObject(oldBrands.get(0)).getString("code")));
			}
			//rawFields: "null",	rawFields: "",	"null"转成""；否则，直接赋值
			newPoi.put("rawFields", CollectConvertUtils.convertStr(oldPoi.getString("rawFields")));
			/*attachments: "[]",	attachments: [{	参照word中的照片赋值原则
				  id: "BDCF4FF0B1CC4A18BB4249DF4BBF51AB",	
				  content: "BDCF4FF0B1CC4A18BB4249DF4BBF51AB.jpg",	
				  extContent: {	
				    shootDate: "20170303213020",	当前时间
				    latitude: 40.01316,	显示坐标四舍五入截取小数点后5位赋值
				    longitude: 116.47907,	显示坐标四舍五入截取小数点后5位赋值
				    deviceNum: "",	赋""
				    direction: 0	赋0
				  },	
				  type: 1,	
				  tag: 3	
				}],	*/
			convertAttachments(lifecycle, inPath,newPoi, oldPoi);
			/*sameFid: "",	"查询库里的同一关系，找到同一关系poi赋值对应的fid（同一关系都是两两一组）;
			若为删除对象(t_lifecycle: 1),则
			1.不需要补足同一关系.
			2.查找是否有同一关系，若有，则获取其同一关系poi的fid，查看txt中是否有该poi，若有同一关系poi，则在处理此同一关系poi时，需将sameFid赋值"""""
			*/
			convertSameFid(conn,lifecycle, newPoi, oldPoi, oldPoiObj);
			//线上：mergeDate: "20161110214719",	一体化：t_operateDate: "20170303213109",	原则：直接赋值
			newPoi.put("t_operateDate", CollectConvertUtils.convertStr(oldPoi.getString("mergeDate")));
			//sourceName: "Android",	sourceName: "Android"	直接赋值	
			newPoi.put("sourceName", CollectConvertUtils.convertStr(oldPoi.getString("sourceName")));
			return newPoi;
		}catch (Exception e) {
			DbUtils.commitAndCloseQuietly(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 一体化：sameFid: "",	
	 * 原则：查询库里的同一关系，找到同一关系poi赋值对应的fid（同一关系都是两两一组）;
	 *		若为删除对象(t_lifecycle: 1),则
	 * 		1.不需要补足同一关系.
	 * 		2.查找是否有同一关系，若有，则获取其同一关系poi的fid，查看txt中是否有该poi，若有同一关系poi，则在处理此同一关系poi时，需将sameFid赋值""
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 * @throws Exception 
	 */
	private static void convertSameFid(Connection conn,int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) throws Exception {
		if(lifecycle==3||lifecycle==1){//删除新增都不需要补充同一关系
			newPoi.put("sameFid", "");
			return;
		}
		HashSet<Long> poiSet = new HashSet<Long>();
		poiSet.add(oldPoiObj.objPid());
		Map<Long, Long> pidMap = IxPoiSelector.getSamePoiPidsByThisPids(conn,poiSet);
		if(pidMap!=null&&pidMap.containsKey(oldPoiObj.objPid())){
			BasicObj samePid = ObjSelector.selectByPid(conn, ObjectName.IX_POI, null, true,pidMap.get(oldPoiObj.objPid()), false);
			if(samePid!=null){
				newPoi.put("sameFid",((IxPoi)samePid.getMainrow()).getPoiNum());
			}
		}
	}
	/**
	 * 线上：attachments: "[]",	
	 * 一体化+原则：attachments: [{	参照word中的照片赋值原则
				  id: "BDCF4FF0B1CC4A18BB4249DF4BBF51AB",	
				  content: "BDCF4FF0B1CC4A18BB4249DF4BBF51AB.jpg",	
				  extContent: {	
				    shootDate: "20170303213020",	当前时间
				    latitude: 40.01316,	显示坐标四舍五入截取小数点后5位赋值
				    longitude: 116.47907,	显示坐标四舍五入截取小数点后5位赋值
				    deviceNum: "",	赋""
				    direction: 0	赋0
				  },	
				  type: 1,	
				  tag: 3	
				}]
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 * @throws ParseException 
	 */
	private static void convertAttachments(int lifecycle, String path,JSONObject newPoi,
			JSONObject oldPoi) throws ParseException {
		JSONArray oldAttachs = JSONArray.fromObject(oldPoi.getString("attachments"));
		JSONArray newAttachs=new JSONArray();
		if(oldAttachs==null||oldAttachs.size()==0){
			newPoi.put("attachments", newAttachs);
			return;
		}		
		for(Object oldAttach:oldAttachs){
			JSONObject newAttachJson = new JSONObject();
			JSONObject oldAttachJson = JSONObject.fromObject(oldAttach);
			
			newAttachJson.put("type", oldAttachJson.getInt("type"));
			newAttachJson.put("tag", oldAttachJson.getInt("tag"));
			
			JSONObject extContent = new JSONObject();
			extContent.put("shootDate", new SimpleDateFormat("yyyyMMddkkmmss").format(new Date()));
			extContent.put("deviceNum","");
			extContent.put("direction",0);
			Geometry geometry=new WKTReader().read(newPoi.getString("geometry"));
			extContent.put("latitude",geometry.getCoordinate().y);
			extContent.put("longitude",geometry.getCoordinate().x);
			newAttachJson.put("extContent", extContent);
			
			String idStr=UuidUtils.genUuid();
			newAttachJson.put("id", idStr);
			newAttachJson.put("content", idStr+".jpg");
			
			//照片重命名
			String oldPath=oldAttachJson.getString("url");
			String[] oldPathList = oldPath.split("/");
			String oldName = oldPathList[oldPathList.length-1];
			CollectConvertUtils.reNamePhoto(path+"/"+oldName, idStr+".jpg");
			newAttachs.add(newAttachJson);
		}		
		newPoi.put("attachments", newAttachs);		
	}
	/**
	 * 线上：gasStation: "null",（包含字段fuelType,oilType,egType,mgType,payment,service,servicePro,openHour）	
	 * 一体化：gasStation: null,（包含字段fuelType,oilType,egType,mgType,payment,service,servicePro,openHour,rowId）	
	 * 原则：字符串转json，直接赋值；然后全字段在IX_POI_GASSTATION中查找，存在非删除记录，则赋rowId，否则生成rowId
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertGasStation(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		String oldGasStr=CollectConvertUtils.convertStr(oldPoi.getString("gasStation"));
		if(oldGasStr==null||oldGasStr.isEmpty()){
			newPoi.put("gasStation", JSONNull.getInstance());
			return;
		}
		JSONObject oldGas = JSONObject.fromObject(oldGasStr);
		JSONObject newGas=new JSONObject();
		newGas.put("fuelType",CollectConvertUtils.convertStr(oldGas.getString("fuelType")));
		newGas.put("oilType",CollectConvertUtils.convertStr(oldGas.getString("oilType")));
		newGas.put("egType",CollectConvertUtils.convertStr(oldGas.getString("egType")));
		newGas.put("mgType",CollectConvertUtils.convertStr(oldGas.getString("mgType")));
		newGas.put("payment",CollectConvertUtils.convertStr(oldGas.getString("payment")));
		newGas.put("service",CollectConvertUtils.convertStr(oldGas.getString("service")));
		newGas.put("servicePro",CollectConvertUtils.convertStr(oldGas.getString("servicePro")));
		newGas.put("openHour",CollectConvertUtils.convertStr(oldGas.getString("openHour")));
		//rowid获取
		newGas.put("rowId", UuidUtils.genUuid());
		if(lifecycle!=3){
			List<IxPoiGasstation> gasList = oldPoiObj.getIxPoiGasstations();
			if(gasList!=null&&gasList.size()>0){
				IxPoiGasstation gas=gasList.get(0);
				newGas.put("rowId", gas.getRowId());
				}
			}
		newPoi.put("gasStation", newGas);
	}
	/**
	 * 线上：chargingPole: ""	
	 * 一体化：chargingPole: [],(或者groupId,acdc,plugType,power,voltage,current,mode,count,plugNum,prices,
	 * openType,availableState,manufacturer,factoryNum,plotNum,productNum,parkingNum,floor,locationType,payment,rowId)
	 * 原则：	取oracle库中对应记录生成，没有则赋值""
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertChargingPole(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		JSONArray poles=new JSONArray();
		newPoi.put("chargingPole", poles);
		if(lifecycle!=3){
			List<IxPoiChargingplot> chargingsList = oldPoiObj.getIxPoiChargingplots();
			if(chargingsList!=null&&chargingsList.size()>0){
				for(IxPoiChargingplot charging:chargingsList){
					JSONObject newCharging=new JSONObject();
					newCharging.put("groupId",charging.getGroupId());
					newCharging.put("acdc",charging.getAcdc());
					newCharging.put("plugType",charging.getPlugType());
					newCharging.put("power",charging.getPower());
					newCharging.put("voltage",charging.getVoltage());
					newCharging.put("current",charging.getCurrent());
					newCharging.put("mode",charging.getMode());
					newCharging.put("count",charging.getCount());
					newCharging.put("plugNum",charging.getPlugNum());
					newCharging.put("prices",charging.getPrices());
					newCharging.put("openType",charging.getOpenType());
					newCharging.put("availableState",charging.getAvailableState());
					newCharging.put("manufacturer",charging.getManufacturer());
					newCharging.put("factoryNum",charging.getFactoryNum());
					newCharging.put("plotNum",charging.getPlotNum());
					newCharging.put("productNum",charging.getProductNum());
					newCharging.put("parkingNum",charging.getParkingNum());
					newCharging.put("floor",charging.getFloor());
					newCharging.put("locationType",charging.getLocationType());
					newCharging.put("payment",charging.getPayment());
					newCharging.put("rowId",charging.getRowId());
					poles.add(newCharging);}
				}
			}
		if(poles!=null&&poles.size()>0){newPoi.put("chargingPole", poles);}
	}
	/**
	 * 线上：chargingStation: "null",	
	 * 一体化：chargingStation: null,(或者type,changeBrands,changeOpenType,servicePro,chargingNum,openHour,
	 * parkingFees,parkingInfo,availableState,rowId)	]
	 * 原则：取oracle库中对应记录生成，没有则赋值""
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertChargingStation(int lifecycle,
			JSONObject newPoi, JSONObject oldPoi, IxPoiObj oldPoiObj) {
		newPoi.put("chargingStation", JSONNull.getInstance());
		JSONObject newCharging=new JSONObject();
		if(lifecycle!=3){
			List<IxPoiChargingstation> chargingsList = oldPoiObj.getIxPoiChargingstations();
			if(chargingsList!=null&&chargingsList.size()>0){
				IxPoiChargingstation charging=chargingsList.get(0);
				newCharging.put("type",charging.getChargingType());
				newCharging.put("changeBrands",charging.getChangeBrands());
				newCharging.put("changeOpenType",charging.getChangeOpenType());
				newCharging.put("servicePro",charging.getServiceProv());
				newCharging.put("chargingNum",charging.getChargingNum());
				newCharging.put("openHour",charging.getOpenHour());
				newCharging.put("parkingFees",charging.getParkingFees());
				newCharging.put("parkingInfo",charging.getParkingInfo());
				newCharging.put("availableState",charging.getAvailableState());
				newCharging.put("rowId",charging.getRowId());
				}
			}
		if(newCharging!=null&&newCharging.size()>0){newPoi.put("chargingStation", newCharging);}
	}
	/**
	 * 线上：hotel: "null",	
	 * 一体化：hotel: null,	
	 * 原则：字符串转json，直接赋值；然后全字段在IX_POI_HOTEL中查找，存在非删除记录，则赋rowId，否则生成rowId
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void converHotel(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		String oldHotelStr=CollectConvertUtils.convertStr(oldPoi.getString("hotel"));
		if(oldHotelStr==null||oldHotelStr.isEmpty()){
			newPoi.put("hotel", JSONNull.getInstance());
			return;
		}
		JSONObject oldHotel = JSONObject.fromObject(oldHotelStr);
		JSONObject newHotel=new JSONObject();
		newHotel.put("rating",oldHotel.getInt("rating"));
		newHotel.put("creditCards",CollectConvertUtils.convertStr(oldHotel.getString("creditCards")));
		newHotel.put("description",CollectConvertUtils.convertStr(oldHotel.getString("description")));
		newHotel.put("checkInTime",CollectConvertUtils.convertStr(oldHotel.getString("checkInTime")));
		newHotel.put("checkOutTime",CollectConvertUtils.convertStr(oldHotel.getString("checkOutTime")));
		newHotel.put("roomCount",oldHotel.getInt("roomCount"));
		newHotel.put("roomType",CollectConvertUtils.convertStr(oldHotel.getString("roomType")));
		newHotel.put("roomPrice",CollectConvertUtils.convertStr(oldHotel.getString("roomPrice")));
		newHotel.put("breakfast",oldHotel.getInt("breakfast"));
		newHotel.put("service",CollectConvertUtils.convertStr(oldHotel.getString("service")));
		newHotel.put("parking",oldHotel.getInt("parking"));
		newHotel.put("openHour",CollectConvertUtils.convertStr(oldHotel.getString("openHour")));
		//rowid获取
		newHotel.put("rowId", UuidUtils.genUuid());
		if(lifecycle!=3){
			List<IxPoiHotel> hotelList = oldPoiObj.getIxPoiHotels();
			if(hotelList!=null&&hotelList.size()>0){
				IxPoiHotel hotel=hotelList.get(0);
				newHotel.put("rowId", hotel.getRowId());
				}
			}
		newPoi.put("hotel", newHotel);
	}
	/**
	 * 线上：parkings: "null",（包含字段tollStd,tollDes,tollWay,openTime,totalNum,payment,remark,buildingType,
	 * resHigh,resWidth,resWeigh,certificate,vehicle,haveSpecialPlace,womenNum,handicapNum,miniNum,vipNum）	
	 * 一体化：parkings: null,（包含字段tollStd,tollDes,tollWay,openTime,totalNum,payment,remark,buildingType,
	 * resHigh,resWidth,resWeigh,certificate,vehicle,haveSpecialPlace,womenNum,handicapNum,miniNum,vipNum,rowId）
	 * 原则：字符串转json，直接赋值；然后全字段在IX_POI_PARKING中查找，存在非删除记录，则赋rowId，否则生成rowId
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertParkings(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		String oldParkingsStr=CollectConvertUtils.convertStr(oldPoi.getString("parkings"));
		if(oldParkingsStr==null||oldParkingsStr.isEmpty()){
			newPoi.put("parkings", JSONNull.getInstance());
			return;
		}
		JSONObject oldParkings = JSONObject.fromObject(oldParkingsStr);
		JSONObject newParkings=new JSONObject();
		newParkings.put("tollStd",CollectConvertUtils.convertStr(oldParkings.getString("tollStd")));
		newParkings.put("tollDes",CollectConvertUtils.convertStr(oldParkings.getString("tollDes")));
		newParkings.put("tollWay",CollectConvertUtils.convertStr(oldParkings.getString("tollWay")));
		newParkings.put("openTime",CollectConvertUtils.convertStr(oldParkings.getString("openTime")));
		newParkings.put("totalNum",oldParkings.getInt("totalNum"));
		newParkings.put("payment",CollectConvertUtils.convertStr(oldParkings.getString("payment")));
		newParkings.put("remark",CollectConvertUtils.convertStr(oldParkings.getString("remark")));
		newParkings.put("buildingType",CollectConvertUtils.convertStr(oldParkings.getString("buildingType")));
		newParkings.put("resHigh",oldParkings.getDouble("resHigh"));
		newParkings.put("resWidth",oldParkings.getDouble("resWidth"));
		newParkings.put("resWeigh",oldParkings.getDouble("resWeigh"));
		newParkings.put("certificate",oldParkings.getInt("certificate"));
		newParkings.put("vehicle",oldParkings.getInt("vehicle"));
		newParkings.put("haveSpecialPlace",CollectConvertUtils.convertStr(oldParkings.getString("haveSpecialPlace")));
		newParkings.put("womenNum",oldParkings.getInt("womenNum"));
		newParkings.put("handicapNum",oldParkings.getInt("handicapNum"));
		newParkings.put("miniNum",oldParkings.getInt("miniNum"));
		newParkings.put("vipNum",oldParkings.getInt("vipNum"));
		//rowid获取
		newParkings.put("rowId", UuidUtils.genUuid());
		if(lifecycle!=3){
			List<IxPoiParking> parkingList = oldPoiObj.getIxPoiParkings();
			if(parkingList!=null&&parkingList.size()>0){
				IxPoiParking parking=parkingList.get(0);
				newParkings.put("rowId", parking.getRowId());
				}
			}
		newPoi.put("parkings", newParkings);
	}
	/**
	 *线上：foodtypes: "null",	
	 *一体化+原则：foodtypes: {	字符串转json；为null，则直接赋值（"foodtypes":null）
	  foodtype: "2016",	foodtype对应赋值
	  rowId: "488FC0E51C314C76A14E285D1B11E656",	全字段查询IX_POI_RESTAURANT表，有非删除记录，则赋rowId，否则生成rowId
	  openHour: "",	openHour对应赋值
	  parking: 0,	parking对应赋值
	  avgCost: 0,	avgCost对应赋值
	  creditCards: ""	creditCards对应赋值
	},
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertFoodtypes(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		String oldfoodTypesStr=CollectConvertUtils.convertStr(oldPoi.getString("foodtypes"));
		if(oldfoodTypesStr==null||oldfoodTypesStr.isEmpty()){
			newPoi.put("foodtypes", JSONNull.getInstance());
			return;
		}
		JSONObject oldFoodTypes = JSONObject.fromObject(oldfoodTypesStr);
		JSONObject newFoodTypes=new JSONObject();
		newFoodTypes.put("foodtype", CollectConvertUtils.convertStr(oldFoodTypes.getString("foodtype")));
		newFoodTypes.put("openHour", CollectConvertUtils.convertStr(oldFoodTypes.getString("openHour")));
		newFoodTypes.put("parking", oldFoodTypes.getInt("parking"));
		newFoodTypes.put("avgCost", oldFoodTypes.getInt("avgCost"));
		newFoodTypes.put("creditCards", CollectConvertUtils.convertStr(oldFoodTypes.getString("creditCards")));
		//rowid获取
		newFoodTypes.put("rowId", UuidUtils.genUuid());
		if(lifecycle!=3){
			List<IxPoiRestaurant> restList = oldPoiObj.getIxPoiRestaurants();
			if(restList!=null&&restList.size()>0){
				IxPoiRestaurant rest=restList.get(0);
				newFoodTypes.put("rowId", rest.getRowId());
				}
			}
		newPoi.put("foodtypes", newFoodTypes);
	}
	/**
	 * 线上：contacts: "[]",（或者"contacts":"[{\"number\":\"021-55277040\",\"linkman\":null,\"type\":1,
	 * \"weChatUrl\":null,\"priority\":1}]"）	
	 * 一体化：contacts: [],（或者"contacts":[{"linkman":"","number":"010-95105888","priority":1,"type":1,
	 * "rowId":"48F7237652530C9CE0530100007F0FE7"}]）	
	 * 原则：字符串转json。Linkman为null，赋值"";number，type，priority直接赋值；weChatUrl不处理;rowId,全字段查询IX_POI_CONTACT表，有非删除记录，则赋rowId，否则生成rowId	 
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertContacts(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		JSONArray oldContacts = JSONArray.fromObject(oldPoi.getString("contacts"));
		JSONArray newContacts=new JSONArray();
		if(oldContacts==null||oldContacts.size()==0){
			newPoi.put("contacts", newContacts);
			return;
		}		
		for(Object oldContact:oldContacts){
			JSONObject newContactJson = new JSONObject();
			JSONObject oldContactJson = JSONObject.fromObject(oldContact);
			newContactJson.put("number", oldContactJson.getString("number"));
			newContactJson.put("type", oldContactJson.getInt("type"));
			newContactJson.put("priority", oldContactJson.getInt("priority"));
			String linkman=oldContactJson.getString("linkman");
			if(linkman==null||linkman.isEmpty()){
				newContactJson.put("linkman", "");}
			else{newContactJson.put("linkman", linkman);}
			
			//rowid获取
			newContactJson.put("rowId", UuidUtils.genUuid());
			if(lifecycle!=3){
				List<IxPoiContact> contactList = oldPoiObj.getIxPoiContacts();
				if(contactList!=null&&contactList.size()>0){
					for(IxPoiContact poiContact:contactList){
						if(CollectConvertUtils.compareStr(poiContact.getContact(),oldContactJson.getString("number"))
								&&poiContact.getContactType()==oldContactJson.getInt("type")
								&&poiContact.getPriority()==oldContactJson.getInt("priority")){
							newContactJson.put("rowId", poiContact.getRowId());
							break;
						}
					}
				}
			}
			newContacts.add(newContactJson);
		}		
		newPoi.put("contacts", newContacts);
	}
	/**
	 * 线上：relateChildren: "[]",（或者"relateChildren":"[{\"childFid\":\"00366620161022153619\",\"type\":2,
		 \"childPid\":94592396,\"childRowkey\":null}）	
		 一体化：relateChildren: [],（或者[{"childFid":"00000220170306114342","childPid":0,"type":2,
		 "rowId":"58C6E1A005104DC1B152205EBD810FBA"}]）	
		 原则：字符串转json；若为[]，则直接赋值；childFid，type直接赋值；childPid,通过childFid在ix_poi表中查找，
		 若有则赋对应pid，同时若当前记录的poi的pid有值，则通过父，子共同查找非删除关系，若有，则赋对应rowId,否则生成新rowId
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 * @throws Exception 
	 */
	private static void convertRelateChildren(Connection conn,int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) throws Exception {
		JSONArray oldChilds = JSONArray.fromObject(oldPoi.getString("relateChildren"));
		JSONArray newChilds=new JSONArray();
		if(oldChilds==null||oldChilds.size()==0){
			newPoi.put("relateChildren", newChilds);
			return;
		}		
		for(Object oldChild:oldChilds){
			JSONObject newChildJson = new JSONObject();
			JSONObject oldChildJson = JSONObject.fromObject(oldChild);
			newChildJson.put("childFid", oldChildJson.getString("childFid"));
			newChildJson.put("type", oldChildJson.getInt("type"));
			IxPoiObj childPoiObj = loadPoiByFid(conn, oldChildJson.getString("childFid"),true);
			if(childPoiObj==null){
				newChildJson.put("childPid", 0);
			}else{
				newChildJson.put("childPid",childPoiObj.objPid());
			}
			//rowid获取
			newChildJson.put("rowId", UuidUtils.genUuid());
			if(lifecycle!=3){
				List<IxPoiChildren> childList = oldPoiObj.getIxPoiChildrens();
				if(childList!=null&&childList.size()>0){
					for(IxPoiChildren poiChild:childList){
						if(poiChild.getChildPoiPid()==newChildJson.getLong("childPid")
								&&poiChild.getRelationType()==newChildJson.getInt("type")){
							newChildJson.put("rowId", poiChild.getRowId());
							break;
						}
					}
				}
			}
			newChilds.add(newChildJson);
		}		
		newPoi.put("relateChildren", newChilds);
	}
	/**
	 * 根据fid获取poi对象
	 * @param conn
	 * @param fid
	 * @return
	 * @throws Exception
	 */
	public static IxPoiObj loadPoiByFid(Connection conn,String fid,boolean isMainOnly) throws Exception{
		String objType = "IX_POI";
		String colName = "POI_NUM";
		String colValue = fid;
		boolean isLock = false;
		BasicObj obj = ObjSelector.selectBySpecColumn(conn, objType, null,isMainOnly, colName,colValue, isLock);
		return (IxPoiObj) obj;
	}
	/**
	 * 转pid
	 * 线上 fid: "0523081102MFY00291",	
	 *一体化fid: "00166420170303213016",	原则：直接赋值
	 *		pid: 0,	原则：根据fid查IX_POI表，若有则，更新pid；否则为0
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertPid(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		if(lifecycle!=3){
			newPoi.put("pid", oldPoiObj.objPid());
		}else{
			newPoi.put("pid",0);
		}
	}
}
=======
package com.navinfo.dataservice.scripts.tmp.service;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import oracle.net.aso.n;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

public class CollectConvertMain {
	private static Logger log = LogManager.getLogger(CollectConvertMain.class);
	
	/**
	 * json转换模块
	 * 文件：com.navinfo.dataservice.scripts.tmp.service.CollectConvertMain.java
	 * 实现方式：java
	 * 输入：JSONObject 线上格式
	 * 输出：JSONObject 一体化格式
	 * 原则：
	 * 1.json格式转换，具体原则参照excel
	 * 2.照片转换时，可调用照片重命名模块
	 * 3.根据一体化格式geo获取dbid模块
	 * @param dbId 可选参数前端脚本可能会传入，非0则用，否则自行计算
	 * @param inPath data/12/
	 * @param oldPoi
	 * @return
	 * @throws Exception 
	 */
	public static JSONObject convertMain(int dbId, String inPath, JSONObject oldPoi) throws Exception{
		JSONObject newPoi=new JSONObject();
		Connection conn = null;
		try{
			/*
			 * 线上 geometry: "POINT (120.1733200000000039 32.3584900000000033)"
			 * 一体化geometry: "POINT (116.47958 40.01322)",	
			 * 原则：四舍五入截取小数点后5位赋值
			 */
			Geometry oldGeo = new WKTReader().read(oldPoi.getString("geometry"));
			String newGeoStr=GeoTranslator.jts2Wkt(oldGeo,0.00001, 5);
			newPoi.put("geometry", newGeoStr);
			//获取poi所在大区连接
			if(dbId==0){dbId=CollectConvertUtils.getDbidByGeo(newGeoStr);}	
			conn = DBConnector.getInstance().getConnectionById(dbId);
			/*线上 fid: "0523081102MFY00291",	
			 *一体化fid: "00166420170303213016",	原则：直接赋值
			 *		pid: 0,	原则：根据fid查IX_POI表，若有则，更新pid；否则为0
		     */
			String fid=oldPoi.getString("fid");
			newPoi.put("fid", fid);
			IxPoiObj oldPoiObj =null;
			//lifecycle: "2"	t_lifecycle: 3,	字符串转数字后赋值
			//0 无； 1 删除；2 更新；3 新增；
			int lifecycle=CollectConvertUtils.convertInt(oldPoi.getString("lifecycle"));			
			oldPoiObj = loadPoiByFid(conn, fid,false);
			if(oldPoiObj==null){
				if(lifecycle==2){lifecycle=3;}
				else if(lifecycle==1){throw new Exception("数据fid="+fid+"在大区"+dbId+"中不存在:lifecycle="+lifecycle);}
			}else{
				//row.setOpType(OperationType.PRE_DELETED);
				if(!oldPoiObj.getMainrow().getOpType().equals(OperationType.PRE_DELETED)&&lifecycle==3){
					lifecycle=2;
				}
				if(oldPoiObj.getMainrow().getOpType().equals(OperationType.PRE_DELETED)&&(lifecycle==3||lifecycle==2)){
					throw new Exception("数据fid="+fid+"在大区"+dbId+"中存在,且为删除记录:lifecycle="+lifecycle);
				}
			}
			newPoi.put("t_lifecycle", lifecycle);
			convertPid(lifecycle,newPoi,oldPoi,oldPoiObj);
			/*线上meshid: "486041",	一体化meshid: 0,	原则：字符串转数字后赋值*/
			newPoi.put("meshid",CollectConvertUtils.convertInt(oldPoi.getString("meshid")));
			//name: "泰州市顺旺铸造有限公司",	name: "Ｉｉｉｉ",	"null"转成""；否则，直接赋值
			newPoi.put("name", CollectConvertUtils.convertStr(oldPoi.getString("name")));
			//kindCode: "220100",	kindCode: "110101",	直接赋值
			newPoi.put("kindCode", CollectConvertUtils.convertStr(oldPoi.getString("kindCode")));
			//vipFlag: "null",	vipFlag: "",	"null"转成""；否则，直接赋值
			newPoi.put("vipFlag", CollectConvertUtils.convertStr(oldPoi.getString("vipFlag")));
			//truck: "0",	truck: 0,	字符串转数字后赋值
			newPoi.put("truck", CollectConvertUtils.convertInt(oldPoi.getString("truck")));
			/* 线上guide: "{\"latitude\":32.35849986671124,\"longitude\":120.17348080519618,\"linkPid\":0}",	
			 * 一体化guide: {linkPid: 208002607,longitude: 116.47924,latitude: 40.01292}
			 * 原则：guide的字符串转json格式；linkPid，直接赋值；longitude，latitude四舍五入截取小数点后5位赋值
			 */
			JSONObject oldGuide = JSONObject.fromObject(oldPoi.getString("guide"));
			JSONObject newGuide=new JSONObject();
			newGuide.put("linkPid", oldGuide.getLong("linkPid"));
			newGuide.put("longitude", CollectConvertUtils.convertDouble(oldGuide.getDouble("longitude"),5));
			newGuide.put("latitude", CollectConvertUtils.convertDouble(oldGuide.getDouble("latitude"),5));
			newPoi.put("guide", newGuide);
			//address: "null",	address: "",	"null"转成""；否则，直接赋值
			newPoi.put("address", CollectConvertUtils.convertStr(oldPoi.getString("address")));
			//postCode: "null",	postCode: "",	"null"转成""；否则，直接赋值
			newPoi.put("postCode", CollectConvertUtils.convertStr(oldPoi.getString("postCode")));
			//level: "B3",	level: "B3",	"null"转成""；否则，直接赋值
			newPoi.put("level", CollectConvertUtils.convertStr(oldPoi.getString("level")));
			//open24H: "2",	open24H: 0,	字符串转数字后赋值
			newPoi.put("open24H", CollectConvertUtils.convertInt(oldPoi.getString("open24H")));
			/* 线上relateParent: "null",（或者"relateParent":"{\"parentFid\":\"00366620161022153845\",\"parentRowkey\":null}"）
			 * 一体化parentFid: "",（或者"parentFid":"00000220170306135753"）	
			 * 原则："null"转成""；relateParent的字符串转json格式；取parentFid直接赋值
			 */
			String oldParentStr=CollectConvertUtils.convertStr(oldPoi.getString("relateParent"));
			if(oldParentStr.isEmpty()){
				newPoi.put("parentFid", "");
			}else{
				newPoi.put("parentFid", JSONObject.fromObject(oldParentStr).getString("parentFid"));
			}
			/* 线上：relateChildren: "[]",（或者"relateChildren":"[{\"childFid\":\"00366620161022153619\",\"type\":2,
			 * \"childPid\":94592396,\"childRowkey\":null}）	
			 * 一体化：relateChildren: [],（或者[{"childFid":"00000220170306114342","childPid":0,"type":2,
			 * "rowId":"58C6E1A005104DC1B152205EBD810FBA"}]）	
			 * 原则：字符串转json；若为[]，则直接赋值；childFid，type直接赋值；childPid,通过childFid在ix_poi表中查找，
			 * 若有则赋对应pid，同时若当前记录的poi的pid有值，则通过父，子共同查找非删除关系，若有，则赋对应rowId,否则生成新rowId
			 */
			convertRelateChildren(conn,lifecycle,newPoi,oldPoi,oldPoiObj);
			/* 线上：contacts: "[]",（或者"contacts":"[{\"number\":\"021-55277040\",\"linkman\":null,\"type\":1,
			 * \"weChatUrl\":null,\"priority\":1}]"）	
			 * 一体化：contacts: [],（或者"contacts":[{"linkman":"","number":"010-95105888","priority":1,"type":1,
			 * "rowId":"48F7237652530C9CE0530100007F0FE7"}]）	
			 * 原则：字符串转json。Linkman为null，赋值"";number，type，priority直接赋值；weChatUrl不处理;rowId,全字段查询IX_POI_CONTACT表，有非删除记录，则赋rowId，否则生成rowId
			 */
			convertContacts(lifecycle,newPoi,oldPoi,oldPoiObj);
			/*foodtypes: "null",	foodtypes: {	字符串转json；为null，则直接赋值（"foodtypes":null）
				  foodtype: "2016",	foodtype对应赋值
				  rowId: "488FC0E51C314C76A14E285D1B11E656",	全字段查询IX_POI_RESTAURANT表，有非删除记录，则赋rowId，否则生成rowId
				  openHour: "",	openHour对应赋值
				  parking: 0,	parking对应赋值
				  avgCost: 0,	avgCost对应赋值
				  creditCards: ""	creditCards对应赋值
				},	*/
			convertFoodtypes(lifecycle,newPoi,oldPoi,oldPoiObj);
			/*
			 * parkings: "null",（包含字段tollStd,tollDes,tollWay,openTime,totalNum,payment,remark,buildingType,
			 * resHigh,resWidth,resWeigh,certificate,vehicle,haveSpecialPlace,womenNum,handicapNum,miniNum,vipNum）	
			 * parkings: null,（包含字段tollStd,tollDes,tollWay,openTime,totalNum,payment,remark,buildingType,
			 * resHigh,resWidth,resWeigh,certificate,vehicle,haveSpecialPlace,womenNum,handicapNum,miniNum,vipNum,rowId）
			 * 字符串转json，直接赋值；然后全字段在IX_POI_PARKING中查找，存在非删除记录，则赋rowId，否则生成rowId
			 */
			convertParkings(lifecycle,newPoi,oldPoi,oldPoiObj);
			//hotel: "null",	hotel: null,	字符串转json，直接赋值；然后全字段在IX_POI_HOTEL中查找，存在非删除记录，则赋rowId，否则生成rowId
			converHotel(lifecycle,newPoi,oldPoi,oldPoiObj);
			/* 线上：sportsVenues: "null",(包含字段buildingType,sports)
			 * 一体化：sportsVenues: "",	
			 * 原则："null"转成""；否则转json，将buildingType赋给sportsVenues
			 */
			String oldSportsVenues=CollectConvertUtils.convertStr(oldPoi.getString("sportsVenues"));
			if(oldSportsVenues.isEmpty()){
				newPoi.put("sportsVenues", "");
			}else{
				newPoi.put("sportsVenues", CollectConvertUtils.convertStr(JSONObject.fromObject(oldSportsVenues).getString("buildingType")));
			}
			//chargingStation: "null",	chargingStation: null,(或者type,changeBrands,changeOpenType,servicePro,chargingNum,openHour,parkingFees,parkingInfo,availableState,rowId)	取oracle库中对应记录生成，没有则赋值""
			convertChargingStation(lifecycle,newPoi,oldPoi,oldPoiObj);
			//chargingPole: ""	chargingPole: [],(或者groupId,acdc,plugType,power,voltage,current,mode,count,plugNum,prices,openType,availableState,manufacturer,factoryNum,plotNum,productNum,parkingNum,floor,locationType,payment,rowId)	取oracle库中对应记录生成，没有则赋值""
			convertChargingPole(lifecycle,newPoi,oldPoi,oldPoiObj);
			//gasStation: "null",（包含字段fuelType,oilType,egType,mgType,payment,service,servicePro,openHour）	gasStation: null,（包含字段fuelType,oilType,egType,mgType,payment,service,servicePro,openHour,rowId）	字符串转json，直接赋值；然后全字段在IX_POI_GASSTATION中查找，存在非删除记录，则赋rowId，否则生成rowId
			convertGasStation(lifecycle, newPoi, oldPoi, oldPoiObj);
			//indoor: "{\"floor\":null,\"type\":0,\"open\":1}",	indoor: {type: 0,floor: ""}	indoor的字符串转json格式；type直接赋值；floor，null转"",否则直接赋值
			String oldIndoor=CollectConvertUtils.convertStr(oldPoi.getString("indoor"));
			if(oldIndoor.isEmpty()){
				newPoi.put("indoor", JSONNull.getInstance());
			}else{
				JSONObject newIndoor=new JSONObject();
				JSONObject oldIndoorJson=JSONObject.fromObject(oldIndoor);
				newIndoor.put("type", oldIndoorJson.getInt("type"));
				newIndoor.put("floor",CollectConvertUtils.convertStr(oldIndoorJson.getString("floor")));
				newPoi.put("indoor", newIndoor);
			}
			//brands: "[]"(或者包含code),	chain: "",（"chain":"304D"）	brands的字符串转json格式；为[]，赋值"";若有值，取第一个的code赋值chain
			JSONArray oldBrands=JSONArray.fromObject(oldPoi.getString("brands"));
			if(oldBrands==null||oldBrands.size()==0){
				newPoi.put("chain", "");
			}else{
				newPoi.put("chain", CollectConvertUtils.convertStr(JSONObject.fromObject(oldBrands.get(0)).getString("code")));
			}
			//rawFields: "null",	rawFields: "",	"null"转成""；否则，直接赋值
			newPoi.put("rawFields", CollectConvertUtils.convertStr(oldPoi.getString("rawFields")));
			/*attachments: "[]",	attachments: [{	参照word中的照片赋值原则
				  id: "BDCF4FF0B1CC4A18BB4249DF4BBF51AB",	
				  content: "BDCF4FF0B1CC4A18BB4249DF4BBF51AB.jpg",	
				  extContent: {	
				    shootDate: "20170303213020",	当前时间
				    latitude: 40.01316,	显示坐标四舍五入截取小数点后5位赋值
				    longitude: 116.47907,	显示坐标四舍五入截取小数点后5位赋值
				    deviceNum: "",	赋""
				    direction: 0	赋0
				  },	
				  type: 1,	
				  tag: 3	
				}],	*/
			convertAttachments(lifecycle, inPath,newPoi, oldPoi);
			/*sameFid: "",	"查询库里的同一关系，找到同一关系poi赋值对应的fid（同一关系都是两两一组）;
			若为删除对象(t_lifecycle: 1),则
			1.不需要补足同一关系.
			2.查找是否有同一关系，若有，则获取其同一关系poi的fid，查看txt中是否有该poi，若有同一关系poi，则在处理此同一关系poi时，需将sameFid赋值"""""
			*/
			convertSameFid(conn,lifecycle, newPoi, oldPoi, oldPoiObj);
			//线上：mergeDate: "20161110214719",	一体化：t_operateDate: "20170303213109",	原则：直接赋值
			newPoi.put("t_operateDate", CollectConvertUtils.convertStr(oldPoi.getString("mergeDate")));
			//sourceName: "Android",	sourceName: "Android"	直接赋值	
			newPoi.put("sourceName", CollectConvertUtils.convertStr(oldPoi.getString("sourceName")));
			return newPoi;
		}catch (Exception e) {
			DbUtils.commitAndCloseQuietly(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 一体化：sameFid: "",	
	 * 原则：查询库里的同一关系，找到同一关系poi赋值对应的fid（同一关系都是两两一组）;
	 *		若为删除对象(t_lifecycle: 1),则
	 * 		1.不需要补足同一关系.
	 * 		2.查找是否有同一关系，若有，则获取其同一关系poi的fid，查看txt中是否有该poi，若有同一关系poi，则在处理此同一关系poi时，需将sameFid赋值""
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 * @throws Exception 
	 */
	private static void convertSameFid(Connection conn,int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) throws Exception {
		if(lifecycle==3||lifecycle==1){//删除新增都不需要补充同一关系
			newPoi.put("sameFid", "");
			return;
		}
		HashSet<Long> poiSet = new HashSet<Long>();
		poiSet.add(oldPoiObj.objPid());
		Map<Long, Long> pidMap = IxPoiSelector.getSamePoiPidsByThisPids(conn,poiSet);
		if(pidMap!=null&&pidMap.containsKey(oldPoiObj.objPid())){
			BasicObj samePid = ObjSelector.selectByPid(conn, ObjectName.IX_POI, null, true,pidMap.get(oldPoiObj.objPid()), false);
			if(samePid!=null){
				newPoi.put("sameFid",((IxPoi)samePid.getMainrow()).getPoiNum());
			}
		}
	}
	/**
	 * 线上：attachments: "[]",	
	 * 一体化+原则：attachments: [{	参照word中的照片赋值原则
				  id: "BDCF4FF0B1CC4A18BB4249DF4BBF51AB",	
				  content: "BDCF4FF0B1CC4A18BB4249DF4BBF51AB.jpg",	
				  extContent: {	
				    shootDate: "20170303213020",	当前时间
				    latitude: 40.01316,	显示坐标四舍五入截取小数点后5位赋值
				    longitude: 116.47907,	显示坐标四舍五入截取小数点后5位赋值
				    deviceNum: "",	赋""
				    direction: 0	赋0
				  },	
				  type: 1,	
				  tag: 3	
				}]
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 * @throws ParseException 
	 */
	private static void convertAttachments(int lifecycle, String path,JSONObject newPoi,
			JSONObject oldPoi) throws ParseException {
		JSONArray oldAttachs = JSONArray.fromObject(oldPoi.getString("attachments"));
		JSONArray newAttachs=new JSONArray();
		if(oldAttachs==null||oldAttachs.size()==0){
			newPoi.put("attachments", newAttachs);
			return;
		}		
		for(Object oldAttach:oldAttachs){
			JSONObject newAttachJson = new JSONObject();
			JSONObject oldAttachJson = JSONObject.fromObject(oldAttach);
			
			newAttachJson.put("type", oldAttachJson.getInt("type"));
			newAttachJson.put("tag", oldAttachJson.getInt("tag"));
			
			JSONObject extContent = new JSONObject();
			extContent.put("shootDate", new SimpleDateFormat("yyyyMMddkkmmss").format(new Date()));
			extContent.put("deviceNum","");
			extContent.put("direction",0);
			Geometry geometry=new WKTReader().read(newPoi.getString("geometry"));
			extContent.put("latitude",geometry.getCoordinate().y);
			extContent.put("longitude",geometry.getCoordinate().x);
			newAttachJson.put("extContent", extContent);
			
			String idStr=UuidUtils.genUuid();
			newAttachJson.put("id", idStr);
			newAttachJson.put("content", idStr+".jpg");
			
			//照片重命名
			String oldPath=oldAttachJson.getString("url");
			String[] oldPathList = oldPath.split("/");
			String oldName = oldPathList[oldPathList.length-1];
			CollectConvertUtils.reNamePhoto(path+"/"+oldName, idStr+".jpg");
			newAttachs.add(newAttachJson);
		}		
		newPoi.put("attachments", newAttachs);		
	}
	/**
	 * 线上：gasStation: "null",（包含字段fuelType,oilType,egType,mgType,payment,service,servicePro,openHour）	
	 * 一体化：gasStation: null,（包含字段fuelType,oilType,egType,mgType,payment,service,servicePro,openHour,rowId）	
	 * 原则：字符串转json，直接赋值；然后全字段在IX_POI_GASSTATION中查找，存在非删除记录，则赋rowId，否则生成rowId
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertGasStation(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		String oldGasStr=CollectConvertUtils.convertStr(oldPoi.getString("gasStation"));
		if(oldGasStr==null||oldGasStr.isEmpty()){
			newPoi.put("gasStation", JSONNull.getInstance());
			return;
		}
		JSONObject oldGas = JSONObject.fromObject(oldGasStr);
		JSONObject newGas=new JSONObject();
		newGas.put("fuelType",CollectConvertUtils.convertStr(oldGas.getString("fuelType")));
		newGas.put("oilType",CollectConvertUtils.convertStr(oldGas.getString("oilType")));
		newGas.put("egType",CollectConvertUtils.convertStr(oldGas.getString("egType")));
		newGas.put("mgType",CollectConvertUtils.convertStr(oldGas.getString("mgType")));
		newGas.put("payment",CollectConvertUtils.convertStr(oldGas.getString("payment")));
		newGas.put("service",CollectConvertUtils.convertStr(oldGas.getString("service")));
		newGas.put("servicePro",CollectConvertUtils.convertStr(oldGas.getString("servicePro")));
		newGas.put("openHour",CollectConvertUtils.convertStr(oldGas.getString("openHour")));
		//rowid获取
		newGas.put("rowId", UuidUtils.genUuid());
		if(lifecycle!=3){
			List<IxPoiGasstation> gasList = oldPoiObj.getIxPoiGasstations();
			if(gasList!=null&&gasList.size()>0){
				IxPoiGasstation gas=gasList.get(0);
				newGas.put("rowId", gas.getRowId());
				}
			}
		newPoi.put("gasStation", newGas);
	}
	/**
	 * 线上：chargingPole: ""	
	 * 一体化：chargingPole: [],(或者groupId,acdc,plugType,power,voltage,current,mode,count,plugNum,prices,
	 * openType,availableState,manufacturer,factoryNum,plotNum,productNum,parkingNum,floor,locationType,payment,rowId)
	 * 原则：	取oracle库中对应记录生成，没有则赋值""
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertChargingPole(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		JSONArray poles=new JSONArray();
		newPoi.put("chargingPole", poles);
		if(lifecycle!=3){
			List<IxPoiChargingplot> chargingsList = oldPoiObj.getIxPoiChargingplots();
			if(chargingsList!=null&&chargingsList.size()>0){
				for(IxPoiChargingplot charging:chargingsList){
					JSONObject newCharging=new JSONObject();
					newCharging.put("groupId",charging.getGroupId());
					newCharging.put("acdc",charging.getAcdc());
					newCharging.put("plugType",charging.getPlugType());
					newCharging.put("power",charging.getPower());
					newCharging.put("voltage",charging.getVoltage());
					newCharging.put("current",charging.getCurrent());
					newCharging.put("mode",charging.getMode());
					newCharging.put("count",charging.getCount());
					newCharging.put("plugNum",charging.getPlugNum());
					newCharging.put("prices",charging.getPrices());
					newCharging.put("openType",charging.getOpenType());
					newCharging.put("availableState",charging.getAvailableState());
					newCharging.put("manufacturer",charging.getManufacturer());
					newCharging.put("factoryNum",charging.getFactoryNum());
					newCharging.put("plotNum",charging.getPlotNum());
					newCharging.put("productNum",charging.getProductNum());
					newCharging.put("parkingNum",charging.getParkingNum());
					newCharging.put("floor",charging.getFloor());
					newCharging.put("locationType",charging.getLocationType());
					newCharging.put("payment",charging.getPayment());
					newCharging.put("rowId",charging.getRowId());
					poles.add(newCharging);}
				}
			}
		if(poles!=null&&poles.size()>0){newPoi.put("chargingPole", poles);}
	}
	/**
	 * 线上：chargingStation: "null",	
	 * 一体化：chargingStation: null,(或者type,changeBrands,changeOpenType,servicePro,chargingNum,openHour,
	 * parkingFees,parkingInfo,availableState,rowId)	]
	 * 原则：取oracle库中对应记录生成，没有则赋值""
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertChargingStation(int lifecycle,
			JSONObject newPoi, JSONObject oldPoi, IxPoiObj oldPoiObj) {
		newPoi.put("chargingStation", JSONNull.getInstance());
		JSONObject newCharging=new JSONObject();
		if(lifecycle!=3){
			List<IxPoiChargingstation> chargingsList = oldPoiObj.getIxPoiChargingstations();
			if(chargingsList!=null&&chargingsList.size()>0){
				IxPoiChargingstation charging=chargingsList.get(0);
				newCharging.put("type",charging.getChargingType());
				newCharging.put("changeBrands",charging.getChangeBrands());
				newCharging.put("changeOpenType",charging.getChangeOpenType());
				newCharging.put("servicePro",charging.getServiceProv());
				newCharging.put("chargingNum",charging.getChargingNum());
				newCharging.put("openHour",charging.getOpenHour());
				newCharging.put("parkingFees",charging.getParkingFees());
				newCharging.put("parkingInfo",charging.getParkingInfo());
				newCharging.put("availableState",charging.getAvailableState());
				newCharging.put("rowId",charging.getRowId());
				}
			}
		if(newCharging!=null&&newCharging.size()>0){newPoi.put("chargingStation", newCharging);}
	}
	/**
	 * 线上：hotel: "null",	
	 * 一体化：hotel: null,	
	 * 原则：字符串转json，直接赋值；然后全字段在IX_POI_HOTEL中查找，存在非删除记录，则赋rowId，否则生成rowId
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void converHotel(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		String oldHotelStr=CollectConvertUtils.convertStr(oldPoi.getString("hotel"));
		if(oldHotelStr==null||oldHotelStr.isEmpty()){
			newPoi.put("hotel", JSONNull.getInstance());
			return;
		}
		JSONObject oldHotel = JSONObject.fromObject(oldHotelStr);
		JSONObject newHotel=new JSONObject();
		newHotel.put("rating",oldHotel.getInt("rating"));
		newHotel.put("creditCards",CollectConvertUtils.convertStr(oldHotel.getString("creditCards")));
		newHotel.put("description",CollectConvertUtils.convertStr(oldHotel.getString("description")));
		newHotel.put("checkInTime",CollectConvertUtils.convertStr(oldHotel.getString("checkInTime")));
		newHotel.put("checkOutTime",CollectConvertUtils.convertStr(oldHotel.getString("checkOutTime")));
		newHotel.put("roomCount",oldHotel.getInt("roomCount"));
		newHotel.put("roomType",CollectConvertUtils.convertStr(oldHotel.getString("roomType")));
		newHotel.put("roomPrice",CollectConvertUtils.convertStr(oldHotel.getString("roomPrice")));
		newHotel.put("breakfast",oldHotel.getInt("breakfast"));
		newHotel.put("service",CollectConvertUtils.convertStr(oldHotel.getString("service")));
		newHotel.put("parking",oldHotel.getInt("parking"));
		newHotel.put("openHour",CollectConvertUtils.convertStr(oldHotel.getString("openHour")));
		//rowid获取
		newHotel.put("rowId", UuidUtils.genUuid());
		if(lifecycle!=3){
			List<IxPoiHotel> hotelList = oldPoiObj.getIxPoiHotels();
			if(hotelList!=null&&hotelList.size()>0){
				IxPoiHotel hotel=hotelList.get(0);
				newHotel.put("rowId", hotel.getRowId());
				}
			}
		newPoi.put("hotel", newHotel);
	}
	/**
	 * 线上：parkings: "null",（包含字段tollStd,tollDes,tollWay,openTime,totalNum,payment,remark,buildingType,
	 * resHigh,resWidth,resWeigh,certificate,vehicle,haveSpecialPlace,womenNum,handicapNum,miniNum,vipNum）	
	 * 一体化：parkings: null,（包含字段tollStd,tollDes,tollWay,openTime,totalNum,payment,remark,buildingType,
	 * resHigh,resWidth,resWeigh,certificate,vehicle,haveSpecialPlace,womenNum,handicapNum,miniNum,vipNum,rowId）
	 * 原则：字符串转json，直接赋值；然后全字段在IX_POI_PARKING中查找，存在非删除记录，则赋rowId，否则生成rowId
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertParkings(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		String oldParkingsStr=CollectConvertUtils.convertStr(oldPoi.getString("parkings"));
		if(oldParkingsStr==null||oldParkingsStr.isEmpty()){
			newPoi.put("parkings", JSONNull.getInstance());
			return;
		}
		JSONObject oldParkings = JSONObject.fromObject(oldParkingsStr);
		JSONObject newParkings=new JSONObject();
		newParkings.put("tollStd",CollectConvertUtils.convertStr(oldParkings.getString("tollStd")));
		newParkings.put("tollDes",CollectConvertUtils.convertStr(oldParkings.getString("tollDes")));
		newParkings.put("tollWay",CollectConvertUtils.convertStr(oldParkings.getString("tollWay")));
		newParkings.put("openTime",CollectConvertUtils.convertStr(oldParkings.getString("openTime")));
		newParkings.put("totalNum",oldParkings.getInt("totalNum"));
		newParkings.put("payment",CollectConvertUtils.convertStr(oldParkings.getString("payment")));
		newParkings.put("remark",CollectConvertUtils.convertStr(oldParkings.getString("remark")));
		newParkings.put("buildingType",CollectConvertUtils.convertStr(oldParkings.getString("buildingType")));
		newParkings.put("resHigh",oldParkings.getDouble("resHigh"));
		newParkings.put("resWidth",oldParkings.getDouble("resWidth"));
		newParkings.put("resWeigh",oldParkings.getDouble("resWeigh"));
		newParkings.put("certificate",oldParkings.getInt("certificate"));
		newParkings.put("vehicle",oldParkings.getInt("vehicle"));
		newParkings.put("haveSpecialPlace",CollectConvertUtils.convertStr(oldParkings.getString("haveSpecialPlace")));
		newParkings.put("womenNum",oldParkings.getInt("womenNum"));
		newParkings.put("handicapNum",oldParkings.getInt("handicapNum"));
		newParkings.put("miniNum",oldParkings.getInt("miniNum"));
		newParkings.put("vipNum",oldParkings.getInt("vipNum"));
		//rowid获取
		newParkings.put("rowId", UuidUtils.genUuid());
		if(lifecycle!=3){
			List<IxPoiParking> parkingList = oldPoiObj.getIxPoiParkings();
			if(parkingList!=null&&parkingList.size()>0){
				IxPoiParking parking=parkingList.get(0);
				newParkings.put("rowId", parking.getRowId());
				}
			}
		newPoi.put("parkings", newParkings);
	}
	/**
	 *线上：foodtypes: "null",	
	 *一体化+原则：foodtypes: {	字符串转json；为null，则直接赋值（"foodtypes":null）
	  foodtype: "2016",	foodtype对应赋值
	  rowId: "488FC0E51C314C76A14E285D1B11E656",	全字段查询IX_POI_RESTAURANT表，有非删除记录，则赋rowId，否则生成rowId
	  openHour: "",	openHour对应赋值
	  parking: 0,	parking对应赋值
	  avgCost: 0,	avgCost对应赋值
	  creditCards: ""	creditCards对应赋值
	},
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertFoodtypes(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		String oldfoodTypesStr=CollectConvertUtils.convertStr(oldPoi.getString("foodtypes"));
		if(oldfoodTypesStr==null||oldfoodTypesStr.isEmpty()){
			newPoi.put("foodtypes", JSONNull.getInstance());
			return;
		}
		JSONObject oldFoodTypes = JSONObject.fromObject(oldfoodTypesStr);
		JSONObject newFoodTypes=new JSONObject();
		newFoodTypes.put("foodtype", CollectConvertUtils.convertStr(oldFoodTypes.getString("foodtype")));
		newFoodTypes.put("openHour", CollectConvertUtils.convertStr(oldFoodTypes.getString("openHour")));
		newFoodTypes.put("parking", oldFoodTypes.getInt("parking"));
		newFoodTypes.put("avgCost", oldFoodTypes.getInt("avgCost"));
		newFoodTypes.put("creditCards", CollectConvertUtils.convertStr(oldFoodTypes.getString("creditCards")));
		//rowid获取
		newFoodTypes.put("rowId", UuidUtils.genUuid());
		if(lifecycle!=3){
			List<IxPoiRestaurant> restList = oldPoiObj.getIxPoiRestaurants();
			if(restList!=null&&restList.size()>0){
				IxPoiRestaurant rest=restList.get(0);
				newFoodTypes.put("rowId", rest.getRowId());
				}
			}
		newPoi.put("foodtypes", newFoodTypes);
	}
	/**
	 * 线上：contacts: "[]",（或者"contacts":"[{\"number\":\"021-55277040\",\"linkman\":null,\"type\":1,
	 * \"weChatUrl\":null,\"priority\":1}]"）	
	 * 一体化：contacts: [],（或者"contacts":[{"linkman":"","number":"010-95105888","priority":1,"type":1,
	 * "rowId":"48F7237652530C9CE0530100007F0FE7"}]）	
	 * 原则：字符串转json。Linkman为null，赋值"";number，type，priority直接赋值；weChatUrl不处理;rowId,全字段查询IX_POI_CONTACT表，有非删除记录，则赋rowId，否则生成rowId	 
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertContacts(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		JSONArray oldContacts = JSONArray.fromObject(oldPoi.getString("contacts"));
		JSONArray newContacts=new JSONArray();
		if(oldContacts==null||oldContacts.size()==0){
			newPoi.put("contacts", newContacts);
			return;
		}		
		for(Object oldContact:oldContacts){
			JSONObject newContactJson = new JSONObject();
			JSONObject oldContactJson = JSONObject.fromObject(oldContact);
			newContactJson.put("number", oldContactJson.getString("number"));
			newContactJson.put("type", oldContactJson.getInt("type"));
			newContactJson.put("priority", oldContactJson.getInt("priority"));
			String linkman=oldContactJson.getString("linkman");
			if(linkman==null||linkman.isEmpty()){
				newContactJson.put("linkman", "");}
			else{newContactJson.put("linkman", linkman);}
			
			//rowid获取
			newContactJson.put("rowId", UuidUtils.genUuid());
			if(lifecycle!=3){
				List<IxPoiContact> contactList = oldPoiObj.getIxPoiContacts();
				if(contactList!=null&&contactList.size()>0){
					for(IxPoiContact poiContact:contactList){
						if(CollectConvertUtils.compareStr(poiContact.getContact(),oldContactJson.getString("number"))
								&&poiContact.getContactType()==oldContactJson.getInt("type")
								&&poiContact.getPriority()==oldContactJson.getInt("priority")){
							newContactJson.put("rowId", poiContact.getRowId());
							break;
						}
					}
				}
			}
			newContacts.add(newContactJson);
		}		
		newPoi.put("contacts", newContacts);
	}
	/**
	 * 线上：relateChildren: "[]",（或者"relateChildren":"[{\"childFid\":\"00366620161022153619\",\"type\":2,
		 \"childPid\":94592396,\"childRowkey\":null}）	
		 一体化：relateChildren: [],（或者[{"childFid":"00000220170306114342","childPid":0,"type":2,
		 "rowId":"58C6E1A005104DC1B152205EBD810FBA"}]）	
		 原则：字符串转json；若为[]，则直接赋值；childFid，type直接赋值；childPid,通过childFid在ix_poi表中查找，
		 若有则赋对应pid，同时若当前记录的poi的pid有值，则通过父，子共同查找非删除关系，若有，则赋对应rowId,否则生成新rowId
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 * @throws Exception 
	 */
	private static void convertRelateChildren(Connection conn,int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) throws Exception {
		JSONArray oldChilds = JSONArray.fromObject(oldPoi.getString("relateChildren"));
		JSONArray newChilds=new JSONArray();
		if(oldChilds==null||oldChilds.size()==0){
			newPoi.put("relateChildren", newChilds);
			return;
		}		
		for(Object oldChild:oldChilds){
			JSONObject newChildJson = new JSONObject();
			JSONObject oldChildJson = JSONObject.fromObject(oldChild);
			newChildJson.put("childFid", oldChildJson.getString("childFid"));
			newChildJson.put("type", oldChildJson.getInt("type"));
			IxPoiObj childPoiObj = loadPoiByFid(conn, oldChildJson.getString("childFid"),true);
			if(childPoiObj==null){
				newChildJson.put("childPid", 0);
			}else{
				newChildJson.put("childPid",childPoiObj.objPid());
			}
			//rowid获取
			newChildJson.put("rowId", UuidUtils.genUuid());
			if(lifecycle!=3){
				List<IxPoiChildren> childList = oldPoiObj.getIxPoiChildrens();
				if(childList!=null&&childList.size()>0){
					for(IxPoiChildren poiChild:childList){
						if(poiChild.getChildPoiPid()==newChildJson.getLong("childPid")
								&&poiChild.getRelationType()==newChildJson.getInt("type")){
							newChildJson.put("rowId", poiChild.getRowId());
							break;
						}
					}
				}
			}
			newChilds.add(newChildJson);
		}		
		newPoi.put("relateChildren", newChilds);
	}
	/**
	 * 根据fid获取poi对象
	 * @param conn
	 * @param fid
	 * @return
	 * @throws Exception
	 */
	public static IxPoiObj loadPoiByFid(Connection conn,String fid,boolean isMainOnly) throws Exception{
		String objType = "IX_POI";
		String colName = "POI_NUM";
		String colValue = fid;
		boolean isLock = false;
		BasicObj obj = ObjSelector.selectBySpecColumn(conn, objType, null,isMainOnly, colName,colValue, isLock);
		return (IxPoiObj) obj;
	}
	/**
	 * 转pid
	 * 线上 fid: "0523081102MFY00291",	
	 *一体化fid: "00166420170303213016",	原则：直接赋值
	 *		pid: 0,	原则：根据fid查IX_POI表，若有则，更新pid；否则为0
	 * @param lifecycle
	 * @param newPoi
	 * @param oldPoi
	 * @param oldPoiObj
	 */
	private static void convertPid(int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		if(lifecycle!=3){
			newPoi.put("pid", oldPoiObj.objPid());
		}else{
			newPoi.put("pid",0);
		}
	}
}
>>>>>>> origin/master
