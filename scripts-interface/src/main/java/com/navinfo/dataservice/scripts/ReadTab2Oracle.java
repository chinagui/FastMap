package com.navinfo.dataservice.scripts;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Scanner;
import org.apache.commons.dbutils.DbUtils;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.fileConvert.ImportOracle;
import com.navinfo.dataservice.commons.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ReadTab2Oracle {
	
	public static JSONArray importJson(String fileName,int type) throws Exception {
		//JSONArray retArr = new JSONArray();
		Scanner importCitys = new Scanner(new FileInputStream(fileName));
		JSONArray ja = new JSONArray();
		while (importCitys.hasNextLine()) {
			try {
				String line = importCitys.nextLine();
				if(line != null && StringUtils.isNotEmpty(line)){
					JSONObject json = JSONObject.fromObject(line);
					ja.add(json);
				}
				
			} catch (Exception e) {
				throw e;
			}
		}
		return ja;
	}
	
//flag = 0 cityJson ;flag = 1 blockJson 
	public static void main(String[] args) throws Exception{
		System.out.println("start"); 
		JobScriptsInterface.initContext();
		//String filePathString="F:\\readjson\\city2meshgeo.txt";
		//int flag = 0;
		//**********************
		String filePathString="F:\\readjson\\block2gridgeo.txt";
		int flag = 1;
//		String filePathString = String.valueOf(args[0]);
		Connection conn=DBConnector.getInstance().getManConnection();
		//读取json  flag = 0 cityJson ;flag = 1 blockJson 
		
		JSONArray ja = importJson(filePathString,flag);

		ImportOracle.writeOracle(conn,flag, ja);
		DbUtils.commitAndCloseQuietly(conn);
		System.out.println("end"); 
		System.exit(0);
	}
}
