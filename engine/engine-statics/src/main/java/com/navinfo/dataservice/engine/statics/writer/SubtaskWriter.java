package com.navinfo.dataservice.engine.statics.writer;

import java.sql.Connection;
import java.sql.Timestamp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 子任务数据写入oracle
 * @ClassName SubtaskWriter
 * @author Han Shaoming
 * @date 2017年8月1日 下午1:53:28
 * @Description TODO
 */
public class SubtaskWriter extends DefaultWriter {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
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
				Object[][] valueList=new Object[content.size()][16];
				for(int i=0;i<content.size();i++){
					JSONObject jso = content.getJSONObject(i);
					int subtaskId = (int) jso.get("subtaskId");
					int type = (int) jso.get("type");
					int status = (int) jso.get("status");
					int diffDate = (int) jso.get("diffDate");
					int planDate = (int) jso.get("planDate");
					int tipsAllNum = (int) jso.get("tipsAllNum");
					int tipsFinishNum = (int) jso.get("tipsFinishNum");
					int monthPoiLogTotalNum = (int) jso.get("monthPoiLogTotalNum");
					int monthPoiLogFinishNum = (int) jso.get("monthPoiLogFinishNum");
					int poiCollectUploadNum = (int) jso.get("poiCollectUploadNum");
					int poiFinishNum = (int) jso.get("poiFinishNum");
					int poiDayPercent = (int) jso.get("poiDayPercent");
					int roadPercent = (int) jso.get("roadPercent");
					String actualStartDate = (String) jso.get("actualStartDate");
					String actualEndDate = (String) jso.get("actualEndDate");
					int progress = (int) jso.get("progress");
					int percent = (int) jso.get("percent");
					int programType = (int) jso.get("programType");
					//处理数据
					int totalPoi = 0;
					int finishedPoi = 0;
					int percentPoi = 0;
					int totalRoad = 0;
					int finishedRoad = 0;
					int percentRoad = 0;
					//中线
					if(programType == 1){
						//POI_采集
						if(type==0){
							//poi粗编完成度
							totalPoi = poiCollectUploadNum;
							finishedPoi = poiFinishNum;
							percentPoi = poiDayPercent;
						}
					}
					//快线
					else if(programType == 4){
						//一体化_grid粗编_日编
						if(type==3){
							totalRoad = tipsAllNum;
							finishedRoad = tipsFinishNum;
							percentRoad = roadPercent;
						}
					}
					//POI专项_月编(中线,快线相同)
					if(type ==7){
						totalPoi = monthPoiLogTotalNum;
						finishedPoi = monthPoiLogFinishNum;
						percentPoi = percent;
					}
					//保存数据
					Object[] value=new Object[16];
					value[0] = subtaskId;
					value[1] = percent;
					value[2] = diffDate;
					value[3] = progress;
					value[4] = status;
					
					value[5] = totalPoi;
					value[6] = finishedPoi;
					value[7] = totalRoad;
					value[8] = finishedRoad;
					value[9] = percentPoi;
					value[10] = percentRoad;
					value[11] = DateUtils.stringToTimestamp(actualStartDate, "yyyyMMddHHmmss");
					value[12] = DateUtils.stringToTimestamp(actualEndDate, "yyyyMMddHHmmss");
					value[13] = planDate;
					value[14] = statDate;
					value[15] = statDate;
					
					valueList[i] = value;
				}
				
				String dropSql="truncate table FM_STAT_OVERVIEW_SUBTASK";
				String insertSql="INSERT INTO FM_STAT_OVERVIEW_SUBTASK (SUBTASK_ID,PERCENT,DIFF_DATE,PROGRESS,STATUS,"
						+ "TOTAL_POI,FINISHED_POI,TOTAL_ROAD,FINISHED_ROAD,PERCENT_POI,PERCENT_ROAD,ACTUAL_START_DATE,"
						+ "ACTUAL_END_DATE,PLAN_DATE,STAT_TIME,STAT_DATE,TASK_ID) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0)";
				QueryRunner run=new QueryRunner();
				run.execute(conn, dropSql);
				run.batch(conn, insertSql,valueList);
			}
			log.info("end write2Oracle");
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("子任务数据写入oracle出错:" + e.getMessage(), e);
			throw new Exception("子任务数据写入oracle出错:" + e.getMessage(), e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
