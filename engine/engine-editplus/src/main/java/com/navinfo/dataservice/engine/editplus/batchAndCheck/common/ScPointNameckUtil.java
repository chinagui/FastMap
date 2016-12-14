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
	public static Map<String, String> matchTypeD10(String name,Map<String, String> typeD10){
		Map<String, String> matchResult=new HashMap<String, String>();
		for(String key:typeD10.keySet()){
			//name开头匹配关键字
			if(name.startsWith(key)){
				String result=typeD10.get(key);
				matchResult.put(key, result);
				break;
			}
		}
		//matchResult.put("中国工商银行", "工行");
		return matchResult;
	}
	/**
	 * 通用匹配方法
	 */
	public static Map<String, String> matchType(String name,Map<String, String> typeData){
		Map<String, String> matchResult=new HashMap<String, String>();
		for(String key:typeData.keySet()){
			if(name.contains(key)){
				String result=typeData.get(key);
				matchResult.put(key, result);
			}
		}
		return matchResult;
	}
}
