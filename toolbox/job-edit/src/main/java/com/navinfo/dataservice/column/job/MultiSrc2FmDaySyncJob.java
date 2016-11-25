package com.navinfo.dataservice.column.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.edit.upload.UploadPois;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.column.job.Fm2MultiSrcSyncJob.Fm2MultiSrcExportThread;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.download.DownloadUtils;
import com.navinfo.navicommons.exception.ServiceRtException;
import com.navinfo.navicommons.exception.ThreadExecuteException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: MultiSrc2FmDaySyncJob
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: MultiSrc2FmDaySyncJob.java
 */
public class MultiSrc2FmDaySyncJob extends AbstractJob {
	
	protected VMThreadPoolExecutor threadPoolExecutor;
	
	public MultiSrc2FmDaySyncJob(JobInfo jobInfo) {
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
			MultiSrc2FmDaySyncJobRequest req = (MultiSrc2FmDaySyncJobRequest)request;
			//下载解压远程文件包
			String localUnzipDir = downloadAndUnzip(req.getRemoteZipFile());
			response("下载文件完成",null);
			//读取文件
			JSONArray pois = read(localUnzipDir);
			response("读取文件完成",null);
			//导入
			//先分库
			Map<Integer,UploadPois> poiMap = new HashMap<Integer,UploadPois>();//key:大区id
			for(Object o:pois){
				JSONObject poi=(JSONObject)o;
				String adminId=poi.getString("adminId");
				int regionId = Integer.parseInt(adminId.substring(0, 2));
				UploadPois upoi = poiMap.get(regionId);
				if(upoi==null){
					upoi=new UploadPois();
					poiMap.put(regionId, upoi);
				}
				upoi.addJsonPoi(poi);
			}
			long t = System.currentTimeMillis();
			int dbSize = poiMap.size();
			if(dbSize==0)return;
			if(poiMap.size()==1){
				Map.Entry<Integer,UploadPois> entry = poiMap.entrySet().iterator().next();
				new MultiSrc2FmDayThread(null,entry.getKey(),entry.getValue()).run();;
			}else{
				if(dbSize>10){
					initThreadPool(10);
				}else{
					initThreadPool(dbSize);
				}
				final CountDownLatch latch = new CountDownLatch(dbSize);
				threadPoolExecutor.addDoneSignal(latch);// 执行转数据
				for(Map.Entry<Integer, UploadPois> entry:poiMap.entrySet()){
					threadPoolExecutor.execute(new MultiSrc2FmDayThread(latch,entry.getKey(),entry.getValue()));
				}
				latch.await();
				if (threadPoolExecutor.getExceptions().size() > 0) {
					throw new Exception(threadPoolExecutor.getExceptions().get(0));
				}
			}
			log.debug("导入完成");
			response("导入完成",null);
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
	private String downloadAndUnzip(String remoteZipFile)throws Exception{
		String uploadRoot = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.uploadPath);
		//每个月独立目录
		String curYm = DateUtils.getCurYyyymm();
		String monthDir = uploadRoot+"multisrc"+File.separator+curYm+File.separator;
		File mdirFile = new File(monthDir);
		if(!mdirFile.exists()){
			mdirFile.mkdirs();
		}
		//获取zip包名
		String fileName = remoteZipFile.substring(remoteZipFile.lastIndexOf("/"));
		//下载
		String localZipFile = monthDir+fileName;
		DownloadUtils.download(remoteZipFile,localZipFile);
		//解压
		String localUnzipDir = monthDir+fileName.substring(0,fileName.indexOf("."));
		ZipUtils.unzipFile(localZipFile,localUnzipDir);
		return localUnzipDir;
	}
	private JSONArray read(String localUnzipDir)throws Exception{
		Scanner lines = null;
		try{
			JSONArray pois = new JSONArray();
			File dataDir = new File(localUnzipDir);
			if (!dataDir.exists()) {
				throw new Exception("文件目录不存在");
			}
			File[] files = dataDir.listFiles();
			for(File file:files){
				if(file.isFile()&&file.getName().endsWith("day.txt")){
					lines = new Scanner(new FileInputStream(file));
					while (lines.hasNextLine()) {
						String line = lines.nextLine();
						JSONObject poi = JSONObject.fromObject(line);
						pois.add(poi);
					}
					break;
				}
			}
			return pois;
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			if(lines!=null){
				lines.close();
			}
		}
	}
	
	class MultiSrc2FmDayThread implements Runnable{
		CountDownLatch latch = null;
		int regionId=0;
		UploadPois pois=null;
		MultiSrc2FmDayThread(CountDownLatch latch,int regionId,UploadPois pois){
			this.latch=latch;
			this.regionId=regionId;
			this.pois=pois;
		}
		
		@Override
		public void run() {
			Connection conn=null;
			try{
				//region-->dbId
				int dbId=0;
				conn=DBConnector.getInstance().getConnectionById(dbId);
				//OperationResult or = new OperationResult();
				//MultiSrcPoiDayImportor
				//PoiRelationImportor
				log.debug("dbId("+dbId+")转出成功。");
			}catch(Exception e){
				log.error(e.getMessage(),e);
				destroyExpFile();
				throw new ThreadExecuteException("");
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
