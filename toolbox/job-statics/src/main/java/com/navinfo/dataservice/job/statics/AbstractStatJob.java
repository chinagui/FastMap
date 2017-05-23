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
		try{
			result = stat();
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}finally{
			//send stat result to MQ
			try{
//				JobMsgPublisher.sendStatJobResult(request.getJobType(),)
			}catch(Exception e){
				log.warn("注意：");
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
