package com.navinfo.dataservice.engine.fcc.tips;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.engine.fcc.tips.model.TipsIndexModel;
import com.navinfo.navicommons.geo.computation.GridUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;

import net.sf.json.JsonConfig;
import org.apache.commons.lang.StringUtils;

/**
 * @ClassName: TipsUtils.java
 * @author y
 * @date 2016-11-16 上午9:56:56
 * @Description: TODO
 * 
 */


public class TipsUtils {
	

	public static int[] notExpSourceType = { 8001, 8002, 8003, 8004, 8005, 8006, 8007, 8008, 8009, 8010, 1211,1520 }; // 不下载的tips
	
	//关于空值得定义：对象NULL,数据[]，字符串""
	static Object OBJECT_NULL_DEFAULT_VALUE=JSONNull.getInstance();

	static String STRING_NULL_DEFAULT_VALUE="";


	/**
	 * @Description:生成tip索引信息(目前上传用)
	 * @param json（上传txt生成的json）
	 * @param stage
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-12-27 上午10:08:16
	 */
	public static TipsDao generateSolrIndex(JSONObject json, int stage) throws Exception {
		TipsDao tipsIndexModel = new TipsDao();
		tipsIndexModel.setId(json.getString("rowkey"));
		tipsIndexModel.setStage(stage);
        tipsIndexModel.setT_date(json.getString("t_date"));//当前时间
        tipsIndexModel.setT_operateDate(json.getString("t_operateDate"));//t_operateDate原值导入
        tipsIndexModel.setT_lifecycle(json.getInt("t_lifecycle"));
        tipsIndexModel.setT_command(json.getInt("t_command"));
        tipsIndexModel.setHandler(json.getInt("t_handler"));
        tipsIndexModel.setS_sourceType(json.getString("s_sourceType"));

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
	public static TipsDao generateSolrIndex(String rowkey, int stage, String operateDate, int handler,
                                               JSONObject trackJson, JSONObject sourceJson, JSONObject geomJson,
                                               JSONObject deepJson, JSONObject feedbackJson) throws Exception {
		TipsDao tipsIndexModel = new TipsDao();
        tipsIndexModel.setId(rowkey);
        tipsIndexModel.setStage(stage);
        tipsIndexModel.setT_date(trackJson.getString("t_date"));//当前时间
        tipsIndexModel.setT_operateDate(operateDate);//t_operateDate原值导入
        tipsIndexModel.setT_lifecycle(trackJson.getInt("t_lifecycle"));
        tipsIndexModel.setT_command(trackJson.getInt("t_command"));
        tipsIndexModel.setHandler(handler);
        tipsIndexModel.setS_sourceType(sourceJson.getString("s_sourceType"));
        if(StringUtils.isNotEmpty(sourceJson.getString("s_project"))) {
            tipsIndexModel.setS_project(sourceJson.getString("s_project"));
        }
        com.alibaba.fastjson.JSONObject fastGuide = TipsUtils.netJson2fastJson(geomJson.getJSONObject("g_guide"));
        tipsIndexModel.setG_guide(fastGuide.toString());

        com.alibaba.fastjson.JSONObject fastLocation = TipsUtils.netJson2fastJson(geomJson.getJSONObject("g_location"));
        JSONObject g_location = geomJson.getJSONObject("g_location");
        tipsIndexModel.setG_location(fastLocation.toString());

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
        tipsIndexModel.setT_dEditStatus(trackJson.getInt("t_dEditStatus"));
        tipsIndexModel.setT_dEditMeth(trackJson.getInt("t_dEditMeth"));
        tipsIndexModel.setT_mEditStatus(trackJson.getInt("t_mEditStatus"));
        tipsIndexModel.setT_mEditMeth(trackJson.getInt("t_mEditMeth"));

        return tipsIndexModel;
	}

    public static TipsDao generateSolrIndex(String rowkey, String operateDate,
                                                   JSONObject trackJson, JSONObject sourceJson, JSONObject geomJson,
                                                   JSONObject deepJson,JSONObject feedbackJson) throws Exception {

    	TipsDao tipsIndexModel = new TipsDao();
        if(trackJson.containsKey("t_trackInfo")) {
            JSONArray trackInfoArr = trackJson.getJSONArray("t_trackInfo");
            int size = trackInfoArr.size();
            if(size > 0) {
                JSONObject lastTrackInfo = trackInfoArr.getJSONObject(size - 1);
                tipsIndexModel.setStage(lastTrackInfo.getInt("stage"));
                tipsIndexModel.setHandler(lastTrackInfo.getInt("handler"));
            }
        }

        tipsIndexModel.setId(rowkey);
        tipsIndexModel.setT_date(trackJson.getString("t_date"));//当前时间
        tipsIndexModel.setT_operateDate(operateDate);//t_operateDate原值导入
        tipsIndexModel.setT_lifecycle(trackJson.getInt("t_lifecycle"));
        tipsIndexModel.setT_command(trackJson.getInt("t_command"));
        tipsIndexModel.setS_sourceType(sourceJson.getString("s_sourceType"));

        com.alibaba.fastjson.JSONObject fastGuide = TipsUtils.netJson2fastJson(geomJson
                .getJSONObject("g_guide"));
        tipsIndexModel.setG_guide(fastGuide.toString());

        com.alibaba.fastjson.JSONObject fastLocation = TipsUtils.netJson2fastJson(geomJson
                .getJSONObject("g_location"));
        JSONObject g_location = geomJson.getJSONObject("g_location");
        tipsIndexModel.setG_location(fastLocation.toString());

        com.alibaba.fastjson.JSONObject fastDeep = TipsUtils.netJson2fastJson(deepJson);
        tipsIndexModel.setDeep(fastDeep.toString());

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

//    public static JSONObject stringToSFJson(String text) {
//        com.alibaba.fastjson.JSONObject fastJson = com.alibaba.fastjson.JSONObject.parseObject(text);
//        JSONObject jsonObject = JsonUtils.fastJson2netJson(fastJson);
//        return jsonObject;
//    }

    public static JSONObject stringToSFJson(String text) {
        Map<String,Object> fastJson = com.alibaba.fastjson.JSONObject.parseObject(text,new TypeReference<Map<String,Object>>(){});
        JSONObject jsonObject = new JSONObject();
        for(String key : fastJson.keySet()) {
            if(fastJson.get(key) == null) {
                jsonObject.put(key, net.sf.json.JSONNull.getInstance());
            }else {
                jsonObject.put(key, fastJson.get(key));
            }

        }
        return jsonObject;
    }

    public static com.alibaba.fastjson.JSONObject netJson2fastJson(JSONObject json)
    {
        com.alibaba.fastjson.JSONObject obj = new com.alibaba.fastjson.JSONObject();

        for(Object key :json.keySet())
        {


            obj.put((String)key, json.get(key));
        }

        return obj;
    }

    public static JSONObject tipsFromJSONObject(TipsDao tipsDao) {
        JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);
        JSONObject json = JSONObject.fromObject(tipsDao, jsonConfig);
        return json;
    }

    public static String ClobToString(Clob clob) throws SQLException, IOException {

        String reString = "";
        Reader is = clob.getCharacterStream();// 得到流
        BufferedReader br = new BufferedReader(is);
        String s = br.readLine();
        StringBuffer sb = new StringBuffer();
        while (s != null) {// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
            sb.append(s);
            s = br.readLine();
        }
        reString = sb.toString();
        return reString;
    }

    public static void main(String[] args) throws Exception {
        String parameter = "{\"subtaskId\":108,\"grids\":[46597623,47590731,47590700,47590730,47591701,46597711,47591700,46597730,46597633,46597720,50600122,46597603,46597613,47591603,47590603],\"mdFlag\":\"d\",\"workStatus\":5}";
        JSONObject jsonObject = TipsUtils.stringToSFJson(parameter);
        JSONArray girdArray = jsonObject.getJSONArray("grids");
        String wkt = GridUtils.grids2Wkt(girdArray);
        System.out.println(wkt);


    }
}
