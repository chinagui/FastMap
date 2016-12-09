package com.navinfo.dataservice.diff.scanner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculator;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculatorFactory;
import com.navinfo.dataservice.bizcommons.glm.GlmGridRefInfo;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.bizcommons.glm.LogGeoInfo;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.diff.exception.DiffException;
import com.navinfo.navicommons.database.QueryRunner;

/** 
* @ClassName: JavaLogGridCalculator 
* @author Xiao Xiaowen 
* @date 2016年4月13日 下午2:30:29 
* @Description: 履历和修改后的数据都在diffServer上，访问修改前的数据通过CrossUser方式
*/
public class LogGridCalculatorByCrossUser implements LogGridCalculator {
	protected static  Logger log = LoggerRepos
			.getLogger(LogGridCalculatorByCrossUser.class);
    protected OracleSchema diffServer;
    protected String rightSchemaUserName;
    protected QueryRunner runner;
    public LogGridCalculatorByCrossUser(OracleSchema diffServer,String rightSchemaUserName){
        this.diffServer = diffServer;
        this.rightSchemaUserName=rightSchemaUserName;
        runner = new QueryRunner();
    }

	@Override
	public void calc(GlmTable table,String gdbVerison) throws DiffException {
		Connection conn = null;
		PreparedStatement stmt = null;
		PreparedStatement stmt4Geo = null;
		try{
			GlmGridCalculator calculator = GlmGridCalculatorFactory.getInstance().create(gdbVerison);
			conn = diffServer.getPoolDataSource().getConnection();
			String flushLogGridSql = "INSERT INTO LOG_DETAIL_GRID (LOG_ROW_ID,GRID_ID,GRID_TYPE) VALUES (?,?,?)";
			stmt = conn.prepareStatement(flushLogGridSql);
			
			//计算new grid:insert+update类型的履历
        	Map<String,LogGeoInfo> newGrids = calculator.calc(table.getName(), new Integer[]{1,3}, conn);
        	flushLogGrids(newGrids,0,stmt,conn);
        	//计算old grid：update+delete类型的履历
        	Map<String,LogGeoInfo> oldGrids = calculator.calc(table.getName(), new Integer[]{2,3}, conn,"CROSS_USER",rightSchemaUserName);
        	flushLogGrids(oldGrids,1,stmt,conn);
        	//填充几何依赖
        	String geoSql = "UPDATE LOG_DETAIL SET GEO_NM=?,GEO_PID=?";
        	stmt4Geo=conn.prepareStatement(geoSql);
        	flushLogDetailGeo(newGrids,stmt4Geo);
			conn.commit();
		}catch(Exception e){
			log.error(e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new DiffException(e.getMessage(),e);
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.closeQuietly(stmt4Geo);
			DbUtils.closeQuietly(conn);
		}
	}
	private void flushLogGrids(Map<String,LogGeoInfo> grids,int gridType,PreparedStatement stmt,Connection conn)throws SQLException{
		if(grids!=null){
			int batchCount=0;
			for(Entry<String,LogGeoInfo> entry:grids.entrySet()){
				for(String grid:entry.getValue().getGrids()){
					stmt.setString(1, entry.getKey());
					stmt.setString(2, grid);
					stmt.setInt(3, gridType);
					stmt.addBatch();
					batchCount++;
				}
			    if (batchCount % 1000 == 0) {
					stmt.executeBatch();
					stmt.clearBatch();
				}
			}
			//剩余不到1000的执行掉
			stmt.executeBatch();
			stmt.clearBatch();
		}
	}

	private void flushLogDetailGeo(Map<String,LogGeoInfo> grids,PreparedStatement stmt)throws SQLException{
		if(grids!=null){
			int batchCount=0;
			for(Entry<String,LogGeoInfo> entry:grids.entrySet()){
				stmt.setString(1, entry.getValue().getGeoName());
				stmt.setLong(2, entry.getValue().getGeoPid());
				stmt.addBatch();
				batchCount++;
			    if (batchCount % 1000 == 0) {
					stmt.executeBatch();
					stmt.clearBatch();
				}
			}
			//剩余不到1000的执行掉
			stmt.executeBatch();
			stmt.clearBatch();
		}
	}
}
