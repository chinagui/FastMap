package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.DbUtils;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.DateUtilsEx;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * 元数据库下载,生成 sqllite并存入下载文件夹
 * @author zhangli5174
 *
 */
public class IncrementSynchroMetaGdbInterface {
	
	
	private static void synchroMetaAndGdbRdname() throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			
			List<String> dbLinks = searchMeta2GdbDblinks(conn);
			
			//获取当前时间
			Timestamp curTime = DateUtilsEx.getCurTime();
			//获取一天前的时间
			Timestamp beginTime = DateUtilsEx.getDayOfDelayDays(curTime, -1);
			String thisDate = DateUtilsEx.getTimeStr(curTime, "yyyy-MM-dd HH:mm:ss");
			String beginDate = DateUtilsEx.getTimeStr(beginTime, "yyyy-MM-dd HH:mm:ss");
			
			
			Map<Integer,Integer> dmlRdNames = searchDmlRdNamesLast(conn,beginDate,thisDate);
			if(dbLinks != null && dbLinks.size() > 0){
				for(int i=0 ;i < dbLinks.size() ; i++){
					//增量新增元数据库新增的数据
					synchroGdbByDblink(conn,dbLinks.get(i));
					//增量修改元数据修改的数据
					synchroUpdateGdbByDblink(conn,dbLinks.get(i),dmlRdNames);
					
				}
			}			
			System.out.println("Metadata export end");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}


	private static List<String> searchMeta2GdbDblinks(Connection conn) throws Exception {
		System.out.println("Start to search gdb DbLink...");
		String selectSql = " select distinct t.DB_LINK dblink from user_db_links t where t.DB_LINK like 'RG_DBLINK_%' ";
		Statement pstmt = null;
		ResultSet resultSet = null;
		List<String> dbLinks = null;
		try {
			pstmt = conn.createStatement();
			resultSet = pstmt.executeQuery(selectSql);
			resultSet.setFetchSize(5000);
			dbLinks = new ArrayList<String>();
			while (resultSet.next()) {
				dbLinks.add(resultSet.getString("dblink"));
			}
			System.out.println("search gdb DbLink end");
			return dbLinks;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
		}
	}

	private static Map<Integer, Integer> searchDmlRdNamesLast(Connection conn, String beginDate, String thisDate) throws Exception {
		System.out.println("Start to search gdb META_DML_LOGS...");
		
		String selectSql = " select * from META_DML_LOGS t where t.dml_table = 'RD_NAME'  "
				+ " and ( t.dml_date "
					+ " between to_date('"+beginDate+"','yyyy-mm-dd hh24:mi:ss') "
					+ " and to_date('"+thisDate+"','yyyy-mm-dd hh24:mi:ss') "
					+ " )  ";
		
		Statement pstmt = null;
		ResultSet resultSet = null;
		Map<Integer, Integer> dbLinks = null;
		try {
			pstmt = conn.createStatement();
			resultSet = pstmt.executeQuery(selectSql);
			resultSet.setFetchSize(5000);
			dbLinks = new HashMap<Integer, Integer>();
			while (resultSet.next()) {
				dbLinks.put(resultSet.getInt("dml_object_id"),resultSet.getInt("dml_type"));
			}
			System.out.println("search gdb META_DML_LOGS end");
			return dbLinks;
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
		}
	}
	
	private static void synchroGdbByDblink(Connection conn, String dbLink) throws Exception {
		System.out.println("Start to synchroGdbByDblink :"+dbLink);
		
		PreparedStatement stmt = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("MERGE INTO 	RD_NAME@"+dbLink+" R  \n");
			sql.append("USING RD_NAME  T  \n");
			sql.append("ON (R.NAME_ID = T.NAME_ID )  \n");
			sql.append("WHEN NOT MATCHED THEN \n");
			sql.append("  INSERT( \n");
			sql.append("     R.name_id,R.name_groupid,R.lang_code,R.name,R.type,R.base,R.prefix,R.infix,R.suffix,R.name_phonetic, \n");
			sql.append("     R.type_phonetic,R.base_phonetic,R.prefix_phonetic,R.infix_phonetic,R.suffix_phonetic,R.src_flag,R.road_type, \n");
			sql.append("     R.admin_id,R.code_type,R.voice_file,R.src_resume,R.pa_region_id,R.memo,R.route_id,R.u_record,R.u_fields,R.split_flag,R.city,R.process_flag \n");
			sql.append("  ) VALUES ( \n");
			sql.append("  	 T.name_id,T.name_groupid,T.lang_code,T.name,T.type,T.base,T.prefix,T.infix,T.suffix,T.name_phonetic, \n");
			sql.append("  	 T.type_phonetic,T.base_phonetic,T.prefix_phonetic,T.infix_phonetic,T.suffix_phonetic,T.src_flag,T.road_type, \n");
			sql.append("  	 T.admin_id,T.code_type,T.voice_file,T.src_resume,T.pa_region_id,T.memo,T.route_id,T.u_record,T.u_fields,T.split_flag,T.city,T.process_flag \n");
			sql.append("  ) \n");
			stmt = conn.prepareStatement(sql.toString());
			System.out.println("sql: "+sql.toString());
			int num = stmt.executeUpdate();
			System.out.println("dbLink: "+dbLink+" 新增的道路名数量 :"+num);
			conn.commit();
			System.out.println("synchroGdbByDblink end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(stmt);
		}
	}
	
	private static void synchroUpdateGdbByDblink(Connection conn, String dbLink, Map<Integer, Integer> dmlRdNames) throws Exception {
		System.out.println("Start to synchroGdbByDblink :"+dbLink);
		PreparedStatement stmt = null;
		try{
			if(dmlRdNames==null||dmlRdNames.size()==0){
				return;
			}
//			String sql = "UPDATE POI_EDIT_STATUS SET STATUS=2,FRESH_VERIFIED=1,RAW_FIELDS=? WHERE PID = ? AND STATUS IN (0,3)";
			StringBuilder sql = new StringBuilder();
			sql.append(" UPDATE RD_NAME@"+dbLink+" R  set (");
				sql.append("     R.name_id,R.name_groupid,R.lang_code,R.name,R.type,R.base,R.prefix,R.infix,R.suffix,R.name_phonetic, \n");
				sql.append("     R.type_phonetic,R.base_phonetic,R.prefix_phonetic,R.infix_phonetic,R.suffix_phonetic,R.src_flag,R.road_type, \n");
				sql.append("     R.admin_id,R.code_type,R.voice_file,R.src_resume,R.pa_region_id,R.memo,R.route_id,R.u_record,R.u_fields,R.split_flag,R.city,R.process_flag \n");
			sql.append(" ) = ( ");
				sql.append(" SELECT  ");
				sql.append("   	 T.name_id,T.name_groupid,T.lang_code,T.name,T.type,T.base,T.prefix,T.infix,T.suffix,T.name_phonetic, \n");
				sql.append("  	 T.type_phonetic,T.base_phonetic,T.prefix_phonetic,T.infix_phonetic,T.suffix_phonetic,T.src_flag,T.road_type, \n");
				sql.append("  	 T.admin_id,T.code_type,T.voice_file,T.src_resume,T.pa_region_id,T.memo,T.route_id,T.u_record,T.u_fields,T.split_flag,T.city,T.process_flag \n");
				sql.append(" FROM RD_NAME T  WHERE T.NAME_ID =?  ");
			sql.append(" ) ");
			sql.append(" where R.NAME_ID =? ");
			System.out.println(sql.toString());
			stmt = conn.prepareStatement(sql.toString());
			for(Entry<Integer,Integer> entry:dmlRdNames.entrySet()){
				if(entry.getValue() == 2){
					stmt.setInt(1, entry.getKey());
					stmt.setInt(2, entry.getKey());
					stmt.addBatch();
				}
				
			}
			stmt.executeBatch();
			stmt.clearBatch();
		}catch(Exception e){
			throw e;
		}finally{
			DbUtils.closeQuietly(stmt);
		}
		
	}
	public static void main(String[] args) {
		try{
			//初始化context
			JobScriptsInterface.initContext();
			
			synchroMetaAndGdbRdname();
			
			System.out.println("Over.");
			System.exit(0);
		}catch(Exception e){
			System.out.println("Oops, something wrong...");
			e.printStackTrace();
		}
	}


	
	
}
