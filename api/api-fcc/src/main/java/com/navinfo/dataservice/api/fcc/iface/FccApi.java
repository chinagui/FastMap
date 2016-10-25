package com.navinfo.dataservice.api.fcc.iface;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public interface FccApi {
	
	public JSONArray searchDataBySpatial(String wkt, int type, JSONArray stages) throws Exception;
	
	/**
	 * @Description:根据grid，查询子tips的数据总量和已完成量，
	 * @param grids
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-10-25 上午10:57:37
	 */
	public JSONObject getSubTaskStats(JSONArray grids) throws Exception;

}
