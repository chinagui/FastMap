package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.fileConvert.ImportOracle;
import com.navinfo.dataservice.commons.fileConvert.LoadTab;

public class loadTab2OracleTest {
	
	public static void main(String[] args) throws Exception{
		System.out.println("start"); 
		JobScriptsInterface.initContext();
		//String filePathString="D:/temp/block_tab2json/bj.TAB";
		String filePathString = String.valueOf(args[0]);
		Connection conn=DBConnector.getInstance().getManConnection();
		List<Map<String, Object>> dataList = LoadTab.readTabReturnAllData(filePathString);
		ImportOracle.writeOracle(conn, "test1125", dataList);
		DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end"); 
		System.exit(0);
	}
}
