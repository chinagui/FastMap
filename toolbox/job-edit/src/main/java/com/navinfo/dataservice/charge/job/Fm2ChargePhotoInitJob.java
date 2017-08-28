package com.navinfo.dataservice.charge.job;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Date;
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

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.control.row.charge.ChargePhotoConvertor;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.exception.ServiceRtException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * FM导入桩家的初始化照片数据包
 * @ClassName Fm2ChargePhotoInitRequest
 * @author Han Shaoming
 * @date 2017年8月25日 下午2:27:16
 * @Description TODO
 */
public class Fm2ChargePhotoInitJob extends AbstractJob {

	protected VMThreadPoolExecutor threadPoolExecutor;
	
	public Fm2ChargePhotoInitJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute() throws JobException {
		PrintWriter pw = null;
		try {
			JSONObject result = new JSONObject();
			//0.处理参数
			Fm2ChargePhotoInitRequest req = (Fm2ChargePhotoInitRequest) request;
			List<Integer> dbIdList = req.getDbIds();
			//1.处理大区库
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			List<Region> regionList = manApi.queryRegionList();
			Set<Integer> dbIds = new HashSet<Integer>();
			for (Region region : regionList) {
				if(region.getDailyDbId() != null){
					dbIds.add(region.getDailyDbId());
				}
			}
			//是否转换特定大区库
			if(dbIdList != null && dbIdList.size() > 0){
				dbIds.clear();
				dbIds.addAll(dbIdList);
			}
			//2.开始执行导出数据
			//01.生成目录
//			String rootDownloadPath = SystemConfigFactory.getSystemConfig().getValue(PropConstant.downloadFilePathRoot);
			String rootDownloadPath = "D:\\temp\\";
			//每个月独立目录
			String curYm = DateUtils.getCurYyyymm();
			String monthDir = rootDownloadPath+"chargeHome"+File.separator+curYm+File.separator;
			File mdirFile = new File(monthDir);
			if(!mdirFile.exists()){
				mdirFile.mkdirs();
			}
			//月下,一次同步的文件放在自己的目录下
			String sysTime = DateUtils.dateToString(new Date(), DateUtils.DATE_COMPACTED_FORMAT);
			String mydir = monthDir+sysTime+File.separator;
			String photoPath = sysTime+File.separator;
			File file = new File(mydir);
			file.mkdirs();
			//2.开始执导出数据到文件
			log.info("开始执行导出数据");
			Map<Integer,Integer> stats = new ConcurrentHashMap<Integer,Integer>();
			Map<Integer,JSONObject> chargePOIs = new ConcurrentHashMap<Integer,JSONObject>();
			long t = System.currentTimeMillis();
			int dbSize = dbIds.size();
			
			if(dbSize==1){
				int dbId = dbIds.iterator().next();
				new Fm2ChargeExportThread(null,dbId,chargePOIs,stats,photoPath,mydir).run();
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
					threadPoolExecutor.execute(new Fm2ChargeExportThread(latch,dbId,chargePOIs,stats,photoPath,mydir));
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
			log.info("所有大区库导出json完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			//4.写入数据文件
			String datasFile = mydir+"photo.txt";
			log.info("写入数据文件:"+datasFile);
			pw = new PrintWriter(datasFile);
			pw.println(result.toString());
		} catch(Exception e){
			log.error(e.getMessage(),e);
			throw new JobException(e.getMessage(),e);
		}finally{
			if(pw!=null){
				pw.close();
			}
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
		Map<Integer,JSONObject> chargePOIs;
		Map<Integer,Integer> stats;
		String photoPath = null;
		String mydir = null;
		Fm2ChargeExportThread(CountDownLatch latch,int dbId,Map<Integer,JSONObject> chargePOIs,Map<Integer,Integer> stats,
				String photoPath,String mydir){
			this.latch = latch;
			this.dbId = dbId;
			this.chargePOIs = chargePOIs;
			this.stats = stats;
			this.photoPath = photoPath;
			this.mydir = mydir;
		}
		
		@Override
		public void run() {
			Connection conn=null;
			JSONObject jso = new JSONObject();
			JSONArray poiLog = new JSONArray();
			JSONArray chargePoi = new JSONArray();
			try{
				conn=DBConnector.getInstance().getConnectionById(dbId);
				//查询充电站
				String kindCode = "230218";
				List<Long> pidList = IxPoiSelector.getPidsByKindCodeInit(conn, kindCode);
				//设置查询子表
				Set<String> selConfig = new HashSet<String>();
				selConfig.add("IX_POI_NAME");
				selConfig.add("IX_POI_ADDRESS");
				selConfig.add("IX_POI_CHILDREN");
				selConfig.add("IX_POI_CHARGINGSTATION");
				selConfig.add("IX_POI_PHOTO");  
				//...
				if(pidList.size()>0){
					Map<Long,BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, selConfig, false,pidList, true, false);
					//根据条件过滤数据
					Set<Long> rmPids = new HashSet<Long>();
					for(BasicObj obj:objs.values()){
						IxPoiObj poiObj = (IxPoiObj) obj;
						long pid = poiObj.objPid();
						//当POI的pid为0时，此站或桩不转出（外业作业中的新增POI未经过行编）
						if(pid == 0){rmPids.add(pid);continue;}
						//如果站下没有充电桩或站下所有的充电桩均为删除状态，则站及桩均不转出（当IX_POI_CHARGINGSTATION表中的CHARGING_TYPE=2或4时，充电站需要转出）；
						List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
						if(rows == null || rows.size() == 0){rmPids.add(pid);continue;}
						if(rows != null && rows.size() > 0){
							for (BasicRow row : rows) {
								IxPoiChargingstation ixPoiChargingstation = (IxPoiChargingstation) row;
								int type = ixPoiChargingstation.getChargingType();
								if(type != 2 && type != 4){
									List<BasicRow> childs = poiObj.getRowsByName("IX_POI_CHILDREN");
									if(childs == null || childs.size() == 0 ){rmPids.add(pid);continue;}
								}
							}
						}
					}
					for (Long pid : rmPids) {
						objs.remove(pid);
						pidList.removeAll(rmPids);
					}
					System.out.println("====================dbId("+dbId+")=======================,"+pidList.size());
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
						selConfigC.add("IX_POI_CHARGINGPLOT");
						selConfigC.add("IX_POI_PHOTO");
						//查询数据
						objsChild = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, selConfigC, false,childPids, true, false);
					}
					//执行具体的转换
					String dir = mydir+dbId+File.separator;
					File file = new File(dir);
					file.mkdirs();
					ChargePhotoConvertor photoConvertor = new ChargePhotoConvertor(objsChild,photoPath+dbId+File.separator,dir);
					for(BasicObj obj:objs.values()){
						try {
							JSONArray initPoi = photoConvertor.initPoi((IxPoiObj) obj);
							if(initPoi != null && initPoi.size() > 0){
								chargePoi.addAll(initPoi);
							}
						} catch (Exception e) {
							log.error(e.getMessage(),e);
						}
					}
					//错误日志
					JSONArray errorLog = photoConvertor.getErrorLog();
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
				log.error("dbId("+dbId+")转桩家失败");
				poiLog.add("dbId("+dbId+")转桩家失败,原因:"+e.getMessage());
//				throw new ThreadExecuteException("dbId("+dbId+")转桩家失败");
			}finally{
				//处理数据
				//处理日志
				jso.put("data", chargePoi);
				jso.put("log", poiLog);
				chargePOIs.put(dbId, jso);
				
				DbUtils.closeQuietly(conn);
				if(latch!=null){
					latch.countDown();
				}
			}
		}
	}
	
	
}
