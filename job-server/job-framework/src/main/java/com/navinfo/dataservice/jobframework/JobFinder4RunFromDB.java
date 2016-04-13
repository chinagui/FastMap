package com.navinfo.dataservice.jobframework;

import com.navinfo.dataservice.api.job.model.JobMsgType;
import com.navinfo.dataservice.jobframework.runjob.JobThreadPoolExecutor;

/** 
* @ClassName: JobFinderFromDB 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午5:05:49 
* @Description: TODO
*/
public class JobFinder4RunFromDB implements JobFinder {
	@Override
	public void startFinding(JobMsgType jobMshType) {
		// TODO Auto-generated method stub
		JobThreadPoolExecutor.getInstance().getCorePoolSize();
	}
	@Override
	public void stopFinding(JobMsgType jobMshType) {
		// TODO Auto-generated method stub
	}

}
