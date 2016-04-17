package com.navinfo.dataservice.api;


import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.datalock.iface.DatalockExternalService;
import com.navinfo.dataservice.api.job.iface.JobExternalService;

/** 
* @ClassName: RemoteServices 
* @author Xiao Xiaowen 
* @date 2016年3月23日 下午5:03:02 
* @Description: TODO
*/
@Service("remoteServices")
public class RemoteServices {
	
	@Resource(name="remoteObjectGetter")
	private RemoteObjectGetter remoteObjectGetter ;
	
	public  RemoteObjectGetter getRemoteObjectGetter() {
		return remoteObjectGetter;
	}

	public void setRemoteObjectGetter(RemoteObjectGetter remoteObjectGetter) {
		this.remoteObjectGetter = remoteObjectGetter;
	}
	
	public DatalockExternalService getDatalockExternalService()throws ServiceException{
		return (DatalockExternalService)remoteObjectGetter.getRemoteObject("datalockExternalService");
	}
	public JobExternalService getJobExternalService()throws ServiceException{
		return (JobExternalService)remoteObjectGetter.getRemoteObject("jobExternalService");
	}
}
