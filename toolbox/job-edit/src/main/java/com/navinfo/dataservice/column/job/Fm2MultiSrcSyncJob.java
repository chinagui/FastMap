package com.navinfo.dataservice.column.job;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.commons.util.ZipUtils;
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
	private void shutDownPoolExecutor(){log.debug("关闭线程池");
	if (threadPoolExecutor != null && !threadPoolExecutor.isShutdown()) {
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
		PrintWriter pw = null;
		try{
			Fm2MultiSrcSyncJobRequest req = (Fm2MultiSrcSyncJobRequest)request;
			String lastSyncTime=req.getLastSyncTime();
			String syncTime=req.getSyncTime();
			//1. 生成目录
			String rootDownloadPath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.downloadFilePathRoot);
			//每个月独立目录
			String curYm = DateUtils.getCurYyyymm();
			String monthDir = rootDownloadPath+"multisrc"+File.separator+curYm+File.separator;
			File mdirFile = new File(monthDir);
			if(!mdirFile.exists()){
				mdirFile.mkdirs();
			}
			//月下一次同步的文件放在自己的目录下
			String mydir = monthDir+syncTime+"_day"+File.separator;
			File file = new File(mydir);
			file.mkdirs();
			//2.多线程执行导出单库数据到文本
			log.debug("开始执导出数据到文件");
			Map<Integer,Integer> stats = new ConcurrentHashMap<Integer,Integer>();
			long t = System.currentTimeMillis();
			int dbSize = req.dbIds.size();
			if(dbSize==1){
				new Fm2MultiSrcExportThread(null,req.dbIds.get(0),lastSyncTime,syncTime,mydir,stats).run();
			}else{
				if(dbSize>10){
					initThreadPool(10);
				}else{
					initThreadPool(dbSize);
				}
				final CountDownLatch latch = new CountDownLatch(dbSize);
				threadPoolExecutor.addDoneSignal(latch);
				// 执行转数据
				for(int dbId:req.dbIds){
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
			//
			//5.组装URL，通知多源平台
			String zipUrl = "multisrc/"+curYm+"/"+syncTime+"_day.zip";
			notifyMultiSrc(zipUrl);
			super.response("通知多源完成", null);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}finally {
			if(pw!=null){
				pw.close();
			}
			shutDownPoolExecutor();
		}
	}
	private void notifyMultiSrc(String zipFile){
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
		}catch(Exception e){
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
			try{
				conn=DBConnector.getInstance().getConnectionById(dbId);
				//...
				stats.put(dbId, 1000);
				log.debug("dbId("+dbId+")转出成功。");
			}catch(Exception e){
				log.error(e.getMessage(),e);
				destroyExpFile();
				throw new ThreadExecuteException("dbId("+dbId+")转多源失败，同步时间范围为start("+lastSyncTime+"),end("+syncTime+")");
			}finally{
				DbUtils.closeQuietly(conn);
				if(latch!=null){
					latch.countDown();
				}
			}
		}
		
		private void destroyExpFile(){
			try{
				
			}catch(Exception e){
				log.error(e.getMessage(), e);
			}
		}
		
	}
}
