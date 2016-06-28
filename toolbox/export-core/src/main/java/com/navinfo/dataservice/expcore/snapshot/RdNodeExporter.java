package com.navinfo.dataservice.expcore.snapshot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

public class RdNodeExporter {
	public static void run(Connection sqliteConn,
			Statement stmt, Connection conn, String operateDate)
			throws Exception {
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

		Statement stmt2 = conn.createStatement();

		String oraSql = "select b.node_pid,b.geometry,c.mesh_id,a.is_main from rd_cross_node a,rd_node b,rd_node_mesh c where c.node_pid=b.node_pid and a.node_pid=b.node_pid and a.u_record!=2 and b.u_record!=2";

		ResultSet resultSet = stmt2.executeQuery(oraSql);

		resultSet.setFetchSize(5000);

		int count = 0;

		while (resultSet.next()) {

			JSONObject json = enclosingRdNode(resultSet, operateDate);

			int pid = json.getInt("pid");

			prep.setInt(1, pid);

			prep.setString(2, json.getString("geometry"));

			prep.setString(3, json.getString("display_style"));

			prep.setString(4, json.getString("display_text"));

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

		if (isMain == 0) {
			json.put("display_style", "28864");
		} else {
			json.put("display_style", "12566463");
		}

		json.put("display_text", "");

		String meshid = rs.getString("mesh_id");

		json.put("meshid", meshid);

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
