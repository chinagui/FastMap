package com.navinfo.dataservice.FosEngine.tips;

import net.sf.json.JSONObject;

public class TipsParse {

	public static JSONObject getSourceConstruct() {

		JSONObject jo = JSONObject
				.fromObject("{\"s_featureKind\":2,\"s_Project\":null,\"s_sourceCode\":11,"
						+ "\"s_sourceId\":null,\"s_sourceType\":\"7\",\"s_reliability\":100}");

		return jo;
	}

	public static JSONObject getTrackConstruct() {

		JSONObject jo = JSONObject
				.fromObject("{\"t_lifecycle\":0,\"t_trackInfo\":[]}");

		return jo;
	}

	public static JSONObject getGeometryConstruct() {

		JSONObject jo = JSONObject
				.fromObject("{\"g_location\":null,\"g_guide\":null}");

		return jo;
	}

}
