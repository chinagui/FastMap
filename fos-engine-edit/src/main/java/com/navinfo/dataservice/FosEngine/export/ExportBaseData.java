package com.navinfo.dataservice.FosEngine.export;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.uima.pear.util.FileUtil;
import org.sqlite.SQLiteConfig;

import com.navinfo.dataservice.FosEngine.comm.util.StringUtils;
import com.navinfo.dataservice.commons.db.OracleAddress;
import com.navinfo.dataservice.commons.util.ZipUtils;

public class ExportBaseData {

	public static final WKT wkt = new WKT();

	/**
	 * 导出基础数据
	 * 
	 * @param oa
	 * @param dir
	 * @throws Exception
	 */
	private static void exportRdLine(OracleAddress oa, String dir)
			throws Exception {
		String sql = "select a.*,        display_text.name,        styleFactors1.types,        styleFactors2.lane_types,        speedlimits.from_speed_limit,        speedlimits.to_speed_limit,        forms.forms   from rd_link a,        (select max(name1)||'/'||max(name2)||'/'||max(name3)||'/'||max(name4) name,link_pid from ( select a.link_pid, case when name_class=1 and seq_num=1 then b.name else null end as name1,  case when name_class=1 and seq_num=2 then b.name else null end as name2,  case when name_class=2 and seq_num=1 then b.name else null end as name3,    case when name_class=2 and seq_num=2 then b.name else null end as name4            from rd_link_name a, rd_name b          where a.name_groupid = b.name_groupid            and b.lang_code = 'CHI'            and a.u_record != 2) group by link_pid) display_text,        (select link_pid,                listagg(type, ',') within group(order by type) types           from (select a.link_pid, type                   from rd_link_limit a                  where type in (0, 4, 5, 6)                    and a.u_record != 2)          group by link_pid) styleFactors1,        (select link_pid,                listagg(lane_type, ',') within group(order by lane_type) lane_types           from rd_lane a          where a.u_record != 2          group by link_pid) styleFactors2,        (select link_pid, from_speed_limit, to_speed_limit           from rd_link_speedlimit a          where speed_type = 0            and a.u_record != 2) speedlimits,        (select link_pid,                listagg(form_of_way, ',') within group(order by form_of_way) forms           from rd_link_form          where u_record != 2          group by link_pid) forms  where a.link_pid = display_text.link_pid(+)    and a.link_pid = styleFactors1.link_pid(+)    and a.link_pid = styleFactors2.link_pid(+)    and a.link_pid = speedlimits.link_pid(+)    and a.link_pid = forms.link_pid(+)    and a.u_record != 2";
		
		File mkdirFile = new File(dir + "/tmp");

		mkdirFile.mkdirs();

		PrintWriter out = new PrintWriter(dir + "/tmp/rdline.txt");

		String operateDate = StringUtils.getCurrentTime();

		Connection conn = oa.getConn();

		Statement stmt = conn.createStatement();

		ResultSet resultSet = stmt.executeQuery(sql);

		resultSet.setFetchSize(5000);
		
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		while (resultSet.next()) {

			JSONObject json = enclosingRdLine(resultSet, operateDate);
			
			int pid = json.getInt("pid");
			
			if (map.containsKey(pid)){
				continue;
			}
			
			map.put(pid, 0);

			out.println(json.toString());
		}

		out.flush();

		out.close();

		// 压缩文件
		ZipUtils.zipFile(dir + "/tmp/", dir + "/" + operateDate + ".zip");

		FileUtil.deleteDirectory(new File(dir + "/tmp"));
	}

	private static JSONObject enclosingRdLine(ResultSet rs, String operateDate)
			throws Exception {

		JSONObject json = new JSONObject();

		int pid = rs.getInt("link_pid");

		json.put("pid", pid);

		String meshid = rs.getString("mesh_id");

		json.put("meshid", meshid);
		
		JSONArray names = new JSONArray();

		String name = rs.getString("name");
		
		String display_text = "";
		
		if( name != null){
		
			String[] sss = name.split("/");
			
			boolean flag1=false;
			
			for(String s : sss){
				if(s!=null){
					if(flag1){
						display_text+="/";
					}
					
					display_text+=s;
					
					JSONObject namejson = new JSONObject();
					
					namejson.put("name", s);
					
					names.add(namejson);
					
					flag1=true;
				}
			}
		}
		
		json.put("names", names);
		
		json.put("display_text", display_text);

		int kind = rs.getInt("kind");

		json.put("kind", kind);

		int direct = rs.getInt("direct");

		json.put("direct", direct);

		int appInfo = rs.getInt("app_info");

		json.put("appInfo", appInfo);

		int tollInfo = rs.getInt("toll_info");

		json.put("tollInfo", tollInfo);

		int multiDigitized = rs.getInt("multi_digitized");

		json.put("multiDigitized", multiDigitized);

		int specialTraffic = rs.getInt("special_traffic");

		json.put("specialTraffic", specialTraffic);

		int fc = rs.getInt("function_class");

		json.put("fc", fc);

		int laneNum = rs.getInt("lane_num");

		json.put("laneNum", laneNum);

		int laneLeft = rs.getInt("lane_left");

		json.put("laneLeft", laneLeft);

		int laneRight = rs.getInt("lane_right");

		json.put("laneRight", laneRight);

		int isViaduct = rs.getInt("is_viaduct");

		json.put("isViaduct", isViaduct);

		int paveStatus = rs.getInt("pave_status");

		json.put("paveStatus", paveStatus);

		STRUCT struct = (STRUCT) rs.getObject("geometry");

		JGeometry geom = JGeometry.load(struct);

		String geometry = new String(wkt.fromJGeometry(geom));

		json.put("geometry", geometry);

		String forms = rs.getString("forms");

		forms = "[" + (forms == null ? "" : forms) + "]";

		JSONArray array = JSONArray.fromObject(forms);

		JSONArray formsArray = new JSONArray();

		for (int i = 0; i < array.size(); i++) {
			JSONObject form = new JSONObject();

			form.put("form", array.getInt(i));

			formsArray.add(form);
		}

		json.put("forms", formsArray);

		JSONArray styleFactors = new JSONArray();

		String types = rs.getString("types");

		if (types != null && types.length() > 0) {
			String[] splits = types.split(",");

			for (String s : splits) {
				JSONObject jo = new JSONObject();

				jo.put("factor", Integer.parseInt(s));

				styleFactors.add(jo);
			}
		}

		String laneTypes = rs.getString("lane_types");

		if (laneTypes != null && laneTypes.length() > 0) {

			String[] splits = laneTypes.split(",");

			for (String s : splits) {

				String bin = Integer.toBinaryString(Integer.valueOf(s));

				int len = bin.length();

				boolean flag = false;

				if (bin.length() < 12) {
					flag = true;
				} else {
					String p = bin.substring(len - 12, len - 11);

					if ("1".equals(p)) {
						flag = true;
					}
				}

				if (flag) {
					JSONObject jo = new JSONObject();
					jo.put("factor", 99);
					styleFactors.add(jo);
					break;
				}
			}
		}

		json.put("styleFactors", styleFactors);
		
		int style = computeStyle(paveStatus, isViaduct, formsArray, styleFactors);
		
		json.put("display_style", kind+","+style);

		int from_speed_limit = rs.getInt("from_speed_limit");

		int to_speed_limit = rs.getInt("to_speed_limit");

		JSONObject jo = new JSONObject();

		jo.put("from", from_speed_limit);

		jo.put("to", to_speed_limit);

		json.put("speedLimit", jo);

		json.put("op_date", operateDate);

		json.put("op_lifecycle", 0);

		return json;
	}
	
	private static int computeStyle(int paveStatus, int isViaduct, JSONArray forms, JSONArray styleFactors){
		
		if(paveStatus == 1){
			return 0;
		}
		
		Set<Integer> formSet = new HashSet<Integer>();
		
		for(int i=0;i<forms.size();i++){
			JSONObject json = forms.getJSONObject(i);
			
			formSet.add(json.getInt("form"));
		}
		
		if(isViaduct == 1){
			if(formSet.contains(24)){
				return 1;
			}
			else if (formSet.contains(30)){
				return 2;
			}
			return 3;
		}
		
		if(formSet.contains(15)){
			return 4;
		}
		
		if(formSet.contains(16)){
			return 5;
		}
		
		if(formSet.contains(17)){
			return 6;
		}
		
		if(formSet.contains(22)){
			return 7;
		}
		
		if(formSet.contains(24)){
			if(formSet.contains(30)){
				return 8;
			}
			else{
				return 9;
			}
		}
		
		if(formSet.contains(30)){
			return 10;
		}
		
		if(formSet.contains(31)){
			return 11;
		}
		
		if(formSet.contains(34)){
			return 12;
		}
		
		if(formSet.contains(36)){
			if(formSet.contains(52)){
				return 13;
			}
			else{
				return 14;
			}
		}
		
		if(formSet.contains(52)){
			return 15;
		}
		
		if(formSet.contains(53)){
			return 16;
		}
		
		if(formSet.contains(60)){
			return 17;
		}
		
		Set<Integer> styleSet = new HashSet<Integer>();
		
		for(int i=0;i<styleFactors.size();i++){
			JSONObject json = styleFactors.getJSONObject(i);
			
			styleSet.add(json.getInt("factor"));
		}
		
		if(styleSet.contains(0)){
			return 18;
		}
		
		if(styleSet.contains(6)){
			return 19;
		}
		
		if(styleSet.contains(99)){
			return 20;
		}
		
		return -1;
	}

	/**
	 * 导出道路底图到spatialite
	 * 
	 * @param oa
	 * @param dir
	 * @throws Exception
	 */
	public static void exportBaseData2Sqlite(OracleAddress oa, String dir)
			throws Exception {

		
		File mkdirFile = new File(dir + "/tmp");

		mkdirFile.mkdirs();

		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		Connection sqliteConn = null;

		// enabling dynamic extension loading
		// absolutely required by SpatiaLite
		SQLiteConfig config = new SQLiteConfig();
		config.enableLoadExtension(true);

		// create a database connection
		sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + dir
				+ "/tmp/gdbdata.sqlite", config.toProperties());
		Statement stmt = sqliteConn.createStatement();
		stmt.setQueryTimeout(30); // set timeout to 30 sec.

		// loading SpatiaLite
		stmt.execute("SELECT load_extension('/usr/local/lib/mod_spatialite.so')");

		// enabling Spatial Metadata
		// using v.2.4.0 this automatically initializes SPATIAL_REF_SYS and
		// GEOMETRY_COLUMNS
		stmt.execute("SELECT InitSpatialMetadata()");
		
		sqliteConn.setAutoCommit(false);

		String operateDate = StringUtils.getCurrentTime();
		
		exportRdLine2Sqlite(sqliteConn, stmt, oa, operateDate);
		
		exportRdNode2Sqlite(sqliteConn, stmt, oa, operateDate);
		
		sqliteConn.close();

		// 压缩文件
		ZipUtils.zipFile(dir + "/tmp/", dir + "/" + operateDate + ".zip");

		FileUtil.deleteDirectory(new File(dir + "/tmp"));

	}
	
	private static void exportRdLine2Sqlite(Connection sqliteConn, Statement stmt, OracleAddress oa, String operateDate) throws Exception{
		// creating a LINESTRING table
		stmt.execute("create table gdb_rdLine(pid integer primary key)");
		stmt.execute("select addgeometrycolumn('gdb_rdLine','geometry',4326,'GEOMETRY','XY')");
		stmt.execute("select createspatialindex('gdb_rdLine','geometry')");
		stmt.execute("alter table gdb_rdLine add display_style text;");
		stmt.execute("alter table gdb_rdLine add display_text text;");
		stmt.execute("alter table gdb_rdLine add meshid text;");
		stmt.execute("alter table gdb_rdLine add kind integer;");
		stmt.execute("alter table gdb_rdLine add direct integer;");
		stmt.execute("alter table gdb_rdLine add appInfo integer;");
		stmt.execute("alter table gdb_rdLine add tollInfo integer;");
		stmt.execute("alter table gdb_rdLine add multiDigitized integer;");
		stmt.execute("alter table gdb_rdLine add specialTraffic integer;");
		stmt.execute("alter table gdb_rdLine add fc integer;");
		stmt.execute("alter table gdb_rdLine add laneNum integer;");
		stmt.execute("alter table gdb_rdLine add laneLeft integer;");
		stmt.execute("alter table gdb_rdLine add laneRight integer;");
		stmt.execute("alter table gdb_rdLine add isViaduct integer;");
		stmt.execute("alter table gdb_rdLine add paveStatus integer;");
		stmt.execute("alter table gdb_rdLine add forms Blob;");
		stmt.execute("alter table gdb_rdLine add styleFactors Blob;");
		stmt.execute("alter table gdb_rdLine add speedLimit Blob;");
		stmt.execute("alter table gdb_rdLine add op_date text;");
		stmt.execute("alter table gdb_rdLine add op_lifecycle integer;");
		stmt.execute("alter table gdb_rdLine add names Blob;");

		String insertSql = "insert into gdb_rdLine values("
				+ "?, GeomFromText(?, 4326), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement prep = sqliteConn.prepareStatement(insertSql);

		Connection conn = oa.getConn();

		Statement stmt2 = conn.createStatement();
		
		String sql = "select a.*,        display_text.name,        styleFactors1.types,        styleFactors2.lane_types,        speedlimits.from_speed_limit,        speedlimits.to_speed_limit,        forms.forms   from rd_link a,        (select max(name1)||'/'||max(name2)||'/'||max(name3)||'/'||max(name4) name,link_pid from ( select a.link_pid, case when name_class=1 and seq_num=1 then b.name else null end as name1,  case when name_class=1 and seq_num=2 then b.name else null end as name2,  case when name_class=2 and seq_num=1 then b.name else null end as name3,    case when name_class=2 and seq_num=2 then b.name else null end as name4            from rd_link_name a, rd_name b          where a.name_groupid = b.name_groupid            and b.lang_code = 'CHI'            and a.u_record != 2) group by link_pid) display_text,        (select link_pid,                listagg(type, ',') within group(order by type) types           from (select a.link_pid, type                   from rd_link_limit a                  where type in (0, 4, 5, 6)                    and a.u_record != 2)          group by link_pid) styleFactors1,        (select link_pid,                listagg(lane_type, ',') within group(order by lane_type) lane_types           from rd_lane a          where a.u_record != 2          group by link_pid) styleFactors2,        (select link_pid, from_speed_limit, to_speed_limit           from rd_link_speedlimit a          where speed_type = 0            and a.u_record != 2) speedlimits,        (select link_pid,                listagg(form_of_way, ',') within group(order by form_of_way) forms           from rd_link_form          where u_record != 2          group by link_pid) forms  where a.link_pid = display_text.link_pid(+)    and a.link_pid = styleFactors1.link_pid(+)    and a.link_pid = styleFactors2.link_pid(+)    and a.link_pid = speedlimits.link_pid(+)    and a.link_pid = forms.link_pid(+)    and a.u_record != 2";
		
		ResultSet resultSet = stmt2.executeQuery(sql);

		resultSet.setFetchSize(5000);

		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		int count = 0;

		while (resultSet.next()) {

			JSONObject json = enclosingRdLine(resultSet, operateDate);

			int pid = json.getInt("pid");

			if (map.containsKey(pid)) {
				continue;
			}

			map.put(pid, 0);

			prep.setInt(1, pid);

			prep.setString(2, json.getString("geometry"));

			prep.setString(3, json.getString("display_style"));

			prep.setString(4, json.getString("display_text"));
			
			prep.setString(5, json.getString("meshid"));

			prep.setInt(6, json.getInt("kind"));

			prep.setInt(7, json.getInt("direct"));

			prep.setInt(8, json.getInt("appInfo"));

			prep.setInt(9, json.getInt("tollInfo"));

			prep.setInt(10, json.getInt("multiDigitized"));

			prep.setInt(11, json.getInt("specialTraffic"));

			prep.setInt(12, json.getInt("fc"));

			prep.setInt(13, json.getInt("laneNum"));

			prep.setInt(14, json.getInt("laneLeft"));

			prep.setInt(15, json.getInt("laneRight"));

			prep.setInt(16, json.getInt("isViaduct"));

			prep.setInt(17, json.getInt("paveStatus"));

			byte[] forms = json.getString("forms").getBytes();

			prep.setBinaryStream(18, new ByteArrayInputStream(forms),
					forms.length);

			byte[] styleFactors = json.getString("styleFactors").getBytes();

			prep.setBinaryStream(19, new ByteArrayInputStream(styleFactors),
					styleFactors.length);

			byte[] speedLimit = json.getString("speedLimit").getBytes();

			prep.setBinaryStream(20, new ByteArrayInputStream(speedLimit),
					speedLimit.length);

			prep.setString(21, json.getString("op_date"));

			prep.setInt(22, json.getInt("op_lifecycle"));
			
			byte[] names = json.getString("names").getBytes();

			prep.setBinaryStream(23, new ByteArrayInputStream(names),
					names.length);

			prep.executeUpdate();

			count += 1;

			if (count % 5000 == 0) {
				sqliteConn.commit();
			}
		}

		sqliteConn.commit();
	}
	
	private static void exportRdNode2Sqlite(Connection sqliteConn, Statement stmt, OracleAddress oa, String operateDate) throws Exception{
		// creating a LINESTRING table
		stmt.execute("create table gdb_rdNode(pid integer primary key)");
		stmt.execute("select addgeometrycolumn('gdb_rdNode','geometry',4326,'GEOMETRY','XY')");
		stmt.execute("select createspatialindex('gdb_rdNode','geometry')");
		stmt.execute("alter table gdb_rdNode add display_style text;");
		stmt.execute("alter table gdb_rdNode add display_text text;");
		stmt.execute("alter table gdb_rdNode add meshid text;");
		stmt.execute("alter table gdb_rdNode add isMain integer;");
		stmt.execute("alter table gdb_rdNode add op_date text;");
		stmt.execute("alter table gdb_rdNode add op_lifecycle integer;");

		String insertSql = "insert into gdb_rdNode values("
				+ "?, GeomFromText(?, 4326), ?, ?, ?, ?, ?, ?)";

		PreparedStatement prep = sqliteConn.prepareStatement(insertSql);

		Connection conn = oa.getConn();

		Statement stmt2 = conn.createStatement();
		
		String oraSql = "select b.node_pid,b.geometry,c.mesh_id,a.is_main from rd_cross_node a,rd_node b,rd_node_mesh c where c.node_pid=b.node_pid and a.node_pid=b.node_pid and a.u_record!=2 and b.u_record!=2";

		ResultSet resultSet = stmt2.executeQuery(oraSql);

		resultSet.setFetchSize(5000);

//		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		int count = 0;

		while (resultSet.next()) {

			JSONObject json = enclosingRdNode(resultSet, operateDate);

			int pid = json.getInt("pid");

//			if (map.containsKey(pid)) {
//				continue;
//			}

//			map.put(pid, 0);

			prep.setInt(1, pid);

			prep.setString(2, json.getString("geometry"));

			prep.setString(3, "");

			prep.setString(4, "");

			prep.setString(5, json.getString("meshid"));

			prep.setInt(6, json.getInt("isMain"));

			prep.setString(7, json.getString("op_date"));

			prep.setInt(8, json.getInt("op_lifecycle"));

			prep.executeUpdate();

			count += 1;

			if (count % 5000 == 0) {
				sqliteConn.commit();
			}
		}

		sqliteConn.commit();
	}
	
	private static JSONObject enclosingRdNode(ResultSet rs, String operateDate)
			throws Exception {

		JSONObject json = new JSONObject();

		int pid = rs.getInt("node_pid");

		json.put("pid", pid);
		
		int isMain = rs.getInt("is_main");

		json.put("isMain", isMain);

		String meshid = rs.getString("mesh_id");

		json.put("meshid", meshid);

		STRUCT struct = (STRUCT) rs.getObject("geometry");

		JGeometry geom = JGeometry.load(struct);

		String geometry = new String(wkt.fromJGeometry(geom));

		json.put("geometry", geometry);

		json.put("op_date", operateDate);

		json.put("op_lifecycle", 0);

		return json;
	}

	public static void exportBaseData(OracleAddress oa, String dir)
			throws Exception {

		exportRdLine(oa, dir);
		

	}

	public static void main(String[] args) throws Exception {
		String username1 = "beijing11";

		String password1 = "beijing11";

		int port1 = 1521;

		String ip1 = "192.168.4.131";

		String serviceName1 = "orcl";

		OracleAddress oa1 = new OracleAddress(username1, password1, port1, ip1,
				serviceName1);

		 //exportBaseData(oa1, "c:/1");
		exportBaseData2Sqlite(oa1, "./");
		 System.out.println("done");
	}
}
