package com.navinfo.dataservice.bizcommons.datarow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculator;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculatorFactory;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.RandomUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DbLinkCreator;

import oracle.sql.CLOB;

/** 
* @ClassName: CkResultTool 
* @author Xiao Xiaowen 
* @date 2016年6月22日 下午1:57:01 
* @Description: TODO
*  
*/
public class CkResultTool {
	protected static Logger log = LoggerRepos.getLogger(CkResultTool.class);

	/**
	 * 计算并更新NI_VAL_EXCEPTION表的MD5值
	 * @param schema
	 * @throws SQLException
	 */
	public static void generateCkMd5(OracleSchema schema)throws SQLException{
		Connection conn=null;
		try{
			log.debug("开始计算并更新NI_VAL_EXCEPTION表的MD5值");
			conn = schema.getPoolDataSource().getConnection();
			String sql = "UPDATE NI_VAL_EXCEPTION A SET A.MD5_CODE = LOWER(UTL_RAW.CAST_TO_RAW(DBMS_OBFUSCATION_TOOLKIT.MD5(INPUT_STRING =>RULEID||INFORMATION||TARGETS||NVL(ADDITION_INFO,'null'))))";
			QueryRunner runner = new QueryRunner();
			int count = runner.update(conn, sql);
			log.debug("计算并更新NI_VAL_EXCEPTION表的MD5值完毕，共更新了"+count+"条记录");
		}catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new SQLException("计算并更新NI_VAL_EXCEPTION表的MD5值时出现错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 计算并更新NI_VAL_EXCEPTION表的子表CK_RESULT_OBJECT
	 * @param schema
	 * @throws SQLException
	 */
	public static void generateCkResultObject(OracleSchema schema)throws SQLException{
		Connection conn=null;
		PreparedStatement stmt = null;
		try{
			log.debug("开始计算并更新CK_RESULT_OBJECT表");
			conn = schema.getDriverManagerDataSource().getConnection();
			String sql = "SELECT MD5_CODE,TARGETS FROM NI_VAL_EXCEPTION";
			String insertSql = "INSERT INTO CK_RESULT_OBJECT(CK_MD5_CODE,TABLE_NAME,PID)VALUES(?,?,?)";
			QueryRunner runner = new QueryRunner();
			Map<String,String> rows = runner.query(conn, sql, new ResultSetHandler<Map<String,String>>(){

				@Override
				public Map<String,String> handle(ResultSet rs) throws SQLException {
					Map<String,String> rows= new HashMap<String,String>();
					while(rs.next()){
						rows.put(rs.getString("MD5_CODE"),DataBaseUtils.clob2String((CLOB)rs.getClob("TARGETS")));
					}
					return rows;
				}
				
			});
			int count=0;
			stmt = conn.prepareStatement(insertSql);
			for(Map.Entry<String, String> entry:rows.entrySet()){
				String value = StringUtils.removeBlankChar(entry.getValue());
				if(value!=null&&value.length()>2){
					String subValue = value.substring(1, value.length()-1);
					log.debug(subValue);
					for(String table:subValue.split("\\];\\[")){
						stmt.setString(1, entry.getKey());
						String[] arr = table.split(",");
						stmt.setString(2, arr[0]);
						stmt.setString(3, arr[1]);
					    stmt.addBatch();
					    count++;
					    if (count % 1000 == 0) {
							stmt.executeBatch();
							stmt.clearBatch();
						}
					}
				}
			}
			//剩余不到1000的执行掉
			stmt.executeBatch();
			stmt.clearBatch();
			log.debug("计算并更新CK_RESULT_OBJECT表完毕，共写入了"+count+"条记录");
		}catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new SQLException("计算并更新CK_RESULT_OBJECT表时出现错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 计算并更新NI_VAL_EXCEPTION表的子表NI_VAL_EXCEPTION_GRID
	 * @param schema
	 * @throws SQLException
	 */
	public static void generateCkResultGrid(OracleSchema schema)throws SQLException{
		Connection conn=null;
		PreparedStatement stmt = null;
		try{
			log.debug("开始计算并更新NI_VAL_EXCEPTION_GRID表");
			conn = schema.getDriverManagerDataSource().getConnection();
			String sql = "SELECT RESERVED,TARGETS FROM NI_VAL_EXCEPTION";
			String insertSql = "INSERT INTO NI_VAL_EXCEPTION_GRID (CK_RESULT_ID,GRID_ID) VALUES (?,?)";
			QueryRunner runner = new QueryRunner();
			Map<String,String> rows = runner.query(conn, sql, new ResultSetHandler<Map<String,String>>(){

				@Override
				public Map<String,String> handle(ResultSet rs) throws SQLException {
					Map<String,String> rows= new HashMap<String,String>();
					while(rs.next()){
						rows.put(rs.getString("RESERVED"),DataBaseUtils.clob2String((CLOB)rs.getClob("TARGETS")));
					}
					return rows;
				}
				
			});
			String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);
			Glm glm = GlmCache.getInstance().getGlm(gdbVersion);
			GlmGridCalculator calculator = GlmGridCalculatorFactory.getInstance().create(gdbVersion);
			int count=0;
			stmt = conn.prepareStatement(insertSql);
			for(Map.Entry<String, String> entry:rows.entrySet()){
				String value = StringUtils.removeBlankChar(entry.getValue());
				if(value!=null&&value.length()>2){
					String subValue = value.substring(1, value.length()-1);
					log.debug(subValue);
					for(String table:subValue.split("\\];\\[")){
						String[] arr = table.split(",");
						String pidColName = glm.getTablePidColName(arr[0]);
						String[] grids = calculator.calc(arr[0], pidColName,Long.valueOf(arr[1]), conn);
						for(String grid:grids){
							stmt.setString(1, entry.getKey());
							stmt.setLong(2, Long.valueOf(grid));
							stmt.addBatch();
						    count++;
						    if (count % 1000 == 0) {
								stmt.executeBatch();
								stmt.clearBatch();
							}
						}
						break;//只取第一个要素计算grid
					}
				}
			}
			//剩余不到1000的执行掉
			stmt.executeBatch();
			stmt.clearBatch();
			log.debug("开始计算并更新NI_VAL_EXCEPTION_GRID表完毕，共写入了"+count+"条记录");
		}catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new SQLException("开始计算并更新NI_VAL_EXCEPTION_GRID表时出现错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public static void moveNiVal(OracleSchema srcSchema,OracleSchema targetSchema,String[] grids)throws Exception{
		Connection conn=null;
		try{
			conn = targetSchema.getDriverManagerDataSource().getConnection();
			//create db link
			DbLinkCreator cr = new DbLinkCreator();
			String dbLinkName = targetSchema.getConnConfig().getUserName()+"_"+RandomUtil.nextNumberStr(4);
			cr.create(dbLinkName, false, targetSchema.getDriverManagerDataSource(), srcSchema.getConnConfig().getUserName(), srcSchema.getConnConfig().getUserPasswd()
					, srcSchema.getConnConfig().getServerIp(), String.valueOf(srcSchema.getConnConfig().getServerPort()), srcSchema.getConnConfig().getDbName());
			
			QueryRunner runner = new QueryRunner();
			String sql = "INSERT INTO NI_VAL_EXCEPTION SELECT "+DataRowTool.getSelectColumnString(conn,"NI_VAL_EXCEPTION")+" FROM NI_VAL_EXCEPTION@"+dbLinkName+" T WHERE EXISTS (SELECT 1 FROM NI_VAL_EXCEPTION_GRID@"+dbLinkName+" G WHERE T.RESERVED=G.CK_RESULT_ID AND G.GRID_ID IN ("+org.apache.commons.lang.StringUtils.join(grids,",")+"))";
			runner.execute(conn, sql);
			sql = "INSERT INTO CK_RESULT_OBJECT SELECT "+DataRowTool.getSelectColumnString(conn,"CK_RESULT_OBJECT")+" FROM CK_RESULT_OBJECT@"+dbLinkName+" T WHERE EXISTS (SELECT 1 FROM NI_VAL_EXCEPTION_GRID@"+dbLinkName+" G WHERE T.CK_RESULT_ID=G.CK_RESULT_ID AND G.GRID_ID IN ("+org.apache.commons.lang.StringUtils.join(grids,",")+"))";
			runner.execute(conn, sql);
			sql = "INSERT INTO NI_VAL_EXCEPTION_GRID SELECT "+DataRowTool.getSelectColumnString(conn,"NI_VAL_EXCEPTION_GRID")+" FROM NI_VAL_EXCEPTION_GRID@"+dbLinkName+" T WHERE T.GRID_ID IN ("+org.apache.commons.lang.StringUtils.join(grids,",")+")";
			runner.execute(conn, sql);
			//删除dblink
			cr.drop(dbLinkName, false, targetSchema.getDriverManagerDataSource());
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("搬检查结果错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
