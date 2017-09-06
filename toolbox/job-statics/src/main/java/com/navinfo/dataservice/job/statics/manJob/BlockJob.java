package com.navinfo.dataservice.job.statics.manJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;

import net.sf.json.JSONObject;

public class BlockJob extends AbstractStatJob {
	private static final String db_name = SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);

	public BlockJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String stat() throws JobException {
		BlockJobRequest statReq = (BlockJobRequest)request;
		MongoDao md = new MongoDao(db_name);
		try {
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			String timestamp = statReq.getTimestamp();
			log.info("获取全部区县信息");
			Map<Integer, Map<String, Object>> blockMap = manApi.blockStatic();
			log.info(blockMap);
			//从mango库中查询数据
			log.info("获取无数据区县信息");
			Map<Integer, Map<String, Object>> notaskMap = queryBlockNotaskData(timestamp, md);
			
			List<Map<String, Object>> keyMaps = new ArrayList<>();
			Set<Integer> idIter = blockMap.keySet();
			for(int blockId:idIter){
				Map<String, Object> blockTmp=blockMap.get(blockId);
				if(notaskMap.containsKey(blockId)){
					blockTmp.put("tipsTotal",notaskMap.get(blockId).get("tipsTotal"));
					blockTmp.put("dealershipTotal",notaskMap.get(blockId).get("dealershipTotal"));
					blockTmp.put("noDealershipTotal",notaskMap.get(blockId).get("noDealershipTotal"));
				}
				keyMaps.add(blockTmp);
			}			
			Map<String, Object> result = new HashMap<>();
			result.put("block", keyMaps);
			log.info("end blockJob");
			log.info(JSONObject.fromObject(result).toString());
			return JSONObject.fromObject(result).toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}
	}
		
		/**
		 * 从mango库中查询任务统计数据
		 * @param String
		 * @param Map<Integer, Object>
		 * 
		 * */
		public Map<Integer, Map<String, Object>> queryBlockNotaskData(String timestamp, MongoDao md){
			
			Map<Integer, Map<String, Object>> result = new HashMap<>();
			String blockNotaskName = "block_notask";
			BasicDBObject query = new BasicDBObject();
			query.put("timestamp", timestamp);		
			MongoCursor<Document> blockNotask = md.find(blockNotaskName, query).iterator();
			//统计一个任务下所有子任务的personTips
			while(blockNotask.hasNext()){
				JSONObject notaskJson = JSONObject.fromObject(blockNotask.next());
				Map<String, Object> map = new HashMap<>();
				map.putAll(notaskJson);
				result.put(notaskJson.getInt("blockId"), map);
			}
			return result;
		}

}
