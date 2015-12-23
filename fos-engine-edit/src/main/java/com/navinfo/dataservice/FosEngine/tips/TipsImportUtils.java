package com.navinfo.dataservice.FosEngine.tips;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * tips导入辅助工具，如rowkey生成，source生成
 * 
 * @author lilei3774
 * 
 */
public class TipsImportUtils {

	/**
	 * 根据类型、位置、唯一ID组合ROWKEY
	 * 
	 * @param lonlat
	 * @param uniqId
	 * @param type
	 * @return
	 */
	public static String generateRowkey( String uniqId,
			String type) {

		StringBuilder rowkey = new StringBuilder();

		rowkey.append("11");

		rowkey.append(uniqId);

		return rowkey.toString();
	}

	public static String generateSource(String type) {
		JSONObject sourcejson = new JSONObject();

		sourcejson.put("s_featureKind", 2);
		sourcejson.put("s_Project", JSONObject.fromObject(null));
		sourcejson.put("s_sourceCode", 11);
		sourcejson.put("s_sourceId", JSONObject.fromObject(null));
		sourcejson.put("s_sourceType", type);
		sourcejson.put("s_reliability", 100);

		return sourcejson.toString();
	}
	
	public static String generateTrack(String date){
		
		JSONArray trackinfoarray = new JSONArray();

		JSONObject trackinfo = new JSONObject();

		trackinfo.put("stage", 0);
		trackinfo.put("date", date);
		trackinfo.put("handler", 0);
		
		return trackinfoarray.toString();
	}
}
