package com.navinfo.dataservice.engine.fcc.service;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;

import net.sf.json.JSONArray;

public class FccApiImpl implements FccApi{

	@Override
	public JSONArray searchDataBySpatial(String wkt) throws Exception {
		try {
			TipsSelector selector = new TipsSelector();
			JSONArray array = selector.searchDataBySpatial(wkt);
			return array;
		} catch (Exception e) {
			throw e;
		}
		
	}

}
