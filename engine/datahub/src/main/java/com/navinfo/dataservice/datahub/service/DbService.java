package com.navinfo.dataservice.datahub.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.datahub.model.DbServer;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.RandomUtil;
import com.navinfo.dataservice.datahub.chooser.DbServerChooser;
import com.navinfo.dataservice.datahub.chooser.strategy.DbServerStrategy;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName: DbService 
* @author Xiao Xiaowen 
* @date 2016年6月7日 上午9:29:04 
* @Description: TODO
*  
*/
public class DbService {
	private volatile static DbService instance;
	public static DbService getInstance(){
		if(instance==null){
			synchronized(DbService.class){
				if(instance==null){
					instance=new DbService();
				}
			}
		}
		return instance;
	}
	private DbService(){}
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	protected String mainSql = "SELECT D.DB_ID,D.DB_NAME,D.DB_USER_NAME,D.DB_USER_PASSWD,D.DB_ROLE,D.BIZ_TYPE,D.TABLESPACE_NAME,D.GDB_VERSION,D.DB_STATUS,D.CREATE_TIME,D.DESCP,S.SERVER_ID,S.SERVER_TYPE,S.SERVER_IP,S.SERVER_PORT FROM DB_HUB D,DB_SERVER S ";

	public DbInfo createDb(String dbName,String userName,String userPasswd,String bizType,String descp,String gdbVersion,String refDbName,String refUserName,String refDbType)throws DataHubException{
		String strategyType = null;
		Map<String,String> strategyParam = new HashMap<String,String>();
		if(StringUtils.isNotEmpty(refDbName)&&StringUtils.isNotEmpty(refDbType)){
			strategyType = DbServerStrategy.USE_REF_DB;
			strategyParam.put("refDbName", refDbName);
			strategyParam.put("refDbType", refDbType);
			log.debug("使用参考策略创建新库。");
		}
//		else if(StringUtils.isNotEmpty(provCode)){
//			strategyType = DbServerStrategy.BY_PROVINCE;
//			strategyParam.put("provinceCode", provCode);
//		}
		else{
			//strategyType = DbServerStrategy.RANDOM;
			strategyParam = null;
			log.debug("使用随机策略创建新库。");
		}
		
		return createDb(dbName,userName,userPasswd,bizType,descp, gdbVersion, strategyType, strategyParam);
	}
	
	private DbInfo createDb(String dbName,String userName,String userPasswd,String bizType,String descp,String gdbVersion,String strategyType,Map<String,String> strategyParamMap)throws DataHubException{
		DbInfo db = null;
		Connection conn = null;
		int dbId = 0;
		QueryRunner run = new QueryRunner();
		try{
			String checkSql = "select count(1) from db_hub where db_status>0 and db_name=? and biz_type=?";
			DbServer server = null;
			synchronized(DbService.class){
				conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
				if(StringUtils.isEmpty(userName)){
					userName="RD_" + RandomUtil.nextString(10);
				}else{
					//验证同类型是否已经存在
					int count = run.queryForInt(conn, checkSql, userName,bizType);
					if(count>0){
						throw new DataHubException("数据库已经存在，不能重复创建。");
					}
				}
				//寻找服务器
				if(StringUtils.isEmpty(strategyType)){
					strategyType = SystemConfigFactory.getSystemConfig().getValue("dbserver.strategy.default",DbServerStrategy.RANDOM);
				}
				server = DbServerChooser.getInstance().getPriorDbServer(bizType,strategyType,strategyParamMap);
				//写入记录
				String insSql = "insert into db_hub(db_id,db_name,db_user_name,db_user_passwd,db_role,biz_type,gdb_version,server_id,db_status,create_time,descp)" +
						"values(DB_SEQ.nextval,?,?,?,0,?,?,?,1,sysdate,?)";
				run.update(conn, insSql, dbName,userName,userName,bizType,gdbVersion,server.getSid(),descp);
				conn.commit();
			}
			dbId = run.queryForInt(conn, "SELECT DB_SEQ.CURRVAL FROM DUAL");
			//由工厂去创建
			db = DbFactory.getInstance().create(dbId,dbName,userName,userPasswd, bizType, gdbVersion, server);
			//创建完成，更新db状态
			String updateSql = "UPDATE DB_HUB SET TABLESPACE_NAME=?,DB_STATUS=2 WHERE DB_ID=?";
			run.update(conn, updateSql,db.getTablespaceName(),db.getDbId());
			return db;
		}catch (Exception e) {
			//出错了，需要重置数据量的创建状态
			if(dbId>0){
				resetDbInfoStatus(3,dbId);
			}
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new DataHubException("从管理库中查询出现sql或格式错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	private void resetDbInfoStatus(int status,int dbId){
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String errSql = "UPDATE DB_HUB SET DB_STATUS=? WHERE DB_ID=?";
			log.debug(errSql);
			new QueryRunner().update(conn, errSql,status,dbId);
			conn.commit();
		}catch(Exception err){
			log.error("修改新创建的数据库的状态出错，需要手动修改。dbid="+dbId);
			log.error(err.getMessage(),err);
		}finally {
			DbUtils.closeQuietly(conn);
		}
	}
	public DbInfo getDbByName(String dbName,String dbType)throws DataHubException{
		DbInfo db=null;
		Connection conn = null;
		try{
			String sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND D.DB_NAME=? AND D.BIZ_TYPE=? AND D.DB_ROLE=0";
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			db = run.query(conn,sql, new DbResultSetHandler(false),dbName,dbType);
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new DataHubException("从管理库中查询出现sql或格式错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		if(db==null){
			throw new DataHubException("未查询到该数据库的信息。");
		}
		return db;
	}
	public DbInfo getDbById(int dbId)throws DataHubException{
		DbInfo db=null;
		Connection conn = null;
		try{
			String sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND D.DB_ID=? AND D.DB_ROLE=0";
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			db = run.query(conn,sql, new DbResultSetHandler(false),dbId);
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new DataHubException("从管理库中查询出现sql或格式错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		if(db==null){
			throw new DataHubException("未查询到该数据库的信息。");
		}
		return db;
	}
	public DbInfo getOnlyDbByName(String dbName)throws DataHubException{
		DbInfo db=null;
		Connection conn = null;
		try{
			String sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND D.DB_NAME=? AND D.DB_ROLE=0";
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			db = run.query(conn,sql, new DbResultSetHandler(true),dbName);
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new DataHubException("从管理库中查询出现sql或格式错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		if(db==null){
			throw new DataHubException("未查询到该数据库的信息。");
		}
		return db;
	}
	public DbInfo getOnlyDbByType(String bizType)throws DataHubException{
		DbInfo db=null;
		Connection conn = null;
		try{
			String sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND D.BIZ_TYPE=? AND D.DB_ROLE=0";
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			db = run.query(conn,sql, new DbResultSetHandler(true),bizType);
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new DataHubException("从管理库中查询出现sql或格式错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		if(db==null){
			throw new DataHubException("未查询到该数据库的信息。");
		}
		return db;
	}
	/**
	 * 先用先用server_id和db_type查找，如果查不到，再只用server_id查找
	 * @param normalDb
	 * @return
	 * @throws DataHubException
	 */
	public DbInfo getSuperDb(DbInfo normalDb)throws DataHubException{
		if(normalDb==null)return null;
		DbInfo db=null;
		Connection conn = null;
		try{
			String sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND D.BIZ_TYPE=? AND D.SERVER_ID=? AND D.DB_ROLE=1";
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			db = run.query(conn,sql, new DbResultSetHandler(false),normalDb.getBizType(),normalDb.getDbServer().getSid());
			if(db==null){
				sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND D.SERVER_ID=? AND D.DB_ROLE=1";
				db = run.query(conn,sql, new DbResultSetHandler(false),normalDb.getDbServer().getSid());
			}
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new DataHubException("从管理库中查询出现sql或格式错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		if(db==null){
			throw new DataHubException("未查询到该数据库的超级用户信息。");
		}
		return db;
	}
	public static void main(String[] args){
		try{
//			String conString = dbMan.getDbConnectStrByPid(1L);
//			System.out.println(conString);
//			System.out.println(RandomUtil.nextString(8));
			//DbInfo db = dbMan.createDb("TEMP_BJ_01", "prjRoad", "4TEST","240+");
			DbInfo db = DbService.getInstance().getDbByName("TEMP_BJ_01","prjRoad");
			System.out.println(db.getConnectParam());
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	class DbResultSetHandler implements ResultSetHandler<DbInfo>{
		boolean checkCount=false;
		DbResultSetHandler(boolean checkCount){
			super();
			this.checkCount=checkCount;
		}
		

		/* (non-Javadoc)
		 * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
		 */
		@Override
		public DbInfo handle(ResultSet rs)throws SQLException{
			DbInfo db = null; 
			if(rs.next()){
				 DbServer server = new DbServer(rs.getString("SERVER_TYPE")
						 ,rs.getString("SERVER_IP"),rs.getInt("SERVER_PORT"));
				 server.setSid(rs.getInt("SERVER_ID"));
				 db = DbFactory.getInstance()
						 .newdb(rs.getInt("DB_ID"),rs.getString("DB_NAME")
								 ,rs.getString("DB_USER_NAME"),rs.getString("DB_USER_PASSWD")
								 ,rs.getInt("DB_ROLE"),rs.getString("TABLESPACE_NAME")
								 ,rs.getString("BIZ_TYPE"),server
								 ,rs.getString("GDB_VERSION"),rs.getInt("DB_STATUS")
								 ,rs.getTimestamp("CREATE_TIME"),rs.getString("DESCP"));
			 }
			if(checkCount&&rs.next()){
				throw new SQLException("验证错误:结果集超过1条。");
			}
			return db;
		}
		
	}
}
