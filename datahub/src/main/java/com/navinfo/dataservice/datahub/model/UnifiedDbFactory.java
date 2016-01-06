package com.navinfo.dataservice.datahub.model;

import java.util.Date;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.oracle.DbServerType;
import com.navinfo.dataservice.datahub.creator.DbPhysicalCreator;
import com.navinfo.dataservice.datahub.creator.MongoDbPhysicalCreator;
import com.navinfo.dataservice.datahub.creator.OracleSchemaPhysicalCreator;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.navicommons.utils.StringUtils;

/** 
 * @ClassName: UnifiedDbFactory 
 * @author Xiao Xiaowen 
 * @date 2015-12-7 上午10:02:07 
 * @Description: TODO
 */
public class UnifiedDbFactory {
	protected Logger log = Logger.getLogger(this.getClass());
	private static class SingletonHolder{
		private static final UnifiedDbFactory INSTANCE =new UnifiedDbFactory();
	}
	public static UnifiedDbFactory getInstance(){
		return SingletonHolder.INSTANCE;
	}
	private UnifiedDbFactory(){}
	public UnifiedDb newdb(int dbId,String dbName,String dbUserName,String dbUserPasswd,int dbRole,String tablespaceName,String dbType
			,DbServer dbServer,String gdbVersion,int createStatus,Date createTime,String descp){
		UnifiedDb db = null;
		if(DbServerType.TYPE_MONGODB.equals(dbServer.getType())){
			db = new MongoDb(dbId,dbName,dbUserName,dbUserPasswd,dbRole,tablespaceName,dbType
					,dbServer,gdbVersion,createStatus,createTime,descp);
		}else if(DbServerType.TYPE_ORACLE.equals(dbServer.getType())){
			db = new OracleSchema(dbId,dbName,dbUserName,dbUserPasswd,dbRole,tablespaceName,dbType
					,dbServer,gdbVersion,createStatus,createTime,descp);
		}
		return db;
	}
	public UnifiedDb create(int dbId,String dbName,String dbType,String gdbVersion,DbServer dbServer)throws DataHubException{
		UnifiedDb db = null;
		DbPhysicalCreator physicalCreator = null;
		if(DbServerType.TYPE_MONGODB.equals(dbServer.getType())){
			db = new MongoDb(dbId,dbName,dbType,gdbVersion,dbServer,1);
			physicalCreator = new MongoDbPhysicalCreator();
		}else if(DbServerType.TYPE_ORACLE.equals(dbServer.getType())){
			db = new OracleSchema(dbId,dbName,dbType,gdbVersion,dbServer,1);
			physicalCreator = new OracleSchemaPhysicalCreator();
		}else{
			throw new DataHubException("UnifiedDbFactory不支持创建的数据库类型。servertype:"+dbServer.getType());
		}
		physicalCreator.create(db);
		if(StringUtils.isEmpty(gdbVersion)){
			log.info("传入的gdb模型版本号为空，忽略创建gdb模型。");
		}else{
			physicalCreator.installGdbModel(db, gdbVersion);
		}
		return db;
	}
}
