package com.navinfo.dataservice.datahub.creator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.database.oracle.PoolDataSourceFactory;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.model.UnifiedDb;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.SqlExec;
import com.navinfo.navicommons.utils.StringUtils;

/** 
 * @ClassName: OracleSchemaCreator 
 * @author Xiao Xiaowen 
 * @date 2015-12-25 下午3:40:38 
 * @Description: TODO
 */
public class OracleSchemaPhysicalCreator implements DbPhysicalCreator{
	protected Logger log = Logger.getLogger(this.getClass());
	
	public void create(UnifiedDb db)throws DataHubException{
		//超级用户
		if(db.isSuperDb()){
			throw new DataHubException("超级用户不能被创建。");
		}
		UnifiedDb suDb = db.getSuperDb();
		String suUserName = suDb.getDbUserName();
		String suUserPasswd = suDb.getDbUserPasswd();
		//表空间
		String tablespaceName = suDb.getTablespaceName();
		if(StringUtils.isEmpty(tablespaceName)){
			tablespaceName=SystemConfig.getSystemConfig().getValue("datahub.oracle.defaultTablespaces", "GDB_DATA");
		}
		db.setTablespaceName(tablespaceName);
		//用户名和密码同数据库名
		String dbUserName = db.getDbName();
		db.setDbUserName(dbUserName);
		db.setDbUserPasswd(dbUserName);
		db.setDbRole(0);
		
		Connection conn = null;
		try{
			Properties conProps = new Properties();
			conProps.put("user", suUserName);
			conProps.put("password", suUserPasswd);
			conProps.put("defaultRowPrefetch", "15");
//			conProps.put("internal_logon", "sysdba");
			DriverManagerDataSource dataSource = new DriverManagerDataSource();
			dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
			String url = PoolDataSourceFactory.createUrl(suDb.getDbServer().getIp(), suDb.getDbServer().getPort(), suDb.getDbServer().getServiceName());
			dataSource.setUrl(url);
			/*
			 * dataSource.setUsername(sysName);
			 * dataSource.setPassword(sysPassword);
			 */
			dataSource.setConnectionProperties(conProps);
			conn = dataSource.getConnection();
			String createUseSql = "create user "
					+ dbUserName
					+ " identified by "
					+ db.getDbUserPasswd()
					+ " default tablespace " + tablespaceName;
			QueryRunner runner = new QueryRunner();
			runner.execute(conn, createUseSql);
			runner.execute(conn, "grant connect,resource to  " + dbUserName);
			runner.execute(conn, "grant create session to  " + dbUserName);
			runner.execute(conn, "grant alter session to  " + dbUserName);
			runner.execute(conn, "grant create sequence to " + dbUserName);
			runner.execute(conn, "grant create table to  " + dbUserName);
			runner.execute(conn, "grant create view to " + dbUserName);
			runner.execute(conn, "grant create trigger to " + dbUserName);
			runner.execute(conn, "grant create synonym  to " + dbUserName);
			runner.execute(conn, "grant create type to " + dbUserName);
			runner.execute(conn, "grant create  snapshot to " + dbUserName);
			runner.execute(conn, "grant create procedure  to   " + dbUserName);
			runner.execute(conn, "grant query rewrite to " + dbUserName);
			runner.execute(conn, "grant analyze any to  " + dbUserName);
			runner.execute(conn, "grant create database link to " + dbUserName);
			//runner.execute(conn, "grant execute on dbms_lock to " + dbUserName);
			runner.execute(conn, "grant create job to " + dbUserName);
			runner.execute(conn, "grant execute any procedure to " + dbUserName);
			runner.execute(conn, "grant manage scheduler to " + dbUserName);
			runner.execute(conn, "GRANT SELECT_CATALOG_ROLE TO PUBLIC ");
			runner.execute(conn, "GRANT ALTER SYSTEM TO PUBLIC ");
			runner.execute(conn, "grant execute any type to " + dbUserName);

			runner.execute(conn, "grant create public database link to "
					+ dbUserName);
			runner
					.execute(conn, "grant debug connect session to   "
							+ dbUserName);
		}catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataHubException("创建用户时出现SQL错误。"+e.getMessage(),e);
		} finally {
			DbUtils.closeQuietly(conn);
		}	
	}
	public void installGdbModel(UnifiedDb db,String gdbVersion)throws DataHubException{
		Connection conn = null;
		try{
			conn = db.getDriverManagerDataSource().getConnection();
			String schemaCreateFile = "/com/navinfo/dataservice/datahub/resources/"
					+ gdbVersion + "/schema/table_create.sql";
			SqlExec sqlExec = new SqlExec(conn);
			sqlExec.execute(schemaCreateFile);
		}catch(Exception e){
			log.error("给目标库安装GDB模型时出错。原因为："+e.getMessage(),e);
			throw new DataHubException("给目标库安装GDB模型时出错。原因为："+e.getMessage(),e);
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
}
