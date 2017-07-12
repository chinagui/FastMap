package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.RegionMesh;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;

/**
 * @des hadoop库初始化大区库
 * @author fhx
 *
 */
public class InitializeRegionDBPoiFlag {
	private static Logger log = LoggerRepos.getLogger(InitializeRegionDBPoiFlag.class);
	private static QueryRunner run = new QueryRunner();

	public static void main(String[] args) throws Exception{
		//String path = args[0];

		String path = "C:/Users/fhx/Desktop/poi_info.txt";
		JobScriptsInterface.initContext();
					
	    HbasePoiInfo hbaseInfo = new HbasePoiInfo();
	    hbaseInfo.getHBaseDataInfo(path, "not_find_pid.txt");
	}
}
