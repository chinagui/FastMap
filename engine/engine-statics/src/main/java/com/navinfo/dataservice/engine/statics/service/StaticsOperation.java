package com.navinfo.dataservice.engine.statics.service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.navinfo.dataservice.api.statics.model.SubtaskStatInfo;
import com.navinfo.dataservice.engine.statics.StatMain;
import com.navinfo.dataservice.engine.statics.overview.OverviewMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;

import net.sf.json.JSONObject;

/** 
 * @ClassName: StaticsOperation
 * @author songdongyan
 * @date 2016年9月12日
 * @Description: StaticsOperation.java
 */
public class StaticsOperation {

	/**
	 * 
	 */
	public StaticsOperation() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param gridIds
	 * @param roadColName 
	 * @param poiColName 
	 * @return
	 */
	public static SubtaskStatInfo getSubtaskStatByGrids(List<Integer> gridIds, String poiColName, String roadColName) {
		// TODO Auto-generated method stub
		SubtaskStatInfo subtaskStatInfo = new SubtaskStatInfo();
		
		List<String> gridIdList = new ArrayList<String>();
		for(int i = 0;i<gridIds.size();i++){
			String s = String.valueOf(gridIds.get(i));
			gridIdList.add(i, s);
		}
		
		int totalPOI = 0;
		int finishPOI = 0;
		int totalROAD = 0;
		int finishROAD = 0;
		
		MongoDao md = new MongoDao(StatMain.db_name);

		int total = gridIdList.size();

		MongoCursor<Document> iter = md
				.find(poiColName, Filters.in("grid_id", gridIdList))
				.sort(Sorts.descending("stat_date")).batchSize(total)
				.iterator();

		int count = 0;
		while (iter.hasNext()) {

			JSONObject json = JSONObject.fromObject(iter.next());

			JSONObject poi = json.getJSONObject("poi");
			
			totalPOI += poi.getInt("total");
			finishPOI += poi.getInt("finish");
			
			count++;
			if (count >= total) {
				break;
			}
		}

		iter = md.find(roadColName, Filters.in("grid_id", gridIdList))
				.sort(Sorts.descending("stat_date")).batchSize(gridIdList.size())
				.iterator();

		count = 0;
		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());

			JSONObject road = json.getJSONObject("road");

			totalROAD += road.getInt("total");
			finishROAD += road.getInt("finish");

			count++;
			if (count >= total) {
				break;
			}
		}
		
		subtaskStatInfo.setTotalPoi(totalPOI);
		subtaskStatInfo.setWorkingPoi(totalPOI-finishPOI);
		subtaskStatInfo.setFinishPoi(finishPOI);
		
		subtaskStatInfo.setFinishRoad(finishROAD);
		subtaskStatInfo.setTotalRoad(totalROAD);
		subtaskStatInfo.setWorkingRoad(totalROAD-finishROAD);
		
		return subtaskStatInfo;
	}
	
	public static String getLastStatDate(String colName){
		MongoDao md = new MongoDao(StatMain.db_name);
		Document doc1 = md
				.find(colName, null).first();
		Document doc = md
				.find(colName, null)
				.sort(Sorts.descending("stat_date")).first();
		return JSONObject.fromObject(doc).getString("stat_date");
	}


	/**
	 * @param subtaskId
	 * @param string
	 * @param result
	 * @return
	 */
	public static SubtaskStatInfo assembleResult(int subtaskId, String type, SubtaskStatInfo subtaskStatInfo) {
		// TODO Auto-generated method stub
		subtaskStatInfo.setSubtaskId(subtaskId);

		int percent = 100;

		if(type.equals("poi")){
			if(subtaskStatInfo.getTotalPoi()!=0){
				percent = (int) (subtaskStatInfo.getFinishPoi()*100/subtaskStatInfo.getTotalPoi());
			}
		}else if(type.equals("road")){
			if(subtaskStatInfo.getTotalRoad()!=0){
				percent = (int) (subtaskStatInfo.getFinishRoad()*100/subtaskStatInfo.getTotalRoad());
			}
		}else{
			int percentPOI = 100;
			if(subtaskStatInfo.getTotalPoi()!=0){
				percentPOI = (int) (subtaskStatInfo.getFinishPoi()*100/subtaskStatInfo.getTotalPoi());
			}
			int percentROAD = 100;
			if(subtaskStatInfo.getTotalRoad()!=0){
				percentROAD = (int) (subtaskStatInfo.getFinishRoad()*100/subtaskStatInfo.getTotalRoad());
			}
			percent = (int) (percentROAD*0.5 + percentPOI*0.5);
		}

		subtaskStatInfo.setPercent(percent);
		return subtaskStatInfo;
	}

	/**
	 * @param blockId
	 * @param poiColName
	 * @param roadColName
	 * @return
	 */
	public static SubtaskStatInfo getSubtaskStatByBlock(int blockId, String poiColName, String roadColName) {
		// TODO Auto-generated method stub
		SubtaskStatInfo subtaskStatInfo = new SubtaskStatInfo();
		
		List<String> blockIdList = new ArrayList<String>();
		blockIdList.add(String.valueOf(blockId));
		int total = blockIdList.size();

		int totalPOI = 0;
		int finishPOI = 0;
		int totalROAD = 0;
		int finishROAD = 0;
		
		MongoDao md = new MongoDao(StatMain.db_name);

		MongoCursor<Document> iter = md
				.find(poiColName, Filters.in("block_id", blockIdList))
				.sort(Sorts.descending("stat_date")).batchSize(total)
				.iterator();

		int count = 0;
		while (iter.hasNext()) {

			JSONObject json = JSONObject.fromObject(iter.next());

			JSONObject poi = json.getJSONObject("poi");
			
			totalPOI += poi.getInt("total");
			finishPOI += poi.getInt("finish");
			
			count++;
			if (count >= total) {
				break;
			}
		}

		iter = md.find(roadColName, Filters.in("block_id", blockIdList))
				.sort(Sorts.descending("stat_date")).batchSize(total)
				.iterator();

		count = 0;
		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());

			JSONObject road = json.getJSONObject("road");

			totalROAD += road.getInt("total");
			finishROAD += road.getInt("finish");

			count++;
			if (count >= total) {
				break;
			}
		}
		
		subtaskStatInfo.setTotalPoi(totalPOI);
		subtaskStatInfo.setWorkingPoi(totalPOI-finishPOI);
		subtaskStatInfo.setFinishPoi(finishPOI);
		
		subtaskStatInfo.setFinishRoad(finishROAD);
		subtaskStatInfo.setTotalRoad(totalROAD);
		subtaskStatInfo.setWorkingRoad(totalROAD-finishROAD);
		
		return subtaskStatInfo;
	}

	/**
	 * @param cityId
	 * @param poiColName
	 * @param roadColName
	 * @return
	 */
	public static SubtaskStatInfo getSubtaskStatByCity(int cityId, String poiColName, String roadColName) {
		// TODO Auto-generated method stub
		SubtaskStatInfo subtaskStatInfo = new SubtaskStatInfo();
		List<String> blockIdList = new ArrayList<String>();
		blockIdList.add(String.valueOf(cityId));
		int total = blockIdList.size();

		int totalPOI = 0;
		int finishPOI = 0;
		int totalROAD = 0;
		int finishROAD = 0;
		
		MongoDao md = new MongoDao(StatMain.db_name);

		MongoCursor<Document> iter = md
				.find(poiColName, Filters.in("block_id", blockIdList))
				.sort(Sorts.descending("stat_date")).batchSize(total)
				.iterator();

		int count = 0;
		while (iter.hasNext()) {

			JSONObject json = JSONObject.fromObject(iter.next());

			JSONObject poi = json.getJSONObject("poi");
			
			totalPOI += poi.getInt("total");
			finishPOI += poi.getInt("finish");
			
			count++;
			if (count >= total) {
				break;
			}
		}

		iter = md.find(poiColName, Filters.in("city_id", blockIdList))
				.sort(Sorts.descending("stat_date")).batchSize(total)
				.iterator();

		count = 0;
		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());

			JSONObject road = json.getJSONObject("road");

			totalROAD += road.getInt("total");
			finishROAD += road.getInt("finish");

			count++;
			if (count >= total) {
				break;
			}
		}
		
		subtaskStatInfo.setTotalPoi(totalPOI);
		subtaskStatInfo.setWorkingPoi(totalPOI-finishPOI);
		subtaskStatInfo.setFinishPoi(finishPOI);
		
		subtaskStatInfo.setFinishRoad(finishROAD);
		subtaskStatInfo.setTotalRoad(totalROAD);
		subtaskStatInfo.setWorkingRoad(totalROAD-finishROAD);
		
		return subtaskStatInfo;
	}

	public static List<Integer> getOpen100TaskIdList() {
		// TODO Auto-generated method stub
		MongoDao md = new MongoDao(StatMain.db_name);
		String colName = OverviewMain.col_name_task;
		MongoCursor<Document> iter = md
				.find(colName, Filters.and(Filters.eq("percent", 100), Filters.eq("taskStatus", 1),Filters.eq("stat_date", getLastStatDate(colName))))
				.iterator();
		List<Integer> taskIdList=new ArrayList<Integer>();
		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());
			int taskId = json.getInt("taskId");
			taskIdList.add(taskId);
		}
		return taskIdList;
	} 
}


