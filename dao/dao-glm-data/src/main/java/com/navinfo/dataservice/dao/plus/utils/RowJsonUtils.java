package com.navinfo.dataservice.dao.plus.utils;

import java.util.Date;
import java.util.Map;

import com.navinfo.dataservice.commons.util.DateUtils;
import com.vividsolutions.jts.geom.Geometry;

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
}
