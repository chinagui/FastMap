package com.navinfo.dataservice.jobframework;


/** 
* @ClassName: JobFinderFromDB 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午5:05:49 
* @Description: TODO
*/
public class JobFinderFromDB implements JobFinder {
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
