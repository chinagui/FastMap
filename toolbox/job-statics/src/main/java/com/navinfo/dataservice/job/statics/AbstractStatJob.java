package com.navinfo.dataservice.job.statics;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.mq.job.JobMsgPublisher;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

import net.sf.json.JSONObject;

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
				String identify=statReq.getIdentify();
				if(StringUtils.isEmpty(identify)){
					identify=statReq.getTimestamp();
				}
				JSONObject identifyJson=statReq.getIdentifyJson();
				if(identifyJson==null||identifyJson.size()==0){
					identifyJson=new JSONObject();
					identifyJson.put("timestamp", statReq.getTimestamp());
				}
				JobMsgPublisher.sendStatJobResult(statReq.getJobType(),statReq.getTimestamp(),identify,identifyJson,result,jobInfo.getId());
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
