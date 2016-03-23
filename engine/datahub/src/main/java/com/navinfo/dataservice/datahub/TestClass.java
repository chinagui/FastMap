package com.navinfo.dataservice.datahub;


import com.navinfo.dataservice.api.RemoteServices;
import com.navinfo.dataservice.api.iface.datalock.DatalockService;

/** 
* @ClassName: TestClass 
* @author Xiao Xiaowen 
* @date 2016年3月22日 下午6:15:16 
* @Description: TODO
*/
public class TestClass {
	public String test(String name)throws Exception{
		return RemoteServices.getDatalockService().test(name);
	}
}	
