package com.navinfo.dataservice.scripts;

import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

public class Fm2MultiSrcSyncScript {

	private static QueryRunner runner = new QueryRunner();
	
	public static JSONObject execute(JSONObject request) throws Exception{
		JSONObject response = new JSONObject();
		try {
			//todo
			//...
		} catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}finally{
		}
		return response;
	}

}
