package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.uima.pear.util.FileUtil;
import org.sqlite.SQLiteConfig;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: ComputeRegionMeshBoundary
 * @author xiaoxiaowen4127
 * @date 2017年7月25日
 * @Description: ComputeRegionMeshBoundary.java
 */
public class ComputeRegionMeshBoundary {
	public static Logger log = Logger.getLogger(SyncTips2Oracle.class);

	public static Set<String> getSingleRegionMeshes(Connection conn,int regionId)throws Exception{
		String sql = "SELECT M.MESH FROM CP_MESHLIST@METADB_LINK M,CP_REGION_PROVINCE R WHERE M.ADMINCODE=R.ADMINCODE AND REGION_ID=?";
		return new QueryRunner().query(conn, sql, new ResultSetHandler<Set<String>>(){

			@Override
			public Set<String> handle(ResultSet rs) throws SQLException {
				Set<String> res = new HashSet<String>();
				while(rs.next()){
					res.add(rs.getString("MESH"));
				}
				return res;
			}
			
		},regionId);
	}
	
	public static void writeSqliteDb(String dir,String fileName,Map<Integer,Set<String>> regionMeshes)throws Exception{
		File file = new File(dir);

		if (file.exists()) {
			FileUtil.deleteDirectory(file);
		}

		file.mkdirs();

		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		Connection sqliteConn = null;

		// enabling dynamic extension loading
		// absolutely required by SpatiaLite
		SQLiteConfig config = new SQLiteConfig();
		config.enableLoadExtension(true);

		// create a database connection
		sqliteConn = DriverManager.getConnection("jdbc:sqlite:" + dir
				+ "/regionBoundary.sqlite", config.toProperties());
		Statement stmt = sqliteConn.createStatement();
		stmt.setQueryTimeout(30); // set timeout to 30 sec.

		// loading SpatiaLite
		stmt.execute("SELECT load_extension('/usr/local/lib/mod_spatialite.so')");

		// enabling Spatial Metadata
		// using v.2.4.0 this automatically initializes SPATIAL_REF_SYS and
		// GEOMETRY_COLUMNS
		stmt.execute("SELECT InitSpatialMetadata()");

		sqliteConn.setAutoCommit(false);
		
		//create table
		stmt.execute("create table region_boundary(region_id text primary key)");
		stmt.execute("select addgeometrycolumn('region_boundary','boundary',4326,'GEOMETRY','XY')");//add GEOMETRY column
		stmt.execute("select createspatialindex('region_boundary','boundary')");
		log.info("table created.");
		//insert rows
		String sql = "insert into region_boundary values(?, GeomFromText(?, 4326))";
		PreparedStatement prep = sqliteConn.prepareStatement(sql);
		for(Entry<Integer,Set<String>> entry:regionMeshes.entrySet()){
			prep.setString(1, String.valueOf(entry.getKey()));
			Geometry geo = MeshUtils.meshes2Jts(entry.getValue());
			prep.setString(2, geo.toText());
			prep.executeUpdate();
			log.info("region:"+entry.getKey()+" inserted");
		}
		sqliteConn.commit();
		prep.close();
		stmt.close();
		sqliteConn.close();
	}
	
	public static void writeJsonFile(Map<Integer,Set<String>> regionMeshes)throws Exception{

		JSONArray ja = new JSONArray();
		for(Entry<Integer,Set<String>> entry:regionMeshes.entrySet()){
			JSONObject jo = new JSONObject();
			jo.put("regionId", entry.getKey());
			Geometry geo = MeshUtils.meshes2Jts(entry.getValue());
			jo.put("boundary", geo.toText());
			ja.add(jo);
		}
		log.info(ja.toString());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection conn = null;
		try{
			DbInfo tiInfo = DbService.getInstance().getOnlyDbByBizType("fmMan");
			final OracleSchema schema = new OracleSchema(
					DbConnectConfig.createConnectConfig(tiInfo.getConnectParam()));
			conn = schema.getPoolDataSource().getConnection();
			int[] regionIds = new int[]{13};
			Map<Integer,Set<String>> regionMeshes = new HashMap<Integer,Set<String>>();
			for(int i:regionIds){
				regionMeshes.put(i, getSingleRegionMeshes(conn,i));
			}
			//
			//writeJsonFile(regionMeshes);
			//
			if(args.length!=2){
				System.out.println("ERROR:need args:dir filename");
				return;
			}
			String dir = args[0];
			if(StringUtils.isEmpty(dir)){
				System.out.println("ERROR:need args:dir filename");
				return;
			}
			String fileName = args[1];
			if(StringUtils.isEmpty(fileName)){
				System.out.println("ERROR:need args:dir filename");
				return;
			}
			writeSqliteDb(dir,fileName,regionMeshes);
			
		}catch(Exception e){
			log.error(e.getMessage(),e);
			log.info("Over.");
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}

}
