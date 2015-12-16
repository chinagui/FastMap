package com.navinfo.dataservice.FosEngine.comm.util;

import net.sf.json.JSONObject;

public class SerializeUtils {

	public static Object deserializeFromJson(JSONObject json,Class c){
		Object obj = JSONObject.toBean(json, c);
		
		return obj;
	}
}
