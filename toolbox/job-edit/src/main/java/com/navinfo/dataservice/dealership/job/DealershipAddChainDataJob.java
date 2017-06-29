package com.navinfo.dataservice.dealership.job;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

import net.sf.json.JSONObject;

/**
 * @ClassName: DealershipAddChainDataJob
 * @author 宋鹤
 * 
 */
public class DealershipAddChainDataJob extends AbstractJob {

	public DealershipAddChainDataJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		DealershipAddChainDataJobRequest req = (DealershipAddChainDataJobRequest) this.request;
		try {
			JobInfo addChainDatajobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
			AbstractJob addChainDatajob = JobCreateStrategy.createAsSubJob(addChainDatajobInfo,
					req.getSubJobRequest("DealershipTableAndDbDiffJob"), this);
			addChainDatajob.run();
			if (addChainDatajob.getJobInfo().getStatus()!= 3) {
				String msg = (addChainDatajob.getException()==null)?"未知错误。":"错误："+addChainDatajob.getException().getMessage();
				throw new Exception("补充数据调用库查分job内部发生"+msg);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}
	}
}
