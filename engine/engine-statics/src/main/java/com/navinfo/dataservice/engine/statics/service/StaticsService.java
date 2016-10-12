package com.navinfo.dataservice.engine.statics.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.bson.Document;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.BlockExpectStatInfo;
import com.navinfo.dataservice.api.statics.model.GridChangeStatInfo;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;
import com.navinfo.dataservice.api.statics.model.SubtaskStatInfo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.statics.StatMain;
import com.navinfo.dataservice.engine.statics.expect.ExpectStatusMain;
import com.navinfo.dataservice.engine.statics.expect.PoiCollectExpectMain;
import com.navinfo.dataservice.engine.statics.expect.PoiDailyExpectMain;
import com.navinfo.dataservice.engine.statics.expect.PoiMonthlyExpectMain;
import com.navinfo.dataservice.engine.statics.expect.RoadCollectExpectMain;
import com.navinfo.dataservice.engine.statics.expect.RoadDailyExpectMain;
import com.navinfo.dataservice.engine.statics.expect.RoadMonthlyExpectMain;
import com.navinfo.dataservice.engine.statics.overview.OverviewMain;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.poidaily.PoiDailyMain;
import com.navinfo.dataservice.engine.statics.poimonthly.PoiMonthlyMain;
import com.navinfo.dataservice.engine.statics.roadcollect.RoadCollectMain;
import com.navinfo.dataservice.engine.statics.roaddaily.RoadDailyMain;
import com.navinfo.dataservice.engine.statics.roadmonthly.RoadMonthlyMain;
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

	/**
	 * 查询grid的最新一天的统计信息
	 * 
	 * @param grids
	 * @param poiColName
	 * @param roadColName
	 * @return
	 */
	public List<GridStatInfo> getLatestStatByGrids(List<String> grids,
			String poiColName, String roadColName) {

		MongoDao md = new MongoDao(StatMain.db_name);

		List<GridStatInfo> list = new ArrayList<GridStatInfo>();

		Map<String, GridStatInfo> map = new HashMap<String, GridStatInfo>();

		for (String grid : grids) {
			GridStatInfo info = new GridStatInfo();

			info.setGridId(grid);

			map.put(grid, info);
		}

		int total = grids.size();

		MongoCursor<Document> iter = md
				.find(poiColName, Filters.in("grid_id", grids))
				.sort(Sorts.descending("stat_date")).batchSize(total)
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

			info.setPercentPoi((int)poi.getDouble("percent"));

			info.setTotalPoi(poi.getInt("total"));

			map.put(gridId, info);

			count++;

			if (count >= total) {
				break;
			}
		}

		iter = md.find(roadColName, Filters.in("grid_id", grids))
				.sort(Sorts.descending("stat_date")).batchSize(grids.size())
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

			info.setPercentRoad((int)road.getDouble("all_percent"));

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

	/**
	 * 
	 * 查询grid的变迁图
	 * 
	 * @param grids
	 * @param stage
	 * @param type
	 * @param date
	 * @return
	 */
	public List<GridChangeStatInfo> getChangeStatByGrids(Set<String> grids,
			int stage, int type, String date) {

		String colName;
		if (stage == 0) {// 采集
			if (type == 0) {
				colName = PoiCollectMain.col_name_grid;
			} else {
				colName = RoadCollectMain.col_name_grid;
			}
		} else if (stage == 1) {// 日编
			if (type == 0) {
				colName = PoiDailyMain.col_name_grid;
			} else {
				colName = RoadDailyMain.col_name_grid;
			}
		} else { // 月编
			if (type == 0) {
				colName = PoiMonthlyMain.col_name_grid;
			} else {
				colName = RoadMonthlyMain.col_name_grid;
			}
		}
		MongoDao md = new MongoDao(StatMain.db_name);

		List<GridChangeStatInfo> list = new ArrayList<GridChangeStatInfo>();

		Map<String, GridChangeStatInfo> map = new HashMap<String, GridChangeStatInfo>();

		for (String grid : grids) {
			GridChangeStatInfo info = new GridChangeStatInfo();

			info.setGridId(grid);

			map.put(grid, info);
		}

		int total = grids.size();

		MongoCursor<Document> iter = md
				.find(colName,
						Filters.and(Filters.in("grid_id", grids),
								Filters.eq("stat_date", date)))
				.batchSize(total).iterator();

		int count = 0;
		while (iter.hasNext()) {

			JSONObject json = JSONObject.fromObject(iter.next());

			String gridId = json.getString("grid_id");

			GridChangeStatInfo info = map.get(gridId);

			if (info == null) {
				info = new GridChangeStatInfo();

				info.setGridId(gridId);
			}

			if (type == 0) {
				JSONObject poi = json.getJSONObject("poi");

				info.setPercent((int) poi.getDouble("percent"));
			} else {
				JSONObject road = json.getJSONObject("road");

				if(stage==0){
					info.setPercent((int) road.getDouble("percent"));
				}
				else{
					info.setPercent((int) road.getDouble("all_percent"));
				}
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

	/**
	 * 查询Block是否达到预期的状态，0未达到，1已达到
	 * @param blocks
	 * @return
	 */
	public Map<Integer, Integer> getExpectStatusByBlocks(Set<Integer> blocks) {

		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		
		for(Integer block : blocks){
			map.put(block, 0);
		}

		MongoDao md = new MongoDao(StatMain.db_name);

		MongoCursor<Document> iter = md
				.find(ExpectStatusMain.col_name_block,Filters.in("block_id", blocks)
								)
				.batchSize(blocks.size()).iterator();

		while (iter.hasNext()) {
			
			Document doc = iter.next();
			
			int status = doc.getInteger("status");
			
			int blockId = doc.getInteger("block_id");
			
			map.put(blockId, status);
			
		}
		
		return map;
	}
	
	/**
	 * 查询城市的是否达到预期的状态， 0未达到，1达到
	 * @param citys
	 * @return
	 */
	public Map<Integer, Integer> getExpectStatusByCitys(Set<Integer> citys) {

		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		for (Integer city : citys) {
			map.put(city, city%2);
		}

		return map;
	}

	/**
	 * 
	 * 查询Block的预期统计信息
	 * 
	 * @param blockId
	 * @param stage
	 * @param type
	 * @return
	 */
	public List<BlockExpectStatInfo> getExpectStatByBlock(int blockId,
			int stage, int type) {

		String colName;

		if (stage == 0) {// 采集
			if (type == 0) {
				colName = PoiCollectExpectMain.col_name_block;
			} else {
				colName = RoadCollectExpectMain.col_name_block;
			}
		} else if (stage == 1) {// 日编
			if (type == 0) {
				colName = PoiDailyExpectMain.col_name_block;
			} else {
				colName = RoadDailyExpectMain.col_name_block;
			}
		} else { // 月编
			if (type == 0) {
				colName = PoiMonthlyExpectMain.col_name_block;
			} else {
				colName = RoadMonthlyExpectMain.col_name_block;
			}
		}

		List<BlockExpectStatInfo> list = new ArrayList<BlockExpectStatInfo>();

		MongoDao md = new MongoDao(StatMain.db_name);

		MongoCursor<Document> iter = md
				.find(colName, Filters.eq("block_id", blockId))
				.sort(Sorts.ascending("stat_date")).iterator();

		while (iter.hasNext()) {

			Document doc = iter.next();

			BlockExpectStatInfo info = new BlockExpectStatInfo();

			info.setDate(doc.getString("stat_date"));

			info.setExpect(doc.getDouble("expect"));

			info.setFinish(doc.getDouble("finish"));

			info.setPercent(doc.getInteger("percent"));

			list.add(info);
		}

		return list;

	}
	
	/**
	 * 
	 * 查询subtask数量及完成情况
	 * 
	 * @param subtaskId
	 * @return
	 * @throws Exception 
	 */
	public SubtaskStatInfo getStatBySubtask(int subtaskId){
		SubtaskStatInfo subtaskStatInfo = new SubtaskStatInfo();
		subtaskStatInfo.setSubtaskId(subtaskId);
		ManApi api=(ManApi) ApplicationContextUtil.getBean("manApi");
	
		Subtask subtask = null;
		try {
			subtask = api.queryBySubtaskId(subtaskId);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		//POI采集,道路采集，一体化采集
		if((subtask.getType()==0&&subtask.getStage()==0)
				||(subtask.getType()==1&&subtask.getStage()==0)
				||(subtask.getType()==2&&subtask.getStage()==0)){
			String poiColName = PoiCollectMain.col_name_grid;
			String roadColName = RoadCollectMain.col_name_grid;
			List<Integer> gridIds = null;
			try {
				gridIds = api.getGridIdsBySubtaskId(subtaskId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			SubtaskStatInfo result = StaticsOperation.getSubtaskStatByGrids(gridIds,poiColName,roadColName);
			//POI采集
			if(subtask.getType()==0){
				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"poi",result);
			}
			//道路采集
			else if(subtask.getType()==1){
				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"road",result);
			}
			//一体化采集
			else if(subtask.getType()==2){
				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"unity",result);
			}
		}
		//POI日编，一体化GRID粗编
		else if((subtask.getType()==0&&subtask.getStage()==1)
				||(subtask.getType()==3&&subtask.getStage()==1)){
			String poiColName = PoiDailyMain.col_name_grid;
			String roadColName = RoadDailyMain.col_name_grid;
			List<Integer> gridIds = null;
			try {
				gridIds = api.getGridIdsBySubtaskId(subtaskId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
			SubtaskStatInfo result = StaticsOperation.getSubtaskStatByGrids(gridIds,poiColName,roadColName);
			//POI日编
			if(subtask.getType()==0){
				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"poi",result);
			}
			//一体化GRID粗编
			else if(subtask.getType()==3){
				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"unity",result);
			}
		}
		//道路grid精编，道路grid粗编
		else if((subtask.getType()==8&&subtask.getStage()==2)
				||(subtask.getType()==9&&subtask.getStage()==2)){
			String poiColName = PoiMonthlyMain.col_name_grid;
			String roadColName = RoadMonthlyMain.col_name_grid;
			List<Integer> gridIds = null;
			try {
				gridIds = api.getGridIdsBySubtaskId(subtaskId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
			SubtaskStatInfo result = StaticsOperation.getSubtaskStatByGrids(gridIds,poiColName,roadColName);

			subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"road",result);
		}
		//多源POI，一体化区域粗编
		//根据block
		else if((subtask.getType()==4&&subtask.getStage()==1)
				||(subtask.getType()==5&&subtask.getStage()==1)){
			String poiColName = PoiDailyMain.col_name_block;
			String roadColName = RoadDailyMain.col_name_block;
			int blockId = subtask.getBlockId();
					
			SubtaskStatInfo result = StaticsOperation.getSubtaskStatByBlock(blockId,poiColName,roadColName);
			//一体化区域粗编
			if(subtask.getType()==4){
				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"unity",result);
			}
			//多源POI
			else if(subtask.getType()==5){
				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"poi",result);
			}
		}
		//代理店，POI专项，道路区域专项
		//根据city
		else if((subtask.getType()==6&&subtask.getStage()==2)
				||(subtask.getType()==7&&subtask.getStage()==2)
				||(subtask.getType()==10&&subtask.getStage()==2)){
			String poiColName = PoiMonthlyMain.col_name_city;
			String roadColName = RoadMonthlyMain.col_name_city;
			int taskId = subtask.getTaskId();
			int cityId = 0;
			try {
				cityId = api.queryCityIdByTaskId(taskId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
			SubtaskStatInfo result = StaticsOperation.getSubtaskStatByCity(cityId,poiColName,roadColName);
			//代理店,POI专项
			if(subtask.getType()==6||subtask.getType()==7){
				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"poi",result);
			}
			//道路区域专项
			else if(subtask.getType()==10){
				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"road",result);
			}
		}
	
		return subtaskStatInfo;
	}
	
	public List<Integer> getOpen100TaskIdList() {
		// TODO Auto-generated method stub
		
		List<Integer> taskIdList=StaticsOperation.getOpen100TaskIdList();
		return taskIdList;
	}

	/**
	 * 
	 * 查询subtask完成情况
	 * 
	 * @param subtaskIdList
	 * @return
	 */
	public Map<Integer, SubtaskStatInfo> getStatBySubtaskIdList(List<Integer> subtaskIdList) {
		// TODO Auto-generated method stub
		
		Map<Integer, SubtaskStatInfo> SubtaskStatInfos = new HashMap<Integer, SubtaskStatInfo>(); 
		Map<Integer,Integer> subtaskIdFlag = new HashMap<Integer,Integer>();
		
		MongoDao md = new MongoDao(StatMain.db_name);

		int total = subtaskIdList.size();

		MongoCursor<Document> iter = md
				.find(StatMain.col_name_subtask, Filters.in("subtaskId", subtaskIdList))
				.sort(Sorts.descending("stat_date")).batchSize(total)
				.iterator();
		
		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());
			
			int subtaskId = json.getInt("subtaskId");
			if(subtaskIdFlag.containsKey(subtaskId)){
				continue;
			}else{
				SubtaskStatInfo subtaskStatInfo = new SubtaskStatInfo();
				subtaskStatInfo.setSubtaskId(subtaskId);
				subtaskStatInfo.setPercent(json.getInt("percent"));
				subtaskStatInfo.setProgress(json.getInt("progress"));
				subtaskStatInfo.setDiffDate(json.getInt("diffDate"));
				SubtaskStatInfos.put(subtaskId, subtaskStatInfo);
			}
		}
		return SubtaskStatInfos;

	}
	
	
	public static void main(String[] args) throws Exception {
		
//		String wkt = "POLYGON ((116.55736132939865 40.37309069499443, 116.88314510913636 40.37309069499443, 116.88314510913636 40.25788148053289, 116.55736132939865 40.25788148053289, 116.55736132939865 40.37309069499443))";
//		
//		WKTReader r = new WKTReader();
//		Geometry geo = r.read(wkt);
//		Set<String> grids = CompGeometryUtil.geo2GridsWithoutBreak(geo);
		
//		List<String> grids = new ArrayList<String>();
//		grids.add("60563600");
//		StaticsService.getInstance().getLatestStatByGrids(grids, PoiDailyMain.col_name_grid, RoadDailyMain.col_name_grid);
		
//		StaticsService.getInstance().getChangeStatByGrids(grids, 0, 2, "20160620");
	}


}
