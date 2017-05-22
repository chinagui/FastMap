package com.navinfo.dataservice.engine.fcc.tips;

import java.util.Map;

import com.navinfo.dataservice.engine.fcc.tips.model.TipsIndexModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.UuidUtils;

/**
 * @ClassName: TipsUtils.java
 * @author y
 * @date 2016-11-16 上午9:56:56
 * @Description: TODO
 * 
 */


public class TipsUtils {
	

	public static int[] notExpSourceType = { 8001, 8002, 8003, 8004, 8005, 8006, 8007, 8008, 8009, 8010, 1211 }; // 不下载的tips
	
	//关于空值得定义：对象NULL,数据[]，字符串""
	static Object OBJECT_NULL_DEFAULT_VALUE=JSONNull.getInstance();

	static String STRING_NULL_DEFAULT_VALUE="";
	
	
//	/**
//	 * 组装Track(上传、接边、预处理都调用)
//	 *
//	 * @param lifecycle
//	 * @param handler
//	 * @param oldTrackInfo
//	 * @param currentDate
//	 * @param t_cStatus
//	 * @param t_dStatus
//	 * @param t_mStatus
//	 * @param t_fStatus
//	 * @return
//	 */
//	public static JSONObject generateTrackJson(int lifecycle,int stage, int handler,
//			int command, JSONArray oldTrackInfo, String t_operateDate,
//			String currentDate, int t_cStatus, int t_dStatus, int t_mStatus,
//			int t_inMeth, int t_pStatus, int t_dInProc, int t_mInProc, int t_fStatus) {
//
//		JSONObject jsonTrack = new JSONObject();
//
//		jsonTrack.put("t_lifecycle", lifecycle);
//
//		jsonTrack.put("t_command", command);
//
//		jsonTrack.put("t_date", currentDate);//数据入库时服务器时间
//
//		jsonTrack.put("t_cStatus", t_cStatus);
//
//		jsonTrack.put("t_dStatus", t_dStatus);
//
//		jsonTrack.put("t_mStatus", t_mStatus);
//
//		//jsonTrack.put("t_inStatus", t_inStatus);
//
//		jsonTrack.put("t_inMeth", t_inMeth);
//
//		jsonTrack.put("t_pStatus", t_pStatus);
//
//		jsonTrack.put("t_dInProc", t_dInProc);
//
//		jsonTrack.put("t_mInProc", t_mInProc);
//
//		JSONObject jsonTrackInfo = new JSONObject();
//
//		jsonTrackInfo.put("stage", stage);
//
//		jsonTrackInfo.put("date", t_operateDate); //t_operateDate 原值导入
//
//		jsonTrackInfo.put("handler", handler);
//
//		if (null == oldTrackInfo) {
//
//			oldTrackInfo = new JSONArray();
//		}
//
//		oldTrackInfo.add(jsonTrackInfo);
//
//		jsonTrack.put("t_trackInfo", oldTrackInfo);
//
//
//		return jsonTrack;
//	}

	/**
	 * @Description:生成tip索引信息(目前上传用)
	 * @param json（上传txt生成的json）
	 * @param stage
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-12-27 上午10:08:16
	 */
	public static TipsIndexModel generateSolrIndex(JSONObject json, int stage) throws Exception {
		TipsIndexModel tipsIndexModel = new TipsIndexModel();
		tipsIndexModel.setId(json.getString("rowkey"));
		tipsIndexModel.setStage(stage);
        tipsIndexModel.setT_date(json.getString("t_date"));//当前时间
        tipsIndexModel.setT_operateDate(json.getString("t_operateDate"));//t_operateDate原值导入
        tipsIndexModel.setT_lifecycle(json.getInt("t_lifecycle"));
        tipsIndexModel.setT_command(json.getInt("t_command"));
        tipsIndexModel.setHandler(json.getInt("t_handler"));
        tipsIndexModel.setS_sourceType(json.getString("s_sourceType"));
        tipsIndexModel.setS_sourceCode(json.getInt("s_sourceCode"));
        tipsIndexModel.setG_guide(json.getString("g_guide"));

        JSONObject g_location = json.getJSONObject("g_location");
        tipsIndexModel.setG_location(g_location.toString());

        JSONObject deep = json.getJSONObject("deep");
        tipsIndexModel.setDeep(deep.toString());

        JSONObject feedback = json.getJSONObject("feedback");
        tipsIndexModel.setFeedback(feedback.toString());

		String sourceType = json.getString("s_sourceType");

		//这个主要是g_location:目前只用于tips的下载和渲染
        tipsIndexModel.setWktLocation(TipsImportUtils.generateSolrWkt(sourceType, deep,
                g_location, feedback));
		
		//统计坐标，用于其他的：tips的查询、统计等
        tipsIndexModel.setWkt(TipsImportUtils.generateSolrStatisticsWkt(sourceType, deep,
				g_location, feedback));

        tipsIndexModel.setS_reliability(json.getInt("s_reliability"));
        tipsIndexModel.setS_qTaskId(json.getInt("s_qTaskId"));
        tipsIndexModel.setS_mTaskId(json.getInt("s_mTaskId"));
        tipsIndexModel.setS_qSubTaskId(json.getInt("s_qSubTaskId"));
        tipsIndexModel.setS_mSubTaskId(json.getInt("s_mSubTaskId"));

        String tipDiff = "{}";
		if(json.containsKey("tipdiff")){
            tipDiff = json.getString("tipdiff");
		}
        tipsIndexModel.setTipdiff(tipDiff);

		Map<String,String >relateMap = TipsLineRelateQuery.getRelateLine(sourceType, deep);
		tipsIndexModel.setRelate_links(relateMap.get("relate_links"));
        tipsIndexModel.setRelate_nodes(relateMap.get("relate_nodes"));

        tipsIndexModel.setT_tipStatus(json.getInt("t_tipStatus"));
        //Tips上传赋值为0，无需赋值
//        tipsIndexModel.setT_dEditStatus(json.getInt("t_dEditStatus"));
//        tipsIndexModel.setT_dEditMeth(json.getInt("t_dEditMeth"));
//        tipsIndexModel.setT_mEditStatus(json.getInt("t_mEditStatus"));
//        tipsIndexModel.setT_mEditMeth(json.getInt("t_mEditMeth"));

		return tipsIndexModel;
	}


    /**
     * 新增，根据字段值，新增一个sorl Json(街边 预计处理用)
     * @param rowkey
     * @param stage
     * @param operateDate
     * @param handler
     * @param trackJson
     * @param sourceJson
     * @param geomJson
     * @param deepJson
     * @param feedbackJson
     * @return
     * @throws Exception
     */
	public static TipsIndexModel generateSolrIndex(String rowkey, int stage, String operateDate, int handler,
                                               JSONObject trackJson, JSONObject sourceJson, JSONObject geomJson,
                                               JSONObject deepJson, JSONObject feedbackJson) throws Exception {
		TipsIndexModel tipsIndexModel = new TipsIndexModel();
        tipsIndexModel.setId(rowkey);
        tipsIndexModel.setStage(stage);
        tipsIndexModel.setT_date(trackJson.getString("t_date"));//当前时间
        tipsIndexModel.setT_operateDate(operateDate);//t_operateDate原值导入
        tipsIndexModel.setT_lifecycle(trackJson.getInt("t_lifecycle"));
        tipsIndexModel.setT_command(trackJson.getInt("t_command"));
        tipsIndexModel.setHandler(handler);
        tipsIndexModel.setS_sourceType(sourceJson.getString("s_sourceType"));
        tipsIndexModel.setS_sourceCode(sourceJson.getInt("s_sourceCode"));
        tipsIndexModel.setG_guide(geomJson.getString("g_guide"));

        JSONObject g_location = geomJson.getJSONObject("g_location");
        tipsIndexModel.setG_location(g_location.toString());

        tipsIndexModel.setDeep(deepJson.toString());

        tipsIndexModel.setFeedback(feedbackJson.toString());

        String sourceType = sourceJson.getString("s_sourceType");

        //这个主要是g_location:目前只用于tips的下载和渲染
        tipsIndexModel.setWktLocation(TipsImportUtils.generateSolrWkt(sourceType, deepJson,
                g_location, feedbackJson));

        //统计坐标，用于其他的：tips的查询、统计等
        tipsIndexModel.setWkt(TipsImportUtils.generateSolrStatisticsWkt(sourceType, deepJson,
                g_location, feedbackJson));

        tipsIndexModel.setS_reliability(sourceJson.getInt("s_reliability"));
        tipsIndexModel.setS_qTaskId(sourceJson.getInt("s_qTaskId"));
        tipsIndexModel.setS_mTaskId(sourceJson.getInt("s_mTaskId"));
        tipsIndexModel.setS_qSubTaskId(sourceJson.getInt("s_qSubTaskId"));
        tipsIndexModel.setS_mSubTaskId(sourceJson.getInt("s_mSubTaskId"));

        String tipDiff = "{}";
        tipsIndexModel.setTipdiff(tipDiff);

        Map<String,String >relateMap = TipsLineRelateQuery.getRelateLine(sourceType, deepJson);
        tipsIndexModel.setRelate_links(relateMap.get("relate_links"));
        tipsIndexModel.setRelate_nodes(relateMap.get("relate_nodes"));

        tipsIndexModel.setT_tipStatus(trackJson.getInt("t_tipStatus"));
        //Tips上传赋值为0，无需赋值
//        tipsIndexModel.setT_dEditStatus(json.getInt("t_dEditStatus"));
//        tipsIndexModel.setT_dEditMeth(json.getInt("t_dEditMeth"));
//        tipsIndexModel.setT_mEditStatus(json.getInt("t_mEditStatus"));
//        tipsIndexModel.setT_mEditMeth(json.getInt("t_mEditMeth"));

        return tipsIndexModel;
	}

    public static TipsIndexModel generateSolrIndex(String rowkey, String operateDate,
                                                   JSONObject trackJson, JSONObject sourceJson, JSONObject geomJson,
                                                   JSONObject deepJson, JSONObject feedbackJson) throws Exception {
        JSONArray trackInfoArr = trackJson.getJSONArray("t_trackInfo");
        int size = trackInfoArr.size();
        JSONObject lastTrackInfo = trackInfoArr.getJSONObject(size - 1);
        TipsIndexModel tipsIndexModel = new TipsIndexModel();
        tipsIndexModel.setId(rowkey);
        tipsIndexModel.setStage(lastTrackInfo.getInt("stage"));
        tipsIndexModel.setT_date(trackJson.getString("t_date"));//当前时间
        tipsIndexModel.setT_operateDate(operateDate);//t_operateDate原值导入
        tipsIndexModel.setT_lifecycle(trackJson.getInt("t_lifecycle"));
        tipsIndexModel.setT_command(trackJson.getInt("t_command"));
        tipsIndexModel.setHandler(lastTrackInfo.getInt("handler"));
        tipsIndexModel.setS_sourceType(sourceJson.getString("s_sourceType"));
        tipsIndexModel.setS_sourceCode(sourceJson.getInt("s_sourceCode"));
        tipsIndexModel.setG_guide(geomJson.getString("g_guide"));

        JSONObject g_location = geomJson.getJSONObject("g_location");
        tipsIndexModel.setG_location(g_location.toString());

        tipsIndexModel.setDeep(deepJson.toString());

        tipsIndexModel.setFeedback(feedbackJson.toString());

        String sourceType = sourceJson.getString("s_sourceType");

        //这个主要是g_location:目前只用于tips的下载和渲染
        tipsIndexModel.setWktLocation(TipsImportUtils.generateSolrWkt(sourceType, deepJson,
                g_location, feedbackJson));

        //统计坐标，用于其他的：tips的查询、统计等
        tipsIndexModel.setWkt(TipsImportUtils.generateSolrStatisticsWkt(sourceType, deepJson,
                g_location, feedbackJson));

        tipsIndexModel.setS_reliability(sourceJson.getInt("s_reliability"));
        tipsIndexModel.setS_qTaskId(sourceJson.getInt("s_qTaskId"));
        tipsIndexModel.setS_mTaskId(sourceJson.getInt("s_mTaskId"));
        tipsIndexModel.setS_qSubTaskId(sourceJson.getInt("s_qSubTaskId"));
        tipsIndexModel.setS_mSubTaskId(sourceJson.getInt("s_mSubTaskId"));

        String tipDiff = "{}";
        tipsIndexModel.setTipdiff(tipDiff);

        Map<String,String >relateMap = TipsLineRelateQuery.getRelateLine(sourceType, deepJson);
        tipsIndexModel.setRelate_links(relateMap.get("relate_links"));
        tipsIndexModel.setRelate_nodes(relateMap.get("relate_nodes"));

        tipsIndexModel.setT_tipStatus(trackJson.getInt("t_tipStatus"));
        //Tips上传赋值为0，无需赋值
//        tipsIndexModel.setT_dEditStatus(json.getInt("t_dEditStatus"));
//        tipsIndexModel.setT_dEditMeth(json.getInt("t_dEditMeth"));
//        tipsIndexModel.setT_mEditStatus(json.getInt("t_mEditStatus"));
//        tipsIndexModel.setT_mEditMeth(json.getInt("t_mEditMeth"));

        return tipsIndexModel;
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
	 * @Description:根据备注信息生成一个feedback的一条记录（接边预处理用）
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

		newFeedback.put("userRole", TipsUtils.OBJECT_NULL_DEFAULT_VALUE);

		newFeedback.put("type", type);

		newFeedback.put("content", memo);

		newFeedback.put("auditRemark", TipsUtils.STRING_NULL_DEFAULT_VALUE);

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
	 * @param s_mSubTaskId 
	 * @param s_mTaskId 
	 * @param s_qSubTaskId 
	 * @param s_qTaskId 
	 * @time:2016-11-17 下午8:30:29
	 */
	public static JSONObject newSource(int s_featureKind, String s_project,
			int s_sourceCode, String s_sourceId, String s_sourceType,
			int s_reliability, int s_sourceProvider, int s_qTaskId, int s_qSubTaskId, int s_mTaskId, int s_mSubTaskId) {
		JSONObject source = new JSONObject();
		source.put("s_featureKind", s_featureKind);
		source.put("s_project", s_project);
		source.put("s_sourceCode", s_sourceCode);
		source.put("s_sourceId", TipsUtils.STRING_NULL_DEFAULT_VALUE);
		source.put("s_sourceType", s_sourceType);
		source.put("s_reliability", s_reliability);
		source.put("s_sourceProvider", s_sourceProvider);
		source.put("s_qTaskId", s_qTaskId);
		source.put("s_qSubTaskId", s_qSubTaskId);
		source.put("s_mTaskId", s_mTaskId);
		source.put("s_mSubTaskId", s_mSubTaskId);
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

//	/**
//	 * @Description:通过tips的json生成Solr索引
//	 * @param jsonInfo：和规格完全一直的json数据
//	 * @param currentDate
//	 * @return
//	 * @author: y
//	 * @param user
//	 * @throws Exception
//	 * @time:2017-3-13 下午5:03:43
//	 */
//	public static JSONObject generateSolrIndexFromTipsJson(JSONObject jsonInfo,
//			String currentDate) throws Exception {
//		JSONObject index = new JSONObject();
//		JSONObject track=jsonInfo.getJSONObject("track");
//		JSONArray trackInfoArr=track.getJSONArray("t_trackInfo");
//		int size=trackInfoArr.size();
//		JSONObject lastTrackInfo=trackInfoArr.getJSONObject(size-1);
//
//		String sourceType=jsonInfo.getJSONObject("source").getString("s_sourceType");
//		JSONObject g_location=jsonInfo.getJSONObject("geometry").getJSONObject("g_location");
//		JSONObject deep=jsonInfo.getJSONObject("deep");
//		JSONObject feedback=null;
//	    if(jsonInfo.containsKey("feedback")){
//	    	feedback=jsonInfo.getJSONObject("feedback");
//	    }
//
//		index.put("id", jsonInfo.getString("rowkey"));
//		index.put("stage", lastTrackInfo.getInt("stage"));
//		index.put("t_date", currentDate);
//		index.put("t_operateDate", currentDate);
//		index.put("t_lifecycle", track.getInt("t_lifecycle"));
//		index.put("t_command", track.getInt("t_command"));
//		index.put("handler",lastTrackInfo.getInt("handler"));
//		index.put("s_sourceType",sourceType);
//		index.put("s_sourceCode",jsonInfo.getJSONObject("source").getInt("s_sourceCode"));
//		index.put("g_location",g_location);
//		index.put("g_guide",jsonInfo.getJSONObject("geometry").getJSONObject("g_guide").toString());
//
//		//这个主要是g_location:目前只用于tips的下载和渲染
//		index.put("wktLocation", TipsImportUtils.generateSolrWkt(sourceType, deep,
//				g_location, feedback));
//
//		//统计坐标，用于其他的：tips的查询、统计等
//		index.put("wkt", TipsImportUtils.generateSolrStatisticsWkt(sourceType, deep,
//				g_location, feedback));
//
//	   index.put("deep",jsonInfo.getJSONObject("deep").toString());
//
//	   if(feedback!=null){
//		   index.put("feedback",feedback);
//	   }else{
//		   JSONArray  infoArr=new JSONArray();
//		   feedback=new JSONObject();
//		   feedback.put("f_array", infoArr);
//		   index.put("feedback",feedback);
//	   }
//
//	   index.put("s_reliability",jsonInfo.getJSONObject("source").getInt("s_reliability"));
//	   index.put("t_cStatus", track.getInt("t_cStatus"));
//	   index.put("t_dStatus", track.getInt("t_dStatus"));
//	   index.put("t_mStatus", track.getInt("t_mStatus"));
//	   index.put("t_inMeth", track.getInt("t_inMeth"));
//	   index.put("t_pStatus", track.getInt("t_pStatus"));
//	   index.put("t_dInProc", track.getInt("t_dInProc"));
//	   index.put("t_mInProc", track.getInt("t_mInProc"));
//	   index.put("s_qTaskId", jsonInfo.getJSONObject("source").getInt("s_qTaskId"));
//	   index.put("s_mTaskId", jsonInfo.getJSONObject("source").getInt("s_mTaskId"));
//	   index.put("t_fStatus", track.getInt("t_fStatus"));
//
//	   if(jsonInfo.containsKey("tipdiff")){
//		   index.put("tipdiff", jsonInfo.getJSONObject("tipdiff").toString());
//	   }else{
//		   index.put("tipdiff", "{}");
//	   }
//
//	   index.put("s_qSubTaskId", jsonInfo.getJSONObject("source").getInt("s_qSubTaskId"));
//	   index.put("s_mSubTaskId", jsonInfo.getJSONObject("source").getInt("s_mSubTaskId"));
//
//		Map<String,String >relateMap=TipsLineRelateQuery.getRelateLine(sourceType, deep);
//
//		index.put("relate_links", relateMap.get("relate_links"));
//
//		index.put("relate_nodes", relateMap.get("relate_nodes"));
//
//
//	   return index;
//	}

}
