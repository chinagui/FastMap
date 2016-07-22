package com.navinfo.dataservice.impcore.selector;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.sql.SqlClause;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * 默认的履历选择器，只是根据履历的逻辑，不包含选择表，选择编辑状态等业务
* @ClassName: DefaultLogSelector 
* @author Xiao Xiaowen 
* @date 2016年6月23日 下午1:52:10 
* @Description: TODO
*  
*/
public class DefaultLogSelector extends LogSelector {
	public DefaultLogSelector(OracleSchema logSchema) {
		super(logSchema);
	}
	@Override
	protected int selectLog(Connection conn) throws Exception{
		QueryRunner run = new QueryRunner();
		SqlClause sqlClause = this.getPrepareSql(conn);
		this.log.debug(sqlClause.getSql());
		if (sqlClause==null) return 0;
		if (sqlClause.getValues()==null || sqlClause.getValues().size()==0){
			return run.update(conn, sqlClause.getSql());
		}
		return run.update(conn, sqlClause.getSql(),sqlClause.getValues().toArray());
	}
	@Override
	protected int extendLog(Connection conn)throws Exception{
		int result = run.update(conn, getExtendLogSql());
		if(result>0){
			return result+extendLog(conn);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.impcore.selector.LogSelector#unselect(boolean)
	 */
	@Override
	public void unselect(boolean commitStatus) throws Exception {
		Connection conn = null;
		try{
			conn = logSchema.getPoolDataSource().getConnection();
			run.update(conn, getUnlockLogSql(commitStatus));
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(),e);
		}finally{
			DbUtils.rollbackAndCloseQuietly(conn);
		}
	}
	
	protected SqlClause getPrepareSql(Connection conn) throws Exception{
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(tempTable);
		sb.append(" SELECT DISTINCT P.OP_ID,P.OP_DT FROM LOG_OPERATION P,LOG_DETAIL L,LOG_DETAIL_GRID T WHERE P.OP_ID=L.OP_ID AND L.ROW_ID=T.LOG_ROW_ID AND  P.COM_STA=0 ");
		if(stopTime!=null){
			sb.append(" AND P.OP_DT<=TO_DATE('");
			sb.append(DateUtils.dateToString(stopTime, DateUtils.DATE_COMPACTED_FORMAT)+ "','yyyymmddhh24miss')"); 
		}
		List<Object> values = new ArrayList<Object> ();
		if(grids!=null&&grids.size()>0){
			SqlClause inClause = SqlClause.genInClauseWithMulInt(conn,grids," T.GRID_ID ");
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
		sb.append(tempTable);
		sb.append(" T USING (SELECT P.OP_ID,P.OP_DT FROM LOG_OPERATION P,LOG_DETAIL L WHERE EXISTS (SELECT 1 FROM LOG_DETAIL L1,");
		sb.append(tempTable);
		sb.append(" T1 WHERE L1.OP_ID=T1.OP_ID AND L1.ROW_ID=L.ROW_ID AND P.OP_DT<=T1.OP_DT) AND P.OP_ID=L.OP_ID AND P.COM_STA=0) TP ON (T.OP_ID=TP.OP_ID) WHEN NOT MATCHED THEN INSERT VALUES (TP.OP_ID,TP.OP_DT)");
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
