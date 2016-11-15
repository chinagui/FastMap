package com.navinfo.dataservice.commons.util;

import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.JSONUtils;

public class JsonUtils {

	private static JsonConfig strConfig = null;

	private static JsonConfig configString() {
		JsonConfig jsonConfig = new JsonConfig();

		jsonConfig.registerJsonValueProcessor(String.class, new JsonValueProcessor() {

			@Override
			public Object processObjectValue(String key, Object value, JsonConfig arg2) {
				if (value == null) {
					return null;
				}
				if (JSONUtils.mayBeJSON(value.toString())) {
					return "\"" + value + "\"";
				}

				return value;

			}

			@Override
			public Object processArrayValue(Object value, JsonConfig arg1) {
				return value;
			}
		});
		return jsonConfig;
	}

	public static JsonConfig getStrConfig() {
		if (strConfig == null) {
			strConfig = configString();
		}

		return strConfig;
	}

	public static String getString(JSONObject json, String key) {
		Object value = json.get(key);
		if (JSONUtils.isNull(value)) {
			return null;
		} else {
			return value.toString();
		}

	}
	
	/**
	 * 控制输出JSON的格式
	 * 
	 * @return JsonConfig
	 */
	public static JsonConfig getJsonConfig() {
		JsonConfig jsonConfig = new JsonConfig();

		jsonConfig.registerJsonValueProcessor(String.class, new JsonValueProcessor() {

			@Override
			public Object processObjectValue(String key, Object value, JsonConfig arg2) {
				if (value == null) {
					return null;
				}

				if (JSONUtils.mayBeJSON(value.toString())) {
					return "\"" + value + "\"";
				}

				return value;

			}

			@Override
			public Object processArrayValue(Object value, JsonConfig arg1) {
				return value;
			}
		});

		return jsonConfig;
	}
	
	public static int getInt(JSONObject json, String key) {
		if (json.containsKey(key)) {
			return json.getInt(key);
		}
		return 0;
	}

	public static String getStringValueFromJSONArray(JSONArray array) {
		if (array != null) {
			return array.toString().replace("[", "").replace("]", "");
		}
		return null;
	}
	
	public static JSONObject fastJson2netJson(com.alibaba.fastjson.JSONObject json)
	{
		JSONObject obj = new JSONObject();
		
		for(Entry<String, Object> entry :json.entrySet())
		{
			String key = entry.getKey();
			
			Object value = entry.getValue();
			
			obj.put(key, value);
		}
		
		return obj;
	}
}
