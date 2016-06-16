package com.navinfo.dataservice.engine.statics;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;

@Service("staticsApi")
public class StaticsApiImpl implements StaticsApi {

	@Override
	public List<GridStatInfo> getCollectStatByGrids(List<String> grids)
			throws Exception {
		List<GridStatInfo> list = new ArrayList<GridStatInfo>();

		for (String grid : grids) {
			GridStatInfo info = new GridStatInfo();

			info.setGridId(grid);

			list.add(info);
		}

		return list;
	}

	@Override
	public List<GridStatInfo> getDailyEditStatByGrids(List<String> grids)
			throws Exception {
		List<GridStatInfo> list = new ArrayList<GridStatInfo>();

		for (String grid : grids) {
			GridStatInfo info = new GridStatInfo();

			info.setGridId(grid);

			list.add(info);
		}

		return list;
	}

	@Override
	public List<GridStatInfo> getMonthlyEditStatByGrids(List<String> grids)
			throws Exception {
		List<GridStatInfo> list = new ArrayList<GridStatInfo>();

		for (String grid : grids) {
			GridStatInfo info = new GridStatInfo();

			info.setGridId(grid);

			list.add(info);
		}

		return list;
	}

}
