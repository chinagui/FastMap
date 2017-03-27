package com.navinfo.dataservice.bizcommons.datarow;

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

import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
import com.navinfo.navicommons.database.sql.ProcedureBase;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculator;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculatorFactory;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.util.RandomUtil;
import com.navinfo.dataservice.commons.util.StringUtils;


/** 
 * @ClassName: ExporterExternalTools 
 * @author Xiao Xiaowen 
 * @date 2016-1-20 上午10:10:03 
 * @Description: TODO
 */
public class DataRowTool {
	protected static Logger log = Logger.getLogger(DataRowTool.class);

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
	public static void turnOffFkConstrain(OracleSchema schema)throws Exception{
		try{
			ProcedureBase pb = new ProcedureBase(schema.getDriverManagerDataSource());
			String sql = "BEGIN\n"
					+"  FOR T IN (SELECT TABLE_NAME,CONSTRAINT_NAME FROM USER_CONSTRAINTS WHERE CONSTRAINT_TYPE='R') LOOP\n"
					+"    EXECUTE IMMEDIATE 'ALTER TABLE '||T.TABLE_NAME||' DISABLE CONSTRAINT '||T.CONSTRAINT_NAME;\n"
					+"  END LOOP;\n"
					+"END;";
			pb.callProcedure(sql);
		}catch (Exception e) {
			log.error("打开外键键时出现错误，原因："+e.getMessage(), e);
			throw new Exception("打开外键键时出现错误，原因："+e.getMessage(),e);
		}
	}
	public static String getSelectColumnString(Connection conn, String tableName) throws SQLException {
		String sql = "select COLUMN_NAME from USER_TAB_COLUMNS where table_name = ? ORDER BY COLUMN_ID";
		QueryRunner runner = new QueryRunner();
		return runner.query(conn, sql, new ResultSetHandler<String>() {
			StringBuilder builder = new StringBuilder();

			@Override
			public String handle(ResultSet rs) throws SQLException {
				int i = 0;
				while (rs.next()) {
					if (i > 0) {
						builder.append(",");
					}
					builder.append("\"");
					builder.append(rs.getString(1));
					builder.append("\"");
					i++;

				}
				return builder.toString();
			}
		}, tableName);

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{

		}catch(Exception e){
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
//		String str = "[RD_CROSS,559665];[RD_LINK,334978]";
//		System.out.println(str.substring(1, str.length()-1));
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
