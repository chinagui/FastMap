package com.navinfo.dataservice.expcore.external;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oracle.sql.CLOB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.datahub.glm.Glm;
import com.navinfo.dataservice.datahub.glm.GlmCache;
import com.navinfo.dataservice.datahub.glm.GlmTable;
import com.navinfo.dataservice.datahub.model.OracleSchema;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.dataservice.commons.util.StringUtils;


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
	 * 计算并更新NI_VAL_EXCEPTION表的MD5值
	 * @param schema
	 * @throws SQLException
	 */
	public static void generateCkMd5(OracleSchema schema)throws SQLException{
		Connection conn=null;
		try{
			log.debug("开始计算并更新NI_VAL_EXCEPTION表的MD5值");
			conn = schema.getDriverManagerDataSource().getConnection();
			String sql = "UPDATE NI_VAL_EXCEPTION A SET A.RESERVED = LOWER(UTL_RAW.CAST_TO_RAW(DBMS_OBFUSCATION_TOOLKIT.MD5(INPUT_STRING =>RULEID||INFORMATION||TARGETS||NVL(ADDITION_INFO,'null'))))";
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
			String sql = "SELECT RESERVED,TARGETS FROM NI_VAL_EXCEPTION";
			String insertSql = "INSERT INTO CK_RESULT_OBJECT(CK_RESULT_ID,TABLE_NAME,PID)VALUES(?,?,?)";
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
			throw new SQLException("计算并更新NI_VAL_EXCEPTION表的MD5值时出现错误，原因："+e.getMessage(),e);
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
				GlmTable table = glm.getEditTables().get(tableName);
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
				tables = glm.getEditTables().keySet();
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
		String str = "[RD_CROSS,559665];[RD_LINK,334978]";
		System.out.println(str.substring(1, str.length()-1));
//		Matcher matcher = Pattern.compile("\\[(.*?)\\]",Pattern.DOTALL).matcher(str);
//		if(matcher.find()){
//			System.out.println("Count:"+matcher.groupCount());
//			System.out.println("0:"+matcher.group(0));
////			System.out.println("1:"+matcher.group(1));
//			for(int i=1;i<=matcher.groupCount();i++){
//				System.out.println("1:"+matcher.group(i));
//			}
//		}
	}

}
