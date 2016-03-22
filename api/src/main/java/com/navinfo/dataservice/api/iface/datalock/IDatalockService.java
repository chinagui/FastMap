package com.navinfo.dataservice.api.iface.datalock;

import java.util.Set;

import com.navinfo.dataservice.api.ServiceException;

/** 
* @ClassName: IDatalockService 
* @author Xiao Xiaowen 
* @date 2016年3月21日 下午5:52:21 
* @Description: TODO
*/
public interface IDatalockService {
	Set<Integer> query(int prjId,Set<Integer> meshes)throws ServiceException;
}
