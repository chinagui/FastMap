package com.navinfo.dataservice.expcore.snapshot;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.vividsolutions.jts.geom.Geometry;

public class RdGscExporter {
	
	public static void run(Connection sqliteConn,
			Statement stmt, Connection conn, String operateDate, Set<Integer> meshes)
			throws Exception {
		// creating a LINESTRING table
		stmt.execute("create table gdb_rdLink_gsc(uuid text primary key, gscPid integer)");
		stmt.execute("select addgeometrycolumn('gdb_rdLink_gsc','geometry',4326,'GEOMETRY','XY')");
		stmt.execute("select createspatialindex('gdb_rdLink_gsc','geometry')");
		stmt.execute("alter table gdb_rdLink_gsc add display_style text;");
		stmt.execute("alter table gdb_rdLink_gsc add display_text text;");
		stmt.execute("alter table gdb_rdLink_gsc add meshid text;");
		stmt.execute("alter table gdb_rdLink_gsc add z integer;");

		String insertSql = "insert into gdb_rdLink_gsc values("
				+ "?, ?, GeomFromText(?, 4326), ?, ?, ?, ?)";

		PreparedStatement prep = sqliteConn.prepareStatement(insertSql);

		String sql = "with tmp1 as  (select pid     from rd_gsc_link a    where a.table_name in ('RD_LINK', 'RW_LINK')    group by pid   having count(1) > 1), tmp2 as  (select a.*, b.geometry, b.mesh_id     from rd_gsc_link a, rd_link b, tmp1 c    where a.link_pid = b.link_pid      and a.pid = c.pid      and a.table_name = 'RD_LINK'), tmp3 as  (select a.*, b.geometry, b.mesh_id     from rd_gsc_link a, rw_link b, tmp1 c    where a.link_pid = b.link_pid      and a.pid = c.pid      and a.table_name = 'RW_LINK') select * from (select *   from tmp2 union all select * from tmp3) where mesh_id in (select to_number(column_value) from table(clob_to_table(?)))";

		Clob clob = conn.createClob();
		clob.setString(1, StringUtils.join(meshes, ","));

		PreparedStatement stmt2 = conn.prepareStatement(sql);

		stmt2.setClob(1, clob);
		
		ResultSet resultSet = stmt2.executeQuery();

		resultSet.setFetchSize(5000);

		int count = 0;

		while (resultSet.next()) {

			JSONObject json = enclosingRdLineGsc(resultSet, operateDate);

			prep.setString(1, json.getString("uuid"));

			prep.setInt(2, json.getInt("gscPid"));

			prep.setString(3, json.getString("geometry"));

			prep.setString(4, json.getString("display_style"));

			prep.setString(5, json.getString("display_text"));

			prep.setString(6, json.getString("meshid"));

			prep.setInt(7, json.getInt("z"));

			prep.executeUpdate();

			count += 1;

			if (count % 5000 == 0) {
				sqliteConn.commit();
			}
		}

		sqliteConn.commit();
	}

	private static JSONObject enclosingRdLineGsc(ResultSet rs,
			String operateDate) throws Exception {

		JSONObject json = new JSONObject();

		json.put("uuid", UuidUtils.genUuid());

		json.put("gscPid", rs.getInt("pid"));

		json.put("meshid", rs.getString("mesh_id"));

		json.put("z", rs.getInt("zlevel"));

		json.put("display_style", "");

		json.put("display_text", "");

		STRUCT struct = (STRUCT) rs.getObject("geometry");

		Geometry geo = GeoTranslator.struct2Jts(struct);

		int startEnd = rs.getInt("start_end");
		
		int seqNum = rs.getInt("shp_seq_num");

		Geometry line = DisplayUtils.getGscLine(geo, startEnd, seqNum);

		String geometry = GeoTranslator.jts2Wkt(line);

		json.put("geometry", geometry);

		return json;
	}
}
