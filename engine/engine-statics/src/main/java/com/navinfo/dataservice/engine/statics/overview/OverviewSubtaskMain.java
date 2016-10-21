package com.navinfo.dataservice.engine.statics.overview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.statics.StatMain;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.poidaily.PoiDailyMain;
import com.navinfo.dataservice.engine.statics.roadcollect.RoadCollectMain;
import com.navinfo.dataservice.engine.statics.roaddaily.RoadDailyMain;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.OracleDao;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;

import net.sf.json.JSONObject;

/** 
 * @ClassName: OverviewSubtaskStat
 * @author songdongyan
 * @date 2016年10月18日
 * @Description: OverviewSubtaskStat.java
 */
public class OverviewSubtaskMain {

	/**
	 * 
	 */
	private static Logger log = null;
	public static final String col_name_subtask = "fm_stat_overview_subtask";
	private String db_name;
	private static String stat_date;
	private static String stat_time;

	public OverviewSubtaskMain(String dbn, String stat_time) {
		this.db_name = dbn;
		this.stat_date = stat_time.substring(0, 8);
		this.stat_time = stat_time;
	}
	
	
	/**
	 * 统计结果mongo结果库初始化
	 */
	private void initMongoDb() {

		MongoDao mdao = new MongoDao(db_name);
		MongoDatabase md = mdao.getDatabase();
		// 初始化 col_name_grid
		Iterator<String> iter_subtask = md.listCollectionNames().iterator();
		boolean flag_subtask = true;
		while (iter_subtask.hasNext()) {
			if (iter_subtask.next().equalsIgnoreCase(col_name_subtask)) {
				flag_subtask = false;
				break;
			}
		}

		if (flag_subtask) {
			md.createCollection(col_name_subtask);
			md.getCollection(col_name_subtask).createIndex(new BasicDBObject("subtaskId", 1));
			md.getCollection(col_name_subtask).createIndex(new BasicDBObject("statDate", 1));
			log.info("-- -- create mongo collection " + col_name_subtask + " ok");
			log.info("-- -- create mongo index on " + col_name_subtask + "(subtaskId，statDe) ok");
		}

		// 删除当天重复统计数据
		BasicDBObject query = new BasicDBObject();
		query.put("statDate", stat_date);
		mdao.deleteMany(col_name_subtask, query);

	}
	
	/**
	 * @param subtask
	 * @param roadColName 
	 * @param poiColName 
	 * @return
	 * @throws ParseException 
	 */
	public static Document getSubtaskStatThroughGrids(Subtask subtask, String poiColName, String roadColName) throws ParseException {
		// TODO Auto-generated method stub
		Document stat = new Document();

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		
		stat.put("subtaskId", subtask.getSubtaskId());
		stat.put("blockManId", subtask.getBlockManId());
		stat.put("status", subtask.getStatus());
		stat.put("planStartDate", df.format(subtask.getPlanStartDate()));
		stat.put("planEndDate", df.format(subtask.getPlanEndDate()));
		stat.put("planDate", StatUtil.daysOfTwo(subtask.getPlanStartDate(), subtask.getPlanEndDate()));
		stat.put("actualStartDate", df.format(subtask.getPlanStartDate()));
		stat.put("actualEndDate", df.format(subtask.getPlanEndDate()));
		stat.put("diffDate", StatUtil.daysOfTwo(df.parse(stat_date),subtask.getPlanStartDate()));
		stat.put("statDate", stat_date);
		stat.put("statTime", stat_time);

		
		List<Integer> gridIds = subtask.getGridIds();
		List<String> gridIdList = new ArrayList<String>();
		//grid进度详情
		Map<Integer,Integer> gridPercentDetailPOI = new HashMap<Integer,Integer>();
		Map<Integer,Integer> gridPercentDetailROAD = new HashMap<Integer,Integer>();
		
		for(int i = 0;i<gridIds.size();i++){
			String s = String.valueOf(gridIds.get(i));
			gridIdList.add(i, s);
			gridPercentDetailPOI.put(gridIds.get(i), 0);
			gridPercentDetailROAD.put(gridIds.get(i), 0);
		}
		
		int type = subtask.getType();

		int totalPOI = 0;
		int finishPOI = 0;
		int totalROAD = 0;
		int finishROAD = 0;

		MongoDao md = new MongoDao(StatMain.db_name);

		int total = gridIdList.size();
		//POI
		MongoCursor<Document> iter = md
				.find(poiColName, Filters.in("grid_id", gridIdList))
				.sort(Sorts.descending("stat_date")).batchSize(total)
				.iterator();

		int count = 0;
		while (iter.hasNext()) {

			JSONObject json = JSONObject.fromObject(iter.next());
			
			int gridId = json.getInt("grid_id");
			JSONObject poi = json.getJSONObject("poi");
			
			int percent = poi.getInt("percent");
			
			gridPercentDetailPOI.put(gridId, percent);
			
			totalPOI += poi.getInt("total");
			finishPOI += poi.getInt("finish");
			
			count++;
			if (count >= total) {
				break;
			}
		}
		//ROAD
		iter = md.find(roadColName, Filters.in("grid_id", gridIdList))
				.sort(Sorts.descending("stat_date")).batchSize(gridIdList.size())
				.iterator();

		count = 0;
		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());
			
			int gridId = json.getInt("grid_id");
			JSONObject road = json.getJSONObject("road");
			int percent = road.getInt("percent");
			gridPercentDetailROAD.put(gridId, percent);

			totalROAD += road.getInt("total");
			finishROAD += road.getInt("finish");

			count++;
			if (count >= total) {
				break;
			}
		}
		//详细信息
		Map<String,Integer> detailsPOI = new HashMap<String,Integer>();
		Map<String,Integer> detailsROAD = new HashMap<String,Integer>();
		Map<String,Map<String,Integer>> details = new HashMap<String,Map<String,Integer>>();
		
		detailsPOI.put("total", totalPOI);
		detailsPOI.put("finish", finishPOI);
		if(totalPOI > 0){
			detailsPOI.put("percent", finishPOI*100/totalPOI);
		}else{
			detailsPOI.put("percent", 0);
		}
		
		detailsROAD.put("total", totalROAD);
		detailsROAD.put("finish", finishROAD);
		if(totalROAD > 0){
			detailsROAD.put("percent", finishROAD*100/totalROAD);
		}else{
			detailsROAD.put("percent", 0);
		}
		
		details.put("poi", detailsPOI);
		details.put("road", detailsROAD);
		
		//grid进度详情
		if(type == 0){
			//POI
			stat.put("gridPercentDetails", gridPercentDetailPOI);
			stat.put("percent", detailsPOI.get("percent"));
		}else if (type == 1){
			//道路
			stat.put("gridPercentDetails", gridPercentDetailROAD);
			stat.put("percent", detailsROAD.get("percent"));
		}else{
			//一体化
			Map<Integer,Integer> gridPercentDetail = new HashMap<Integer,Integer>();
			for(Map.Entry<Integer, Integer> entry : gridPercentDetailPOI.entrySet()){
				int gridId = entry.getKey();
				int percent = (int) (gridPercentDetailPOI.get(gridId)*0.5 + gridPercentDetailROAD.get(gridId)*0.5);
				gridPercentDetail.put(gridId, percent);
			}
			stat.put("gridPercentDetails", gridPercentDetail);
			stat.put("percent", detailsPOI.get("percent")*0.5 + detailsROAD.get("percent")*0.5);
		}
		
		if((int)stat.get("diffDate") < 0){
			stat.put("progress", 2);
		}else{
			if((int)stat.get("planDate") == 0){
				if((int)stat.get("percent") == 100){
					stat.put("progress", 1);
				}else{
					stat.put("progress", 2);
				}
			}else{
				int percentSchedule = 100 - (int)stat.get("diffDate")*100/(int)stat.get("planDate");
				if((int)stat.get("percent") >= percentSchedule){
					stat.put("progress", 1);
				}else{
					stat.put("progress", 2);
				}
			}

		}
		
		return stat;
	}
	
	public Document getSubtaskStat(Subtask subtask) throws ParseException{
		Document doc = new Document();
		ManApi api=(ManApi) ApplicationContextUtil.getBean("manApi");

		//POI采集,道路采集，一体化采集
		if((subtask.getType()==0&&subtask.getStage()==0)
				||(subtask.getType()==1&&subtask.getStage()==0)
				||(subtask.getType()==2&&subtask.getStage()==0)){
			String poiColName = PoiCollectMain.col_name_grid;
			String roadColName = RoadCollectMain.col_name_grid;
			List<Integer> gridIds = null;
			try {
				gridIds = api.getGridIdsBySubtaskId(subtask.getSubtaskId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			subtask.setGridIds(gridIds);
			doc = getSubtaskStatThroughGrids(subtask,poiColName,roadColName);
		}
		//POI日编，一体化GRID粗编
		else if((subtask.getType()==0&&subtask.getStage()==1)
				||(subtask.getType()==3&&subtask.getStage()==1)){
			String poiColName = PoiDailyMain.col_name_grid;
			String roadColName = RoadDailyMain.col_name_grid;
			List<Integer> gridIds = null;
			try {
				gridIds = api.getGridIdsBySubtaskId(subtask.getSubtaskId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			subtask.setGridIds(gridIds);	
			doc = getSubtaskStatThroughGrids(subtask,poiColName,roadColName);
		}
		
		return doc;
	}
	
	public void runStat() {
		log = LogManager.getLogger(PoiCollectMain.class);

		log.info("-- begin stat:" + col_name_subtask);

		try {
			// 初始化mongodb数据库
			initMongoDb();

			//执行统计
			List<Subtask> subtaskListNeedStatistics = OracleDao.getSubtaskListNeedStatistics();
			List<Document> subtaskListWithStatistics = OracleDao.getSubtaskListWithStatistics();

			MongoDao md = new MongoDao(db_name);
			Iterator<Subtask> subtaskItr = subtaskListNeedStatistics.iterator();
			while(subtaskItr.hasNext()){
				Document subtask = getSubtaskStat(subtaskItr.next());
				subtaskListWithStatistics.add(subtask);
			}
			md.insertMany(col_name_subtask, subtaskListWithStatistics);
			
			log.info("-- end stat:" + col_name_subtask);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	public static void main(String[] args){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		OverviewSubtaskMain overviewSubtaskStat = new OverviewSubtaskMain("fm_stat", "201610191340");
		overviewSubtaskStat.runStat();
	}
}
