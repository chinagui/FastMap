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
public class FmPoiDailyReleaseLogSelector extends DeafultDailyReleaseLogSelector {
	public FmPoiDailyReleaseLogSelector(OracleSchema logSchema,List<Integer> grids) {
		super(logSchema,grids);
	}
	
	public FmPoiDailyReleaseLogSelector(OracleSchema logSchema,List<Integer> grids,Date stopTime) {
		super(logSchema,grids);
		this.stopTime = stopTime;
		
	}
	
	
	/**
	 *  快线任务范围内的履历（1、快线项目关闭范围，Road提该范围全部未出品履历，POI按对象提该范围已完成粗编且未提交出品的履历；）
	 *  加上剩余范围的履历（剩余其他范围，POI按对象提该范围已完成粗编且已完成日落月的履历。）
	 * @return
	 * @throws Exception 
	 */
	@Override
	protected int selectLog(Connection conn) throws Exception{
		if(this.getGrids()!=null&&this.getGrids().size()>0){
			return super.selectLog(conn)+allLogOperation(conn);
		}else{
			return allLogOperation(conn);
		}
	}
	
	/**
	 * 将剩余范围的履历补充进去
	 * @param tempTable 履历初步筛选生成的临时表，记录op_id,op_dt;
	 * @return 
	 * @throws SQLException 
	 */
	private int allLogOperation(Connection conn) throws SQLException {
		StringBuilder sb = new StringBuilder();
		List<Object> values = new ArrayList<Object> ();
		sb.append("MERGE INTO ");
		sb.append(super.tempTable);
		sb.append(" T USING (SELECT DISTINCT P.OP_ID, P.OP_DT,P.OP_SEQ\r\n" + 
				"  FROM LOG_OPERATION P, LOG_DETAIL L, LOG_DETAIL_GRID T,LOG_DAY_RELEASE R\r\n" + 
				" WHERE P.OP_ID = L.OP_ID\r\n" + 
				"   AND L.ROW_ID = T.LOG_ROW_ID\r\n" + 
				"   AND P.OP_ID = R.OP_ID\r\n" +
				"	AND EXISTS (SELECT 1 FROM POI_EDIT_STATUS E WHERE E.PID=L.OB_PID AND E.STATUS = 3)\r\n"); 
		sb.append(" AND R.REL_POI_STA=0");
		//sb.append(" AND P.com_sta=1");
		if (this.stopTime!=null){
			String stopTimeSqlFormat = DateUtils.dateToString(stopTime, DateUtils.DATE_COMPACTED_FORMAT);
			sb.append("   and p.op_dt < to_date('"+stopTimeSqlFormat+"', 'yyyymmddhh24miss')\r\n") ;
		}
		String gdbVesion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		List<String> tableNames = GlmCache.getInstance().getGlm(gdbVesion).getEditTableNames(GlmTable.FEATURE_TYPE_POI);
		sb.append(" AND L.TB_NM IN ('"+StringUtils.join(tableNames,"','")+"')) TP "
				+ "ON (T.OP_ID=TP.OP_ID) "
				+ "WHEN NOT MATCHED THEN INSERT VALUES (TP.OP_ID,TP.OP_DT,TP.OP_SEQ)");
		
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
		log.info("allLogOperation:"+sqlClause);
		int count = sqlClause.update(conn);
		return count;
		
	}
	//先筛选出来快线范围内的数据
	protected  SqlClause getPrepareSql(Connection conn) throws Exception{
		List<Object> values = new ArrayList<Object> ();
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(super.tempTable);
		sb.append(" SELECT DISTINCT P.OP_ID, P.OP_DT,P.OP_SEQ\r\n" + 
				"  FROM LOG_OPERATION P, LOG_DETAIL L, LOG_DETAIL_GRID T,LOG_DAY_RELEASE R\r\n" + 
				" WHERE P.OP_ID = L.OP_ID\r\n" + 
				"   AND L.ROW_ID = T.LOG_ROW_ID\r\n" + 
				"   AND P.OP_ID = R.OP_ID\r\n" +
				"	AND EXISTS (SELECT 1 FROM POI_EDIT_STATUS E WHERE E.PID=L.OB_PID AND E.STATUS = 3)\r\n"); 
		sb.append(" AND R.REL_POI_STA=0");
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
		}
		String gdbVesion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		List<String> tableNames = GlmCache.getInstance().getGlm(gdbVesion).getEditTableNames(GlmTable.FEATURE_TYPE_POI);
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
		sb.append(" T1 WHERE L1.OP_ID=T1.OP_ID AND L1.TB_ROW_ID=L.TB_ROW_ID AND P.OP_DT<=T1.OP_DT) AND P.OP_ID=L.OP_ID AND P.OP_ID=R.OP_ID AND R.REL_POI_STA=0) TP "
				+ "ON (T.OP_ID=TP.OP_ID) "
				+ "WHEN NOT MATCHED THEN INSERT VALUES (TP.OP_ID,TP.OP_DT,TP.OP_SEQ)");
		List<Object> values = new ArrayList<Object> ();
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
		return sqlClause;
	}
	

}

