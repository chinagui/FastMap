package com.navinfo.dataservice.expcore.job;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/** 
* @ClassName: GdbExportJob 
* @author Xiao Xiaowen 
* @date 2016年6月14日 上午9:48:03 
* @Description: TODO
*  
*/
public class GdbExportJob extends AbstractJob {

	public GdbExportJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		// TODO Auto-generated method stub

	}

}
