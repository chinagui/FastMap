package com.navinfo.dataservice.engine.fcc.tips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.undo.CannotUndoException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * 预处理tips操作类
 * 
 * @ClassName: PretreatmentTipsOperator.java
 * @author y
 * @date 2016-11-17 下午8:13:38
 * @Description: TODO
 * 
 */
public class PretreatmentTipsOperator extends BaseTipsOperate{

	static String FC_SOURCE_TYPE = "8001"; // FC预处理理tips

	static int FC_DEFAULT_STAGE = 2;

	private static final Logger logger = Logger
			.getLogger(PretreatmentTipsOperator.class);

	public PretreatmentTipsOperator() {

	}

	/**
	 * @Description:创建一个tips
	 * @param sourceType
	 * @param g_location
	 * @param g_guide
	 * @param user
	 *            :feedback.content
	 * @param user
	 * @author: y
	 * @param deep
	 * @param memo
	 * @param type
	 * @throws Exception
	 * @time:2016-11-15 上午11:03:20
	 */
	public void create(String sourceType, JSONObject lineGeometry, int user,
			JSONObject deep, String memo) throws Exception {

		Connection hbaseConn;
		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();
			Table htab = hbaseConn.getTable(TableName
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

			// 3.track
			int stage = 5;  //0 初始化；1 外业采集；2 内业日编；3 内业月编；4 GDB增量； 5内业预处理  

			int t_lifecycle = 3;
			int t_command = 0;
			int t_cStatus = 0;
			int t_dStatus = 0;
			int t_mStatus = 0;
			//int t_inStatus = 0;
			int t_inMeth = 0;
			int t_pStatus = 0;
			int t_dInProc = 0;
			int t_mInProc = 0;  

			JSONObject jsonTrack = TipsUtils.generateTrackJson(t_lifecycle,stage,
					user, t_command, null, currentDate,currentDate, t_cStatus, t_dStatus,
					t_mStatus, t_inMeth,t_pStatus,t_dInProc,t_mInProc);

			// source
			int s_sourceCode = 14;
			int s_reliability = 100;
			int s_featureKind = 2;

			JSONObject source = new JSONObject();
			source = TipsUtils.newSource(s_featureKind, null, s_sourceCode,
					null, sourceType, s_reliability, 0);

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
			JSONObject g_guide = deepNew.getJSONObject("geo") ;
			jsonGeom.put("g_location", lineGeometry);
			jsonGeom.put("g_guide", g_guide);

			// put
			Put put = new Put(rowkey.getBytes());

			put.addColumn("data".getBytes(), "track".getBytes(), jsonTrack
					.toString().getBytes());

			put.addColumn("data".getBytes(), "geometry".getBytes(), jsonGeom
					.toString().getBytes());

			put.addColumn("data".getBytes(), "feedback".getBytes(), feedbackObj
					.toString().getBytes());

			put.addColumn("data".getBytes(), "source".getBytes(), source
					.toString().getBytes());

			put.addColumn("data".getBytes(), "deep".getBytes(), deepNew
					.toString().getBytes());

			// solr index json
			JSONObject solrIndex = TipsUtils.generateSolrIndex(rowkey, stage,
					currentDate, currentDate, t_lifecycle, t_command, user,
					t_cStatus, t_dStatus, t_mStatus, sourceType, s_sourceCode,
					g_guide, lineGeometry, deepNew, feedbackObj, s_reliability,t_inMeth,t_pStatus,t_dInProc,t_mInProc);

			solr.addTips(solrIndex);

			List<Put> puts = new ArrayList<Put>();

			puts.add(put);

			htab.put(puts);

			htab.close();

		} catch (IOException e) {
			logger.error("新增tips出错：原因：" + e.getMessage());
			throw new Exception("新增tips出错：原因：" + e.getMessage(), e);
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

		Connection hbaseConn;
		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();

			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			String[] queryColNames = { "track", "deep", "geometry" };

			Result result = getResultByRowKey(htab, rowkey, queryColNames);

			if (result.isEmpty()) {
				return false;
			}

			Put put = new Put(rowkey.getBytes());

			// 1.update track

			JSONObject track = JSONObject.fromObject(new String(result
					.getValue("data".getBytes(), "track".getBytes())));
			
			String date = DateUtils.dateToString(new Date(), DateUtils.DATE_COMPACTED_FORMAT);

			track = addTrackInfo(user, track,date);
			
			track.put("t_lifecycle", 2);

			// 2.update geometry

			JSONObject geometry = JSONObject.fromObject(new String(result
					.getValue("data".getBytes(), "geometry".getBytes())));

			geometry.put("g_location", lineGeometry);
			
			JSONObject  guideNew=getMidPointByGeometry(lineGeometry);
			
			geometry.put("g_guide", guideNew);

			// 2.update deep.geo(根据新的几何信息计算几何中心点)

			JSONObject deep = JSONObject.fromObject(new String(result.getValue(
					"data".getBytes(), "deep".getBytes())));

			deep.put("geo", guideNew);
			
			put.addColumn("data".getBytes(), "track".getBytes(), track
					.toString().getBytes());

			put.addColumn("data".getBytes(), "geometry".getBytes(), geometry
					.toString().getBytes());

			put.addColumn("data".getBytes(), "deep".getBytes(), deep.toString()
					.getBytes());

			// update solr

			JSONObject solrIndex = solr.getById(rowkey);
			
			solrIndex.put("t_lifecycle", 2);

			solrIndex.put("t_date", date);

			solrIndex.put("handler", user);

			solrIndex.put("g_location", lineGeometry);
			
			solrIndex.put("g_guide", guideNew);

			solrIndex.put("deep", deep);
			
			JSONObject feedbackObj=JSONObject.fromObject(solrIndex.get("feedback"));
			
			solrIndex.put("wkt", TipsImportUtils.generateSolrWkt(
					String.valueOf(FC_SOURCE_TYPE), deep, lineGeometry, feedbackObj));

			solr.addTips(solrIndex);

			htab.put(put);
			
			htab.close();

			return false;

		} catch (IOException e) {

			logger.error("tips修形出错,rowkey:" + rowkey + "原因：" + e.getMessage());

			throw new Exception("tips修形出错,rowkey:" + rowkey + "原因："
					+ e.getMessage(), e);
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
			
			String date = DateUtils.dateToString(new Date(), DateUtils.DATE_COMPACTED_FORMAT);

			track = addTrackInfo(user, track,date);

			// 2.update deep.geo(用户挪动后的点)

			JSONObject deep = JSONObject.fromObject(new String(result.getValue(
					"data".getBytes(), "deep".getBytes())));

			deep.put("geo", tipGeometry);

			put.addColumn("data".getBytes(), "track".getBytes(), track
					.toString().getBytes());

			put.addColumn("data".getBytes(), "deep".getBytes(), deep.toString()
					.getBytes());

			// update solr

			JSONObject solrIndex = solr.getById(rowkey);

			solrIndex.put("t_date", date);

			solrIndex.put("handler", user);

			solrIndex.put("deep", deep);

			solr.addTips(solrIndex);

			htab.put(put);
			
			htab.close();

			return false;

		} catch (IOException e) {

			logger.error("tips点移动出错,rowkey:" + rowkey + "原因：" + e.getMessage());

			throw new Exception("tips点移动出错,rowkey:" + rowkey + "原因："
					+ e.getMessage(), e);
		}
	}

	/**
	 * @Description:打断（线几何）
	 * @param rowkey
	 * @param tipGeometry
	 * @param user
	 * @author: y
	 * @throws Exception
	 * @time:2016-11-18 下午2:16:09
	 */
	public boolean breakLine(String rowkey, JSONObject tipGeometry, int user)
			throws Exception {

		Connection hbaseConn;
		try {
			
			JSONObject solrIndex = solr.getById(rowkey);
			
			String s_sourceType=solrIndex.getString("s_sourceType");
			
			hbaseConn = HBaseConnector.getInstance().getConnection();

			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));

			Result result = getResultByRowKey(htab, rowkey, null);

			if (result.isEmpty()) {
				return false;
			}

			// 0.copy一个新的tips,rowkey重新申请
			
			String newRowkey=TipsUtils.getNewRowkey(s_sourceType);
			
			Put newPut = copyNewATips(result,newRowkey);
			
			Put put = new Put(rowkey.getBytes());
			
			JSONObject newSolrIndex=JSONObject.fromObject(solrIndex);
			
			newSolrIndex.put("id", newRowkey);
			
			
			//1.cut line
			
			Point point=(Point)GeoTranslator.geojson2Jts(tipGeometry);
			
			JSONObject oldGeo=JSONObject.fromObject(solrIndex.get("g_location")) ;
			
			List<JSONObject> cutGeoResult=cutLineByPoint(point,oldGeo);
			
			
			JSONObject  geo1=new JSONObject();
			
			JSONObject  geo2=new JSONObject();
			
			
			JSONObject g_location1=cutGeoResult.get(0);
			
			JSONObject g_location2=cutGeoResult.get(1);
			
			
			//JSONObject g_guide=JSONObject.fromObject(solrIndex.get("g_guide"));
			
			JSONObject  g_guide1=getMidPointByGeometry(g_location1);
			
			JSONObject  g_guide2=getMidPointByGeometry(g_location2);
			
			geo1.put("g_location", g_location1);
			
			geo1.put("g_guide",g_guide1);
			
			geo2.put("g_location", g_location2);
			
			geo2.put("g_guide", g_guide2);
			
			solrIndex.put("g_location", g_location1);

			newSolrIndex.put("g_location", g_location2);
			
			solrIndex.put("g_guide", g_guide1);

			newSolrIndex.put("g_guide", g_guide2);
			
			//旧的feedback两个都是一样的，取一个就好了
			JSONObject feedbackObj=JSONObject.fromObject(solrIndex.get("feedback"));
			
			solrIndex.put("wkt", TipsImportUtils.generateSolrWkt(
					String.valueOf(FC_SOURCE_TYPE), null, g_location1, feedbackObj));
			
			newSolrIndex.put("wkt", TipsImportUtils.generateSolrWkt(
					String.valueOf(FC_SOURCE_TYPE), null, g_location2, feedbackObj));
			
			
			put.addColumn("data".getBytes(), "geometry".getBytes(), geo1
					.toString().getBytes());
			
			newPut.addColumn("data".getBytes(), "geometry".getBytes(), geo2
					.toString().getBytes());
			
			
			//update deep (重新计算point)
			//如果是FC预处理的tips需求更新deep.geo
			if(FC_SOURCE_TYPE.equals(solrIndex.getString("s_sourceType"))){
				
				updateFcTipDeep(solrIndex, newPut, put, newSolrIndex,
						g_guide1, g_guide2);
				
			}
			
			// 2.update track
			
			JSONObject track = JSONObject.fromObject(new String(result
					.getValue("data".getBytes(), "track".getBytes())));
			
			String date = DateUtils.dateToString(new Date(), DateUtils.DATE_COMPACTED_FORMAT);

			track = addTrackInfo(user, track,date);
			
			JSONObject newTrack=JSONObject.fromObject(track);
			
			put.addColumn("data".getBytes(), "track".getBytes(), track
					.toString().getBytes());
			
			newPut.addColumn("data".getBytes(), "track".getBytes(), newTrack
					.toString().getBytes());
			
			// update solr

			solrIndex.put("t_date", date);

			solrIndex.put("handler", user);

			solr.addTips(solrIndex);
			
			solr.addTips(newSolrIndex);

			htab.put(put);
			
			htab.put(newPut);
			
			htab.close();

			return false;

		} catch (Exception e) {
			
			e.printStackTrace();

			logger.error("打断出错,rowkey:" + rowkey + "原因：" + e.getMessage());

			throw new Exception("打断出错,rowkey:" + rowkey + "原因："
					+ e.getMessage(), e);
		}
	}

	/**
	 * @Description:TOOD
	 * @param user
	 * @param track
	 * @return
	 * @author: y
	 * @time:2016-11-18 下午8:03:59
	 */
	private JSONObject addTrackInfo(int user, JSONObject track,String date) {
		JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");

		// 1.1 new trackInfo
		JSONObject jsonTrackInfo = TipsUtils.newTrackInfo(FC_DEFAULT_STAGE,
				date, user);

		trackInfoArr.add(jsonTrackInfo);

		track.put("t_trackInfo", trackInfoArr);

		track.put("t_date", date);
		
		return track;
	}

	/**
	 * @Description:TOOD
	 * @param solrIndex
	 * @param newPut
	 * @param put
	 * @param newSolrIndex
	 * @param g_location1
	 * @param g_location2
	 * @throws Exception
	 * @author: y
	 * @time:2016-11-18 下午7:58:46
	 */
	private void updateFcTipDeep(JSONObject solrIndex, Put newPut, Put put,
			JSONObject newSolrIndex, JSONObject g_guide1,
			JSONObject g_guide2) throws Exception {
		
		
		JSONObject deep1=JSONObject.fromObject(solrIndex.get("deep"));;
		
		// 几何中心点
		JSONObject pointGeo1 = g_guide1;


		deep1.put("geo", pointGeo1);
		
		
		JSONObject deep2=JSONObject.fromObject(solrIndex.get("deep"));;
		
		// 几何中心点
		JSONObject  pointGeo2= g_guide2;


		deep2.put("geo", pointGeo2);
		
		put.addColumn("data".getBytes(), "deep".getBytes(), deep1.toString()
				.getBytes());
		
		newPut.addColumn("data".getBytes(), "deep".getBytes(), deep2.toString()
				.getBytes());
		
		solrIndex.put("deep", deep1);
		
		newSolrIndex.put("deep", deep2);
		
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
	private Put copyNewATips(Result result,String rowkey) {

		Put put=new Put(rowkey.getBytes());
		
		if (result != null) {
			List<Cell> ceList = result.listCells();
			if (ceList != null && ceList.size() > 0) {
				for (Cell cell : ceList) {
					String value = Bytes.toString(cell.getValueArray(),
							cell.getValueOffset(), cell.getValueLength());
					String colName = Bytes.toString(cell.getQualifierArray(),
							cell.getQualifierOffset(),
							cell.getQualifierLength());
					put.addColumn("data".getBytes(), colName.getBytes(), value.toString()
							.getBytes());
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
	public void submit2Web(int user) throws Exception {
		
		
		try{
			
			//String wkt = GridUtils.grids2Wkt(grids);
			
			//List<JSONObject> snapshots=solr.queryHasNotSubmitPreTipsByWktAndUser(wkt,user);
			
			List<JSONObject> snapshots=solr.queryHasNotSubmitPreTipsByWktAndUser(user);
			
			String currentDate=StringUtils.getCurrentTime();
			
			List<Get> gets = new ArrayList<Get>();
			
			for (JSONObject solrIndex : snapshots) {
				
				String rowkey=solrIndex.getString("id");
				
				Get get = new Get(rowkey.getBytes());

				get.addColumn("data".getBytes(), "track".getBytes());

				get.addColumn("data".getBytes(), "feedback".getBytes());

				gets.add(get);
				
				solrIndex.put("t_pStatus", 1);  //更新t_pStatus=1 
				
				solrIndex.put("t_date", currentDate);  //更新t_date
				
				solrIndex.put("t_lifecycle", 2);
				
				
				solr.addTips(solrIndex);
				
			}
			Connection hbaseConn = HBaseConnector.getInstance().getConnection();
			
			Table htab = hbaseConn
					.getTable(TableName.valueOf(HBaseConstant.tipTab));
			
			Map<String, JSONObject> tipsTracks=loadTipsTrack(htab,gets);
			
			
			Set<String> keys=tipsTracks.keySet(); 
			
			for (String rowkey : keys) {
				
				// 1.更新feddback和track
				JSONObject track = tipsTracks.get(rowkey).getJSONObject("track");

				JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");

				// 新增一个trackInfo
				JSONObject jsonTrackInfo = new JSONObject();

				jsonTrackInfo.put("date", currentDate);

				jsonTrackInfo.put("handler", user);
				
				jsonTrackInfo.put("stage", 5);

				trackInfoArr.add(jsonTrackInfo);

				track.put("t_trackInfo", trackInfoArr);
				
				track.put("t_lifecycle", 2);
				
				track.put("t_pStatus", 1); //已提交
				
				track.put("t_date", currentDate );
				
				//更新hbase
				
				Put put = new Put(rowkey.getBytes());
				
				put.addColumn("data".getBytes(), "track".getBytes(), track.toString()
						.getBytes());

				htab.put(put);
				
			}

		} catch (IOException e) {
			
			logger.error(e.getMessage(), e);
			
			throw new Exception("tips提交出错，原因：" + e.getMessage(), e);
		}
	}
	
	
	
	
	/**
	 * 从Hbase读取Tips信息（只有track）
	 * @param htab
	 * @param gets
	 * @throws Exception
	 */
	private Map<String, JSONObject> loadTipsTrack(Table htab, List<Get> gets) throws Exception {
		
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

				jo.put("track",track);

		/*		if (result.containsColumn("data".getBytes(),
						"feedback".getBytes())) {
					JSONObject feedback = JSONObject.fromObject(new String(
							result.getValue("data".getBytes(),
									"feedback".getBytes())));

					jo.put("feedback", feedback);
				} else {
					jo.put("feedback", TipsUtils.OBJECT_NULL_DEFAULT_VALUE);
				}*/

				tips.put(rowkey, jo);
			} catch (Exception e) {
				logger.error(e.getMessage(),e.getCause());
				throw e;
			}
		}
		return tips;
	}

	
	
	/**
	 * @Description:给定一个点，打断线几何
	 * @param point 
	 * @param geojson 线几何
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2016-11-18 下午7:20:47
	 */
	private  List<JSONObject> cutLineByPoint(Point point,JSONObject  geojson) throws Exception{
		
		
		List<JSONObject> resultGeoList = new ArrayList<JSONObject>();
		
		
		
		Geometry geo=GeoTranslator.geojson2Jts(geojson);
		
		//将坐标点扩大100000倍，（web给的坐标点，可能不在线上，有一定的误差）
		geo=GeoTranslator.transform(geo, 100000, 5);
		
		geojson=GeoTranslator.jts2Geojson(geo);
		
		double lon = point.getCoordinate().x * 100000;
		double lat = point.getCoordinate().y * 100000;
		
/*		double lon = point.getCoordinate().x ;
		double lat = point.getCoordinate().y ;*/
		JSONArray ja1 = new JSONArray();
		JSONArray ja2 = new JSONArray();
		JSONArray jaLink = geojson.getJSONArray("coordinates");
		boolean hasFound = false;// 打断的点是否和形状点重合或者是否在线段上
		for (int i = 0; i < jaLink.size() - 1; i++) {
			JSONArray jaPS = jaLink.getJSONArray(i);
			if (i == 0) {
				ja1.add(jaPS);
			}
			JSONArray jaPE = jaLink.getJSONArray(i + 1);
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
		
		//生成两个新的geo
		JSONObject sGeojson1 = new JSONObject();
		sGeojson1.put("type", "LineString");
		sGeojson1.put("coordinates", ja1);
		
		
		JSONObject sGeojson2 = new JSONObject();
		sGeojson2.put("type", "LineString");
		sGeojson2.put("coordinates", ja2);
		
		//缩小0.00001倍
		sGeojson1=GeoTranslator.jts2Geojson(GeoTranslator.transform(GeoTranslator.geojson2Jts(sGeojson1), 0.00001, 5));
		
		sGeojson2=GeoTranslator.jts2Geojson(GeoTranslator.transform(GeoTranslator.geojson2Jts(sGeojson2), 0.00001, 5));
		
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
	public void updateFeedbackMemoAndDeep(String rowkey, int user, String memo,JSONObject deepInfo) throws Exception {

		try {

			Connection hbaseConn = HBaseConnector.getInstance().getConnection();

			Table htab = hbaseConn.getTable(TableName
					.valueOf(HBaseConstant.tipTab));
			//获取solr数据
			JSONObject solrIndex = solr.getById(rowkey);
			
			String sourceType=solrIndex.getString("s_sourceType");
			
			int stage=2;
			//如果是预处理的tips则stage=5
			
			if(solrIndex.getString("s_sourceType").equals(PretreatmentTipsOperator.FC_SOURCE_TYPE)){
				stage=5; 
			}

			// 获取到改钱的 feddback和track
			JSONObject oldTip = getOldTips(rowkey, htab);

			// 1.更新feddback和track
			JSONObject track = oldTip.getJSONObject("track");

			JSONArray trackInfoArr = track.getJSONArray("t_trackInfo");

			String date = DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");

			// 新增一个trackInfo
			JSONObject jsonTrackInfo = new JSONObject();

			jsonTrackInfo.put("stage", stage);

			jsonTrackInfo.put("date", date);

			jsonTrackInfo.put("handler", user);

			trackInfoArr.add(jsonTrackInfo);

			track.put("t_trackInfo", trackInfoArr);
			
			track.put("t_lifecycle", 2);

			// 2.更新feedback

			// 新增一个f_array type=3的是文字
			JSONObject feedBack = oldTip.getJSONObject("feedback");

			JSONArray f_array = feedBack.getJSONArray("f_array");


			for (Object object : f_array) {

				JSONObject obj = JSONObject.fromObject(object);
				
				//先删掉

				if (obj.getInt("type") == 3) {
					
					f_array.remove(obj);

					break;
				}
			}
			// 如果count=0,则说明原来没有备注，则，增加一条

			int type = 3; // 文字

			JSONObject newFeedback = TipsUtils.newFeedback(user, memo, type,
					date);

			f_array.add(newFeedback);

			// 更新feedback
			feedBack.put("f_array", f_array);
			
			JSONObject newDeep=null;
			
			if( FC_SOURCE_TYPE.equals(sourceType)&& deepInfo!=null&&!deepInfo.isNullObject()) {
				
				JSONObject deep=oldTip.getJSONObject("deep");
				
				newDeep=deep;
				
				newDeep.put("fc", deepInfo.get("fc"));
			}

		    
		    
			Put put = new Put(rowkey.getBytes());

			put.addColumn("data".getBytes(), "track".getBytes(), track
					.toString().getBytes());

			put.addColumn("data".getBytes(), "feedback".getBytes(), feedBack
					.toString().getBytes());
			
			if(newDeep!=null){
				put.addColumn("data".getBytes(), "deep".getBytes(), newDeep
						.toString().getBytes());
			}

			// 同步更新solr

			solrIndex.put("stage", stage); 

			solrIndex.put("t_date", date);

			//
			solrIndex.put("t_lifecycle", 2);

			solrIndex.put("handler", user);

			solrIndex.put("feedback", feedBack);
			
			if(newDeep!=null){
				solrIndex.put("deep", newDeep);
			}

			solr.addTips(solrIndex);

			htab.put(put);
			
			htab.close();

		} catch (IOException e) {
			
			e.printStackTrace();
			
			logger.error(e.getMessage(), e);
			
			throw new Exception("改备注信息出错：rowkey:"+rowkey+"原因：" + e.getMessage(), e);
		}

	}
	
	

}
