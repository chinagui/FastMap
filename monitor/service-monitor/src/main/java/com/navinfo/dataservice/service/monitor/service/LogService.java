package com.navinfo.dataservice.service.monitor.service;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.service.monitor.model.FosLog;

/** 
 * @ClassName: LogService
 * @author xiaoxiaowen4127
 * @date 2017年8月10日
 * @Description: LogService.java
 */
public class LogService {
	private volatile static LogService instance;
	public static LogService getInstance(){
		if(instance==null){
			synchronized(LogService.class){
				if(instance==null){
					instance=new LogService();
				}
			}
		}
		return instance;
	}
	private LogService(){}
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	public FosLog getByJobId(int jobId)throws Exception{
		FosLog log = new FosLog(String.valueOf(jobId));
		//...
		return log; 
	}

	public FosLog getByRequestCode(String jobId)throws Exception{
		FosLog log = new FosLog(String.valueOf(jobId));
		//...
		return log; 
	}
}
