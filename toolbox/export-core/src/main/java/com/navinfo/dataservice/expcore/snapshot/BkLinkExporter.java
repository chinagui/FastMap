package com.navinfo.dataservice.expcore.snapshot;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

public class BkLinkExporter {
	public static void run(Connection sqliteConn,
			Statement stmt, Connection conn, String operateDate, Set<Integer> meshes)
			throws Exception {
		// creating a LINESTRING table
		stmt.execute("create table gdb_bkLine(pid integer primary key)");
		stmt.execute("select addgeometrycolumn('gdb_bkLine','geometry',4326,'GEOMETRY','XY')");
		stmt.execute("select createspatialindex('gdb_bkLine','geometry')");
		stmt.execute("alter table gdb_bkLine add display_style text;");
		stmt.execute("alter table gdb_bkLine add display_text text;");
		stmt.execute("alter table gdb_bkLine add meshid text;");
		stmt.execute("alter table gdb_bkLine add kind integer;");
		stmt.execute("alter table gdb_bkLine add op_date text;");
		stmt.execute("alter table gdb_bkLine add op_lifecycle integer;");

		String insertSql = "insert into gdb_bkLine values("
				+ "?, GeomFromText(?, 4326), ?, ?, ?, ?, ?, ?)";
		PreparedStatement prep =null;
		PreparedStatement stmt2 =null;
		ResultSet resultSet =null;
		try{
			prep = sqliteConn.prepareStatement(insertSql);

			String sql = "select a.link_pid,a.geometry,a.mesh_id,a.kind from rw_link a where a.scale=0 and a.u_record != 2 and a.mesh_id in (select to_number(column_value) from table(clob_to_table(?)))";

			Clob clob = conn.createClob();
			clob.setString(1, StringUtils.join(meshes, ","));

			stmt2 = conn.prepareStatement(sql);

			stmt2.setClob(1, clob);
			
			resultSet = stmt2.executeQuery();

			resultSet.setFetchSize(5000);

			Map<Integer, Integer> map = new HashMap<Integer, Integer>();

			int count = 0;

			while (resultSet.next()) {

				JSONObject json = enclosingBkLine(resultSet, operateDate);

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

				prep.setString(7, json.getString("op_date"));

				prep.setInt(8, json.getInt("op_lifecycle"));

				prep.executeUpdate();

				count += 1;

				if (count % 5000 == 0) {
					sqliteConn.commit();
				}
			}

			sqliteConn.commit();
		}finally{
			try{if(resultSet!=null) resultSet.close();}catch(Exception e){}
			try{if(stmt2!=null) stmt2.close();}catch(Exception e){}
			try{if(prep!=null) prep.close();}catch(Exception e){}
		}
		
	}

	private static JSONObject enclosingBkLine(ResultSet rs, String operateDate)
			throws Exception {

		JSONObject json = new JSONObject();

		int pid = rs.getInt("link_pid");

		json.put("pid", pid);

		String meshid = rs.getString("mesh_id");

		json.put("meshid", meshid);

		json.put("display_text", "");

		int kind = rs.getInt("kind");

		json.put("kind", kind);
		
		json.put("display_style", kind+",23");

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
