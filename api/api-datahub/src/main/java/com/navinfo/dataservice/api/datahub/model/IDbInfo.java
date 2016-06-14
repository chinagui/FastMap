package com.navinfo.dataservice.api.datahub.model;

import java.util.Date;

/** 
* @ClassName: IDbInfo 
* @author Xiao Xiaowen 
* @date 2016年6月14日 下午3:06:10 
* @Description: TODO
*  
*/
public interface IDbInfo {
	int getDbId() ;

	String getDbName() ;

	String getDbUserName() ;

	String getDbUserPasswd() ;

	int getDbRole() ;

	IDbServer getDbServer() ;
}
