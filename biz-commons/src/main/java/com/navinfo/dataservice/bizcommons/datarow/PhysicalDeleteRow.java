package com.navinfo.dataservice.bizcommons.datarow;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.sql.ExpSQL;
import com.navinfo.dataservice.bizcommons.datarow.SQLMultiThreadExecutor;
import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: RemoveDuplicateRow 
 * @author Xiao Xiaowen 
 * @date 2016-1-25 下午6:47:44 
 * @Description: TODO
 */
public class PhysicalDeleteRow {
	protected static Logger log = LoggerRepos.getLogger(PhysicalDeleteRow.class);
	public static void doDelete(OracleSchema schema)throws Exception{
		Set<String> tables = new HashSet<String>();
		String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
		Glm glm = GlmCache.getInstance().getGlm(gdbVersion);
		tables.addAll(glm.getEditTableNames(GlmTable.FEATURE_TYPE_ALL));
//		tables.addAll(glm.getExtendTableNames(GlmTable.FEATURE_TYPE_ALL));
		doDelete(tables,schema);
	}
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
				ThreadLocalContext ctx = new ThreadLocalContext(log);
				SQLMultiThreadExecutor.execute(schema.getDriverManagerDataSource(),expSQLs, ctx);
			}
			log.debug("物理删除逻辑删除的数据已完成");
		}
	}
}
