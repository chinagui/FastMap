package com.navinfo.dataservice.scripts.tmp;

import java.sql.Clob;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.txt.TxtReader;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.scripts.JobScriptsInterface;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class ImportGeo {
	private static Logger log = LogManager.getLogger(ImportGeo.class);
	/**
	 * 参数文件路径：D:\\temp\\test.txt
	 * txt格式： 
	 * 2021 LINESTRING(85.67859999999999 47.41666666666665,85.87909999999998 47.41666666666665)
	 * GEO2 LINESTRING(85.67859999999999 47.41666666666665,85.87909999999998 47.41666666666665)
	 * 导入的目标位置：man库,geo_tmp表，表结构：(id varchar(100),geo sdo_geometry() )
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		log.info("start");
		try{
			JobScriptsInterface.initContext();
			String filepath = String.valueOf(args[0]);
			//String filepath ="D:\\temp\\1779.txt";
			Connection conn=DBConnector.getInstance().getManConnection();
			log.info("read txt start");
			List<String> contents=TxtReader.readTxtFile(filepath);
			List<Map<String,String>> sources=new ArrayList<>();
			for(String line:contents){
				Map<String,String> map=new HashMap<>();
				String[] lineList = line.split(" ");
				String id=lineList[0];
				String wkt=line.substring(id.length()+1);//.replace(id+" ", "");
				map.put("id", id);
				map.put("geo", wkt);
				sources.add(map);
			}
			log.info("read txt end");
			String sql="insert into geo_tmp(id,geo) values(?,sdo_geometry(?,8307))";
			QueryRunner run=new QueryRunner();
			Object[][] params=new Object[sources.size()][2];
			for(int i=0;i<sources.size();i++){
				params[i][0]=String.valueOf(sources.get(i).get("id"));
				String geoWkt=(String) sources.get(i).get("geo");
				//log.info(geoWkt);
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, geoWkt);
				//STRUCT struct = GeoTranslator.wkt2Struct(conn, geoWkt);
				//log.info( GeoTranslator.struct2Wkt(struct));
				//params[i][1]=struct;
				params[i][1]=clob;
			}
			run.batch(conn, sql, params);
			log.info("insert txt end");
			DbUtils.commitAndCloseQuietly(conn);
			log.info("end");
		}catch (Exception e) {
			log.error("", e);
		}
		System.exit(0);
	}
}
