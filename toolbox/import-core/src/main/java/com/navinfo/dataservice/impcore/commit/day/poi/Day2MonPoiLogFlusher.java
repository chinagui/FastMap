package com.navinfo.dataservice.impcore.commit.day.poi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.ResultSetHandler;
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
public class Day2MonPoiLogFlusher extends LogFlusher {
	public Day2MonPoiLogFlusher(int regionId, DbInfo sourceDbInfo,
			DbInfo targetDbInfo) {
		super(regionId, sourceDbInfo, targetDbInfo, null, null, LogFlusher.FEATURE_POI,
				FmEditLock.TYPE_COMMIT);
	}
	@Override
	public  SqlClause getPrepareSql() throws Exception{
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ");
			sb.append(this.getTempTable());
			sb.append(" SELECT DISTINCT P.OP_ID,P.OP_DT FROM LOG_OPERATION P,LOG_DETAIL L,LOG_DETAIL_GRID T  WHERE P.OP_ID=L.OP_ID AND L.ROW_ID=T.LOG_ROW_ID AND P.COM_STA = 0 ");
			sb.append(" AND "+this.getFeatureFilter());
			sb.append(" AND EXISTS(SELECT 1 FROM IX_POI P,POI_EDIT_STATUS I WHERE L.TB_ROW_ID=P.ROW_ID AND P.PID=I.PID AND I.STATUS=3)");
			SqlClause sqlClause = new SqlClause(sb.toString(),null);
			return sqlClause;
	}
	@Override
	public String getFeatureFilter(){
		String gdbVesion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		List<String> tableNames = GlmCache.getInstance().getGlm(gdbVesion).getEditTableNames(GlmTable.FEATURE_TYPE_POI);
		return "  L.TB_NM IN ('"+StringUtils.join(tableNames,"','")+"')";
		
	}
	@Override
	public int lockSourceDbGrid() throws Exception {
		//不锁
		return 0;
	}
	@Override
	public void unlockSourceDbGrid(int lockHookId) {
		// 不锁也不解锁
		
	}
	@Override
	public void unlockTargetDbGrid(int lockHookId) {
		// 解锁月库
		if (0==lockHookId) return ;//没有进行grid加锁，直接返回；
		try{
			DatalockApi datalockApi = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
			datalockApi.unlockGrid(lockHookId,FmEditLock.DB_TYPE_MONTH);
		}catch(Exception e){
			this.log.warn("grid解锁时，出现异常", e);
		}
		
	}
	@Override
	public int lockTargetDbGrid() throws Exception {
		// 锁定月库
		DatalockApi datalockApi = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
		int regionId = this.getRegionId();
		int lockObject=this.getLockObject();
		Collection<Integer> gridsToLock = queryGrids2Lock();
		if (CollectionUtils.isEmpty(gridsToLock)) return 0;//如果没有要锁定的grids，则返回
		return datalockApi.lockGrid(regionId , lockObject, gridsToLock, this.getLockType(),FmEditLock.DB_TYPE_MONTH ,0);
	}
	/**
	 * 获取要锁定的grid的列表，因为poi日落月时，没有grids参数；只能通过履历来计算
	 * @return
	 * @throws SQLException
	 */
	private Collection<Integer> queryGrids2Lock() throws SQLException {
		String sql = "select DISTINCT G.GRID_ID AS GRID_ID\r\n" + 
				"  from LOG_OPERATION      P,\r\n" + 
				"       LOG_DETAIL         L,\r\n" + 
				"       LOG_DETAIL_GRID    G,\r\n" + 
				"       "+this.getTempTable()+" T \r\n" + 
				" WHERE P.OP_ID = L.OP_ID\r\n" + 
				"   AND L.ROW_ID = G.LOG_ROW_ID\r\n" + 
				"   AND P.OP_ID = T.OP_ID";
		QueryRunner run = new QueryRunner();
		ResultSetHandler<List<Integer>> rsl = new ResultSetHandler<List<Integer>>(){

			@Override
			public List<Integer> handle(ResultSet rs) throws SQLException {
				List<Integer> grids = new ArrayList<Integer>();
				while(rs.next()){
					grids.add(rs.getInt("GRID_ID"));
				}
				return grids;
			}};
		return run.query(this.getSourceDbConn(), sql,rsl );
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

