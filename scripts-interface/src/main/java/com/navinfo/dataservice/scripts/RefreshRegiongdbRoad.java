package com.navinfo.dataservice.scripts;

import java.io.File;
import java.util.Iterator;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/** 
* @ClassName: refreshFmgdbRoad 
* @author Xiao Xiaowen 
* @date 2016年5月26日 下午5:42:06 
* @Description: TODO
*  
*/
public class RefreshRegiongdbRoad {
	
	public static JSONObject execute(JSONObject request)throws Exception{
		JSONObject response = new JSONObject();
		try{
			
		}catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}
		return response;
	}
}
