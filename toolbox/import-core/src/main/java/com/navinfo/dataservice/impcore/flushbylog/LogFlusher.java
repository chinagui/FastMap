package com.navinfo.dataservice.impcore.flushbylog;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.sql.DataSource;

import oracle.spatial.util.WKT;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.edit.iface.DatalockApi;
import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.dataservice.commons.util.NaviListUtils;
import com.navinfo.dataservice.impcore.exception.LockException;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DbLinkCreator;

/*
 * @author mayunfei
 * 2016年6月8日
 * 描述：import-coreLogFlusher.java
 */
public abstract class LogFlusher {
	public static final String FEATURE_ALL="ALL";
	public static final String FEATURE_POI="POI";
	public static final String FEATURE_ROAD="ROAD";
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	private DbInfo sourceDbInfo;
	private DbInfo targetDbInfo;
	private Connection sourceDbConn;
	private Connection targetDbConn;
	private List<Integer> grids;
	private String stopTime;//履历生成的截止时间；yyyymmddhh24miss
	private String tempTable;
	private String tempFailLogTable;//刷履历失败的日志记录临时表
	private String featureType = FEATURE_ALL;
	private String targetDbLink;
	private int regionId;
	private int lockType;
	private int gridLockSeq;
	protected Connection getSourceDbConn() {
		return sourceDbConn;
	}
	protected List<Integer> getGrids() {
		return grids;
	}
	protected String getStopTime() {
		return stopTime;
	}
	protected String getTempTable() {
		return tempTable;
	}
	protected int getLockType() {
		return lockType;
	}

	/**
	 * @param regionInfo
	 * @param sourceDbInfo
	 * @param targetDbInfo
	 * @param grids//履历所在的grids列表；
	 * @param stopTime 履历截止时间；为空时，刷履历时，不进行履历生成时间的判断；
	 * @param featureType //ROAD POI
	 * @param lockType 参考FmEditLock 锁类型：1检查、2批处理、3借图幅、4还履历，5日落月
	 */
	public LogFlusher(int regionId,DbInfo sourceDbInfo, DbInfo targetDbInfo,
			List<Integer> grids, String stopTime,String featureType, int lockType) {
		super();
		this.sourceDbInfo = sourceDbInfo;
		this.targetDbInfo = targetDbInfo;
		this.grids = grids;
		this.stopTime = stopTime;
		this.featureType=featureType;
		this.regionId = regionId;
		this.lockType = lockType;
	}
	protected int getRegionId() {
		return regionId;
	}
	protected String getFeatureType() {
		return featureType;
	}
	protected int getLockObject() {
		if (FEATURE_ROAD.equals(this.featureType)){
			return FmEditLock.LOCK_OBJ_ROAD;
		}
		if (FEATURE_POI.equals(this.featureType)){
			return FmEditLock.LOCK_OBJ_POI;
		}
		return FmEditLock.LOCK_OBJ_ALL;
	}

	public void setLog(Logger log) {
		this.log = log;
	}
	public FlushResult perform() throws Exception{
		FlushResult flushResult= new FlushResult();
		int targetGridLockHookId =0;
		int sourceGridLockHookId =0;
		try{
			initConnections();//创建源、目标库的connection
			closeAutoCommit();//关闭autoCommit，手工控制数据库事务
			createTempTable();
			createTargetDbLink();
			createFailueLogTempTable();
			sourceGridLockHookId=lockSourceDbGrid();
			prepareAndLockLog();
			targetGridLockHookId = lockTargetDbGrid();
			flushResult = flushData(this.sourceDbConn,this.targetDbConn);
			recordFailLog2Temptable(flushResult);
			//修改全部履历的提交状态为“已提交”
			moveLog(flushResult, tempTable);
			updateLogCommitStatus(tempTable);
			commitAndCloseConnections();
		}catch(Exception e){
			this.log.warn("exception accured", e);
			flushResult.setResultMsg(e.getMessage());
			this.unlockPreparedLog();
			rollbackConnections();
			throw e ;
		}finally{
			unlockTargetDbGrid(targetGridLockHookId);
			dropMonthDbLink();
			unlockSourceDbGrid(sourceGridLockHookId);
			this.closeConnections();
		}
		return flushResult;
	}
	public abstract int lockSourceDbGrid() throws Exception ;
	public abstract void unlockSourceDbGrid(int lockHookId) ;
	public abstract void unlockTargetDbGrid(int lockHookId);
	public abstract int lockTargetDbGrid() throws Exception ;
	private  void unlockPreparedLog(){
		try{
			QueryRunner run = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE LOG_OPERATION L SET L.LOCK_STA=0 WHERE EXISTS (SELECT 1 FROM ");
			sb.append(tempTable);
			sb.append(" T WHERE L.OP_ID=T.OP_ID)");
			run.update(this.sourceDbConn, sb.toString());
			this.sourceDbConn.commit();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public  abstract SqlClause getPrepareSql() throws Exception;
	/**
	 * 实现类可以覆盖改方法的实现,根据刷履历的要素类型,实现preparesql的生成,以及 刷履历sql的定制化
	 * @return
	 */
	public String getFeatureFilter(){
		return " 1=1";
	}
	private void initConnections() throws Exception{
		this.sourceDbConn = createConnection(this.sourceDbInfo);
		this.targetDbConn = createConnection(this.targetDbInfo);
	}
	private Connection createConnection(DbInfo dbInfo ) throws Exception{
		OracleSchema oraSchema = new OracleSchema(DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
		return oraSchema.getDriverManagerDataSource().getConnection();
	}
	private void closeConnections(){
		DbUtils.closeQuietly(this.sourceDbConn);
		DbUtils.closeQuietly(this.targetDbConn);
	} 
	protected abstract  void updateLogCommitStatus(String tempTable) throws Exception ;
	/*{
		QueryRunner run = new QueryRunner();
		String sql = "update LOG_OPERATION set com_dt = sysdate,com_sta=1,LOCK_STA=0 where OP_ID IN (SELECT OP_ID FROM "+tempTable+")";
		run.execute(this.sourceDbConn, sql);
	}*/
	private void createTargetDbLink() throws Exception{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd");
		String dbLinkName = "dblink_" + sdf.format(new Date());
		OracleSchema oraSchema = new OracleSchema(DbConnectConfig.createConnectConfig(this.sourceDbInfo.getConnectParam()));
		DataSource dblinkContainer= oraSchema.getDriverManagerDataSource();
		new DbLinkCreator().create(dbLinkName, 
									true, 
									dblinkContainer , 
									this.targetDbInfo.getDbUserName(), 
									this.targetDbInfo.getDbUserPasswd(), 
									this.targetDbInfo.getDbServer().getIp(), 
									String.valueOf(this.targetDbInfo.getDbServer().getPort()), 
									oraSchema.getConnConfig().getServiceName());
		this.targetDbLink = dbLinkName;
	}
	/*
	      将成功的operation相关的履历搬移到目标库；
	      可以被子类重载复写
	 */
	protected  void moveLog(FlushResult flushResult, String tempTable) throws Exception {
		
		String dbLinkName=this.targetDbLink;
		Statement stmt =null;
		try{
			stmt = this.sourceDbConn.createStatement();
			String moveSql = "insert into log_detail@" + dbLinkName
					+ " select l.* from log_detail l,"+tempTable+" t where l.op_id=t.op_id"
					+" AND NOT EXISTS(SELECT 1 FROM "+this.tempFailLogTable+" f WHERE f.row_id=l.row_Id)";
			log.debug(moveSql);
			flushResult.setLogDetailMoved(stmt.executeUpdate(moveSql));
			
			moveSql = "INSERT INTO LOG_DETAIL_GRID@"+dbLinkName
					+" SELECT P.* FROM LOG_DETAIL_GRID P,LOG_DETAIL L,"+tempTable+" T WHERE L.OP_ID=T.OP_ID AND L.ROW_ID=P.LOG_ROW_ID"
					+" AND NOT EXISTS(SELECT 1 FROM "+this.tempFailLogTable+" f WHERE f.row_id=l.row_Id)";
			flushResult.setLogDetailGridMoved(stmt.executeUpdate(moveSql));	
			
			moveSql = "INSERT INTO LOG_OPERATION@"+dbLinkName
					+"(OP_ID,US_ID,OP_CMD,OP_DT) SELECT L.OP_ID,L.US_ID,L.OP_CMD,L.OP_DT FROM LOG_OPERATION L,"+tempTable+" T,LOG_DETAIL D WHERE L.OP_ID=T.OP_ID  AND L.OP_ID=D.OP_ID";
			flushResult.setLogOpMoved(stmt.executeUpdate(moveSql));
		}finally{
			DbUtils.closeQuietly(stmt);
		}
	
		

	}

	private void dropMonthDbLink(){
		String sqlDropDblink = "drop database link " + this.targetDbLink;
		try {
			QueryRunner run = new QueryRunner();
			run.execute(this.sourceDbConn, sqlDropDblink);
			OracleSchema oraSchema = new OracleSchema(DbConnectConfig.createConnectConfig(this.sourceDbInfo.getConnectParam()));
			DataSource dblinkContainer= oraSchema.getDriverManagerDataSource();
			new DbLinkCreator().drop(this.targetDbLink, 
										true, 
										dblinkContainer );
		} catch (Exception e) {
			this.log.warn(e.getMessage());
		}
		
	}
	
	private void recordFailLog2Temptable(FlushResult flushResult) throws Exception{
		if (flushResult.isSuccess()) return ;
		QueryRunner run = new QueryRunner();
		String sql = "insert into "+this.tempFailLogTable+" values(?,?)";
		Object[][] batchParams = NaviListUtils.toArrayMatrix(flushResult.getFailedLog());
		run.batch(this.sourceDbConn, sql, batchParams);
	}
	private FlushResult flushData(Connection sourceDbConn,Connection targetDbConn) throws Exception {
		String logQuerySql = "SELECT L.* FROM LOG_DETAIL L," + tempTable
				+ " T WHERE L.OP_ID=T.OP_ID "				
				+ " AND "+(StringUtils.isEmpty(this.getFeatureFilter())?"1=1":this.getFeatureFilter())
				+ " ORDER BY T.OP_DT"
				;
		this.log.debug(logQuerySql);
		Statement sourceStmt = null;
		ResultSet rs = null;
		try{
			sourceStmt = sourceDbConn.createStatement();
			rs = sourceStmt.executeQuery(logQuerySql);
			rs.setFetchSize(1000);
			FlushResult flushResult =new FlushResult();
			LogWriter logWriter = new LogWriter(targetDbConn);
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
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(sourceStmt);
		}

		
	}
	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	private  int prepareLog()throws Exception{
		QueryRunner run = new QueryRunner();
		SqlClause sqlClause = this.getPrepareSql();
		if (sqlClause==null) return 0;
		this.log.debug(sqlClause.getSql());
		if (sqlClause.getValues()==null || sqlClause.getValues().size()==0){
			return run.update(this.sourceDbConn, sqlClause.getSql());
		}
		return run.update(this.sourceDbConn, sqlClause.getSql(),sqlClause.getValues().toArray());
	}
	private  void prepareAndLockLog()throws LockException{
		try{
			int logOperationCount = 0;
			//2.select by conditions
			logOperationCount+=this.prepareLog();
			//3.
			if (CollectionUtils.isNotEmpty(this.grids)){//如果是全部回，则不扩履历
				logOperationCount+=extendLogByRowId();
			}
			lockPreparedLog(logOperationCount);
			this.sourceDbConn.commit();
		}catch(Exception e){
			throw new LockException("锁定要准备回库的履历时出现错误:"+e.getMessage(),e);
		}
	}
	private  void lockPreparedLog(int rowCount)throws LockException,SQLException{
		QueryRunner run = new QueryRunner();
		StringBuilder sb = getOperationLockSql();
		this.log.debug(sb);
		int result = run.update(this.sourceDbConn, sb.toString());
		if(result<rowCount){
			throw new LockException("部分履历已经被其他回库操作锁定,请稍候再试。");
		}
	}
	protected StringBuilder getOperationLockSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE LOG_OPERATION L SET L.LOCK_STA=1 WHERE EXISTS (SELECT 1 FROM ");
		sb.append(tempTable);
		sb.append(" T WHERE L.OP_ID=T.OP_ID) AND L.LOCK_STA=0");
		return sb;
	}
	private  int extendLogByRowId()throws SQLException{
		QueryRunner run = new QueryRunner();
		StringBuilder sb = getExtendLogSql();
		if(sb==null) return 0;
		int result = run.update(this.sourceDbConn, sb.toString());
		if(result>0){
			return result+extendLogByRowId();
		}
		return result;
	}
	public abstract StringBuilder getExtendLogSql() ;
	/**
	 * 创建临时表TEMP_LOG_OP_xxxx，用于存放
	 * @throws SQLException 
	 */
	private void createTempTable() throws SQLException {
		QueryRunner run = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		String tempTable = "TEMP_LOG_OP_"+new Random().nextInt(1000000);
		sb.append("CREATE TABLE ");
		sb.append(tempTable);
		sb.append("(OP_ID RAW(16),OP_DT TIMESTAMP)");
		run.execute(this.sourceDbConn, sb.toString());
		this.setTempTable(tempTable);	
		
	}
	private void createFailueLogTempTable()throws Exception{
		QueryRunner run = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		String tempTable = "TEMP_FAIL_LOG_"+new Random().nextInt(1000000);
		sb.append("CREATE TABLE ");
		sb.append(tempTable);
		sb.append("(OP_ID RAW(16),ROW_ID RAW(16))");
		run.execute(this.sourceDbConn, sb.toString());
		this.setTempFailLogTable(tempTable);
	}
	private void setTempTable(String tempTable) {
		this.tempTable = tempTable;
	}
	private void setTempFailLogTable(String tempTable) {
		this.tempFailLogTable = tempTable;
	}
	private void commitAndCloseConnections() throws SQLException {
		DbUtils.commitAndClose(this.sourceDbConn);
		DbUtils.commitAndClose(this.targetDbConn);
	}
	private void rollbackConnections() {
		try {
			DbUtils.rollback(this.sourceDbConn);
		} catch (SQLException e) {
			this.log.warn("exception catched when rollback sourceDbConn ");
		}
		try {
			DbUtils.rollback(this.targetDbConn);
		} catch (SQLException e) {
			this.log.warn("exception catched when rollback targetDbConn ");
		}
	}
	/**
	 * 将源、目标库的数据库连接设置autoCommit=False；
	 * 手工管理数据库的事务提交和回滚；
	 * @throws SQLException
	 */
	private void closeAutoCommit() throws SQLException {
		this.sourceDbConn.setAutoCommit(false);
		this.sourceDbConn.setAutoCommit(false);
	}
	protected String getGlmFeatureType(String featureType) {
		if (FEATURE_ROAD.equals(featureType)){
			return GlmTable.FEATURE_TYPE_ROAD;
		}
		if (FEATURE_POI.equals(featureType)){
			return GlmTable.FEATURE_TYPE_POI;
		}
		return GlmTable.FEATURE_TYPE_ALL;
	}
	
	

	
}

