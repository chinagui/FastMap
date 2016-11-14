package com.navinfo.dataservice.impcore.selector;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.sql.SqlClause;

/*
 * @author MaYunFei
 * 2016年7月21日
 * 描述：import-corePoiDailyReleaseLogSelector.java
 */
public class PoiDailyReleaseLogSelector extends DeafultDailyReleaseLogSelector {
	public PoiDailyReleaseLogSelector(OracleSchema logSchema,List<Integer> grids) {
		super(logSchema,grids);
	}
	protected  SqlClause getPrepareSql(Connection conn) throws Exception{
		List<Object> values = new ArrayList<Object> ();
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(super.tempTable);
		sb.append(" SELECT DISTINCT P.OP_ID, P.OP_DT\r\n" + 
				"  FROM LOG_OPERATION P, LOG_DETAIL L, LOG_DETAIL_GRID T,LOG_DAY_RELEASE R\r\n" + 
				" WHERE P.OP_ID = L.OP_ID\r\n" + 
				"   AND L.ROW_ID = T.LOG_ROW_ID\r\n" + 
				"   AND P.OP_ID = R.OP_ID\r\n" +
				"	AND EXISTS (SELECT 1 FROM POI_EDIT_STATUS E WHERE E.PID=L.OB_PID AND E.STATUS = 3)\r\n"); 
		sb.append(" AND R.REL_POI_STA=0");
		if(this.getGrids()!=null&&this.getGrids().size()>0){
			SqlClause inClause = SqlClause.genInClauseWithMulInt(conn,this.getGrids()," T.GRID_ID ");
			if (inClause!=null)
				sb .append(" AND "+ inClause.getSql());
			values.addAll(inClause.getValues());
		}
		String gdbVesion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		List<String> tableNames = GlmCache.getInstance().getGlm(gdbVesion).getEditTableNames(GlmTable.FEATURE_TYPE_POI);
		sb.append(" AND L.TB_NM IN ('"+StringUtils.join(tableNames,"','")+"')");
		
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
		return sqlClause;
	}
	
	protected String getExtendLogSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("MERGE INTO ");
		sb.append(this.tempTable);
		sb.append(" T USING (SELECT P.OP_ID,P.OP_DT FROM LOG_OPERATION P,LOG_DETAIL L,LOG_DAY_RELEASE R WHERE EXISTS (SELECT 1 FROM LOG_DETAIL L1,");
		sb.append(this.tempTable);
		sb.append(" T1 WHERE L1.OP_ID=T1.OP_ID AND L1.ROW_ID=L.ROW_ID AND P.OP_DT<=T1.OP_DT) AND P.OP_ID=L.OP_ID AND P.OP_ID=R.OP_ID AND R.REL_POI_STA=0) TP "
				+ "ON (T.OP_ID=TP.OP_ID) "
				+ "WHEN NOT MATCHED THEN INSERT VALUES (TP.OP_ID,TP.OP_DT)");
		return sb.toString();
	}
	

}

