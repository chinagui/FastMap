package com.navinfo.dataservice.engine.statics.service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.navinfo.dataservice.engine.statics.StatMain;
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
	public static JSONObject getSubtaskStatByGrids(List<Integer> gridIds, String poiColName, String roadColName) {
		// TODO Auto-generated method stub
		JSONObject data = new JSONObject();
		
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
		
		JSONObject poi = new JSONObject();
		poi.put("total", totalPOI);
		poi.put("finish", finishPOI);
		poi.put("working", (totalPOI-finishPOI));
		data.put("poi", poi);
		
		JSONObject road = new JSONObject();
		road.put("total", totalROAD);
		road.put("finish", finishROAD);
		road.put("working", (totalROAD-finishROAD));
		data.put("road", road);
		
		return data;
	}

	/**
	 * @param blockId
	 * @param poiColName
	 * @param roadColName
	 * @return
	 */
	public static JSONObject getBlockStat(int blockId, String poiColName, String roadColName) {
		// TODO Auto-generated method stub
		JSONObject data = new JSONObject();
		List<String> blockIdList = new ArrayList<String>();
		blockIdList.add(String.valueOf(blockId));
		int total = blockIdList.size();

		List<String> gridIdList = new ArrayList<String>();
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

		iter = md.find(poiColName, Filters.in("block_id", blockIdList))
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
		
		JSONObject poi = new JSONObject();
		poi.put("total", totalPOI);
		poi.put("finish", finishPOI);
		poi.put("working", (totalPOI-finishPOI));
		data.put("poi", poi);
		
		JSONObject road = new JSONObject();
		road.put("total", totalROAD);
		road.put("finish", finishROAD);
		road.put("working", (totalROAD-finishROAD));
		data.put("road", road);
		
		return data;
	}

	/**
	 * @param cityId
	 * @param poiColName
	 * @param roadColName
	 * @return
	 */
	public static JSONObject getCityStat(int cityId, String poiColName, String roadColName) {
		// TODO Auto-generated method stub
		JSONObject data = new JSONObject();
		List<String> blockIdList = new ArrayList<String>();
		blockIdList.add(String.valueOf(cityId));
		int total = blockIdList.size();

		List<String> gridIdList = new ArrayList<String>();
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

		iter = md.find(poiColName, Filters.in("block_id", blockIdList))
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
		
		JSONObject poi = new JSONObject();
		poi.put("total", totalPOI);
		poi.put("finish", finishPOI);
		poi.put("working", (totalPOI-finishPOI));
		data.put("poi", poi);
		
		JSONObject road = new JSONObject();
		road.put("total", totalROAD);
		road.put("finish", finishROAD);
		road.put("working", (totalROAD-finishROAD));
		data.put("road", road);
		
		return data;
	}

	/**
	 * @param subtaskId
	 * @param string
	 * @param result
	 * @return
	 */
	public static JSONObject assembleResult(int subtaskId, String type, JSONObject data) {
		// TODO Auto-generated method stub
		JSONObject result = new JSONObject();
		result.put("subtaskId", subtaskId);
		
		JSONObject poi = result.getJSONObject("poi");
		JSONObject road = result.getJSONObject("road");
		
		int percent = 100;

		if(type.equals("poi")){
			if(poi.getInt("total")!=0){
				percent = poi.getInt("finish")*100/poi.getInt("total");
			}
		}else if(type.equals("road")){
			if(road.getInt("total")!=0){
				percent = road.getInt("finish")*100/road.getInt("total");
			}
		}else{
			int percentPOI = 100;
			if(poi.getInt("total")!=0){
				percentPOI = poi.getInt("finish")*100/poi.getInt("total");
			}
			int percentROAD = 100;
			if(road.getInt("total")!=0){
				percentROAD = road.getInt("finish")*100/road.getInt("total");
			}
			percent = (int) (percentROAD*0.5 + percentPOI*0.5);
		}

		result.put("percent", percent);
		return result;
	}
}


