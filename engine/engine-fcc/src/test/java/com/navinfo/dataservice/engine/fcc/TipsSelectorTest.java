package com.navinfo.dataservice.engine.fcc;

import net.sf.json.JSONArray;

import org.junit.Test;

import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;

public class TipsSelectorTest {

	TipsSelector selector = new TipsSelector();

	//根据网格、类型、作业状态获取tips的snapshot列表（rowkey，点位，类型）
	//@Test
	public void testGetSnapshot() {
		JSONArray grid = JSONArray
				.fromObject("[59567101,59567102,59567103,59567104,59567201,60560301,60560302,60560303,60560304]");
		JSONArray stage = new JSONArray();
		stage.add(1);
		int type = 1101;
		int projectId = 11;
		try {
			System.out.println(selector.getSnapshot(grid, stage, type,
					projectId));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
    //根据瓦片扩圈获取Tips数据
	@Test
	public void testSearchDataByTileWithGap() {
		JSONArray types = new JSONArray();
		types.add(1301);
		types.add(1205);
		types.add(1401);
		types.add(1110);
		types.add(1515);
		types.add(1105);
		types.add(1806);
		try {
			System.out.println(selector.searchDataByTileWithGap(107944, 49615, 17,
					20, types));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//根据网格获取tips统计
	//	@Test
		public void testGetStats() {
			JSONArray grid = JSONArray
					.fromObject("[59567101,59567102,59567103,59567104,59567201,60560301,60560302,60560303,60560304]");
			JSONArray stage = new JSONArray();
			try {
				System.out.println(selector.getStats(grid, stage));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		//根据rowkey获取单个tips的详细信息
		@Test
		public void testSearchDataByRowkey() {
			try {
				System.out.println("sorl by rowkey:");
				System.out.println(selector.searchDataByRowkey("021401e52571a598aa4e618692e0bfb702f728"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		//根据wkt范围获取tips的snapshot列表
	//	@Test
		public void testSearchDataBySpatial() {
			try {
				JSONArray ja =
						selector.searchDataBySpatial("POLYGON ((113.70469 26.62879, 119.70818 26.62879, 119.70818 29.62948, 113.70469 29.62948, 113.70469 26.62879))");

				System.out.println(ja.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

}
