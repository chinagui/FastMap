package com.navinfo.dataservice.jobframework;

import java.util.Set;

/** 
* @ClassName: JobFinder 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午4:58:57 
* @Description: TODO
*/
public interface JobFinder {
	
	void startFinding()throws Exception;
	void stopFinding()throws Exception;
}
