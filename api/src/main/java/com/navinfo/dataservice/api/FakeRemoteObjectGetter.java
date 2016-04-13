package com.navinfo.dataservice.api;

import org.springframework.stereotype.Service;

/** 
* @ClassName: FakeRemoteObjectGetter 
* @author Xiao Xiaowen 
* @date 2016年3月23日 上午11:22:53 
* @Description: TODO
*/
@Service("remoteObjectGetter")
public class FakeRemoteObjectGetter implements  RemoteObjectGetter {

	@Override
	public Object getRemoteObject(String name) throws ServiceException {
		try{
			return ApplicationContextUtil.getBean(name);
		}catch(Exception e){
			throw new ServiceException(e.getMessage(),e);
		}
	}

	@Override
	public Object getRemoteObject(String name, Class type) throws ServiceException {
		try{
			return ApplicationContextUtil.getBean(name, type);
		}catch(Exception e){
			throw new ServiceException(e.getMessage(),e);
		}
	}


}
