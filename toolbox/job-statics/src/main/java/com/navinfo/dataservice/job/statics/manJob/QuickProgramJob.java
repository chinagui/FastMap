package com.navinfo.dataservice.job.statics.manJob;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;

import net.sf.json.JSONObject;

/**
 * 项目统计
 * @ClassName ProgramJob
 * @author songhe
 * @date 2017年9月4日
 * 
 */
public class QuickProgramJob extends AbstractStatJob {
	public QuickProgramJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public String stat() throws JobException {
		try {
			//获取统计时间
			QuickProgramJobRequest statReq = (QuickProgramJobRequest)request;
			log.info("start stat "+statReq.getJobType());
			ProgramJobUtils util=new ProgramJobUtils();
			JSONObject result = util.stat(statReq.getTimestamp(), statReq.getType());
			log.info("end stat "+statReq.getJobType());
			return result.toString();
			
		} catch (Exception e) {
			log.error("任务统计:"+e.getMessage(), e);
			throw new JobException("任务统计:"+e.getMessage(),e);
		}
	}

}
