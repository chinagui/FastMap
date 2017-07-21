package com.navinfo.dataservice.scripts;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;


/**
 * @des hadoop库初始化大区库
 * @author fhx
 *
 */
public class InitializeRegionDBPoiFlag {
	private static Logger log = LoggerRepos.getLogger(InitializeRegionDBPoiFlag.class);
	private static QueryRunner run = new QueryRunner();

	public static void main(String[] args) throws Exception{
		try {
			String path = args[0];

			//String path = "C:/Users/fhx/Desktop/poi_info.txt";
			JobScriptsInterface.initContext();

			HbasePoiInfo hbaseInfo = new HbasePoiInfo();
			hbaseInfo.getHBaseDataInfo(path, "not_find_pid.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
