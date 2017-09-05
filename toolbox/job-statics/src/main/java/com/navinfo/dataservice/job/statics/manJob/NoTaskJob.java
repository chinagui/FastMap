package com.navinfo.dataservice.job.statics.manJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;

import net.sf.json.JSONObject;

public class NoTaskJob extends AbstractStatJob {
	
	private static final String db_name = SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
	
	public NoTaskJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String stat() throws JobException {
		NoTaskJobRequest statReq = (NoTaskJobRequest)request;
		MongoDao md = new MongoDao(db_name);
		try {
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			Map<Integer, Map<Integer, Set<Integer>>> cityMeshs = manApi.queryAllCityGrids();
			String timestamp = statReq.getTimestamp();

			// 查询并统计所有的无任务poi数据
			Map<Integer, Object> poiMap = queryAllPoiData(timestamp, md);
			// 查询并统计所有的无任务tips数据
			Map<Integer, Object> tipsMap = queryAllTipsData(timestamp, md);
			//根据block和city处理分类统计数据
			Map<String, Object> result = convertData(poiMap, tipsMap, cityMeshs);

			List<Map<String, Object>> cityStat = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> blockStat = new ArrayList<Map<String, Object>>();

			Map<Integer, Map<String, Object>> cityNoTask = (Map<Integer, Map<String, Object>>) result.get("city");
			for(Map.Entry<Integer, Map<String, Object>> entry : cityNoTask.entrySet()){
				Map<String, Object> cell = new HashMap<String, Object>();
				cell.put("cityId", entry.getKey());
				cell.put("tipsTotal", entry.getValue().get("tipsTotal"));
				cell.put("poiTotal", entry.getValue().get("poiTotal"));
				cell.put("dealershipTotal", entry.getValue().get("dealershipTotal"));
				cell.put("noDealershipTotal", entry.getValue().get("noDealershipTotal"));
				cityStat.add(cell);
			}

			Map<Integer, Map<String, Object>> blockNoTask = (Map<Integer, Map<String, Object>>) result.get("block");
			for(Map.Entry<Integer, Map<String, Object>> entry : blockNoTask.entrySet()){
				Map<String, Object> cell = new HashMap<String, Object>();
				cell.put("blockId", entry.getKey());
				cell.put("tipsTotal", entry.getValue().get("tipsTotal"));
				cell.put("poiTotal", entry.getValue().get("poiTotal"));
				cell.put("dealershipTotal", entry.getValue().get("dealershipTotal"));
				cell.put("noDealershipTotal", entry.getValue().get("noDealershipTotal"));
				blockStat.add(cell);
			}

			Map<String, Object> notaskData = new HashMap<>();
			notaskData.put("block_notask ", blockStat);
			notaskData.put("city_notask", cityStat);
			log.info("result:" + notaskData);
			
			return JSONObject.fromObject(notaskData).toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}
	}
		

	/**
	 * 处理city和block统计数据
	 * @param Map<Integer, Object>
	 * @param Map<Integer, Map<Integer, Set<Integer>>>
	 * @return Map<String, Object>
	 * 
	 * */
	public Map<String, Object> convertData(Map<Integer, Object> poiMap, Map<Integer, Object> tipsMap,
			Map<Integer, Map<Integer, Set<Integer>>> cityMeshs) {

		Map<Integer, Object> blocks = new HashMap<>();
		Map<Integer, Object> citys = new HashMap<>();
		Map<String, Object> result = new HashMap<>();
		for(Entry<Integer, Map<Integer, Set<Integer>>> cityEntry : cityMeshs.entrySet()){
			int cityId = cityEntry.getKey();
			int cityPoiCount = 0;
			int cityDealershipCount = 0;
			int cityTipCount = 0;
			int cityNoDealershipCount = 0;
			Map<String, Integer> cityData = new HashMap<>();
			Map<Integer, Set<Integer>> blockMap = cityEntry.getValue();
			for(Entry<Integer, Set<Integer>> blockEntry : blockMap.entrySet()){
				Map<String, Integer> blockData = new HashMap<>();
				int blockId = blockEntry.getKey();
				Set<Integer> grids = blockEntry.getValue();
				int blockPoiCount = 0;
				int blockDealershipCount = 0;
				int blockNoDealershipCount = 0;
				int blockTipCount = 0;
				for(Entry<Integer, Object> gridEntry : poiMap.entrySet()){
					int gridid = gridEntry.getKey();
					if(grids.contains(gridid)){
						Map<String, Integer> poiData = (Map<String, Integer>) gridEntry.getValue();
						blockPoiCount += poiData.get("poiCount");
						blockDealershipCount += poiData.get("dealershipCount");
						blockNoDealershipCount += poiData.get("dealershipCount");
					}
				}
				for(Entry<Integer, Object> tipsEntry : tipsMap.entrySet()){
					int gridid = tipsEntry.getKey();
					if(grids.contains(gridid)){
						blockTipCount += (int) tipsEntry.getValue();
					}
				}
				blockData.put("poiTotal", blockPoiCount);
				blockData.put("noDealershipTotal", blockNoDealershipCount);
				blockData.put("dealershipTotal", blockDealershipCount);
				blockData.put("tipsTotal", blockTipCount);
				blocks.put(blockId, blockData);
				cityPoiCount +=  blockPoiCount;
				cityDealershipCount += blockDealershipCount;
				cityTipCount += blockTipCount;
				cityNoDealershipCount += blockNoDealershipCount;
			}
			cityData.put("poiTotal", cityPoiCount);
			cityData.put("dealershipTotal", cityDealershipCount);
			cityData.put("tipsTotal", cityTipCount);
			cityData.put("noDealershipTotal", cityNoDealershipCount);
			citys.put(cityId, cityData);
		}
		result.put("city", citys);
		result.put("block", blocks);
		return result;
	}

	/**
	 * 查询无任务tips数据
	 * @param String
	 * @param MongoDao
	 * @return Map<Integer, Object>
	 * @throws Exception
	 * 
	 */
	public Map<Integer, Object> queryAllTipsData(String timestamp, MongoDao md) {
		
		Map<Integer, Object> noTasks = new HashMap<>();

		String planTableName = "grid_notask_tips";
		BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
		MongoCursor<Document> plan = md.find(planTableName, filter).iterator();
		while (plan.hasNext()) {
			
			JSONObject json = JSONObject.fromObject(plan.next());
			int gridId = json.getInt("gridId");
			int tipsTotal = json.getInt("noTaskTotal");
			noTasks.put(gridId, tipsTotal);
		}
		return noTasks;
	}

	/**
	 * 查询无任务POI数据
	 * @param String
	 * @param MongoDao
	 * @return Map<Integer, Object>
	 * @throws Exception
	 * 
	 */
	public Map<Integer, Object> queryAllPoiData(String timestamp, MongoDao md) {
		
		Map<Integer, Object> noTasks = new HashMap<>();

		String planTableName = "grid_day_poi";
		BasicDBObject filter = new BasicDBObject("timestamp", timestamp);
		MongoCursor<Document> plan = md.find(planTableName, filter).iterator();
		while (plan.hasNext()) {
			
			JSONObject json = JSONObject.fromObject(plan.next());
			int gridId = json.getInt("gridId");
			
			Map<String, Object> data = new HashMap<>();
			int noDealershipNum = json.getInt("noDealershipNum");
			int dealershipNum = json.getInt("dealershipNum");
			int poiNum = json.getInt("poiNum");
			data.put("dealershipCount", dealershipNum);
			data.put("poiCount", poiNum);
			data.put("noDealershipCount", noDealershipNum);
			
			noTasks.put(gridId, data);
		}
		return noTasks;
	}

}
