package com.navinfo.dataservice.impcore.commit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.dataservice.impcore.flushbylog.LogFlusher;

/*
 * @author MaYunFei
 * 2016年6月17日
 * 描述：import-coreDay2MonLogFlusher.java
 */
public class AllFeatureLogFlusher extends LogFlusher {
	public AllFeatureLogFlusher(int regionId, DbInfo sourceDbInfo,
			DbInfo targetDbInfo,List<Integer> grids, String stopTime) {
		super(regionId, sourceDbInfo, targetDbInfo, grids, stopTime, LogFlusher.FEATURE_ALL,
				FmEditLock.TYPE_DEFAULT);
	}
	@Override
	public  SqlClause getPrepareSql() throws Exception{
		List<Object> values = new ArrayList<Object> ();
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(this.getTempTable());
		sb.append(" SELECT DISTINCT P.OP_ID,P.OP_DT FROM LOG_OPERATION P,LOG_DETAIL L,LOG_DETAIL_GRID T WHERE P.OP_ID=L.OP_ID AND L.ROW_ID=T.LOG_ROW_ID AND P.COM_STA = 0");
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
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void unlockSourceDbGrid(int lockHookId) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void unlockTargetDbGrid(int lockHookId) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int lockTargetDbGrid() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public StringBuilder getExtendLogSql() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	protected void updateLogCommitStatus(String tempTable) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
}

