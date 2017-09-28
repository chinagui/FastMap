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

import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.txt.TxtReader;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.scripts.JobScriptsInterface;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class TaskGeo {
	private static Logger log = LogManager.getLogger(TaskGeo.class);
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
			Connection conn=DBConnector.getInstance().getManConnection();
			List<Task> taskList = TaskService.getInstance().queryTaskAll();
			for (Task t:taskList){
				TaskService.getInstance().updateTaskGeo(conn, t.getTaskId());
			}
			DbUtils.commitAndCloseQuietly(conn);
			log.info("end");
		}catch (Exception e) {
			log.error("", e);
		}
		System.exit(0);
	}
}
