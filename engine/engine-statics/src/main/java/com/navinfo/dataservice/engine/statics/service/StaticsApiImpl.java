package com.navinfo.dataservice.engine.statics.service;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.BlockExpectStatInfo;
import com.navinfo.dataservice.api.statics.model.GridChangeStatInfo;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;
import com.navinfo.dataservice.api.statics.model.SubtaskStatInfo;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.poidaily.PoiDailyMain;
import com.navinfo.dataservice.engine.statics.roadcollect.RoadCollectMain;
import com.navinfo.dataservice.engine.statics.roaddaily.RoadDailyMain;

@Service("staticsApi")
public class StaticsApiImpl implements StaticsApi {

	private static final String poi_month_grid_col_name = "fm_stat_month_poi_grid";

	private static final String road_month_grid_col_name = "fm_stat_month_road_grid";

	@Override
	public List<GridStatInfo> getLatestCollectStatByGrids(List<String> grids)
			throws Exception {
		return StaticsService.getInstance().getLatestStatByGrids(grids,
				PoiCollectMain.col_name_grid, RoadCollectMain.col_name_grid);
	}

	@Override
	public List<GridStatInfo> getLatestDailyEditStatByGrids(List<String> grids)
			throws Exception {
		return StaticsService.getInstance().getLatestStatByGrids(grids,
				PoiDailyMain.col_name_grid, RoadDailyMain.col_name_grid);
	}

	@Override
	public List<GridStatInfo> getLatestMonthlyEditStatByGrids(List<String> grids)
			throws Exception {
		return StaticsService.getInstance().getLatestStatByGrids(grids,
				poi_month_grid_col_name, road_month_grid_col_name);
	}

	@Override
	public List<GridChangeStatInfo> getChangeStatByGrids(Set<String> grids,
			int type, int stage, String date) throws Exception {
		return StaticsService.getInstance().getChangeStatByGrids(grids, stage, type, date);
	}

	@Override
	public Map<Integer, Integer> getExpectStatusByBlocks(Set<Integer> blocks) {
		return StaticsService.getInstance().getExpectStatusByBlocks(blocks);
	}

	@Override
	public List<BlockExpectStatInfo> getExpectStatByBlock(int blockId, int stage, int type) {
		return StaticsService.getInstance().getExpectStatByBlock(blockId, stage, type);
	}

	@Override
	public Map<Integer, Integer> getExpectStatusByCitys(Set<Integer> citys) {
		return StaticsService.getInstance().getExpectStatusByCitys(citys);
	}

	@Override
	public SubtaskStatInfo getStatBySubtask(int subtaskId) {
		return StaticsService.getInstance().getStatBySubtask(subtaskId);
	}

	@Override
	public List<Integer> getOpen100TaskIdList() throws Exception {
		return StaticsService.getInstance().getOpen100TaskIdList();
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.api.statics.iface.StaticsApi#getStatBySubtaskIdList(java.util.List)
	 */
	@Override
	public Map<Integer, SubtaskStatInfo> getStatBySubtaskIdList(List<Integer> subtaskIdList) {
		// TODO Auto-generated method stub
		return StaticsService.getInstance().getStatBySubtaskIdList(subtaskIdList);
	}

}
