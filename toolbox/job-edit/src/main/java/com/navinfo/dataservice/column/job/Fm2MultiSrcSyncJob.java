package com.navinfo.dataservice.column.job;

import java.sql.Connection;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.thread.VMThreadPoolExecutor;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.exception.ThreadExecuteException;

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
        threadPoolExecutor = new VMThreadPoolExecutor(poolSize,
        		poolSize,
				3,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue(),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}
	private void shutDownPoolExecutor(){
		
	}

	@Override
	public void execute() throws JobException {
		try{
			Fm2MultiSrcSyncJobRequest req = (Fm2MultiSrcSyncJobRequest)request;
			//
			int poolSize = req.dbIds.size();
			if(poolSize>1){
				if(poolSize<10){
					initThreadPool(poolSize);
				}else{
					initThreadPool(10);
				}
			}else{
				
			}
			String dir = DateUtils.getCurYmd();
			String file = "";
			
			//URL
			
			String url = SystemConfigFactory.getSystemConfig().getValue(PropConstant.serverUrl)+"/resources/download/multisrc/"+dir+"/"+file;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}finally {
			shutDownPoolExecutor();
		}
	}
	class Fm2MultiSrcExportThread implements Runnable{
		CountDownLatch latch = null;
		int dbId=0;
		Date lastSyncTime=null;
		Date syncTime=null;
		Fm2MultiSrcExportThread(CountDownLatch latch,int dbId,Date lastSyncTime,Date syncTime){
			this.latch=latch;
			this.dbId=dbId;
			this.lastSyncTime=lastSyncTime;
			this.syncTime=syncTime;
		}
		
		@Override
		public void run() {
			Connection conn=null;
			try{
				conn=DBConnector.getInstance().getConnectionById(dbId);
				
			}catch(Exception e){
				log.error(e.getMessage(),e);
				destroyExpFile();
				throw new ThreadExecuteException("dbId("+dbId+")转多源失败，同步时间范围为start("+DateUtils.dateToString(lastSyncTime, DateUtils.DATE_COMPACTED_FORMAT)+"),end("+DateUtils.dateToString(syncTime,DateUtils.DATE_COMPACTED_FORMAT)+")");
			}finally{
				//
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
