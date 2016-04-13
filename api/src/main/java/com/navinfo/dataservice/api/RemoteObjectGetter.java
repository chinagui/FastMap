package com.navinfo.dataservice.api;

/** 
* @ClassName: RemoteObjectGetter 
* @author Xiao Xiaowen 
* @date 2016年3月23日 上午11:17:50 
* @Description: TODO
*/
public interface RemoteObjectGetter {
	Object getRemoteObject(String name)throws ServiceException;
	Object getRemoteObject(String name,Class type)throws ServiceException;
}
