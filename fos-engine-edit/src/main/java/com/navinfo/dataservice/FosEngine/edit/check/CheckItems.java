package com.navinfo.dataservice.FosEngine.edit.check;

import java.util.HashMap;
import java.util.Map;

public class CheckItems {

	private static Map<String,String> map = new HashMap<String,String>();
	
	static{
		
	}
	
	public static String getInforByRuleId(String ruleId){
		return map.get(ruleId);
	}
}
