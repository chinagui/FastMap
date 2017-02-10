package com.navinfo.dataservice.impcore.selector;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.impcore.exception.LockException;
import com.navinfo.dataservice.impcore.flushbylog.LogFlushUtil;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * 履历刷库选择器，选择后，生成op_id临时表，并锁定已选择的数据
* @ClassName: LogSelector 
* @author Xiao Xiaowen 
* @date 2016年6月23日 下午1:28:57 
* @Description: TODO
*  
*/
public abstract class LogSelector {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected OracleSchema logSchema;
	protected List<Integer> grids;
	protected List<Integer> filterGrids;
	protected Date stopTime;
	protected String tempTable;
	protected QueryRunner run;
	public LogSelector(OracleSchema logSchema){
		this.logSchema = logSchema;
		run = new QueryRunner();
	}
	/**
	 * 返回选中的履历op_id存放的临时表名
	 * @return
	 */
	public String select()throws Exception{
		Connection conn = null;
		try{
			int logOperationCount = 0;
			conn = logSchema.getPoolDataSource().getConnection();
			tempTable = LogFlushUtil.getInstance().createTempTable(conn);
			log.debug("Temp table Created:"+tempTable);
			logOperationCount+=selectLog(conn);
			logOperationCount+=extendLog(conn);
			String lockSql = getOperationLockSql();
			log.debug("logOperationCount:"+logOperationCount);
			log.debug("lockSql"+lockSql);
			if(StringUtils.isNotEmpty(lockSql)){
				int result = run.update(conn, lockSql);
				if(result<logOperationCount){
					throw new LockException("部分履历已经被其他回库操作锁定,请稍候再试。");
				}
			}
			return tempTable;
		}catch(Exception e){
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	protected String getOperationLockSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE LOG_OPERATION L SET L.LOCK_STA=1 WHERE EXISTS (SELECT 1 FROM ");
		sb.append(tempTable);
		sb.append(" T WHERE L.OP_ID=T.OP_ID) AND L.LOCK_STA=0");
		return sb.toString();
	}
	protected String getUnlockLogSql(boolean commitStatus){
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE LOG_OPERATION L SET L.LOCK_STA=0 ");
		if(commitStatus){
			sb.append(",L.COM_STA=1,L.COM_DT=SYSDATE");
		}
		sb.append(" WHERE EXISTS (SELECT 1 FROM ");
		sb.append(tempTable);
		sb.append(" T WHERE L.OP_ID=T.OP_ID)");
		return sb.toString();
	}
	protected abstract int selectLog(Connection conn)throws Exception;
	protected abstract int extendLog(Connection conn)throws Exception;
	/**
	 * 根据传入提交状态，如果提交失败，则将锁定状态解锁，如果提交成功，则解锁并将提交状态改为已提交
	 * @param commitStatus
	 * @throws Exception
	 */
	public void unselect(boolean commitStatus) throws Exception {
		String sql = getUnlockLogSql(commitStatus);
		if(StringUtils.isEmpty(sql))return;
		Connection conn = null;
		try{
			conn = logSchema.getPoolDataSource().getConnection();
			run.update(conn, getUnlockLogSql(commitStatus));
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public List<Integer> getFilterGrids() {
		return filterGrids;
	}
	public void setFilterGrids(List<Integer> filterGrids) {
		this.filterGrids = filterGrids;
	}
}
