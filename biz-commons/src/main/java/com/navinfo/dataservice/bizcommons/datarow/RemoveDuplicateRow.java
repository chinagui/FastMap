package com.navinfo.dataservice.bizcommons.datarow;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.bizcommons.sql.ExpSQL;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: RemoveDuplicateRow 
 * @author Xiao Xiaowen 
 * @date 2016-1-25 下午6:47:44 
 * @Description: TODO
 */
public class RemoveDuplicateRow {
	protected static Logger log = Logger.getLogger(RemoveDuplicateRow.class);
	/**
	 * 库中数据去重
	 * 去重表来源于glm模型可编辑部分
	 * @param schema
	 * @throws Exception
	 */
	public static void removeDup(OracleSchema schema)throws Exception{
		List<String> tables = new ArrayList<String>();
		String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		Glm glm = GlmCache.getInstance().getGlm(gdbVersion);
		tables.addAll(glm.getEditTableNames(GlmTable.FEATURE_TYPE_ALL));
		removeDup(tables,schema);
	}
	/**
	 * 指定表数据去重
	 * 暂时只支持NI_VAL_EXCEPTION去重
	 * @param tables
	 * @param schema
	 * @throws Exception
	 */
	public static void removeDup(List<String> tables,OracleSchema schema)throws Exception{
		if(tables!=null&&tables.size()>0){
			List<ExpSQL> expSQLs = new ArrayList<ExpSQL>();
			for(String name:tables){
				ExpSQL expSQL = new ExpSQL();
				if("NI_VAL_EXCEPTION".equals(name)){
					expSQL.setSql("DELETE FROM NI_VAL_EXCEPTION WHERE ROWID NOT IN (SELECT MAX(ROWID) FROM NI_VAL_EXCEPTION GROUP BY MD5_CODE)");
					expSQLs.add(expSQL);
				}else{
//					StringBuilder sb = new StringBuilder();
//					sb.append("DELETE FROM ");
//					sb.append(name);
//					sb.append(" P WHERE P.ROWID NOT IN (SELECT MAX(T.ROWID) FROM ");
//					sb.append(name);
//					sb.append(" T WHERE P.ROW_ID=T.ROW_ID)");
				}
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
				ThreadLocalContext ctx = new ThreadLocalContext(log);
				SQLMultiThreadExecutor.execute(schema.getDriverManagerDataSource(),expSQLs, ctx);
			}
		}
	}
}
