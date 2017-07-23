package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.scripts.model.Block4Imp;
import com.navinfo.dataservice.scripts.model.City4Imp;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DbLinkCreator;
import com.navinfo.navicommons.database.sql.SqlExec;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/** 
 * @ClassName: ImportCityBlockByJson
 * @author xiaoxiaowen4127
 * @date 2017年2月26日
 * @Description: ImportCityBlockByJson.java
 */
public class ImportCityBlockByOracle {
	
	private static Logger log = Logger.getLogger(ImportCityBlockByOracle.class);

	private static QueryRunner runner = new QueryRunner();
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			if(args==null||args.length!=3){
				System.out.println("ERROR:need args:rawBlockTable,cityTable blockTable");
				return;
			}

			String rawBlockTable = args[0];
			String cityTable = args[1];
			String blockTable = args[2];

			imp(rawBlockTable,cityTable,blockTable);
			
			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}
	}
	private static long getCityId(Connection conn)throws SQLException{
		String sql = "SELECT CITY_SEQ.NEXTVAL FROM DUAL";
		return runner.queryForLong(conn, sql);
	}
	
	private static long getBlockId(Connection conn)throws SQLException{
		String sql = "SELECT BLOCK_SEQ.NEXTVAL FROM DUAL";
		return runner.queryForLong(conn, sql);
	}
	
	public static void imp(String rawBlockTable,String cityTable,String blockTable)throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		try{

			DbInfo manInfo = DbService.getInstance().getOnlyDbByBizType("fmMan");

			OracleSchema manSchema = new OracleSchema(
					DbConnectConfig.createConnectConfig(manInfo.getConnectParam()));
			conn = manSchema.getPoolDataSource().getConnection();
			QueryRunner run = new QueryRunner();
			Map<String,City4Imp> citys = parseCity(conn,rawBlockTable,cityTable);
			System.out.println("read city table over. city size:"+citys.size());
			//write citys
			String insCitySql = "INSERT INTO CITY (CITY_ID,CITY_NAME,ADMIN_ID,PROVINCE_NAME,GEOMETRY,REGION_ID) VALUES (?,?,?,?,SDO_GEOMETRY(?,8307),(select REGION_ID from cp_region_province where province=?))";
			String updCityGridSql = "UPDATE GRID SET CITY_ID=? WHERE GRID_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
			int cityCount=0;
			for(City4Imp city:citys.values()){
				long cityId= getCityId(conn);
				Geometry cityGeo = MeshUtils.meshes2Jts(city.getMeshes());
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, JtsGeometryFactory.writeWKT(cityGeo));
				run.update(conn, insCitySql, cityId,city.getCityName(),city.getAdminId(),city.getProvName(),clob,city.getProvName());
				//update grid table
				Set<String> grids = new HashSet<String>();
				for(String mesh:city.getMeshes()){
					grids.addAll(CompGridUtil.mesh2Grid(mesh));
				}

				Clob clob2 = ConnectionUtil.createClob(conn);
				clob2.setString(1, StringUtils.join(grids,","));
				run.update(conn, updCityGridSql,cityId, clob2);
				cityCount++;
				if(cityCount%10==0){
					System.out.println("imp city:"+cityCount);
				}
			}
			conn.commit();
			System.out.println("imp city:"+cityCount);
			//
			Map<String,Block4Imp> blocks = parseBlock(conn,rawBlockTable,blockTable);
			getBlockOriginGeometry(conn,rawBlockTable,blocks);
			System.out.println("read block table over. block size:"+blocks.size());
			//write blocks

			String insBlockSql = "INSERT INTO BLOCK (BLOCK_ID,CITY_ID,BLOCK_NAME,GEOMETRY,WORK_PROPERTY,ORIGIN_GEO) VALUES (?,(SELECT CITY_ID FROM CITY WHERE CITY_NAME=?),?,SDO_GEOMETRY(?,8307),?,SDO_GEOMETRY(?,8307))";
			String updBlockGridSql = "UPDATE GRID SET BLOCK_ID=? WHERE GRID_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
			int bCount =0;
			for(Block4Imp block:blocks.values()){
				long blockId= getBlockId(conn);
				Geometry blockGeo = CompGridUtil.grids2Jts(block.getGrids());
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, JtsGeometryFactory.writeWKT(blockGeo));
				Clob clog2 = ConnectionUtil.createClob(conn,JtsGeometryFactory.writeWKT(block.getOriginGeomtry()));
				
				run.update(conn, insBlockSql, blockId,block.getCityName(),block.getBlockName(),clob,block.getWrokProperty(),clog2);
				//update grid table
				Clob clob2 = ConnectionUtil.createClob(conn);
				clob2.setString(1, StringUtils.join(block.getGrids(),","));
				run.update(conn, updBlockGridSql,blockId, clob2);
				bCount++;
				if(bCount%10==0){
					System.out.println("imp block:"+bCount);
				}
			}
			conn.commit();
			System.out.println("imp block:"+bCount);

			//update city admin geo
			//初始化脚本，忽略事务一致了
			DbInfo gdbInfo = DbService.getInstance().getOnlyDbByBizType("nationRoad");
			DbConnectConfig gdbConfig = DbConnectConfig.createConnectConfig(gdbInfo.getConnectParam());
			DbLinkCreator cr = new DbLinkCreator();
			cr.create("FMGDB_LINK", false, manSchema.getPoolDataSource(), 
					gdbConfig.getUserName(), gdbConfig.getUserPasswd(), gdbConfig.getServerIp(), String.valueOf(gdbConfig.getServerPort()), gdbConfig.getServiceName());
			SqlExec sqlExec = new SqlExec(conn);
			String sqlFile = "/com/navinfo/dataservice/scripts/resources/update_man_city_admingeo.sql";
			sqlExec.execute(sqlFile);
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
			throw e;
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public static Map<String,City4Imp> parseCity(Connection conn,String rawBlockTable,String cityTable)throws Exception{
		try{
			System.out.println("Starting read city table...");
			String sql = "SELECT M.MESH_ID MESH_ID,M.CITY CITY,M.PROVINCE PROVINCE,TO_CHAR(TO_NUMBER(SUBSTR(R.BLOCKCODE,0,4))*100) CODE,M.GEOMETRY GEOMETRY FROM "+cityTable+" M,(SELECT CITY,BLOCKCODE FROM "+rawBlockTable+" A WHERE A.ROWID = (SELECT MAX(ROWID) FROM "+rawBlockTable+" B WHERE A.CITY=B.CITY)) R WHERE M.CITY=R.CITY";
			
			return new QueryRunner().query(conn, sql, new ResultSetHandler<Map<String,City4Imp>>(){

				@Override
				public Map<String, City4Imp> handle(ResultSet rs) throws SQLException {
					Map<String,City4Imp> citys = new HashMap<String,City4Imp>();
					while(rs.next()){
						String cityName = rs.getString("CITY");
						if(citys.containsKey(cityName)){
							citys.get(cityName).getMeshes().add(rs.getString("MESH_ID"));
						}else{
							City4Imp newCity = new City4Imp();
							newCity.setCityName(cityName);
							newCity.setAdminId(Integer.parseInt(rs.getString("CODE")));
							newCity.setProvName(rs.getString("PROVINCE"));
							Set<String> meshes = new HashSet<String>();
							meshes.add(rs.getString("MESH_ID"));
							newCity.setMeshes(meshes);
							citys.put(cityName, newCity);
						}
					}
					return citys;
				}
				
			});
			
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		
	}

	public static Map<String,Block4Imp> parseBlock(Connection conn,String rawBlockTable,String blockTable)throws Exception{
		try{
			System.out.println("Starting read block file...");
			String sql = "SELECT G.GRID_ID,G.BLOCKCODE,G.GEOMETRY,R.CITY,R.PROVINCE,R.COUNTY,R.JOB1,R.JOB2 FROM "+blockTable+" G,"+rawBlockTable+" R WHERE G.BLOCKCODE=R.BLOCKCODE";
			return new QueryRunner().query(conn, sql, new ResultSetHandler<Map<String,Block4Imp>>(){

				@Override
				public Map<String, Block4Imp> handle(ResultSet rs) throws SQLException {
					Map<String,Block4Imp> blocks = new HashMap<String,Block4Imp>();
					while(rs.next()){
						String blockCode = rs.getString("BLOCKCODE");
						String gridId = rs.getString("GRID_ID");
						if(blocks.containsKey(blockCode)){
							blocks.get(blockCode).getGrids().add(gridId);
						}else{
							Block4Imp newBlock = new Block4Imp();
							String cityName = rs.getString("CITY");
							newBlock.setCityName(cityName);
							String county = rs.getString("COUNTY");
							String job1 = rs.getString("JOB1");
							String job2 = rs.getString("JOB2");
							String blockName =rs.getString("PROVINCE")+cityName+(StringUtils.isEmpty(county)?"":county)+(StringUtils.isEmpty(job1)?"":job1)+(StringUtils.isEmpty(job2)?"":job2);
							newBlock.setBlockName(blockName);
//							newBlock.setWrokProperty(blockJson.getString("workProperty"));
							Set<String> grids = new HashSet<String>();
							grids.add(gridId);
							newBlock.setGrids(grids);
							blocks.put(blockCode, newBlock);
						}
					}
					return blocks;
				}
				
			});
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		
	}
	public static void getBlockOriginGeometry(Connection conn,String rawBlockTable,Map<String,Block4Imp> blocks)throws Exception{
		try{
			System.out.println("Starting read raw block geo file...");
			String sql = "SELECT BLOCKCODE,GEOMETRY FROM "+rawBlockTable;
			Map<String,Geometry> geos = new QueryRunner().query(conn, sql, new ResultSetHandler<Map<String,Geometry>>(){

				@Override
				public Map<String, Geometry> handle(ResultSet rs) throws SQLException {
					try{
						Map<String,Geometry> geos = new HashMap<String,Geometry>();
						while(rs.next()){
							STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
							Geometry geo = GeoTranslator.struct2Jts(struct);
							String blockCode = rs.getString("BLOCKCODE");
							geos.put(blockCode, geo);
						}
						return geos;
					}catch(Exception e){
						log.error(e.getMessage(),e);
						throw new SQLException(e.getMessage(),e);
					}
				}
				
			});
			for(Entry<String,Geometry> entry:geos.entrySet()){
				Block4Imp block = blocks.get(entry.getKey());
				if(block!=null){
					block.setOriginGeomtry(entry.getValue());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		
	}
}
