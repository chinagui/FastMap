package com.navinfo.dataservice.engine.statics.writer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.navinfo.dataservice.engine.statics.tools.MongoDao;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DayPlanWriter extends DefaultWriter{
	
	/**
	 * 统计信息写入mongo库
	 * @param messageJSON
	 */
	@Override
	public void write2Mongo(String timestamp,JSONObject identifyJson,JSONObject messageJSON){
		for(Object collectionNameTmp:messageJSON.keySet()){
			String collectionName=String.valueOf(collectionNameTmp);
			//初始化统计collection
			initMongoDb(collectionName,timestamp,identifyJson);
			
			List<Map<String,Double>> list = (List<Map<String, Double>>) messageJSON.get(collectionName);
			
			Iterator<Map<String, Double>> it = list.iterator();
            while (it.hasNext()) {
            	Map<String, Double> map = (Map<String, Double>) it.next();
               //统计信息入库
    			Document resultDoc=new Document();
    			resultDoc.put("taskId",map.get("taskId"));
    			resultDoc.put("linkPlanLen",map.get("linkPlanLen"));
    			resultDoc.put("linkAllLen",map.get("linkAllLen"));
    			resultDoc.put("poiPlanNum",map.get("poiPlanNum"));
    			resultDoc.put("poiAllNum",map.get("poiAllNum"));
    			resultDoc.put("link17AllLen",map.get("link17AllLen"));
    			resultDoc.put("link27AllLen",map.get("link27AllLen"));
    	
    			MongoDao md = new MongoDao(dbName);
    			md.insertOne(collectionName, resultDoc);
            }
			
		}
		log.info("end write2Mongo");
	}

}
