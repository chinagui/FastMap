package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.sqlite.SQLiteConfig;
import com.ibm.icu.text.SimpleDateFormat;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * 元数据库下载,生成 sqllite并存入下载文件夹
 * @author zhangli5174
 *
 */
public class ExpMeta2SqliteScriptsInterface {

	private static void exportMetadata2Sqlite(Connection sqliteConn) throws Exception {
		System.out.println("Start to export metadata...");
		Connection conn = null;
		Connection gdbConn = null;

		try {
			conn = DBConnector.getInstance().getMetaConnection();
			gdbConn = DBConnector.getInstance().getMkConnection();
			pointChargingChain(conn,sqliteConn);
			
			fmControl(conn,sqliteConn);
			
			pointFoodtype(conn,sqliteConn);
			pointPoicodeNew(conn,sqliteConn);
			pointKindNew(conn,sqliteConn);
			pointChainCode(conn,sqliteConn);
			
			pointBrandFoodtype(conn,sqliteConn);
			
			pointCode2Level(conn,sqliteConn);
			
			pointNameck(conn,sqliteConn);
			pointAdminArea(conn,sqliteConn);
			pointFocus(conn, sqliteConn);
			pointTruck(conn, sqliteConn);
			metaPoiIcon(gdbConn, sqliteConn);
			
			
			
			
			//fmControl(conn,sqliteConn);
			//chainCode(conn,sqliteConn);
			
			System.out.println("Metadata export end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeConnection(conn);
		}
	}

	/**
	 * @Title: createSqlite
	 * @Description: 生成sqlite 文件
	 * @param dir
	 * @return
	 * @throws Exception  Connection
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月1日 上午9:47:29 
	 */
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
	
	/**
	 * @Title: sqliteInit
	 * @Description: 初始化 sqlite 库中的表
	 * @return  List<String>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月1日 上午9:48:21 
	 */
	public static List<String> sqliteInit() {
		List<String> sqliteList = new ArrayList<String>();
		sqliteList.add("CREATE TABLE SC_POINT_CHARGING_CHAIN (chain_name text,chain_code text,hm_flag text,memo text)");
		sqliteList.add("CREATE TABLE SC_FM_CONTROL (id integer,kind_code text,kind_change integer,parent integer,parent_level integer,important integer,name_keyword text,level text,eng_permit integer,agent integer,region integer,tenant integer,extend integer,extend_photo integer,photo integer,internal integer,chain integer,tel_cs integer,add_cs integer,disp_onlink integer)");
		//sqliteList.add("CREATE TABLE SC_POINT_CHAIN_CODE (chain_name text,chain_code text,kg_flag text,hm_flag text,memo text,type integer,chain_name_cht text,chain_name_eng text,category integer,weight integer)");
		sqliteList.add("CREATE TABLE SC_POINT_FOODTYPE (poikind text,foodtype text,type text,kg_flag text,hm_flag text,memo text,foodtypename text,chain text)");
		
		sqliteList.add("CREATE TABLE SC_POINT_POICODE_NEW (class_name text,class_code text,sub_class_name text,sub_class_code text,kind_name text,kind_code text,mhm_des text,kg_des text,col_des text,descript text,flag text,level text,u_fields text,u_recode integer,icon_name text,type integer,kind_use integer)");
		sqliteList.add("CREATE TABLE SC_POINT_KIND_NEW (id integer,poikind text,poikind_chain text,poikind_rating integer,poikind_flagcode text,poikind_category integer,r_kind text,r_kind_chain text,r_kind_rating integer,r_kind_flagcode text,r_kind_category integer,type integer,equal integer,kg_flag text,hm_flag text,memo text)");
		//*********zl 2016.11.30*************
		sqliteList.add("CREATE TABLE SC_POINT_CHAIN_CODE (chain_name text,chain_code text,kg_flag text,hm_flag text,memo text,type integer,chain_name_cht text,chain_name_eng text,category integer,weight integer)");
		
		sqliteList.add("CREATE TABLE SC_POINT_BRAND_FOODTYPE (id text,chi_key text,poikind text,chain text,foodtype text,kg_flag text,hm_flag text,memo text)");
		
		sqliteList.add("CREATE TABLE SC_POINT_CODE2LEVEL (id integer,kind_name text,kind_code text,old_poi_level text,new_poi_level text,memo text,descript text,kg_flag text,hm_flag text,chain  text,rating integer,flagcode text,category integer)");
		
		
		sqliteList.add("CREATE TABLE SC_POINT_NAMECK (id text,pre_key text,result_key text,ref_key text,type text,kg_flag text, hm_flag text,memo text,kind text,adminarea text,chain text )");
		
		sqliteList.add("CREATE TABLE SC_POINT_ADMINAREA (adminareacode text,province text,province_short text,city text,city_short text,areacode text,district text,district_short text,type text,whole text,postcode text,phonenum_len text,whole_py text,remark text )");
		sqliteList.add("CREATE TABLE SC_POINT_FOCUS (id integer,poi_num text,poiname text,memo text,descript text,type integer,poikind text,r_kind text,equal integer,kg_flag text,hm_flag text)");
		
		sqliteList.add("CREATE TABLE SC_POINT_TRUCK (id integer,kind_name text,kind text,chain_name text,chain text,depth_information text,memo text,type integer,truck integer)");

		sqliteList.add("CREATE TABLE META_POIICON (fid integer,pid text,name text ,type integer)");
		//***********************************
		return sqliteList;
	}
	
	/**
	 * 1
	 * @Title: pointChargingChain 
	 * @Description: SC_POINT_CHARGING_CHAIN
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月30日 下午1:57:27 
	 */
	public static void pointChargingChain(Connection conn,Connection sqliteConn) throws Exception{
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
	
	//2
	// SC_FM_CONTROL
	public static void fmControl(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export SC_FM_CONTROL...");
		String insertSql = "insert into SC_FM_CONTROL(id,kind_code,kind_change,parent,parent_level,important,name_keyword,level,eng_permit,agent,region,tenant,extend,extend_photo,photo,internal,chain,tel_cs,add_cs,disp_onlink) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		//String selectSql = "select id,kind_code,kind_change,parent,parent_level,important,name_keyword,level,eng_permit,agent,region,tenant,extend,extend_photo,photo,internal,chain,tel_cs,add_cs,disp_onlink from SC_FM_CONTROL";
		String selectSql = "select * from SC_FM_CONTROL";
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
				prep.setInt(18, resultSet.getInt("tel_cs"));
				prep.setInt(19, resultSet.getInt("add_cs"));
				prep.setInt(20, resultSet.getInt("disp_onlink"));
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
	/*public static void chainCode(Connection conn,Connection sqliteConn) throws Exception{
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
	}*/
	
	/**
	 * 3
	 * @Title: pointFoodtype
	 * @Description: SC_POINT_FOODTYPE
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月30日 下午1:56:13 
	 */
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
	
	
	/**
	 * 4
	 * @Title: pointPoicodeNew
	 * @Description: SC_POINT_POICODE_NEW
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月30日 下午1:56:00 
	 */
	public static void pointPoicodeNew(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export SC_POINT_POICODE_NEW...");
		String insertSql = "insert into SC_POINT_POICODE_NEW(kind_code,kind_name,descript,mhm_des,kind_use,kg_des,class_code,class_name,"
				+"sub_class_code,sub_class_name,icon_name,u_recode,u_fields,col_des,flag,\"LEVEL\",type ) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		String selectSql = "select kind_code,kind_name,descript,mhm_des,kind_use,kg_des,class_code,class_name,"
				+"sub_class_code,sub_class_name,icon_name,u_recode,u_fields,col_des,flag,\"LEVEL\",type  "
				+"from SC_POINT_POICODE_NEW";
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
				prep.setString(1, resultSet.getString("kind_code"));
				prep.setString(2, resultSet.getString("kind_name"));
				prep.setString(3, resultSet.getString("descript"));
				prep.setString(4, resultSet.getString("mhm_des"));
				prep.setInt(5, resultSet.getInt("kind_use"));
				prep.setString(6, resultSet.getString("kg_des"));
				prep.setString(7, resultSet.getString("class_code"));
				prep.setString(8, resultSet.getString("class_name"));
				prep.setString(9, resultSet.getString("sub_class_code"));
				prep.setString(10, resultSet.getString("sub_class_name"));
				prep.setString(11, resultSet.getString("icon_name"));
				prep.setInt(12, resultSet.getInt("u_recode"));
				prep.setString(13, resultSet.getString("u_fields"));
				prep.setString(14, resultSet.getString("col_des"));
				prep.setString(15, resultSet.getString("flag"));
				prep.setString(16, resultSet.getString("LEVEL"));
				prep.setInt(17, resultSet.getInt("type"));
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("SC_POINT_POICODE_NEW end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}
	
		/**
		 * 5
		 * @Title: pointKindNew
		 * @Description: SC_POINT_KIND_NEW
		 * @param conn
		 * @param sqliteConn
		 * @throws Exception  void
		 * @throws 
		 * @author zl zhangli5174@navinfo.com
		 * @date 2016年11月30日 下午1:55:44 
		 */
		public static void pointKindNew(Connection conn,Connection sqliteConn) throws Exception{
			System.out.println("Start to export SC_POINT_KIND_NEW...");
			String insertSql = "insert into SC_POINT_KIND_NEW(id,poikind,poikind_chain,poikind_rating,poikind_flagcode,poikind_category,r_kind,r_kind_chain,"
					+"r_kind_rating,r_kind_flagcode,r_kind_category,type,equal,kg_flag,hm_flag,memo ) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			String selectSql = "select id,poikind,poikind_chain,poikind_rating,poikind_flagcode,poikind_category,r_kind,r_kind_chain,"
					+"r_kind_rating,r_kind_flagcode,r_kind_category,type,equal,kg_flag,hm_flag,memo  "
					+"from SC_POINT_KIND_NEW";
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
					prep.setString(2, resultSet.getString("poikind"));
					prep.setString(3, resultSet.getString("poikind_chain"));
					prep.setInt(4, resultSet.getInt("poikind_rating"));
					prep.setString(5, resultSet.getString("poikind_flagcode"));
					prep.setInt(6, resultSet.getInt("poikind_category"));
					prep.setString(7, resultSet.getString("r_kind"));
					prep.setString(8, resultSet.getString("r_kind_chain"));
					prep.setInt(9, resultSet.getInt("r_kind_rating"));
					prep.setString(10, resultSet.getString("r_kind_flagcode"));
					prep.setInt(11, resultSet.getInt("r_kind_category"));
					prep.setInt(12, resultSet.getInt("type"));
					prep.setInt(13, resultSet.getInt("equal"));
					prep.setString(14, resultSet.getString("kg_flag"));
					prep.setString(15, resultSet.getString("hm_flag"));
					prep.setString(16, resultSet.getString("memo"));
					prep.executeUpdate();
					
					count += 1;
					if (count % 5000 == 0) {
						sqliteConn.commit();
					}
				}
				sqliteConn.commit();
				System.out.println("SC_POINT_KIND_NEW end");
			} catch (Exception e) {
				throw e;
			} finally {
				DBUtils.closeResultSet(resultSet);
				DBUtils.closeStatement(pstmt);
				DBUtils.closeStatement(prep);
			}
		}
		
		
		/**
		 * 6
		 * @Title: pointChainCode
		 * @Description: SC_POINT_CHAIN_CODE
		 * @param conn
		 * @param sqliteConn
		 * @throws Exception  void
		 * @throws 
		 * @author zl zhangli5174@navinfo.com
		 * @date 2016年11月30日 下午1:55:30 
		 */
		public static void pointChainCode(Connection conn,Connection sqliteConn) throws Exception{
			System.out.println("Start to export SC_POINT_CHAIN_CODE...");
			String insertSql = "insert into SC_POINT_CHAIN_CODE(chain_name,chain_code,kg_flag,hm_flag,memo,type,"
					+"chain_name_cht,chain_name_eng,category,weight ) values(?,?,?,?,?,?,?,?,?,?)";
			String selectSql = "select chain_name,chain_code,kg_flag,hm_flag,memo,type,"
					+"chain_name_cht,chain_name_eng,category,weight  "
					+"from SC_POINT_CHAIN_CODE";
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

		/**
		 * 7
		 * @Title: pointBrandFoodtype
		 * @Description: SC_POINT_BRAND_FOODTYPE
		 * @param conn
		 * @param sqliteConn
		 * @throws Exception  void
		 * @throws 
		 * @author zl zhangli5174@navinfo.com
		 * @date 2016年11月30日 下午1:54:54 
		 */
		public static void pointBrandFoodtype(Connection conn,Connection sqliteConn) throws Exception{
			System.out.println("Start to export SC_POINT_BRAND_FOODTYPE...");
			String insertSql = "insert into SC_POINT_BRAND_FOODTYPE(id,chi_key,poikind,chain,foodtype,kg_flag,hm_flag,memo"
					+" ) values(?,?,?,?,?,?,?,?)";
			String selectSql = "select id,chi_key,poikind,chain,foodtype,kg_flag,hm_flag,memo "
					//+"chain_name_cht,chain_name_eng,category,weight  "
					+"from SC_POINT_BRAND_FOODTYPE";
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
					prep.setString(1, resultSet.getString("id"));
					prep.setString(2, resultSet.getString("chi_key"));
					prep.setString(3, resultSet.getString("poikind"));
					prep.setString(4, resultSet.getString("chain"));
					prep.setString(5, resultSet.getString("foodtype"));
					prep.setString(6, resultSet.getString("kg_flag"));
					prep.setString(7, resultSet.getString("hm_flag"));
					prep.setString(8, resultSet.getString("memo"));
					prep.executeUpdate();
					
					count += 1;
					if (count % 5000 == 0) {
						sqliteConn.commit();
					}
				}
				sqliteConn.commit();
				System.out.println("SC_POINT_BRAND_FOODTYPE end");
			} catch (Exception e) {
				throw e;
			} finally {
				DBUtils.closeResultSet(resultSet);
				DBUtils.closeStatement(pstmt);
				DBUtils.closeStatement(prep);
			}
		}
		
		
		/**
		 * 8
		 * @Title: pointCode2Level
		 * @Description: SC_POINT_CODE2LEVEL
		 * @param conn
		 * @param sqliteConn
		 * @throws Exception  void
		 * @throws 
		 * @author zl zhangli5174@navinfo.com
		 * @date 2016年11月30日 下午2:02:03 
		 */
		public static void pointCode2Level(Connection conn,Connection sqliteConn) throws Exception{
			System.out.println("Start to export SC_POINT_CODE2LEVEL...");
			String insertSql = "insert into SC_POINT_CODE2LEVEL(id,kind_name,kind_code,old_poi_level,new_poi_level,memo,"
					+"descript,kg_flag,hm_flag,chain,rating,flagcode,category ) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
			String selectSql = "select id,kind_name,kind_code,old_poi_level,new_poi_level,memo, "
					+"descript,kg_flag,hm_flag,chain,rating,flagcode,category  "
					+"from SC_POINT_CODE2LEVEL";
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
					prep.setString(2, resultSet.getString("kind_name"));
					prep.setString(3, resultSet.getString("kind_code"));
					prep.setString(4, resultSet.getString("old_poi_level"));
					prep.setString(5, resultSet.getString("new_poi_level"));
					prep.setString(6, resultSet.getString("memo"));
					prep.setString(7, resultSet.getString("descript"));
					prep.setString(8, resultSet.getString("kg_flag"));
					prep.setString(9, resultSet.getString("hm_flag"));
					prep.setString(10, resultSet.getString("chain"));
					prep.setInt(11, resultSet.getInt("rating"));
					prep.setString(12, resultSet.getString("flagcode"));
					prep.setInt(13, resultSet.getInt("category"));
					prep.executeUpdate();
					
					count += 1;
					if (count % 5000 == 0) {
						sqliteConn.commit();
					}
				}
				sqliteConn.commit();
				System.out.println("SC_POINT_CODE2LEVEL end");
			} catch (Exception e) {
				throw e;
			} finally {
				DBUtils.closeResultSet(resultSet);
				DBUtils.closeStatement(pstmt);
				DBUtils.closeStatement(prep);
			}
		}
		
		/**
		 * 9
		 * @Title: pointNameck
		 * @Description: SC_POINT_NAMECK
		 * @param conn
		 * @param sqliteConn
		 * @throws Exception  void
		 * @throws 
		 * @author zl zhangli5174@navinfo.com
		 * @date 2016年11月30日 下午2:14:00 
		 */
		public static void pointNameck(Connection conn,Connection sqliteConn) throws Exception{
			System.out.println("Start to export SC_POINT_NAMECK...");
			String insertSql = "insert into SC_POINT_NAMECK(id,pre_key,result_key,ref_key,type,kg_flag, "
					+"hm_flag,memo,kind,adminarea,chain ) values(?,?,?,?,?,?,?,?,?,?,?)";
			String selectSql = "select id,pre_key,result_key,ref_key,type,kg_flag, "
					+"hm_flag,memo,kind,adminarea,chain   "
					+"from SC_POINT_NAMECK";
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
					prep.setString(1, resultSet.getString("id"));
					prep.setString(2, resultSet.getString("pre_key"));
					prep.setString(3, resultSet.getString("result_key"));
					prep.setString(4, resultSet.getString("ref_key"));
					prep.setString(5, resultSet.getString("type"));
					prep.setString(6, resultSet.getString("kg_flag"));
					prep.setString(7, resultSet.getString("hm_flag"));
					prep.setString(8, resultSet.getString("memo"));
					prep.setString(9, resultSet.getString("kind"));
					prep.setString(10, resultSet.getString("adminarea"));
					prep.setString(11, resultSet.getString("chain"));
					prep.executeUpdate();
					
					count += 1;
					if (count % 5000 == 0) {
						sqliteConn.commit();
					}
				}
				sqliteConn.commit();
				System.out.println("SC_POINT_NAMECK end");
			} catch (Exception e) {
				throw e;
			} finally {
				DBUtils.closeResultSet(resultSet);
				DBUtils.closeStatement(pstmt);
				DBUtils.closeStatement(prep);
			}
		}
		
		
		/**
		 * 10
		 * @Title: pointAdminArea
		 * @Description: SC_POINT_ADMINAREA
		 * @param conn
		 * @param sqliteConn
		 * @throws Exception  void
		 * @throws 
		 * @author zl zhangli5174@navinfo.com
		 * @date 2016年11月30日 下午2:21:44 
		 */
		public static void pointAdminArea(Connection conn,Connection sqliteConn) throws Exception{
			System.out.println("Start to export SC_POINT_ADMINAREA...");
			String insertSql = "insert into SC_POINT_ADMINAREA(adminareacode,province,province_short,city,city_short,areacode,district, "
					+"district_short,type,whole,postcode,phonenum_len,whole_py,remark ) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			String selectSql = "select adminareacode,province,province_short,city,city_short,areacode,district, "
					+"district_short,type,whole,postcode,phonenum_len,whole_py,remark  "
					+"from SC_POINT_ADMINAREA";
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
					prep.setString(1, resultSet.getString("adminareacode"));
					prep.setString(2, resultSet.getString("province"));
					prep.setString(3, resultSet.getString("province_short"));
					prep.setString(4, resultSet.getString("city"));
					prep.setString(5, resultSet.getString("city_short"));
					prep.setString(6, resultSet.getString("areacode"));
					prep.setString(7, resultSet.getString("district"));
					prep.setString(8, resultSet.getString("district_short"));
					prep.setString(9, resultSet.getString("type"));
					prep.setString(10, resultSet.getString("whole"));
					prep.setString(11, resultSet.getString("postcode"));
					prep.setString(12, resultSet.getString("phonenum_len"));
					prep.setString(13, resultSet.getString("whole_py"));
					prep.setString(14, resultSet.getString("remark"));
					prep.executeUpdate();
					
					count += 1;
					if (count % 5000 == 0) {
						sqliteConn.commit();
					}
				}
				sqliteConn.commit();
				System.out.println("SC_POINT_ADMINAREA end");
			} catch (Exception e) {
				throw e;
			} finally {
				DBUtils.closeResultSet(resultSet);
				DBUtils.closeStatement(pstmt);
				DBUtils.closeStatement(prep);
			}
		}
		
		
		/**
		 * 11
		 * @Title: pointFocus
		 * @Description: SC_POINT_FOCUS
		 * @param conn
		 * @param sqliteConn
		 * @throws Exception  void
		 * @throws 
		 * @author zl zhangli5174@navinfo.com
		 * @date 2016年11月30日 下午2:28:52 
		 */
	public static void pointFocus(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export SC_POINT_FOCUS...");
		String insertSql = "insert into SC_POINT_FOCUS(id,poi_num,poiname,memo,descript,type, "
				+"poikind,r_kind,equal,kg_flag,hm_flag ) values(?,?,?,?,?,?,?,?,?,?,?)";
		String selectSql = "select id,poi_num,poiname,memo,descript,type, "
				+"poikind,r_kind,equal,kg_flag,hm_flag  "
				+"from SC_POINT_FOCUS";
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
				prep.setString(2, resultSet.getString("poi_num"));
				prep.setString(3, resultSet.getString("poiname"));
				prep.setString(4, resultSet.getString("memo"));
				prep.setString(5, resultSet.getString("descript"));
				prep.setInt(6, resultSet.getInt("type"));
				prep.setString(7, resultSet.getString("poikind"));
				prep.setString(8, resultSet.getString("r_kind"));
				prep.setInt(9, resultSet.getInt("equal"));
				prep.setString(10, resultSet.getString("kg_flag"));
				prep.setString(11, resultSet.getString("hm_flag"));
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("SC_POINT_FOCUS end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}
		
	
	/**
	 * 12
	 * @Title: pointTruck
	 * @Description: SC_POINT_TRUCK
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月30日 下午2:35:18 
	 */
	public static void pointTruck(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export SC_POINT_TRUCK...");
		String insertSql = "insert into SC_POINT_TRUCK(id,kind_name,kind,chain_name,chain, "
				+"depth_information,memo,type,truck ) values(?,?,?,?,?,?,?,?,?)";
		String selectSql = "select id,kind_name,kind,chain_name,chain, "
				+"depth_information,memo,type,truck "
				+"from SC_POINT_TRUCK";
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
				prep.setString(2, resultSet.getString("kind_name"));
				prep.setString(3, resultSet.getString("kind"));
				prep.setString(4, resultSet.getString("chain_name"));
				prep.setString(5, resultSet.getString("chain"));
				prep.setInt(6, resultSet.getInt("depth_information"));
				prep.setString(7, resultSet.getString("memo"));
				prep.setInt(8, resultSet.getInt("type"));
				prep.setInt(9, resultSet.getInt("truck"));
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("SC_POINT_TRUCK end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}
	
	/**
	 * 13
	 * @Title: metaPoiIcon
	 * @Description: META_POIICON
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月30日 下午2:52:07 
	 */
	public static void metaPoiIcon(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export META_POIICON...");
		String insertSql = "insert into META_POIICON(fid,pid,name,type) values(?,?,?,?)";
		String selectSql = "select distinct pid, poi_num, name, 1 type from ix_poi p, ix_poi_name n, cmg_building_poi b "
				+ "	 where p.pid = n.poi_pid and b.poi_pid = p.pid "
				+ " and not exists (select 1 from ix_poi_icon i1 where i1.poi_pid = p.pid) "
				+ " UNION all "
				+ "select distinct pid, poi_num, name, 2 type from ix_poi p, ix_poi_name n, ix_poi_icon i "
				+ " where p.pid = n.poi_pid  and i.poi_pid = p.pid  "
				+ " and not exists (select 1 from cmg_building_poi b1 where b1.poi_pid = p.pid) "
				+ "  union all "
				+" select distinct pid, poi_num, name, 3 type from ix_poi p, ix_poi_name n, ix_poi_icon i, cmg_building_poi b "
				+"  where p.pid = n.poi_pid and i.poi_pid = p.pid and b.poi_pid = p.pid " ;
				
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
				prep.setInt(1, resultSet.getInt("pid"));//fid
				prep.setString(2, resultSet.getString("poi_num"));//pid
				prep.setString(3, resultSet.getString("name"));
				prep.setInt(4, resultSet.getInt("type"));
				
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("META_POIICON end");
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
		
//		String filePath = SystemConfigFactory.getSystemConfig().getValue(
//				PropConstant.downloadFilePathRoot);
		//String filePath = "f:";
		try {
			Connection sqliteConn = createSqlite(dir);

			exportMetadata2Sqlite(sqliteConn);
			
			//4.打包生成zip文件，放在月目录下
			String zipFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".zip";
			ZipUtils.zipFile(dir+"/metadata.sqlite",dir+"/"+zipFileName);
			sqliteConn.close();
		} catch (Exception e) {
			System.out.println(e);
		}finally{
			File metaSqliteFile = new File(dir+"/metadata.sqlite");
			if(metaSqliteFile.exists()){
				metaSqliteFile.delete();
			}
		}
	}
	
	public static void main(String[] args) {
		//String dir = "f:";  //本地测试用
		String dir = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.downloadFilePathRoot);  //服务器部署路径
		export2SqliteByNames(dir+"/metadata");
	}
	
}
