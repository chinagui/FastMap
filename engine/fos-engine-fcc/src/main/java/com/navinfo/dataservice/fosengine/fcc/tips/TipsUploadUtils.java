package com.navinfo.dataservice.fosengine.fcc.tips;

import net.sf.json.JSONObject;

public class TipsUploadUtils {

	public static JSONObject getSourceConstruct() {

		JSONObject jo = JSONObject
				.fromObject("{\"s_featureKind\":2,\"s_project\":null,\"s_sourceCode\":11,"
						+ "\"s_sourceId\":null,\"s_sourceType\":\"7\",\"s_reliability\":100,\"s_sourceProvider\":0}");

		return jo;
	}

	public static JSONObject getGeometryConstruct() {

		JSONObject jo = JSONObject
				.fromObject("{\"g_location\":null,\"g_guide\":null}");

		return jo;
	}

}
