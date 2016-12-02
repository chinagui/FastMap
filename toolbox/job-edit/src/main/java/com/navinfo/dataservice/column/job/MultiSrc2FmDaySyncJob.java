package com.navinfo.dataservice.column.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.edit.iface.FmMultiSrcSyncApi;
import com.navinfo.dataservice.api.edit.model.FmMultiSrcSync;
import com.navinfo.dataservice.api.edit.model.MultiSrcFmSync;
import com.navinfo.dataservice.api.edit.upload.UploadPois;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.dao.plus.editman.PoiEditStatus;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.engine.editplus.operation.imp.MultiSrcPoiDayImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.MultiSrcPoiDayImportorCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.PoiRelationImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.PoiRelationImportorCommand;
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
	
	Map<String,String> errLog=new ConcurrentHashMap<String,String>();
	
	public MultiSrc2FmDaySyncJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		
		FmMultiSrcSyncApi syncApi;
		try{
			syncApi = (FmMultiSrcSyncApi)ApplicationContextUtil
				.getBean("fmMultiSrcSyncApi");
			MultiSrc2FmDaySyncJobRequest req = (MultiSrc2FmDaySyncJobRequest)request;
			//下载解压远程文件包
			String localUnzipDir = downloadAndUnzip(syncApi,req.getRemoteZipFile());
			response("下载文件完成",null);
			//执行导入
			imp(syncApi,localUnzipDir);
			response("导入完成",null);
			//写导入统计结果并生成zip文件下载url
			String resFileName = localUnzipDir.substring(localUnzipDir.lastIndexOf(File.separator), localUnzipDir.length())+"_res.txt";
			String zipFile = writeImpResFile(syncApi,resFileName);
			response("生成统计结果完成",null);
			//通知多源
			notifyMultiSrc(zipFile,syncApi);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}finally {
			shutDownPoolExecutor();
		}
		
	}
	
	private String downloadAndUnzip(FmMultiSrcSyncApi syncApi,String remoteZipFile)throws Exception{
		try{
//			String uploadRoot = SystemConfigFactory.getSystemConfig().getValue(PropConstant.uploadPath);
			String uploadRoot = "F:\\data\\multisrc\\upload\\";
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
			log.debug("下载完成");
			//解压
			String localUnzipDir = monthDir+fileName.substring(0,fileName.indexOf("."));
			ZipUtils.unzipFile(localZipFile,localUnzipDir);
			log.debug("解压完成");
			//设置下载成功状态
			syncApi.updateMultiSrcFmSyncStatus(MultiSrcFmSync.STATUS_DOWNLOAD_SUCCESS,jobInfo.getId());
			return localUnzipDir;
		}catch(Exception e){
			log.error(e.getMessage(),e);
			//设置下载失败状态
			syncApi.updateMultiSrcFmSyncStatus(MultiSrcFmSync.STATUS_DOWNLOAD_FAIL,jobInfo.getId());
			throw e;
		}
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
	private Map<Integer,UploadPois> distribute(JSONArray pois)throws Exception{
		Map<Integer,UploadPois> poiMap = new HashMap<Integer,UploadPois>();//key:大区dbid
		ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
		//key:admincode,value:day dbid
		Map<Integer,Integer> adminDbMap = manApi.listDayDbIdsByAdminId();
		for(Object o:pois){
			JSONObject poi=(JSONObject)o;
			try{
				int adminId=Integer.parseInt(poi.getString("adminId").substring(0, 2));
				adminId=adminId*10000;
				int dbId = 0;
				if(adminDbMap.containsKey(adminId)){
					dbId = adminDbMap.get(adminId);
					UploadPois upoi = poiMap.get(dbId);
					if(upoi==null){
						upoi=new UploadPois();
						poiMap.put(dbId, upoi);
					}
					upoi.addJsonPoi(poi);
				}else{
					errLog.put(poi.getString("fid"), adminId+"的大区库未找到");
				}
			}catch(NumberFormatException e){
				log.warn(e.getMessage(),e);
				errLog.put(poi.getString("fid"), "adminId格式不正确");
			}
		}
		log.debug("分发数据完成");
		return poiMap;
	}
	private void imp(FmMultiSrcSyncApi syncApi,String localUnzipDir)throws Exception{
		try{
			long t = System.currentTimeMillis();
			
			//读取文件
			JSONArray pois = read(localUnzipDir);
			response("读取文件完成",null);

			//分库
			Map<Integer,UploadPois> poiMap = distribute(pois);
			
			//执行导入
			int dbSize = poiMap.size();
			if(dbSize==0){
				log.debug("无数据需要导入，导入结束");
				return;
			}
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
				threadPoolExecutor.addDoneSignal(latch);
				// 执行转数据
				for(Map.Entry<Integer, UploadPois> entry:poiMap.entrySet()){
					threadPoolExecutor.execute(new MultiSrc2FmDayThread(latch,entry.getKey(),entry.getValue()));
				}
				latch.await();
				if (threadPoolExecutor.getExceptions().size() > 0) {
					throw new Exception(threadPoolExecutor.getExceptions().get(0));
				}
			}
			//设置导入成功状态
			syncApi.updateMultiSrcFmSyncStatus(MultiSrcFmSync.STATUS_IMP_SUCCESS,jobInfo.getId());
			log.debug("导入完成，用时"+((System.currentTimeMillis()-t)/1000)+"s");
		}catch(Exception e){
			log.error(e.getMessage(),e);
			//设置导入失败状态
			syncApi.updateMultiSrcFmSyncStatus(MultiSrcFmSync.STATUS_IMP_FAIL,jobInfo.getId());
			throw e;
		}
	}
	private String writeImpResFile(FmMultiSrcSyncApi syncApi,String resFileName)throws Exception{
		PrintWriter pw = null;
		try{
			String rootDownloadPath = SystemConfigFactory.getSystemConfig().getValue(PropConstant.downloadFilePathRoot);
			String curYm = DateUtils.getCurYyyymm();
			//每个月独立目录
			String monthDir = rootDownloadPath+"multisrc"+File.separator+curYm+File.separator;
			File mdirFile = new File(monthDir);
			if(!mdirFile.exists()){
				mdirFile.mkdirs();
			}
			pw = new PrintWriter(monthDir+resFileName);
			pw.println(JSONObject.fromObject(errLog).toString());
			//设置生成导入结果成功状态
			syncApi.updateMultiSrcFmSyncStatus(MultiSrcFmSync.STATUS_CREATE_RES_SUCCESS,jobInfo.getId());
			return "multisrc"+File.separator+curYm+File.separator+resFileName;
		}catch(Exception e){
			log.error(e.getMessage(),e);
			//设置生成导入结果失败状态
			syncApi.updateMultiSrcFmSyncStatus(MultiSrcFmSync.STATUS_CREATE_RES_FAIL,jobInfo.getId());
			throw e;
		}finally{
			if(pw!=null){
				pw.close();
			}
		}
	}

	private void notifyMultiSrc(String zipFile,FmMultiSrcSyncApi syncApi){
		try{
			//
			log.debug("开始通知多源平台");
			String zipFileUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.serverUrl)
					+SystemConfigFactory.getSystemConfig().getValue(PropConstant.downloadUrlPathRoot)
					+zipFile;
			log.debug("导入的统计数据包url:"+zipFileUrl);
			Map<String,String> parMap = new HashMap<String,String>();
			parMap.put("url", zipFileUrl);
			String result = ServiceInvokeUtil.invoke("", parMap, 10000);
			log.debug("notify multisrc result:"+result);
			syncApi.updateMultiSrcFmSyncStatus(MultiSrcFmSync.STATUS_NOTIFY_SUCCESS,jobInfo.getId());
		}catch(Exception e){
			try{
				syncApi.updateMultiSrcFmSyncStatus(MultiSrcFmSync.STATUS_NOTIFY_FAIL,jobInfo.getId());
			}catch(Exception ex){
				log.error(ex.getMessage(),ex);
			}
			log.warn("日库同步数据包已生成，通知多源平台时发生错误，请联系多源平台运维");
			log.error(e.getMessage(),e);
		}
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
	class MultiSrc2FmDayThread implements Runnable{
		CountDownLatch latch = null;
		int dbId=0;
		UploadPois pois=null;
		MultiSrc2FmDayThread(CountDownLatch latch,int dbId,UploadPois pois){
			this.latch=latch;
			this.dbId=dbId;
			this.pois=pois;
		}
		
		@Override
		public void run() {
			Connection conn=null;
			try{
				conn=DBConnector.getInstance().getConnectionById(dbId);
				//导入数据
				MultiSrcPoiDayImportorCommand cmd = new MultiSrcPoiDayImportorCommand(pois);
				MultiSrcPoiDayImportor imp = new MultiSrcPoiDayImportor(conn,null);
				imp.operate(cmd);
				imp.persistChangeLog(OperationSegment.SG_ROW, jobInfo.getUserId());
				//数据打多源标识
				PoiEditStatus.tagMultiSrcPoi(conn, imp.getSourceTypes());
				//导入父子关系
				PoiRelationImportorCommand relCmd = new PoiRelationImportorCommand();
				relCmd.setPoiRels(imp.getParentPid());
				PoiRelationImportor relImp = new PoiRelationImportor(conn,imp.getResult());
				relImp.operate(relCmd);
				relImp.persistChangeLog(OperationSegment.SG_ROW, jobInfo.getUserId());
				errLog.putAll(imp.getErrLog());
				log.debug("dbId("+dbId+")转入成功。");
			}catch(Exception e){
				DbUtils.rollbackAndCloseQuietly(conn);
				log.error(e.getMessage(),e);
				throw new ThreadExecuteException("");
			}finally{
				DbUtils.commitAndCloseQuietly(conn);
				if(latch!=null){
					latch.countDown();
				}
			}
		}
		
	}
}
