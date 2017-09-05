package com.navinfo.dataservice.job.statics;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.dao.mq.job.JobMsgPublisher;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/** 
 * @ClassName: AbstractStatJob
 * @author xiaoxiaowen4127
 * @date 2017年5月22日
 * @Description: AbstractStatJob.java
 */
public abstract class AbstractStatJob extends AbstractJob{

	public AbstractStatJob(JobInfo jobInfo) {
		super(jobInfo);
	}
	
	public void execute()throws JobException{
		String result = null;
		AbstractStatJobRequest statReq = (AbstractStatJobRequest)request;
		try{
			result = stat();
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			//send stat result to MQ
			try{
				JobMsgPublisher.sendStatJobResult(statReq.getJobType(),statReq.getIdentify(),result,jobInfo.getId());
			}catch(Exception e){
				log.warn("注意，统计结果未成功发送，原因："+e.getMessage());
				log.error(e.getMessage(),e);;
			}
		}
	}
	
	/**
	 * 
	 * @return:统计结果的字符串
	 * @throws JobException
	 */
	public abstract String stat()throws JobException;

}
