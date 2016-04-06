package com.navinfo.dataservice.api.datalock.iface;

import java.util.Set;

import com.navinfo.dataservice.api.RemoteServiceException;

/** 
* @ClassName: IDatalockService 
* @author Xiao Xiaowen 
* @date 2016年3月21日 下午5:52:21 
* @Description: TODO
*/
public interface DatalockService {
	Set<Integer> query(int prjId,Set<Integer> meshes)throws RemoteServiceException;
	String test(String name)throws RemoteServiceException;
}
