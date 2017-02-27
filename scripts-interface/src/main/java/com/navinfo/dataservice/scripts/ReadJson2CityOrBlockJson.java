package com.navinfo.dataservice.scripts;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import com.navinfo.dataservice.commons.fileConvert.ReadAndCreateJson;
import com.navinfo.dataservice.commons.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class ReadJson2CityOrBlockJson {	
	
	public static void importJson(String fileName) throws Exception {
		JSONObject retObj = new JSONObject();
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
		retObj = changeData(ja, 1);
		//return retObj;
	}
	
	//将传回来的json 转换成我们所需的 flag = 0 cityJson ;flag = 1 blockJson 
	public static JSONObject changeData(JSONArray ja,int flag) throws Exception {
		JSONObject retObj = new JSONObject();
		
		if(flag == 0){//city
			JSONArray cityList = ReadAndCreateJson.readCityJson2List(ja);
			export2Txt(cityList,"F:/readjson","city2meshgeo.txt");
		}else{
			JSONArray  blockList = ReadAndCreateJson.readBlockJson2List(ja);
			export2Txt(blockList,"F:/readjson","block2gridgeo.txt");
		}
		return null;
	}
	
	public static void export2Txt(JSONArray ja,  String folderName,
			String fileName) throws Exception {
		if (!folderName.endsWith("/")) {
			folderName += "/";
		}

		fileName = folderName + fileName;

		PrintWriter pw = new PrintWriter(fileName);
		try {
//			logger.info("starting load data...");
//			logger.info("data total:"+data.size());
//			logger.info("starting convert data...");
//			JSONArray ja = changeData(data);
//			logger.info("begin write json to file");
			for (int j = 0; j < ja.size(); j++) {
				pw.println(ja.getJSONObject(j).toString());
			}
//			logger.info("file write ok");
		} catch (Exception e) {
			throw e;
		} finally {
			pw.close();

		}
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println("start"); 
		JobScriptsInterface.initContext();//F:\tabfile
		String filePathString="F:\\readjson\\newblock.txt";
		importJson( filePathString);
		System.out.println("end"); 
		System.exit(0);
	}
}
