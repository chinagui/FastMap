package com.navinfo.dataservice.engine.statics.overview;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
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
import com.navinfo.dataservice.engine.statics.tools.StatInit;
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
//		stat.put("blockManId", subtask.getBlockManId());
		stat.put("status", subtask.getStatus());
		stat.put("planStartDate", df.format(subtask.getPlanStartDate()));
		stat.put("planEndDate", df.format(subtask.getPlanEndDate()));
		stat.put("planDate", StatUtil.daysOfTwo(subtask.getPlanStartDate(), subtask.getPlanEndDate()));
		stat.put("actualStartDate", df.format(subtask.getPlanStartDate()));
		stat.put("actualEndDate", df.format(subtask.getPlanEndDate()));
		stat.put("diffDate", StatUtil.daysOfTwo(df.parse(stat_date),subtask.getPlanStartDate()));
		stat.put("statDate", stat_date);
		stat.put("statTime", stat_time);

		
		List<Integer> gridIds = (List<Integer>) subtask.getGridIds().keySet();
		List<String> gridIdList = new ArrayList<String>();
		//grid进度详情
		Map<String,Integer> gridPercentDetailPOI = new HashMap<String,Integer>();
		Map<String,Integer> gridPercentDetailROAD = new HashMap<String,Integer>();
		
		for(int i = 0;i<gridIds.size();i++){
			String s = String.valueOf(gridIds.get(i));
			gridIdList.add(i, s);
			gridPercentDetailPOI.put(s, 0);
			gridPercentDetailROAD.put(s, 0);
		}
		
		int type = subtask.getType();

		int totalPoi = 0;
		int finishedPoi = 0;
		int percentPoi = 0;
		int totalRoad = 0;
		int finishedRoad = 0;
		int percentRoad = 0;

		MongoDao md = new MongoDao(StatMain.db_name);

		Pattern pattern = Pattern.compile("^" + stat_date + ".*$", Pattern.CASE_INSENSITIVE);
		BasicDBObject query = new BasicDBObject();
		query.put("stat_date", pattern);
		query.put("grid_id",new BasicDBObject(QueryOperators.IN, gridIdList));
		
		//POI
		MongoCursor<Document> iter = md.find(poiColName, query).iterator();

		while (iter.hasNext()) {

			JSONObject json = JSONObject.fromObject(iter.next());
			
			String gridId = json.getString("grid_id");
			JSONObject poi = json.getJSONObject("poi");
			
			int percent = poi.getInt("percent");
			
			gridPercentDetailPOI.put(gridId, percent);
			
			totalPoi += poi.getInt("total");
			finishedPoi += poi.getInt("finish");
		}
		//ROAD
		iter = md.find(roadColName, query).iterator();

		while (iter.hasNext()) {
			JSONObject json = JSONObject.fromObject(iter.next());
			
			String gridId = json.getString("grid_id");
			JSONObject road = json.getJSONObject("road");
			int percent = road.getInt("percent");
			gridPercentDetailROAD.put(gridId, percent);

			totalRoad += road.getInt("total");
			finishedRoad += road.getInt("finish");
		}
		
		if(totalPoi > 0){
			percentPoi = finishedPoi*100/totalPoi;
		}else{
			percentPoi = 0;
		}

		if(totalRoad > 0){
			percentRoad = finishedRoad*100/totalRoad;
		}else{
			percentRoad = 0;
		}

		stat.put("totalPoi", totalPoi);
		stat.put("finishedPoi", finishedPoi);
		stat.put("percentPoi", percentPoi);
		stat.put("totalRoad", totalRoad);
		stat.put("finishedRoad", finishedRoad);
		stat.put("percentRoad", percentRoad);
		
		
		//grid进度详情
		if(type == 0){
			//POI
			stat.put("gridPercentDetails", gridPercentDetailPOI);
			stat.put("percent", percentPoi);
		}else if (type == 1){
			//道路
			stat.put("gridPercentDetails", gridPercentDetailROAD);
			stat.put("percent", percentRoad);
		}else{
			//一体化
			Map<String,Integer> gridPercentDetail = new HashMap<String,Integer>();
			for(Map.Entry<String, Integer> entry : gridPercentDetailPOI.entrySet()){
				String gridId = entry.getKey();
				int percent = (int) (gridPercentDetailPOI.get(gridId)*0.5 + gridPercentDetailROAD.get(gridId)*0.5);
				gridPercentDetail.put(gridId, percent);
			}
			stat.put("gridPercentDetails", gridPercentDetail);
			stat.put("percent", (int)(percentPoi*0.5 + percentRoad*0.5));
		}
		
		
		if((int)stat.get("diffDate") < 0){
			stat.put("progress", 2);
		}else{
			if((int)stat.get("planDate") == 0){
				if(stat.getInteger("percent") == 100){
					stat.put("progress", 1);
				}else{
					stat.put("progress", 2);
				}
			}else{
				int percentSchedule = 100 - (int)stat.get("diffDate")*100/(int)stat.get("planDate");
				if(stat.getInteger("percent") >= percentSchedule){
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
			
//			subtask.setGridIds(gridIds);
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
//			subtask.setGridIds(gridIds);	
			doc = getSubtaskStatThroughGrids(subtask,poiColName,roadColName);
		}
		//一体化区域粗编
		else if(subtask.getType()==4&&subtask.getStage()==1){
			doc = getSubtaskStatSpecial(subtask);
		}
		return doc;
	}
	
	/**
	 * @param subtask
	 * @return
	 * @throws ParseException 
	 */
	private Document getSubtaskStatSpecial(Subtask subtask) throws ParseException {
		// TODO Auto-generated method stub
		Document stat = new Document();

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		
		stat.put("subtaskId", subtask.getSubtaskId());
//		stat.put("blockManId", subtask.getBlockManId());
		stat.put("status", subtask.getStatus());
		stat.put("planStartDate", df.format(subtask.getPlanStartDate()));
		stat.put("planEndDate", df.format(subtask.getPlanEndDate()));
		stat.put("planDate", StatUtil.daysOfTwo(subtask.getPlanStartDate(), subtask.getPlanEndDate()));
		stat.put("actualStartDate", df.format(subtask.getPlanStartDate()));
		stat.put("actualEndDate", df.format(subtask.getPlanEndDate()));
		stat.put("diffDate", StatUtil.daysOfTwo(df.parse(stat_date),subtask.getPlanStartDate()));
		stat.put("statDate", stat_date);
		stat.put("statTime", stat_time);
		
		stat.put("totalPoi", 0);
		stat.put("finishedPoi", 0);
		stat.put("percentPoi", 100);
		stat.put("totalRoad", 0);
		stat.put("finishedRoad", 0);
		stat.put("percentRoad", 100);
		
		stat.put("gridPercentDetails", new HashMap<String,Integer>());
		stat.put("percent", 100);
		stat.put("progress", 1);
		
		return stat;
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
//				break;
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
		OverviewSubtaskMain overviewSubtaskStat = new OverviewSubtaskMain("fm_stat", "201610221340");
		overviewSubtaskStat.runStat();
	}
}
