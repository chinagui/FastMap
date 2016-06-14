package com.navinfo.dataservice.api.datahub.model;

import java.util.Set;

/** 
* @ClassName: IDbServer 
* @author Xiao Xiaowen 
* @date 2016年6月14日 下午3:08:33 
* @Description: TODO
*  
*/
public interface IDbServer {

	String getType() ;

	String getIp();
	
	int getPort() ;

}
