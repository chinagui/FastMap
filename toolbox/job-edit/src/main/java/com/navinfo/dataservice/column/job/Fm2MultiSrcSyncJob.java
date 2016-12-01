package com.navinfo.dataservice.column.job;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
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

import com.navinfo.dataservice.api.edit.iface.FmMultiSrcSyncApi;
import com.navinfo.dataservice.api.edit.model.FmMultiSrcSync;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.commons.util.ZipUtils;
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
import com.navinfo.dataservice.engine.editplus.convert.MultiSrcPoiConvertor;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.exception.ServiceRtException;
import com.navinfo.navicommons.exception.ThreadExecuteException;
import net.sf.json.JSONObject;

/** 
 * @ClassName: Fm2MultiSrcSyncJob
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: Fm2MultiSrcSyncJob.java
 */
public class Fm2MultiSrcSyncJob extends AbstractJob {

	protected VMThreadPoolExecutor threadPoolExecutor;
	int currentStatus=FmMultiSrcSync.STATUS_CREATING;

	public Fm2MultiSrcSyncJob(JobInfo jobInfo) {
		super(jobInfo);
	}
	
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

	@Override
	public void execute() throws JobException {
		FmMultiSrcSyncApi syncApi;
		try{
			syncApi = (FmMultiSrcSyncApi)ApplicationContextUtil
					.getBean("fmMultiSrcSyncApi");
			//设置创建中状态
			syncApi.updateFmMultiSrcSyncStatus(currentStatus,jobInfo.getId());
			
			Fm2MultiSrcSyncJobRequest req = (Fm2MultiSrcSyncJobRequest)request;
			String lastSyncTime=req.getLastSyncTime();
			String syncTime=req.getSyncTime();
			String curYm = DateUtils.getCurYyyymm();
			
			//2. 生成导出数据包
			createZipFile(syncApi,req.getDbIds(),lastSyncTime,syncTime,curYm);
			
			//3.组装URL，通知多源平台
			String zipUrl = "multisrc/"+curYm+"/"+syncTime+"_day.zip";
			log.debug("生成数据包url："+zipUrl);
			notifyMultiSrc(zipUrl,syncApi);
			super.response("通知多源完成", null);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}
	}
	
	private void createZipFile(FmMultiSrcSyncApi syncApi,List<Integer> dbIds,String lastSyncTime,String syncTime,String curYm)throws Exception{
		PrintWriter pw = null;
		try{
			//1.生成目录
//			String rootDownloadPath = SystemConfigFactory.getSystemConfig().getValue(PropConstant.downloadFilePathRoot);
			String rootDownloadPath = "F:\\data\\";
			//每个月独立目录
			String monthDir = rootDownloadPath+"multisrc"+File.separator+curYm+File.separator;
			File mdirFile = new File(monthDir);
			if(!mdirFile.exists()){
				mdirFile.mkdirs();
			}
			//月下,一次同步的文件放在自己的目录下
			String mydir = monthDir+syncTime+"_day"+File.separator;
			File file = new File(mydir);
			file.mkdirs();
			//2.开始执导出数据到文件
			log.debug("开始执导出数据到文件");
			Map<Integer,Integer> stats = new ConcurrentHashMap<Integer,Integer>();
			long t = System.currentTimeMillis();
			int dbSize = dbIds.size();
			if(dbSize==1){
				new Fm2MultiSrcExportThread(null,dbIds.get(0),lastSyncTime,syncTime,mydir,stats).run();
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
					threadPoolExecutor.execute(new Fm2MultiSrcExportThread(latch,dbId,lastSyncTime,syncTime,mydir,stats));
				}
				latch.await();
				if (threadPoolExecutor.getExceptions().size() > 0) {
					throw new Exception(threadPoolExecutor.getExceptions().get(0));
				}
			}
			log.debug("所有大区库导出到文件完毕。用时："+((System.currentTimeMillis()-t)/1000)+"s.");
			super.response("大区库导出json文件完成", null);
			//3.写入统计文件
			String statsFile = mydir+syncTime+"_day_stat.txt";
			pw = new PrintWriter(statsFile);
			JSONObject jo=new JSONObject();
			for(Map.Entry<Integer, Integer> entry:stats.entrySet()){
				jo.put(syncTime+"_day_"+entry.getKey(), entry.getValue());
			}
			pw.println(jo.toString());
			//4.打包生成zip文件，放在月目录下
			String zipFileName = monthDir + syncTime+"_day.zip";
			ZipUtils.zipFile(mydir,zipFileName);
			super.response("统计文件生成并打包完成", null);
			currentStatus=FmMultiSrcSync.STATUS_CREATED_SUCCESS;
			syncApi.updateFmMultiSrcSync(currentStatus, zipFileName,jobInfo.getId());
		}catch(Exception e){
			log.error(e.getMessage(),e);
			syncApi.updateFmMultiSrcSyncStatus(FmMultiSrcSync.STATUS_CREATED_FAIL,jobInfo.getId());
			throw e;
		}finally{
			if(pw!=null){
				pw.close();
			}
			shutDownPoolExecutor();
		}
	}
	
	private void notifyMultiSrc(String zipFile,FmMultiSrcSyncApi syncApi){
		try{
			//
			log.debug("开始通知多源平台");
			String zipFileUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.serverUrl)
					+SystemConfigFactory.getSystemConfig().getValue(PropConstant.downloadUrlPathRoot)
					+zipFile;
			log.debug("数据包url:"+zipFileUrl);
			Map<String,String> parMap = new HashMap<String,String>();
			parMap.put("url", zipFileUrl);
			String result = ServiceInvokeUtil.invoke("", parMap, 10000);
			log.debug("notify multisrc result:"+result);
			syncApi.updateFmMultiSrcSyncStatus(FmMultiSrcSync.STATUS_SYNC_SUCCESS,jobInfo.getId());
		}catch(Exception e){
			try{
				syncApi.updateFmMultiSrcSyncStatus(FmMultiSrcSync.STATUS_SYNC_FAIL,jobInfo.getId());
			}catch(Exception ex){
				log.error(ex.getMessage(),ex);
			}
			log.warn("日库同步数据包已生成，通知多源平台时发生错误，请联系多源平台运维");
			log.error(e.getMessage(),e);
		}
	}
	class Fm2MultiSrcExportThread implements Runnable{
		CountDownLatch latch = null;
		int dbId=0;
		String lastSyncTime=null;
		String syncTime=null;
		String dir=null;
		Map<Integer,Integer> stats;
		Fm2MultiSrcExportThread(CountDownLatch latch,int dbId,String lastSyncTime,String syncTime,String dir,Map<Integer,Integer> stats){
			this.latch=latch;
			this.dbId=dbId;
			this.lastSyncTime=lastSyncTime;
			this.syncTime=syncTime;
			this.dir=dir;
			this.stats=stats;
		}
		
		@Override
		public void run() {
			Connection conn=null;
			PrintWriter pw = null;
			try{
				conn=DBConnector.getInstance().getConnectionById(dbId);
				pw = new PrintWriter(dir+syncTime+"_day_"+dbId+".txt");
				//获取有变更的数据pid
				LogReader lr = new LogReader(conn);
				//key:liftcyle,value:pids
				Map<Integer,Collection<Long>> updatePids = lr.getUpdatedObj(ObjectName.IX_POI, GlmFactory.getInstance().getObjByType(ObjectName.IX_POI).getMainTable().getName(), null, lastSyncTime,syncTime);
				//查询已提交的数据
				List<Long> pidList = new ArrayList<Long>();
				for(Map.Entry<Integer, Collection<Long>> entry:updatePids.entrySet()){
					if(entry.getValue()!=null&&entry.getValue().size()>0){
						pidList.addAll(PoiEditStatus.pidFilterByEditStatus(conn, entry.getValue(), 3));
					}
				}
				//设置查询子表
				Set<String> selConfig = new HashSet<String>();
				selConfig.add("IX_POI_NAME");
				selConfig.add("IX_POI_ADDRESS");
				selConfig.add("IX_POI_CONTACT");
				selConfig.add("IX_POI_RESTAURANT");
				selConfig.add("IX_POI_HOTEL");
				selConfig.add("IX_POI_DETAIL");
				selConfig.add("IX_POI_CHILDREN");
				selConfig.add("IX_POI_PARENT");
				selConfig.add("IX_POI_PARKING");
				selConfig.add("IX_POI_CHARGINGSTATION");
				selConfig.add("IX_POI_CHARGINGPLOT");
				selConfig.add("IX_POI_GASSTATION");
				//...
				if(pidList.size()>0){
					Map<Long,BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, selConfig, pidList, true, false);
					//设置lifeCycle
					for(Map.Entry<Long, BasicObj> entry:objs.entrySet()){
						for(Map.Entry<Integer, Collection<Long>> ent:updatePids.entrySet()){
							if(ent.getValue().contains(entry.getKey())){
								entry.getValue().setLifeCycle(ent.getKey());
								break;
							}
						}
					}
					//设置父fid
					 Map<Long, String> ParentFids = IxPoiSelector.getParentFidByPids(conn, objs.keySet());
					for(Map.Entry<Long, String> entry:ParentFids.entrySet()){
						((IxPoiObj)objs.get(entry.getKey())).setParentFid(entry.getValue());
					}
					//设置子fid
					Map<Long,List<Long>> objMap = new HashMap<Long, List<Long>>();
					for(BasicObj obj:objs.values()){
						IxPoiObj poi = (IxPoiObj) obj;
						List<Long> childPids = new ArrayList<Long>();
						List<BasicRow> rows = poi.getRowsByName("IX_POI_CHILDREN");
						if(rows!=null && rows.size()>0){
							for(BasicRow row:rows){
								IxPoiChildren children = (IxPoiChildren) row;
								childPids.add(children.getChildPoiPid());
							}
						}
						objMap.put(obj.objPid(), childPids);
					}
					Map<Long, List<Map<Long, Object>>> childFids = IxPoiSelector.getChildFidByPids(conn, objMap);
					for(Map.Entry<Long, List<Map<Long, Object>>> entry:childFids.entrySet()){
						((IxPoiObj)objs.get(entry.getKey())).setChildFid(entry.getValue());
					}
					//设置adminId
					 Map<Long,Long> adminIds = IxPoiSelector.getAdminIdByPids(conn, objs.keySet());
					for(Map.Entry<Long, Long> entry:adminIds.entrySet()){
						((IxPoiObj)objs.get(entry.getKey())).setAdminId(entry.getValue());
					}
					
					MultiSrcPoiConvertor conv = new MultiSrcPoiConvertor();
					for(BasicObj obj:objs.values()){
						pw.println(conv.toJson((IxPoiObj)obj).toString());
					}
					stats.put(dbId, objs.size());
				}else{
					stats.put(dbId, 0);
				}
				log.debug("dbId("+dbId+")转出成功。");
			}catch(Exception e){
				log.error(e.getMessage(),e);
				throw new ThreadExecuteException("dbId("+dbId+")转多源失败，同步时间范围为start("+lastSyncTime+"),end("+syncTime+")");
			}finally{
				DbUtils.closeQuietly(conn);
				try{
					if(pw!=null)pw.close();
				}catch(Exception ie){
					log.error(ie.getMessage(),ie);
				}
				if(latch!=null){
					latch.countDown();
				}
			}
		}
		
	}
}
