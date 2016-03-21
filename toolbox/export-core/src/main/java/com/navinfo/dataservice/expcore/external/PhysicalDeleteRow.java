package com.navinfo.dataservice.expcore.external;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.thread.ThreadLocalContext;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.dataservice.expcore.sql.ExpSQL;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: RemoveDuplicateRow 
 * @author Xiao Xiaowen 
 * @date 2016-1-25 下午6:47:44 
 * @Description: TODO
 */
public class PhysicalDeleteRow {
	protected static Logger log = Logger.getLogger(PhysicalDeleteRow.class);
	public static void doDelete(Set<String> tables,OracleSchema schema)throws Exception{
		if(tables!=null&&tables.size()>0){
			List<ExpSQL> expSQLs = new ArrayList<ExpSQL>();
			for(String name:tables){
				ExpSQL expSQL = new ExpSQL();
				expSQL.setSql("DELETE FROM "+name+" WHERE U_RECORD=2");
				expSQLs.add(expSQL);
			}
			if(expSQLs.size()==1){
				Connection conn = null;
				try{
					conn = schema.getDriverManagerDataSource().getConnection();
					QueryRunner runner = new QueryRunner();
					runner.execute(conn, expSQLs.get(0).getSql());
				}catch(Exception e){
					DbUtils.rollbackAndCloseQuietly(conn);
					log.error(e.getMessage());
					throw e;
				}finally{
					DbUtils.commitAndCloseQuietly(conn);
				}
			}else if(expSQLs.size()>1){
				ExecuteExternlToolSql executor = new ExecuteExternlToolSql(schema);
				ThreadLocalContext ctx = new ThreadLocalContext(log);
				executor.execute(expSQLs, ctx);
			}
		}
	}
}
