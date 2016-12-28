package com.navinfo.dataservice.engine.editplus.batchAndCheck.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.model.ScPointNameckObj;

public class ScPointNameckUtil {
	/**
	 * 需要按照顺序进行key值替换名称，所以用list，按照key长度存放。若preKey包含resultKey则跳过（批处理规则FM-BAT-20-137需求）
	 * @param name
	 * @param typeD1
	 * @return
	 */
	public static List<ScPointNameckObj> matchTypeD1(String name,List<ScPointNameckObj> typeD1){
		List<ScPointNameckObj> matchResult=new ArrayList<ScPointNameckObj>();
		for(ScPointNameckObj obj:typeD1){
			if(name.contains(obj.getPreKey())){
				if(!obj.getPreKey().contains(obj.getResultKey())){
					matchResult.add(obj);
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
	
	/**
	 * 通用匹配方法,name包含typeData列表中，则返回。
	 * typeData=['ei','tu'] 
	 * 若name="ytuy",返回['tu'] ；若name="yty",返回[] 
	 * @param name
	 * @param typeData
	 * @return
	 */
	public static List<String> matchType(String name,List<String> typeData){
		List<String> matchResult=new ArrayList<String>();
		for(String key:typeData){
			if(name.contains(key)){
				matchResult.add(key);
			}
		}
		return matchResult;
	}
}
