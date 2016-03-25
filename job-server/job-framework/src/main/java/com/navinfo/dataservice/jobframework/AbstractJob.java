package com.navinfo.dataservice.jobframework;

/** 
* @ClassName: AbstractJob 
* @author Xiao Xiaowen 
* @date 2016年3月25日 下午4:12:36 
* @Description: TODO
*/
public abstract class AbstractJob implements Runnable {

	protected JobInfo jobInfo;
	AbstractJob(JobInfo jobInfo){
		this.jobInfo=jobInfo;
	}
	@Override
	public void run() {
		

	}

}
