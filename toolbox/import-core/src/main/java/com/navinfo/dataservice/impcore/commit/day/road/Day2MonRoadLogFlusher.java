package com.navinfo.dataservice.impcore.commit.day.road;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.edit.iface.DatalockApi;
import com.navinfo.dataservice.api.edit.model.FmEditLock;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.dataservice.impcore.flushbylog.LogFlusher;
import com.navinfo.navicommons.database.QueryRunner;

/*
 * @author MaYunFei
 * 2016年6月17日
 * 描述：import-coreDay2MonLogFlusher.java
 */
public class Day2MonRoadLogFlusher extends LogFlusher {
	public Day2MonRoadLogFlusher(int regionId, DbInfo sourceDbInfo,
			DbInfo targetDbInfo, List<Integer> grids, String stopTime) {
		super(regionId, sourceDbInfo, targetDbInfo, grids, stopTime, LogFlusher.FEATURE_ROAD,
				FmEditLock.TYPE_COMMIT);
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
		
		sb.append(" AND " + this.getFeatureFilter());
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
		return sqlClause;
	}
	@Override
	public String getFeatureFilter(){
		String gdbVesion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		List<String> tableNames = GlmCache.getInstance().getGlm(gdbVesion).getEditTableNames(GlmTable.FEATURE_TYPE_ROAD);
		return "  L.TB_NM IN ('"+StringUtils.join(tableNames,"','")+"')";
		
	}
	@Override
	public int lockTargetDbGrid() throws Exception {
		DatalockApi datalockApi = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
		int regionId = this.getRegionId();
		int lockObject=this.getLockObject();
		return datalockApi.lockGrid(regionId , lockObject, this.getGrids(), this.getLockType(),FmEditLock.DB_TYPE_MONTH,0);
	}
	@Override
	public void unlockTargetDbGrid(int lockHookId) {
		if (0==lockHookId) return ;//没有进行grid加锁，直接返回；
		try{
			DatalockApi datalockApi = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
			datalockApi.unlockGrid(lockHookId,FmEditLock.DB_TYPE_MONTH);
		}catch(Exception e){
			this.log.warn("grid解锁时，出现异常", e);
		}
		
	};
	@Override
	public int lockSourceDbGrid() {
		return 0;//不进行sourcedb的grid加锁
	}
	@Override
	public void unlockSourceDbGrid(int lockHookId) {
		// 不进行sourcedb的grid加锁解锁
		
	}
	@Override
	public StringBuilder getExtendLogSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("MERGE INTO ");
		sb.append(this.getTempTable());
		sb.append(" T USING (SELECT P.OP_ID,P.OP_DT FROM LOG_OPERATION P,LOG_DETAIL L WHERE EXISTS (SELECT 1 FROM LOG_DETAIL L1,");
		sb.append(this.getTempTable());
		sb.append(" T1 WHERE L1.OP_ID=T1.OP_ID AND L1.ROW_ID=L.ROW_ID AND P.OP_DT<=T1.OP_DT) AND P.OP_ID=L.OP_ID AND P.COM_STA=0) TP ON (T.OP_ID=TP.OP_ID) WHEN NOT MATCHED THEN INSERT VALUES (TP.OP_ID,TP.OP_DT)");
		return sb;
	}
	@Override
	protected void updateLogCommitStatus(String tempTable) throws Exception {
		QueryRunner run = new QueryRunner();
		String sql = "update LOG_OPERATION set com_dt = sysdate,com_sta=1,LOCK_STA=0 where OP_ID IN (SELECT OP_ID FROM "+tempTable+")";
		run.execute(this.getSourceDbConn(), sql);
		
	}
}

