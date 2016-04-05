package com.navinfo.dataservice.jobframework.runjob;

import com.navinfo.dataservice.jobframework.JobFinder;

/** 
* @ClassName: JobFinderFromDB 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午5:05:49 
* @Description: TODO
*/
public class JobFinder4RunFromDB implements JobFinder {
	@Override
	public void startFinding() {
		// TODO Auto-generated method stub
		JobThreadPoolExecutor.getInstance().getCorePoolSize();
	}
	@Override
	public void stopFinding() {
		// TODO Auto-generated method stub
	}

}
