package com.navinfo.dataservice.engine.statics.writer;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

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
public class CityWriter extends DefaultWriter {
	
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
				Object[][] valueList = new Object[content.size()][4];
				for(int i = 0; i < content.size(); i++){
					JSONObject jso = content.getJSONObject(i);
					int cityId = (int) jso.get("cityId");
					int tipsTotal = (int) jso.get("tipsTotal");
					int poiTotal = (int) jso.get("poiTotal");
					
					//保存数据
					Object[] value = new Object[4];
					value[0] = cityId;
					value[1] = tipsTotal;
					value[2] = poiTotal;
					value[3] = statDate;
					
					valueList[i] = value;
				}
				
				String dropSql = "truncate table FM_STAT_OVERVIEW_CITY";
				String insertSql = "INSERT INTO FM_STAT_OVERVIEW_CITY"
						+ "(CITY_ID,NOTASK_TIPS_TOTAL,NOTASK_POI_TOTAL,STAT_DATE) "
						+ "VALUES (?,?,?,?)";
				QueryRunner run = new QueryRunner();
				run.execute(conn, dropSql);
				run.batch(conn, insertSql, valueList);
			}
			log.info("end write2Oracle");
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("城市数据写入oracle出错:" + e.getMessage(), e);
			throw new Exception("城市数据写入oracle出错:" + e.getMessage(), e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
