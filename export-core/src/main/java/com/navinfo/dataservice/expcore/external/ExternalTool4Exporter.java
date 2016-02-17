package com.navinfo.dataservice.expcore.external;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.datahub.glm.Glm;
import com.navinfo.dataservice.datahub.glm.GlmCache;
import com.navinfo.dataservice.datahub.glm.GlmTable;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.QueryRunner;


/** 
 * @ClassName: ExporterExternalTools 
 * @author Xiao Xiaowen 
 * @date 2016-1-20 上午10:10:03 
 * @Description: TODO
 */
public class ExternalTool4Exporter {
	protected static Logger log = Logger.getLogger(ExternalTool4Exporter.class);

	//进一步可以做多线程
	
	public static void turnOnPkConstrain(OracleSchema schema,Set<String> tables)throws Exception{
		Connection conn=null;
		try{
			conn = schema.getDriverManagerDataSource().getConnection();
			DataBaseUtils.turnOnPkConstraint(conn, tables);
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("打开主键时出现错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public static void turnOffPkConstrain(OracleSchema schema,Set<String> tables)throws Exception{
		Connection conn=null;
		try{
			conn = schema.getDriverManagerDataSource().getConnection();
			DataBaseUtils.turnOffPkConstraint(conn, tables);
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("关闭主键时出现错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 计算并更新CK_EXCEPTION表的MD5值
	 * @param schema
	 * @throws SQLException
	 */
	public static void generateCkMd5(OracleSchema schema)throws SQLException{
		Connection conn=null;
		try{
			log.debug("开始计算并更新CK_EXCEPTION表的MD5值");
			conn = schema.getDriverManagerDataSource().getConnection();
			String sql = "UPDATE CK_EXCEPTION A SET A.RESERVED = LOWER(UTL_RAW.CAST_TO_RAW(DBMS_OBFUSCATION_TOOLKIT.MD5(INPUT_STRING =>RULE_ID||INFORMATION||TARGETS||NVL(ADDITION_INFO,'null'))))";
			QueryRunner runner = new QueryRunner();
			int count = runner.update(conn, sql);
			log.debug("计算并更新CK_EXCEPTION表的MD5值完毕，共更新了"+count+"条记录");
		}catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new SQLException("计算并更新CK_EXCEPTION表的MD5值时出现错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public static void removeDupRecord(String gdbVersion,OracleSchema schema,Set<String> tables)throws Exception{
		Connection conn=null;
		try{
			conn = schema.getDriverManagerDataSource().getConnection();
			if(tables==null||tables.size()==0){
				return ;
			}
			Glm glm = GlmCache.getInstance().getGlm(gdbVersion);
			Set<GlmTable> removeDupTables = new HashSet<GlmTable>();
			for(String tableName:tables){
				StringBuilder sb = new StringBuilder();
				GlmTable table = glm.getTables().get(tableName);
				if(table.isPksHasBigColumn()){
					
				}else{
					sb.append("DELETE FROM ");
					sb.append(table);
					sb.append(" P WHERE P.ROWID!=(SELECT MAX(T.ROWID) FROM ");
					sb.append(table);
					sb.append(" T WHERE ");
				}
			}
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("打开主键时出现错误，原因："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public static void physicalDeleteRow(String gdbVersion,OracleSchema schema,Set<String> tables)throws Exception{
		try{
			if(tables==null||tables.size()==0){
				Glm glm = GlmCache.getInstance().getGlm(gdbVersion);
				tables = glm.getTables().keySet();
			}
			PhysicalDeleteRow.doDelete(tables, schema);
			
		}catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new Exception("打开主键时出现错误，原因："+e.getMessage(),e);
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Set<String> set = new HashSet<String>();
	}

}
