package com.navinfo.dataservice.engine.statics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.GridChangeStatInfo;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.poidaily.PoiDailyMain;
import com.navinfo.dataservice.engine.statics.roadcollect.RoadCollectMain;
import com.navinfo.dataservice.engine.statics.roaddaily.RoadDailyMain;
import com.navinfo.navicommons.geo.computation.CompGridUtil;

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

	public static void main(String[] args) throws Exception {
		StaticsApiImpl impl = new StaticsApiImpl();

		List<String> grids = new ArrayList<String>();

		Set<String> set = CompGridUtil.mesh2Grid("595672");

		for (String grid : set) {
			grids.add(grid);
		}

		List<GridStatInfo> list = impl.getLatestCollectStatByGrids(grids);

		System.out.println(list.size());
	}

	@Override
	public List<GridChangeStatInfo> getChangeStatByGrids(Set<String> grids,
			int type, int stage, String date) throws Exception {
		return StaticsService.getInstance().getChangeStatByGrids(grids, stage, type, date);
	}
}
