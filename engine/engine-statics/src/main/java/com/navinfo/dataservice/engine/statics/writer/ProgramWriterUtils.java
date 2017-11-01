package com.navinfo.dataservice.engine.statics.writer;

import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 项目数据写入oracle
 * @ClassName ProgramWriter
 * @author songhe
 * @date 2017年9月5日
 * 
 */
public class ProgramWriterUtils{
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	private SimpleDateFormat sd = new SimpleDateFormat("yyyyMMddHHmmss");
	protected String dbName=SystemConfigFactory.getSystemConfig().getValue(PropConstant.fmStat);
	
	public void write2Other(String timestamp,JSONObject messageJSON,int programType) throws Exception {
		Connection conn=null;
		try{
			log.info("start write2Oracle");
			conn = DBConnector.getInstance().getManConnection();
			for(Object collectionNameTmp : messageJSON.keySet()){
				String collectionName = String.valueOf(collectionNameTmp);
				//统计日期
				Timestamp statDate = DateUtils.stringToTimestamp(timestamp, "yyyyMMddHHmmss");
				//处理数据
				JSONArray content = messageJSON.getJSONArray(collectionName);
				Object[][] valueList = new Object[content.size()][16];
				for(int i = 0; i < content.size(); i++){
					JSONObject jso = content.getJSONObject(i);
					int type = (int) jso.get("type");
					int status = (int) jso.get("status");
					String actualEndDate = (String) jso.get("actualEndDate");
					double roadPlanTotal = Double.valueOf(String.valueOf(jso.get("roadPlanTotal")));
					int poiPlanTotal = (int) jso.get("poiPlanTotal");
					int programId = (int) jso.get("programId");
					int prograss = (int) jso.get("isOverDue");
					int cityId = (int) jso.get("cityId");
					int inforId = (int) jso.get("inforId");
					String collectAcutalStartDate = StringUtils.isBlank(jso.get("collectAcutalStartDate").toString()) ? sd.format(new Date()) : jso.get("collectAcutalStartDate").toString();
					String collectAcutalEndDate = StringUtils.isBlank(jso.get("collectAcutalEndDate").toString()) ? sd.format(new Date()) : jso.get("collectAcutalEndDate").toString();
					String dayAcutalStartDate = StringUtils.isBlank(jso.get("dayAcutalStartDate").toString()) ? sd.format(new Date()) : jso.get("dayAcutalStartDate").toString();
					String dayAcutalEndDate = StringUtils.isBlank(jso.get("dayAcutalEndDate").toString()) ? sd.format(new Date()) : jso.get("dayAcutalEndDate").toString();
					String monthAcutalStartDate = StringUtils.isBlank(jso.get("monthAcutalStartDate").toString()) ? sd.format(new Date()) : jso.get("monthAcutalStartDate").toString();
					String monthAcutalEndDate = StringUtils.isBlank(jso.get("monthAcutalEndDate").toString()) ? sd.format(new Date()) : jso.get("monthAcutalEndDate").toString();
					
					//保存数据
					Object[] value = new Object[16];
					value[0] = programId;
					value[1] = status;
					value[2] = statDate;
					value[3] = prograss;
					value[4] = poiPlanTotal;
					value[5] = type;
					value[6] = cityId;
					value[7] = inforId;
					value[8] = roadPlanTotal;
					value[9] = DateUtils.stringToTimestamp(actualEndDate, DateUtils.DATE_YMD);
					value[10] = DateUtils.stringToTimestamp(collectAcutalStartDate,DateUtils.DATE_YMD);
					value[11] = DateUtils.stringToTimestamp(collectAcutalEndDate, DateUtils.DATE_YMD);
					value[12] = DateUtils.stringToTimestamp(dayAcutalStartDate, DateUtils.DATE_YMD);
					value[13] = DateUtils.stringToTimestamp(dayAcutalEndDate, DateUtils.DATE_YMD);
					value[14] = DateUtils.stringToTimestamp(monthAcutalStartDate, DateUtils.DATE_YMD);
					value[15] = DateUtils.stringToTimestamp(monthAcutalEndDate, DateUtils.DATE_YMD);
					
					valueList[i] = value;
				}
				
				String dropSql = "delete from FM_STAT_OVERVIEW_PROGRAM o where exists(select 1 from program p where p.type="+programType+" and p.program_id=o.program_id)";
				String insertSql = "INSERT INTO FM_STAT_OVERVIEW_PROGRAM "
						+ "(PROGRAM_ID,STATUS,STAT_DATE,PROGRESS,POI_PLAN_TOTAL,"
						+ "TYPE,CITY_ID,INFOR_ID,ROAD_PLAN_TOTAL,ACTUAL_END_DATE,"
						+ "COLLECT_ACTUAL_START_DATE,COLLECT_ACTUAL_END_DATE,"
						+ "DAILY_ACTUAL_START_DATE,DAILY_ACTUAL_END_DATE,"
						+ "MONTHLY_ACTUAL_START_DATE,MONTHLY_ACTUAL_END_DATE) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				QueryRunner run = new QueryRunner();
				run.execute(conn, dropSql);
				run.batch(conn, insertSql, valueList);
			}
			log.info("end write2Oracle");
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("项目数据写入oracle出错:" + e.getMessage(), e);
			throw new Exception("项目数据写入oracle出错:" + e.getMessage(), e);
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
	public void initMongoDb(String collectionName,String timestamp,JSONObject identifyJson,int type) {
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
					new BasicDBObject("type", 1));
			log.info("-- -- create mongo collection " + collectionName + " ok");
			log.info("-- -- create mongo index on " + collectionName + "(timestamp,type) ok");
		}
		
		// 删除时间点相同的重复统计数据
		log.info("删除时间点相同的重复统计数据 mongo "+collectionName+",identifyJson="+identifyJson);
		BasicDBObject query = new BasicDBObject();
		query.putAll(identifyJson);
		query.put("type", type);
		mdao.deleteMany(collectionName, query);
	}
}
