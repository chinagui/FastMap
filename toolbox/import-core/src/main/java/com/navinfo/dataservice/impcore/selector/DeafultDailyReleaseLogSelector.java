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
 * 描述：import-coreDeafultDailyReleaseLogSelector.java
 */
public class DeafultDailyReleaseLogSelector extends DefaultLogSelector {
	List<Integer> grids =null;
	public DeafultDailyReleaseLogSelector(OracleSchema logSchema,List<Integer> grids) {
		super(logSchema);
		this.grids = grids;
		// TODO Auto-generated constructor stub
	}

	protected List<Integer> getGrids() {
		return grids;
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
				"   AND P.OP_ID = R.OP_ID"+ 
				"   AND L.OB_NM !='IX_POI'\r\n") ; 
		sb.append(" AND R.REL_ALL_STA=0");//POI和全要素 这里实现不同
		if(this.getGrids()!=null&&this.getGrids().size()>0){
			SqlClause inClause = SqlClause.genInClauseWithMulInt(conn,this.getGrids()," T.GRID_ID ");
			if (inClause!=null)
				sb .append(" AND "+ inClause.getSql());
			values.addAll(inClause.getValues());
		}
		sb.append(" union all");
		sb.append(" SELECT DISTINCT P.OP_ID, P.OP_DT,P.OP_SEQ\r\n" + 
				"  FROM LOG_OPERATION P, LOG_DETAIL L, LOG_DETAIL_GRID T,LOG_DAY_RELEASE R\r\n" + 
				" WHERE P.OP_ID = L.OP_ID\r\n" + 
				"   AND L.ROW_ID = T.LOG_ROW_ID\r\n" + 
				"   AND P.OP_ID = R.OP_ID\r\n" + 
				"   AND L.OB_NM ='IX_POI'\r\n" +
				"	AND EXISTS (SELECT 1 FROM POI_EDIT_STATUS E WHERE E.PID=L.OB_PID AND E.STATUS = 3)\r\n") ; 
		sb.append(" AND R.REL_ALL_STA=0");//POI和全要素 这里实现不同
		if(this.getGrids()!=null&&this.getGrids().size()>0){
			SqlClause inClause = SqlClause.genInClauseWithMulInt(conn,this.getGrids()," T.GRID_ID ");
			if (inClause!=null)
				sb .append(" AND "+ inClause.getSql());
			values.addAll(inClause.getValues());
		}
		SqlClause sqlClause = new SqlClause(sb.toString(),values);
		return sqlClause;
	}
	
	protected String getExtendLogSql() {
		StringBuilder sb = new StringBuilder();
		sb.append("MERGE INTO ");
		sb.append(this.tempTable);
		sb.append(" T USING (SELECT P.OP_ID,P.OP_DT,P.OP_SEQ FROM LOG_OPERATION P,LOG_DETAIL L,LOG_DAY_RELEASE R WHERE EXISTS (SELECT 1 FROM LOG_DETAIL L1,");
		sb.append(this.tempTable);
		sb.append(" T1 WHERE L1.OP_ID=T1.OP_ID AND L1.ROW_ID=L.ROW_ID AND P.OP_DT<=T1.OP_DT) AND P.OP_ID=L.OP_ID AND P.OP_ID=R.OP_ID "
				+ "AND R.REL_ALL_STA=0" //全要素和poi这的实现不同
				+ ") TP "
				+ "ON (T.OP_ID=TP.OP_ID) "
				+ "WHEN NOT MATCHED THEN INSERT VALUES (TP.OP_ID,TP.OP_DT,TP.OP_SEQ)");
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

	

}

