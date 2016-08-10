package com.navinfo.dataservice.api.fcc.iface;

import net.sf.json.JSONArray;

public interface FccApi {
	
	public JSONArray searchDataBySpatial(String wkt) throws Exception;

}
