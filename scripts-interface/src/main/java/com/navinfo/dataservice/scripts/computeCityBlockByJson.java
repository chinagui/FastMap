package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.scripts.model.Block4Imp;
import com.navinfo.dataservice.scripts.model.City;
import com.navinfo.dataservice.scripts.model.City4Imp;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: ImportCityBlockByJson
 * @author xiaoxiaowen4127
 * @date 2017年2月26日
 * @Description: ImportCityBlockByJson.java
 */
public class computeCityBlockByJson {

	private static QueryRunner runner = new QueryRunner();
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			if(args==null||args.length!=1){
				System.out.println("ERROR:need args:raw block file");
				return;
			}

			String rawBlockFile = args[0];
			
			compute(rawBlockFile);

			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}
	}
	
	public static void compute(String rawBlockFile)throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		try{
			//read
			JSONArray rawBlocks = readJsonFile(rawBlockFile);
			//city
			
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
			throw e;
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	private static JSONArray readJsonFile(String rawBlockFile)throws Exception{
		Scanner scan = null;
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(rawBlockFile);
			scan = new Scanner(fis);
			JSONArray ja = new JSONArray();
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				if(line != null && StringUtils.isNotEmpty(line)){
					JSONObject json = JSONObject.fromObject(line);
					ja.add(json);
				}
			}
			return ja;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(fis!=null)fis.close();
			if(scan!=null)scan.close();
		}
	}
	private static JSONArray computeCitys(JSONArray rawBlocks)throws Exception{
		Map<String,City> citys = new HashMap<String,City>();
		for(Object obj:rawBlocks){
			JSONObject jo = (JSONObject)obj;

			String geoWkt=jo.getString("geometry");
			Geometry jtsGeo = JtsGeometryFactory.read(geoWkt);
			Set<String> meshIds = CompGeometryUtil.geoToMeshesWithoutBreak(jtsGeo);
			if(meshIds != null && meshIds.size() > 0){
				String provinceVal = jo.getString("province");
	            String cityVal = jo.getString("city");
	            String codeVal = jo.getString("code");
	            String nameVal = jo.getString("name");
	            String job1Val = jo.getString("job1");
	            String areaVal = jo.getString("area");
	            String countyVal = jo.getString("county");
	           // String geometry = "";
	            String workPropertyVal = jo.getString("workProperty");
	            String job2Val = jo.getString("job2");      
				
				if(citys.containsKey(cityVal)){
					citys.get(cityVal).getMeshIds().addAll(meshIds);
				}else{
					City city = new City();
					city.setName(nameVal);
					city.setCity(cityVal);;
					city.setArea(areaVal);
					city.setCounty(countyVal);
					city.setBlockCode(codeVal);
					city.setProvince(provinceVal);
					city.setMeshIds(meshIds);
					city.setJob1(job1Val);
					city.setJob2(job2Val);
					city.setWorkProperty(workPropertyVal);
					citys.put(cityVal, city);
				}
			}
		}
		JSONArray cityJsons = new JSONArray();
		//convert to json
		if(citys != null && citys.size() > 0){
			for(String cityName : citys.keySet()){
				City cityObj = citys.get(cityName);
				//添加数据到 JsonArray
				String wktStr = null;
				for(String meshId : cityObj.getMeshIds()){
					JSONObject meshObj = new JSONObject();
					meshObj.put("city", cityObj.getCity());
					meshObj.put("area", cityObj.getArea());
					meshObj.put("code", cityObj.getBlockCode());
					meshObj.put("county", cityObj.getCounty());
					meshObj.put("name", cityObj.getName());
					meshObj.put("province", cityObj.getProvince());
					meshObj.put("meshId", meshId);
					meshObj.put("job1", cityObj.getJob1());
					meshObj.put("job2", cityObj.getJob2());
					meshObj.put("workProperty", cityObj.getWorkProperty());
					//meshObj.put("", cityObj.get);
					
					if(meshId != null && StringUtils.isNotEmpty(meshId)){
						wktStr = MeshUtils.mesh2WKT(meshId); 
						System.out.println("meshId: "+meshId+" wktStr: "+wktStr);
					}
					meshObj.put("geometry", wktStr);
		    
				    cityJsons.add(meshObj);
				 }
		    }
        }
		return cityJsons;
	}
}
