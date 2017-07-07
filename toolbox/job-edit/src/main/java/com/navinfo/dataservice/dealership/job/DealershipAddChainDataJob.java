package com.navinfo.dataservice.dealership.job;

import java.util.List;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.control.dealership.service.DataEditService;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;

/**
 * @ClassName: DealershipAddChainDataJob
 * @author 宋鹤
 * 
 */
public class DealershipAddChainDataJob extends AbstractJob {

	public DealershipAddChainDataJob(JobInfo jobInfo) {
		super(jobInfo);
	}
	
	private DataEditService dealerShipEditService = DataEditService.getInstance();

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
			long userId = req.getUserId();
			List<String> chainCodeList = req.getChainCodeList();
			//这里修改为list吧，可扩展性强点，避免以后修改为支持多品牌还得该
			for(String chainCode : chainCodeList){
				log.info("chainCode:"+chainCode+",    userId:"+userId+"开始执行启动录入作业");
				dealerShipEditService.startWork(chainCode, userId);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(), e);
		}
	}
}