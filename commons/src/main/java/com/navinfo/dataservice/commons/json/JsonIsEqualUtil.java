package com.navinfo.dataservice.commons.json;


import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.navinfo.dataservice.commons.util.StringUtils;

/**
 * 判断两个json字符串是否相等
 * @author SGQ
 *
 */
public class JsonIsEqualUtil {
	private static final Logger logger = Logger.getLogger(JsonIsEqualUtil.class);
	
	public static boolean equalsJson(String json1, String json2){
		if(json1 == null && null == json2){
			return true;
		}
		if(!StringUtils.isEmpty(json1)&&!StringUtils.isEmpty(json2)&&json1.equals(json2)){
			return true;
		}
		try {
			 
			JsonParser parser = new JsonParser();
			JsonObject obj = (JsonObject) parser.parse(json1);
			JsonParser parser1 = new JsonParser();
			JsonObject obj1 = (JsonObject) parser1.parse(json2);
			logger.debug("json1:"+ json1 + ",json2:" + json2);
			return obj.equals(obj1);

		}catch (Exception e){
			e.printStackTrace();
			return StringUtils.equals(json1, json2);
		}
	}	
}
