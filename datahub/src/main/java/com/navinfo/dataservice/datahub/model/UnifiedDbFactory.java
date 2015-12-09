package com.navinfo.dataservice.datahub.model;

import com.navinfo.dataservice.commons.database.oracle.DbServerType;

/** 
 * @ClassName: UnifiedDbFactory 
 * @author Xiao Xiaowen 
 * @date 2015-12-7 上午10:02:07 
 * @Description: TODO
 */
public class UnifiedDbFactory {
	private static class SingletonHolder{
		private static final UnifiedDbFactory INSTANCE =new UnifiedDbFactory();
	}
	public static UnifiedDbFactory getInstance(){
		return SingletonHolder.INSTANCE;
	}
	private UnifiedDbFactory(){}
	public UnifiedDb create(String dbServerType){
		UnifiedDb db = null;
		if(DbServerType.TYPE_MONGODB.equals(dbServerType)){
			db = new MongoDb();
		}else if(DbServerType.TYPE_ORACLE.equals(dbServerType)){
			db = new OracleSchema();
		}
		return db;
	}
}
