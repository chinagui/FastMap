package com.navinfo.dataservice.jobframework;

import java.util.Set;

import com.navinfo.dataservice.api.job.model.JobMsgType;

/** 
* @ClassName: JobFinder 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午4:58:57 
* @Description: TODO
*/
public interface JobFinder {
	
	void startFinding(JobMsgType jobMsgType)throws Exception;
	void stopFinding(JobMsgType jobMsgType)throws Exception;
}
