package com.navinfo.dataservice.datahub.service;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.datahub.model.DbServer;
import com.navinfo.dataservice.commons.database.DbServerType;
import com.navinfo.dataservice.datahub.creator.DbPhysicalCreator;
import com.navinfo.dataservice.datahub.creator.MongoDbPhysicalCreator;
import com.navinfo.dataservice.datahub.creator.OracleSchemaPhysicalCreator;
import com.navinfo.dataservice.datahub.exception.DataHubException;

/** 
 * @ClassName: UnifiedDbFactory 
 * @author Xiao Xiaowen 
 * @date 2015-12-7 上午10:02:07 
 * @Description: TODO
 */
public class DbFactory {
	protected Logger log = Logger.getLogger(this.getClass());
	private static class SingletonHolder{
		private static final DbFactory INSTANCE =new DbFactory();
	}
	public static DbFactory getInstance(){
		return SingletonHolder.INSTANCE;
	}
	private DbFactory(){}
	/**
	 * 只创建DbInfo对象，不会物理创建数据库
	 * @return
	 */
	public DbInfo newdb(int dbId,String dbName,String dbUserName,String dbUserPasswd,int dbRole,String tablespaceName,String bizType
			,DbServer dbServer,String gdbVersion,int dbStatus,Date createTime,String descp){
		return new DbInfo(dbId,dbName,dbUserName,dbUserPasswd,dbRole,tablespaceName,bizType
				,dbServer,gdbVersion,dbStatus,createTime,descp);
	}
	public DbInfo create(int dbId,String dbName,String userName,String userPasswd,String bizType,String gdbVersion,DbServer dbServer)throws DataHubException{
		DbInfo db = new DbInfo(dbId,dbName,userName,userPasswd,bizType,gdbVersion,dbServer,1);;
		DbPhysicalCreator physicalCreator = null;
		if(DbServerType.TYPE_MONGODB.equals(dbServer.getType())){
			physicalCreator = new MongoDbPhysicalCreator();
		}else if(DbServerType.TYPE_ORACLE.equals(dbServer.getType())){
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
