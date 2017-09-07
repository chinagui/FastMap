package com.navinfo.dataservice.engine.statics.writer;

import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.DateUtils;
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
public class ProgramWriter extends DefaultWriter {
	
	private SimpleDateFormat sd = new SimpleDateFormat("yyyyMMddHHmmss");
	
	public void write2Other(String timestamp,JSONObject messageJSON) throws Exception {
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
					value[9] = DateUtils.stringToTimestamp(actualEndDate, "yyyyMMddHHmmss");
					value[10] = DateUtils.stringToTimestamp(collectAcutalStartDate, "yyyyMMddHHmmss");
					value[11] = DateUtils.stringToTimestamp(collectAcutalEndDate, "yyyyMMddHHmmss");
					value[12] = DateUtils.stringToTimestamp(dayAcutalStartDate, "yyyyMMddHHmmss");
					value[13] = DateUtils.stringToTimestamp(dayAcutalEndDate, "yyyyMMddHHmmss");
					value[14] = DateUtils.stringToTimestamp(monthAcutalStartDate, "yyyyMMddHHmmss");
					value[15] = DateUtils.stringToTimestamp(monthAcutalEndDate, "yyyyMMddHHmmss");
					
					valueList[i] = value;
				}
				
				String dropSql = "truncate table FM_STAT_OVERVIEW_PROGRAM";
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
}
