package com.navinfo.dataservice.impcore.release.mon;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.edit.iface.DatalockApi;
import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.dataservice.impcore.flushbylog.LogFlusher;
import com.navinfo.navicommons.database.QueryRunner;

/*
 * @author MaYunFei
 * 2016年6月17日
 * 描述：import-coreDay2MonLogFlusher.java
 */
/**
 * 出品刷履历：从gdb+库到idb月库
 * @author MaYunFei
 * 2016年6月18日
 */
public class ReleaseMonthlyLogFlusher extends LogFlusher {
	public ReleaseMonthlyLogFlusher(DbInfo sourceDbInfo,
			DbInfo targetDbInfo, List<Integer> grids, String stopTime) {
		super(0, sourceDbInfo, targetDbInfo, grids, stopTime, 
				LogFlusher.FEATURE_ALL,
				FmEditLock.TYPE_RELEASE);
	}
	@Override
	public  SqlClause getPrepareSql() throws Exception{
		List<Object> values = new ArrayList<Object> ();
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(this.getTempTable());
		sb.append(" SELECT DISTINCT P.OP_ID,P.OP_DT "
				+ "FROM LOG_OPERATION P,LOG_DETAIL L,LOG_DETAIL_GRID T "
				+ "WHERE P.OP_ID=L.OP_ID "
				+ "	AND L.ROW_ID=T.LOG_ROW_ID "
				+ " AND P.COM_STA=0 "
				);
		if(StringUtils.isNotEmpty(this.getStopTime())){
			sb.append(" AND P.OP_DT<=TO_DATE('");
			sb.append(this.getStopTime()+ "','yyyymmddhh24miss')"); 
		}
		if(this.getGrids()!=null&&this.getGrids().size()>0){
			SqlClause inClause = SqlClause.genInClauseWithMulInt(this.getSourceDbConn(),this.getGrids()," T.GRID_ID ");
			if (inClause!=null){
				sb .append(" AND "+ inClause.getSql());
				values.addAll(inClause.getValues());
			}
		}
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
		return sqlClause;
	}
	
	@Override
	public int lockSourceDbGrid() throws Exception {
		return 0;//月出品不锁定GDB+的grid；
	}
	@Override
	public void unlockSourceDbGrid(int lockHookId) {
		//月出品不锁定GDB+的grid；，也不解锁；		
	}
	@Override
	public  StringBuilder getExtendLogSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("MERGE INTO ");
		sb.append(this.getTempTable());
		sb.append(" T USING (SELECT P.OP_ID,P.OP_DT FROM LOG_OPERATION P,LOG_DETAIL L WHERE EXISTS (SELECT 1 FROM LOG_DETAIL L1,");
		sb.append(this.getTempTable());
		sb.append(" T1 WHERE L1.OP_ID=T1.OP_ID AND L1.ROW_ID=L.ROW_ID AND P.OP_DT<=T1.OP_DT) AND P.OP_ID=L.OP_ID AND P.COM_STA=0) TP ON (T.OP_ID=TP.OP_ID) WHEN NOT MATCHED THEN INSERT VALUES (TP.OP_ID,TP.OP_DT)");
		return sb;
	}
	@Override
	public int lockTargetDbGrid() throws Exception {
		return 0;//不进行出品库的grid加锁
	}
	@Override
	public void unlockTargetDbGrid(int lockHookId) {
		//不进行出品库的grid加锁
	}
	
	@Override
	protected void updateLogCommitStatus(String tempTable) throws Exception {
		QueryRunner run = new QueryRunner();
		String sql = "update LOG_OPERATION set com_dt = sysdate,com_sta=1,LOCK_STA=0 where OP_ID IN (SELECT OP_ID FROM "+tempTable+")";
		run.execute(this.getSourceDbConn(), sql);
	}
}

