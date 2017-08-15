package com.navinfo.dataservice.control.row.charge;

import java.sql.Connection;
import java.util.Collection;
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
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.editman.PoiEditStatus;
import com.navinfo.dataservice.dao.plus.glm.GlmFactory;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.navicommons.exception.ServiceRtException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * FM增量导入桩家
 * @ClassName Fm2ChargeAdd
 * @author Han Shaoming
 * @date 2017年7月17日 下午8:16:02
 * @Description TODO
 */
public class Fm2ChargeAdd {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	protected VMThreadPoolExecutor threadPoolExecutor;
	
	public JSONObject excute(List<Region> regionList,String lastSyncTime,String syncTime,List<Integer> dbIdList) throws Exception {
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
			//是否转换特定大区库
			if(dbIdList != null && dbIdList.size() > 0){
				dbIds.clear();
				dbIds.addAll(dbIdList);
			}
			//2.开始执行导出数据
			log.debug("开始执行导出数据");
			Map<Integer,Integer> stats = new ConcurrentHashMap<Integer,Integer>();
			Map<Integer,JSONObject> chargePOIs = new ConcurrentHashMap<Integer,JSONObject>();
			long t = System.currentTimeMillis();
			int dbSize = dbIds.size();
			int monDbId = 0;
			if(dbSize==1){
				int dbId = dbIds.iterator().next();
				if(regionMap.containsKey(dbId)){
					monDbId = regionMap.get(dbId);
				}
				new Fm2ChargeExportThread(null,dbId,monDbId,chargePOIs,stats,lastSyncTime,syncTime).run();
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
					threadPoolExecutor.execute(new Fm2ChargeExportThread(latch,dbId,monDbId,chargePOIs,stats,lastSyncTime,syncTime));
				}
				latch.await();
				if (threadPoolExecutor.getExceptions().size() > 0) {
					throw new Exception(threadPoolExecutor.getExceptions().get(0));
				}
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
		String lastSyncTime=null;
		String syncTime=null;
		Map<Integer,JSONObject> chargePOIs;
		Map<Integer,Integer> stats;
		Fm2ChargeExportThread(CountDownLatch latch,int dbId,int monDbId,Map<Integer,JSONObject> chargePOIs,Map<Integer,Integer> stats,String lastSyncTime,String syncTime){
			this.latch = latch;
			this.dbId = dbId;
			this.monDbId = monDbId;
			this.chargePOIs = chargePOIs;
			this.stats = stats;
			this.lastSyncTime=lastSyncTime;
			this.syncTime=syncTime;
		}
		
		@Override
		public void run() {
			Connection conn=null;
			Connection connM=null;
			JSONObject jso = new JSONObject();
			JSONArray poiLog = new JSONArray();
			JSONArray chargePoi = new JSONArray();
			try{
				conn=DBConnector.getInstance().getConnectionById(dbId);
				connM = DBConnector.getInstance().getConnectionById(monDbId);
				//查询充电站(包括删除)
				String kindCode = "230218";
				List<Long> stationList = IxPoiSelector.getPidsByKindCode(conn, kindCode,true);
				//获取有变更的数据pid
				LogReader lr = new LogReader(conn);
				//key:liftcyle,value:pids
				Map<Integer,Collection<Long>> updatePids = lr.getUpdatedObj(ObjectName.IX_POI, GlmFactory.getInstance().getObjByType(ObjectName.IX_POI).getMainTable().getName(), null, lastSyncTime,syncTime);
				Set<Long> pids = new HashSet<Long>();
				for(Map.Entry<Integer, Collection<Long>> entry:updatePids.entrySet()){
					if(entry.getValue()!=null&&entry.getValue().size()>0){
						pids.addAll(entry.getValue());
					}
				}
				//查询所有的父
				Map<Long, Long> parentPids = IxPoiSelector.getParentPidByPids(conn, pids);
				if(parentPids != null && parentPids.size() > 0){
					pids.addAll(parentPids.values());
				}
				//判断是否为充电站
				Set<Long> pidList = new HashSet<Long>();
				for (Long pid : pids) {
					if(stationList.contains(pid)){
						pidList.add(pid);
					}
				}
				//查询已提交的数据
				Set<Long> submitPidList = new HashSet<Long>();
				for(Map.Entry<Integer, Collection<Long>> entry:updatePids.entrySet()){
					if(entry.getValue()!=null&&entry.getValue().size()>0){
						submitPidList.addAll(PoiEditStatus.pidFilterByEditStatus(conn, pidList, 3));
					}
				}
				//判断月库中数据履历
				//获取有变更的数据pid
				LogReader lm = new LogReader(connM);
				//key:liftcyle,value:pids
				Collection<Long> updatePidsM = lm.getUpdatedObjByPids(ObjectName.IX_POI, submitPidList, lastSyncTime,syncTime);
				//设置月库查询子表
				Set<String> selConfigM = new HashSet<String>();
				selConfigM.add("IX_POI_NAME");
				selConfigM.add("IX_POI_ADDRESS");
				Map<Long,BasicObj> objsM = new HashMap<Long,BasicObj>();
				if(updatePidsM != null && updatePidsM.size() > 0){
					objsM = ObjBatchSelector.selectByPids(connM, ObjectName.IX_POI, selConfigM, false,updatePidsM, true, false);
				}
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
				if(submitPidList.size()>0){
					Map<Long,BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, selConfig, false,submitPidList, true, false);
					if(objs.size() == 0){
						throw new Exception("没有要导出的数据");
					}
					//设置adminId
					Map<Long,Long> adminIds = IxPoiSelector.getAdminIdByPids(conn, objs.keySet());
					for(Map.Entry<Long, Long> entry:adminIds.entrySet()){
						((IxPoiObj)objs.get(entry.getKey())).setAdminId(entry.getValue());
					}
					//查询充电桩子对象
					Set<Long> childPids = new HashSet<Long>();
					for(BasicObj obj:objs.values()){
						IxPoiObj poi = (IxPoiObj) obj;
						List<BasicRow> rows = poi.getRowsByName("IX_POI_CHILDREN");
						if(rows!=null && rows.size()>0){
							for(BasicRow row:rows){
								IxPoiChildren children = (IxPoiChildren) row;
								childPids.add(children.getChildPoiPid());
							}
						}
					}
					Map<Long,BasicObj> objsChild = new HashMap<Long,BasicObj>();
					if(childPids.size() > 0){
						//设置查询子表
						Set<String> selConfigC = new HashSet<String>();
						selConfigC.add("IX_POI_NAME");
						selConfigC.add("IX_POI_ADDRESS");
						selConfigC.add("IX_POI_CHARGINGPLOT");
						//查询数据
						objsChild = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, selConfigC, false,childPids, true, false);
					}
					//获取履历
					LogReader logReader = new LogReader(conn);
					Map<Long, List<Map<String, Object>>> logDatas = logReader.getLogByPid(ObjectName.IX_POI, submitPidList);
					//获取鲜度验证信息
					Map<Long, Map<String, Object>> freshDatas = PoiEditStatus.getFreshData(conn, submitPidList);
					//获取省市城市
					MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
					Map<String, Map<String, String>> scPointAdminarea = metadataApi.scPointAdminareaByAdminId();
					//执行具体的转换
					ChargePoiConvertor poiConvertor = new ChargePoiConvertor(scPointAdminarea,objsChild,logDatas,freshDatas);
					for(BasicObj obj:objs.values()){
						try {
							long pid = obj.objPid();
							IxPoiObj objM = null;
							if(objsM.containsKey(pid)){
								objM = (IxPoiObj) objsM.get(pid);
							}
							JSONObject addPoi = poiConvertor.addPoi((IxPoiObj) obj,objM);
							if(addPoi != null){
								addPoi.put("dbId", dbId);
								chargePoi.add(addPoi);
							}
						} catch (Exception e) {
							log.error(e.getMessage(),e);
						}
					}
					//错误日志
					JSONArray errorLog = poiConvertor.getErrorLog();
					for (int i = 0; i < errorLog.size(); i++) {
						String str = errorLog.getString(i);
						poiLog.add("dbId("+dbId+"),"+str);
					}
					stats.put(dbId, objs.size());
				}else{
					stats.put(dbId, 0);
				}
				log.debug("dbId("+dbId+")转出成功。");
				poiLog.add("dbId("+dbId+")转出成功。");
			}catch(Exception e){
				log.error(e.getMessage(),e);
				log.error("dbId("+dbId+")转桩家失败,同步时间范围为start("+lastSyncTime+"),end("+syncTime+")");
				poiLog.add("dbId("+dbId+")转桩家失败,原因:"+e.getMessage());
//				throw new ThreadExecuteException("dbId("+dbId+")转桩家失败,同步时间范围为start("+lastSyncTime+"),end("+syncTime+")");
			}finally{
				//处理数据
				//处理日志
				jso.put("data", chargePoi);
				jso.put("log", poiLog);
				chargePOIs.put(dbId, jso);
				
				DbUtils.closeQuietly(conn);
				DbUtils.closeQuietly(connM);
				if(latch!=null){
					latch.countDown();
				}
			}
		}
		
	}

}
