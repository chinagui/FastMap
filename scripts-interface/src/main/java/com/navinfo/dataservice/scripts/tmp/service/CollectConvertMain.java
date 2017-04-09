package com.navinfo.dataservice.scripts.tmp.service;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

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
	 * @param oldPoi
	 * @return
	 * @throws Exception 
	 */
	public static JSONObject convertMain(JSONObject oldPoi) throws Exception{
		JSONObject newPoi=new JSONObject();
		Connection conn = null;
		try{
			//lifecycle: "2"	t_lifecycle: 3,	字符串转数字后赋值
			//0 无； 1 删除；2 更新；3 新增；
			int lifecycle=Integer.valueOf(oldPoi.getString("lifecycle"));
			newPoi.put("t_lifecycle", lifecycle);
			/*
			 * 线上 geometry: "POINT (120.1733200000000039 32.3584900000000033)"
			 * 一体化geometry: "POINT (116.47958 40.01322)",	
			 * 原则：四舍五入截取小数点后5位赋值
			 */
			Geometry oldGeo = new WKTReader().read(oldPoi.getString("geometry"));
			String newGeoStr=GeoTranslator.jts2Wkt(oldGeo,0.00001, 5);
			newPoi.put("geometry", newGeoStr);
			//获取poi所在大区连接
			int dbId=CollectConvertUtils.getDbidByGeo(newGeoStr);		
			conn = DBConnector.getInstance().getConnectionById(dbId);
			/*线上 fid: "0523081102MFY00291",	
			 *一体化fid: "00166420170303213016",	原则：直接赋值
			 *		pid: 0,	原则：根据fid查IX_POI表，若有则，更新pid；否则为0
		     */
			String fid=oldPoi.getString("fid");
			newPoi.put("fid", fid);
			IxPoiObj oldPoiObj =null;
			if(lifecycle!=3){
				oldPoiObj = loadPoiByFid(conn, fid);
				if(oldPoiObj==null){
					throw new Exception("数据fid="+fid+"在大区"+dbId+"中不存在");
				}
			}
			convertPid(lifecycle,newPoi,oldPoi,oldPoiObj);
			/*线上meshid: "486041",	一体化meshid: 0,	原则：字符串转数字后赋值*/
			newPoi.put("meshid", Integer.valueOf(oldPoi.getString("meshid")));
			//name: "泰州市顺旺铸造有限公司",	name: "Ｉｉｉｉ",	"null"转成""；否则，直接赋值
			newPoi.put("name", CollectConvertUtils.convertStr(oldPoi.getString("name")));
			//kindCode: "220100",	kindCode: "110101",	直接赋值
			newPoi.put("kindCode", CollectConvertUtils.convertStr(oldPoi.getString("kindCode")));
			//vipFlag: "null",	vipFlag: "",	"null"转成""；否则，直接赋值
			newPoi.put("vipFlag", CollectConvertUtils.convertStr(oldPoi.getString("vipFlag")));
			//truck: "0",	truck: 0,	字符串转数字后赋值
			newPoi.put("truck", Integer.valueOf(oldPoi.getString("truck")));
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
			newPoi.put("open24H", Integer.valueOf(oldPoi.getString("open24H")));
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
//sportsVenues: "null",(包含字段buildingType,sports)	sportsVenues: "",	"null"转成""；否则转json，将buildingType赋给sportsVenues
//chargingStation: "null",	chargingStation: null,(或者type,changeBrands,changeOpenType,servicePro,chargingNum,openHour,parkingFees,parkingInfo,availableState,rowId)	取oracle库中对应记录生成，没有则赋值""
//chargingPole: ""	chargingPole: [],(或者groupId,acdc,plugType,power,voltage,current,mode,count,plugNum,prices,openType,availableState,manufacturer,factoryNum,plotNum,productNum,parkingNum,floor,locationType,payment,rowId)	取oracle库中对应记录生成，没有则赋值""
//gasStation: "null",（包含字段fuelType,oilType,egType,mgType,payment,service,servicePro,openHour）	gasStation: null,（包含字段fuelType,oilType,egType,mgType,payment,service,servicePro,openHour,rowId）	字符串转json，直接赋值；然后全字段在IX_POI_GASSTATION中查找，存在非删除记录，则赋rowId，否则生成rowId
//indoor: "{\"floor\":null,\"type\":0,\"open\":1}",	indoor: {type: 0,floor: ""}	indoor的字符串转json格式；type直接赋值；floor，null转"",否则直接赋值
//brands: "[]",	chain: "",（"chain":"304D"）	brands的字符串转json格式；为[]，赋值"";若有值，取第一个赋值chain
//rawFields: "null",	rawFields: "",	"null"转成""；否则，直接赋值
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
	/*sameFid: "",	"查询库里的同一关系，找到同一关系poi赋值对应的fid（同一关系都是两两一组）;
若为删除对象(t_lifecycle: 1),则
1.不需要补足同一关系.
2.查找是否有同一关系，若有，则获取其同一关系poi的fid，查看txt中是否有该poi，若有同一关系poi，则在处理此同一关系poi时，需将sameFid赋值"""""
*/
//mergeDate: "20161110214719",	t_operateDate: "20170303213109",	直接赋值
//sourceName: "Android",	sourceName: "Android"	直接赋值
		
		return null;
		}catch (Exception e) {
			DbUtils.commitAndCloseQuietly(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
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
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
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
	 */
	private static void convertRelateChildren(Connection conn,int lifecycle, JSONObject newPoi,
			JSONObject oldPoi, IxPoiObj oldPoiObj) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 根据fid获取poi对象
	 * @param conn
	 * @param fid
	 * @return
	 * @throws Exception
	 */
	public static IxPoiObj loadPoiByFid(Connection conn,String fid) throws Exception{
		String objType = "IX_POI";
		String colName = "POI_NUM";
		String colValue = fid;
		boolean isLock = false;
		BasicObj obj = ObjSelector.selectBySpecColumn(conn, objType, null,false, colName,colValue, isLock);
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
