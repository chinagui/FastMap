package com.navinfo.dataservice.engine.script.tmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.dataservice.engine.fcc.tips.TipsLineRelateQuery;
import com.navinfo.dataservice.engine.fcc.tips.TipsStatConstant;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @ClassName: TipsIndexCreateScript.java
 * @author y
 * @date 2017-8-28 下午1:50:13
 * @Description:因tips规格升级的时候，新增tips规格中未配置关联link的索引，导致部分tips tips_link及tips_nodes表为生成
 * 新增tips类型：519  2202  2203  2204  1212  1213  1117  1214 1520  2101
 * 2202没有关联link不需要批
 * 批处理tips类型1519  2203  2204  1212  1213  1117  1214 1520  2101
 * 数据并不多，所以用这种方式
 */
public class TipsLinkCreateScript {

	private static final Logger log = Logger.getLogger(TipsLinkCreateScript.class);

	protected static int total = 0;
	protected static VMThreadPoolExecutor poolExecutor;

	private static String tableName=HBaseConstant.tipTab;
	private static TipsIndexOracleOperator op;

	public static void doSync() throws Exception {
		java.sql.Connection conn = null;
		Table htab = null;
		try {
			conn = DBConnector.getInstance().getTipsIdxConnection();
			htab = HBaseConnector.getInstance().getConnection()
					.getTable(TableName.valueOf(tableName));

			op = new TipsIndexOracleOperator(conn);
			
			String sql="SELECT * FROM tips_index i WHERE i.s_sourcetype IN(1519,220,2204, 1212,1213,1117,1214,1520,2101) ";
			
			List<TipsDao> daoList=op.queryWithOutHbase(sql,null);
			
			log.debug("haha .....................");
			
			if(daoList==null||daoList.size()==0){
				return ;
			}
			
			log.debug("query tips to batch count:"+daoList.size());
			
			List<Get> getList=new ArrayList<Get>();
			for (TipsDao tipsDao : daoList) {
				Get get=new Get(tipsDao.getId().getBytes());
				getList.add(get);
			}
			Result[] results = htab.get(getList);
			int num = 0;
			String rowkey="";
			//新的索引
			List<TipsDao> tis = new ArrayList<TipsDao>();

			for (Result result : results) {
				try {
					// oracle已有，则不添加
					rowkey=	new String(result.getRow());
					log.debug("rowkey:"+rowkey);
					tis.add(convert(result));
					num++;
					if (num % 1000 == 0) {
						op.update(tis);
						conn.commit();
						tis.clear();
						log.info("num:" + num);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}

			op.update(tis);
			conn.commit();
			tis.clear();
			setTotal( num);
			
			htab.close();
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	private static TipsDao convert(Result result) throws Exception {
		try {
			if (result == null) {
				log.info("result is null");
				return null;
			}
			TipsDao ti = new TipsDao();

			// rowkey
			ti.setId(Bytes.toString(result.getRow()));

			// 显示坐标
			String geoString = Bytes.toString(result.getValue(
					Bytes.toBytes("data"), Bytes.toBytes("geometry")));
			JSONObject geoJSON = JSONObject.fromObject(geoString);
			JSONObject locationJSON = geoJSON.getJSONObject("g_location");
			Geometry locationGeo = GeoTranslator.geojson2Jts(locationJSON);

			ti.setWktLocation(locationGeo);

			// track
			String trackString = Bytes.toString(result.getValue(
					Bytes.toBytes("data"), Bytes.toBytes("track")));
			JSONObject trackJSON = JSONObject.fromObject(trackString);
			String tDate = trackJSON.getString("t_date");
			int lifecycle = trackJSON.getInt("t_lifecycle");
			// int command = trackJSON.getInt("t_command");
			int dEditMeth = trackJSON.getInt("t_dEditMeth");
			int dEditStatus = trackJSON.getInt("t_dEditStatus");
			int mEditMeth = trackJSON.getInt("t_mEditMeth");
			int mEditStatus = trackJSON.getInt("t_mEditStatus");
			int tipStatus = trackJSON.getInt("t_tipStatus");
			ti.setT_date(tDate);
			ti.setT_lifecycle(lifecycle);
			// ti.settCommand(command);
			ti.setT_dEditMeth(dEditMeth);
			ti.setT_dEditStatus(dEditStatus);
			ti.setT_mEditMeth(mEditMeth);
			ti.setT_mEditStatus(mEditStatus);
			ti.setT_tipStatus(tipStatus);

			// track履历
			JSONArray trackInfoArray = trackJSON.getJSONArray("t_trackInfo");
			int stage = 0;
			String tOperateDate = tDate;
			int handler = 0;
			if (trackInfoArray != null && trackInfoArray.size() > 0) {
				JSONObject lastTrack = trackInfoArray
						.getJSONObject(trackInfoArray.size() - 1);
				stage = lastTrack.getInt("stage");
				if (lastTrack.containsKey("date")) {
					tOperateDate = lastTrack.getString("date");
				}
				if (lastTrack.containsKey("handler")) {
					handler = lastTrack.getInt("handler");
				}
			}
			ti.setT_operateDate(tOperateDate);
			ti.setStage(stage);
			ti.setHandler(handler);
			
			if(trackJSON.containsKey("t_dataDate")){
				ti.setT_dataDate(trackJSON.getString("t_dataDate"));
			}

			// source
			String sourceString = Bytes.toString(result.getValue(
					Bytes.toBytes("data"), Bytes.toBytes("source")));
			JSONObject sourceJSON = JSONObject.fromObject(sourceString);
			String sourceType = sourceJSON.getString("s_sourceType");
			int qTaskId = sourceJSON.getInt("s_qTaskId");
			int mTaskId = sourceJSON.getInt("s_mTaskId");
			int qSubTaskId = sourceJSON.getInt("s_qSubTaskId");
			int mSubTaskId = sourceJSON.getInt("s_mSubTaskId");

			ti.setS_sourceType(sourceType);
			ti.setS_qTaskId(qTaskId);
			ti.setS_mTaskId(mTaskId);
			ti.setS_qSubTaskId(qSubTaskId);
			ti.setS_mSubTaskId(mSubTaskId);

			// 统计坐标
			Geometry wkt = locationGeo;
			// String wkt = locationWkt;
			String deepString = Bytes.toString(result.getValue(
					Bytes.toBytes("data"), Bytes.toBytes("deep")));

			JSONObject deepJSON = JSONObject.fromObject(deepString);
			if (TipsStatConstant.gSLocTipsType.contains(sourceType)) {
				JSONObject gSLocJSON = deepJSON.getJSONObject("gSLoc");
				wkt = GeoTranslator.geojson2Jts(gSLocJSON);
				// wkt = GeoTranslator.jts2Wkt(gSLocGeo);
			} else if (TipsStatConstant.gGeoTipsType.contains(sourceType)) {
				JSONObject ggeoJSON = deepJSON.getJSONObject("geo");
				wkt = GeoTranslator.geojson2Jts(ggeoJSON);
				// wkt = GeoTranslator.jts2Wkt(ggeoGeo);
			}
			ti.setWkt(wkt);

			Map<String, String> relateMap = TipsLineRelateQuery.getRelateLine(
					sourceType, deepJSON);

			ti.setRelate_links(relateMap.get("relate_links"));
			ti.setRelate_nodes(relateMap.get("relate_nodes"));


			return ti;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

	protected static synchronized void setTotal( int num) {
		total += num;
		log.info("total:" + total);
	}

	public static void sync() throws Exception {
		long t=System.currentTimeMillis();
		doSync();
		log.debug("全部线程完成,用时：" + (System.currentTimeMillis() - t) + "ms");
	}

	
	public static void initContext(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "dubbo-app-scripts.xml","dubbo-scripts.xml" }); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			log.info("......................start batch tips_link......................");
			initContext();
			sync();
			log.info("......................all tips create index Over......................");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			log.info("......................all tips create index Over......................");
			System.exit(0);
		}

	}

}
