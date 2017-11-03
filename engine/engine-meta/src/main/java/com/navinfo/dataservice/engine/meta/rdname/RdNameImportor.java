package com.navinfo.dataservice.engine.meta.rdname;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.*;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.navinfo.navicommons.geo.computation.JtsGeometryConvertor;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSON;
import net.sf.json.util.JSONUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.alibaba.druid.sql.visitor.functions.Substring;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.engine.meta.mesh.MeshSelector;
import com.navinfo.navicommons.geo.computation.MeshUtils;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/**
 * @ClassName: RdNameImportor.java
 * @author y
 * @date 2016-6-24下午3:56:36
 * @Description: 道路名导入元数据库
 * 
 */
public class RdNameImportor {

	private static final String[] ignoreNames = { "N", "n", "NO", "no", "No",
			"无道路名", "无" };

	private static final String DEFAULT_LANG_CODE = "CHI";
	
	private Logger log = Logger.getLogger(RdNameImportor.class);

	/**
	 * @Description:道路名导入
	 * @param name
	 * @param gLocation
	 * @param rowkey
	 * @author: y
	 * @throws Exception
	 * @time:2016-6-28 下午3:12:21
	 */
	public void importName(String name, JSONObject gLocation,
			String rowkey, String sourceType) throws Exception {
		// 不满足条件的名称，不入库
		if (isIgnoreName(name.toString())) {
			return;
		}
		// 全角转半角(先转换成半角)
		name = ExcelReader.f2h(name);
		name = name.replace("#", "号");
		// 将罗马字符转为阿拉伯数字
		name = RomanUtils.replaceAllRoman2RrabicNum(name);
		JSONObject nameObj = new JSONObject();
		// 入库前转换成全角
		name = ExcelReader.h2f(name);
		nameObj.put("name", name);
		// 判断RD_name中是否已存在,不存在则新增并拆分；存在则不处理
		Geometry geoLocation = GeoTranslator.geojson2Jts(gLocation);
        if (!geoLocation.getGeometryType().equals(GeometryTypeName.POINT)) {//面
            geoLocation = JtsGeometryFactory.createLineString(geoLocation.getCoordinates());
        }
        String meshes[] = CompGeometryUtil.geo2MeshesWithoutBreak(geoLocation);
        RdNameSelector nameSelector = new RdNameSelector();
        MeshSelector meshSelector = new MeshSelector();
        int roadType = 0;
        for (String meshId : meshes) {
            List<Integer> adminIdList = null;
            if(sourceType.equals("1407") || sourceType.equals("8006")) {
                adminIdList = new ArrayList<>();
                adminIdList.add(214);
                if(sourceType.equals("1407")) {
                    roadType = 4;
                }else {
                    roadType = 1;
                }
            } else {
                adminIdList = meshSelector.getAdminIdByMesh(meshId);
            }
            int existsAdminId = nameSelector.isNameExists(name, adminIdList, sourceType);
            if (existsAdminId != 0) {//该图幅所在行政区划name已存在
                log.warn(name+"已存在");
            }else {//该图幅所在行政区划name不存在
				//TODO 暂时保存一个
                String srcResume ="\"tips\":\""+rowkey+"\"";
                //city字段（20170912）：根据tips几何（显示坐标）所在的图幅号与SC_PARTITION_MESHLIST关联，然后获得“CITY”字段的值，赋值给RD_NAME表的“CITY”，如果有多个值时，使用半角的“|”。如“A|B”
                StringBuffer city=new StringBuffer("");
                List<String> cityList  =meshSelector.getCityListByMesh(meshId);
                int index=0;
                if(cityList!=null&&cityList.size()!=0){
                	for (String cityStr : cityList) {
                		if(index!=0){
                			city.append("|"+cityStr);
                		}else{
                			city.append(cityStr);
                		}
                		index++;
    				}
                }
                
                
                insertNameAndTeilen(name, DEFAULT_LANG_CODE, adminIdList.get(0), srcResume, roadType,city.toString());
            }
        }

	}

	/**
	 * @Description:新增道路名，并拆分
	 * @author: y
	 * @param srcResume
	 * @param adminId
	 * @param langCode
	 * @param name
	 * @throws Exception
	 * @time:2016-6-28 下午3:49:58
	 */
	private void insertNameAndTeilen(String name, String langCode, int adminId,
			String srcResume, int roadType,String city) throws Exception {
		
		Connection conn=null;
		try{
		
		conn = DBConnector.getInstance().getMetaConnection();
		
		RdNameOperation operation = new RdNameOperation(conn);
		
		RdName rdName = new RdName();

		rdName.setName(name);
		rdName.setLangCode(langCode);

		rdName.setSrcResume(srcResume);
		rdName.setSrcFlag(0);
		rdName.setRoadType(roadType);
		rdName.setAdminId(adminId);
		rdName.setCodeType(0);
		rdName.setSplitFlag(0);
		rdName.setProcessFlag(0);
		rdName.setCity(city); //20170912修改

		if (name.contains("高速公路")
				|| (name.contains("高架") && !name.endsWith("高架桥"))
				|| name.contains("快速") ||isAandNuber(name) ) {
			rdName.setRoadType(1);//高速
			rdName.setAdminId(214);
		}

		// 新增一条道路名
		RdName rdNameNew = operation.saveName(rdName);
		// 对道路名拆分
		RdNameTeilen teilen = new RdNameTeilen(conn);
		teilen.teilenName(rdNameNew.getNameId(), rdNameNew.getNameGroupid(),
				rdNameNew.getLangCode(), rdNameNew.getRoadType(),rdNameNew.getuFields());
		
		}catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		}
		finally{
			DbUtils.commitAndCloseQuietly(conn);
		}

	}

	/**
	 * @Description:判断名称是不是A+数字
	 * @param name
	 * @return
	 * @author: y
	 * @time:2016-7-2 下午3:53:35
	 */
	private boolean isAandNuber(String name) {
		String regex = "^A[0-9]+$"; 
		Pattern pattern = Pattern.compile(regex);
		//需要转换成半角在匹配
		Matcher matcher=pattern.matcher(ExcelReader.f2h(name));
		return matcher.matches();
	}

	/**
	 * 道路名入元数据库
	 */
	/*
	 * 
	 * public void importToRdName(Map<String, JSONObject> roadNameTips) {
	 * Set<Entry<String, JSONObject>> set = roadNameTips.entrySet(); Map<String,
	 * JSONObject> nameResult = new HashMap<String, JSONObject>();
	 * 
	 * Iterator<Entry<String, JSONObject>> it = set.iterator();
	 * 
	 * while (it.hasNext()) { String rowkey = "";
	 * 
	 * Entry<String, JSONObject> en = it.next();
	 * 
	 * rowkey = en.getKey(); JSONObject nameTipJson = en.getValue(); JSONObject
	 * deep = JSONObject.fromObject(nameTipJson.get("deep"));
	 * 
	 * // 坐标 JSONObject geo = deep.getJSONObject("geo"); JSONArray jaCoords =
	 * geo.getJSONArray("coordinates"); // 坐标转图幅 String [] meshes =
	 * MeshUtils.point2Meshes(jaCoords.getDouble(0), jaCoords.getDouble(1));
	 * 
	 * // 名称 JSONArray names = geo.getJSONArray("n_array"); for (Object name :
	 * names) { if (name != null && StringUtils.isNotEmpty(name.toString())) {
	 * if (!isIgnoreName(name.toString())) { putName(nameResult, rowkey,
	 * name.toString(),meshes); } } }
	 * 
	 * } }
	 */

	/**
	 * @Description:名称处理
	 * @param nameResult
	 * @param rowkey
	 * @param name
	 * @return:
	 * @author: y
	 * @param meshes
	 * @throws Exception
	 * @time:2016-6-24 下午3:51:06
	 */
	/*
	 * private void putName(Map<String, JSONObject> nameResult, String rowkey,
	 * String name, String[] meshes) throws Exception { //全角转半角(先转换成半角)
	 * name=ExcelReader.f2h(name); name=name.replace("#", "号"); //将罗马字符转为阿拉伯数字
	 * name=RomanUtils.replaceAllRoman2RrabicNum(name); JSONObject nameObj=new
	 * JSONObject(); //入库前转换成全角 name=ExcelReader.h2f(name);
	 * nameObj.put("name",name); //判断RD_name中是否已存在
	 * if(!exitsInMeshAdmin(meshes,name)){ nameResult.put(rowkey, nameObj); }
	 * 
	 * }
	 */


	/**
	 * @Description:判断是否是不入库的名称
	 * @param name
	 * @return
	 * @return:
	 * @author: y
	 * @time:2016-6-24 下午3:45:12
	 */
	private boolean isIgnoreName(String name) {
		for (String rn : ignoreNames) {
			if (rn.equals(name) || ExcelReader.h2f(rn).equals(name)) {
				log.info("name:"+name+"不满足入库名称条件");
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @Title: importRdNameFromWeb
	 * @Description: 增加参数 subtaskId
	 * @param params
	 * @param subtaskId
	 * @param userId 
	 * @return
	 * @throws Exception  JSONObject
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月14日 下午6:14:57 
	 */
	public JSONObject importRdNameFromWeb(JSONObject params, int subtaskId, long userId) throws Exception {
		JSONObject result = new JSONObject();
		
		Connection conn = null;
		
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			
			RdNameSelector selector = new RdNameSelector(conn);
			
			String currentDate = com.navinfo.dataservice.commons.util.StringUtils.getCurrentTime();	
			RdName rdName = Json2Obj(params);
			rdName.setuFields("{\"userId\":"+userId+",\"u_date\":\""+currentDate+"\"}");
			log.info("{\"userId\":"+userId+",\"u_date\":\""+currentDate+"\"}");
			log.debug("rdName:"+rdName);
			// 判断是否存在重复name
			JSONObject rdNameExists = selector.checkRdNameExists(rdName);
			
			// 存在重复name
			if (rdNameExists.size() > 0) {
				result.put("flag", -1);
				result.put("data", rdNameExists);
				return result;
			}
			
			RdNameOperation operation = new RdNameOperation(conn);
			//web新增rd_name是，根据当前子任务的id， 赋值给rd_name.src_resume 格式 "task":3443434
			if(rdName.getSrcResume() == null || StringUtils.isEmpty(rdName.getSrcResume()) ){
				if(subtaskId > 0){
					rdName.setSrcResume("\"task\":"+subtaskId);
				}
			}
			// 新增或更新一条道路名
			RdName rdNameNew = operation.saveOrUpdate(rdName);
			//处理完数据自动进行拆分
			//**********zl 2017.05.08***
			if(rdNameNew != null && rdNameNew.getSplitFlag() != 1){
				log.info("auto RdNameTeilen ");
				RdNameTeilen teilen = new RdNameTeilen(conn);
				teilen.teilenName(rdNameNew.getNameId(), rdNameNew.getNameGroupid(),rdNameNew.getLangCode(),rdNameNew.getRoadType(),rdNameNew.getuFields());
			}
			//**************************
			
			JSONObject json = JSONObject.fromObject(rdNameNew);
			result.put("flag", 1);
			result.put("data", json);
			
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 将前台的json转为对象
	 * @author wangdongbin
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	private RdName Json2Obj(JSONObject params) throws Exception {
		RdName rdName = new RdName();
		
		try {
			Iterator keys = params.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				
				if (JSONNull.getInstance() ==  params.get(key)) {
					continue;
				}
				
				try {
					Field f = rdName.getClass().getDeclaredField(key);
					
					f.setAccessible(true);

					
					
					f.set(rdName, params.get(key));
				} catch (NoSuchFieldException e)  {
					continue;
				}
			}
			//判断 当道路名为英文是不转全角
			if(rdName.getLangCode().equals("ENG")){
				rdName.setName(rdName.getName());
			}else{
				rdName.setName(ExcelReader.h2f(rdName.getName()));
			}
			
			
			return rdName;
		} catch (Exception e) {
			throw e;
		}
		
	}
	
	public static void main(String[] args) {
		String a = "\"tips\":\"reuwoireuwir83erewr343\"";
	//	a.substring();
        String s = "{\"type\":\"MultiLineString\",\"coordinates\":[[[116.36948,40.1675],[116.37251,40.16732]],[[116.37368,40.16667],[116.37436,40.16565]]]}";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("g_location", s);
        Geometry geoLocation = GeoTranslator.geojson2Jts(jsonObject.getJSONObject("g_location"));
        Geometry geoLocationLine = JtsGeometryFactory.createLineString(geoLocation.getCoordinates());
		System.out.println(geoLocationLine.toString());

        s = "{\"coordinates\":[[[116.49546,40.13406],[116.49546,40.13406],[116.49539,40.13407],[116.49538,40.13408],[116.49534,40.13411],[116.49533,40.13411],[116.4953,40.13419],[116.4953,40.1342],[116.49531,40.13425],[116.49531,40.13426],[116.49534,40.13431],[116.49535,40.13432],[116.4954,40.13435],[116.49541,40.13435],[116.49548,40.13437],[116.49548,40.13437],[116.49556,40.13437],[116.49557,40.13437],[116.49565,40.13433],[116.49566,40.13432],[116.4957,40.13425],[116.4957,40.13424],[116.4957,40.13417],[116.49569,40.13416],[116.49565,40.13411],[116.49565,40.1341],[116.49555,40.13406],[116.49554,40.13406],[116.49546,40.13406]]],\"type\":\"Polygon\"}";
//        jsonObject = new JSONObject();
        jsonObject.put("g_location", s);
        geoLocation = GeoTranslator.geojson2Jts(jsonObject.getJSONObject("g_location"));
        geoLocationLine = JtsGeometryFactory.createLineString(geoLocation.getCoordinates());
        System.out.println(geoLocationLine.toString());
		
	}
}
