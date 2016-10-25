package com.navinfo.dataservice.engine.fcc.service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;

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

	@Override
	public JSONObject getSubTaskStats(JSONArray grids) throws Exception {
		JSONObject result=new JSONObject();
		
		if (grids==null||grids.isEmpty()) {
			
            throw new IllegalArgumentException("参数错误:grids不能为空。");
        }

		TipsSelector selector = new TipsSelector();
		
		//统计日编总量 stage=1
		int total=selector.getTipsCountByStage(grids, 1);
		
		//统计日编已完成量stage=2 and t_dStatus=1
		int finished=selector.getTipsCountByStageAndTdStatus(grids,2,1);
		
		result.put("total", total);
		
		result.put("finished", finished);
		
		return result;
	}

}
