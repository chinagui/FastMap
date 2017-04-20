package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.scripts.model.Block4Imp;
import com.navinfo.dataservice.scripts.model.City4Imp;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/** 
 * @ClassName: ImportCityBlockByJson
 * @author xiaoxiaowen4127
 * @date 2017年2月26日
 * @Description: ImportCityBlockByJson.java
 */
public class computeCityBlockByJson {

	private static QueryRunner runner = new QueryRunner();
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			if(args==null||args.length!=2){
				System.out.println("ERROR:need args:cityFile blockFile");
				return;
			}

			String cityFile = args[0];
			String blockFile = args[1];

			JobScriptsInterface.initContext();
			
			imp(cityFile,blockFile);

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
	
	public static void imp(String cityFile,String blockFile)throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			Map<String,City4Imp> citys = parseCity(cityFile);
			//write citys
			String insCitySql = "INSERT INTO CITY (CITY_ID,CITY_NAME,ADMIN_ID,PROVINCE_NAME,GEOMETRY,REGION_ID) VALUES (?,?,?,?,SDO_GEOMETRY(?,8307),(select REGION_ID from cp_region_province where province=?))";
			String updCityGridSql = "UPDATE GRID SET CITY_ID=? WHERE GRID_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
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
			}
			//
			Map<String,Block4Imp> blocks = parseBlock(citys,blockFile);
			//write blocks

			String insBlockSql = "INSERT INTO BLOCK (BLOCK_ID,CITY_ID,BLOCK_NAME,GEOMETRY,WORK_PROPERTY) VALUES (?,(SELECT CITY_ID FROM CITY WHERE CITY_NAME=?),?,SDO_GEOMETRY(?,8307),?)";
			String updBlockGridSql = "UPDATE GRID SET BLOCK_ID=? WHERE GRID_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
			for(Block4Imp block:blocks.values()){
				long blockId= getBlockId(conn);
				Geometry blockGeo = CompGridUtil.grids2Jts(block.getGrids());
				Clob clob = ConnectionUtil.createClob(conn);
				clob.setString(1, JtsGeometryFactory.writeWKT(blockGeo));
				run.update(conn, insBlockSql, blockId,block.getCityName(),block.getBlockName(),clob,block.getWrokProperty());
				//update grid table
				Clob clob2 = ConnectionUtil.createClob(conn);
				clob2.setString(1, StringUtils.join(block.getGrids(),","));
				run.update(conn, updBlockGridSql,blockId, clob2);
			}
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
			throw e;
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public static Map<String,City4Imp> parseCity(String cityFile)throws Exception{
		BufferedReader reader = null;
		try{
			System.out.println("Starting read city file...");
			Map<String,City4Imp> citys = new HashMap<String,City4Imp>();
			File file = new File(cityFile);

			InputStreamReader read = new InputStreamReader(
					new FileInputStream(file));

			reader = new BufferedReader(read);
			String line=null;
			while ((line = reader.readLine()) != null){
				JSONObject cityJson = JSONObject.fromObject(line);
				String cityName = cityJson.getString("city");
				if(citys.containsKey(cityName)){
					citys.get(cityName).getMeshes().add(cityJson.getString("meshId"));
				}else{
					City4Imp newCity = new City4Imp();
					newCity.setCityName(cityName);
					newCity.setAdminId(Integer.parseInt(cityJson.getString("code").substring(0, 6)));
					newCity.setProvName(cityJson.getString("province"));
					Set<String> meshes = new HashSet<String>();
					meshes.add(cityJson.getString("meshId"));
					newCity.setMeshes(meshes);
					citys.put(cityName, newCity);
				}
			}
			System.out.println("read city file over. city size:"+citys.size());
			return citys;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(reader!=null){
				reader.close();
			}
		}
		
	}

	public static Map<String,Block4Imp> parseBlock(Map<String,City4Imp> citys,String blockFile)throws Exception{
		BufferedReader reader = null;
		try{
			System.out.println("Starting read block file...");
			Map<String,Block4Imp> blocks = new HashMap<String,Block4Imp>();
			File file = new File(blockFile);

			InputStreamReader read = new InputStreamReader(
					new FileInputStream(file));

			reader = new BufferedReader(read);
			String line=null;
			while ((line = reader.readLine()) != null){
				JSONObject blockJson = JSONObject.fromObject(line);
				//先判断计算的grid是否超过了城市界定的图幅，如果超过了，忽略
				String cityName = blockJson.getString("city");
				String grid = blockJson.getString("gridId");
				String mesh = grid.substring(0, 6);
				if(citys.containsKey(cityName)&&citys.get(cityName).getMeshes().contains(mesh)){

					String blockCode = blockJson.getString("code");
					if(blocks.containsKey(blockCode)){
						blocks.get(blockCode).getGrids().add(grid);
					}else{
						Block4Imp newBlock = new Block4Imp();
						newBlock.setCityName(cityName);
						String blockName =blockJson.getString("province")+cityName+blockJson.getString("county")+blockJson.getString("job1")+blockJson.getString("job2");
						newBlock.setBlockName(blockName);
						newBlock.setWrokProperty(blockJson.getString("workProperty"));
						Set<String> grids = new HashSet<String>();
						grids.add(grid);
						newBlock.setGrids(grids);
						blocks.put(blockCode, newBlock);
					}
				}
			}
			System.out.println("read block file over. block size:"+citys.size());
			return blocks;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(reader!=null){
				reader.close();
			}
		}
		
	}
}
