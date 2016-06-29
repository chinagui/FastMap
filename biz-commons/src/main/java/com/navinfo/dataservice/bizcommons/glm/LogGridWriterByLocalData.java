package com.navinfo.dataservice.bizcommons.glm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName: LogGridWrite 
* @author Xiao Xiaowen 
* @date 2016年6月28日 下午4:18:26 
* @Description: TODO
*  
*/
public class LogGridWriterByLocalData {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	protected OracleSchema logSchema;
	public LogGridWriterByLocalData(OracleSchema logSchema){
		this.logSchema=logSchema;
	}
	public void write()throws Exception{
		//1. 确定tables
		Connection conn = null;
		try{
			conn = logSchema.getPoolDataSource().getConnection();
			String sql = "SELECT DISTINCT TB_NM FROM LOG_DETAIL";
			Collection<String> tables = new QueryRunner().query(conn, sql, new ResultSetHandler<Collection<String>>(){

				@Override
				public Collection<String> handle(ResultSet rs) throws SQLException {
					Collection<String> results = new HashSet<String>();
					while(rs.next()){
						results.add(rs.getString("TB_NM"));
					}
					return results;
				}
				
			});
			String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
			Glm glm = GlmCache.getInstance().getGlm(gdbVersion);
			Collection<GlmTable> logTables = new ArrayList<GlmTable>();
			for(String key:tables){
				logTables.add(glm.getGlmTable(key));
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}
	public void write(Collection<GlmTable> logTables)throws Exception{
		
	}
}
