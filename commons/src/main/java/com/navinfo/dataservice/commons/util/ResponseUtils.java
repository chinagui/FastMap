package com.navinfo.dataservice.commons.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

public class ResponseUtils {

	/**
	 * 组成执行成功的返回结果
	 * @param data
	 * @return
	 */
	public static String assembleRegularResult(Object data){
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		map.put("errcode", 0);
		
		map.put("errmsg", "success");
		
		if (data !=null){
		
		map.put("data", data);
		}else{
			map.put("data", null);
		}
		
		return JSONObject.fromObject(map).toString().replace(StringUtils.PlaceHolder, "");
	}
	
	
	
	/**
	 * 组装执行失败后结果
	 * @param errmsg
	 * @return
	 */
	public static String assembleFailResult(String errmsg){
		
		JSONObject json = new JSONObject();
		
		json.put("errcode", -1);
		
		json.put("data", null);
		
		json.put("errmsg", errmsg==null?"":errmsg);
		
		return json.toString();
	}
	
	public static String assembleFailResult(String errmsg, int errcode){
		
		JSONObject json = new JSONObject();
		
		json.put("errcode", errcode);
		
		json.put("errmsg", errmsg==null?"":errmsg);
		
		return json.toString();
	}
	
}
