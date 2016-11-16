package com.navinfo.dataservice.column.job;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/** 
 * @ClassName: Fm2MultiSrcSyncJob
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: Fm2MultiSrcSyncJob.java
 */
public class Fm2MultiSrcSyncJob extends AbstractJob {


	public Fm2MultiSrcSyncJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		//
		String dir = DateUtils.getCurYmd();
		String file = "";
		
		//URL
		
		String url = SystemConfigFactory.getSystemConfig().getValue(PropConstant.serverUrl)+"/resources/download/multisrc/"+dir+"/"+file;
	}

}
