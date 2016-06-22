package com.navinfo.dataservice.commons.util;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.JSONUtils;

public class JsonUtils {
	
	private static JsonConfig strConfig = null;
	
	private static JsonConfig configString(){
		JsonConfig jsonConfig = new JsonConfig();

		jsonConfig.registerJsonValueProcessor(String.class,
				new JsonValueProcessor() {

					@Override
					public Object processObjectValue(String key, Object value,
							JsonConfig arg2) {
						if (value == null) {
							return null;
						}

						if (JSONUtils.mayBeJSON(value.toString())) {
							return "\"" + value + "\"";
						}

						return value;

					}

					@Override
					public Object processArrayValue(Object value,
							JsonConfig arg1) {
						return value;
					}
				});
		
		return jsonConfig;
	}
	
	public static JsonConfig getStrConfig(){
		if (strConfig == null){
			strConfig = configString();
		}
		
		return strConfig;
	}
	
	public static String getString(JSONObject json, String key){
		Object value = json.get(key);
		if(value == JSONNull.getInstance()){
			return null;
		}
		else{
			return value.toString();
		}
		
	}
}
