package com.navinfo.dataservice.impcore.release.day;

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
public class ReleaseDailyLogFlusher extends LogFlusher {
	public ReleaseDailyLogFlusher(int regionId, DbInfo sourceDbInfo,
			DbInfo targetDbInfo, List<Integer> grids,String featureType) {
		super(regionId, sourceDbInfo, targetDbInfo, grids,null , 
				featureType,//LogFlusher.FEATURE_POI,
				FmEditLock.TYPE_RELEASE);
	}
	@Override
	public  SqlClause getPrepareSql() throws Exception{
		List<Object> values = new ArrayList<Object> ();
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(this.getTempTable());
		sb.append(" SELECT DISTINCT P.OP_ID, P.OP_DT\r\n" + 
				"  FROM LOG_OPERATION P, LOG_DETAIL L, LOG_DETAIL_GRID T,LOG_DAY_RELEASE R\r\n" + 
				" WHERE P.OP_ID = L.OP_ID\r\n" + 
				"   AND L.ROW_ID = T.LOG_ROW_ID\r\n" + 
//				"   AND P.COM_STA = 1\r\n" + 
				"   AND P.OP_ID = R.OP_ID\r\n") ; 
		sb.append(getRelStaClause());
		if(this.getGrids()!=null&&this.getGrids().size()>0){
			SqlClause inClause = SqlClause.genInClauseWithMulInt(this.getSourceDbConn(),this.getGrids()," T.GRID_ID ");
			if (inClause!=null){
				sb .append(" AND "+ inClause.getSql());
				values.addAll(inClause.getValues());
			}
		}
		sb.append(" AND "+this.getFeatureFilter());
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
		return sqlClause;
	}
	/**
	 * 根据出品的要素类型，获取出品状态的sql查询条件
	 * @return
	 */
	private String getRelStaClause(){
		String glmFeatureType = getGlmFeatureType(this.getFeatureType());
		if (GlmTable.FEATURE_TYPE_ALL.equals(glmFeatureType)){
			return "AND R.REL_ALL_STA=0";
		}
		return " AND R.REL_POI_STA=0";
	}
	@Override
	public String getFeatureFilter(){
		String glmFeatureType = getGlmFeatureType(this.getFeatureType());
		if (GlmTable.FEATURE_TYPE_ALL.equals(glmFeatureType)){
			return " 1=1 ";
		}
		String gdbVesion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		List<String> tableNames = GlmCache.getInstance().getGlm(gdbVesion).getEditTableNames(glmFeatureType);
		return " L.TB_NM IN ('"+StringUtils.join(tableNames,"','")+"')";
		
	}
	@Override
	public int lockSourceDbGrid() throws Exception {
		DatalockApi datalockApi = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
		int regionId = this.getRegionId();
		int lockObject=this.getLockObject();
		return datalockApi.lockGrid(regionId , lockObject, this.getGrids(), this.getLockType(),FmEditLock.DB_TYPE_DAY ,0);
	}
	@Override
	public void unlockSourceDbGrid(int lockHookId) {
		if (0==lockHookId) return ;//没有进行grid加锁，直接返回；
		try{
			DatalockApi datalockApi = (DatalockApi) ApplicationContextUtil.getBean("datalockApi");
			datalockApi.unlockGrid(lockHookId,FmEditLock.DB_TYPE_DAY);
		}catch(Exception e){
			this.log.warn("grid解锁时，出现异常", e);
		}
		
	}
	@Override
	public  StringBuilder getExtendLogSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("MERGE INTO ");
		sb.append(this.getTempTable());
		sb.append(" T USING (SELECT P.OP_ID,P.OP_DT FROM LOG_OPERATION P,LOG_DETAIL L,LOG_DAY_RELEASE R WHERE EXISTS (SELECT 1 FROM LOG_DETAIL L1,");
		sb.append(this.getTempTable());
		sb.append(" T1 WHERE L1.OP_ID=T1.OP_ID AND L1.ROW_ID=L.ROW_ID AND P.OP_DT<=T1.OP_DT) AND P.OP_ID=L.OP_ID AND P.OP_ID=R.OP_ID  "+getRelStaClause()+") TP "
				+ "ON (T.OP_ID=TP.OP_ID) "
				+ "WHEN NOT MATCHED THEN INSERT VALUES (TP.OP_ID,TP.OP_DT)");
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
	
	/* 修改日出品库的出品状态LOG_DAY_RELEASE表中<br/>
	 * poi要素类型，仅修改poi相关的出品状态、时间和锁；<br/>
	 * 全要素只修改全要素对应的相关的出品状态、时间和锁；<br/>
	 * @see com.navinfo.dataservice.impcore.flushbylog.LogFlusher#updateLogCommitStatus(java.lang.String)
	 */
	@Override
	protected void updateLogCommitStatus(String tempTable) throws Exception {
		QueryRunner run = new QueryRunner();
		String sql = "update LOG_DAY_RELEASE set rel_poi_dt = sysdate,rel_poi_sta=1,REL_POI_LOCK=0 where OP_ID IN (SELECT OP_ID FROM "+tempTable+")";
		String glmFeatureType = getGlmFeatureType(this.getFeatureType());
		if (GlmTable.FEATURE_TYPE_ALL.equals(glmFeatureType)){
			sql="update LOG_DAY_RELEASE set rel_all_dt = sysdate,rel_all_sta=1,REL_ALL_LOCK=0 where OP_ID IN (SELECT OP_ID FROM "+tempTable+")";
		}
		run.execute(this.getSourceDbConn(), sql);
	}
	@Override
	protected StringBuilder getOperationLockSql() {
		String glmFeatureType = getGlmFeatureType(this.getFeatureType());
		//如果是全要素，则更新LOCK_ALL_STA=1 
		if (GlmTable.FEATURE_TYPE_ALL.equals(glmFeatureType)){
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE LOG_DAY_RELEASE L SET L.REL_ALL_LOCK=1 WHERE EXISTS (SELECT 1 FROM ");
			sb.append(this.getTempTable());
			sb.append(" T WHERE L.OP_ID=T.OP_ID) AND L.REL_ALL_LOCK=0");
			return sb;
		} 
		//如果是POI日出品则只更新LOCK_POI_STA=1
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE LOG_DAY_RELEASE L SET L.REL_POI_LOCK=1 WHERE EXISTS (SELECT 1 FROM ");
		sb.append(this.getTempTable());
		sb.append(" T WHERE L.OP_ID=T.OP_ID) AND L.REL_POI_LOCK=0");
		return sb;
	}
}

