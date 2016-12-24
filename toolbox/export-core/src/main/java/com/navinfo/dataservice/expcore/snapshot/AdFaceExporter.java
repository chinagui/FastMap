package com.navinfo.dataservice.expcore.snapshot;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

public class AdFaceExporter {
	public static void run(Connection sqliteConn,
			Statement stmt, Connection conn, String operateDate, Set<Integer> meshes)
			throws Exception {
		// creating a GEOMETRY table
		stmt.execute("create table gdb_adFace(pid integer primary key, region_id integer)");
		stmt.execute("select addgeometrycolumn('gdb_adFace','geometry',4326,'GEOMETRY','XY')");
		stmt.execute("select createspatialindex('gdb_adFace','geometry')");
		stmt.execute("alter table gdb_adFace add display_style text;");
		stmt.execute("alter table gdb_adFace add display_text text;");
		stmt.execute("alter table gdb_adFace add meshid text;");
		stmt.execute("alter table gdb_adFace add op_date text;");
		stmt.execute("alter table gdb_adFace add op_lifecycle integer;");

		String insertSql = "insert into gdb_adFace values("
				+ "?,?, GeomFromText(?, 4326), ?, ?, ?, ?, ?)";

		PreparedStatement pstm = sqliteConn.prepareStatement(insertSql);

		String sql = "select a.face_pid,a.region_id, a.geometry, a.mesh_id from ad_face a where a.u_record != 2 and a.mesh_id in (select to_number(column_value) from table(clob_to_table(?)))";

		Clob clob = conn.createClob();
		clob.setString(1, StringUtils.join(meshes, ","));

		PreparedStatement stmt2 = conn.prepareStatement(sql);

		stmt2.setClob(1, clob);
		
		ResultSet rs = stmt2.executeQuery();

		rs.setFetchSize(5000);

		int count = 0;
		
		WKT wkt = new WKT();
		
		while (rs.next()) {
			
			pstm.setInt(1, rs.getInt("face_pid"));
			pstm.setInt(2, rs.getInt("region_id"));
			String geom = new String(wkt.fromJGeometry(wkt.toJGeometry(rs.getBytes("geometry"))));
			pstm.setString(3, geom);

			pstm.setString(4, "");

			pstm.setString(5, "");
			pstm.setString(6, String.valueOf(rs.getInt("mesh_id")));
			pstm.setString(7, operateDate);
			pstm.setInt(8, 0);
			pstm.executeUpdate();

			count += 1;

			if (count % 5000 == 0) {
				sqliteConn.commit();
			}
		}

		sqliteConn.commit();
	}
}
