package com.navinfo.dataservice.dealership.job;

import java.util.List;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.control.dealership.service.DataEditService;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

/**
 * @ClassName: DealershipLiveUpdateJob
 * @author 孙佳伟
 * 
 */
public class DealershipLiveUpdateJob extends AbstractJob {
	private DataEditService dealerShipEditService = DataEditService.getInstance();
	
	public DealershipLiveUpdateJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		DealershipLiveUpdateJobRequest req = (DealershipLiveUpdateJobRequest) this.request;
		try {
			JobInfo liveUpdateJobInfo = new JobInfo(jobInfo.getId(), jobInfo.getGuid());
			AbstractJob liveUpdateJob = JobCreateStrategy.createAsSubJob(liveUpdateJobInfo,
					req.getSubJobRequest("DealershipTableAndDbDiffJob"), this);
			liveUpdateJob.run();
			if (liveUpdateJob.getJobInfo().getStatus()!= 3) {
				String msg = (liveUpdateJob.getException()==null)?"未知错误。":"错误："+liveUpdateJob.getException().getMessage();
				throw new Exception("实时更新调用库查分job内部发生"+msg);
			}
			List<String> chainCodeList = req.getChainCodeList();
			int userId = req.getUserId();
			for (String chainCode : chainCodeList) {
				dealerShipEditService.startWork(chainCode, userId);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}
	}
}
