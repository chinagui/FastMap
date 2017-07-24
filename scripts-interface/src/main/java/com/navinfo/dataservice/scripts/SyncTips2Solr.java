package com.navinfo.dataservice.scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.hbase.async.Scanner;

import com.alibaba.dubbo.common.json.JSON;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrBulkUpdater;
import com.navinfo.dataservice.dao.fcc.SolrConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.engine.fcc.tips.TipsImportUtils;
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
public class SyncTips2Solr {
	
	public static Logger log = Logger.getLogger(SyncTips2Solr.class);
	
	protected static Set<String> syncCols = new HashSet<String>();
	protected static int total=0;
	protected static VMThreadPoolExecutor poolExecutor;
	
	private String index;
	private String tableName;
	private SolrBulkUpdater sbu;
	
	public SyncTips2Solr(String tableName,String index){
		this.tableName=tableName;
		this.index=index;
		sbu=new SolrBulkUpdater(TipsImportUtils.QueueSize, TipsImportUtils.ThreadCount);
	}
	
	public void doSync()throws Exception{
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		Table htab = hbaseConn.getTable(TableName.valueOf(tableName));
		
		Scan scan = new Scan();
		scan.addFamily("data".getBytes());
		scan.setCaching(5000);
		scan.setStartRow(index.getBytes());
		scan.setStopRow((index+"a").getBytes());
		
		ResultScanner rs = htab.getScanner(scan);
		
		Result[] results = null;
		int num=0;
		while ((results = rs.next(5000)).length > 0){
			for (Result result : results){
				try{
					sbu.addTips(convert(result));
				}catch(Exception e){
					log.error(e.getMessage(),e);
				}
				num++;
				if(num%10000==0){
					sbu.commit();
					log.info("index:"+index+",num:"+num);
				}
			}
		}
		sbu.commit();
		setTotal(index,num);
		htab.close();
	}
	private JSONObject convert(Result result)throws Exception{
		JSONObject json = new JSONObject();
		try{
			if(result==null){
				log.info("result is null");
				return json;
			}
			
			//rowkey
			json.put("id", Bytes.toString(result.getRow()));
			
			//log.info("rowkey:"+Bytes.toString(result.getRow()));
			
			//显示坐标
			String geoString = Bytes.toString(result.getValue(Bytes.toBytes("data"), Bytes.toBytes("geometry")));
			JSONObject geoJSON = JSONObject.fromObject(geoString);
			JSONObject locationJSON = geoJSON.getJSONObject("g_location");
			Geometry locationGeo = GeoTranslator.geojson2Jts(locationJSON);
			String locationWkt = GeoTranslator.jts2Wkt(locationGeo);
			JSONObject guideJSON = geoJSON.getJSONObject("g_guide");
			if(syncCols.contains("wktLocation")){
				json.put("wktLocation", locationWkt);
			}
			if(syncCols.contains("g_location")){
				json.put("g_location", locationJSON);
			}
			if(syncCols.contains("g_guide")){
				json.put("g_guide", guideJSON);
			}
			
			//track
			String trackString = Bytes.toString(result.getValue(Bytes.toBytes("data"), Bytes.toBytes("track")));
			JSONObject trackJSON = JSONObject.fromObject(trackString);
			String tDate = trackJSON.getString("t_date");
			int lifecycle = trackJSON.getInt("t_lifecycle");
			int command = trackJSON.getInt("t_command");
			int dEditMeth = trackJSON.getInt("t_dEditMeth");
			int dEditStatus = trackJSON.getInt("t_dEditStatus");
			int mEditMeth = trackJSON.getInt("t_mEditMeth");
			int mEditStatus = trackJSON.getInt("t_mEditStatus");
			int tipStatus = trackJSON.getInt("t_tipStatus");
			if(syncCols.contains("t_date")){
				json.put("t_date", tDate);
			}
			if(syncCols.contains("t_lifecycle")){
				json.put("t_lifecycle", lifecycle);
			}
			if(syncCols.contains("t_command")){
				json.put("t_command", command);
			}
			if(syncCols.contains("t_dEditMeth")){
				json.put("t_dEditMeth", dEditMeth);
			}
			if(syncCols.contains("t_dEditStatus")){
				json.put("t_dEditStatus", dEditStatus);
			}
			if(syncCols.contains("t_mEditMeth")){
				json.put("t_mEditMeth", mEditMeth);
			}
			if(syncCols.contains("t_mEditStatus")){
				json.put("t_mEditStatus", mEditStatus);
			}
			if(syncCols.contains("t_tipStatus")){
				json.put("t_tipStatus", tipStatus);
			}
			
			//track履历
			JSONArray trackInfoArray = trackJSON.getJSONArray("t_trackInfo");
			int stage = 0;
			String tOperateDate = tDate;
			int handler = 0;
			if(trackInfoArray != null && trackInfoArray.size() > 0) {
				JSONObject lastTrack = trackInfoArray.getJSONObject(trackInfoArray.size() - 1);
				stage = lastTrack.getInt("stage");
				tOperateDate = lastTrack.getString("date");
				handler = lastTrack.getInt("handler");
			}
			if(syncCols.contains("t_operateDate")){
				json.put("t_operateDate", tOperateDate);
			}
			if(syncCols.contains("stage")){
				json.put("stage", stage);
			}
			if(syncCols.contains("handler")){
				json.put("handler", handler);
			}
			
			//source
			String sourceString = Bytes.toString(result.getValue(Bytes.toBytes("data"), Bytes.toBytes("source")));
			JSONObject sourceJSON = JSONObject.fromObject(sourceString);
			String sourceType = sourceJSON.getString("s_sourceType");
			int sourceCode = sourceJSON.getInt("s_sourceCode");
			int reliability = sourceJSON.getInt("s_reliability");
			int qTaskId = sourceJSON.getInt("s_qTaskId");
			int mTaskId = sourceJSON.getInt("s_mTaskId");
			int qSubTaskId = sourceJSON.getInt("s_qSubTaskId");
			int mSubTaskId = sourceJSON.getInt("s_mSubTaskId");
			if(syncCols.contains("s_sourceType")){
				json.put("s_sourceType", sourceType);
			}
			if(syncCols.contains("s_sourceCode")){
				json.put("s_sourceCode", sourceCode);
			}
			if(syncCols.contains("s_reliability")){
				json.put("s_reliability", reliability);
			}
			if(syncCols.contains("s_qTaskId")){
				json.put("s_qTaskId", qTaskId);
			}
			if(syncCols.contains("s_mTaskId")){
				json.put("s_mTaskId", mTaskId);
			}
			if(syncCols.contains("s_qSubTaskId")){
				json.put("s_qSubTaskId", qSubTaskId);
			}
			if(syncCols.contains("s_mSubTaskId")){
				json.put("s_mSubTaskId", mSubTaskId);
			}
			
			//统计坐标
			String wkt = locationWkt;
			String deepString = Bytes.toString(result.getValue(Bytes.toBytes("data"), Bytes.toBytes("deep")));

			if(syncCols.contains("deep")){
				json.put("deep", deepString);
			}
			JSONObject deepJSON  = JSONObject.fromObject(deepString);
			if(TipsStatConstant.gSLocTipsType.contains(sourceType)) {
				JSONObject gSLocJSON = deepJSON.getJSONObject("gSLoc");
				Geometry gSLocGeo = GeoTranslator.geojson2Jts(gSLocJSON);
				wkt = GeoTranslator.jts2Wkt(gSLocGeo);
			}else if(TipsStatConstant.gGeoTipsType.contains(sourceType)) {
				JSONObject ggeoJSON = deepJSON.getJSONObject("geo");
				Geometry ggeoGeo = GeoTranslator.geojson2Jts(ggeoJSON);
				wkt = GeoTranslator.jts2Wkt(ggeoGeo);
			}
			if(syncCols.contains("wkt")){
				json.put("wkt", wkt);
			}
			
			
			String feedbackString = Bytes.toString(result.getValue(Bytes.toBytes("data"), Bytes.toBytes("feedback")));
			if(syncCols.contains("feedback")){
				json.put("feedback", feedbackString);
			}
			
			Map<String,String> relateMap = TipsLineRelateQuery.getRelateLine(sourceType, deepJSON);

			if(syncCols.contains("relate_links")){
				json.put("relate_links", relateMap.get("relate_links"));
			}
			if(syncCols.contains("relate_nodes")){
				json.put("relate_nodes", relateMap.get("relate_nodes"));
			}
			
			String diffString = JSON.NULL;
			byte[] diffBytes = result.getValue(Bytes.toBytes("data"), Bytes.toBytes("tiffDiff"));
			if(diffBytes != null) {
				diffString = Bytes.toString(diffBytes);
			}
			if(syncCols.contains("tipdiff")){
				json.put("tipdiff", diffString);
			}
			
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}
		return json;
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
	
	public static void sync(final String tableName){
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
						new SyncTips2Solr(tableName,index).doSync();
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
			if(args.length<2){
				System.out.println("ERROR:need args:htable col1[ col2 col3]");
				return;
			}
			String ihtable = args[0];
			if(StringUtils.isEmpty(ihtable)){
				System.out.println("ERROR:need args:htable col1[ col2 col3]");
				return;
			}
			for(int i=1; i<args.length;i++){
				syncCols.add(args[i]);
		    }
			if(syncCols.size()==0){
				System.out.println("ERROR:need args:htable col1[ col2 col3]");
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
