package com.navinfo.dataservice.control.row.charge;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.exception.ServiceRtException;
import com.navinfo.navicommons.exception.ThreadExecuteException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * FM初始化导入桩家
 * @ClassName Fm2ChargeInit
 * @author Han Shaoming
 * @date 2017年7月17日 下午8:16:42
 * @Description TODO
 */
public class Fm2ChargeInit {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	private volatile static Fm2ChargeInit instance;
	public static Fm2ChargeInit getInstance(){
		if(instance==null){
			synchronized(Fm2ChargeInit.class){
				if(instance==null){
					instance=new Fm2ChargeInit();
				}
			}
		}
		return instance;
	}
	private Fm2ChargeInit(){}
	
	protected VMThreadPoolExecutor threadPoolExecutor;
	
	public JSONObject excute(List<Region> regionList) throws Exception {
		try {
			JSONObject result = new JSONObject();
			//1.处理大区库
			Set<Integer> dbIds = new HashSet<Integer>();
			Map<Integer,Integer> regionMap = new HashMap<Integer,Integer>();
			for (Region region : regionList) {
				if(region.getDailyDbId() != null){
					dbIds.add(region.getDailyDbId());
					//处理日库月库
					if(region.getMonthlyDbId() != null){
						regionMap.put(region.getDailyDbId(), region.getMonthlyDbId());
					}
				}
			}
			//2.开始执行导出数据
			log.debug("开始执行导出数据");
			Map<Integer,Integer> stats = new ConcurrentHashMap<Integer,Integer>();
			Map<Integer,JSONObject> chargePOIs = new ConcurrentHashMap<Integer,JSONObject>();
			long t = System.currentTimeMillis();
			int dbSize = dbIds.size();
			int monDbId = 0;
			try {
				if(dbSize==1){
					int dbId = dbIds.iterator().next();
					if(regionMap.containsKey(dbId)){
						monDbId = regionMap.get(dbId);
					}
					new Fm2ChargeExportThread(null,dbId,monDbId,chargePOIs,stats).run();
				}else{
					if(dbSize>10){
						initThreadPool(10);
					}else{
						initThreadPool(dbSize);
					}
					final CountDownLatch latch = new CountDownLatch(dbSize);
					threadPoolExecutor.addDoneSignal(latch);
					// 执行转数据
					for(int dbId:dbIds){
						if(regionMap.containsKey(dbId)){
							monDbId = regionMap.get(dbId);
						}
						threadPoolExecutor.execute(new Fm2ChargeExportThread(latch,dbId,monDbId,chargePOIs,stats));
					}
					latch.await();
					if (threadPoolExecutor.getExceptions().size() > 0) {
						throw new Exception(threadPoolExecutor.getExceptions().get(0));
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(),e);
			}
			//3.处理各大区库数据
			JSONArray data = new JSONArray();
			JSONArray errorLog = new JSONArray();
			for(Map.Entry<Integer, JSONObject> entry : chargePOIs.entrySet()){
				JSONObject jso = entry.getValue();
				JSONArray chargePOI = jso.getJSONArray("data");
				JSONArray chargePOILog = jso.getJSONArray("log");
				if(chargePOI != null && chargePOI.size() > 0){
					data.addAll(chargePOI);
				}
				if(chargePOILog != null && chargePOILog.size() > 0){
					errorLog.addAll(chargePOILog);
				}
			}
			result.put("total", data.size());
			result.put("data", data);
			result.put("log", errorLog);
			log.debug("所有大区库导出json完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			
			return result;
		} catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			shutDownPoolExecutor();
		}
	}
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initThreadPool(int poolSize)throws Exception{
		log.debug("开始初始化线程池");
        threadPoolExecutor = new VMThreadPoolExecutor(poolSize,
        		poolSize,
				3,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue(),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}
	private void shutDownPoolExecutor(){
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
	
	class Fm2ChargeExportThread implements Runnable{
		CountDownLatch latch = null;
		int dbId=0;
		int monDbId=0;
		Map<Integer,JSONObject> chargePOIs;
		Map<Integer,Integer> stats;
		Fm2ChargeExportThread(CountDownLatch latch,int dbId,int monDbId,Map<Integer,JSONObject> chargePOIs,Map<Integer,Integer> stats){
			this.latch = latch;
			this.dbId = dbId;
			this.monDbId = monDbId;
			this.chargePOIs = chargePOIs;
			this.stats = stats;
		}
		
		@Override
		public void run() {
			Connection conn=null;
			try{
				conn=DBConnector.getInstance().getConnectionById(dbId);
				//查询充电站
				String kindCode = "230218";
				List<Long> pidList = IxPoiSelector.getPidsByKindCode(conn, kindCode,false);
				//设置查询子表
				Set<String> selConfig = new HashSet<String>();
				selConfig.add("IX_POI_NAME");
				selConfig.add("IX_POI_ADDRESS");
				selConfig.add("IX_POI_CONTACT");
				selConfig.add("IX_POI_CHILDREN");
				selConfig.add("IX_POI_CHARGINGSTATION");
				selConfig.add("IX_POI_CHARGINGPLOT");
				selConfig.add("IX_POI_FLAG_METHOD");
				//...
				JSONObject jso = new JSONObject();
				JSONArray chargePoi = new JSONArray();
				JSONArray poiLog = new JSONArray();
				if(pidList.size()>0){
					Map<Long,BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, selConfig, false,pidList, true, false);
					//设置adminId
					Map<Long,Long> adminIds = IxPoiSelector.getAdminIdByPids(conn, objs.keySet());
					for(Map.Entry<Long, Long> entry:adminIds.entrySet()){
						((IxPoiObj)objs.get(entry.getKey())).setAdminId(entry.getValue());
					}
					//执行具体的转换
					ChargePoiConvertor poiConvertor = new ChargePoiConvertor();
					for(BasicObj obj:objs.values()){
						try {
							JSONObject initPoi = poiConvertor.initPoi((IxPoiObj) obj,conn);
							if(initPoi != null){
								chargePoi.add(initPoi);
							}
							//错误日志
							JSONArray errorLog = poiConvertor.getErrorLog();
							for (int i = 0; i < errorLog.size(); i++) {
								String str = errorLog.getString(i);
								poiLog.add("dbId("+dbId+"),"+str);
							}
						} catch (Exception e) {
							log.error(e.getMessage(),e);
						}
					}
					stats.put(dbId, objs.size());
				}else{
					stats.put(dbId, 0);
				}
				//处理数据
				jso.put("data", chargePoi);
				jso.put("log", poiLog);
				chargePOIs.put(dbId, jso);
				log.debug("dbId("+dbId+")转出成功。");
			}catch(Exception e){
				log.error(e.getMessage(),e);
				throw new ThreadExecuteException("dbId("+dbId+")转桩家失败");
			}finally{
				DbUtils.closeQuietly(conn);
				if(latch!=null){
					latch.countDown();
				}
			}
		}
		
	}
}
