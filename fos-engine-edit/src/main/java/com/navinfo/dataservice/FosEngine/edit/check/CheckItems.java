package com.navinfo.dataservice.FosEngine.edit.check;

import java.util.HashMap;
import java.util.Map;

public class CheckItems {

	private static Map<String,String> map = new HashMap<String,String>();
	
	static{
		map.put("SHAPING_CHECK_CROSS_RDLINK_RDLINK", "两条Link相交，必须做立交或者打断");
		map.put("GLM01015", "闭合环未打断");
		map.put("GLM56004", "修行中产生自相交，请做立交或打断");
		map.put("GLM01014", "闭合环未打断");
		map.put("GLM01025", "第一个形状点的坐标与link的起点坐标不相同；最后一个形状点的坐标与link的终点坐标不相同");
		map.put("GLM01027", "Link形状点大于490");
		map.put("PERMIT_CHECK_NO_REPEAT", "该位置已有节点，同一坐标不能有两个节点，请创建点点立交");
		map.put("GLM03001", "Node的接续link数必须小于等于7");
		map.put("GLM03056", "障碍物属性的点挂接link数不等于2");
	}
	
	public static String getInforByRuleId(String ruleId){
		return map.get(ruleId);
	}
}
