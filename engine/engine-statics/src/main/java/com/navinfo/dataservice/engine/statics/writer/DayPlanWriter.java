package com.navinfo.dataservice.engine.statics.writer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.Document;

import com.navinfo.dataservice.engine.statics.tools.MongoDao;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DayPlanWriter extends DefaultWriter{
	
	/**
	 * 统计信息写入mongo库
	 * @param messageJSON
	 */
	public void write2Mongo(String timestamp,JSONObject messageJSON){
		for(Object collectionNameTmp:messageJSON.keySet()){
			String collectionName=String.valueOf(collectionNameTmp);
			//初始化统计collection
			initMongoDb(collectionName,timestamp);
			
			JSONArray jsonArray = messageJSON.getJSONArray(collectionName);
			
			Iterator<Object> it = jsonArray.iterator();
            while (it.hasNext()) {
                JSONObject jo = (JSONObject) it.next();
               //统计信息入库
    			Document resultDoc=new Document();
    			resultDoc.put("taskId",jo.get("taskId"));
    			resultDoc.put("linkPlanLen",jo.get("linkPlanLen"));
    			resultDoc.put("linkAllLen",jo.get("linkAllLen"));
    			resultDoc.put("poiPlanNum",jo.get("poiPlanNum"));
    			resultDoc.put("poiAllNum",jo.get("poiAllNum"));
    			resultDoc.put("link17AllLen",jo.get("link17AllLen"));
    			resultDoc.put("link27AllLen",jo.get("link27AllLen"));
    	
    			MongoDao md = new MongoDao(dbName);
    			md.insertOne(collectionName, resultDoc);
            }
			
		}
		log.info("end write2Mongo");
	}

}
