package com.navinfo.dataservice.datahub.creator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.DataSourceUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.PackageExec;
import com.navinfo.navicommons.database.sql.SqlExec;

/** 
 * @ClassName: OracleSchemaCreator 
 * @author Xiao Xiaowen 
 * @date 2015-12-25 下午3:40:38 
 * @Description: TODO
 */
public class OracleSchemaPhysicalCreator implements DbPhysicalCreator{
	protected Logger log = Logger.getLogger(this.getClass());
	
	public void create(DbInfo db)throws DataHubException{
		//超级用户
		if(db.isSuperDb()){
			throw new DataHubException("超级用户不能被创建。");
		}
		DbInfo suDb = DbService.getInstance().getSuperDb(db);
		String suUserName = suDb.getDbUserName();
		String suUserPasswd = suDb.getDbUserPasswd();
		//表空间
		String tablespaceName = suDb.getTablespaceName();
		if(StringUtils.isEmpty(tablespaceName)){
			tablespaceName=SystemConfigFactory.getSystemConfig().getValue("datahub.oracle.defaultTablespaces", "USERS");
		}
		db.setTablespaceName(tablespaceName);
		//
		String dbUserName = db.getDbUserName();
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
			String url = DataSourceUtil.createOracleJdbcUrl(suDb.getDbServer().getIp(), suDb.getDbServer().getPort(), suDb.getDbServer().getServiceName());
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
			//md5
			runner.execute(conn, "grant execute on dbms_crypto to "+ dbUserName);
			
			//跨用户访问
			runner.execute(conn, "GRANT SELECT ANY TABLE TO "+dbUserName);
		}catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataHubException("创建用户时出现SQL错误。"+e.getMessage(),e);
		} finally {
			DbUtils.closeQuietly(conn);
		}	
	}
	public void installGdbModel(DbInfo db,String gdbVersion)throws DataHubException{
		Connection conn = null;
		try{
			DbConnectConfig connConfig = DbConnectConfig.createConnectConfig(db.getConnectParam());
			conn = MultiDataSourceFactory.getInstance().getDataSource(connConfig).getConnection();
			// gdb
			String schemaCreateFile = "/com/navinfo/dataservice/datahub/resources/"
					+ gdbVersion + "/schema/table_create_gdb.sql";
			SqlExec sqlExec = new SqlExec(conn);
			sqlExec.execute(schemaCreateFile);
			String indexFile = "/com/navinfo/dataservice/datahub/resources/"
					+ gdbVersion + "/schema/index.sql";
			sqlExec.execute(indexFile);
			//gdb+
			PackageExec packageExec = new PackageExec(conn);
			String indexPlus = "/com/navinfo/dataservice/datahub/resources/"
					+ gdbVersion + "/schema/index+.pck";
			packageExec.execute(indexPlus);
			String plusFile = "/com/navinfo/dataservice/datahub/resources/"
					+ gdbVersion + "/schema/table_create_plus.sql";
			sqlExec.execute(plusFile);
		}catch(Exception e){
			log.error("给目标库安装GDB模型时出错。原因为："+e.getMessage(),e);
			throw new DataHubException("给目标库安装GDB模型时出错。原因为："+e.getMessage(),e);
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
}
