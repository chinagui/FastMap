package com.navinfo.dataservice.engine.statics.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.bson.Document;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.navinfo.dataservice.api.statics.model.GridChangeStatInfo;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;
import com.navinfo.dataservice.engine.statics.StatMain;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.poidaily.PoiDailyMain;
import com.navinfo.dataservice.engine.statics.roadcollect.RoadCollectMain;
import com.navinfo.dataservice.engine.statics.roaddaily.RoadDailyMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;

public class StaticsService {
	private StaticsService() {
	}

	private static class SingletonHolder {
		private static final StaticsService INSTANCE = new StaticsService();
	}

	public static StaticsService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public List<GridStatInfo> getLatestStatByGrids(List<String> grids,
			String poiColName, String roadColName) {

		MongoDao md = new MongoDao(StatMain.db_name);

		List<GridStatInfo> list = new ArrayList<GridStatInfo>();

		Map<String, GridStatInfo> map = new HashMap<String, GridStatInfo>();
		
		for(String grid : grids){
			GridStatInfo info = new GridStatInfo();
			
			info.setGridId(grid);
			
			map.put(grid, info);
		}

		int total = grids.size();

		MongoCursor<Document> iter = md
				.find(poiColName, Filters.in("grid_id", grids))
				.sort(Sorts.descending("stat_time")).batchSize(total)
				.iterator();

		int count = 0;
		while (iter.hasNext()) {

			JSONObject json = JSONObject.fromObject(iter.next());

			String gridId = json.getString("grid_id");

			JSONObject poi = json.getJSONObject("poi");

			GridStatInfo info = map.get(gridId);

			if (info == null) {
				info = new GridStatInfo();

				info.setGridId(gridId);
			}

			info.setFinishPoi(poi.getInt("finish"));

			info.setPercentPoi(poi.getDouble("percent"));

			info.setTotalPoi(poi.getInt("total"));

			map.put(gridId, info);

			count++;

			if (count >= total) {
				break;
			}
		}

		iter = md.find(roadColName, Filters.in("grid_id", grids))
				.sort(Sorts.descending("stat_time")).batchSize(grids.size())
				.iterator();

		count = 0;
		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());

			String gridId = json.getString("grid_id");

			JSONObject road = json.getJSONObject("road");

			GridStatInfo info = map.get(gridId);

			if (info == null) {
				info = new GridStatInfo();

				info.setGridId(gridId);
			}

			info.setFinishRoad(road.getDouble("finish"));

			info.setPercentRoad(road.getDouble("percent"));

			info.setTotalRoad(road.getDouble("total"));

			map.put(gridId, info);

			count++;

			if (count >= total) {
				break;
			}
		}

		for (Map.Entry<String, GridStatInfo> e : map.entrySet()) {
			list.add(e.getValue());
		}

		return list;
	}
	
	public List<GridChangeStatInfo> getChangeStatByGrids(List<String> grids,
			int stage, int type, String date) {
		
		String colName;
		if(stage == 0){//采集
			if (type==0){
				colName = PoiCollectMain.col_name_grid;
			}
			else{
				colName = RoadCollectMain.col_name_grid;
			}
		}
		else if (stage == 1){//日编
			if (type==0){
				colName = PoiDailyMain.col_name_grid;
			}
			else{
				colName = RoadDailyMain.col_name_grid;
			}
		}
		else{ //月编
			if (type==0){
				colName = PoiDailyMain.col_name_grid;
			}
			else{
				colName = RoadDailyMain.col_name_grid;
			}
		}
		MongoDao md = new MongoDao(StatMain.db_name);

		List<GridChangeStatInfo> list = new ArrayList<GridChangeStatInfo>();

		Map<String, GridChangeStatInfo> map = new HashMap<String, GridChangeStatInfo>();
		
		for(String grid : grids){
			GridChangeStatInfo info = new GridChangeStatInfo();
			
			info.setGridId(grid);
			
			map.put(grid, info);
		}

		int total = grids.size();

		MongoCursor<Document> iter = md
				.find(colName, Filters.and(Filters.in("grid_id", grids), Filters.eq("stat_date", date)))
				.batchSize(total)
				.iterator();

		int count = 0;
		while (iter.hasNext()) {

			JSONObject json = JSONObject.fromObject(iter.next());

			String gridId = json.getString("grid_id");

			GridChangeStatInfo info = map.get(gridId);

			if (info == null) {
				info = new GridChangeStatInfo();

				info.setGridId(gridId);
			}
			
			if(type==0){
				JSONObject poi = json.getJSONObject("poi");
	
				info.setPercent((int)poi.getDouble("percent"));
			}
			else{
				JSONObject road = json.getJSONObject("road");
				
				info.setPercent((int)road.getDouble("total"));
			}

			map.put(gridId, info);

			count++;

			if (count >= total) {
				break;
			}
		}

		for (Map.Entry<String, GridChangeStatInfo> e : map.entrySet()) {
			list.add(e.getValue());
		}

		return list;
	}
}
