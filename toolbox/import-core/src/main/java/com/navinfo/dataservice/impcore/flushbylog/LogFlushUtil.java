package com.navinfo.dataservice.impcore.flushbylog;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.edit.iface.DatalockApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.impcore.exception.LockException;
import com.navinfo.dataservice.impcore.mover.LogMoveResult;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DbLinkCreator;

/*
 * @author MaYunFei
 * 2016年6月21日
 * 描述：import-coreLogFlushUtil.java
 */
public class LogFlushUtil {
	private static class SingletonHolder {
		private static final LogFlushUtil INSTANCE = new LogFlushUtil();
	}

	public static final LogFlushUtil getInstance() {
		return SingletonHolder.INSTANCE;
	}
	/**根据给定履历查询的sql，从源库中获取履历；将变更刷新到目标库； 调用者，需要自己负责数据库事务的提交、会滚和关闭
	 * @param sourceDbConn 源库
	 * @param targetDbConn 目标库
	 * @param logQuerySql  履历查询的sql
	 * @return 履历刷新的结果
	 * @throws Exception
	 */
	public  FlushResult flush(Connection sourceDbConn,Connection targetDbConn,String logQuerySql) throws Exception {
		return flush(sourceDbConn,targetDbConn,logQuerySql,false);
		
	}
	public  FlushResult flush(Connection sourceDbConn,Connection targetDbConn,String logQuerySql,boolean ignoreError) throws Exception {
		LogReader logReader = new LogReader(sourceDbConn,logQuerySql);
		return flush(logReader,targetDbConn,ignoreError);
		
	}
	
	private  FlushResult flush(LogReader logReader,Connection targetDbConn,boolean ignoreError) throws Exception {
		ResultSet rs = logReader.read();
		try{
			rs.setFetchSize(1000);
			FlushResult flushResult =new FlushResult();
			LogWriter logWriter = new LogWriter(targetDbConn,ignoreError);
			while (rs.next()) {
				flushResult .addTotal();
				int opType = rs.getInt("op_tp");
				String rowId = rs.getString("row_id");
				String opId = rs.getString("op_id");
				String newValue = rs.getString("new");
				String tableName = rs.getString("tb_nm");
				String tableRowId = rs.getString("tb_row_id");

				EditLog editLog = new EditLog(opType, rowId, opId, rowId,newValue, tableName, tableRowId);
				ILogWriteListener listener = new LogWriteListener(flushResult);
				logWriter.write(editLog , listener );
				
			}
			return flushResult;
		}finally{
			logReader.close();
		}
		
	}
	/**
	 * 将源库中的履历搬移到目标库
	 * @param sourceDbConn 源库的连接
	 * @param targetDbLink 目标库的dblink
	 * @param logDetailMoveSql log_detail的搬移sql
	 * @param logDetailGridMoveSql  log_detail_grid 的搬移sql
	 * @param logOperationMoveSql log_operation 的搬移sql
	 * @return
	 * @throws Exception
	 */
	public LogMoveResult  moveLog(Connection sourceDbConn,
			String targetDbLink,
			String logDetailMoveSql,
			String logDetailGridMoveSql,
			String logOperationMoveSql
			) throws Exception {
			QueryRunner run = new QueryRunner();
			LogMoveResult moveResult= new LogMoveResult();
			if (StringUtils.isNotEmpty(logDetailMoveSql)){
				moveResult.setLogDetailMoveCount(run.update(sourceDbConn, logDetailMoveSql));
			}
			if (StringUtils.isNotEmpty(logDetailGridMoveSql)){
				moveResult.setLogDetailGridMoveCount(run.update(sourceDbConn, logDetailGridMoveSql));
			}
			if (StringUtils.isNotEmpty(logOperationMoveSql)){
				moveResult.setLogOperationMoveCount(run.update(sourceDbConn, logOperationMoveSql));
			}
			return moveResult;
		
	
	}
	/**在给定的数据库上面，创建临时表；由于该方法是DDL，因此会自动提交事务。
	 * @param sourceDbConn
	 * @return
	 * @throws SQLException
	 */
	public String createTempTable(Connection sourceDbConn) throws SQLException {
		String tempTable = "TEMP_LOG_OP_"+new Random().nextInt(1000000);
		return createTempTable(sourceDbConn,tempTable);
		
	}
	/**在给定的数据库上面，创建临时表；由于该方法是DDL，因此会自动提交事务。
	 * @param sourceDbConn
	 * @return
	 * @throws SQLException
	 */
	public String createTempTable(Connection sourceDbConn,final String tempTable) throws SQLException {
		QueryRunner run = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		sb.append(tempTable);
		sb.append("(OP_ID RAW(16),OP_DT TIMESTAMP)");
		run.execute(sourceDbConn, sb.toString());
		return tempTable;
		
	}
	
	/**将要刷的履历的op_id,OP_DT 提取到createTempTable 方法创建的临时表中
	 * @param sourceDbConn
	 * @param prepareSql
	 * @param isExtLog
	 * @param extLogSql
	 * @param isLockLog
	 * @param logLockSql
	 * @throws LockException
	 */
	public  void prepareAndLockLog(Connection sourceDbConn,
			String prepareSql,
			List<Object> prepareParaValues,
			boolean isExtLog,//是否要进行扩履历
			String extLogSql,
			boolean isLockLog,//是否要对履历加锁
			String logLockSql
			)throws LockException{
		try{
			int logOperationCount = 0;
			//2.select by conditions
			logOperationCount+=this.prepareLog(sourceDbConn,prepareSql,prepareParaValues);
			//3.
			if (isExtLog){//如果是全部回，则不扩履历
				logOperationCount+=extendLogByRowId(sourceDbConn,extLogSql);
			}
			if (isLockLog){
				lockPreparedLog(logOperationCount, sourceDbConn, logLockSql);
			}
			sourceDbConn.commit();
		}catch(Exception e){
			throw new LockException("锁定要准备回库的履历时出现错误:"+e.getMessage(),e);
		}
	}
	/**
	 * 解除log锁；根据repareTempTable中的履历和log_operation进行关联，设置其lock_sta为0
	 * @param sourceDbConn
	 * @param prepareTempTable
	 */
	public  void unlockPreparedLog(Connection sourceDbConn,String prepareTempTable){
		try{
			QueryRunner run = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE LOG_OPERATION L SET L.LOCK_STA=0 WHERE EXISTS (SELECT 1 FROM ");
			sb.append(prepareTempTable);
			sb.append(" T WHERE L.OP_ID=T.OP_ID)");
			run.update(sourceDbConn, sb.toString());
			sourceDbConn.commit();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**锁定大区库给定的grid
	 * @param regionId 大区id
	 * @param lockObject 锁定的要素对象类型 POI: 参考FmEditLock.LOCK_OBJ_POI poi要素； LOCK_OBJ_ROAD,道路要素；LOCK_OBJ_ALL 全部要素
	 * @param lockType  参考 FmEditLock.TYPE_BORROW 借履历，TYPE_CHECK 检查，TYPE_BATCH 批处理，TYPE_COMMIT 提交，TYPE_RELEASE 出品
	 * @param dbType 参考 FmEditLock.DB_TYPE_DAY 日库   DB_TYPE_MONTH 月库
	 * @param grids 要锁定的grid的列表
	 * @return lock的sequence id ； 0 表示没有锁定
	 * @throws Exception
	 */
	public int lockTargetDbGrid(int regionId,int lockObject,int lockType,String dbType,List<Integer> grids) throws Exception {
		DatalockApi datalockApi = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
		return datalockApi.lockGrid(regionId , lockObject, grids, lockType,dbType ,0);
	}
	/**根据grid锁的id，进行解锁
	 * @param lockSeqence lock的sequence id ； 0 表示没有锁定
	 * @param dbType 参考 FmEditLock.DB_TYPE_DAY 日库   DB_TYPE_MONTH 月库
	 * @throws Exception 
	 */
	public void unlockTargetDbGrid(int lockSeqence,String dbType ) throws Exception {
		if (0==lockSeqence) return ;//没有进行grid加锁，直接返回；
		DatalockApi datalockApi = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
		datalockApi.unlockGrid(lockSeqence,dbType);
	};
	/**
	 * 根据dbinfo，初始化数据库连接
	 * @param dbInfo
	 * @param autoCommit 是否设置数据库事务的autocommit，
	 * @return
	 * @throws Exception
	 */
	public Connection intiConenction(DbInfo dbInfo ,boolean autoCommit) throws Exception{
		Connection conn = createConnection(dbInfo);
		conn.setAutoCommit(autoCommit);
		return conn;
	}
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhMMss");
	/**在源数据库上面创建指向目标db的dblink
	 * @param sourceDbInfo
	 * @param targetDbInfo
	 * @return dblink的名称 dblink_yyyymmddhhMMss
	 * @throws Exception
	 */
	public String createTargetDbLink(DbInfo sourceDbInfo,DbInfo targetDbInfo) throws Exception{
		String dbLinkName = "dblink_" + sdf.format(new Date());
		OracleSchema oraSchema = new OracleSchema(DbConnectConfig.createConnectConfig(sourceDbInfo.getConnectParam()));
		DataSource dblinkContainer= oraSchema.getDriverManagerDataSource();
		new DbLinkCreator().create(dbLinkName, 
									true, 
									dblinkContainer , 
									targetDbInfo.getDbUserName(), 
									targetDbInfo.getDbUserPasswd(), 
									targetDbInfo.getDbServer().getIp(), 
									String.valueOf(targetDbInfo.getDbServer().getPort()), 
									targetDbInfo.getDbName());
		return dbLinkName;
	}
	/**
	 * @return 大区id和对应grid的mapping信息
	 * @throws Exception
	 */
	public List<Region> queryRegionGridsMapping(List<Integer> grids) throws Exception {
		ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
		List<Region> regionWithGridsList= manApi.queryRegionWithGrids(grids);
		return regionWithGridsList;
	}
	private Connection createConnection(DbInfo dbInfo ) throws Exception{
		OracleSchema oraSchema = new OracleSchema(DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
		return oraSchema.getDriverManagerDataSource().getConnection();
	}
	
	private  int prepareLog(Connection sourceDbConn,String prepareSql,List<Object> values)throws Exception{
		QueryRunner run = new QueryRunner();
		if (CollectionUtils.isEmpty(values)){
			return run.update(sourceDbConn, prepareSql);
		}
		return run.update(sourceDbConn, prepareSql,values.toArray());
	}
	private  int extendLogByRowId(Connection sourceDbConn, String extLogSql)throws SQLException{
		QueryRunner run = new QueryRunner();
		int result = run.update(sourceDbConn, extLogSql);
		if(result>0){
			return result+extendLogByRowId(sourceDbConn, extLogSql);
		}
		return result;
	}
	private  void lockPreparedLog(int rowCount,Connection sourceDbConn,String lockSql)throws LockException,SQLException{
		QueryRunner run = new QueryRunner();
		int result = run.update(sourceDbConn, lockSql);
		if(result<rowCount){
			throw new LockException("部分履历已经被其他回库操作锁定,请稍候再试。");
		}
	}
	
}

