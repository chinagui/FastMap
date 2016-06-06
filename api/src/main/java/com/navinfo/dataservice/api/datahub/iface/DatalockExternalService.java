package com.navinfo.dataservice.api.datahub.iface;

import java.util.Set;

import com.navinfo.dataservice.api.ExternalService;

/** 
* @ClassName: IDatalockService 
* @author Xiao Xiaowen 
* @date 2016年3月21日 下午5:52:21 
* @Description: TODO
*/
public interface DatalockExternalService extends ExternalService{
	Set<Integer> query(int prjId,Set<Integer> meshes)throws Exception;
	String test(String name)throws Exception;
}
