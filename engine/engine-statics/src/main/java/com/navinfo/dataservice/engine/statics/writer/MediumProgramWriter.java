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
public class MediumProgramWriter extends DefaultWriter {
	
	public void write2Other(String timestamp,JSONObject messageJSON) throws Exception {
		ProgramWriterUtils utils=new ProgramWriterUtils();
		utils.write2Other(timestamp, messageJSON, 1);
	}
	
	public void initMongoDb(String collectionName,String timestamp,JSONObject identifyJson) {
		ProgramWriterUtils utils=new ProgramWriterUtils();
		utils.initMongoDb(collectionName,timestamp, identifyJson, 1);
	}
}
