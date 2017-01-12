package com.navinfo.dataservice.expcore.source;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.SQLQuery;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.navicommons.database.sql.PackageExec;
import com.navinfo.navicommons.database.sql.SqlExec;

/** 
 * @ClassName: OracleTarget 
 * @author Xiao Xiaowen 
 * @date 2015-10-29 下午5:30:04 
 * @Description: TODO
 *  
 */
public class OracleSource implements ExportSource {
	private Logger log = Logger.getLogger(OracleSource.class);
	private OracleSchema schema;
	private String tempTableSuffix=null;
	private boolean newTableSuffix=false;
	
	public OracleSource(OracleSchema schema){
		this.schema=schema;
	}
	/**
	 * @return the schema
	 */
	public OracleSchema getSchema() {
		return schema;
	}
	/**
	 * @param schema the schema to set
	 */
	public void setSchema(OracleSchema schema) {
		this.schema = schema;
	}
	
	public void init(String gdbVersion)throws ExportException{
		//占有临时表
		lockTempTableResource();
		//
		Connection conn = null;
		try{
			conn=schema.getPoolDataSource().getConnection();
			installExportViews(gdbVersion,conn);
			//create or truncate temp tables
			createOrTruncateTempTables(gdbVersion,conn);
			//
			//view.sql,"f_str_append_ifnotexists.fnc",
			String[] pkgFullNames={"logger.pck","PK_TABLE_STATS.pck"};
			for(String pkgFulName:pkgFullNames){
				installPackage(gdbVersion,pkgFulName,conn);
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw new ExportException("初始化源时发生错误:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	private void installExportViews(String gdbVersion,Connection conn)throws ExportException{
		try{
			String viewFile = "/com/navinfo/dataservice/expcore/resources/" + gdbVersion + "/scripts/view.sql";
			SqlExec sqlExec = new SqlExec(conn);
			sqlExec.execute(viewFile);
		}catch(Exception e){
			log.error("初始化源.创建视图时发生错误。",e);
			throw new ExportException("初始化源.创建视图时发生错误。",e);
		}
	}
	private void createOrTruncateTempTables(String gdbVersion,Connection conn) throws ExportException {
		InputStream is = null;
		try {
			String schemaCreateFile = "/com/navinfo/dataservice/expcore/resources/" + gdbVersion + (newTableSuffix?"/scripts/temp_table_create.sql":"/scripts/temp_table_truncate.sql");
			is = OracleSource.class.getResourceAsStream(schemaCreateFile);
			if (is == null) {
				Thread.currentThread().getContextClassLoader().getResourceAsStream(schemaCreateFile);
			}
			if (is == null)
				throw new ExportException("在给源库创建或清理临时表时无法找到配置文件:" + schemaCreateFile);
			Reader reader = new InputStreamReader(is, "UTF-8");
			StringBuilder builder = new StringBuilder();
			BufferedReader in = new BufferedReader(reader);
			String line;
			while ((line = in.readLine()) != null) {
				Map<String, String> pro = new HashMap<String, String>(1);
				pro.put("suffix", this.tempTableSuffix);
				line = StringUtils.expandVariables(line, pro);
				builder.append(line + "\n");
			}
			SqlExec sqlExec = new SqlExec(conn);
			sqlExec.execute(new ByteArrayInputStream(builder.toString().getBytes()));
		} catch(Exception e){
			log.error("初始化源.创建临时表时发生错误。",e);
			throw new ExportException("初始化源.创建临时表时发生错误。"+e.getMessage(),e);
		}finally {
			if (is != null) {
				try{
					is.close();
				}catch(Exception e){
					log.error(e.getMessage(),e);
				}
			}
		}
	}
	private void installPackage(String gdbVersion,String pkgFulName, Connection conn) throws Exception {
		String pkgName = pkgFulName.substring(0, pkgFulName.indexOf(".")).toUpperCase();
		String sql = "select count(1) package_count from user_objects o where o.OBJECT_NAME = '"+pkgName+"' " +
				"and (object_type = 'PACKAGE' or object_type = 'PACKAGE BODY')";
		SQLQuery sqlQuery = new SQLQuery(conn);
		List<Map<String, String>> packages = sqlQuery.queryMap(sql);
		log.debug("package count " + packages.get(0).get("PACKAGE_COUNT"));
		if (!"2".equals(packages.get(0).get("PACKAGE_COUNT")))
		{
			String fulName = "/com/navinfo/dataservice/expcore/resources/" + gdbVersion + "/scripts/"+pkgFulName;
			log.debug("使用以下路径中的包：" + fulName);
			PackageExec packageExec = new PackageExec(conn);
			packageExec.execute(fulName);
		}
		
	}
	
	@Override
	public void release() {
		log.info("释放临时表资源");
		releaseTempTableResource();
		//不需要关闭数据源
//		log.info("destroy the pooldatasource of the source schema.");
//		if(schema!=null){
//			schema.closePoolDataSource();
//		}
	}
	@Override
	public String getTempSuffix() {
		return tempTableSuffix;
	}
	public void lockTempTableResource()throws ExportException{
		Connection conn = null;
		try{
			conn=schema.getPoolDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			
			//先检查临时表管理表是否存在，如果不存在，则创建，并且插入一条suffix=1的记录，并且当前请求直接占用suffix=1的该条记录
			//如果是集群环境多个服务同时执行到此步骤，都判断源无临时表管理表，那么后创建的的服务中会抛出失败
			synchronized(OracleSource.class){
				int count=0;
				String checkSql = "SELECT count(1) FROM user_tables O WHERE O.table_name = ? ";
				count = run.queryForInt(conn, checkSql, "TEMP_TABLE_RESOURCES");
				if(count!=1){
					String createSql = "create table temp_table_resources ("
									  +" suffix number(4) not null primary key,"
									  +" release     NUMBER(1) not null,"
									  +" lock_time   DATE,"
									  +" unlock_time date,"
									  +" description VARCHAR2(256))";
					run.execute(conn, createSql);
					run.execute(conn, "create index IDX_temp_table_resources on temp_table_resources (release)");
					run.execute(conn, "insert into temp_table_resources (suffix,release,lock_time) values(1,0,sysdate)");
					conn.commit();
					this.tempTableSuffix="1";
					this.newTableSuffix=true;
					return;
				}
			}
			//此时temp_table_resources表中至少会有一条数据，for update语句可以锁住
			String lockSql = "select * from temp_table_resources for update";
			run.execute(conn, lockSql);
			String minSql = "select min(suffix) from temp_table_resources where release=1";
			int getSuffix = run.queryForInt(conn, minSql);
			if(getSuffix>0){
				log.info("从已释放临时表记录中找到suffix="+getSuffix+"的临时表资源。");
				String updateMinSql = "update temp_table_resources set release=0,lock_time=sysdate,unlock_time=null where suffix=?";
				run.update(conn, updateMinSql, getSuffix);
				this.tempTableSuffix=String.valueOf(getSuffix);
				return;
			}
			//
			int newSuffix = run.queryForInt(conn,"select max(suffix)+1 from temp_table_resources");
			
			String insertMaxSql = "insert into temp_table_resources (suffix,release,lock_time) values(?,0,sysdate)";
			run.update(conn, insertMaxSql,newSuffix);
			this.tempTableSuffix=String.valueOf(newSuffix);
			this.newTableSuffix=true;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(),e);
			throw new ExportException("占有临时表资源时发生错误:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	public void releaseTempTableResource(){
		Connection conn = null;
		try{
			conn=schema.getPoolDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			String updateMinSql = "update temp_table_resources set release=1,unlock_time=sysdate where suffix="+this.getTempSuffix();
			log.debug("释放临时表sql:"+updateMinSql);
			run.update(conn, updateMinSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error("注意：释放临时表时发生错误，请手动释放。原因:"+e.getMessage(),e);
			//throw new ExportException("释放临时表资源时发生错误:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public static void main(String[] args){
		String pkgFulName = "ss.pck";
		String pkgName = pkgFulName.substring(0, pkgFulName.indexOf("."));
		System.out.println(pkgName);
	}
}
