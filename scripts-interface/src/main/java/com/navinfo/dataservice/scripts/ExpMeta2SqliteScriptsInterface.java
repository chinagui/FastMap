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
			
			scPointChargingChain(conn,sqliteConn);
			ciParaControl(conn,sqliteConn);
			ciParaFood(conn,sqliteConn);
			ciParaIcon(gdbConn,sqliteConn);
			ciParaKind(conn,sqliteConn);
			ciParaKindChain(conn,sqliteConn);
			ciParaKindMedium(conn,sqliteConn);
			ciParaKindTop(conn,sqliteConn);
			ciParaSensitiveWords(conn,sqliteConn);
			ciParaTel(conn,sqliteConn);
			scPointFocus(conn,sqliteConn);
			pointTruck(conn,sqliteConn);
			scPointNameck(conn,sqliteConn);
			//scPointChargeManu(conn,sqliteConn);
			ciParaKindSame(conn, sqliteConn);
			
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
		//1.充电站品牌表：
		sqliteList.add("CREATE TABLE SC_POINT_CHARGING_CHAIN (chain_name text,chain_code text,hm_flag text,memo text)");
		//2.小分类业务逻辑控制表
		sqliteList.add("CREATE TABLE CI_PARA_CONTROL (id integer PRIMARY KEY,kind_id integer,kind_code text,kind_change integer,parent integer,parent_level integer,important integer,name_keyword text,level text,eng_permit integer,agent integer,region integer,tenant integer,extend integer,extend_photo integer,photo integer,internal integer,chain integer,tel_cs integer,add_cs integer,disp_onlink integer)");
		//3.FOODTYPE值域表
		sqliteList.add("CREATE TABLE CI_PARA_FOOD (id integer PRIMARY KEY,kind_id integer,food_name text,food_code integer,foodtype integer)");
		//4.POI Icon表
		sqliteList.add("CREATE TABLE CI_PARA_ICON (id integer ,idcode text,name_in_nav text,type integer)");
		//5.POI分类表：
		sqliteList.add("CREATE TABLE CI_PARA_KIND (id integer PRIMARY KEY,medium_id integer,name text,code integer,kind_code text,description text,region integer,type integer)");
		//6.品牌分类表：
		sqliteList.add("CREATE TABLE CI_PARA_KIND_CHAIN (id integer PRIMARY KEY,kind_code text,chain text,chain_name text,foodtype text,level text,chain_type integer)");
		//7.POI中分类代码表：
		sqliteList.add("CREATE TABLE CI_PARA_KIND_MEDIUM (id integer PRIMARY KEY,top_id integer,code integer,name text)");
		//8.POI大分类代码表：
		sqliteList.add("CREATE TABLE CI_PARA_KIND_TOP (id integer PRIMARY KEY,code integer,name text)");
		//9.敏感关键字配置表：
		sqliteList.add("CREATE TABLE CI_PARA_SENSITIVE_WORDS (id integer PRIMARY KEY,sensitive_word text,type integer)");
		//10.电话区号表：
		sqliteList.add("CREATE TABLE CI_PARA_TEL (id integer PRIMARY KEY,city_code text,code text,tel_len integer)");
		//11.客户重点关注POI表：
		sqliteList.add("CREATE TABLE SC_POINT_FOCUS (id integer PRIMARY KEY,poi_num text,poiname text,memo text,descript text,type integer,poikind text,r_kind text,equal integer,kg_flag text,hm_flag text)");
		//12.卡车地图标识表：
		sqliteList.add("CREATE TABLE SC_POINT_TRUCK (id integer PRIMARY KEY,kind_name text,kind text,chain_name text,chain text,depth_information text,memo text,type integer,truck integer)");
		//13.POI名称相关检查配置表：
		sqliteList.add("CREATE TABLE SC_POINT_NAMECK (id integer PRIMARY KEY,pre_key text,result_key text,ref_key text,type text,kg_flag text, hm_flag text,memo text,kind text,adminarea text )");
		//14.充电站充电桩生产商配置表：
		//sqliteList.add("CREATE TABLE SC_POINT_CHARGE_MANU (serial_id text,full_name text,simply_name text,product_model text,product_type text,voltate text,current text,power text,memo text)");
		//15.同一关系分类表：
		sqliteList.add("CREATE TABLE CI_PARA_KIND_SAME (id integer PRIMARY KEY,kind_code text,kind_code_samepoi text )");
		
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
	public static void scPointChargingChain(Connection conn,Connection sqliteConn) throws Exception{
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
	
	/**2
	 * @Title: fmControl
	 * @Description: CI_PARA_CONTROL
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月21日 下午7:58:36 
	 */
	public static void ciParaControl(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export CI_PARA_CONTROL...");
		String insertSql = "insert into CI_PARA_CONTROL(id,kind_code,kind_change,parent,parent_level,important,"
				+ "name_keyword,level,eng_permit,agent,region,tenant,extend,extend_photo,"
				+ "photo,internal,chain,tel_cs,add_cs,disp_onlink,kind_id) values(null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
				//prep.setInt(1, null);
				prep.setInt(1, resultSet.getInt("kind_code"));//kind_id
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
			System.out.println("CI_PARA_CONTROL end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}
	
	/**
	 * 3
	 * @Title: pointFoodtype
	 * @Description: CI_PARA_FOOD
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月30日 下午1:56:13 
	 */
	public static void ciParaFood(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export CI_PARA_FOOD...");
		//id,kind_id,food_name,food_code,foodtype
		String insertSql = "insert into CI_PARA_FOOD(id,kind_id,food_name,food_code,foodtype) values(null,?,?,?,?)";
		String selectSql = "select poikind,foodtypename,foodtype,type from SC_POINT_FOODTYPE";
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
				prep.setInt(1, resultSet.getInt("poikind"));
				prep.setString(2, resultSet.getString("foodtypename"));
				prep.setInt(3, resultSet.getInt("foodtype"));
				int type = 0;
				String typestr = resultSet.getString("type");
				if(typestr.equals("A") || typestr.equals("C")){
					type = 1;
				}else if(typestr.equals("B")){
					type = 2;
				}
				prep.setInt(4, type);
				
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("CI_PARA_FOOD end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}
	/**4
	 * @Title: ciParaIcon
	 * @Description: TODO
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月23日 下午8:44:19 
	 */
	public static void ciParaIcon(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export CI_PARA_ICON...");
		//id ,idcode,name_in_nav,type 
		String insertSql = "insert into CI_PARA_ICON(id ,idcode,name_in_nav,type) values(?,?,?,?)";
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
			System.out.println("CI_PARA_ICON end");
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
	 * @Title: ciParaKind
	 * @Description: CI_PARA_KIND
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月30日 下午1:56:00 
	 */
	public static void ciParaKind(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export CI_PARA_KIND...");
		String insertSql = "insert into CI_PARA_KIND(id,medium_id,name,code,kind_code,description,region,type) values(?,?,?,?,?,?,?,?)";
		String selectSql = "select kind_code,class_code,sub_class_code,kind_name,descript,mhm_des,kind_use from SC_POINT_POICODE_NEW";
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
				prep.setInt(1, resultSet.getInt("kind_code"));
				prep.setInt(2, Integer.parseInt(resultSet.getString("class_code")+resultSet.getString("sub_class_code")));
				prep.setString(3, resultSet.getString("kind_name"));
				prep.setInt(4, Integer.parseInt(resultSet.getString("kind_code").substring(resultSet.getString("kind_code").length()-2)));
				prep.setString(5, resultSet.getString("kind_code"));
				prep.setString(6, resultSet.getString("descript"));
				int region = 0;
				String regionStr = resultSet.getString("mhm_des");
				if(regionStr.equals("D")){
					region = 1;
				}else if(regionStr.equals("HM") || regionStr.equals("H") || regionStr.equals("M")){
					region = 2;
				}
				prep.setInt(7, region);
				prep.setInt(8, resultSet.getInt("kind_use"));
				
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("CI_PARA_KIND end");
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
	 * @Title: ciParaKindChain
	 * @Description: CI_PARA_KIND_CHAIN
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月30日 下午1:55:44 
	 */
	public static void ciParaKindChain(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export CI_PARA_KIND_CHAIN...");
		//id ,kind_id ,kind_code ,chain ,chain_name ,foodtype ,level ,chain_type
		String insertSql = "insert into CI_PARA_KIND_CHAIN(id ,kind_code ,chain ,chain_name ,foodtype ,level ,chain_type ) values(?,?,?,?,?,?,?)";
		String selectSql = "select n.id,n.poikind,n.r_kind,"
				+ " (select chain_name from SC_POINT_CHAIN_CODE where chain_code = n.r_kind) chain_name,"
				+ " (select foodtype from sc_point_brand_foodtype where poikind = n.poikind and chain = n.r_kind) foodtype,"
				+ "  nvl((select new_poi_level  from sc_point_code2level  where kind_code = n.poikind and category=1 and (new_poi_level = 'A' or new_poi_level = 'B1')),'B1') new_poi_level,"
				+ " (select type from SC_POINT_CHAIN_CODE where chain_code = n.r_kind)  type "
				+ " from SC_POINT_KIND_NEW n "
				+ " where n.type = 8";
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
				prep.setString(3, resultSet.getString("r_kind"));
				prep.setString(4, resultSet.getString("chain_name"));
				prep.setString(5, resultSet.getString("foodtype"));
				prep.setString(6, resultSet.getString("new_poi_level"));
				prep.setInt(7, resultSet.getInt("type"));
				
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("CI_PARA_KIND_CHAIN end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}
		
	/**7
	 * @Title: ciParaKindMedium
	 * @Description: CI_PARA_KIND_MEDIUM
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月23日 下午8:37:02 
	 */
	public static void ciParaKindMedium(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export CI_PARA_KIND_MEDIUM...");
		String insertSql = "insert into CI_PARA_KIND_MEDIUM(id ,top_id ,code ,name) values(?,?,?,?)";
		String selectSql = "select distinct class_code ||''|| sub_class_code cc,class_code,sub_class_code,sub_class_name from SC_POINT_POICODE_NEW ";
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
				prep.setInt(1, resultSet.getInt("cc"));
				prep.setInt(2, resultSet.getInt("class_code"));
				prep.setInt(3, resultSet.getInt("sub_class_code"));
				prep.setString(4, resultSet.getString("sub_class_name"));
				
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("CI_PARA_KIND_MEDIUM end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}
		
	/**8
	 * @Title: ciParaKindTop
	 * @Description: CI_PARA_KIND_TOP
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月23日 下午8:38:37 
	 */
	public static void ciParaKindTop(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export CI_PARA_KIND_TOP...");
		//id ,kind_id ,kind_code ,chain ,chain_name ,foodtype ,level ,chain_type
		String insertSql = "insert into CI_PARA_KIND_TOP(id ,code ,name) values(?,?,?)";
		String selectSql = "select distinct class_code,class_name from SC_POINT_POICODE_NEW ";
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
				prep.setInt(1, resultSet.getInt("class_code"));
				prep.setInt(2, resultSet.getInt("class_code"));
				prep.setString(3, resultSet.getString("class_name"));
				
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("CI_PARA_KIND_TOP end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}
	/**9
	 * @Title: ciParaSensitiveWords
	 * @Description: CI_PARA_SENSITIVE_WORDS
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月23日 下午8:40:08 
	 */
	public static void ciParaSensitiveWords(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export CI_PARA_SENSITIVE_WORDS...");
		//id ,kind_id ,kind_code ,chain ,chain_name ,foodtype ,level ,chain_type
		String insertSql = "insert into CI_PARA_SENSITIVE_WORDS(id,sensitive_word ,type) values(?,?,?)";
		String selectSql = "select id,sensitive_word,type from SC_SENSITIVE_WORDS ";
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
				prep.setString(2, resultSet.getString("sensitive_word"));
				prep.setInt(3, resultSet.getInt("type"));
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("CI_PARA_SENSITIVE_WORDS end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}
	
	/**10
	 * @Title: ciParaTel
	 * @Description: CI_PARA_TEL
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月23日 下午8:40:59 
	 */
	public static void ciParaTel(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export CI_PARA_TEL...");
		String insertSql = "insert into CI_PARA_TEL(id,city_code,code,tel_len) values(null,?,?,?)";
		String selectSql = "select adminareacode,areacode,phonenum_len from SC_POINT_ADMINAREA ";
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
				prep.setString(2, resultSet.getString("areacode"));
				prep.setInt(3, resultSet.getInt("phonenum_len")+1);
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("CI_PARA_TEL end");
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
	 * @Title: scPointFocus
	 * @Description: SC_POINT_FOCUS
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月30日 下午2:28:52 
	 */
	public static void scPointFocus(Connection conn,Connection sqliteConn) throws Exception{
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
	 * @Title: pointNameck
	 * @Description: SC_POINT_NAMECK
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月30日 下午2:14:00 
	 */
	public static void scPointNameck(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export SC_POINT_NAMECK...");
		String insertSql = "insert into SC_POINT_NAMECK(id,pre_key,result_key,ref_key,type,kg_flag, "
				+"hm_flag,memo,kind,adminarea ) values(?,?,?,?,?,?,?,?,?,?)";
		String selectSql = "select id,pre_key,result_key,ref_key,type,kg_flag, "
				+"hm_flag,memo,kind,adminarea  "
				+"from SC_POINT_NAMECK where type = 4 or type = 6 ";
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
				//prep.setString(11, resultSet.getString("chain"));
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
	
	/**14
	 * @Title: scPointChargeManu
	 * @Description: SC_POINT_CHARGE_MANU
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月23日 下午8:13:22 
	 */
	public static void scPointChargeManu(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export SC_POINT_CHARGE_MANU...");
		String insertSql = "insert into SC_POINT_CHARGE_MANU(serial_id,full_name,simply_name,product_model,product_type,voltate, "
				+"current,power,memo ) values(?,?,?,?,?,?,?,?,?)";
		String selectSql = "select serial_id,full_name,simply_name,product_model,product_type,voltate, "
				+"current,power,memo  "
				+"from SC_DEEP_MANUFACTURER";
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
				prep.setString(1, resultSet.getString("serial_id"));
				prep.setString(2, resultSet.getString("full_name"));
				prep.setString(3, resultSet.getString("simply_name"));
				prep.setString(4, resultSet.getString("product_model"));
				prep.setString(5, resultSet.getString("product_type"));
				prep.setString(6, resultSet.getString("voltate"));
				prep.setString(7, resultSet.getString("current"));
				prep.setString(8, resultSet.getString("power"));
				prep.setString(9, resultSet.getString("memo"));
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("SC_POINT_CHARGE_MANU end");
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
			DBUtils.closeStatement(prep);
		}
	}
	/**15
	 * @Title: ciParaKindSame
	 * @Description: CI_PARA_KIND_SAME
	 * @param conn
	 * @param sqliteConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月23日 下午9:01:45 
	 */
	public static void ciParaKindSame(Connection conn,Connection sqliteConn) throws Exception{
		System.out.println("Start to export CI_PARA_KIND_SAME...");
		String insertSql = "insert into CI_PARA_KIND_SAME(id,kind_code,kind_code_samepoi) values(?,?,?)";
		String selectSql = "select id ,poikind,r_kind  "
				+"from SC_POINT_KIND_NEW  where type = 5 ";
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
				prep.setString(2, resultSet.getString("poikind"));
				prep.setString(3, resultSet.getString("r_kind"));
				
				prep.executeUpdate();
				
				count += 1;
				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}
			sqliteConn.commit();
			System.out.println("CI_PARA_KIND_SAME end");
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
		String dir = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.downloadFilePathRoot);  //服务器部署路径
		File metaSqliteFile = new File(dir+"/metadata.sqlite");
		if(metaSqliteFile.exists()){
			metaSqliteFile.delete();
		}
		export2SqliteByNames(dir+"/metadata");
	}
	
}
