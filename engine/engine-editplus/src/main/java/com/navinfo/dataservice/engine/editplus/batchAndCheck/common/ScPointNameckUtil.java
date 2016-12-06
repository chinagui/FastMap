package com.navinfo.dataservice.engine.editplus.batchAndCheck.common;

import java.util.HashMap;
import java.util.Map;

public class ScPointNameckUtil {
	public static Map<String, String> matchTypeD1(String name,Map<String, String> typeD1){
		Map<String, String> matchResult=new HashMap<String, String>();
		for(String key:typeD1.keySet()){
			if(name.contains(key)){
				String result=typeD1.get(key);
				if(!key.contains(result)){
					matchResult.put(key, result);
				}
			}
		}
		//matchResult.put("国家人口和计划生育委员会", "人口计生委");
		return matchResult;
	}
}
