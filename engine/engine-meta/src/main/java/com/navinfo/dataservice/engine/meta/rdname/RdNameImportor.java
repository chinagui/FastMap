package com.navinfo.dataservice.engine.meta.rdname;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.RomanUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
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
	 * @param longitude
	 * @param latitude
	 * @param rowkey
	 * @author: y
	 * @throws Exception
	 * @time:2016-6-28 下午3:12:21
	 */
	public void importName(String name, double longitude, double latitude,
			String rowkey) throws Exception {
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
		String meshes[] = MeshUtils.point2Meshes(longitude, latitude);
		//从元数据库查询adminId
		int adminId = new MeshSelector().getAdminIdByLocation(longitude,
				latitude);
		if (!exitsInMeshAdmin(meshes, name)) {
			String srcResume = rowkey + ","
					+ DateUtils.dateToString(new Date(), "yyyy-MM-dd");
			insertNameAndTeilen(name, DEFAULT_LANG_CODE, adminId, srcResume);
		}else{
			log.warn(name+"已存在");
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
			String srcResume) throws Exception {
		
		//***********************以下代码是路演环境临时使用*begin***********************
		Connection conn=null;
		
		try{
			
		
		String dbId= SystemConfigFactory.getSystemConfig().getValue("region_db_id");
		
		if(StringUtils.isEmpty(dbId)){
			throw new Exception("未配置region_db_id系统参数。");
		}

		//路演环境临时使用
		conn = DBConnector.getInstance().getConnectionById(Integer.parseInt(dbId));
		
		RdNameOperation operation = new RdNameOperation(conn);
		
		//***********************end ***********************
		
		//RdNameOperation operation = new RdNameOperation();
		
		RdName rdName = new RdName();

		rdName.setName(name);
		rdName.setLangCode(langCode);

		rdName.setSrcResume(srcResume);
		rdName.setSrcFlag(0);
		rdName.setRoadType(0);
		rdName.setAdminId(adminId);
		rdName.setCodeType(0);
		rdName.setSplitFlag(0);
		rdName.setProcessFlag(0);

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
				rdNameNew.getLangCode(), rdNameNew.getRoadType());
		
		}catch (Exception e) {
			throw e;
		}
		finally{
			DbUtils.closeQuietly(conn);
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
	 * @Description:查询name在图幅所在的行政区划中是否存在
	 * @param meshes
	 * @param name
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2016-6-28 下午1:49:01
	 */
	private boolean exitsInMeshAdmin(String[] meshes, String name)
			throws Exception {
		RdNameSelector nameSelector = new RdNameSelector();
		MeshSelector meshSelector = new MeshSelector();
		int adminId = 0;
		for (String meshId : meshes) {
			adminId = meshSelector.getAdminIdByMesh(meshId);
			adminId = nameSelector.isNameExists(name, adminId);
			if (adminId != 0) {
				return true;
			}
		}
		return false;
	}

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
				System.out.println("name:"+name+"不满足入库名称条件");
				return true;
			}
		}
		return false;
	}
	
	/**
	 * web端保存rdName
	 * @author wangdongbin
	 * @param rdName
	 * @return
	 * @throws Exception
	 */
	public JSONObject importRdNameFromWeb(JSONObject params,int dbId) throws Exception {
		JSONObject result = new JSONObject();
		
		Connection conn = null;
		
		try {
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			RdNameSelector selector = new RdNameSelector(conn);
			
			RdName rdName = Json2Obj(params);
			
			// 判断是否存在重复name
			JSONObject rdNameExists = selector.checkRdNameExists(rdName);
			
			// 存在重复name
			if (rdNameExists.size() > 0) {
				result.put("flag", -1);
				result.put("data", rdNameExists);
				return result;
			}
			
			RdNameOperation operation = new RdNameOperation(conn);
			// 新增或更新一条道路名
			RdName rdNameNew = operation.saveOrUpdate(rdName);
			
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
			
			rdName.setName(ExcelReader.h2f(rdName.getName()));
			
			return rdName;
		} catch (Exception e) {
			throw e;
		}
		
	}
}
