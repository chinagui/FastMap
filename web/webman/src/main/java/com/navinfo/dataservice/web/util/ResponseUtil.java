package com.navinfo.dataservice.web.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

public class ResponseUtil {

	public static void setResponseHeader(HttpServletResponse response) {

		response.setContentType("text/html;charset=utf-8");

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, DELETE,PUT");
	}
	
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
		
		return JSONObject.fromObject(map).toString();
	}
	
	
	
	/**
	 * 组装执行失败后结果
	 * @param errmsg
	 * @return
	 */
	public static String assembleFailResult(String errmsg, String errid){
		
		JSONObject json = new JSONObject();
		
		json.put("errcode", -1);
		
		json.put("errmsg", errmsg);
		
		json.put("errid", errid);
		
		return json.toString();
	}
	
	
	public static void main(String[] at){
		System.out.println(assembleRegularResult(null));
	}
	
	
}
