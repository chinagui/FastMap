package com.navinfo.dataservice.datahub.manager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.navinfo.dataservice.commons.config.SystemConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.oracle.DbServerType;
import com.navinfo.dataservice.datahub.chooser.DbServerChooser;
import com.navinfo.dataservice.datahub.chooser.strategy.DbServerStrategy;
import com.navinfo.dataservice.datahub.creator.DbPhysicalCreator;
import com.navinfo.dataservice.datahub.creator.MongoDbPhysicalCreator;
import com.navinfo.dataservice.datahub.exception.DataHubException;
import com.navinfo.dataservice.datahub.model.DbServer;
import com.navinfo.dataservice.datahub.model.MongoDb;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.datahub.model.UnifiedDb;
import com.navinfo.dataservice.datahub.model.UnifiedDbFactory;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.utils.RandomUtil;
import com.navinfo.navicommons.utils.StringUtils;

/** 
 * @ClassName: DbManager 
 * @author Xiao Xiaowen 
 * @date 2015-12-1 上午10:40:33 
 * @Description: TODO
 */
public class DbManager {
	protected Logger log = Logger.getLogger(this.getClass());
	protected String mainSql = "SELECT D.DB_ID,D.DB_NAME,D.DB_USER_NAME,D.DB_USER_PASSWD,D.DB_ROLE,D.BIZ_TYPE,D.TABLESPACE_NAME,D.GDB_VERSION,D.DB_STATUS,D.CREATE_TIME,D.DESCP,S.SERVER_ID,S.SERVER_TYPE,S.SERVER_IP,S.SERVER_PORT,S.SERVICE_NAME FROM DB_HUB D,DB_SERVER S ";
	public UnifiedDb createDb(String bizType,String descp)throws DataHubException{
		return createDb(null, bizType, descp, null, null,null);
	}
	public UnifiedDb createDb(String dbName,String bizType,String descp)throws DataHubException{
		return createDb(dbName, bizType, descp, null, null,null);
	}
	public UnifiedDb createDb(String dbName,String bizType,String descp,String gdbVersion)throws DataHubException{
		return createDb(dbName, bizType, descp, null, null,gdbVersion);
	}
	public UnifiedDb createDb(String dbName,String bizType,String descp,String strategyType,Map<String,String> strategyParamMap)throws DataHubException{
		return createDb(dbName, bizType, descp, null, null,null);
	}
	public UnifiedDb createDb(String dbName,String bizType,String descp,String strategyType,Map<String,String> strategyParamMap,String gdbVersion)throws DataHubException{
		UnifiedDb db = null;
		Connection conn = null;
		int dbId = 0;
		QueryRunner run = new QueryRunner();
		try{
			String checkSql = "select count(1) from db_hub where db_status>0 and db_name=? and biz_type=?";
			DbServer server = null;
			synchronized(DbManager.class){
				conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
				if(StringUtils.isEmpty(dbName)){
					dbName="VM_" + RandomUtil.nextString(10);
				}else{
					//验证同类型是否已经存在
					int count = run.queryForInt(conn, checkSql, dbName,bizType);
					if(count>0){
						throw new DataHubException("数据库已经存在，不能重复创建。");
					}
				}
				//寻找服务器
				if(StringUtils.isEmpty(strategyType)){
					strategyType = SystemConfig.getSystemConfig().getValue("dbserver.strategy.default");
				}
				server = DbServerChooser.getInstance().getPriorDbServer(bizType,strategyType,strategyParamMap);
				//写入记录
				String insSql = "insert into db_hub(db_id,db_name,db_user_name,db_user_passwd,db_role,biz_type,gdb_version,server_id,db_status,create_time,descp)" +
						"values(DB_SEQ.nextval,?,?,?,0,?,?,?,1,sysdate,?)";
				run.update(conn, insSql, dbName,dbName,dbName,bizType,gdbVersion,server.getSid(),descp);
				conn.commit();
			}
			dbId = run.queryForInt(conn, "SELECT DB_SEQ.CURRVAL FROM DUAL");
			//由工厂去创建
			db = UnifiedDbFactory.getInstance().create(dbId,dbName, bizType, gdbVersion, server);
			//创建完成，更新db状态
			String updateSql = "UPDATE DB_HUB SET TABLESPACE_NAME=?,DB_STATUS=2 WHERE DB_ID=?";
			run.update(conn, updateSql,db.getTablespaceName(),db.getDbId());
			return db;
		}catch (Exception e) {
			//出错了，需要重置数据量的创建状态
			if(dbId>0){
				resetUnifiedDbStatus(3,dbId);
			}
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new DataHubException("从管理库中查询出现sql或格式错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	private void resetUnifiedDbStatus(int status,int dbId){
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
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
	public UnifiedDb getDbByName(String dbName,String dbType)throws DataHubException{
		UnifiedDb db=null;
		Connection conn = null;
		try{
			String sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND D.DB_NAME=? AND D.BIZ_TYPE=? AND D.DB_ROLE=0";
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
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
	public UnifiedDb getDbById(int dbId)throws DataHubException{
		UnifiedDb db=null;
		Connection conn = null;
		try{
			String sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND D.DB_ID=? AND D.DB_ROLE=0";
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
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
	public UnifiedDb getOnlyDbByName(String dbName)throws DataHubException{
		UnifiedDb db=null;
		Connection conn = null;
		try{
			String sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND D.DB_NAME=? AND D.DB_ROLE=0";
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
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
	public UnifiedDb getOnlyDbByType(String bizType)throws DataHubException{
		UnifiedDb db=null;
		Connection conn = null;
		try{
			String sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND D.BIZ_TYPE=? AND D.DB_ROLE=0";
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
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
	public UnifiedDb getSuperDb(UnifiedDb normalDb)throws DataHubException{
		if(normalDb==null)return null;
		UnifiedDb db=null;
		Connection conn = null;
		try{
			String sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND D.BIZ_TYPE=? AND D.SERVER_ID=? AND D.DB_ROLE=1";
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
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
			DbManager dbMan = new DbManager();
//			String conString = dbMan.getDbConnectStrByPid(1L);
//			System.out.println(conString);
//			System.out.println(RandomUtil.nextString(8));
			//UnifiedDb db = dbMan.createDb("TEMP_BJ_01", "prjRoad", "4TEST","240+");
			UnifiedDb db = dbMan.getDbByName("TEMP_BJ_01","prjRoad");
			System.out.println(db.getConnectParam());
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	class DbResultSetHandler implements ResultSetHandler<UnifiedDb>{
		boolean checkCount=false;
		DbResultSetHandler(boolean checkCount){
			super();
			this.checkCount=checkCount;
		}
		

		/* (non-Javadoc)
		 * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
		 */
		@Override
		public UnifiedDb handle(ResultSet rs)throws SQLException{
			UnifiedDb db = null; 
			if(rs.next()){
				 DbServer server = new DbServer(rs.getString("SERVER_TYPE")
						 ,rs.getString("SERVER_IP"),rs.getInt("SERVER_PORT"),rs.getString("SERVICE_NAME"));
				 server.setSid(rs.getInt("SERVER_ID"));
				 db = UnifiedDbFactory.getInstance()
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
