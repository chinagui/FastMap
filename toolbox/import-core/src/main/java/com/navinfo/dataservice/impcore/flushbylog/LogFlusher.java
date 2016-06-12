package com.navinfo.dataservice.impcore.flushbylog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.impcore.exception.LockException;
import com.navinfo.navicommons.database.QueryRunner;

/*
 * @author mayunfei
 * 2016年6月8日
 * 描述：import-coreLogFlusher.java
 */
public class LogFlusher {
	private static WKT wktUtil = new WKT();
	private DbInfo sourceDbInfo;
	private DbInfo targetDbInfo;
	private Connection sourceDbConn;
	private Connection targetDbConn;
	private List<Integer> grids;
	private String stopTime;//履历生成的截止时间；yyyymmddhh24miss
	private String tempTable;
	
	
	
	public LogFlusher(DbInfo sourceDbInfo, DbInfo targetDbInfo,
			List<Integer> grids, String stopTime) {
		super();
		this.sourceDbInfo = sourceDbInfo;
		this.targetDbInfo = targetDbInfo;
		this.grids = grids;
		this.stopTime = stopTime;
		
	}
	
	
	public FlushResult perform() throws Exception{
		FlushResult result= new FlushResult();
		try{
			initConnections();//创建源、目标库的connection
			closeAutoCommit();//关闭autoCommit，手工控制数据库事务
			createTempTable();
			prepareAndLockLog();
			String logQuerySql = "SELECT L.* FROM LOG_DETAIL L," + tempTable
					+ " T WHERE L.OP_ID=T.OP_ID ORDER BY T.OP_DT";
			FlushResult flushResult = flushData(logQuerySql);
			if (flushResult.isSuccess()) {
				moveLog(flushResult, tempTable);
				updateLogCommitStatus(tempTable);
				flushResult.setResultMsg("Success");
			} else {
				flushResult.setResultMsg("Fail");
				throw new Exception("刷数据失败。");
			}
			commitAndCloseConnections();
		}catch(Exception e){
			rollbackAndCloseConnections();
		}finally{
			this.closeConnections();
		}
		return result;
	}
	private void initConnections() throws Exception{
		this.sourceDbConn = createConnection(this.sourceDbInfo);
		this.targetDbConn = createConnection(this.targetDbInfo);
	}
	private Connection createConnection(DbInfo dbInfo ) throws Exception{
		return DriverManager.getConnection(
				"jdbc:oracle:thin:@" + dbInfo.getDbServer().getIp() + ":"
						+ dbInfo.getDbServer().getPort() + ":" + "orcl",
				dbInfo.getDbUserName(),
				dbInfo.getDbUserPasswd());
	}
	private void closeConnections(){
		DbUtils.closeQuietly(this.sourceDbConn);
		DbUtils.closeQuietly(this.targetDbConn);
	} 
	private  void updateLogCommitStatus(String tempTable) throws Exception {
		QueryRunner run = new QueryRunner();
		String sql = "update LOG_OPERATION set com_dt = sysdate,com_sta=1,LOCK_STA=0 where OP_ID IN (SELECT OP_ID FROM "+tempTable+")";
		run.execute(this.sourceDbConn, sql);
	}
	//FIXME:这里使用create，drop等ddl语句会导致事务提交，需要将相关的ddl语句进行提前或者滞后处理；
	private  void moveLog(FlushResult flushResult, String tempTable) throws Exception {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd");

		String dbLinkName = "dblink_" + sdf.format(new Date());

		String sqlCreateDblink = "create database link "
				+ dbLinkName
				+ "  connect to "
				+ this.targetDbInfo.getDbUserName()
				+ " identified by "
				+ this.targetDbInfo.getDbUserPasswd()
				+ "  using '(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = "
				+this.targetDbInfo.getDbServer().getIp()
				+ " )(PORT = "+this.targetDbInfo.getDbServer().getPort()+" )))(CONNECT_DATA = (SERVICE_NAME = orcl )))'";

		Statement stmt = this.sourceDbConn.createStatement();

		stmt.execute(sqlCreateDblink);
		
		String moveSql = "INSERT INTO LOG_OPERATION@"+dbLinkName
				+"(OP_ID,US_ID,OP_CMD,OP_DT) SELECT L.OP_ID,L.US_ID,L.OP_CMD,L.OP_DT FROM LOG_OPERATION L,"+tempTable+" T WHERE L.OP_ID=T.OP_ID";
		flushResult.setLogOpMoved(stmt.executeUpdate(moveSql));
		
		moveSql = "insert into log_detail@" + dbLinkName
				+ " select l.* from log_detail l,"+tempTable+" t where l.op_id=t.op_id";
		flushResult.setLogDetailMoved(stmt.executeUpdate(moveSql));
		
		moveSql = "INSERT INTO LOG_DETAIL_GRID@"+dbLinkName
				+" SELECT P.* FROM LOG_DETAIL_GRID P,LOG_DETAIL L,"+tempTable+" T WHERE L.OP_ID=T.OP_ID AND L.ROW_ID=P.LOG_ROW_ID";
		flushResult.setLogDetailGridMoved(stmt.executeUpdate(moveSql));		

		String sqlDropDblink = "drop database link " + dbLinkName;

		stmt.execute(sqlDropDblink);

	}
	private FlushResult flushData(String logQuerySql) throws Exception {

		Statement sourceStmt = this.sourceDbConn.createStatement();

		ResultSet rs = sourceStmt.executeQuery(logQuerySql);
		try{
			rs.setFetchSize(1000);

			FlushResult flushResult =new FlushResult();
			while (rs.next()) {

				flushResult .addTotal();

				int op_tp = rs.getInt("op_tp");

				String rowId = rs.getString("row_id");

				if (op_tp == 1) {// 新增

					flushResult.addInsertTotal();

					if (insertData(rs) == 0) {
						flushResult.addInsertFailed();

						flushResult.addInsertFailedRowId(rowId);
					}

				} else if (op_tp == 3) { // 修改

					flushResult.addUpdateTotal();

					if (updateData(rs) == 0) {
						flushResult.addUpdateFailed();

						flushResult.addUpdateFailedRowId(rowId);
					}

				} else if (op_tp == 2) { // 删除

					flushResult.addDeleteTotal();

					if (deleteData(rs) == 0) {
						flushResult.addDeleteFailed();

						flushResult.addDeleteFailedRowId(rowId);
					}
				}

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
	 * @throws SQLException
	 */
	private  int prepareLog()throws SQLException{
		QueryRunner run = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(tempTable);
		sb.append(" SELECT DISTINCT P.OP_ID,P.OP_DT FROM LOG_OPERATION P,LOG_DETAIL L,LOG_DETAIL_GRID T WHERE P.OP_ID=L.OP_ID AND L.ROW_ID=T.LOG_ROW_ID AND P.COM_STA = 0");
		if(StringUtils.isNotEmpty(stopTime)){
			sb.append(" AND P.OP_DT<=TO_DATE('");
			sb.append(stopTime+ "','yyyymmddhh24miss')"); 
		}
		if(grids!=null&&grids.size()>0){
			sb.append(" AND T.GRID_ID IN (");
			sb.append(StringUtils.join(grids, ","));
			sb.append(")");
		}
		return run.update(this.sourceDbConn, sb.toString());
	}
	private  void prepareAndLockLog()throws LockException{
		try{
			int logOperationCount = 0;
			//2.select by conditions
			logOperationCount+=this.prepareLog();
			//3.
			logOperationCount+=extendLogByRowId();
			lockPreparedLog(logOperationCount);
			this.sourceDbConn.commit();
		}catch(Exception e){
			throw new LockException("锁定要准备回库的履历时出现错误:"+e.getMessage(),e);
		}
	}
	private  void lockPreparedLog(int rowCount)throws LockException,SQLException{
		QueryRunner run = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE LOG_OPERATION L SET L.LOCK_STA=1 WHERE EXISTS (SELECT 1 FROM ");
		sb.append(tempTable);
		sb.append(" T WHERE L.OP_ID=T.OP_ID) AND L.LOCK_STA=0");
		int result = run.update(this.sourceDbConn, sb.toString());
		if(result<rowCount){
			throw new LockException("部分履历已经被其他回库操作锁定,请稍候再试。");
		}
	}
	private  int extendLogByRowId()throws SQLException{
		QueryRunner run = new QueryRunner();
		StringBuilder sb = new StringBuilder();
		sb.append("MERGE INTO ");
		sb.append(tempTable);
		sb.append(" T USING (SELECT P.OP_ID,P.OP_DT FROM LOG_OPERATION P,LOG_DETAIL L WHERE EXISTS (SELECT 1 FROM LOG_DETAIL L1,");
		sb.append(tempTable);
		sb.append(" T1 WHERE L1.OP_ID=T1.OP_ID AND L1.ROW_ID=L.ROW_ID AND P.OP_DT<=T1.OP_DT) AND P.OP_ID=L.OP_ID AND P.COM_STA=0) TP ON (T.OP_ID=TP.OP_ID) WHEN NOT MATCHED THEN INSERT VALUES (TP.OP_ID,TP.OP_DT)");
		int result = run.update(this.sourceDbConn, sb.toString());
		if(result>0){
			return result+extendLogByRowId();
		}
		return result;
	}
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
	private void setTempTable(String tempTable) {
		this.tempTable = tempTable;
	}
	private void commitAndCloseConnections() throws SQLException {
		DbUtils.commitAndClose(this.sourceDbConn);
		DbUtils.commitAndClose(this.targetDbConn);
	}
	private void rollbackAndCloseConnections() {
		DbUtils.rollbackAndCloseQuietly(this.sourceDbConn);
		DbUtils.rollbackAndCloseQuietly(this.targetDbConn);
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
	private int insertData(ResultSet rs) {

		StringBuilder sb = new StringBuilder("insert into ");

		PreparedStatement pstmt = null;

		try {
			String logRowId = rs.getString("row_id");

			String newValue = rs.getString("new");

			JSONObject json = JSONObject.fromObject(newValue);
			
			String tableName = rs.getString("tb_nm").toLowerCase();

			sb.append(tableName);

			sb.append(" (");

			Iterator<String> it = json.keys();

			int keySize = json.keySet().size();

			int tmpPos = 0;

			while (it.hasNext()) {
				if (++tmpPos < keySize) {
					sb.append(it.next());

					sb.append(",");
				} else {
					sb.append(it.next());
				}
			}

			sb.append(",u_record) ");

			sb.append("values(");

			it = json.keys();

			tmpPos = 0;

			while (it.hasNext()) {
				String keyName = it.next();

				sb.append(":");

				sb.append(++tmpPos);

				if (tmpPos < keySize) {

					sb.append(",");
				}
			}

			sb.append(",1)");

			it = json.keys();

			tmpPos = 0;

			pstmt = this.targetDbConn.prepareStatement(sb.toString());

			while (it.hasNext()) {
				tmpPos++;

				String keyName = it.next();

				Object valObj = json.get(keyName);

				if (!"geometry".equalsIgnoreCase(keyName)) {
					
					if(tableName.equals("ck_exception")){
						
						if("create_date".equalsIgnoreCase(keyName) || "update_date".equalsIgnoreCase(keyName))
						{
							Timestamp ts = new Timestamp( DateUtils.stringToLong(valObj.toString(), "yyyy-MM-dd HH:mm:ss"));
									
							pstmt.setTimestamp(tmpPos, ts);
						}
						else{
							pstmt.setObject(tmpPos, valObj);
						}
					}
					else{
						pstmt.setObject(tmpPos, valObj);
					}
				} else {
					
					if(tableName.equalsIgnoreCase("ck_exception")){
						pstmt.setObject(tmpPos, valObj);
					}
					else{
						JGeometry jg = wktUtil.toJGeometry(valObj.toString()
								.getBytes());
	
						jg.setSRID(8307);
	
						STRUCT s = JGeometry.store(jg, this.targetDbConn);
	
						pstmt.setObject(tmpPos, s);
					}
				}

			}

			int result = pstmt.executeUpdate();

//			logDetails.add("hextoraw('"+logRowId+"')");

			return result;
		} catch (Exception e) {
			System.out.println(sb.toString());
			e.printStackTrace();

			return 0;
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}
	}

	private int updateData(ResultSet rs) {

		PreparedStatement pstmt = null;

		StringBuilder sb = new StringBuilder("update ");

		try {
			String logRowId = rs.getString("row_id");

			String newValue = rs.getString("new");

			JSONObject json = JSONObject.fromObject(newValue);
			
			String tableName = rs.getString("tb_nm").toLowerCase();

			sb.append(tableName);

			sb.append(" set ");

			Iterator<String> it = json.keys();

			int keySize = json.keySet().size();

			int tmpPos = 0;

			while (it.hasNext()) {
				String keyName = it.next();

				Object valObj = json.get(keyName);

				sb.append(keyName);

				sb.append("=:");

				sb.append(++tmpPos);

				if (tmpPos < keySize) {

					sb.append(",");
				}
			}

			sb.append(",u_record=3 where row_id = hextoraw('");

			sb.append(rs.getString("tb_row_id"));

			sb.append("')");

			it = json.keys();

			tmpPos = 0;

			pstmt = this.targetDbConn.prepareStatement(sb.toString());

			while (it.hasNext()) {
				tmpPos++;

				String keyName = it.next();

				Object valObj = json.get(keyName);

				if (!"geometry".equalsIgnoreCase(keyName)) {

					pstmt.setObject(tmpPos, valObj);
				} else {
					
					if(tableName.equalsIgnoreCase("ck_exception")){
						pstmt.setObject(tmpPos, valObj);
					}
					else{
						JGeometry jg = wktUtil.toJGeometry(valObj.toString()
								.getBytes());
	
						jg.setSRID(8307);
	
						STRUCT s = JGeometry.store(jg, this.targetDbConn);
	
						pstmt.setObject(tmpPos, s);
					}
				}

			}

			int result = pstmt.executeUpdate();

//			logDetails.add("hextoraw('"+logRowId+"')");

			return result;

		} catch (Exception e) {
			System.out.println(sb.toString());
			e.printStackTrace();

			return 0;
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}
	}

	private int deleteData(ResultSet rs) {

		PreparedStatement pstmt = null;

		StringBuilder sb = new StringBuilder("update ");

		try {

			String logRowId = rs.getString("row_id");

			String sql = "update " + rs.getString("tb_nm")
					+ " set u_record = 2 where row_id =hextoraw('"
					+ rs.getString("tb_row_id") + "')";

			pstmt = this.targetDbConn.prepareStatement(sql);

			int result = pstmt.executeUpdate();

//			logDetails.add("hextoraw('"+logRowId+"')");

			return result;

		} catch (Exception e) {
			System.out.println(sb.toString());
			e.printStackTrace();

			return 0;
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}

	}
	
}

