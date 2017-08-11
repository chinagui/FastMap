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

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

public class BkFaceExporterSp9 {
	public static void run(Connection sqliteConn,
			Statement stmt, Connection conn, String operateDate, Set<Integer> meshes)
			throws Exception {
		// creating a LINESTRING table
		stmt.execute("create table gdb_bkFace(pid integer primary key)");
		stmt.execute("select addgeometrycolumn('gdb_bkFace','geometry',4326,'GEOMETRY','XY')");
		stmt.execute("select createspatialindex('gdb_bkFace','geometry')");
		stmt.execute("alter table gdb_bkFace add display_style text;");
		stmt.execute("alter table gdb_bkFace add display_text text;");
		stmt.execute("alter table gdb_bkFace add meshid text;");
		stmt.execute("alter table gdb_bkFace add kind integer;");
		stmt.execute("alter table gdb_bkFace add op_date text;");
		stmt.execute("alter table gdb_bkFace add op_lifecycle integer;");

		String insertSql = "insert into gdb_bkFace values("
				+ "?, GeomFromText(?, 4326), ?, ?, ?, ?, ?, ?)";
//				+ "?,  ?, ?, ?, ?, ?, ?)";
		PreparedStatement prep =null;
		PreparedStatement stmt2 =null;
		ResultSet resultSet = null;
		try{
			prep = sqliteConn.prepareStatement(insertSql);

//			String sql = "select a.face_pid,a.geometry,a.mesh_id,a.kind from lc_face a where a.scale=0 and a.u_record != 2 and a.mesh_id in (select to_number(column_value) from table(clob_to_table(?)))";

			//******zl 2017.02.17 增加查询 lu_face表中  kind = 6 的数据 //****zl 2017.05.04 增加了 cmg_buildface 中数据
			String sql = " select a.face_pid,a.geometry,a.mesh_id,a.kind from lc_face a where a.scale=0 and a.u_record != 2 and a.mesh_id in (select to_number(column_value) from table(clob_to_table(?))) "
						+ " union all "
						+ " select a.face_pid,a.geometry,a.mesh_id,106 kind from lu_face a where a.kind=6 and a.u_record != 2 and a.mesh_id in (select to_number(column_value) from table(clob_to_table(?))) "
						+ " union all  "
						+ " select a.face_pid,a.geometry,a.mesh_id,201 kind from cmg_buildface a where  a.u_record != 2 and A.MESH_ID=0";
			Clob clob = conn.createClob();
			clob.setString(1, StringUtils.join(meshes, ","));
			
			stmt2 = conn.prepareStatement(sql);

			stmt2.setClob(1, clob);
			stmt2.setClob(2, clob);
			
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

		json.put("display_text", "");

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
