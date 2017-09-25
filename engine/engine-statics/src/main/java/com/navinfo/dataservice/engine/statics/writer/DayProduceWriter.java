package com.navinfo.dataservice.engine.statics.writer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.bson.Document;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import net.sf.json.JSONObject;

public class DayProduceWriter extends DefaultWriter{
	
	/**
	 * 统计信息写入mongo库
	 * @param messageJSON
	 * zl
	 */
	@Override
	public void write2Mongo(String timestamp,JSONObject identifyJson,JSONObject messageJSON){
		for(Object collectionNameTmp:messageJSON.keySet()){
			String collectionName=String.valueOf(collectionNameTmp);
			//初始化统计collection,删除当天的统计记录
			initMongoDbByDate(collectionName,timestamp);
			
			List<Map<String,Double>> list = (List<Map<String, Double>>) messageJSON.get(collectionName);
			
			Iterator<Map<String, Double>> it = list.iterator();
            while (it.hasNext()) {
            	Map<String, Double> map = (Map<String, Double>) it.next();
               //统计信息入库
    			Document resultDoc=new Document();
    			resultDoc.put("dpUpdateRoad",map.get("dpUpdateRoad"));
    			resultDoc.put("dpAddRoad",map.get("dpAddRoad"));
    			resultDoc.put("dpUpdatePoi",map.get("dpUpdatePoi"));
    			resultDoc.put("dpAddPoi",map.get("dpAddPoi"));
    			resultDoc.put("dpAverage",map.get("dpAverage"));
    	
    			MongoDao md = new MongoDao(dbName);
    			md.insertOne(collectionName, resultDoc);
            }
			
		}
		log.info("end write2Mongo");
	}

	
	/**
	 * @param collectionName
	 * @param timestamp
	 * @param 
	 */
	public void initMongoDbByDate(String collectionName,String timestamp) {
		log.info("init mongo "+collectionName);
		MongoDao mdao = new MongoDao(dbName);
		MongoDatabase md = mdao.getDatabase();
		// 初始化 col_name_grid
		Iterator<String> iter_grid = md.listCollectionNames().iterator();
		boolean flag_grid = true;
		while (iter_grid.hasNext()) {
			if (iter_grid.next().equalsIgnoreCase(collectionName)) {
				flag_grid = false;
				break;
			}
		}

		if (flag_grid) {
			md.createCollection(collectionName);
			md.getCollection(collectionName).createIndex(
					new BasicDBObject("timestamp", 1));
			createMongoSelfIndex(md, collectionName);
			log.info("-- -- create mongo collection " + collectionName + " ok");
			log.info("-- -- create mongo index on " + collectionName + "(timestamp) ok");
		}
		
		
		String dateStr = timestamp.substring(0,8); 
		// 删除当天的统计数据
		log.info("删除时间点相同的重复统计数据 mongo "+collectionName+",timestamp="+timestamp);
		Pattern pattern = Pattern.compile("^"+dateStr);
		BasicDBObject query = new BasicDBObject("timestamp", pattern);
		
		mdao.deleteMany(collectionName, query);
	}
	
	public static void main(String[] args) {
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00"); 
//		getAvgMap.put("updateRoad", Double.parseDouble(df.format(updateRoad)));
//		getAvgMap.put("addRoad", Double.parseDouble(df.format(addRoad)));
		double a = 2.3;
		double b = 5.5;
		int c = 3 ;
		int d = 10 ;
		System.out.println(Double.parseDouble(df.format(a/b)));
		System.out.println(df.format(a/b));
		
		System.out.println("int :"+((float)c/d) + "  :"+((float)d/c));
		System.out.println(Double.parseDouble(df.format(c/d)));
		System.out.println(df.format(c/d));
		
	}
}
