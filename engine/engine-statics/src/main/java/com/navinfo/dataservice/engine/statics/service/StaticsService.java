package com.navinfo.dataservice.engine.statics.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
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
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
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
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

public class StaticsService {
	private static final String QUICK_MONITOR = "quick_monitor";
	private static final String MEDIUM_MONITOR = "medium_monitor";
	private static final String PROGRAM = "program";
	private static final String CITY = "city";
	private static final String BLOCK = "block";
	private static final String MONGO_PRODUCT_MONITOR = "product_monitor";
	

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
	
//	/**
//	 * 
//	 * 查询subtask数量及完成情况
//	 * 
//	 * @param subtaskId
//	 * @return
//	 * @throws Exception 
//	 */
//	public SubtaskStatInfo getStatBySubtask(int subtaskId){
//		SubtaskStatInfo subtaskStatInfo = new SubtaskStatInfo();
//		subtaskStatInfo.setSubtaskId(subtaskId);
//		ManApi api=(ManApi) ApplicationContextUtil.getBean("manApi");
//	
//		Subtask subtask = null;
//		try {
//			subtask = api.queryBySubtaskId(subtaskId);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//		//POI采集,道路采集，一体化采集
//		if((subtask.getType()==0&&subtask.getStage()==0)
//				||(subtask.getType()==1&&subtask.getStage()==0)
//				||(subtask.getType()==2&&subtask.getStage()==0)){
//			String poiColName = PoiCollectMain.col_name_grid;
//			String roadColName = RoadCollectMain.col_name_grid;
//			List<Integer> gridIds = null;
//			try {
//				gridIds = api.getGridIdsBySubtaskId(subtaskId);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			SubtaskStatInfo result = StaticsOperation.getSubtaskStatByGrids(gridIds,poiColName,roadColName);
//			//POI采集
//			if(subtask.getType()==0){
//				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"poi",result);
//			}
//			//道路采集
//			else if(subtask.getType()==1){
//				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"road",result);
//			}
//			//一体化采集
//			else if(subtask.getType()==2){
//				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"unity",result);
//			}
//		}
//		//POI日编，一体化GRID粗编
//		else if((subtask.getType()==0&&subtask.getStage()==1)
//				||(subtask.getType()==3&&subtask.getStage()==1)){
//			String poiColName = PoiDailyMain.col_name_grid;
//			String roadColName = RoadDailyMain.col_name_grid;
//			List<Integer> gridIds = null;
//			try {
//				gridIds = api.getGridIdsBySubtaskId(subtaskId);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//					
//			SubtaskStatInfo result = StaticsOperation.getSubtaskStatByGrids(gridIds,poiColName,roadColName);
//			//POI日编
//			if(subtask.getType()==0){
//				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"poi",result);
//			}
//			//一体化GRID粗编
//			else if(subtask.getType()==3){
//				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"unity",result);
//			}
//		}
//		//道路grid精编，道路grid粗编
//		else if((subtask.getType()==8&&subtask.getStage()==2)
//				||(subtask.getType()==9&&subtask.getStage()==2)){
//			String poiColName = PoiMonthlyMain.col_name_grid;
//			String roadColName = RoadMonthlyMain.col_name_grid;
//			List<Integer> gridIds = null;
//			try {
//				gridIds = api.getGridIdsBySubtaskId(subtaskId);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//					
//			SubtaskStatInfo result = StaticsOperation.getSubtaskStatByGrids(gridIds,poiColName,roadColName);
//
//			subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"road",result);
//		}
//		//多源POI，一体化区域粗编
//		//根据block
//		else if((subtask.getType()==4&&subtask.getStage()==1)
//				||(subtask.getType()==5&&subtask.getStage()==1)){
//			String poiColName = PoiDailyMain.col_name_block;
//			String roadColName = RoadDailyMain.col_name_block;
////			int blockId = subtask.getBlockId();
//			int blockId = 1;
//					
//			SubtaskStatInfo result = StaticsOperation.getSubtaskStatByBlock(blockId,poiColName,roadColName);
//			//一体化区域粗编
//			if(subtask.getType()==4){
//				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"unity",result);
//			}
//			//多源POI
//			else if(subtask.getType()==5){
//				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"poi",result);
//			}
//		}
//		//代理店，POI专项，道路区域专项
//		//根据city
//		else if((subtask.getType()==6&&subtask.getStage()==2)
//				||(subtask.getType()==7&&subtask.getStage()==2)
//				||(subtask.getType()==10&&subtask.getStage()==2)){
//			String poiColName = PoiMonthlyMain.col_name_city;
//			String roadColName = RoadMonthlyMain.col_name_city;
//			int taskId = subtask.getTaskId();
//			int cityId = 0;
//			try {
//				//cityId = api.queryCityIdByTaskId(taskId);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//					
//			SubtaskStatInfo result = StaticsOperation.getSubtaskStatByCity(cityId,poiColName,roadColName);
//			//代理店,POI专项
//			if(subtask.getType()==6||subtask.getType()==7){
//				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"poi",result);
//			}
//			//道路区域专项
//			else if(subtask.getType()==10){
//				subtaskStatInfo = StaticsOperation.assembleResult(subtaskId,"road",result);
//			}
//		}
//	
//		return subtaskStatInfo;
//	}
	
//	public List<Integer> getOpen100TaskIdList() {
//		// TODO Auto-generated method stub
//		
//		List<Integer> taskIdList=StaticsOperation.getOpen100TaskIdList();
//		return taskIdList;
//	}

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
	
	/**
	 * 查询当最近一次的mongo中task相应的统计数据
	 * @param int taskId
	 * @return Map<Integer, Map<String,Object>>
	 * @throws ServiceException 
	 */
	public Map<String, Object> getTaskProgressFromMongo(int taskId) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat));
			BasicDBObject filter = new BasicDBObject("taskId", taskId);
			FindIterable<Document> findIterable = mongoDao.find("task", filter).sort(new BasicDBObject("timestamp",-1));
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<String, Object> task = new HashMap<>();
			//处理数据
			if(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				task.put("poiUnfinishNum", (int) jso.get("poiUnfinishNum"));
				task.put("crowdTipsTotal", (int) jso.get("crowdTipsTotal"));
				task.put("inforTipsTotal", (int) jso.get("inforTipsTotal"));
				task.put("multisourcePoiTotal", (int) jso.get("multisourcePoiTotal"));
				task.put("collectTipsUploadNum", (int) jso.get("collectTipsUploadNum"));
				task.put("poiUploadNum", (int) jso.get("poiUploadNum"));
				task.put("tipsCreateByEditNum", (int) jso.get("tipsCreateByEditNum"));
				task.put("poiUnfinishNum", (int) jso.get("poiUnfinishNum"));
				task.put("dayEditTipsUnFinishNum", (int) jso.get("dayEditTipsNoWorkNum"));
				task.put("dayEditTipsFinishNum", (int) jso.get("dayEditTipsFinishNum"));
				task.put("tipsCreateByEditNum", (int) jso.get("tipsCreateByEditNum"));
				task.put("tipsCreateByEditNum", (int) jso.get("tipsCreateByEditNum"));
				
				task.put("day2MonthNum", (int) jso.get("day2MonthNum"));
				int monthPoiLogTotalNum = (int) jso.get("monthPoiLogTotalNum");
				int monthPoiLogFinishNum = (int) jso.get("monthPoiLogFinishNum");
				int monthPoiLogUnFinishNum = monthPoiLogTotalNum - monthPoiLogFinishNum;
				task.put("monthPoiLogFinishNum", (int) jso.get("monthPoiLogFinishNum"));
				task.put("monthPoiLogUnFinishNum", monthPoiLogUnFinishNum);
				task.put("roadPlanTotal", Double.valueOf(jso.get("roadPlanTotal").toString()));
				task.put("poiPlanTotal", (int) jso.get("poiPlanTotal"));
			}
			return task;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 获取快线统计
	 * @return Map<String, Object>
	 * @throws ServiceException 
	 */
	public Map<String, Object> quickMonitor() throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat));
			FindIterable<Document> findIterable = mongoDao.find(QUICK_MONITOR,null).sort(new BasicDBObject("timestamp",-1));
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<String, Object> task = new HashMap<>();
			//处理数据
			if(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				task.putAll(jso);
				JSONObject collectOverdueReasonNum = JSONObject.fromObject(jso.get("collectOverdueReasonNum"));
				//只取占比最多的前4个原因，其余的显示为其他，并给出百分比，例如{“原因1”：2，“原因2”：3, “原因3”：3, “原因4”：3, “other”：3}
				JSONArray collectReason2=reForm(collectOverdueReasonNum,4,true);
				task.put("collectOverdueReasonNum",collectReason2);
				
				JSONObject dayOverdueReasonNum  = JSONObject.fromObject(jso.get("dayOverdueReasonNum"));
				//只取占比最多的前4个原因，其余的显示为其他，并给出百分比，例如{“原因1”：2，“原因2”：3, “原因3”：3, “原因4”：3, “other”：3}
				JSONArray dayOverdueReasonNum2=reForm(dayOverdueReasonNum ,4,true);
				task.put("dayOverdueReasonNum",dayOverdueReasonNum2);
				
				JSONObject denyReasonNum  = JSONObject.fromObject(jso.get("denyReasonNum"));
				//只取占比最多的前4个原因，其余的显示为其他，并给出百分比，例如{“原因1”：2，“原因2”：3, “原因3”：3, “原因4”：3, “other”：3}
				JSONArray denyReasonNum2=reForm(denyReasonNum ,4,true);
				task.put("denyReasonNum",denyReasonNum2);
				
				JSONObject cityDetail  = JSONObject.fromObject(jso.get("cityDetail"));
				//只取占比最多的前4个原因，其余的显示为其他，并给出百分比，例如{“原因1”：2，“原因2”：3, “原因3”：3, “原因4”：3, “other”：3}
				JSONArray cityDetail2=reFormCity(cityDetail ,8);
				task.put("cityDetail",cityDetail2);
			}
			return task;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * originJson中的key为描述，value为百分比，按照百分比排序，此处将取前top的，其余归为一个百分比
	 * 例如top=4。返回：{“原因1”：2，“原因2”：3, “原因3”：3, “原因4”：3, “other”：3}
	 * @param originJson
	 * @param top
	 * @return
	 */
	private JSONArray reFormCity(JSONObject originJson,int top){
		if(originJson.size()==0){
			return new JSONArray();
		}
		List<Map<String,Object>> keyList=new ArrayList<>();
		Iterator iter = originJson.keys();
		while(iter.hasNext()){
			String key = String.valueOf(iter.next());
			JSONObject value = JSONObject.fromObject(originJson.get(key));
			Map<String,Object> tMap=new HashMap<>();
			tMap.put("name", key);
			tMap.put("count", value.get("total"));
			tMap.put("link", value.get("roadActualTotal"));
			keyList.add(tMap);
		}
		//冒泡排序
		for (int i = 0; i < keyList.size() -1; i++){    //最多做n-1趟排序
            for(int j = 0 ;j < keyList.size() - i - 1; j++){    //对当前无序区间score[0......length-i-1]进行排序(j的范围很关键，这个范围是在逐步缩小的)
            	Map<String,Object> jMap=keyList.get(j);
            	String jkey = String.valueOf(jMap.get("name"));
            	int jValue = (int)jMap.get("count");
            	int jValue2 = (int)jMap.get("link");
            	Map<String,Object> j1Map=keyList.get(j + 1);
            	String j1key = String.valueOf(j1Map.get("name"));
            	int j1Value = (int)j1Map.get("count");
            	int j1Value2 = (int)j1Map.get("link");
            	if(jValue< j1Value){    //把小的值交换到后面
            		Map<String,Object> tMap=new HashMap<>();
            		tMap.put("name", j1key);
            		tMap.put("count", j1Value);
        			tMap.put("link",j1Value2);
            		keyList.add(j, tMap);
            		Map<String,Object> t1Map=new HashMap<>();
            		t1Map.put("name", jkey);
            		t1Map.put("count", jValue);
        			t1Map.put("link",jValue2);
            		keyList.add(j+1, t1Map);
               }
           }
       }
		JSONArray orderJson=new JSONArray();
		for(int i=0;i<top;i++){
			if(i>=keyList.size()){
				return orderJson;
			}
			orderJson.add(keyList.get(i));
		}
		return orderJson;
	}
	
	/**
	 * originJson中的key为描述，value为百分比，按照百分比排序，此处将取前top的，其余归为一个百分比
	 * 例如top=4。返回：{“原因1”：2，“原因2”：3, “原因3”：3, “原因4”：3, “other”：3}
	 * @param originJson
	 * @param top
	 * @return
	 */
	private JSONArray reForm(JSONObject originJson,int top,boolean getOther){
		if(originJson.size()==0){
			return new JSONArray();
		}
		List<Map<String,Object>> keyList=new ArrayList<>();
		Iterator iter = originJson.keys();
		while(iter.hasNext()){
			String key = String.valueOf(iter.next());
			int value = (int)originJson.get(key);
			Map<String,Object> tMap=new HashMap<>();
			tMap.put("name", key);
			tMap.put("percent", value);
			keyList.add(tMap);
		}
		//冒泡排序
		for (int i = 0; i < keyList.size() -1; i++){    //最多做n-1趟排序
            for(int j = 0 ;j < keyList.size() - i - 1; j++){    //对当前无序区间score[0......length-i-1]进行排序(j的范围很关键，这个范围是在逐步缩小的)
            	Map<String,Object> jMap=keyList.get(j);
            	String jkey = String.valueOf(jMap.get("name"));
            	int jValue = (int)jMap.get("percent");
            	Map<String,Object> j1Map=keyList.get(j + 1);
            	String j1key = String.valueOf(j1Map.get("name"));
            	int j1Value = (int)j1Map.get("percent");
            	if(jValue< j1Value){    //把小的值交换到后面
            		Map<String,Object> tMap=new HashMap<>();
            		tMap.put("name", j1key);
        			tMap.put("percent", j1Value);
            		keyList.add(j, tMap);
            		Map<String,Object> t1Map=new HashMap<>();
            		t1Map.put("name", jkey);
        			t1Map.put("percent", jValue);
            		keyList.add(j+1, t1Map);
               }
           }
       }
		JSONArray orderJson=new JSONArray();
		int topPercent=0;
		for(int i=0;i<top;i++){
			if(i>=keyList.size()){
				return orderJson;
			}
			topPercent=topPercent+(int)keyList.get(i).get("percent");
			orderJson.add(keyList.get(i));
		}
		if(getOther){
			Map<String,Object> tMap=new HashMap<>();
			tMap.put("name", "other");
			tMap.put("percent", 100-topPercent);
			orderJson.add(tMap);
		}
		return orderJson;
	}
	
	/**
	 * 获取快线统计
	 * @return Map<String, Object>
	 * @throws ServiceException 
	 */
	public Map<String, Object> mediumMonitor() throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat));
			FindIterable<Document> findIterable = mongoDao.find(MEDIUM_MONITOR,null).sort(new BasicDBObject("timestamp",-1));
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<String, Object> task = new HashMap<>();
			//处理数据
			if(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				task.putAll(jso);
			}
			return task;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 项目监控接口
	 * 应用场景：管理平台监控快线监控—〉项目详情
	 * 管理平台监控中线监控—〉项目详情
	 * @return Map<String, Object>
	 * @throws ServiceException 
	 */
	public Map<String, Object> cityMonitor(int cityId,int programId) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat));
			
			BasicDBObject filter =null;
			if(cityId!=0){filter=new BasicDBObject("cityId", cityId);}
			if(programId!=0){filter=new BasicDBObject("programId", programId);}
			FindIterable<Document> findIterable = mongoDao.find(PROGRAM,filter).sort(new BasicDBObject("timestamp",-1));
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<String, Object> task = new HashMap<>();
			//处理数据
			if(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				task.putAll(jso);
			}
			if(cityId!=0){
				FindIterable<Document> findIterable2 = mongoDao.find(CITY,filter).sort(new BasicDBObject("timestamp",-1));
				MongoCursor<Document> iterator2 = findIterable2.iterator();
				//处理数据
				if(iterator2.hasNext()){
					//获取统计数据
					JSONObject jso = JSONObject.fromObject(iterator2.next());
					task.putAll(jso);
				}
			}
			return task;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 项目监控接口
	 * 应用场景：管理平台监控快线监控—〉项目详情
	 * 管理平台监控中线监控—〉项目详情
	 * @return Map<String, Object>
	 * @throws ServiceException 
	 */
	public Map<String, Object> blockMonitor(int blockId) throws Exception{
		try {
			MongoDao mongoDao = new MongoDao(SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat));
			BasicDBObject filter = new BasicDBObject("blockId", blockId);
			FindIterable<Document> findIterable = mongoDao.find(BLOCK,filter).sort(new BasicDBObject("timestamp",-1));
			MongoCursor<Document> iterator = findIterable.iterator();
			Map<String, Object> task = new HashMap<>();
			//处理数据
			if(iterator.hasNext()){
				//获取统计数据
				JSONObject jso = JSONObject.fromObject(iterator.next());
				task.putAll(jso);
			}
			return task;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**返回fm_stat_crowd中的blcokcode及经纬度坐标的列表
	 * @return
	 * @throws Exception
	 */
	public JSONArray crowdInfoList() throws Exception {
		String querySql = "select f.block_code,f.x,f.y from fm_stat_crowd f";
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>(){

				@Override
				public JSONArray handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					JSONArray data = new JSONArray();
					while(rs.next()){
						JSONObject obj = new JSONObject();
						try{
							obj.put("blockCode", rs.getString("block_code"));
							obj.put("x", rs.getDouble("x"));
							obj.put("y", rs.getDouble("y"));
							
							data.add(obj);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					return data;
				}

			};
			return run.query(conn, querySql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**创建外业采集子任务的（subtask表work_kind=1,status in (1,0)的city）cityId及admin_geo的经纬度坐标
	 * @return
	 * @throws Exception
	 */
	public JSONArray commonInfoListCity() throws Exception {
		String querySql = "select c.city_id,c.admin_geo.sdo_point.x x,c.admin_geo.sdo_point.y y from city c where city_id in "
				+ "(select city_id from block where block_id in "
				+ "(select block_id from task where task_id in "
				+ "(select task_id from subtask where work_kind=1 and status in (0,1))))";
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>(){

				@Override
				public JSONArray handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					JSONArray dataList = new JSONArray();
					while(rs.next()){
						JSONObject city = new JSONObject();
						try{
							city.put("cityId", rs.getInt("city_id"));
							city.put("x", rs.getDouble("x"));
							city.put("y", rs.getDouble("y"));
							
							dataList.add(city);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					return dataList;
				}
			};
			return run.query(conn, querySql, rsHandler);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**查询mongo库中fm_stat.product_monitor中的统计值
	 * @return
	 * @throws Exception
	 */
	public JSONObject getMongoMonitorData() throws Exception {
		JSONObject data = new JSONObject();
		try{
			String dbName=SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
			MongoDao md = new MongoDao(dbName);
			MongoCollection<Document> collections = md.getDatabase().getCollection(MONGO_PRODUCT_MONITOR);
			// 查询时间戳最新的统计
			Document doc = collections.find().sort(new BasicDBObject("timestamp",-1)).first();
			if(doc != null){
				JSONObject statics = JSONObject.fromObject(doc);
				if(statics.containsKey("_id")){
					statics.remove("_id");
				}
				data.putAll(statics);
			}
			return data;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**获取man库中man_config中的统计值
	 * @param platForm
	 * @return
	 * @throws Exception
	 */
	public JSONObject getOracleMonitorData(String platForm) throws Exception{
		String querySql = "select m.conf_key, m.conf_value from man_config m where m.platform=? ";
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			ResultSetHandler<JSONObject> rsHandler = new ResultSetHandler<JSONObject>(){

				@Override
				public JSONObject handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					JSONObject statics = new JSONObject();
					while(rs.next()){
						String confKey = rs.getString("conf_key");
						String confValue = rs.getString("conf_value");
						if(StringUtils.isNotEmpty(confValue) && confValue.contains("{") && confValue.contains("}")){
							statics.put(confKey, JSONObject.fromObject(confValue));
						}else{
							statics.put(confKey, confValue);
						}
					}
					return statics;
				}
			};
			return run.query(conn, querySql, rsHandler, platForm);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
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
		
		JSONObject a = JSONObject.fromObject("{\"a\":1}");
		JSONObject b = JSONObject.fromObject("{\"b\":2}");
		a.putAll(b);
		System.out.println(a.toString());
		
	}


}
