package com.navinfo.dataservice.api.edit.iface;

import java.util.Set;

/** 
* @ClassName: DatalockApiService 
* @author Xiao Xiaowen 
* @date 2016年6月6日 下午8:31:32 
* @Description: TODO
*  
*/
public interface DatalockApiService {
	Set<Integer> query(int prjId,Set<Integer> meshes)throws Exception;
	String test(String name)throws Exception;
}
