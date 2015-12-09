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
	
	public UnifiedDb createDb(String dbName,String dbType,String descp)throws DataHubException{
		return createDb(dbName, dbType, descp, null, null);
	}
	public UnifiedDb createDb(String dbName,String dbType,String descp,String strategyType,Map<String,String> strategyParamMap)throws DataHubException{
		UnifiedDb db = null;
		Connection conn = null;
		try{
			String checkSql = "select count(1) from unified_db where db_name=? and db_type=?";
			DbServer server = null;
			synchronized(DbManager.class){
				QueryRunner run = new QueryRunner();
				conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
				//验证同类型是否已经存在
				int count = run.queryForInt(conn, checkSql, dbName,dbType);
				if(count>0){
					throw new DataHubException("数据库已经存在，不能重复创建。");
				}
				//寻找服务器
				if(StringUtils.isEmpty(strategyType)){
					strategyType = SystemConfig.getSystemConfig().getValue("dbserver.strategy.default");
				}
				server = DbServerChooser.getInstance().getPriorDbServer(dbType,strategyType,strategyParamMap);
				//写入记录
				String insSql = "insert into unified_db(db_name,db_role,db_type,server_id,create_time,descp)values(?,0,?,?,now(),?)";
				run.update(conn, insSql, dbName,dbType,server.getSid(),descp);
				conn.commit();
			}
			
			if(DbServerType.TYPE_MONGODB.equals(server.getType())){
				db = new MongoDb(dbName,dbType,server);
				//MONGODB不用创建，在第一次使用时会创建
				//也不需要密码
			}else if(DbServerType.TYPE_ORACLE.equals(server.getType())){
				db = new OracleSchema();
				//创建Oracle的一个用户
				//...
				//写入passwd和使用表空间名
				//...
			} 
			return db;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new DataHubException("从管理库中查询出现sql或格式错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public UnifiedDb getDbByName(String dbName,String dbType)throws DataHubException{
		Connection conn = null;
		try{
			String sql = "select D.DB_ID,D.DB_NAME,D.DB_PASSWD,D.DB_ROLE,D.DB_TYPE,D.TABLESPACE_NAME,S.SERVER_TYPE,S.SERVER_IP,S.SERVER_PORT,S.SERVICE_NAME" +
					" from UNIFIED_DB D,UNIFIED_DB_SERVER S where D.DB_NAME=? AND D.DB_TYPE=?";
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			UnifiedDb db = run.query(conn,sql, new DbResultSetHandler(),dbName,dbType);
			return db;
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new DataHubException("从管理库中查询出现sql或格式错误错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public UnifiedDb getDbById(int dbId)throws DataHubException{
		Connection conn = null;
		try{
			String sql = "select D.DB_ID,D.DB_NAME,D.DB_PASSWD,D.DB_ROLE,D.DB_TYPE,D.TABLESPACE_NAME,S.SERVER_TYPE,S.SERVER_IP,S.SERVER_PORT,S.SERVICE_NAME" +
					" from UNIFIED_DB D,UNIFIED_DB_SERVER S where D.DB_ID=?";
			conn = MultiDataSourceFactory.getInstance().getManDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			UnifiedDb db = run.query(conn,sql, new DbResultSetHandler(),dbId);
			return db;
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new DataHubException("从管理库中查询出现sql或格式错误错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public static void main(String[] args){
		try{
			DbManager dbMan = new DbManager();
//			String conString = dbMan.getDbConnectStrByPid(1L);
//			System.out.println(conString);
//			System.out.println(RandomUtil.nextString(8));
			UnifiedDb db = dbMan.createDb("ASCDEFG", "outProjectPoi", "新项目");
			System.out.println(db);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	class DbResultSetHandler implements ResultSetHandler<UnifiedDb>{

		/* (non-Javadoc)
		 * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
		 */
		@Override
		public UnifiedDb handle(ResultSet rs)throws SQLException{
			UnifiedDb db = null; 
			if(rs.next()){
				 String serverType = rs.getString("SERVER_TYPE");
				 db = UnifiedDbFactory.getInstance().create(serverType);
				 db.setDbId(rs.getInt("DB_ID"));
				 db.setDbName(rs.getString("DB_NAME"));
				 db.setDbPasswd(rs.getString("DB_PASSWD"));
				 db.setDbRole(rs.getInt("DB_ROLE"));
				 db.setDbType(rs.getString("DB_TYPE"));
				 db.setTablespaceName(rs.getString("TABLESPACE_NAME"));
				 DbServer server = new DbServer(rs.getString("SERVER_TYPE")
						 ,rs.getString("SERVER_IP"),rs.getString("SERVER_PORT"));
				 server.setServiceName(rs.getString("SERVICE_NAME"));
				 db.setDbServer(server);
				 return db;
			 }
			return db;
		}
		
	}
}
