package com.navinfo.dataservice.api;

import org.springframework.beans.factory.annotation.Autowired;

import com.navinfo.dataservice.api.iface.datalock.DatalockService;

/** 
* @ClassName: RemoteServices 
* @author Xiao Xiaowen 
* @date 2016年3月23日 下午5:03:02 
* @Description: TODO
*/
public class RemoteServices {
	private static RemoteObjectGetter getter =new FakeRemoteObjectGetter();
	public static DatalockService getDatalockService()throws RemoteServiceException{
		return (DatalockService)getter.getRemoteObject("datalockService");
	}
}
