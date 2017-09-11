package com.navinfo.dataservice.scripts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOperator;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.engine.fcc.tips.TipsLineRelateQuery;
import com.navinfo.navicommons.exception.ServiceRtException;
import com.navinfo.navicommons.exception.ThreadExecuteException;
import com.navinfo.nirobot.common.constant.TipsStatConstant;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: SyncTips2Solr
 * @author xiaoxiaowen4127
 * @date 2017年7月12日
 * @Description: SyncTips2Solr.java
 */
public class SyncTips2Oracle {
	
	public static Logger log = Logger.getLogger(SyncTips2Oracle.class);
	
//	protected static Set<String> syncCols = new HashSet<String>();
	protected static int total=0;
	protected static VMThreadPoolExecutor poolExecutor;
	
	
	
	private String index;
	private String tableName;
	private OracleSchema schema;
	private TipsIndexOperator op;
	
	public SyncTips2Oracle(String tableName,String index,OracleSchema schema){
		this.tableName=tableName;
		this.index=index;
		this.schema=schema;
	}
	
	public void doSync()throws Exception{
		java.sql.Connection conn = null;
		Table htab = null;
		try{
			conn = schema.getPoolDataSource().getConnection();
			htab = HBaseConnector.getInstance().getConnection().getTable(TableName.valueOf(tableName));
			
			op = new TipsIndexOracleOperator(conn);
			Scan scan = new Scan();
			scan.addFamily("data".getBytes());
			scan.setCaching(5000);
			scan.setStartRow(index.getBytes());
			scan.setStopRow((index+"a").getBytes());
			
			ResultScanner rs = htab.getScanner(scan);
			
			Result[] results = null;
			int num=0;
			while ((results = rs.next(5000)).length > 0){
				List<TipsDao> tis = new ArrayList<TipsDao>();
				
				for (Result result : results){
					try{
						tis.add(convert(result));
						num++;
						if(num%1000==0){
							op.save(tis);
							conn.commit();
							tis.clear();
							log.info("index:"+index+",num:"+num);
						}
					}catch(Exception e){
						log.error(e.getMessage(),e);
					}
				}

				op.save(tis);
				conn.commit();
				tis.clear();
			}
			conn.commit();
			setTotal(index,num);
			htab.close();
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	private TipsDao convert(Result result)throws Exception{
		try{
			if(result==null){
				log.info("result is null");
				return null;
			}
			TipsDao ti = new TipsDao();
			
			//rowkey
			ti.setId(Bytes.toString(result.getRow()));
			
			//log.info("rowkey:"+Bytes.toString(result.getRow()));
			
			//显示坐标
			String geoString = Bytes.toString(result.getValue(Bytes.toBytes("data"), Bytes.toBytes("geometry")));
			JSONObject geoJSON = JSONObject.fromObject(geoString);
			JSONObject locationJSON = geoJSON.getJSONObject("g_location");
			Geometry locationGeo = GeoTranslator.geojson2Jts(locationJSON);
			
			ti.setWktLocation(locationGeo);
			
			//track
			String trackString = Bytes.toString(result.getValue(Bytes.toBytes("data"), Bytes.toBytes("track")));
			JSONObject trackJSON = JSONObject.fromObject(trackString);
			String tDate = trackJSON.getString("t_date");
			String tDataDate = trackJSON.getString("t_dataDate");
			int lifecycle = trackJSON.getInt("t_lifecycle");
//			int command = trackJSON.getInt("t_command");
			int dEditMeth = trackJSON.getInt("t_dEditMeth");
			int dEditStatus = trackJSON.getInt("t_dEditStatus");
			int mEditMeth = trackJSON.getInt("t_mEditMeth");
			int mEditStatus = trackJSON.getInt("t_mEditStatus");
			int tipStatus = trackJSON.getInt("t_tipStatus");
			ti.setT_date(tDate);
			ti.setT_dataDate(tDataDate);
			ti.setT_lifecycle(lifecycle);
//			ti.settCommand(command);
			ti.setT_dEditMeth(dEditMeth);
			ti.setT_dEditStatus(dEditStatus);
			ti.setT_mEditMeth(mEditMeth);
			ti.setT_mEditStatus(mEditStatus);
			ti.setT_tipStatus(tipStatus);
			
			//track履历
			JSONArray trackInfoArray = trackJSON.getJSONArray("t_trackInfo");
			int stage = 0;
			String tOperateDate = tDate;
			int handler = 0;
			if(trackInfoArray != null && trackInfoArray.size() > 0) {
				JSONObject lastTrack = trackInfoArray.getJSONObject(trackInfoArray.size() - 1);
				stage = lastTrack.getInt("stage");
				if(lastTrack.containsKey("date")){
					tOperateDate = lastTrack.getString("date");
				}
				if(lastTrack.containsKey("handler")){
					handler = lastTrack.getInt("handler");
				}
			}
			ti.setT_operateDate(tOperateDate);
			ti.setStage(stage);
			ti.setHandler(handler);
			
			//source
			String sourceString = Bytes.toString(result.getValue(Bytes.toBytes("data"), Bytes.toBytes("source")));
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
			
			//统计坐标
			Geometry wkt = locationGeo;
//			String wkt = locationWkt;
			String deepString = Bytes.toString(result.getValue(Bytes.toBytes("data"), Bytes.toBytes("deep")));

			JSONObject deepJSON  = JSONObject.fromObject(deepString);
			if(TipsStatConstant.gSLocTipsType.contains(sourceType)) {
				JSONObject gSLocJSON = deepJSON.getJSONObject("gSLoc");
				wkt = GeoTranslator.geojson2Jts(gSLocJSON);
//				wkt = GeoTranslator.jts2Wkt(gSLocGeo);
			}else if(TipsStatConstant.gGeoTipsType.contains(sourceType)) {
				JSONObject ggeoJSON = deepJSON.getJSONObject("geo");
				wkt = GeoTranslator.geojson2Jts(ggeoJSON);
//				wkt = GeoTranslator.jts2Wkt(ggeoGeo);
			}
			ti.setWkt(wkt);
			
			Map<String,String> relateMap = TipsLineRelateQuery.getRelateLine(sourceType, deepJSON);

			ti.setRelate_links(relateMap.get("relate_links"));
			ti.setRelate_nodes(relateMap.get("relate_nodes"));
			
//			String diffString = JSON.NULL;
//			byte[] diffBytes = result.getValue(Bytes.toBytes("data"), Bytes.toBytes("tiffDiff"));
//			if(diffBytes != null) {
//				diffString = Bytes.toString(diffBytes);
//			}
//			ti(diffString);

			return ti;
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}
	}
	
	protected static synchronized void setTotal(String index,int num){
		log.info("finished index:"+index+",num:"+num);
		total+=num;
		log.info("total:"+total);
	}
	
	protected static void initPoolExecutor() {
		int poolSize = 10;
		try {
			poolExecutor = new VMThreadPoolExecutor(poolSize, poolSize, 3,
					TimeUnit.SECONDS, new LinkedBlockingQueue(),
					new ThreadPoolExecutor.CallerRunsPolicy());
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new ServiceRtException("初始化线程池错误:" + e.getMessage(), e);
		}
	}
	protected static void shutDownPoolExecutor() {
		log.debug("关闭线程池");
		if (poolExecutor != null && !poolExecutor.isShutdown()) {
			poolExecutor.shutdownNow();
			try {
				while (!poolExecutor.isTerminated()) {
					log.debug("等待线程结束：线程数为" + poolExecutor.getActiveCount());
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				log.error("关闭线程池失败");
				throw new ServiceRtException("关闭线程池失败", e);
			}
		}
	}
	private static Set<String> getIndexes(){
		Set<String> indexes = new HashSet<String>();
		indexes.add("02");
		for(int i=1;i<23;i++){
			indexes.add("11"+StringUtils.leftPad(String.valueOf(i),2,"0"));
		}
		indexes.add("1180");
		return indexes;
	} 
	
	public static void sync(final String tableName)throws Exception{
		DbInfo tiInfo = DbService.getInstance().getOnlyDbByBizType("fmTipsIdx");
		final OracleSchema schema = new OracleSchema(
				DbConnectConfig.createConnectConfig(tiInfo.getConnectParam()));

		Set<String> indexes = getIndexes();
		final CountDownLatch latch4Log = new CountDownLatch(indexes.size());
		poolExecutor.addDoneSignal(latch4Log);
		// 
		log.debug("开始同步");
		long t = System.currentTimeMillis();
		
		for (final String index:getIndexes()) {
			log.debug("添加同步执行线程，前缀为：" + index);
			poolExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try{
						new SyncTips2Oracle(tableName,index,schema).doSync();
						latch4Log.countDown();
						log.debug("线程完成。前缀" + index);
					}catch(Exception e){
						throw new ThreadExecuteException("线程执行失败。",e);
					}
				}
			});
		}
		try {
			log.debug("等待线程完成");
			latch4Log.await();
		} catch (InterruptedException e) {
			log.warn("线程被打断");
		}
		if (poolExecutor.getExceptions().size() > 0)
			throw new ServiceRtException("线程异常",poolExecutor
					.getExceptions().get(0));
		log.debug("全部线程完成,用时：" + (System.currentTimeMillis() - t) + "ms");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			if(args.length!=1){
				System.out.println("ERROR:need args:htable");
				return;
			}
			String ihtable = args[0];
			if(StringUtils.isEmpty(ihtable)){
				System.out.println("ERROR:need args:htable");
				return;
			}
			initPoolExecutor();
			sync(ihtable);
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}finally{
			shutDownPoolExecutor();
			log.info("Over.");
		}
		
	}

}
