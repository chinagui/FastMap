package com.navinfo.dataservice.engine.statics.writer;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Iterator;

import org.apache.commons.dbutils.DbUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 任务数据写入oracle
 * @ClassName TaskWriter
 * @author Han Shaoming
 * @date 2017年8月4日 下午8:46:58
 * @Description TODO
 */
public class MediumTaskWriter extends DefaultWriter {
	
	public void write2Other(String timestamp,JSONObject messageJSON) throws Exception {
		Connection conn=null;
		try{
			log.info("start write2Oracle");
			conn=DBConnector.getInstance().getManConnection();
			for(Object collectionNameTmp:messageJSON.keySet()){
				String collectionName=String.valueOf(collectionNameTmp);
				//统计日期
				Timestamp statDate = DateUtils.stringToTimestamp(timestamp, "yyyyMMddHHmmss");
				//处理数据
				JSONArray content = messageJSON.getJSONArray(collectionName);
				Object[][] valueList=new Object[content.size()][15];
				for(int i=0;i<content.size();i++){
					JSONObject jso = content.getJSONObject(i);
					int taskId = (int) jso.get("taskId");
					int type = (int) jso.get("type");
					int status = (int) jso.get("status");
					int planDate = (int) jso.get("planDate");
					String actualStartDate = (String) jso.get("actualStartDate");
					String actualEndDate = (String) jso.get("actualEndDate");
					int diffDate = (int) jso.get("diffDate");
					int roadPlanTotal=0;
					try{
						float roadPlanTotalfloat = (float) jso.get("roadPlanTotal");
						roadPlanTotal=(int) roadPlanTotalfloat;
					}catch (Exception e) {
						try {
							double roadPlanTotaldouble = (double) jso.get("roadPlanTotal");
							roadPlanTotal=(int) roadPlanTotaldouble;
						} catch (Exception e2) {
							roadPlanTotal=(int) jso.get("roadPlanTotal");
						}			
					}
					int poiPlanTotal = (int) jso.get("poiPlanTotal");
					int notaskdata_poi_num = (int) jso.get("notaskPoiNum");
					int notaskdata_tips_num = (int) jso.get("notaskTipsNum");
					int percent = (int) jso.get("percent");
					int progress = (int) jso.get("progress");
					int programId = (int) jso.get("programId");
					//保存数据
					Object[] value=new Object[16];
					value[0] = taskId;
					value[1] = progress;
					value[2] = percent;
					value[3] = status;
					value[4] = diffDate;
					
					value[5] = poiPlanTotal;
					value[6] = roadPlanTotal;
					value[7] = type;
					value[8] = planDate;
					value[9] = DateUtils.stringToTimestamp(actualStartDate, "yyyyMMddHHmmss");
					value[10] = DateUtils.stringToTimestamp(actualEndDate, "yyyyMMddHHmmss");
					value[11] = notaskdata_poi_num;
					value[12] = notaskdata_tips_num;
					value[13] = statDate;
					value[14] = statDate;
					value[15] = programId;
					
					valueList[i] = value;
				}
				
				String dropSql="delete from FM_STAT_OVERVIEW_TASK o where exists (select 1 from task t where t.task_id=o.task_id and o.block_id!=0)";
				String insertSql="INSERT INTO FM_STAT_OVERVIEW_TASK (TASK_ID,PROGRESS,PERCENT,STATUS,"
						+ "DIFF_DATE,POI_PLAN_TOTAL,ROAD_PLAN_TOTAL,TYPE,PLAN_DATE,"
						+ "ACTUAL_START_DATE,ACTUAL_END_DATE,NOTASKDATA_POI_NUM,NOTASKDATA_TIPS_NUM,"
						+ "STAT_DATE,STAT_TIME,CONVERT_FLAG,PROGRAM_ID,GROUP_ID) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,?,0)";
				QueryRunner run=new QueryRunner();
				run.execute(conn, dropSql);
				run.batch(conn, insertSql,valueList);
			}
			log.info("end write2Oracle");
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("任务数据写入oracle出错:" + e.getMessage(), e);
			throw new Exception("任务数据写入oracle出错:" + e.getMessage(), e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 统计结果mongo结果库初始化
	 * 1.判断是否有collection，如果没有就自动创建，并建立默认索引，有特殊索引需求，单独继承该类
	 * 2.删除时间点相同的重复统计数据
	 * @param collectionName
	 */
	public void initMongoDb(String collectionName,String timestamp,JSONObject identifyJson) {
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
			md.getCollection(collectionName).createIndex(
					new BasicDBObject("programType", 1));
			createMongoSelfIndex(md, collectionName);
			log.info("-- -- create mongo collection " + collectionName + " ok");
			log.info("-- -- create mongo index on " + collectionName + "(timestamp,workDay) ok");
		}
		
		// 删除时间点相同的重复统计数据
		log.info("删除时间点相同的重复统计数据 mongo "+collectionName+",identifyJson="+identifyJson);
		BasicDBObject query = new BasicDBObject();
		query.putAll(identifyJson);
		query.put("programType", 1);
		mdao.deleteMany(collectionName, query);
	}
}
