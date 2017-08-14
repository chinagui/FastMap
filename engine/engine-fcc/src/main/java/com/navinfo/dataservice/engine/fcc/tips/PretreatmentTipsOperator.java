package com.navinfo.dataservice.engine.fcc.tips;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.TaskType;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.dataservice.engine.fcc.tips.check.TipsPreCheckUtils;
import com.navinfo.dataservice.engine.fcc.tips.model.TipsSource;
import com.navinfo.dataservice.engine.fcc.tips.model.TipsTrack;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.util.*;

/**
 * 预处理tips操作类
 * 
 * @ClassName: PretreatmentTipsOperator.java
 * @author y
 * @date 2016-11-17 下午8:13:38
 * @Description: TODO
 * 
 */
public class PretreatmentTipsOperator extends BaseTipsOperate {

	static String FC_SOURCE_TYPE = "8001"; // FC预处理理tips
//	static int FC_DEFAULT_STAGE = 2;
	public static int COMMAND_INSERT = 0;
	public static int COMMAND_UPADATE = 1;
    public static int PRE_TIPS_STAGE = 5;
    public static int INFO_TIPS_STAGE = 6;
    public static int TIP_FC_SOURCECODE = 14;
	
	private static final Logger logger = Logger
			.getLogger(PretreatmentTipsOperator.class);

	public PretreatmentTipsOperator() {

	}

	/**
	 * @Description:FC预处理创建一个tips
	 * @param sourceType
	 * @param user
	 *            :feedback.content
	 * @param user
	 * @author: y
	 * @param deep
	 * @param memo
	 * @throws Exception
	 * @time:2016-11-15 上午11:03:20
	 */
	public String create(String sourceType, JSONObject lineGeometry, int user,
			JSONObject deep, String memo, int qSubTaskId) throws Exception {

		Connection hbaseConn = null;
		java.sql.Connection oracleConn = null;
		Table htab =null;
		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();
			htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			// 1.rowkey
			String rowkey = TipsUtils.getNewRowkey(sourceType);

			// 2.feedback
			String currentDate = DateUtils.dateToString(new Date(),
					DateUtils.DATE_COMPACTED_FORMAT);

			JSONObject feedbackObj = new JSONObject();
			JSONArray f_array = new JSONArray();
			// memo,如果有，则增加一个备注
			if (StringUtils.isNotEmpty(memo)) {
				int type = 3;
				JSONObject newFeedback2 = TipsUtils.newFeedback(user, memo,
						type, currentDate);
				f_array.add(newFeedback2);
			}
			feedbackObj.put("f_array", f_array);

			int t_lifecycle = PretreatmentTipsOperator.TIP_LIFECYCLE_ADD;

            // 3.track
            TipsTrack track = new TipsTrack();
            track = this.tipSaveUpdateTrack(track, t_lifecycle);
            JSONObject trackJson = JSONObject.fromObject(track);

            // source
            int s_sourceCode = PretreatmentTipsOperator.TIP_FC_SOURCECODE;
            TipsSource source = new TipsSource();
            source.setS_sourceCode(s_sourceCode);
            source.setS_project(String.valueOf(qSubTaskId));//快线子任务ID
            source.setS_sourceType(sourceType);
            JSONObject sourceJson = JSONObject.fromObject(source);

			// deep; 生成deep信息
			JSONObject deepNew = new JSONObject();
			// 根据tips类型生成deep信息
			if (deep != null && !deep.isNullObject()) {
				// fc预处理tips
				if (FC_SOURCE_TYPE.equals(sourceType)) {
					deepNew = newFcDeep(lineGeometry, deep);
				}
			}

			// 4.geometry
			JSONObject jsonGeom = new JSONObject();
			JSONObject g_guide = deepNew.getJSONObject("geo");
			jsonGeom.put("g_location", lineGeometry);
			jsonGeom.put("g_guide", g_guide);

			// put
			Put put = new Put(rowkey.getBytes());
			put.addColumn("data".getBytes(), "track".getBytes(), trackJson
					.toString().getBytes());
            com.alibaba.fastjson.JSONObject fastGeom = TipsUtils.netJson2fastJson(jsonGeom);
			put.addColumn("data".getBytes(), "geometry".getBytes(), fastGeom
					.toString().getBytes());
			put.addColumn("data".getBytes(), "feedback".getBytes(), feedbackObj
					.toString().getBytes());
			put.addColumn("data".getBytes(), "source".getBytes(), sourceJson
					.toString().getBytes());
            com.alibaba.fastjson.JSONObject fastDeep = TipsUtils.netJson2fastJson(deepNew);
			put.addColumn("data".getBytes(), "deep".getBytes(), fastDeep
					.toString().getBytes());

			// solr index json
            TipsDao tipsIndexModel = TipsUtils.generateSolrIndex(rowkey, PretreatmentTipsOperator.PRE_TIPS_STAGE,
                    currentDate, user, trackJson, sourceJson, jsonGeom, deepNew, feedbackObj);
            oracleConn = DBConnector.getInstance().getTipsIdxConnection();
            new TipsIndexOracleOperator(oracleConn).save(tipsIndexModel);

			List<Put> puts = new ArrayList<Put>();
			puts.add(put);
			htab.put(puts);
            return rowkey;
		} catch (IOException e) {
			DbUtils.rollbackAndCloseQuietly(oracleConn);
			logger.error("新增tips出错：原因：" + e.getMessage());
			throw new Exception("新增tips出错：原因：" + e.getMessage(), e);
		}finally{
			DbUtils.commitAndCloseQuietly(oracleConn);
			if(htab != null) {
				htab.close();
			}
		}

	}

	/**
	 * @Description:生成一个fc预处理tips.deep
	 * @param lineGeometry
	 * @param deep
	 * @throws Exception
	 * @author: y
	 * @time:2016-11-18 上午9:23:38
	 */
	private JSONObject newFcDeep(JSONObject lineGeometry, JSONObject deep)
			throws Exception {

		JSONObject deepNew = new JSONObject();
		JSONObject pointGeo;
		int fc;
		// 几何中心点
		pointGeo = getMidPointByGeometry(lineGeometry);

		fc = deep.getInt("fc");

		deepNew.put("geo", pointGeo);

		deepNew.put("fc", fc);

		return deepNew;
	}

	/**
	 * @Description:获得坐标的几何中心点（线）
	 * @param lineGeometry
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-11-18 下午4:18:43
	 */
	private JSONObject getMidPointByGeometry(JSONObject lineGeometry)
			throws Exception {
		JSONObject pointGeo;
		Geometry midGeo = GeometryUtils.getMidPointByLine(GeoTranslator
				.geojson2Jts(lineGeometry));
		pointGeo = GeoTranslator.jts2Geojson(midGeo);
		return pointGeo;
	}
	
	
	/**
	 * @Description:获得坐标的几何中心点（线）或
	 * 1.如果只有两个形状点，则取几何中心点，否则取第二个形状点
	 * @param lineGeometry
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-11-18 下午4:18:43
	 */
	private JSONObject getMidPointByGeometry2(JSONObject lineGeometry)
			throws Exception {
		JSONObject pointGeo;
		
		Geometry geo= GeoTranslator
		.geojson2Jts(lineGeometry);
		
		Coordinate[] cs = geo.getCoordinates();
		
		if(cs.length==2){
			
			Geometry midGeo = GeometryUtils.getMidPointByLine(GeoTranslator
					.geojson2Jts(lineGeometry));
			pointGeo = GeoTranslator.jts2Geojson(midGeo);
		}else{
			
				double x = cs[1].x; //取第二个形状点
				double y = cs[1].y;

			Geometry secondPoint = GeoTranslator.point2Jts(x, y);
			
			pointGeo = GeoTranslator.jts2Geojson(secondPoint);
			
		}
	
		return pointGeo;
	}

	/**
	 * @Description:修改tips的几何
	 * @param rowkey
	 * @param lineGeometry
	 * @param user
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-18 下午2:16:09
	 */
	public boolean editGeo(String rowkey, JSONObject lineGeometry, int user)
			throws Exception {

		Connection hbaseConn = null;
		java.sql.Connection oracleConn = null;
		Table htab = null;
		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();

			htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			String[] queryColNames = { "track", "deep", "geometry" };

			Result result = getResultByRowKey(htab, rowkey, queryColNames);

			if (result.isEmpty()) {
				return false;
			}

			Put put = new Put(rowkey.getBytes());

			// 1.update track
            String trackJson = new String(result
                    .getValue("data".getBytes(), "track".getBytes()));
            TipsTrack track = com.alibaba.fastjson.JSONObject.parseObject(trackJson, TipsTrack.class);
            track = this.tipSaveUpdateTrack(track, PretreatmentTipsOperator.TIP_LIFECYCLE_ADD);

			// 2.update geometry
			JSONObject geometry = JSONObject.fromObject(new String(result
					.getValue("data".getBytes(), "geometry".getBytes())));
			geometry.put("g_location", lineGeometry);
			JSONObject guideNew = getMidPointByGeometry(lineGeometry);
			geometry.put("g_guide", guideNew);

			// 2.update deep.geo(根据新的几何信息计算几何中心点)
			JSONObject deep = JSONObject.fromObject(new String(result.getValue(
					"data".getBytes(), "deep".getBytes())));
			deep.put("geo", guideNew);

            //source 快线子任务
//            JSONObject source = JSONObject.fromObject(new String(result.getValue(
//                    "data".getBytes(), "source".getBytes())));
////            source.put("s_qSubTaskId", qSubTaskId);

			put.addColumn("data".getBytes(), "track".getBytes(), JSONObject.fromObject(track)
					.toString().getBytes());

			put.addColumn("data".getBytes(), "geometry".getBytes(), geometry
					.toString().getBytes());

			put.addColumn("data".getBytes(), "deep".getBytes(), deep.toString()
					.getBytes());

			// update solr
			oracleConn = DBConnector.getInstance().getTipsIdxConnection();
		   
			TipsIndexOracleOperator tipsIndexOracleConn = new TipsIndexOracleOperator(oracleConn);
			
			TipsDao solrIndex = tipsIndexOracleConn.getById(rowkey);

//			solrIndex.setT_lifecycle(PretreatmentTipsOperator.TIP_LIFECYCLE_UPDATE);
			solrIndex.setT_date(track.getT_date());
			solrIndex.setG_location(lineGeometry.toString());
			solrIndex.setG_guide(guideNew.toString());
			solrIndex.setDeep(deep.toString());
			JSONObject feedbackObj = JSONObject.fromObject(solrIndex.getFeedback());
			solrIndex.setWktLocation(TipsImportUtils.generateSolrWkt(
                    String.valueOf(FC_SOURCE_TYPE), deep, lineGeometry,
                    feedbackObj));

			solrIndex.setWkt(TipsImportUtils.generateSolrStatisticsWkt(
                    String.valueOf(FC_SOURCE_TYPE), deep, lineGeometry,
                    feedbackObj));

            Map<String,String> relateMap = TipsLineRelateQuery.getRelateLine(String.valueOf(FC_SOURCE_TYPE), deep);
            solrIndex.setRelate_links(relateMap.get("relate_links"));
            solrIndex.setRelate_nodes(relateMap.get("relate_nodes"));

            
            List<TipsDao> solrIndexList = new ArrayList<TipsDao>();
            solrIndexList.add(solrIndex);
            tipsIndexOracleConn.update(solrIndexList);
            
			htab.put(put);
			
			return false;
		} catch (IOException e) {
			logger.error("tips修形出错,rowkey:" + rowkey + "原因：" + e.getMessage());
			DbUtils.rollbackAndCloseQuietly(oracleConn);
			throw new Exception("tips修形出错,rowkey:" + rowkey + "原因："
					+ e.getMessage(), e);
		}finally {
			DbUtils.commitAndCloseQuietly(oracleConn);
			if(htab!=null) {
				htab.close();
			}
		}
	}

	/**
	 * @Description:修改tips的几何
	 * @param rowkey
	 * @param tipGeometry
	 * @param user
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-18 下午2:16:09
	 */
	public boolean move(String rowkey, JSONObject tipGeometry, int user)
			throws Exception {

		Connection hbaseConn;
		java.sql.Connection oracleConn =null;
		try {

			hbaseConn = HBaseConnector.getInstance().getConnection();

			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			String[] queryColNames = { "track", "deep" };

			Result result = getResultByRowKey(htab, rowkey, queryColNames);

			if (result.isEmpty()) {
				return false;
			}

			Put put = new Put(rowkey.getBytes());

			// 1.update track

			JSONObject track = JSONObject.fromObject(new String(result
					.getValue("data".getBytes(), "track".getBytes())));

			String date = DateUtils.dateToString(new Date(),
					DateUtils.DATE_COMPACTED_FORMAT);
            track.put("t_date", date);
//			track = addTrackInfo(user, track, date);

			// 2.update deep.geo(用户挪动后的点)

			JSONObject deep = JSONObject.fromObject(new String(result.getValue(
					"data".getBytes(), "deep".getBytes())));

			deep.put("geo", tipGeometry);

			put.addColumn("data".getBytes(), "track".getBytes(), track
					.toString().getBytes());

			put.addColumn("data".getBytes(), "deep".getBytes(), deep.toString()
					.getBytes());

			// update solr

			oracleConn = DBConnector.getInstance().getTipsIdxConnection();
			   
			TipsIndexOracleOperator tipOperator = new TipsIndexOracleOperator(oracleConn);
			
			TipsDao solrIndex = tipOperator.getById(rowkey);

			solrIndex.setT_date(date);

			solrIndex.setHandler( user);

			solrIndex.setDeep(deep.toString());

			tipOperator.updateOne(solrIndex);

			htab.put(put);

			htab.close();

			return false;

		} catch (IOException e) {

			logger.error("tips点移动出错,rowkey:" + rowkey + "原因：" + e.getMessage());
			DbUtils.rollbackAndCloseQuietly(oracleConn);
			throw new Exception("tips点移动出错,rowkey:" + rowkey + "原因："
					+ e.getMessage(), e);
		}finally{
			DbUtils.commitAndCloseQuietly(oracleConn);
		}
	}

	/**
	 * @Description:FC预处理打断（线几何）
	 * @param rowkey
	 * @param tipGeometry
	 * @param user
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-18 下午2:16:09
	 */
	public JSONArray breakLine(String rowkey, JSONObject tipGeometry, int user)
			throws Exception {
		Connection hbaseConn = null;
		JSONArray rowkeyArray = new JSONArray();
        Table htab = null;
        java.sql.Connection tipsConn=null;
        try {

        	tipsConn=DBConnector.getInstance().getTipsIdxConnection();
        	TipsIndexOracleOperator operator=new TipsIndexOracleOperator(tipsConn);
        	TipsDao solrIndex = operator.getById(rowkey);
			//JSONObject solrIndex = solr.getById(rowkey);
			String s_sourceType = solrIndex.getS_sourceType();
			hbaseConn = HBaseConnector.getInstance().getConnection();
			htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			Result result = getResultByRowKey(htab, rowkey, null);
			if (result.isEmpty()) {
				return rowkeyArray;
			}

			// 0.copy一个新的tips,rowkey重新申请
			String newRowkey = TipsUtils.getNewRowkey(s_sourceType);
			Put newPut = copyNewATips(result, newRowkey);
			Put put = new Put(rowkey.getBytes());

            rowkeyArray.add(newRowkey);
            rowkeyArray.add(rowkey);
			TipsDao newSolrIndex = solrIndex.copy();
			newSolrIndex.setId(newRowkey);

			// 1.cut line
			Point point = (Point) GeoTranslator.geojson2Jts(tipGeometry);
			JSONObject oldGeo = JSONObject.fromObject(solrIndex.getG_location());
			List<JSONObject> cutGeoResult = cutLineByPoint(point, oldGeo);
			JSONObject geo1 = new JSONObject();
			JSONObject geo2 = new JSONObject();
            JSONObject g_location1 = cutGeoResult.get(0);
			JSONObject g_location2 = cutGeoResult.get(1);
			JSONObject g_guide1 = getMidPointByGeometry(g_location1);
			JSONObject g_guide2 = getMidPointByGeometry(g_location2);

			geo1.put("g_location", g_location1);
			geo1.put("g_guide", g_guide1);
			geo2.put("g_location", g_location2);
			geo2.put("g_guide", g_guide2);
			solrIndex.setG_location(g_location1.toString());
			newSolrIndex.setG_location( g_location2.toString());
			solrIndex.setG_guide(g_guide1.toString());
			newSolrIndex.setG_guide(g_guide2.toString());

			// 旧的feedback两个都是一样的，取一个就好了
			JSONObject feedbackObj = JSONObject.fromObject(solrIndex
					.getFeedback());

			put.addColumn("data".getBytes(), "geometry".getBytes(), geo1
					.toString().getBytes());
			newPut.addColumn("data".getBytes(), "geometry".getBytes(), geo2
					.toString().getBytes());

			// update deep (重新计算point)
			// 如果是FC预处理的tips需求更新deep.geo
			if (FC_SOURCE_TYPE.equals(solrIndex.getS_sourceType())) {
				updateFcTipDeep(solrIndex, newPut, put, newSolrIndex, g_guide1,
						g_guide2);
			}

            solrIndex.setWkt(TipsImportUtils.generateSolrStatisticsWkt(
                    String.valueOf(FC_SOURCE_TYPE), JSONObject.fromObject(solrIndex.getDeep()), g_location1,
                    feedbackObj));

            //这个主要是g_location:目前只用于tips的下载和渲染
            solrIndex.setWktLocation(TipsImportUtils.generateSolrWkt(
                    String.valueOf(FC_SOURCE_TYPE), JSONObject.fromObject(solrIndex.getDeep()), g_location1,
                    feedbackObj));

            newSolrIndex.setWkt(TipsImportUtils.generateSolrStatisticsWkt(
                    String.valueOf(FC_SOURCE_TYPE), JSONObject.fromObject(newSolrIndex.getDeep()), g_location2,
                    feedbackObj));

            //这个主要是g_location:目前只用于tips的下载和渲染
            newSolrIndex.setWktLocation( TipsImportUtils.generateSolrWkt(
                    String.valueOf(FC_SOURCE_TYPE),  JSONObject.fromObject(newSolrIndex.getDeep()), g_location2,
                    feedbackObj));

			// update track
            String trackJson = new String(result
                    .getValue("data".getBytes(), "track".getBytes()));
            TipsTrack track = com.alibaba.fastjson.JSONObject.parseObject(trackJson, TipsTrack.class);
            track = this.tipSaveUpdateTrack(track, PretreatmentTipsOperator.TIP_LIFECYCLE_ADD);
			JSONObject newTrack = JSONObject.fromObject(track);
			put.addColumn("data".getBytes(), "track".getBytes(), newTrack
					.toString().getBytes());
			newPut.addColumn("data".getBytes(), "track".getBytes(), newTrack
					.toString().getBytes());

            // update source
            JSONObject source = JSONObject.fromObject(new String(result
                    .getValue("data".getBytes(), "source".getBytes())));
//            source.put("s_qSubTaskId", qSubTaskId);
            JSONObject newSource = JSONObject.fromObject(source);
            put.addColumn("data".getBytes(), "source".getBytes(), source
                    .toString().getBytes());
            newPut.addColumn("data".getBytes(), "source".getBytes(), newSource
                    .toString().getBytes());

			// update solr
            solrIndex = this.tipSaveUpdateTrackSolr(track, solrIndex);
            newSolrIndex = this.tipSaveUpdateTrackSolr(track, newSolrIndex);
            operator.updateOne(solrIndex);
			operator.updateOne(newSolrIndex);

			htab.put(put);
			htab.put(newPut);
			return rowkeyArray;
		} catch (Exception e) {
			e.printStackTrace();
			DbUtils.rollbackAndCloseQuietly(tipsConn);
			logger.error("打断出错,rowkey:" + rowkey + "原因：" + e.getMessage());
			throw new Exception("打断出错,rowkey:" + rowkey + "原因："
					+ e.getMessage(), e);
		}finally {
			DbUtils.commitAndCloseQuietly(tipsConn);
            if(htab != null) {
                htab.close();
            }
        }
    }

	/**
	 * @Description:TOOD
	 * @param solrIndex
	 * @param newPut
	 * @param put
	 * @param newSolrIndex
	 * @throws Exception
	 * @author: y
	 * @time:2016-11-18 下午7:58:46
	 */
	private void updateFcTipDeep(TipsDao solrIndex, Put newPut, Put put,
			TipsDao newSolrIndex, JSONObject g_guide1, JSONObject g_guide2)
			throws Exception {

		JSONObject deep1 = JSONObject.fromObject(solrIndex.getDeep());
		;

		// 几何中心点
		JSONObject pointGeo1 = g_guide1;

		deep1.put("geo", pointGeo1);

		JSONObject deep2 = JSONObject.fromObject(solrIndex.getDeep());
		;

		// 几何中心点
		JSONObject pointGeo2 = g_guide2;

		deep2.put("geo", pointGeo2);

		put.addColumn("data".getBytes(), "deep".getBytes(), deep1.toString()
				.getBytes());

		newPut.addColumn("data".getBytes(), "deep".getBytes(), deep2.toString()
				.getBytes());

		solrIndex.setDeep(deep1.toString());

		newSolrIndex.setDeep(deep2.toString());

	}

	/**
	 * 
	 * @Description:从旧的tips复制一个新的tips
	 * @param result
	 * @param rowkey
	 * @return
	 * @author: y
	 * @time:2016-11-18 下午7:52:00
	 */
	private Put copyNewATips(Result result, String rowkey) {

		Put put = new Put(rowkey.getBytes());

		if (result != null) {
			List<Cell> ceList = result.listCells();
			if (ceList != null && ceList.size() > 0) {
				for (Cell cell : ceList) {
					String value = Bytes.toString(cell.getValueArray(),
							cell.getValueOffset(), cell.getValueLength());
					String colName = Bytes.toString(cell.getQualifierArray(),
							cell.getQualifierOffset(),
							cell.getQualifierLength());
					put.addColumn("data".getBytes(), colName.getBytes(), value
							.toString().getBytes());
				}
			}
		}
		return put;
	}

	/**
	 * @Description:通过rowkey查询tips的信息
	 * @param htab
	 * @param rowkey
	 * @param queryColNames
	 *            ：查询的字段名
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2016-11-18 下午4:42:45
	 */
	private Result getResultByRowKey(Table htab, String rowkey,
			String[] queryColNames) throws Exception {

		Result result = null;
		try {

			Get get = new Get(rowkey.getBytes());

			// 没有给定字段，则全字段查
			if (queryColNames != null && queryColNames.length != 0) {

				for (String colName : queryColNames) {

					get.addColumn("data".getBytes(), colName.getBytes());
				}
			}

			result = htab.get(get);

		} catch (Exception e) {

			logger.error("根据rowkey查询tips信息出错:" + rowkey + "原因："
					+ e.getMessage());

			throw new Exception("根据rowkey查询tips信息出错:" + rowkey + "原因："
					+ e.getMessage(), e);
		}

		return result;
	}

	/**
	 * @Description:提交（FC预处理完成，提交给web，提交后web可见）
	 * @param user
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-16 上午11:29:03
	 */
	public void submit2Web(int user, int subTaskId) throws Exception {
        Connection hbaseConn = null;
        Table htab = null;
        java.sql.Connection tipsConn=null;
        try {
        	tipsConn =DBConnector.getInstance().getTipsIdxConnection();
        	TipsIndexOracleOperator operator=new TipsIndexOracleOperator(tipsConn);
        	String sql = "select * from tips_index where s_project='"+subTaskId+"' and handler="+user+" AND t_tipStatus=1 AND s_sourceType='8001'";
        	List<TipsDao> tips = operator.query(sql);
            long totalNum = tips.size();
            if(totalNum == 0) {
                return;
            }

			List<Get> gets = new ArrayList<Get>();
			Map<String,TipsDao> tipsMap = new HashMap<String,TipsDao>();
            for (TipsDao tip :tips) {
                String rowkey = tip.getId();
				Get get = new Get(rowkey.getBytes());
				get.addColumn("data".getBytes(), "track".getBytes());
				get.addColumn("data".getBytes(), "feedback".getBytes());
				gets.add(get);
				tipsMap.put(rowkey, tip);
			}

			hbaseConn = HBaseConnector.getInstance().getConnection();
			htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

			Map<String, JSONObject> tipsTracks = loadTipsTrack(htab, gets);

			Set<String> keys = tipsTracks.keySet();
            List<Put> puts = new ArrayList<>();
            List<TipsDao> solrIndexList = new ArrayList<>();
            
			for (String rowkey : keys) {

				// 1.更新feddback和track
				JSONObject trackJson = tipsTracks.get(rowkey).getJSONObject("track");
                TipsTrack track = (TipsTrack)JSONObject.toBean(trackJson, TipsTrack.class);
                track = this.tipSubmitTrack(track, user, PretreatmentTipsOperator.PRE_TIPS_STAGE);

				// 更新hbase
				Put put = new Put(rowkey.getBytes());
				put.addColumn("data".getBytes(), "track".getBytes(), JSONObject.fromObject(track)
						.toString().getBytes());
                puts.add(put);

                //更新solr
                TipsDao solrIndex = tipsMap.get(rowkey);
                solrIndex = this.tipSubmitTrackOracle(track, solrIndex);
                solrIndexList.add(solrIndex);
			}

            //更新hbase
            htab.put(puts);
            operator.update(solrIndexList);


		} catch (IOException e) {

			logger.error(e.getMessage(), e);
			DbUtils.rollbackAndCloseQuietly(tipsConn);
			throw new Exception("tips提交出错，原因：" + e.getMessage(), e);
		}finally {
			DbUtils.commitAndCloseQuietly(tipsConn);
            if(htab != null) {
                htab.close();
            }
        }
    }

	/**
	 * 从Hbase读取Tips信息（只有track）
	 * 
	 * @param htab
	 * @param gets
	 * @throws Exception
	 */
	private Map<String, JSONObject> loadTipsTrack(Table htab, List<Get> gets)
			throws Exception {

		Map<String, JSONObject> tips = new HashMap<String, JSONObject>();

		if (0 == gets.size()) {
			return tips;
		}

		Result[] results = htab.get(gets);

		for (Result result : results) {

			if (result.isEmpty()) {
				continue;
			}

			String rowkey = new String(result.getRow());

			try {
				JSONObject jo = new JSONObject();

				String track = new String(result.getValue("data".getBytes(),
						"track".getBytes()));

				jo.put("track", track);

				tips.put(rowkey, jo);
			} catch (Exception e) {
				logger.error(e.getMessage(), e.getCause());
				throw e;
			}
		}
		return tips;
	}

	/**
	 * @Description:给定一个点，打断线几何
	 * @param point
	 * @param geojson
	 *            线几何
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-18 下午7:20:47
	 */
	private List<JSONObject> cutLineByPoint(Point point, JSONObject geojson)
			throws Exception {

		List<JSONObject> resultGeoList = new ArrayList<JSONObject>();

		Geometry geo = GeoTranslator.geojson2Jts(geojson);

		// 将坐标点扩大100000倍，（web给的坐标点，可能不在线上，有一定的误差）
		geo = GeoTranslator.transform(geo, 100000, 5);

		geojson = GeoTranslator.jts2Geojson(geo);

		double lon = point.getCoordinate().x * 100000;
		double lat = point.getCoordinate().y * 100000;

		/*
		 * double lon = point.getCoordinate().x ; double lat =
		 * point.getCoordinate().y ;
		 */
		JSONArray ja1 = new JSONArray();
		JSONArray ja2 = new JSONArray();
		JSONArray jaLink = geojson.getJSONArray("coordinates");
		boolean hasFound = false;// 打断的点是否和形状点重合或者是否在线段上
        if(jaLink.size() < 2) {
            throw new Exception("打断的点不能在link的端点");
        }
        for (int i = 0; i < jaLink.size() - 1; i++) {
			JSONArray jaPS = jaLink.getJSONArray(i);
          	if (i == 0) {
                if(point.getCoordinate().x == jaPS.getDouble(0)
                        && point.getCoordinate().y == jaPS.getDouble(1)) {
                    throw new Exception("打断的点不能在link的端点");
                }
				ja1.add(jaPS);
			}
			JSONArray jaPE = jaLink.getJSONArray(i + 1);
            if (i == jaLink.size() - 2) {
                if(point.getCoordinate().x == jaPE.getDouble(0)
                        && point.getCoordinate().y == jaPE.getDouble(1)) {
                    throw new Exception("打断的点不能在link的端点");
                }
            }

			if (!hasFound) {
				// 打断点和形状点重合(精度修改，web给的point有误差，有时候不在线上，但也需要打断)
				if (Math.abs(lon - jaPE.getDouble(0)) < 0.0000001
						&& Math.abs(lat - jaPE.getDouble(1)) < 0.0000001) {
					ja1.add(new double[] { lon, lat });
					hasFound = true;
				}
				// 打断点在线段上
				else if (GeoTranslator.isIntersection(
						new double[] { jaPS.getDouble(0), jaPS.getDouble(1) },
						new double[] { jaPE.getDouble(0), jaPE.getDouble(1) },
						new double[] { lon, lat })) {
					ja1.add(new double[] { lon, lat });
					ja2.add(new double[] { lon, lat });
					hasFound = true;
				} else {
					ja1.add(jaPE);
				}
			} else {
				ja2.add(jaPS);
			}
			if (i == jaLink.size() - 2) {
				ja2.add(jaPE);
			}
		}
		if (!hasFound) {
			throw new Exception("打断的点不在打断LINK上");
		}

		// 生成两个新的geo
		JSONObject sGeojson1 = new JSONObject();
		sGeojson1.put("type", "LineString");
		sGeojson1.put("coordinates", ja1);

		JSONObject sGeojson2 = new JSONObject();
		sGeojson2.put("type", "LineString");
		sGeojson2.put("coordinates", ja2);

        if(ja1.size() < 2) {
            throw new Exception("打断的点不能在link的端点");
        }
        if(ja2.size() < 2) {
            throw new Exception("打断的点不能在link的端点");
        }

		// 缩小0.00001倍
		sGeojson1 = GeoTranslator.jts2Geojson(GeoTranslator.transform(
				GeoTranslator.geojson2Jts(sGeojson1), 0.00001, 5));

		sGeojson2 = GeoTranslator.jts2Geojson(GeoTranslator.transform(
				GeoTranslator.geojson2Jts(sGeojson2), 0.00001, 5));

		resultGeoList.add(sGeojson1);
		resultGeoList.add(sGeojson2);

		return resultGeoList;
	}

	/**
	 * @Description:更新备注信息和fc的功能等级
	 * @param rowkey
	 * @param user
	 * @param memo
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-16 上午11:29:03
	 */
	public void updateFeedbackMemoAndDeep(String rowkey, int user, String memo,
			JSONObject deepInfo) throws Exception {
        Connection hbaseConn = null;
        Table htab = null;
        java.sql.Connection conn=null;
        try {

			hbaseConn = HBaseConnector.getInstance().getConnection();

			htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));
			// 获取solr数据
			conn = DBConnector.getInstance().getTipsIdxConnection();
			TipsIndexOracleOperator operator = new TipsIndexOracleOperator(conn);
			TipsDao solrIndex = operator.getById(rowkey);

			String sourceType = solrIndex.getS_sourceType();

			// 获取到改钱的 feddback和track
			JSONObject oldTip = getOldTips(rowkey, htab);

			// 1.更新feedback和track
			JSONObject trackJson = oldTip.getJSONObject("track");
            TipsTrack track = (TipsTrack)JSONObject.toBean(trackJson, TipsTrack.class);
            track = this.tipSaveUpdateTrack(track, PretreatmentTipsOperator.TIP_LIFECYCLE_ADD);

			// 2.更新feedback

			// 新增一个f_array type=3的是文字
			JSONObject feedBack = oldTip.getJSONObject("feedback");

			JSONArray f_array = feedBack.getJSONArray("f_array");

			for (Object object : f_array) {

				JSONObject obj = JSONObject.fromObject(object);

				// 先删掉

				if (obj.getInt("type") == 3) {

					f_array.remove(obj);

					break;
				}
			}
			// 如果count=0,则说明原来没有备注，则，增加一条

			int type = 3; // 文字

			JSONObject newFeedback = TipsUtils.newFeedback(user, memo, type,
					track.getT_date());

			f_array.add(newFeedback);

			// 更新feedback
			feedBack.put("f_array", f_array);

			JSONObject newDeep = null;

			if (FC_SOURCE_TYPE.equals(sourceType) && deepInfo != null
					&& !deepInfo.isNullObject()) {

				JSONObject deep = oldTip.getJSONObject("deep");

				newDeep = deep;

				newDeep.put("fc", deepInfo.get("fc"));
			}

			Put put = new Put(rowkey.getBytes());

			put.addColumn("data".getBytes(), "track".getBytes(), JSONObject.fromObject(track)
					.toString().getBytes());

			put.addColumn("data".getBytes(), "feedback".getBytes(), feedBack
					.toString().getBytes());

			if (newDeep != null) {
				put.addColumn("data".getBytes(), "deep".getBytes(), newDeep
						.toString().getBytes());
			}

			// 同步更新solr
			
            solrIndex = this.tipSaveUpdateTrackSolr(track, solrIndex);
			solrIndex.setFeedback( feedBack.toString());

			if (newDeep != null) {
				solrIndex.setDeep(newDeep.toString());
			}
			operator.updateOne(solrIndex);

			htab.put(put);

		} catch (IOException e) {

			e.printStackTrace();

			logger.error(e.getMessage(), e);
			DbUtils.rollbackAndCloseQuietly(conn);

			throw new Exception("改备注信息出错：rowkey:" + rowkey + "原因："
					+ e.getMessage(), e);
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
            if(htab != null) {
                htab.close();
            }
        }

	}

	/**
	 * @Description:情报矢量化新增或者修改一个tips
	 * @param jsonInfo
	 *            :tips全量信息，需要符合规格定义
	 * @param command
	 *            ：操作指令，0:新增一个tips;1：修改tips
	 * @param user
	 *            :用户id
	 * @author: y
	 * @return
	 * @throws Exception
	 * @time:2017-3-13 下午3:45:36
	 */
	public String saveOrUpdateTips(JSONObject jsonInfo, int command, int user, int dbId)
			throws Exception {
		String rowkey = "";
		Connection hbaseConn = null;
		java.sql.Connection tipsConn=null;
        Table htab = null;
        Map<String, String> allNeedDiffRowkeysCodeMap = new HashMap<String, String>(); // 所有入库需要差分的tips的<rowkey,code
		try {
			tipsConn =DBConnector.getInstance().getTipsIdxConnection();
            JSONObject source = jsonInfo.getJSONObject("source");
            String sourceType = source.getString("s_sourceType");

            if(StringUtils.isEmpty(sourceType)) {
                logger.error("新增tips出错：原因：sourceType为空");
                throw new Exception("新增tips出错：原因：sourceType为空");
            }

            JSONObject geoJson = jsonInfo.getJSONObject("geometry");
            JSONObject locationJson = geoJson.getJSONObject("g_location");
            Geometry locationGeo = null;
            try {
                locationGeo = GeoTranslator.geojson2Jts(locationJson);
            }catch (Exception e) {
                e.printStackTrace();
            }
            if(locationGeo == null) {
                logger.error("新增tips出错：原因：显示坐标非法");
                throw new Exception("新增tips出错：原因：显示坐标非法");
            }

            if(sourceType.equals("1205") || sourceType.equals("1206")
                    || sourceType.equals("1211")) {//新增或者修改
                JSONObject deepJson = jsonInfo.getJSONObject("deep");
                JSONObject fJson = deepJson.getJSONObject("f");
                String relateId = fJson.getString("id");
                int relateLinkType = fJson.getInt("type");

                if(relateLinkType == 1) {//GDB LINK
                    java.sql.Connection oraConn = DBConnector.getInstance().getConnectionById(dbId);
                    boolean isGdbHas = TipsPreCheckUtils.hasInGdb(oraConn, relateId);
                    if(isGdbHas) {
                        logger.error("新增tips出错：原因：关联要素具有上线下分离属性link");
                        throw new Exception("新增tips出错：原因：关联要素具有上线下分离属性link");
                    }
                }

                boolean isSolrHas = TipsPreCheckUtils.hasInOracle(tipsConn, relateId);
                if(isSolrHas) {//Solr
                    logger.error("新增tips出错：原因：关联要素具有上线下分离属性Tips");
                    throw new Exception("新增tips出错：原因：关联要素具有上线下分离属性Tips");
                }
            }

            hbaseConn = HBaseConnector.getInstance().getConnection();
			htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

			String date = StringUtils.getCurrentTime();

			// 新增
			if (command == COMMAND_INSERT) {

				rowkey = TipsUtils.getNewRowkey(sourceType); // 新增的，需要生成rowkey

				jsonInfo.put("rowkey", rowkey);

				//20170518 情报矢量化新增7种Tips deep.id赋值 灵芸and赵航
                if(TipsStatConstant.preTipsDeepIdType.contains(sourceType)) {
                    JSONObject deepJson = jsonInfo.getJSONObject("deep");
                    deepJson.put("id", rowkey.substring(6,rowkey.length()));
                    jsonInfo.put("deep", deepJson);
                }
                
                rowkey=insertOneTips(tipsConn,command,jsonInfo, user, htab, date,dbId);
			}
			// 修改
			else {
				rowkey = jsonInfo.getString("rowkey");

				rowkey=updateOneTips(tipsConn,jsonInfo, user, htab, date,dbId); // 同时修改hbase和solr
			}
			
			//需要进行tips差分
			allNeedDiffRowkeysCodeMap.put(rowkey, sourceType);

		} catch (Exception e) {
			logger.error("更新tips出错：" + e.getMessage() + "\n" + jsonInfo, e);
			DbUtils.rollbackAndCloseQuietly(tipsConn);
			throw new Exception("更新tips出错：" + e.getMessage(),
					e);
		}finally {
			DbUtils.commitAndCloseQuietly(tipsConn);
            if(htab != null) {
                htab.close();
            }
        }
        //Tips差分放在oracle索引提交之后执行，才可以查询到oracle索引的数据
		//20170808  确认，web渲染不再使用差分结果。因此取消差分 
		//TipsDiffer.tipsDiff(allNeedDiffRowkeysCodeMap);
        return rowkey;
	}

	/**
	 * @Description:TOOD
	 * @param jsonInfo
	 * @param user
	 * @param sourceType
	 * @param htab
	 * @param date
	 * @throws Exception
	 * @author: y
	 * @param command 
	 * @time:2017-6-21 下午9:22:54
	 */
	private String cutByMesh(java.sql.Connection tipsConn,int command, JSONObject jsonInfo, int user, String sourceType,
			Table htab, String date,int dbId) throws Exception {
		String returnRowkey ="";//返回给web的rowkey，打断的话没返回打断后的任意一条
		
		JSONObject gLocation = jsonInfo.getJSONObject("geometry").getJSONObject("g_location");
		Geometry geo= GeoTranslator.geojson2Jts(gLocation);
		List<Geometry> geoList=TipsOperatorUtils.cutGeoByMeshes(geo); //按照图幅打断成多个几何
		
		//打断后的测线tips
		List<TipsDao> allTips=new ArrayList<TipsDao>();
		String oldRowkey=jsonInfo.getString("rowkey"); //打断前的rowkey
		
		boolean hasModifyGlocation=hasModifyGLocation(tipsConn,command,oldRowkey,gLocation);
		
		//跨图幅不需要打断，直接保存
		if(geoList==null||geoList.size()==0){
			
			doInsert(tipsConn,jsonInfo, htab, date); 
			
			returnRowkey=oldRowkey;
			
			//这个地方需要加 维护测线上关联tips的角度和引导link
			//2.维护角度的时候，判断一一下测线的显示坐标是否改了，没有改不维护（提高效率）
			
			if(hasModifyGlocation){
				
				TipsDao  obj=new TipsDao();
			    obj.setId(oldRowkey);
			    obj.setG_location(gLocation.toString());
			    allTips.add(obj);
			    //allTips就是当前的tips
				maintainHookTips(tipsConn,oldRowkey,user, allTips,hasModifyGlocation,dbId);
			}
			
			
			return returnRowkey;  
		    
		}
		
		for (Geometry loctionGeometry : geoList) {
			
			JSONObject jsonInfoNew = JSONObject.fromObject(jsonInfo); 
			String newRowkey = TipsUtils.getNewRowkey(sourceType);
			//1.更新rowkey
			jsonInfoNew.put("rowkey", newRowkey); 
			System.out.println("new rowkey-----:"+newRowkey);
			//2.更新geometry
			JSONObject geometry=new JSONObject();
			JSONObject  g_location=GeoTranslator.jts2Geojson(loctionGeometry);
			geometry.put("g_location", g_location);//更新geometry.g_location坐标
			JSONObject g_guide = GeoTranslator.jts2Geojson( GeometryUtils.getMidPointByLine(loctionGeometry));
			geometry.put("g_guide", g_guide);//更新geometry.g_guide坐标
			jsonInfoNew.put("geometry", geometry);
			
			//3. update deep
			//更新deep.geo
			JSONObject newDeep = JSONObject.fromObject(jsonInfo.get("deep"));
			// 几何中心点
			newDeep.put("geo",g_guide);
			//更新deep.len
		    double len = GeometryUtils.getLinkLength(loctionGeometry);
		    newDeep.put("len", len);
		    //更新新deep.id
		    // ROWKEY 维护7种要素id 测线在内
		    newDeep.put("id", newRowkey.substring(6, newRowkey.length()));
		    jsonInfoNew.put("deep", newDeep);
		    
		    //4.保存数据
		    doInsert(tipsConn,jsonInfoNew, htab, date); 
		    
		    TipsDao  obj=new TipsDao();
		    obj.setId(newRowkey);
		    obj.setG_location(g_location.toString());
		    allTips.add(obj);
		    
		    //返回任意一条rowkey
		    if(StringUtils.isEmpty(returnRowkey)){
		    	returnRowkey=newRowkey;
		    }
		    
		}
		
		//如果是修改的，则需要按照打断后的多根测线，维护测线上的tips
		if(command==COMMAND_UPADATE){
			maintainHookTips(tipsConn,oldRowkey,user, allTips,hasModifyGlocation,dbId);
			deleteByRowkey(oldRowkey, 1); //将旧的rowkey删除（物理删除）
		}
		
		return returnRowkey;
		
	}

	/**
	 * @Description:判断坐标是否修改
	 * @param oldRowkey
	 * @param gLocation
	 * @return
	 * @author: y
	 * @param command 
	 * @throws Exception 
	 * @time:2017-7-5 上午9:22:13
	 */
	private boolean hasModifyGLocation(java.sql.Connection tipsConn,int command, String oldRowkey, JSONObject gLocation) throws Exception {
		
		//新增的
		if(command==COMMAND_INSERT){
			
			return false;
		}
        TipsIndexOracleOperator operator = new TipsIndexOracleOperator(tipsConn);
        TipsDao tipsDao = operator.getById(oldRowkey);
		
		if(tipsDao == null){
			return false;
		}
		
		String oldLocation = tipsDao.getG_location();
        Geometry oldGeo = GeoTranslator.geojson2Jts(JSONObject.fromObject(oldLocation));
        String oldWkt = GeoTranslator.jts2Wkt(oldGeo);
        Geometry gLocationGeo = GeoTranslator.geojson2Jts(gLocation);
        String gLocationWkt = GeoTranslator.jts2Wkt(gLocationGeo);
		if(!oldWkt.equals(gLocationWkt)){
			
			return true;
		}
		
		return false;
	}

	/**
	 * @Description:TOOD
	 * @param jsonInfo
	 * @param currentDate
	 * @author: y
	 * @throws Exception
	 * @time:2017-3-14 上午9:25:33
	 */
	private TipsDao addSolr(JSONObject jsonInfo, String currentDate)
			throws Exception {
		try {
            TipsDao tipsIndexModel = TipsUtils.generateSolrIndex(jsonInfo.getString("rowkey"), currentDate,
                    jsonInfo.getJSONObject("track"), jsonInfo.getJSONObject("source"), jsonInfo.getJSONObject("geometry"),
                    jsonInfo.getJSONObject("deep"),jsonInfo.getJSONObject("feedback"));
			return tipsIndexModel;
		} catch (Exception e) {
			logger.error("更新索引出错：" + e.getMessage());
			e.printStackTrace();
			throw new Exception("更新索引出错：" + e.getMessage(), e);
		}

	}

	/**
	 * @Description:更新tips的信息（全量更新）
	 * @param jsonInfo
	 *            ：tips信息（符合规格定义的）
	 * @param user
	 *            ：用户id
	 * @author: y
	 * @param htab
	 * @param date
	 * @throws Exception
	 * @time:2017-3-13 下午6:09:23
	 */
	private String  updateOneTips(java.sql.Connection tipsConn,JSONObject jsonInfo, int user, Table htab,
			String date,int dbId) throws Exception {

		String rowkey = jsonInfo.getString("rowkey");
		try {
			TipsSelector selector = new TipsSelector();
			JSONObject data = selector.searchDataByRowkeyNew(rowkey);
			if (data == null) {
				throw new Exception("没有找到要修改的数据：rowkey" + rowkey);
			}

			// 需要判断是原库的还是 情报的，如果是原库的则，修改lifeCycle=2.否则 lifeCyCle=3
			// 判断是情报的原则：lifeCycle=3且最后一条stage=6
//			int newlifeCycle = getNewLifeCycle(data);
//			JSONObject dataTrack = jsonInfo.getJSONObject("track");
//			dataTrack.put("t_lifecycle", newlifeCycle);
//			jsonInfo.put("track", dataTrack);

			return insertOneTips(tipsConn,COMMAND_UPADATE,jsonInfo, user, htab, date,dbId); // solr信息和hbase数据都直接覆盖（operate_date要不要覆盖？）

		} catch (Exception e) {
			logger.error("修改tips出错,rowkey:" + rowkey + "\n原因：" + e.getMessage());
			throw new Exception("修改tips出错,rowkey:" + rowkey + "\n"
					+ e.getMessage(), e);
		}

	}

	/**
	 * @Description:需要判断是原库的还是 情报的，如果是原库的则，修改lifeCycle=2.否则 lifeCyCle=3
	 *                         判断是情报的原则：lifeCycle=3且最后一条stage=6
	 * @param data
	 * @author: y
	 * @time:2017-4-10 下午2:53:02
	 */
	private int getNewLifeCycle(JSONObject data) {

		JSONObject track = data.getJSONObject("track");

        if(track.containsKey("t_trackInfo")) {

        }
		JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");
		JSONObject lastTrackInfo = trackInfoArr.getJSONObject(trackInfoArr
				.size() - 1);
		int stage = lastTrackInfo.getInt("stage");
		int lifeCycle = track.getInt("t_lifecycle");

		if (lifeCycle == 3 && stage == 6) {

			lifeCycle = 3;
		} else {
			lifeCycle = 2;
		}

		return lifeCycle;

	}

	/**
	 * @Description:新增一个tips
	 * @param jsonInfo
	 *            ：tips信息（符合规格定义的）
	 * @param user
	 *            ：用户id
	 * @author: y
	 * @param command 
	 * @param htab
	 * @param date
	 * @throws Exception
	 * @time:2017-3-13 下午4:47:54
	 */
	private String insertOneTips(java.sql.Connection tipsConn,int command, JSONObject jsonInfo, int user, Table htab,
			String date,int dbId) throws Exception {
		String returnRowkey ="";//返回给web的rowkey，打断的话没返回打断后的任意一条
		try {
			JSONObject source = jsonInfo.getJSONObject("source");
            String sourceType = source.getString("s_sourceType");

		     //如果是2001 测线，则需要判断按图幅打断
            if(sourceType.equals("2001")){
            	
            	returnRowkey=cutByMesh(tipsConn,command,jsonInfo, user, sourceType, htab, date,dbId);
            	
            }else{
            	
            	doInsert(tipsConn,jsonInfo, htab, date);
            	
            	returnRowkey=jsonInfo.getString("rowkey");
            }

		} catch (Exception e) {
			logger.error("新增tips出错：" + e.getMessage() + "\n" + jsonInfo, e);
			throw new Exception("新增tips出错：" + e.getMessage() + "\n" ,
					e);
		}
		
		return returnRowkey;

	}

	/**
	 * @Description:TOOD
	 * @param jsonInfo
	 * @param htab
	 * @param date
	 * @throws IOException
	 * @throws Exception
	 * @author: y
	 * @time:2017-6-21 下午9:24:34
	 */
	private void doInsert(java.sql.Connection tipsConn,JSONObject jsonInfo, Table htab, String date)
			throws Exception {
		Put put = assembleNewPut(jsonInfo);

		htab.put(put);
		
		TipsIndexOracleOperator operator=new TipsIndexOracleOperator(tipsConn);
		TipsDao solrIndex = addSolr(jsonInfo, date);
		operator.updateOne(solrIndex);
	}

	/**
	 * @Description:组装一个新的tips put
	 * @param jsonInfo
	 *            :符合tips规格定义的json信息
	 * @return
	 * @author: y
	 * @param jsonInfo
	 * @time:2017-3-14 上午9:52:16
	 */
	private Put assembleNewPut(JSONObject jsonInfo) {

		String rowkey = jsonInfo.getString("rowkey");

		Put put = new Put(rowkey.getBytes());

		put.addColumn("data".getBytes(), "source".getBytes(), jsonInfo
				.getJSONObject("source").toString().getBytes());

        com.alibaba.fastjson.JSONObject fastGeo = TipsUtils.netJson2fastJson(jsonInfo
                .getJSONObject("geometry"));
		put.addColumn("data".getBytes(), "geometry".getBytes(), fastGeo.toString().getBytes());

		if (jsonInfo.containsKey("information")) {
			put.addColumn("data".getBytes(), "information".getBytes(), jsonInfo
					.getJSONObject("information").toString().getBytes());
		}

        com.alibaba.fastjson.JSONObject fastDeep = TipsUtils.netJson2fastJson(jsonInfo
                .getJSONObject("deep"));
		put.addColumn("data".getBytes(), "deep".getBytes(), fastDeep.toString().getBytes());

		// track信息需要重新组织，需要修改date时间

		JSONObject trackJson = jsonInfo.getJSONObject("track");
        TipsTrack track = (TipsTrack)JSONObject.toBean(trackJson, TipsTrack.class);
        if(track.getT_lifecycle() == TIP_LIFECYCLE_INIT) {
            track.setT_lifecycle(TIP_LIFECYCLE_UPDATE);
        }
        track = this.tipSaveUpdateTrack(track, track.getT_lifecycle());
//		JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");
//
//		JSONObject lastTrackInfo = trackInfoArr.getJSONObject(trackInfoArr
//				.size() - 1);
//
//		trackInfoArr.remove(lastTrackInfo);
//
//		lastTrackInfo.put("date", date); // 修改时间，为服务的当前时间
//
//		trackInfoArr.add(lastTrackInfo);
//
//		track.put("t_trackInfo", trackInfoArr);

//		track.put("t_date", date);// 修改时间，为服务的当前时间
////       20170711维护
//        track.put("t_tipStatus", 1);

        jsonInfo.put("track", JSONObject.fromObject(track));
		put.addColumn("data".getBytes(), "track".getBytes(), JSONObject.fromObject(track).toString()
				.getBytes());

		if (jsonInfo.containsKey("recommended")) {

			put.addColumn("data".getBytes(), "recommended".getBytes(), jsonInfo
					.getJSONObject("recommended").toString().getBytes());
		}

		if (!jsonInfo.containsKey("feedback")) {
            JSONArray infoArr = new JSONArray();
            JSONObject feedback = new JSONObject();
            feedback.put("f_array", infoArr);
            jsonInfo.put("feedback",feedback);
        }
        com.alibaba.fastjson.JSONObject fastFeedback = TipsUtils.netJson2fastJson(jsonInfo
                .getJSONObject("feedback"));
        put.addColumn("data".getBytes(), "feedback".getBytes(), fastFeedback.toString().getBytes());

		if (jsonInfo.containsKey("confirm")) {

			put.addColumn("data".getBytes(), "confirm".getBytes(), jsonInfo
					.getJSONObject("confirm").toString().getBytes());
		}

		if (jsonInfo.containsKey("tipdiff")) {

			put.addColumn("data".getBytes(), "tipdiff".getBytes(), jsonInfo
					.getJSONObject("tipdiff").toString().getBytes());
		}

		if (jsonInfo.containsKey("old")) {

			put.addColumn("data".getBytes(), "old".getBytes(), jsonInfo
					.getJSONObject("old").toString().getBytes());
		}
		return put;
	}

	/**
	 * @Description:批量新增tips
	 * @param jsonInfoArr
	 *            :tips数组
	 * @param user
	 * @author: y
	 * @param command   ：操作指令，0:新增一个tips;1：修改tips
	 * @throws Exception
	 * @time:2017-3-13 下午3:57:32
	 */
	public void batchSaveOrUpdate(JSONArray jsonInfoArr, int user, int command) throws Exception {

		Connection hbaseConn = null;
		java.sql.Connection tipsConn=null;
        Table htab = null;
        Map<String, String> allNeedDiffRowkeysCodeMap = new HashMap<String, String>(); // 所有入库需要差分的tips的<rowkey,code
		
		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();
			htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			String date = StringUtils.getCurrentTime();

			List<Put> puts = new ArrayList<Put>();
            List<TipsDao> solrIndexList = new ArrayList<>();
            
			for (Object jsonInfo : jsonInfoArr) {

				JSONObject tipsInfo = JSONObject.fromObject(jsonInfo);
				
				String rowkey=tipsInfo.getString("rowkey");
				
				JSONObject source = tipsInfo.getJSONObject("source");
				
				String sourceType = source.getString("s_sourceType");
				
				//如果是新增，则生成rowkey
				
				if(command==COMMAND_INSERT){
					
					rowkey = TipsUtils.getNewRowkey(sourceType); // 新增的，需要生成rowkey

					tipsInfo.put("rowkey", rowkey);

                    //20170518 情报矢量化新增7种Tips deep.id赋值 灵芸and赵航
                    if(TipsStatConstant.preTipsDeepIdType.contains(sourceType)) {
                        JSONObject deepJson = tipsInfo.getJSONObject("deep");
                        deepJson.put("id", rowkey.substring(6, rowkey.length()));
                        tipsInfo.put("deep", deepJson);
                    }
				}

				Put put = assembleNewPut(tipsInfo); // 未调用insertOneTips，而分开为两部，是避免多次写hbase,效率降低
				puts.add(put);

				
				TipsDao solrIndex = addSolr(tipsInfo, date);
                solrIndexList.add(solrIndex);
				//需要进行tips差分
				allNeedDiffRowkeysCodeMap.put(rowkey, sourceType);
			}

			htab.put(puts);
			tipsConn=DBConnector.getInstance().getTipsIdxConnection();
			TipsIndexOracleOperator operator=new TipsIndexOracleOperator(tipsConn);
			operator.update(solrIndexList);
            //solr.addTips(solrIndexList);

		} catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(tipsConn);
			logger.error("批量新增tips出错：" + e.getMessage(), e);
			throw new Exception("批量新增tips出错：" + e.getMessage(), e);
		}finally {
            if(htab != null) {
                htab.close();
            }
            DbUtils.commitAndCloseQuietly(tipsConn);
        }
        //Tips差分放在oracle索引提交之后执行，才可以查询到oracle索引的数据
		//20170808  确认，web渲染不再使用差分结果。因此取消差分 
       // TipsDiffer.tipsDiff(allNeedDiffRowkeysCodeMap);

	}

	/**
	 * @Description:判断情报矢量化的tip删除，是逻辑删除还是物理删除 判断原则：
	 * @param rowkey
	 * @param subTaskId
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2017-4-12 下午4:07:37
	 */
	public int getDelTypeByRowkeyAndUserId(String rowkey, int subTaskId)
			throws Exception {
		//20170720 物理删除：t_lifecycle=3，trackinfo=[]，source-->s_qSubTaskId/s_mSubTaskId=当前子任务
        Connection hbaseConn = null;
        Table htab = null;
        try{
            hbaseConn = HBaseConnector.getInstance().getConnection();
            htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

            Result result = htab.get(new Get(rowkey.getBytes()));
            if(result.isEmpty()) {
                return TIP_NOT_DELETE;
            }

            JSONObject trackJson = JSONObject.fromObject(new String(result.getValue(
                    "data".getBytes(), "track".getBytes())));
            JSONObject sourceJson = JSONObject.fromObject(new String(result.getValue(
                    "data".getBytes(), "source".getBytes())));
            TipsTrack track = (TipsTrack)JSONObject.toBean(trackJson, TipsTrack.class);
            List trackInfoList = track.getT_trackInfo();
            int lifecycle = track.getT_lifecycle();
            int taskType = this.getTaskType(subTaskId);//// 1，中线 4，快线
            if(taskType == 0) {
                return TIP_NOT_DELETE;
            }
            int tipTaskId = 0;
            if(taskType == TaskType.Q_TASK_TYPE) {//快线子任务
                tipTaskId = sourceJson.getInt("s_qSubTaskId");
            }else if(taskType == TaskType.M_TASK_TYPE) {//中线子任务
                tipTaskId = sourceJson.getInt("s_mSubTaskId");
            }

            int delType = TIP_LOGICAL_DELETE;//默认逻辑删除0
            if((trackInfoList == null || trackInfoList.size() == 0)
                    && lifecycle == 3 && tipTaskId == subTaskId) {
                delType = TIP_PHYSICAL_DELETE;
            }
            return delType;
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(htab != null) {
                htab.close();
            }
        }
        return 2;
	}

	/**
	 * @Description:情报测线打断 1.测线打段位2根 2.维护挂在该测线上的所有的tips的测线号码(任务范围内的)
	 *                     注明：只维护：情报矢量化的26类tips.不区分状态
	 * @param rowkey
	 *            :测线的rowkey
	 * @param pointGeo
	 *            打断点的坐标
	 * @param user
	 *            ：用户id
	 * @param jobType
	 *            任务类型
	 * @author: y
	 * @throws Exception
	 * 
	 * @time:2017-4-12 下午8:24:43
	 */
	public void cutMeasuringLineCut(String rowkey, JSONObject pointGeo,
			int user, int subTaskId, int jobType,int dbId) throws Exception {
		// 第一步：按打断点，生成两个tips
		java.sql.Connection tipsConn=null;
		try{
			tipsConn=DBConnector.getInstance().getTipsIdxConnection();
			List<TipsDao> resultArr = breakLine2(tipsConn,rowkey, pointGeo, user);
			//第二步 ：维护测线上挂接的tips
			maintainHookTips(tipsConn,rowkey,user, resultArr,false,dbId);
		}catch (Exception e) {
			logger.error("", e);
			DbUtils.rollbackAndCloseQuietly(tipsConn);
			throw e;
		}finally {
			DbUtils.commitAndCloseQuietly(tipsConn);
		}
		

	}

    /**
     * 维护测线上挂接的tips
     * @param tipsConn
     * @param oldRowkey
     * @param user
     * @param resultArr
     * @param hasModifyGlocation
     * @throws SolrServerException
     * @throws IOException
     * @throws Exception
     */
	private void maintainHookTips(java.sql.Connection tipsConn,String oldRowkey, int user, List<TipsDao> resultArr, boolean hasModifyGlocation,int dbId)
			throws Exception {

		// 查询关联Tips
		//20170615 查询和原测线关联的所有Tips		
        String query = "select * from tips_index i where exists(select 1 from tips_links l where i.id=l.id"
        		+ " and l.Link_Id=? )";
        TipsIndexOracleOperator operator=new TipsIndexOracleOperator(tipsConn);
        List<TipsDao> tipsDaos = operator.query(query, oldRowkey);
        
        //List<JSONObject> snapotList = solr.queryTips(query, null);
        List<TipsDao>  updateResult=new  ArrayList<TipsDao>();//维护后的tips （json） List

		for (TipsDao json : tipsDaos) {

			//1.维护关联的测线
			//size>1说明跨图幅打断了，进行打断维护
			if(resultArr.size()>1){
				
				json=updateRelateMeasuringLine(tipsConn,oldRowkey,json, resultArr,dbId);
			}
			
			//2.维护角度和引导坐标 (修改了坐标的才维护)
			if(hasModifyGlocation){
				
				json=updateGuiderAndAgl(tipsConn,json,resultArr,dbId); //若果是跨图幅打断resultArr是多条~~。如果跨图幅没打断 resultArr是一条。就是测线本身	
			}
			
			if(json!=null){
				
				updateResult.add(json);
			}

		}
		//更新后的数据进行更新（只更新了deep+relate_links+g_location+g_guide+solr还需要更新wkt和wktLocation!!）
		saveUpdateData(updateResult,user);
	}

	
	/**
	 * @Description:维护tips的引导坐标和角度
	 * @param result
	 * @return
	 * @author: y
	 * @param tipsConn 
	 * @param linesAfterCut 
	 * @param dbId 
	 * @throws Exception 
	 * @time:2017-6-26 下午7:00:07
	 */
	private TipsDao updateGuiderAndAgl(java.sql.Connection tipsConn, TipsDao result, List<TipsDao> linesAfterCut, int dbId) throws Exception {
		
		RelateTipsGuideAndAglUpdate up=new RelateTipsGuideAndAglUpdate(result, linesAfterCut,dbId,tipsConn);
		
		result=up.excute();
		
		return result;
	}
	
	

	/**
	 * 保存测线打断后维护的数据结果
	 * @param updateResult
	 * @param user 
	 * @throws Exception 
	 */
	private void saveUpdateData( List<TipsDao>  updateResult, int user) throws Exception {
		try {
			batchUpdateRelateTips(updateResult);
		} catch (Exception e) {
		logger.error("测线打断，批量修改出错，"+e.getMessage());
		throw new Exception("测线打断，批量修改出错，"+e.getMessage(), e);
		}
	}

    private void batchUpdateRelateTips(List<TipsDao> updateResult) throws Exception {

        Connection hbaseConn = null;
        java.sql.Connection tipsConn=null;
		Table htab = null;
		Map<String, String> allNeedDiffRowkeysCodeMap = new HashMap<String, String>(); // 所有入库需要差分的tips的<rowkey,code

        try {
            hbaseConn = HBaseConnector.getInstance().getConnection();
            htab = hbaseConn.getTable(TableName
                    .valueOf(HBaseConstant.tipTab));

            List<Put> puts = new ArrayList<Put>();
            List<TipsDao> solrIndexList = new ArrayList<>();
            tipsConn=DBConnector.getInstance().getTipsIdxConnection();
            TipsIndexOracleOperator operator=new TipsIndexOracleOperator(tipsConn);
            for (TipsDao dao : updateResult) {

                //JSONObject tipsInfo = JSONObject.fromObject(jsonInfo);

                String rowkey = dao.getId();
                JSONObject g_location=JSONObject.fromObject(dao.getG_location());
                JSONObject g_guide=JSONObject.fromObject(dao.getG_guide());
                
                //更新hbase
                Put put = new Put(rowkey.getBytes());
                JSONObject deep = JSONObject.fromObject(dao.getDeep()); //更新deep
                put.addColumn("data".getBytes(), "deep".getBytes(), deep.toString()
                        .getBytes());
                JSONObject geometry=new JSONObject(); //更新geometry
                geometry.put("g_location", g_location);
                geometry.put("g_guide", g_guide);
                put.addColumn("data".getBytes(), "geometry".getBytes(), geometry.toString()
                        .getBytes());
                puts.add(put);

                //更新 wkt 及wktLocation  relate_links
                JSONObject feedback=JSONObject.fromObject(dao.getFeedback());
                String sourceType=dao.getS_sourceType();
                dao.setWktLocation(TipsImportUtils.generateSolrWkt(sourceType, deep,g_location, feedback));
                dao.setWkt(TipsImportUtils.generateSolrStatisticsWkt(sourceType, deep,g_location, feedback));
                
                Map<String,String >relateMap = TipsLineRelateQuery.getRelateLine(sourceType, deep);
                dao.setRelate_links(relateMap.get("relate_links"));
                solrIndexList.add(dao);

                //需要进行tips差分
                allNeedDiffRowkeysCodeMap.put(rowkey, sourceType);
            }

            htab.put(puts);
           
            operator.update(solrIndexList);

        } catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(tipsConn);
            logger.error("批量新增tips出错：" + e.getMessage(), e);
            throw new Exception("批量新增tips出错：" + e.getMessage(), e);
        }finally {
			if(htab != null) {
                htab.close();
            }
            DbUtils.commitAndCloseQuietly(tipsConn);
		}
        //Tips差分放在oracle索引提交之后执行，才可以查询到oracle索引的数据
        //20170808  确认，web渲染不再使用差分结果。因此取消差分 
        //TipsDiffer.tipsDiff(allNeedDiffRowkeysCodeMap);
    }
    
    
    /**
	 * @Description:删除tips--情报预处理的删除，重写父类（情报预处理测线删除需要删除测线上的tips）
	 * @param rowkey
	 * @author: y
	 * @param delType :0 逻辑删除，1：物理删除
	 * @throws Exception
	 * @time:2016-11-16 下午5:21:09
	 */
	public void deleteByRowkey(String rowkey, int delType, int subTaskId) throws Exception {
		try {
			
			//物理删除
			if(delType == TIP_PHYSICAL_DELETE){
				
				deletRelateTipsWhenIsMeasureLine(rowkey); //如果是测线，则删除测线上关联的tips
				
				super.physicalDel(rowkey); //删除自己
			}
			//逻辑删除
			else{
                prepLogicDel(rowkey, subTaskId);
			}

		} catch (SolrServerException e) {

			logger.error("删除tips失败，rowkey：" + rowkey + "\n" + e.getMessage(), e);

			throw new Exception(
					"删除tips失败，rowkey：" + rowkey + "\n" + e.getMessage(), e);
		}

	}

    /**
     * @Description:逻辑删除tips(将t_lifecycle改为1：删除)
     * @param rowkey：被删除的tips的rowkey
     * @author: y
     * @throws Exception
     * @time:2017-4-8 下午4:14:57
     */
    protected void prepLogicDel(String rowkey, int subTaskId) throws Exception {
        Connection hbaseConn = null;
        Table htab = null;
        java.sql.Connection oracleConn = null;
        try {
            //修改hbase
            hbaseConn = HBaseConnector.getInstance().getConnection();

            htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

            Get get = new Get(rowkey.getBytes());

            get.addColumn("data".getBytes(), "track".getBytes());
            get.addColumn("data".getBytes(), "source".getBytes());

            Result result = htab.get(get);

            if (result.isEmpty()) {
                throw new Exception("根据rowkey,没有找到需要删除的tips信息，rowkey：" + rowkey);
            }

            Put put = new Put(rowkey.getBytes());

            JSONObject trackJson = JSONObject.fromObject(new String(result.getValue(
                    "data".getBytes(), "track".getBytes())));

            TipsTrack track = (TipsTrack)JSONObject.toBean(trackJson, TipsTrack.class);
            track = this.tipSaveUpdateTrack(track, BaseTipsOperate.TIP_LIFECYCLE_DELETE);
            put.addColumn("data".getBytes(), "track".getBytes(), JSONObject.fromObject(track).toString()
                    .getBytes());

            //20170813逻辑删除维护子任务ID
            JSONObject sourceJson = JSONObject.fromObject(new String(result.getValue(
                    "data".getBytes(), "source".getBytes())));
            Map<String, Integer> taskMap = this.getTaskInfoMap(subTaskId);//// 1，中线 4，快线
            int taskType = taskMap.get("programType");
            int taskId = taskMap.get("taskId");
            if(taskType == TaskType.Q_TASK_TYPE) {//快线子任务
                sourceJson.put("s_qTaskId", taskId);
                sourceJson.put("s_qSubTaskId", subTaskId);
                sourceJson.put("s_mTaskId", 0);
                sourceJson.put("s_mSubTaskId", 0);
            }else if(taskType == TaskType.M_TASK_TYPE) {//中线子任务
                sourceJson.put("s_mTaskId", taskId);
                sourceJson.put("s_mSubTaskId", subTaskId);
                sourceJson.put("s_qTaskId", 0);
                sourceJson.put("s_qSubTaskId", 0);
            }
            put.addColumn("data".getBytes(), "source".getBytes(), sourceJson.toString()
                    .getBytes());

            htab.put(put);

            //同步更新index
            oracleConn = DBConnector.getInstance().getTipsIdxConnection();
            TipsIndexOracleOperator operator = new TipsIndexOracleOperator(oracleConn);
            TipsDao tipsIndex = operator.getById(rowkey);
            tipsIndex = this.tipSaveUpdateTrackSolr(track, tipsIndex);
            tipsIndex.setS_qTaskId(sourceJson.getInt("s_qTaskId"));
            tipsIndex.setS_qSubTaskId(sourceJson.getInt("s_qSubTaskId"));
            tipsIndex.setS_mTaskId(sourceJson.getInt("s_mTaskId"));
            tipsIndex.setS_mSubTaskId(sourceJson.getInt("s_mSubTaskId"));
            List<TipsDao> tipsIndexList = new ArrayList<TipsDao>();
            tipsIndexList.add(tipsIndex);
            operator.update(tipsIndexList);
        }catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(oracleConn);
            e.printStackTrace();
            logger.error("逻辑删除失败"+rowkey+":", e);
        }finally {
            if(htab != null) {
                htab.close();
            }
            DbUtils.commitAndCloseQuietly(oracleConn);
        }

    }

    /**
	 * @Description:测线删除，需要删除测线上挂接的所有tips
	 * @param rowkey
	 * @author: y
     * @throws Exception 
	 * @time:2017-8-10 下午3:16:35
	 */
	private void deletRelateTipsWhenIsMeasureLine(String rowkey) throws Exception {
		java.sql.Connection tipsIndexConn=null;
		Connection hbaseConn = null;
        Table htab = null;
		try{
			//1.判断类型是2001
			tipsIndexConn = DBConnector.getInstance().getTipsIdxConnection();
			TipsIndexOracleOperator  operate=new TipsIndexOracleOperator(tipsIndexConn);
			TipsDao  dao= operate.getById(rowkey);
			//如果不是测线，则不处理
			if(!"2001".equals(dao.getS_sourceType())){
				return;
			}
			
			// 2.查询关联Tips
			//20170615 查询和原测线关联的所有Tips		
	        String query = "select * from tips_index i where exists(select 1 from tips_links l where i.id=l.id"
	        		+ " and l.Link_Id=? )";
	        TipsIndexOracleOperator operator=new TipsIndexOracleOperator(tipsIndexConn);
	        List<TipsDao> tipsDaos = operator.query(query, rowkey);
	        
	        //3.删除关联tips-删索引
	        operator.delete(tipsDaos);
	        
	        //4.删habse
	        if(tipsDaos!=null&&tipsDaos.size()!=0){
        	  hbaseConn = HBaseConnector.getInstance().getConnection();
              htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));
              List list = new ArrayList();
              for (TipsDao tipsDao : tipsDaos) {
            	  Delete d1 = new Delete(tipsDao.getId().getBytes());
            	  list.add(d1);
  			  }
              htab.delete(list);
	        }
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(tipsIndexConn);
			logger.error("删除测线关联tips出错："+e.getMessage(), e);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(tipsIndexConn);
			if(htab != null) {
                htab.close();
            }
		}
		
	}

	/**
	 * @Description:测线打断:返回打断后的两条tips sorl信息
	 * @param rowkey
	 * @param pointGeo
	 * @param user
	 * @author: y
	 * @time:2017-4-17 下午4:12:25
	 */
	private List<TipsDao> breakLine2(java.sql.Connection tipsConn,String rowkey, JSONObject pointGeo, int user) throws Exception {
		
		List<TipsDao> resultArr=new ArrayList<TipsDao>();
		
		Connection hbaseConn;
		try {
			TipsIndexOracleOperator operator=new TipsIndexOracleOperator(tipsConn);
			TipsDao solrIndex = operator.getById(rowkey);

			String s_sourceType = solrIndex.getS_sourceType();

			hbaseConn = HBaseConnector.getInstance().getConnection();

			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			Result result = getResultByRowKey(htab, rowkey, null);

			if (result.isEmpty()) {
				
				throw new Exception("测线打断出错，找不到数据：rowkey:"+rowkey);
			}

			// 0.copy一个新的tips,rowkey重新申请

			String newRowkey = TipsUtils.getNewRowkey(s_sourceType);

			Put newPut = copyNewATips(result, newRowkey);

			Put put = new Put(rowkey.getBytes());

			TipsDao newSolrIndex = solrIndex.copy();

			newSolrIndex.setId(newRowkey);

			// 1.cut line

			Point point = (Point) GeoTranslator.geojson2Jts(pointGeo);

			JSONObject oldGeo = JSONObject.fromObject(solrIndex
					.getG_location());


			List<JSONObject> cutGeoResult = cutLineByPoint(point, oldGeo);

			JSONObject geo1 = new JSONObject();

			JSONObject geo2 = new JSONObject();

			JSONObject g_location1 = cutGeoResult.get(0);

			JSONObject g_location2 = cutGeoResult.get(1);

			// JSONObject
			
			//
		/*	1.测线 deep.geo=?:如果只有两个形状点，则在线段的中央，如果多于两个，取第二个形状点坐标赋值
			2.测线：geometry.g_location=打断后的location
			3.测线：geomtry.g_guide=? geomtry.g_guide=geo
			*/
			
			//更新geomtry
			JSONObject g_guide1 =null;
			
			JSONObject g_guide2 =null;
			
			int pointSize = g_location1.getJSONArray("coordinates").size();
			
			int pointSize2=g_location2.getJSONArray("coordinates").size();

            System.out.println("****************************************pointSize   " + pointSize);
            if(pointSize <= 1) {
                throw new Exception("打断的点不能在link的端点");
            }else if(pointSize > 2){
				g_guide1 = getMidPointByGeometry(g_location1);
			}else{
				g_guide1 = getSencondPoint(g_location1);
			}
            System.out.println("****************************************pointSize2   " + pointSize2);
            if(pointSize2 <= 1) {
                throw new Exception("打断的点不能在link的端点");
            }else if(pointSize2 > 2){
				g_guide2 = getMidPointByGeometry(g_location2);
			}else{
				g_guide2 = getSencondPoint(g_location2);
			}

			geo1.put("g_location", g_location1);

			geo1.put("g_guide", g_guide1);

			geo2.put("g_location", g_location2);

			geo2.put("g_guide", g_guide2);

			solrIndex.setG_location(g_location1.toString());

			newSolrIndex.setG_location(g_location2.toString());

			solrIndex.setG_guide(g_guide1.toString());

			newSolrIndex.setG_guide(g_guide2.toString());
			
			
			// 更新wkt
			JSONObject feedbackObj = JSONObject.fromObject(solrIndex.getFeedback());

			put.addColumn("data".getBytes(), "geometry".getBytes(), geo1
					.toString().getBytes());

			newPut.addColumn("data".getBytes(), "geometry".getBytes(), geo2
					.toString().getBytes());

			// update deep (重新计算point)
			//更新deep.geo
			JSONObject deep1 = JSONObject.fromObject(solrIndex.getDeep());
			// 几何中心点
			deep1.put("geo",g_guide1);

			JSONObject deep2 = JSONObject.fromObject(solrIndex.getDeep());

			deep2.put("geo", g_guide2);
			
			
			//更新deep.len
			double len1 = GeometryUtils.getLinkLength(GeoTranslator.geojson2Jts(g_location1));
            double len2 = GeometryUtils.getLinkLength(GeoTranslator.geojson2Jts(g_location2));
			
			deep1.put("len", len1);
			
			deep2.put("len", len2);

            //更新新deep.id
            // ROWKEY 维护7种要素id 测线在内
            deep2.put("id", newRowkey.substring(6, newRowkey.length()));
			
			solrIndex.setDeep(deep1.toString());

			newSolrIndex.setDeep(deep2.toString());

			put.addColumn("data".getBytes(), "deep".getBytes(), deep1.toString()
					.getBytes());

			newPut.addColumn("data".getBytes(), "deep".getBytes(), deep2.toString()
					.getBytes());

            solrIndex.setWkt(TipsImportUtils.generateSolrStatisticsWkt(
                    "2001", deep1, g_location1,
                    feedbackObj));

            //这个主要是g_location:目前只用于tips的下载和渲染
            solrIndex.setWktLocation(TipsImportUtils.generateSolrWkt("2001", deep1, g_location1,
                    feedbackObj));

            newSolrIndex.setWkt(TipsImportUtils.generateSolrStatisticsWkt(
                    "2001", deep2, g_location2,
                    feedbackObj));

            //这个主要是g_location:目前只用于tips的下载和渲染
            newSolrIndex.setWktLocation(TipsImportUtils.generateSolrWkt(
                    "2001", deep2, g_location2,
                    feedbackObj));

			// 2.update track
			JSONObject trackJson = JSONObject.fromObject(new String(result
					.getValue("data".getBytes(), "track".getBytes())));
            TipsTrack track = (TipsTrack)JSONObject.toBean(trackJson, TipsTrack.class);
            if(track.getT_lifecycle() == TIP_LIFECYCLE_INIT) {
                track.setT_lifecycle(TIP_LIFECYCLE_UPDATE);
            }
            track = this.tipSaveUpdateTrack(track, track.getT_lifecycle());

//			track = addTrackInfo(user, track, date);
            JSONObject trackJson2 = JSONObject.fromObject(track);
			JSONObject newTrack = JSONObject.fromObject(track);

			put.addColumn("data".getBytes(), "track".getBytes(), trackJson2
					.toString().getBytes());

			newPut.addColumn("data".getBytes(), "track".getBytes(), newTrack
					.toString().getBytes());

			// update solr
			solrIndex.setHandler(user);
            newSolrIndex.setHandler(user);

            solrIndex = this.tipSaveUpdateTrackSolr(track, solrIndex);
            newSolrIndex = this.tipSaveUpdateTrackSolr(track, newSolrIndex);

            operator.updateOne(solrIndex);
            operator.updateOne(newSolrIndex);

			htab.put(put);

			htab.put(newPut);

			htab.close();

			resultArr.add(solrIndex);
			
			resultArr.add(newSolrIndex);

		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(tipsConn);

			e.printStackTrace();

			logger.error("测线打断出错,rowkey:" + rowkey + "原因：" + e.getMessage());

			throw new Exception("打断出错,rowkey:" + rowkey + "原因："
					+ e.getMessage(), e);
		}
		
		return resultArr;
		
	}

	/**
	 * @Description:获取g_location1中的第二个形状点坐标
	 * @param g_location1
	 * @return
	 * @author: y
	 * @time:2017-4-17 下午4:27:51
	 */
	private JSONObject getSencondPoint(JSONObject g_location1) {
		JSONObject g_guide1;
		Geometry geo=GeoTranslator.geojson2Jts(g_location1);
		
		Coordinate[] cs = geo.getCoordinates();

		double x  = cs[1].x;
		double y  =cs[1].y;

		Geometry pointGeo = GeoTranslator.point2Jts(x, y);
		
		g_guide1= GeoTranslator.jts2Geojson(pointGeo);
		return g_guide1;
	}

	/**
	 * @Description:根据tips类型，修改tips的关联测线
	 * @param json：需要被维护的tips solr
	 * @author: y
	 * @param tipsConn 
	 * @param oldRowkey :打断前测线的rowkey
	 * @param resultArr :打断后的Links
	 * @param dbId 
	 * @return 
	 * @throws Exception 
	 * @time:2017-4-12 下午8:37:30
	 */
	private TipsDao updateRelateMeasuringLine(java.sql.Connection tipsConn, String oldRowkey, TipsDao json, List<TipsDao> resultArr, int dbId) throws Exception {
		TipsRelateLineUpdate relateLineUpdate = new TipsRelateLineUpdate(oldRowkey,json,
				resultArr,dbId,tipsConn);
		return relateLineUpdate.excute();
	}

	/**
	 * @Description:情报预处理tips提交（按照任务提交）
	 * @param user
	 * @author: y
	 * @param taskId
	 * @throws Exception
	 * @time:2017-4-14 下午2:42:25
	 */
	public void submitInfoJobTips2Web(int user, int taskId)
			throws Exception {

		Connection hbaseConn = null;
        Table htab = null;
        java.sql.Connection tipsConn=null;

		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();
			tipsConn=DBConnector.getInstance().getTipsIdxConnection();

            htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

			TipsSelector selector = new TipsSelector();
			
			int taskType=getTaskType(taskId);
			
			
			if(taskType == TaskType.Q_TASK_TYPE){
				taskType=TaskType.Q_SUB_TASK_TYPE;
			}
			
			else if(taskType == TaskType.M_TASK_TYPE){
				taskType=TaskType.M_SUB_TASK_TYPE;
			}

			else {
				throw new Exception("不支持的任务类型：" + taskType);
			}

            //20170711情报矢量化提交Tips筛选条件按照subtaskid + t_tipstatus
			List<TipsDao> sdList = selector.getTipsByTaskIdAndStatus(tipsConn,taskId,
					taskType);
            long totalNum = sdList.size();
            if(totalNum > Integer.MAX_VALUE || totalNum == 0) {
                return;
            }
            List<Put> puts = new ArrayList<Put>();
            List<TipsDao> solrIndexList = new ArrayList<>();
            TipsIndexOracleOperator operator=new TipsIndexOracleOperator(tipsConn);
            for (int i = 0; i < totalNum; i++) {
                TipsDao doc = sdList.get(i);
                JSONObject snapshot = TipsUtils.tipsFromJSONObject(doc);
                String rowkey = snapshot.getString("id");
                Result result = htab.get(new Get(rowkey.getBytes()));
                if(result.isEmpty()) {
                    continue;
                }
                JSONObject trackJson = JSONObject.fromObject(new String(result.getValue(
                        "data".getBytes(), "track".getBytes())));
                TipsTrack track = (TipsTrack)JSONObject.toBean(trackJson, TipsTrack.class);
                track = this.tipSubmitTrack(track, user, PretreatmentTipsOperator.INFO_TIPS_STAGE);

				// put
				Put put = new Put(rowkey.getBytes());

				put.addColumn("data".getBytes(), "track".getBytes(), JSONObject.fromObject(track)
						.toString().getBytes());

				puts.add(put);

                //更新solr
                TipsDao solrIndex = operator.getById(rowkey);
                solrIndex = this.tipSubmitTrackOracle(track, solrIndex);
                solrIndexList.add(solrIndex);
			}

			htab.put(puts);
			operator.update(solrIndexList);
		} catch (Exception e) {
            DbUtils.rollbackAndCloseQuietly(tipsConn);
			throw new Exception("情报任务提交失败：" + e.getMessage(), e);
		}finally {
            if(htab != null) {
                htab.close();
            }
            DbUtils.commitAndCloseQuietly(tipsConn);
        }

	}

	/**
	 * 根据任务号 获取任务类型
	 * @param taskId
	 * @return
	 * @throws Exception 
	 */
	private int getTaskType(int taskId) throws Exception {
		// 调用 manapi 获取 任务类型、及任务号
		int taskType = 0;
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		try {
			Map<String, Integer> taskMap = manApi.getTaskBySubtaskId(taskId);
			if (taskMap != null) {
				// 1，中线 4，快线
				taskType = taskMap.get("programType");

			}else{
				throw new Exception("根据子任务号，没查到对应的任务号，sutaskid:"+taskId);
			}
		}catch (Exception e) {
			logger.error("根据子任务号，获取任务任务号及任务类型出错：" + e.getMessage(), e);
			throw e;
		}
		return taskType;
	}

    /**
     * 根据任务号 获取任务类型
     * @param taskId
     * @return
     * @throws Exception
     */
    private Map<String, Integer> getTaskInfoMap(int taskId) throws Exception {
        // 调用 manapi 获取 任务类型、及任务号
        int taskType = 0;
        ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
        try {
            Map<String, Integer> taskMap = manApi.getTaskBySubtaskId(taskId);
            if (taskMap != null) {
                // 1，中线 4，快线
                return taskMap;
            }else{
                throw new Exception("根据子任务号，没查到对应的任务号，sutaskid:"+taskId);
            }
        }catch (Exception e) {
            logger.error("根据子任务号，获取任务任务号及任务类型出错：" + e.getMessage(), e);
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
        PretreatmentTipsOperator operator = new PretreatmentTipsOperator();
//        String location = "{\"type\":\"LineString\",\"coordinates\":[[116.33056,39.87161],[116.33129,39.87157]]}";
//        JSONObject oldGeo = JSONObject.fromObject(location);
//        String pointStr ="{\"type\":\"Point\",\"coordinates\":[116.33129163010436,39.87157335577523]}";
//        JSONObject pointJson = JSONObject.fromObject(pointStr);
//        Point point = (Point) GeoTranslator.geojson2Jts(pointJson);
//        List<JSONObject> cutGeoResult = operator.cutLineByPoint(point, oldGeo);
//        JSONObject geo1 = new JSONObject();
//
//        JSONObject geo2 = new JSONObject();
//
//        JSONObject g_location1 = cutGeoResult.get(0);
//
//        JSONObject g_location2 = cutGeoResult.get(1);
//        System.out.println(g_location2);
		operator.submitInfoJobTips2Web( 1664,57);
    }
}
