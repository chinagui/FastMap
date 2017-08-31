package com.navinfo.dataservice.job.statics.manJob;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceRtException;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * @ClassName: NoTaskJob
 * @author songhe
 * @date 2017年8月30日
 * 
 */
public class NoTaskJob extends AbstractStatJob {

	protected VMThreadPoolExecutor threadPoolExecutor;

	/**
	 * @param jobInfo
	 */
	public NoTaskJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public String stat() throws JobException {
		try {
			ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
			List<Region> regionList = manApi.queryRegionList();
			Map<Integer, Map<Integer, Set<Integer>>> cityMeshs = manApi.queryAllCityGrids();
			Set<Integer> dbIds = new HashSet<Integer>();
			dbIds.add(13);
//			for (Region region : regionList) {
//				if (region.getDailyDbId() != null) {
//					dbIds.add(region.getDailyDbId());
//				}
//			}

			Map<Integer, Map<String, List<Map<String, Object>>>> stats = new ConcurrentHashMap<Integer, Map<String, List<Map<String, Object>>>>();
			long time = System.currentTimeMillis();
			int dbSize = dbIds.size();
			if(dbSize == 1){
				new NoTaskStatThread(null, dbIds.iterator().next(), stats, cityMeshs).run();
			}else{
				if(dbSize > 10){
					initThreadPool(10);
				}else{
					initThreadPool(dbSize);
				}
				final CountDownLatch latch = new CountDownLatch(dbSize);
				threadPoolExecutor.addDoneSignal(latch);
				// 执行转数据
				for(int dbId : dbIds){
					threadPoolExecutor.execute(new NoTaskStatThread(latch, dbId, stats, cityMeshs));
				}
				latch.await();
				if(threadPoolExecutor.getExceptions().size() > 0){
					throw new Exception(threadPoolExecutor.getExceptions().get(0));
				}
			}
			log.debug("所有无任务日库作业数据统计完毕。用时：" + ((System.currentTimeMillis() - time) / 1000) + "s.");

			Map<String, List<Map<String, Object>>> result = new HashMap<String, List<Map<String, Object>>>();
			result.put("block_notask", new ArrayList<Map<String, Object>>());
			result.put("city_notask", new ArrayList<Map<String, Object>>());

			for(Entry<Integer, Map<String, List<Map<String, Object>>>> entry : stats.entrySet()){
				result.get("block_notask").addAll(entry.getValue().get("blockStat"));
				result.get("city_notask").addAll(entry.getValue().get("cityStat"));
			}

			log.info("stats:" + JSONObject.fromObject(result).toString());
			return JSONObject.fromObject(result).toString();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}
	}

	private void initThreadPool(int poolSize) throws Exception {
		log.debug("开始初始化线程池");
		threadPoolExecutor = new VMThreadPoolExecutor(poolSize, poolSize, 3, TimeUnit.SECONDS,
				new LinkedBlockingQueue(), new ThreadPoolExecutor.CallerRunsPolicy());
	}

	private void shutDownPoolExecutor() {
		if (threadPoolExecutor != null && !threadPoolExecutor.isShutdown()) {
			log.debug("关闭线程池");
			threadPoolExecutor.shutdownNow();
			try {
				while (!threadPoolExecutor.isTerminated()) {
					log.debug("等待线程结束：线程数为" + threadPoolExecutor.getActiveCount());
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				log.error("关闭线程池失败");
				throw new ServiceRtException("关闭线程池失败", e);
			}
		}
	}

	class NoTaskStatThread implements Runnable {
		CountDownLatch latch = null;
		int dbId = 0;
		Map<Integer, Map<Integer, Set<Integer>>> cityMeshs = new HashMap<>();
		Map<Integer, Map<String, List<Map<String, Object>>>> stats;

		NoTaskStatThread(CountDownLatch latch, int dbId, Map<Integer, Map<String, List<Map<String, Object>>>> stat,
				Map<Integer, Map<Integer, Set<Integer>>> cityMeshs) {
			this.latch = latch;
			this.dbId = dbId;
			this.stats = stat;
			this.cityMeshs = cityMeshs;
		}

		@Override
		public void run() {
			Connection metaConn = null;
			try {
				metaConn = DBConnector.getInstance().getMetaConnection();
				// 查询并统计所有的无任务poi数据
				Map<Integer, Object> poiMap = queryAllPoiData(metaConn);
				// 查询并统计所有的无任务poi数据
				Map<Integer, Object> tipsMap = queryAllTipsData();
				//合并poi和tips数据
//				Map<Integer, Object> resultDataMap = mergePoiAndTips(poiMap, tipsMap);
				//根据block和city处理分类统计数据
				Map<String, Object> result = convertData(poiMap, tipsMap, cityMeshs);

				List<Map<String, Object>> cityStat = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> blockStat = new ArrayList<Map<String, Object>>();

				Map<Integer, Map<String, Object>> cityNoTask = (Map<Integer, Map<String, Object>>) result.get("city");
				for(Map.Entry<Integer, Map<String, Object>> entry : cityNoTask.entrySet()){
					Map<String, Object> cell = new HashMap<String, Object>();
					cell.put("cityId", entry.getKey());
					cell.put("tipsTotal", entry.getValue().get("tipsTotal"));
					cell.put("poiTotal", entry.getValue().get("poiTotal"));
					cell.put("dealershipPoiTotal", entry.getValue().get("dealershipPoiTotal"));
					cityStat.add(cell);
				}

				Map<Integer, Map<String, Object>> blockNoTask = (Map<Integer, Map<String, Object>>) result.get("block");
				for(Map.Entry<Integer, Map<String, Object>> entry : blockNoTask.entrySet()){
					Map<String, Object> cell = new HashMap<String, Object>();
					cell.put("blockId", entry.getKey());
					cell.put("tipsTotal", entry.getValue().get("tipsTotal"));
					cell.put("poiTotal", entry.getValue().get("poiTotal"));
					cell.put("dealershipPoiTotal", entry.getValue().get("dealershipPoiTotal"));
					blockStat.add(cell);
				}

				Map<String, List<Map<String, Object>>> temp = new HashMap<String, List<Map<String, Object>>>();

				temp.put("cityStat", cityStat);
				temp.put("blockStat", blockStat);
				stats.put(dbId, temp);
			}catch(Exception e){
				log.error("dbId(" + dbId + ")POI日库作业数据统计失败" + e.getMessage(), e);
			}finally{
				if(latch != null){
					latch.countDown();
				}
				DbUtils.closeQuietly(metaConn);
			}
		}

		/**
		 * 处理任务，子任务，无任务数据
		 * @param Connection
		 * @throws Exception
		 * 
		 */
		public Map<Integer, Object> queryAllPoiData(final Connection metaConn) throws Exception {
			Connection conn = null;
			try {
				conn = DBConnector.getInstance().getConnectionById(dbId);

				QueryRunner run = new QueryRunner();

				StringBuilder sb = new StringBuilder();
				sb.append(" SELECT P.KIND_CODE,                   ");
				sb.append("        P.CHAIN,                       ");
				sb.append("        S.STATUS,                      ");
				sb.append("        P.GEOMETRY                     ");
				sb.append("   FROM POI_EDIT_STATUS S,IX_POI P     ");
				sb.append("   WHERE P.PID = S.PID                 ");
				sb.append("   AND S.QUICK_TASK_ID = 0             ");
				sb.append("   AND S.MEDIUM_TASK_ID = 0  AND ROWNUM<=20          ");

				String selectSql = sb.toString();

				ResultSetHandler<Map<Integer, Object>> rsHandler = new ResultSetHandler<Map<Integer, Object>>() {
					public Map<Integer, Object> handle(ResultSet rs) throws SQLException {
						Map<Integer, Object> result = new HashMap<Integer, Object>(10240);
						while(rs.next()){
							boolean poiType = false;
							int pois = 0;
							int dealerships = 0;
							int gridId = 0;
							Map<String, Integer> poisMap = new HashMap<>();
							int status = rs.getInt("STATUS");
							String kindCode = rs.getString("KIND_CODE");
							String chain = rs.getString("CHAIN") == null ? "" : rs.getString("CHAIN");

							STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
							Point geo;
							try {
								geo = (Point) GeoTranslator.struct2Jts(struct);
								double x = geo.getX();
								double y = geo.getY();
								String[] grids = CompGridUtil.point2Grids(x, y);
								gridId = Integer.parseInt(grids[0]);
								if(result.containsKey(gridId)){
									poisMap = (Map<String, Integer>) result.get(gridId);
									dealerships = poisMap.get("dealerships");
									pois = poisMap.get("pois");
								}
							}catch(Exception e){
								log.error("处理任务，子任务，无任务数据坐标，无法获取到gridid:" + e.getMessage(), e);
							}

							if(status != 0){
								poiType = wetherDealership(metaConn, kindCode, chain);
							}
							if(poiType){
								dealerships++;
							}else{
								pois++;
							}
							poisMap.put("dealerships", dealerships);
							poisMap.put("pois", pois);
							result.put(gridId, poisMap);
						}
						return result;
					}
				};
				log.info("sql:" + selectSql);
				return run.query(conn, selectSql, rsHandler);
			} catch (Exception e) {
				log.error("从大区库查询处理数据异常:" + e.getMessage(), e);
				DbUtils.closeQuietly(conn);
				throw e;
			} finally {
				DbUtils.closeQuietly(conn);
			}
		}

		/**
		 * 判断是否是代理店数据
		 * 
		 * @param Connection
		 * @param String
		 * @param String
		 * @throws Exception
		 * 
		 */
		public boolean wetherDealership(Connection metaConn, String kindCode, final String chain) {
			try {
				QueryRunner run = new QueryRunner();

				StringBuilder sb = new StringBuilder();
				sb.append(" SELECT P.CHAIN,                       ");
				sb.append("        P.CATEGORY                     ");
				sb.append("   FROM SC_POINT_SPEC_KINDCODE_NEW P   ");
				sb.append("   WHERE P.POI_KIND = '" + kindCode + "'   ");
				sb.append("   AND P.TYPE = 7                      ");

				String selectSql = sb.toString();

				ResultSetHandler<Boolean> rsHandler = new ResultSetHandler<Boolean>() {
					public Boolean handle(ResultSet rs) throws SQLException {
						while (rs.next()) {
							int categeory = rs.getInt("CATEGORY");
							if (1 == categeory) {
								return true;
							}
							String metaChain = rs.getString("CHAIN");
							if (chain.equals(metaChain)) {
								if (3 == categeory || 7 == categeory) {
									return true;
								}
							}
						}
						return false;
					}
				};
				log.info("sql:" + selectSql);
				return run.query(metaConn, selectSql, rsHandler);
			} catch (Exception e) {
				log.error("从元数据库查询数据异常:" + e.getMessage(), e);
				return false;
			}
		}
		
		/**
		 * 处理tips数据
		 * @throws Exception
		 * 
		 */
		public Map<Integer, Object> queryAllTipsData() throws Exception {
			Connection conn = null;
			try {
				conn = DBConnector.getInstance().getTipsIdxConnection();

				QueryRunner run = new QueryRunner();


				String selectSql = "select t.wkt from tips_index t where substr(t.s_sourcetype,0,2) <> 80 "
						+ "and t.s_qtaskid = 0 and t.s_mTaskId = 0 and t.t_tipStatus = 2";

				ResultSetHandler<Map<Integer, Object>> rsHandler = new ResultSetHandler<Map<Integer, Object>>() {
					public Map<Integer, Object> handle(ResultSet rs) throws SQLException {
						Map<Integer, Object> result = new HashMap<Integer, Object>(10240);
						while(rs.next()){
							int gridId = 0;
							int tipsCount = 0;
							
							STRUCT geoStruct=(STRUCT) rs.getObject("wkt");
							Geometry geometry = null;
							try {
								geometry = GeoTranslator.struct2Jts(geoStruct);
							} catch (Exception e) {
								e.printStackTrace();
							}
							Coordinate coordinate = geometry.getCoordinate();
							double x = coordinate.x;
							double y = coordinate.y;
							String[] grids = CompGridUtil.point2Grids(x, y);
							gridId = Integer.parseInt(grids[0]);
							if(result.containsKey(gridId)){
								tipsCount = (int) result.get(gridId);
							}
							tipsCount++;
							result.put(gridId, tipsCount);
						}
						return result;
					}
				};
				log.info("sql:" + selectSql);
				return run.query(conn, selectSql, rsHandler);
			} catch (Exception e) {
				log.error("从大区库查询处理数据异常:" + e.getMessage(), e);
				DbUtils.closeQuietly(conn);
				throw e;
			} finally {
				DbUtils.closeQuietly(conn);
			}
		}

		
		/**
		 * 处理city和block统计数据
		 * @param Map<Integer, Object>
		 * @param Map<Integer, Map<Integer, Set<Integer>>>
		 * @return Map<String, Object>
		 * 
		 * */
		@SuppressWarnings("unchecked")
		public Map<String, Object> convertData(Map<Integer, Object> poiMap, Map<Integer, Object> tipsMap, Map<Integer, Map<Integer, Set<Integer>>> cityMeshs){

			Map<Integer, Object> blocks = new HashMap<>();
			Map<Integer, Object> citys = new HashMap<>();
			Map<String, Object> result = new HashMap<>();
			for(Entry<Integer, Map<Integer, Set<Integer>>> cityEntry : cityMeshs.entrySet()){
				int cityId = cityEntry.getKey();
				int cityPois = 0;
				int cityDealerships = 0;
				int cityTips = 0;
				Map<String, Integer> cityData = new HashMap<>();
				Map<Integer, Set<Integer>> blockMap = cityEntry.getValue();
				for(Entry<Integer, Set<Integer>> blockEntry : blockMap.entrySet()){
					Map<String, Integer> blockData = new HashMap<>();
					int blockId = blockEntry.getKey();
					Set<Integer> grids = blockEntry.getValue();
					int blockPois = 0;
					int blockDealerships = 0;
					int blockTips = 0;
					for(Entry<Integer, Object> gridEntry : poiMap.entrySet()){
						int gridid = gridEntry.getKey();
						if(grids.contains(gridid)){
							Map<String, Integer> poiData = (Map<String, Integer>) gridEntry.getValue();
							blockPois += poiData.get("pois");
							blockDealerships += poiData.get("dealerships");
						}
					}
					for(Entry<Integer, Object> tipsEntry : tipsMap.entrySet()){
						int gridid = tipsEntry.getKey();
						if(grids.contains(gridid)){
							blockTips += (int) tipsEntry.getValue();
						}
					}
					blockData.put("poiTotal", blockPois);
					blockData.put("dealershipPoiTotal", blockDealerships);
					blockData.put("tipsTotal", blockTips);
					blocks.put(blockId, blockData);
				}
				for(Entry<Integer, Object> block : blocks.entrySet()){
					Map<String, Integer> blockData = (Map<String, Integer>) block.getValue();
					cityPois += blockData.get("poiTotal");
					cityDealerships += blockData.get("dealershipPoiTotal");
					cityTips += blockData.get("tipsTotal");
				}
				cityData.put("poiTotal", cityPois);
				cityData.put("dealershipPoiTotal", cityDealerships);
				cityData.put("tipsTotal", cityTips);
				citys.put(cityId, cityData);
			}
			result.put("city", citys);
			result.put("block", blocks);
			return result;
		}
	}

}
