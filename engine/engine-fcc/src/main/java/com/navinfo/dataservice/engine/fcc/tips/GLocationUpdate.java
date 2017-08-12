package com.navinfo.dataservice.engine.fcc.tips;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.navicommons.geo.computation.GeometryTypeName;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

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
	public  static  TipsDao updateStartEndPointLocation(int index,TipsDao json2Update,String sourceType,List<TipsDao> cutLines) {
		//起终点的，需要替换g_location.将旧的坐标替换为新的两条或者多条线的坐标
			
		JSONObject g_location = JSONObject.fromObject(json2Update.getG_location()) ;
		
		JSONArray  coordinates = g_location.getJSONArray("coordinates");
		
		JSONArray  coordinates_new = new JSONArray();//新的一个座标几何
		
		//替换原来测线的几何为 打断后的多条线的几何
		for (int j = 0; j < coordinates.size(); j++) {
			//旧测线所在位置
			if(j == index){
				
				 for (TipsDao json : cutLines) {
	    			 JSONObject newGeo = JSONObject.fromObject(json.getG_location());
	    			
	    			 JSONArray cutLineCoordinates = newGeo.getJSONArray("coordinates"); //打断后的线的几何
	    			 
	    			 coordinates_new.add(cutLineCoordinates);
	    			 
	    		}
				
			}else{
				
				coordinates_new.add(coordinates.get(j));
			}
		}

		g_location.put("coordinates", coordinates_new);

		json2Update.setG_location(GeoTranslator.jts2Geojson(GeoTranslator.geojson2Jts(g_location), 1, 5).toString());
		
		
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

	/**
	 * @Description:更新范围线的几何坐标
	 * @param geoArr 组成线几何数组
	 * @param json 需要更新的tips
	 * @return
	 * @author: y
	 * @time:2017-7-5 下午5:22:29
	 */
	public static TipsDao updateAreaLineLocation(JSONArray geoArr,
			TipsDao json) {
		
		LineString[] lines=new LineString[geoArr.size()] ;
		
		int i=0;
		
		for (Object geo : geoArr) {
			
			JSONObject geoJson=JSONObject.fromObject(geo);
			
			Geometry  geometry=GeoTranslator.geojson2Jts(geoJson);
		
			//这里临时添加特殊处理，主要是因为 web端的数据有问题。geoF不对。？？ 为了星星不报错 临时增加if
			if(geometry.getGeometryType().equals(GeometryTypeName.GEOMETRYCOLLECTION))
			{
				
				int geoNum = geometry.getNumGeometries();
				for (int k= 0; k < geoNum; k++) {
					Geometry subGeo = geometry.getGeometryN(k);
					if (subGeo instanceof LineString) {
						lines[i]=(LineString)subGeo;
						break;
				}
			}
			}else
			{
				LineString line= (LineString)GeoTranslator.geojson2Jts(geoJson);
				
				lines[i]=(LineString)line;
			}
			
			i++;
		}
		
		GeometryFactory factory = new GeometryFactory();
		
		MultiLineString multiLines =factory.createMultiLineString(lines);
		
		Geometry loc= multiLines.convexHull() ;
		
		loc=loc.buffer(GeometryUtils.convert2Degree(5));
		
		JSONObject g_location= GeoTranslator.jts2Geojson(loc);

		json.setG_location(g_location.toString());
		
		return json;
	}

}
