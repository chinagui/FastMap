package com.navinfo.dataservice.dao.plus.utils;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.commons.util.DateUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/** 
 * @ClassName: RowJsonUtils
 * @author xiaoxiaowen4127
 * @date 2016年12月1日
 * @Description: RowJsonUtils.java
 */
public class RowJsonUtils {
	public static JSONObject toJson(Map<String,Object> values){
		if(values!=null&&values.size()>0){
			JSONObject jo = new JSONObject();
			for(Map.Entry<String, Object> attrs:values.entrySet()){
				if(attrs.getValue()==null){
					jo.put(attrs.getKey(), JSONNull.getInstance());
				}else{
					if(attrs.getValue() instanceof Date){
						jo.put(attrs.getKey(), DateUtils.dateToString((Date)attrs.getValue()));
					}else if(attrs.getValue() instanceof Geometry){
						jo.put(attrs.getKey(), ((Geometry)attrs.getValue()).toText());
					}else{
						jo.put(attrs.getKey(), attrs.getValue());
					}
				}
			}
			return jo;
		}
		return null;
	}
	
	public static JSONArray toJson(Collection<String> values){
		if(values!=null&&values.size()>0){
			JSONArray ja = new JSONArray();
			for(String value:values){
				if(value==null){
					ja.add(JSONNull.getInstance());
				}else{
					ja.add(value);
				}
			}
			return ja;
		}
		return null;
	}
	public static void main(String[] args) {
		JSONObject jo = JSONObject.fromObject("");
		System.out.println(jo.toString());
		JSONObject jo2 = JSONObject.fromObject(null);
		System.out.println(jo2.toString());
		Map<String,Object> map=new HashMap<String,Object>();
		map.put("1", JSONNull.getInstance());
		map.put("2", null);
		String name3 = (String)map.get("3");
		System.out.println(name3);
		String name2 = (String)map.get("2");
		System.out.println(name2);
		String name1 = (String)map.get("1");
		System.out.println(name1);
		
	}
}
