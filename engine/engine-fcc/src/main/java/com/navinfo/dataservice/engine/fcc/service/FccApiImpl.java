package com.navinfo.dataservice.engine.fcc.service;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;

import net.sf.json.JSONArray;

@Service("fccApi")
public class FccApiImpl implements FccApi{

	@Override
	public JSONArray searchDataBySpatial(String wkt, int type, JSONArray stages) throws Exception {
		try {
			TipsSelector selector = new TipsSelector();
			JSONArray array = selector.searchDataBySpatial(wkt,type,stages);
			return array;
		} catch (Exception e) {
			throw e;
		}
		
	}

}
