package com.navinfo.dataservice.impcore.selector;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.impcore.exception.LockException;
import com.navinfo.dataservice.impcore.flushbylog.LogFlushUtil;

/*
 * @author gaopengrong
 * 2017年2月21日
 * 描述：import-corePoiDailyReleaseLogSelector.java
 */
public class FmRoadDailyReleaseLogSelector extends DeafultDailyReleaseLogSelector {
	public FmRoadDailyReleaseLogSelector(OracleSchema logSchema,List<Integer> grids) {
		super(logSchema,grids);
	}
	public FmRoadDailyReleaseLogSelector(OracleSchema logSchema,List<Integer> grids,Date stopTime) {
		super(logSchema,grids);
	}
	
	protected  SqlClause getPrepareSql(Connection conn) throws Exception{
		List<Object> values = new ArrayList<Object> ();
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(super.tempTable);
		sb.append(" SELECT DISTINCT P.OP_ID, P.OP_DT,P.OP_SEQ\r\n" + 
				"  FROM LOG_OPERATION P, LOG_DETAIL L, LOG_DETAIL_GRID T,LOG_DAY_RELEASE R\r\n" + 
				" WHERE P.OP_ID = L.OP_ID\r\n" + 
				"   AND L.ROW_ID = T.LOG_ROW_ID\r\n" + 
				"   AND P.OP_ID = R.OP_ID\r\n"); 
		sb.append(" AND R.REL_ALL_STA=0");
		if (this.stopTime!=null){
			String stopTimeSqlFormat = DateUtils.dateToString(stopTime, DateUtils.DATE_COMPACTED_FORMAT);
			sb.append("   and p.op_dt < to_date('"+stopTimeSqlFormat+"', 'yyyymmddhh24miss')\r\n") ;
		}
		if(this.getGrids()!=null&&this.getGrids().size()>0){
			SqlClause inClause = SqlClause.genInClauseWithMulInt(conn,this.getGrids()," T.GRID_ID ");
			if (inClause!=null){
				sb .append(" AND "+ inClause.getSql());
				values.addAll(inClause.getValues());
			}
		}else{
			sb .append(" AND T.GRID_ID in (null) ");
		}
		String gdbVesion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		List<String> tableNames = GlmCache.getInstance().getGlm(gdbVesion).getEditTableNames(GlmTable.FEATURE_TYPE_ROAD);
		sb.append(" AND L.TB_NM IN ('"+StringUtils.join(tableNames,"','")+"')");
		
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
		return sqlClause;
	}
	
	protected SqlClause getExtendLogSql(Connection conn) throws Exception{
		StringBuilder sb = new StringBuilder();
		sb.append("MERGE INTO ");
		sb.append(this.tempTable);
		sb.append(" T USING (SELECT DISTINCT P.OP_ID,P.OP_DT,P.OP_SEQ FROM LOG_OPERATION P,LOG_DETAIL L,LOG_DAY_RELEASE R WHERE EXISTS (SELECT 1 FROM LOG_DETAIL L1,");
		sb.append(this.tempTable);
		sb.append(" T1 WHERE L1.OP_ID=T1.OP_ID AND L1.TB_ROW_ID=L.TB_ROW_ID AND P.OP_DT<=T1.OP_DT) AND P.OP_ID=L.OP_ID AND P.OP_ID=R.OP_ID AND R.REL_ALL_STA=0) TP "
				+ "ON (T.OP_ID=TP.OP_ID) "
				+ "WHEN NOT MATCHED THEN INSERT VALUES (TP.OP_ID,TP.OP_DT,TP.OP_SEQ)");
		List<Object> values = new ArrayList<Object> ();
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
		return sqlClause;
	}
	

}

