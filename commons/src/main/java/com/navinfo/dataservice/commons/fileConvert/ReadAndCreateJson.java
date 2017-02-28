package com.navinfo.dataservice.commons.fileConvert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ReadAndCreateJson {
	private static Logger log = Logger.getLogger(ReadAndCreateJson.class);

	public ReadAndCreateJson() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @Title: readCityJson2List
	 * @Description: 处理cityJson 
	 * @param ja
	 * @return
	 * @throws ParseException  JSONArray
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年2月24日 下午6:55:43 
	 */
	public static JSONArray readCityJson2List(JSONArray ja) throws ParseException {
		JSONArray meshArray = new JSONArray();
		Iterator<Object> it = ja.iterator();
		
		//记录已经被占用的meshId
//		Set<String> usedmeshIds = new HashSet<String>();
		
		Map<String,City> citys = new HashMap<String,City>();//城市名,city
		
		
        while (it.hasNext()) {
            JSONObject ob = (JSONObject) it.next();
            if( ob.getString("city").equals("北京市") || ob.getString("city").equals("天津市")
            		|| ob.getString("city").equals("承德市")){
	
            //
			String geoWkt=ob.getString("geometry");
			Geometry jtsGeo = JtsGeometryFactory.read(geoWkt);
			Set<String> meshIds = CompGeometryUtil.geoToMeshesWithoutBreak(jtsGeo);
			
//			meshIds.removeAll(usedmeshIds);//去除已经分配的meshId
//			usedmeshIds.addAll(meshIds);//将已分配的meshId 放入已占用set
			if(meshIds != null && meshIds.size() > 0){
				String provinceVal = ob.getString("province");
	            String cityVal = ob.getString("city");
	            String codeVal = ob.getString("code");
	            String nameVal = ob.getString("name");
	            String job1Val = ob.getString("job1");
	            String areaVal = ob.getString("area");
	            String countyVal = ob.getString("county");
	           // String geometry = "";
	            String workPropertyVal = ob.getString("workProperty");
	            String job2Val = ob.getString("job2");      
				
				if(citys.containsKey(cityVal)){
					citys.get(cityVal).getMeshIds().addAll(meshIds);
//					String  areaValOld= citys.get(cityVal).getArea();
//					if(areaValOld != null && StringUtils.isNotEmpty(areaValOld) && !areaValOld.contains(areaVal)){
//						citys.get(cityVal).setArea(areaValOld+","+areaVal);
//					}
//					String  codeValOld= citys.get(cityVal).getBlockCode();
//					if(codeValOld != null && StringUtils.isNotEmpty(codeValOld) && !codeValOld.contains(codeVal)){
//						citys.get(cityVal).setBlockCode(codeValOld+","+codeVal);
//					}
//					String  countyValOld= citys.get(cityVal).getCounty();
//					if(countyValOld != null && StringUtils.isNotEmpty(countyValOld) && !countyValOld.contains(countyVal)){
//						citys.get(cityVal).setCounty(countyValOld+","+countyVal);
//					}
//					String  job1ValOld= citys.get(cityVal).getJob1();
//					if(job1ValOld != null && StringUtils.isNotEmpty(job1ValOld) && !job1ValOld.contains(job1Val)){
//						citys.get(cityVal).setJob1(job1ValOld+","+job1Val);
//					}
//					String  job2ValOld= citys.get(cityVal).getJob2();
//					if(job2ValOld != null && StringUtils.isNotEmpty(job2ValOld) && !job2ValOld.contains(job2Val)){
//						citys.get(job2Val).setJob2(job2ValOld+","+job2Val);
//					}
//					String  nameValOld= citys.get(cityVal).getName();
//					if(nameValOld != null && StringUtils.isNotEmpty(nameValOld) && !nameValOld.contains(nameVal)){
//						citys.get(cityVal).setName(nameValOld+","+nameVal);
//					}
//					String  workPropertyValOld= citys.get(cityVal).getWorkProperty();
//					if(workPropertyValOld != null && StringUtils.isNotEmpty(workPropertyValOld) && !workPropertyValOld.contains(workPropertyVal)){
//						citys.get(cityVal).setWorkProperty(workPropertyValOld+","+workPropertyVal);
//					}
//					String  provinceValOld= citys.get(cityVal).getProvince();
//					if(provinceValOld != null && StringUtils.isNotEmpty(provinceValOld) && !provinceValOld.contains(provinceVal)){
//						citys.get(cityVal).setProvince(provinceValOld+","+provinceVal);
//					}
				}else{
					//cityMeshMap.put(feaName, meshIds);
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
        }
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
	        	    
	        	    meshArray.add(meshObj);
	       		 }
				//*****************
		  }
			
        }  
		return meshArray;
	}
	/**
	 * @Title: readBlockJson2List
	 * @Description: 处理block Json
	 * @param ja
	 * @return
	 * @throws Exception  JSONArray
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年2月24日 下午6:56:10 
	 */
	public static JSONArray readBlockJson2List(JSONArray ja) throws Exception {
		JSONArray gridArray = new JSONArray();
		Iterator<Object> it = ja.iterator();
		
		//记录已经被占用的grids
		Set<String> usedGridIds = new HashSet<String>();
		
		Map<String,Block> blocks = new HashMap<String,Block>();//blockCode,Block
		
		
        while (it.hasNext()) {
            JSONObject ob = (JSONObject) it.next();
            if( ob.getString("city").equals("北京市") || ob.getString("city").equals("天津市")
            		|| ob.getString("city").equals("承德市")){
            //
			String geoWkt=ob.getString("geometry");
			Geometry jtsGeo = JtsGeometryFactory.read(geoWkt);
			Set<String> grids = CompGeometryUtil.geo2GridsWithoutBreak(jtsGeo);
			
			grids.removeAll(usedGridIds);//去除已经分配的meshId
			usedGridIds.addAll(grids);//将已分配的meshId 放入已占用set
			if(grids != null && grids.size() > 0){
				String provinceVal = ob.getString("province");
	            String cityVal = ob.getString("city");
	            String codeVal = ob.getString("code");
	            String nameVal = ob.getString("name");
	            String job1Val = ob.getString("job1");
	            String areaVal = ob.getString("area");
	            String countyVal = ob.getString("county");
	            String blockCodeVal = ob.getString("code");
	           // String geometry = "";
	            String workPropertyVal = ob.getString("workProperty");
	            String job2Val = ob.getString("job2");      
				
				if(blocks.containsKey(blockCodeVal)){
					blocks.get(blockCodeVal).getGridIds().addAll(grids);
					
					
//					String  areaValOld= blocks.get(blockCodeVal).getArea();
//					if(areaValOld != null && StringUtils.isNotEmpty(areaValOld) && !areaValOld.contains(areaVal)){
//						blocks.get(blockCodeVal).setArea(areaValOld+","+areaVal);
//					}
//					String  codeValOld= blocks.get(blockCodeVal).getBlockCode();
//					if(codeValOld != null && StringUtils.isNotEmpty(codeValOld) && !codeValOld.contains(codeVal)){
//						blocks.get(blockCodeVal).setBlockCode(codeValOld+","+codeVal);
//					}
//					String  countyValOld= blocks.get(blockCodeVal).getCounty();
//					if(countyValOld != null && StringUtils.isNotEmpty(countyValOld) && !countyValOld.contains(countyVal)){
//						blocks.get(blockCodeVal).setCounty(countyValOld+","+countyVal);
//					}
//					String  job1ValOld= blocks.get(blockCodeVal).getJob1();
//					if(job1ValOld != null && StringUtils.isNotEmpty(job1ValOld) && !job1ValOld.contains(job1Val)){
//						blocks.get(blockCodeVal).setJob1(job1ValOld+","+job1Val);
//					}
//					String  job2ValOld= blocks.get(blockCodeVal).getJob2();
//					if(job2ValOld != null && StringUtils.isNotEmpty(job2ValOld) && !job2ValOld.contains(job2Val)){
//						blocks.get(job2Val).setJob2(job2ValOld+","+job2Val);
//					}
//					String  nameValOld= blocks.get(blockCodeVal).getName();
//					if(nameValOld != null && StringUtils.isNotEmpty(nameValOld) && !nameValOld.contains(nameVal)){
//						blocks.get(blockCodeVal).setName(nameValOld+","+nameVal);
//					}
//					String  workPropertyValOld= blocks.get(blockCodeVal).getWorkProperty();
//					if(workPropertyValOld != null && StringUtils.isNotEmpty(workPropertyValOld) && !workPropertyValOld.contains(workPropertyVal)){
//						blocks.get(blockCodeVal).setWorkProperty(workPropertyValOld+","+workPropertyVal);
//					}
//					String  provinceValOld= blocks.get(blockCodeVal).getProvince();
//					if(provinceValOld != null && StringUtils.isNotEmpty(provinceValOld) && !provinceValOld.contains(provinceVal)){
//						blocks.get(blockCodeVal).setProvince(provinceValOld+","+provinceVal);
//					}
					
					
					
				}else{
					//cityMeshMap.put(feaName, meshIds);
					Block block = new Block();
					block.setName(nameVal);
					block.setCity(cityVal);;
					block.setArea(areaVal);
					block.setCounty(countyVal);
					block.setBlockCode(codeVal);
					block.setProvince(provinceVal);
					block.setGridIds(grids);
					block.setJob1(job1Val);
					block.setJob2(job2Val);
					block.setWorkProperty(workPropertyVal);
					blocks.put(blockCodeVal, block);
				}
			}
            
        	}	 
        }
        if(blocks != null && blocks.size() > 0){
			for(String blockcode : blocks.keySet()){
				Block blockObj = blocks.get(blockcode);
				//添加数据到 JsonArray
				String wktStr = null;
	       		 for(String gridId : blockObj.getGridIds()){
	       			JSONObject gridObj = new JSONObject();
	       			gridObj.put("city", blockObj.getCity());
	       			gridObj.put("area", blockObj.getArea());
	       			gridObj.put("code", blockObj.getBlockCode());
	       			gridObj.put("county", blockObj.getCounty());
	       			gridObj.put("name", blockObj.getName());
	       			gridObj.put("province", blockObj.getProvince());
	       			gridObj.put("gridId", gridId);
	       			gridObj.put("job1", blockObj.getJob1());
	       			gridObj.put("job2", blockObj.getJob2());
	       			gridObj.put("workProperty", blockObj.getWorkProperty());
	       			//meshObj.put("", cityObj.get);
	       			
	        	    if(gridId != null && StringUtils.isNotEmpty(gridId)){
	        	    	wktStr = GridUtils.grid2Wkt(gridId); 
	       	        	System.out.println("gridId: "+gridId+" wktStr: "+wktStr);
	      	        }
	        	    gridObj.put("geometry", wktStr);
	        	    
	        	    gridArray.add(gridObj);
	       		 }
				//*****************
		  }
			
        } 
		return gridArray;
	}
	

}
