package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.util.ArrayList;
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
//		String filePathString="F:\\Fm_Projects_Doc\\道路第八迭代\\blocks\\data\\resources\\grid\\10725.tab";
		String filePathString = String.valueOf(args[0]);
		Connection conn=DBConnector.getInstance().getManConnection();
		List<String> columnNameList=new ArrayList<String>();
		columnNameList.add("NAME");
		List<Map<String, Object>> dataList = LoadTab.readTab(filePathString, columnNameList);
		
		for(int i=0;i<dataList.size();i++){
			Map<String, Object> tmp=dataList.get(i);
			tmp.put("ID", i+1);
		}
		ImportOracle.writeOracle(conn, "SUBTASK_REFER", dataList);
		DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end"); 
		System.exit(0);
	}
}
