package com.navinfo.dataservice.engine.fcc.tips;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;

/**
 * @ClassName: TipsUtils.java
 * @author y
 * @date 2016-11-16 上午9:56:56
 * @Description: TODO
 * 
 */
public class TipsUtils {

	public static int[] notExpSourceType = { 8001, 8002 }; // 不下载的tips

	/**
	 * 组装Track
	 * 
	 * @param lifecycle
	 * @param handler
	 * @param oldTrackInfo
	 * @param t_cStatus
	 * @param t_dStatus
	 * @param t_mStatus
	 * @return
	 */
	public static JSONObject generateTrackJson(int lifecycle,int stage, int handler,
			int command, JSONArray oldTrackInfo, String t_operateDate,
			int t_cStatus, int t_dStatus, int t_mStatus, int t_inStatus,
			int t_inMeth) {

		JSONObject jsonTrack = new JSONObject();

		jsonTrack.put("t_lifecycle", lifecycle);

		jsonTrack.put("t_command", command);

		jsonTrack.put("t_date", t_operateDate);

		jsonTrack.put("t_cStatus", t_cStatus);

		jsonTrack.put("t_dStatus", t_dStatus);

		jsonTrack.put("t_mStatus", t_mStatus);

		jsonTrack.put("t_inStatus", t_inStatus);

		jsonTrack.put("t_inMeth", t_inMeth);

		JSONObject jsonTrackInfo = new JSONObject();

		jsonTrackInfo.put("stage", stage);

		jsonTrackInfo.put("date", t_inMeth);

		jsonTrackInfo.put("handler", handler);

		if (null == oldTrackInfo) {

			oldTrackInfo = new JSONArray();
		}

		oldTrackInfo.add(jsonTrackInfo);

		jsonTrack.put("t_trackInfo", oldTrackInfo);

		return jsonTrack;
	}

	public static JSONObject generateSolrIndex(JSONObject json,
			String currentDate) throws Exception {

		JSONObject index = new JSONObject();

		index.put("id", json.getString("rowkey"));

		index.put("stage", 1);

		index.put("t_date", currentDate);

		index.put("t_operateDate", json.getString("t_operateDate"));

		index.put("t_lifecycle", json.getInt("t_lifecycle"));

		index.put("t_command", json.getInt("t_command"));

		index.put("handler", json.getInt("t_handler"));

		index.put("t_cStatus", json.getInt("t_cStatus"));

		index.put("t_dStatus", json.getInt("t_dStatus"));

		index.put("t_mStatus", json.getInt("t_mStatus"));

		index.put("s_sourceType", json.getString("s_sourceType"));

		index.put("s_sourceCode", json.getInt("s_sourceCode"));

		index.put("g_guide", json.getJSONObject("g_guide"));

		JSONObject g_location = json.getJSONObject("g_location");

		index.put("g_location", g_location);

		JSONObject deep = json.getJSONObject("deep");

		index.put("deep", deep.toString());

		String sourceType = json.getString("s_sourceType");

		JSONArray feedbacks = json.getJSONArray("feedback");

		index.put("feedback", feedbacks.toString());

		index.put("wkt", TipsImportUtils.generateSolrWkt(sourceType, deep,
				g_location, feedbacks));

		index.put("s_reliability", 100);

		return index;
	}

	/**
	 * @Description:新增，根据字段值，新增一个sorl Json
	 * @param rowkey
	 * @param stage
	 * @param operateDate
	 * @param t_lifecycle
	 * @param t_command
	 * @param user
	 * @param t_cStatus
	 * @param t_dStatus
	 * @param t_mStatus
	 * @param sourceType
	 * @param s_sourceCode
	 * @param g_guide
	 * @param g_location
	 * @param deepStr
	 * @param feedBackArr
	 * @return
	 * @author: y
	 * @param currentDate
	 * @param s_reliability
	 * @throws Exception
	 * @time:2016-11-16 上午10:46:38
	 */
	public static JSONObject generateSolrIndex(String rowkey, int stage,
			String operateDate, String currentDate, int t_lifecycle,
			int t_command, int t_handler, int t_cStatus, int t_dStatus,
			int t_mStatus, String sourceType, int s_sourceCode,
			JSONObject g_guide, JSONObject g_location, JSONObject deep,
			JSONArray feedBackArr, int s_reliability) throws Exception {
		JSONObject index = new JSONObject();

		index.put("id", rowkey);

		index.put("stage", stage);

		index.put("t_date", currentDate);

		index.put("t_operateDate", operateDate);

		index.put("t_lifecycle", t_lifecycle);

		index.put("t_command", t_command);

		index.put("handler", t_handler);

		index.put("t_cStatus", t_cStatus);

		index.put("t_dStatus", t_dStatus);

		index.put("t_mStatus", t_mStatus);

		index.put("s_sourceType", sourceType);

		index.put("s_sourceCode", s_sourceCode);
		
		if(g_guide==null){
			
			index.put("g_guide", JSONNull.getInstance());
		}else{
			
			index.put("g_guide", g_guide);
		}

		if(g_location==null){
			
			index.put("g_location", JSONNull.getInstance());
		}else{
			
			index.put("g_location", g_location);
		}


		if (deep != null && !deep.isNullObject()) {
			
			index.put("deep", deep.toString());
		} else {
			
			index.put("deep", JSONNull.getInstance());
		}

		index.put("feedback", feedBackArr.toString());

		index.put("wkt", TipsImportUtils.generateSolrWkt(
				String.valueOf(sourceType), deep, g_location, feedBackArr));

		index.put("s_reliability", s_reliability);

		return index;
	}

	/**
	 * @Description:生成一个tip的rowkey 原则：Tips新增：02+s_sourceType+uuid
	 * @return
	 * @author: y
	 * @param sourceType
	 * @time:2016-11-15 上午11:34:23
	 */
	public static String getNewRowkey(String sourceType) {
		String uuid = UuidUtils.genUuid();
		return "02" + sourceType + uuid;
	}

	/**
	 * @Description:TOOD
	 * @param user
	 * @param memo
	 * @param type
	 * @param operateDate
	 * @return
	 * @author: y
	 * @time:2016-11-16 下午4:54:47
	 */
	public static JSONObject newFeedback(int user, Object memo, int type,
			String operateDate) {
		JSONObject newFeedback = new JSONObject();

		newFeedback.put("user", user);

		newFeedback.put("userRole", JSONNull.getInstance());

		newFeedback.put("type", type);

		newFeedback.put("content", memo);

		newFeedback.put("auditRemark", JSONNull.getInstance());

		newFeedback.put("date", operateDate);
		return newFeedback;
	}

	/**
	 * @Description:根据参数，返回一个source
	 * @param s_featureKind
	 * @param s_project
	 * @param s_sourceCode
	 * @param s_sourceId
	 * @param s_sourceType
	 * @param s_reliability
	 * @param s_sourceProvider
	 * @return
	 * @author: y
	 * @time:2016-11-17 下午8:30:29
	 */
	public static JSONObject newSource(int s_featureKind, String s_project,
			int s_sourceCode, String s_sourceId, String s_sourceType,
			int s_reliability, int s_sourceProvider) {
		JSONObject source = new JSONObject();
		source.put("s_featureKind", s_featureKind);
		source.put("s_project", s_project);
		source.put("s_sourceCode", s_sourceCode);
		source.put("s_sourceId", JSONNull.getInstance());
		source.put("s_sourceType", s_sourceType);
		source.put("s_reliability", s_reliability);
		source.put("s_sourceProvider", s_sourceProvider);
		return source;
	}



	/**
	 * @Description:new a track_info
	 * @param stage
	 * @param date
	 * @param user
	 * @return
	 * @author: y
	 * @time:2016-11-18 下午3:56:26
	 */
	public static JSONObject newTrackInfo(int stage, String date, int user) {

		JSONObject jsonTrackInfo = new JSONObject();

		jsonTrackInfo.put("stage", stage);

		jsonTrackInfo.put("date", date);

		jsonTrackInfo.put("handler", user);

		return jsonTrackInfo;

	}

}
