package com.navinfo.dataservice.engine.statics.writer;

import java.sql.Connection;
import java.sql.Timestamp;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.DateUtils;
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
public class TaskWriter extends DefaultWriter {
	
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
				
				String dropSql="truncate table FM_STAT_OVERVIEW_TASK";
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
}
