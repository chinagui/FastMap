package com.navinfo.dataservice.datahub.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.datahub.model.DbServer;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.DbServerType;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
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
	
	protected String mainSql = "SELECT D.DB_ID,D.DB_NAME,D.DB_USER_NAME,D.DB_USER_PASSWD,D.DB_ROLE,D.BIZ_TYPE,D.TABLESPACE_NAME,D.GDB_VERSION,D.DB_STATUS,D.CREATE_TIME,D.DESCP,S.SERVER_ID,S.SERVER_TYPE,S.SERVER_IP,S.SERVER_PORT,S.SERVICE_NAME FROM DB_HUB D,DB_SERVER S ";

	public DbInfo createOracleDb(String userName,String userPasswd,String bizType,String descp,String gdbVersion,int refDbId)throws DataHubException{
		String strategyType = null;
		Map<String,Object> strategyParam = new HashMap<String,Object>();
		if(refDbId>0){
			strategyType = DbServerStrategy.USE_REF_DB;
			strategyParam.put("refDbId", refDbId);
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
		
		return createDb(DbServerType.TYPE_ORACLE,null,userName,userPasswd,bizType,descp, gdbVersion, strategyType, strategyParam);
	}
	/**
	 * 
	 * @param serverType:必须
	 * @param dbName：ORACLE类型不需要传，会从db所在服务器上取ServiceName，MONGO，MYSQL必传
	 * @param userName：非必须，ORACLE类型会生成一个随机的用户名，MONGO,MYSQL,会使用管理原账户
	 * @param userPasswd，非必须，同userName
	 * @param bizType
	 * @param descp
	 * @param gdbVersion
	 * @param refDbName
	 * @param refUserName
	 * @param refDbType
	 * @return
	 * @throws DataHubException
	 */
	public DbInfo createMongoDb(String dbName,String bizType,String descp,String gdbVersion,int refDbId)throws DataHubException{
		String strategyType = null;
		Map<String,Object> strategyParam = new HashMap<String,Object>();
		if(refDbId>0){
			strategyType = DbServerStrategy.USE_REF_DB;
			strategyParam.put("refDbId", refDbId);
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
		
		return createDb(DbServerType.TYPE_MONGODB,dbName,null,null,bizType,descp, gdbVersion, strategyType, strategyParam);
	}
	public  DbInfo createDb(String serverType,String dbName,String userName,String userPasswd,String bizType,String descp,String gdbVersion,int refDbId)throws DataHubException{
		if(DbServerType.TYPE_ORACLE.equals(serverType)){
			return createOracleDb(userName,userPasswd, bizType, descp, gdbVersion,refDbId);
		}else if(DbServerType.TYPE_MONGODB.equals(serverType)){
			return createMongoDb(dbName,bizType,descp,gdbVersion,refDbId);
		}else{
			throw new DataHubException("暂不支持的库类型");
		}
	}
	
	private DbInfo createDb(String serverType,String dbName,String userName,String userPasswd,String bizType,String descp,String gdbVersion,String strategyType,Map<String,Object> strategyParamMap)throws DataHubException{
		DbInfo db = null;
		Connection conn = null;
		int dbId = 0;
		QueryRunner run = new QueryRunner();
		try{
			String checkSql = null;
			String checkName = null;
			if(!DbServerType.TYPE_ORACLE.equals(serverType)){
				checkSql = "select count(1) from db_hub where db_status>0 and db_name=? and biz_type=?";
				checkName = dbName;
			}else{
				checkSql = "select count(1) from db_hub where db_status>0 and db_user_name=? and biz_type=?";
				checkName = userName;
			}
			
			DbServer server = null;
			synchronized(DbService.class){
				conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
				if(DbServerType.TYPE_ORACLE.equals(serverType)&&StringUtils.isEmpty(userName)){
					userName="HUB_" + RandomUtil.nextString(10);
					userPasswd = userName;
				}else if((DbServerType.TYPE_MONGODB.equals(serverType)||DbServerType.TYPE_MYSQL.equals(serverType))
						&&StringUtils.isEmpty(dbName)){
					dbName = "HUB_" + RandomUtil.nextString(10);
				}else{
					//验证同类型是否已经存在
					int count = run.queryForInt(conn, checkSql,checkName,bizType);
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
				run.update(conn, insSql, dbName,userName,userPasswd,bizType,gdbVersion,server.getSid(),descp);
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
	/**
	 * DB_NAME和DB_USER_NAME都会查询，[D.DB_NAME=? OR D.DB_USER_NAME=?]
	 * @param dbName
	 * @param bizType
	 * @return
	 * @throws DataHubException
	 */
	public DbInfo getDbByName(String name,String bizType)throws DataHubException{
		DbInfo db=null;
		Connection conn = null;
		try{
			String sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND (D.DB_NAME=? OR D.DB_USER_NAME=?) AND D.BIZ_TYPE=? AND D.DB_ROLE=0";
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			db = run.query(conn,sql, new DbResultSetHandler(false),name,name,bizType);
			
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
	/**
	 * DB_NAME和DB_USER_NAME都会查询，[D.DB_NAME=? OR D.DB_USER_NAME=?]
	 * @param name
	 * @return
	 * @throws DataHubException
	 */
	public DbInfo getOnlyDbByName(String name)throws DataHubException{
		DbInfo db=null;
		Connection conn = null;
		try{
			String sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND (D.DB_NAME=? OR D.DB_USER_NAME=?) AND D.DB_ROLE=0";
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			db = run.query(conn,sql, new DbResultSetHandler(true),name,name);
			
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
	 * @Title: getDbsByBizType
	 * @Description: 根据 bizType 获取 oracle数据库连接的List
	 * @param bizType
	 * @return
	 * @throws DataHubException  List<DbInfo>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月10日 下午4:32:04 
	 */
	public List<DbInfo> getDbsByBizType(String bizType)throws DataHubException{
		List<DbInfo> dbis = new ArrayList<DbInfo>();
		//DbInfo db=null;
		Connection conn = null;
		DataSource ds = null;
		
			
		
		try{
			if(bizType != null && StringUtils.isNotEmpty(bizType)){
			String sql = mainSql+" where D.SERVER_ID=S.SERVER_ID AND D.BIZ_TYPE='"+bizType+"' AND D.DB_ROLE=0 "
					+ " AND (D.DB_USER_NAME LIKE 'fm_regiondb_sp6_m_%' OR D.DB_USER_NAME LIKE 'fm_regiondb_sp6_d_%') ";
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			ds = MultiDataSourceFactory.getInstance().getSysDataSource();
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<DbInfo>> rsHandler = new ResultSetHandler<List<DbInfo>>(){
				public List<DbInfo> handle(ResultSet rs) throws SQLException {
					List<DbInfo> list = new ArrayList<DbInfo>();
					while(rs.next()){
						//D.DB_ID,D.DB_NAME,D.DB_USER_NAME,D.DB_USER_PASSWD,D.DB_ROLE,
						//D.BIZ_TYPE,D.TABLESPACE_NAME,D.GDB_VERSION,D.DB_STATUS,D.CREATE_TIME,D.DESCP,
						//S.SERVER_ID,S.SERVER_TYPE,S.SERVER_IP,S.SERVER_PORT,S.SERVICE_NAME 
						DbInfo dbInfo = new DbInfo();
						DbServer dbServer =new DbServer();
						dbInfo.setDbId(rs.getInt("DB_ID"));
						dbInfo.setDbName(rs.getString("DB_NAME"));
						dbInfo.setDbUserName(rs.getString("DB_USER_NAME"));
						dbInfo.setDbUserPasswd(rs.getString("DB_USER_PASSWD"));
						dbInfo.setDbRole(rs.getInt("DB_ROLE"));
						dbInfo.setBizType(rs.getString("BIZ_TYPE"));
						dbInfo.setTablespaceName(rs.getString("TABLESPACE_NAME"));
						dbInfo.setGdbVersion(rs.getString("GDB_VERSION"));
						dbInfo.setDbStatus(rs.getInt("DB_STATUS"));
						dbInfo.setCreateTime(rs.getTimestamp("CREATE_TIME"));
						dbInfo.setDescp(rs.getString("DESCP"));
						dbServer.setSid(rs.getInt("SERVER_ID"));
						dbServer.setType(rs.getString("SERVER_TYPE"));
						dbServer.setIp(rs.getString("SERVER_IP"));
						dbServer.setPort(rs.getInt("SERVER_PORT"));
						dbServer.setServiceName(rs.getString("SERVICE_NAME"));
						dbInfo.setDbServer(dbServer);
						list.add(dbInfo);
					}
					return list;
				}
	    	};
			
			
			
			dbis = run.query(conn, sql,rsHandler);
			//dbis = run.queryForList(ds, sql, bizType);
					//(conn,sql, new DbResultSetHandler(true),bizType);
			}	
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new DataHubException("从元数据库中查询出现sql或格式错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		if(dbis==null){
			throw new DataHubException("未查询到该数据库的信息。");
		}
		return dbis;
	}
	
	public DbInfo getOnlyDbByBizType(String bizType)throws DataHubException{
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
	 * 先用先用server_id和biz_type查找，如果查不到，再只用server_id查找
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
	/**
	 * 如果没找到可重复使用的库，返回null
	 * @param bizType:业务类型
	 * @return
	 * @throws Exception
	 */
	public DbInfo getReuseDb(String bizType)throws Exception{
		DbInfo db=null;
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String selSql = "SELECT T.DB_ID FROM DB_REUSE T WHERE EXISTS (SELECT 1 FROM DB_HUB P WHERE T.DB_ID=P.DB_ID AND P.BIZ_TYPE=?) AND T.USING_STATUS=0 AND ROWNUM=1 FOR UPDATE";
			QueryRunner run = new QueryRunner();
			int dbId = run.queryForInt(conn, selSql,bizType);
			if(dbId>0){
				db = getDbById(dbId);
				String lockSql = "UPDATE DB_REUSE SET USING_STATUS=1 WHERE DB_ID=?";
				run.update(conn, lockSql, dbId);
			}
			return db;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("从管理库中查询出现sql或格式错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	/**
	 * 
	 * @param bizType
	 * @param refDbId:参考库的id
	 * @return
	 * @throws Exception
	 */
	public DbInfo getReuseDb(String bizType,int refDbId)throws Exception{
		DbInfo db=null;
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			String selSql = "SELECT T.DB_ID FROM DB_REUSE T WHERE EXISTS (SELECT 1 FROM DB_HUB P WHERE T.DB_ID=P.DB_ID AND P.BIZ_TYPE=?"
					+ " AND P.SERVER_ID=(SELECT SERVER_ID FROM DB_HUB WHERE DB_ID=?)) AND T.USING_STATUS=0 AND ROWNUM=1 FOR UPDATE";
			QueryRunner run = new QueryRunner();
			int dbId = run.queryForInt(conn, selSql,bizType,refDbId);
			if(dbId>0){
				db = getDbById(dbId);
				String lockSql = "UPDATE DB_REUSE SET USING_STATUS=1 WHERE DB_ID=?";
				run.update(conn, lockSql, dbId);
			}
			return db;
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("从管理库中查询出现sql或格式错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public DbInfo getRandomDbByBizType(String bizType)throws DataHubException{
		DbInfo db=null;
		Connection conn = null;
		try{
			String sql = "SELECT * FROM ("+mainSql+" WHERE D.SERVER_ID=S.SERVER_ID AND D.BIZ_TYPE=? AND D.DB_ROLE=0 ORDER BY DBMS_RANDOM.VALUE) WHERE ROWNUM=1";
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
	public static void main(String[] args){
		try{
//			String conString = dbMan.getDbConnectStrByPid(1L);
//			System.out.println(conString);
//			System.out.println(RandomUtil.nextString(8));
			//DbInfo db = dbMan.createDb("TEMP_BJ_01", "prjRoad", "4TEST","240+");
//			DbInfo db = DbService.getInstance().createOracleDb(null, null, "copVersion", "descp", "250+", null, null);
			DbInfo db = DbService.getInstance().getDbById(8);
			System.out.println(db.getConnectParam());
//			DbInfo su = DbService.getInstance().getSuperDb(db);
//			System.out.println(su.toString());
			
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
				 server.setServiceName(rs.getString("SERVICE_NAME"));
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
