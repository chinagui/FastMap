package com.navinfo.dataservice.bizcommons.glm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;

/** 
* @ClassName: LogGridCalculator 
* @author Xiao Xiaowen 
* @date 2016年6月28日 下午2:22:56 
* @Description: TODO
*  
*/
public class LogGridCalculatorByLocalData  {
	protected static  Logger log = LoggerRepos
			.getLogger(LogGridCalculatorByLocalData.class);
	protected OracleSchema logSchema;
	String flushLogGridSql = "INSERT INTO LOG_DETAIL_GRID (LOG_ROW_ID,GRID_ID,GRID_TYPE) VALUES (?,?,?)";
	
	public LogGridCalculatorByLocalData(OracleSchema logSchema){
		this.logSchema=logSchema;
	}

	public void calc(GlmTable table,boolean fillOldGrids,boolean fillNewGrids) throws Exception {
		Connection conn = null;
		PreparedStatement stmt = null;
		try{
			String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
			GlmGridCalculator calculator = GlmGridCalculatorFactory.getInstance().create(gdbVersion);
			conn = logSchema.getPoolDataSource().getConnection();
			stmt = conn.prepareStatement(flushLogGridSql);
			//计算new grid:insert+update类型的履历
        	if(fillNewGrids){
    			Map<String,String[]> newGrids = calculator.calc(table.getName(), new Integer[]{1,3}, conn);
            	flushLogGrids(newGrids,0,stmt,conn);
        	}
        	//计算old grid：update+delete类型的履历
        	if(fillOldGrids){
            	Map<String,String[]> oldGrids = calculator.calc(table.getName(), new Integer[]{2,3}, conn);
            	flushLogGrids(oldGrids,1,stmt,conn);
        	}
			conn.commit();
		}catch(Exception e){
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(conn);
		}
	}
	private void flushLogGrids(Map<String,String[]> grids,int gridType,PreparedStatement stmt,Connection conn)throws SQLException{
		if(grids!=null){
			int batchCount=0;
			for(Entry<String,String[]> entry:grids.entrySet()){
				for(String grid:entry.getValue()){
					stmt.setString(1, entry.getKey());
					stmt.setString(2, grid);
					stmt.setInt(3, gridType);
					stmt.addBatch();
					batchCount++;
				    if (batchCount % 1000 == 0) {
						stmt.executeBatch();
						stmt.clearBatch();
					}
				}
			}
			//剩余不到1000的执行掉
			stmt.executeBatch();
			stmt.clearBatch();
		}
	}
	
}
