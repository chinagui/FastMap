package com.navinfo.dataservice.FosEngine.test;

import com.navinfo.dataservice.FosEngine.comm.db.HBaseAddress;
import com.navinfo.dataservice.FosEngine.tips.TipsSelector;

import net.sf.json.JSONArray;

public class Test3 {

	public static void main(String[] args) throws Exception {
		
		HBaseAddress.initHBaseClient("192.168.3.156");

		JSONArray stages = new JSONArray();
		
		stages.add(3);
		
		stages.add(1);
		
		JSONArray grids = new JSONArray();
		grids.add(59567201);
		
		System.out.println(TipsSelector.searchDataByTileWithGap(26967, 12409, 15, 2));
	}

}
