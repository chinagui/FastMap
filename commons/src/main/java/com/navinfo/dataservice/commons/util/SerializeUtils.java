package com.navinfo.dataservice.commons.util;

import net.sf.json.JSONObject;

public class SerializeUtils {

	public static Object deserializeFromJson(JSONObject json,Class c){
		Object obj = JSONObject.toBean(json, c);
		
		return obj;
	}
}
