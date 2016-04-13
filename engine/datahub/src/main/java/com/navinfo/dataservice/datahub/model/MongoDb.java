package com.navinfo.dataservice.datahub.model;

import java.util.Date;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.navinfo.dataservice.datahub.exception.DataHubException;

/** 
 * @ClassName: MongoDb 
 * @author Xiao Xiaowen 
 * @date 2015-11-30 下午1:47:59 
 * @Description: TODO
 */
public class MongoDb extends UnifiedDb {
	protected Logger log = Logger.getLogger(this.getClass());
	public MongoDb(int dbId,String dbName,String dbType,String gdbVersion
			,DbServer dbServer,int createStatus){
		super(dbId,dbName,dbType,gdbVersion
				,dbServer,createStatus);
	}
	public MongoDb(int dbId,String dbName,String dbUserName,String dbUserPasswd,int dbRole,String tablespaceName,String dbType
			,DbServer dbServer,String gdbVersion,int createStatus,Date createTime,String descp){
		super(dbId,dbName,dbUserName,dbUserPasswd,dbRole,tablespaceName,dbType
				,dbServer,gdbVersion,createStatus,createTime,descp);
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.datahub.model.AbstractDb#getAdminDb()
	 */
	@Override
	public UnifiedDb getSuperDb() throws DataHubException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getConnectString()throws DataHubException {
		String conString = null;
		try{
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, Boolean.TRUE);
			conString = mapper.writeValueAsString(this);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new DataHubException("将oracleschema实体对象解析成json时发生错误，原因："+e.getMessage(),e);
		}
		return conString;
	}
	@Override
	public DriverManagerDataSource getDriverManagerDataSource() {
		
		return null;
	}
	@Override
	public BasicDataSource getPoolDataSource() {
		
		return null;
	}
	@Override
	public void closePoolDataSource() {
		
	}

}
