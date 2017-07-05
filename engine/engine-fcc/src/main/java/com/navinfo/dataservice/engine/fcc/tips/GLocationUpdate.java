package com.navinfo.dataservice.engine.fcc.tips;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: GLocationUpdate.java
 * @author y
 * @date 2017-7-4 下午3:10:48
 * @Description: TODO
 *  
 */
public class GLocationUpdate {
	
	/**
	 * @Description:更新起终点的g_location
	 * @param index
	 * @author: jiayong
	 * @time:2017-7-4 下午3:25:31
	 */
	public  static  JSONObject updateStartEndPointLocation(int index,JSONObject  json2Update,String sourceType,List<JSONObject> cutLines) {
		//范围线的，需要替换g_location.将旧的坐标替换为新的两条或者多条线的坐标
		if(index != -1 && ("1501".equals(sourceType) || "1507".equals(sourceType) || "1508".equals(sourceType)
				 || "1510".equals(sourceType) || "1511".equals(sourceType) || "1514".equals(sourceType))){
			
			JSONObject g_location = JSONObject.fromObject(json2Update.getString("g_location")) ;
			
			JSONArray  coordinates = g_location.getJSONArray("coordinates");
			
			JSONArray  coordinates_new = new JSONArray();//新的一个座标几何
			
			//替换原来测线的几何为 打断后的多条线的几何
			for (int j = 0; j < coordinates.size(); j++) {
				//旧测线所在位置
				if(j == index){
					
					 for (JSONObject json : cutLines) {
		    			 JSONObject newGeo = json.getJSONObject("g_location");
		    			
		    			 JSONObject cutLineCoordinates = newGeo.getJSONObject("coordinates"); //打断后的线的几何
		    			 
		    			 coordinates_new.add(cutLineCoordinates);
		    		}
					
				}else{
					
					coordinates_new.add(coordinates.get(j));
				}
			}
			
			g_location.put("coordinates", coordinates_new);
			
			json2Update.put("g_location", g_location);
			
		}
		
		return json2Update;
	}
	
	/**
	 * @Description:更新范围线的g_location
	 * @param index
	 * @author: y
	 * @time:2017-7-4 下午3:25:31
	 */
	public  static  JSONObject updateAreaLineLocation(int index,JSONObject  json2Update,String sourceType,List<JSONObject> cutLines) {
		//范围线的，需要替换g_location.将旧的坐标替换为新的两条或者多条线的坐标
		if(index!=-1&&("1601".equals(sourceType)||"1604".equals(sourceType))){
			
			JSONObject g_location=JSONObject.fromObject(json2Update.getString("g_location")) ;
			
			JSONArray  coordinates= g_location.getJSONArray("coordinates");
			
			JSONArray  coordinates_new=new JSONArray();//新的一个座标几何
			
			//替换原来测线的几何为 打断后的多条线的几何
			for (int j = 0; j < coordinates.size(); j++) {
				//旧测线所在位置
				if(j==index){
					
					 for (JSONObject json : cutLines) {
		    			 JSONObject newGeo = json.getJSONObject("g_location");
		    			
		    			 JSONObject cutLineCoordinates =newGeo.getJSONObject("coordinates"); //打断后的线的几何
		    			 
		    			 coordinates_new.add(cutLineCoordinates);
		    		}
					
				}else{
					
					coordinates_new.add(coordinates.get(j));
				}
			}
			
			g_location.put("coordinates", coordinates_new);
			
			
			json2Update.put("g_location", g_location);
			
		}
		
		return json2Update;
	}


}
