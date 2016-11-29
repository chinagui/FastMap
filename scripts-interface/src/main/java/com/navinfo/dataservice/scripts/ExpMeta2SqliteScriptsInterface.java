package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.sqlite.SQLiteConfig;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.sql.DBUtils;

public class ExpMeta2SqliteScriptsInterface {

	private static void exportMetadata2Sqlite(Connection sqliteConn) throws Exception {
		System.out.println("Start to export metadata...");
		Connection conn = null;

		try {
			conn = DBConnector.getInstance().getMetaConnection();

			chargingChain(conn,sqliteConn);
			fmControl(conn,sqliteConn);
			chainCode(conn,sqliteConn);
			
			System.out.println("Metadata export end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeConnection(conn);
		}
	}

	private static Connection createSqlite(String dir) throws Exception {
		System.out.println("Start to create sqlite...");
		
		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		Connection sqliteConn = null;

		// enabling dynamic extension loading
		// absolutely required by SpatiaLite
		SQLiteConfig config = new SQLiteConfig();
		config.enableLoadExtension(true);

		// create a database connection
		sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + dir
				+ "/metadata.sqlite", config.toProperties());
		Statement stmt = sqliteConn.createStatement();
		stmt.setQueryTimeout(30); // set timeout to 30 sec.

		sqliteConn.setAutoCommit(false);
		
		List<String> createSqls = sqliteInit();
		
		for (String sql:createSqls) {
			stmt.execute(sql);
		}
		
		System.out.println("Sqlite create end");
		return sqliteConn;
	}
	
	// 创建sqlite
	public static List<String> sqliteInit() {
		List<String> sqliteList = new ArrayList<String>();
		sqliteList.add("CREATE TABLE SC_POINT_CHARGING_CHAIN (chain_name text,chain_code text,hm_flag text,memo text)");
		sqliteList.add("CREATE TABLE SC_FM_CONTROL (id integer,kind_code text,kind_change integer,parent integer,parent_level integer,important integer,name_keyword text,level text,eng_permit integer,agent integer,region integer,tenant integer,extend integer,extend_photo integer,photo integer,internal integer,chain integer,tel_cs integer,add_cs integer,disp_onlink integer)");
		sqliteList.add("CREATE TABLE SC_POINT_CHAIN_CODE (chain_name text,chain_code text,kg_flag text,hm_flag text,memo text,type integer,chain_name_cht text,chain_name_eng text,category integer,weight integer)");
		sqliteList.add("CREATE TABLE SC_POINT_FOODTYPE (poikind text,foodtype text,type text,kg_flag text,hm_flag text,memo text,foodtypename text,chain text)");
		sqliteList.add("CREATE TABLE SC_POINT_POICODE_NEW (class_name text,class_code text,sub_class_name text,sub_class_code text,kind_name text,kind_code text,mhm_des text,kg_des text,col_des text,descript text,flag text,level text,u_fields text,u_recode integer,icon_name text,type integer,kind_use integer)");
		sqliteList.add("CREATE TABLE SC_POINT_KIND_NEW (id integer,poikind text,poikind_chain text,poikind_rating integer,poikind_flagcode text,poikind_category integer,r_kind text,r_kind_chain text,r_kind_rating integer,r_kind_flagcode text,r_kind_category integer,type integer,equal integer,kg_flag text,hm_flag text,memo text)");
		return sqliteList;
	}
	
	// SC_POINT_CHARGING_CHAIN
	public static void chargingChain(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export SC_POINT_CHARGING_CHAIN...");
		String insertSql = "insert into SC_POINT_CHARGING_CHAIN(chain_name,chain_code,hm_flag,memo) values(?,?,?,?)";
		String selectSql = "select chain_name,chain_code,hm_flag,memo from SC_POINT_CHARGING_CHAIN";
		Statement pstmt = null;
		ResultSet resultSet = null;
		PreparedStatement prep = null;
		try {
			prep = sqliteConn.prepareStatement(insertSql);
			pstmt = conn.createStatement();
			resultSet = pstmt.executeQuery(selectSql);
			resultSet.setFetchSize(5000);
			int count = 0;
			
			while (resultSet.next()) {
				prep.setString(1, resultSet.getString("chain_name"));
				prep.setString(2, resultSet.getString("chain_code"));
				prep.setString(3, resultSet.getString("hm_flag"));
				prep.setString(4, resultSet.getString("memo"));
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("SC_POINT_CHARGING_CHAIN end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}
	
	// SC_FM_CONTROL
	public static void fmControl(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export SC_FM_CONTROL...");
		String insertSql = "insert into SC_FM_CONTROL(id,kind_code,kind_change,parent,parent_level,important,name_keyword,level,eng_permit,agent,region,tenant,extend,extend_photo,photo,internal,chain,tel_cs,add_cs,disp_onlink) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		String selectSql = "select id,kind_code,kind_change,parent,parent_level,important,name_keyword,level,eng_permit,agent,region,tenant,extend,extend_photo,photo,internal,chain,tel_cs,add_cs,disp_onlink from SC_FM_CONTROL";
		Statement pstmt = null;
		ResultSet resultSet = null;
		PreparedStatement prep = null;
		try {
			prep = sqliteConn.prepareStatement(insertSql);
			pstmt = conn.createStatement();
			resultSet = pstmt.executeQuery(selectSql);
			resultSet.setFetchSize(5000);
			int count = 0;

			while (resultSet.next()) {
				prep.setInt(1, resultSet.getInt("id"));
				prep.setString(2, resultSet.getString("kind_code"));
				prep.setInt(3, resultSet.getInt("kind_change"));
				prep.setInt(4, resultSet.getInt("parent"));
				prep.setInt(5, resultSet.getInt("parent_level"));
				prep.setInt(6, resultSet.getInt("important"));
				prep.setString(7, resultSet.getString("name_keyword"));
				prep.setString(8, resultSet.getString("level"));
				prep.setInt(9, resultSet.getInt("eng_permit"));
				prep.setInt(10, resultSet.getInt("agent"));
				prep.setInt(11, resultSet.getInt("region"));
				prep.setInt(12, resultSet.getInt("tenant"));
				prep.setInt(13, resultSet.getInt("extend"));
				prep.setInt(14, resultSet.getInt("extend_photo"));
				prep.setInt(15, resultSet.getInt("photo"));
				prep.setInt(16, resultSet.getInt("internal"));
				prep.setInt(17, resultSet.getInt("chain"));
				prep.setInt(17, resultSet.getInt("tel_cs"));
				prep.setInt(18, resultSet.getInt("add_cs"));
				prep.setInt(19, resultSet.getInt("disp_onlink"));
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("SC_FM_CONTROL end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}
	
	// SC_POINT_CHAIN_CODE
	public static void chainCode(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export SC_POINT_CHAIN_CODE...");
		String insertSql = "insert into SC_POINT_CHAIN_CODE(chain_name,chain_code,kg_flag,hm_flag,memo,type,chain_name_cht,chain_name_eng,category,weight) values(?,?,?,?,?,?,?,?,?,?)";
		String selectSql = "select chain_name,chain_code,kg_flag,hm_flag,memo,type,chain_name_cht,chain_name_eng,category,weight from SC_POINT_CHAIN_CODE";
		Statement pstmt = null;
		ResultSet resultSet = null;
		PreparedStatement prep = null;
		try {
			prep = sqliteConn.prepareStatement(insertSql);
			pstmt = conn.createStatement();
			resultSet = pstmt.executeQuery(selectSql);
			resultSet.setFetchSize(5000);
			int count = 0;
			
			while (resultSet.next()) {
				prep.setString(1, resultSet.getString("chain_name"));
				prep.setString(2, resultSet.getString("chain_code"));
				prep.setString(3, resultSet.getString("kg_flag"));
				prep.setString(4, resultSet.getString("hm_flag"));
				prep.setString(5, resultSet.getString("memo"));
				prep.setInt(6, resultSet.getInt("type"));
				prep.setString(7, resultSet.getString("chain_name_cht"));
				prep.setString(8, resultSet.getString("chain_name_eng"));
				prep.setInt(9, resultSet.getInt("category"));
				prep.setInt(10, resultSet.getInt("weight"));
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("SC_POINT_CHAIN_CODE end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}
	
	// SC_POINT_FOODTYPE
	public static void pointFoodtype(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export SC_POINT_FOODTYPE...");
		String insertSql = "insert into SC_POINT_FOODTYPE(poikind,foodtype,type,kg_flag,hm_flag,memo,foodtypename,chain) values(?,?,?,?,?,?,?,?)";
		String selectSql = "select poikind,foodtype,type,kg_flag,hm_flag,memo,foodtypename,chain from SC_POINT_FOODTYPE";
		Statement pstmt = null;
		ResultSet resultSet = null;
		PreparedStatement prep = null;
		try {
			prep = sqliteConn.prepareStatement(insertSql);
			pstmt = conn.createStatement();
			resultSet = pstmt.executeQuery(selectSql);
			resultSet.setFetchSize(5000);
			int count = 0;

			while (resultSet.next()) {
				prep.setString(1, resultSet.getString("poikind"));
				prep.setString(2, resultSet.getString("foodtype"));
				prep.setString(3, resultSet.getString("type"));
				prep.setString(4, resultSet.getString("kg_flag"));
				prep.setString(5, resultSet.getString("hm_flag"));
				prep.setString(6, resultSet.getString("memo"));
				prep.setString(7, resultSet.getString("foodtypename"));
				prep.setString(8, resultSet.getString("chain"));
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("SC_POINT_FOODTYPE end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}

	public static void export2SqliteByNames(String dir) {

		File mkdirFile = new File(dir);

		mkdirFile.mkdirs();
		
		try {
			Connection sqliteConn = createSqlite(dir);

			exportMetadata2Sqlite(sqliteConn);

			sqliteConn.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void main(String[] args) {
		String dir = "";
		export2SqliteByNames(dir);
	}
	
}
