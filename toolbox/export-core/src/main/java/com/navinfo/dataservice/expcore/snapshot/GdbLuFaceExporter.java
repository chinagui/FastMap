package com.navinfo.dataservice.expcore.snapshot;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

public class GdbLuFaceExporter {
	public static void run(Connection sqliteConn,
			Statement stmt, Connection conn, String operateDate, Set<Integer> meshes)
			throws Exception {
		// creating a LINESTRING table
		stmt.execute("DROP TABLE IF EXISTS gdb_luFace;");
		stmt.execute("create table gdb_luFace(pid integer primary key)");
		stmt.execute("select addgeometrycolumn('gdb_luFace','geometry',4326,'GEOMETRY','XY')");
		stmt.execute("select createspatialindex('gdb_luFace','geometry')");
		stmt.execute("alter table gdb_luFace add display_style text;");
		stmt.execute("alter table gdb_luFace add display_text text;");
		stmt.execute("alter table gdb_luFace add meshid text;");
		stmt.execute("alter table gdb_luFace add kind integer;");
		stmt.execute("alter table gdb_luFace add op_date text;");
		stmt.execute("alter table gdb_luFace add op_lifecycle integer;");

		String insertSql = "insert into gdb_luFace values("
				+ "?, GeomFromText(?, 4326), ?, ?, ?, ?, ?, ?)";
		PreparedStatement prep =null;
		PreparedStatement stmt2 =null;
		ResultSet resultSet =null;
		
		try{
			prep = sqliteConn.prepareStatement(insertSql);

			//******zl 2017.02.17 增加查询 lu_face表中  kind = 6 的数据 
			String sql = " select l.face_pid ,l.geometry,"
					+ " (select n.name from lu_face_name n where n.face_pid = l.face_pid and n.name_groupid = 1 and (n.lang_code = 'CHI' OR n.lang_code = 'CHT') and rownum =1 ) name ,"
					+ " l.mesh_id,l.kind from lu_face l  where "
					+ "  l.kind in (0,1,2,3,4,5,6,7,11,24,30,31,32,33,34,35,36,37,38,39,40) "
					+ "  and l.mesh_id in (select to_number(column_value) from table(clob_to_table(?))) " ;
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(meshes, ","));

			stmt2 = conn.prepareStatement(sql);

			stmt2.setClob(1, clob);
			
			resultSet = stmt2.executeQuery();

			resultSet.setFetchSize(5000);

			int count = 0;

			//*******zl 2017.02.21  去重pid 重复的结果******
			List<Integer> pids = new ArrayList<Integer>();
			
			while (resultSet.next()) {

				JSONObject json = enclosingBkFace(resultSet, operateDate);

				int pid = json.getInt("pid");

				if(!pids.contains(pid)){
					
					pids.add(pid);
				
				prep.setInt(1, pid);

				prep.setString(2, json.getString("geometry"));

				prep.setString(3, json.getString("display_style"));

				prep.setString(4, json.getString("name"));

				prep.setString(5, json.getString("meshid"));

				prep.setDouble(6, json.getInt("kind"));

				prep.setString(7, json.getString("op_date"));

				prep.setInt(8, json.getInt("op_lifecycle"));

				prep.executeUpdate();

				count += 1;

				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
				
				}
			}

			sqliteConn.commit();
		}finally{
			try{if(resultSet!=null) resultSet.close();}catch(Exception e){}
			try{if(stmt2!=null) stmt2.close();}catch(Exception e){}
			try{if(prep!=null) prep.close();}catch(Exception e){}
		}
		
	}

	private static JSONObject enclosingBkFace(ResultSet rs, String operateDate)
			throws Exception {

		JSONObject json = new JSONObject();

		int pid = rs.getInt("face_pid");

		json.put("pid", pid);

		int meshid = rs.getInt("mesh_id");

		json.put("meshid", String.valueOf(meshid));

		String name = "";
		if(rs.getString("name") != null && StringUtils.isNotEmpty(rs.getString("name"))){
			name = rs.getString("name");
		}
		json.put("name", name);

		json.put("display_style", "");

		int kind = rs.getInt("kind");

		json.put("kind", kind);

		STRUCT struct = (STRUCT) rs.getObject("geometry");

		JGeometry geom = JGeometry.load(struct);
		
		WKT wkt = new WKT();

		String geometry = new String(wkt.fromJGeometry(geom));

		json.put("geometry", geometry);

		json.put("op_date", operateDate);

		json.put("op_lifecycle", 0);

		return json;
	}
}
