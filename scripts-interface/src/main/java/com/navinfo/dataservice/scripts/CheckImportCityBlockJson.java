package com.navinfo.dataservice.scripts;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.scripts.model.Block4Imp;
import com.navinfo.dataservice.scripts.model.City4Imp;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

/** 
 * @ClassName: CheckImportCityBlockJson
 * @author zl 
 * @date 2017.06.12
 * @Description: CheckImportCityBlockJson.java
 */
public class CheckImportCityBlockJson {
	private static Logger log = LoggerRepos.getLogger(CheckImportCityBlockJson.class);
	
	//需要更新的city数据  <"错误信息",meshs>
	private static Map<String,Set<String>> cityUpdateMap;
	//需要更新的city数据  <"错误信息",grids>
	private static Map<String,Set<String>> blockUpdateMap;
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			Connection conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.3.152:1521/orcl", "gdb250_16sum_ml_nd", "gdb250_16sum_ml_nd").getConnection();
			

			check(conn);
			
			log.info("Over.");
			System.exit(0);
		} catch (Exception e) {
			log.error("Oops, something wrong...", e );
			e.printStackTrace();
		}
	}
	
	/**
	 * @Title: check
	 * @Description: 执行检查
	 * @param conn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月19日 下午2:56:42 
	 */
	public static void check(Connection conn)throws Exception{
		try{
			cityUpdateMap = new HashMap<String,Set<String>>();
			blockUpdateMap = new HashMap<String,Set<String>>();
			Map<String,City4Imp> citys = parseCity(conn);
			
			Map<String,Block4Imp> blocks = parseBlock(citys,conn);
			Set<String> gridUpdateSet3 = new HashSet<String>();
			for(Map.Entry<String,Block4Imp> block:blocks.entrySet()){
				
				Geometry blockGeo = CompGridUtil.grids2Jts(block.getValue().getGrids());
				//判断是否出现复杂多边形
				if (blockGeo instanceof MultiPolygon) {
					Set<String> grids = block.getValue().getGrids();
					//判断geometry 是否是复杂多边形
					gridUpdateSet3.addAll(grids);
					log.info(" 检查: "+"此block几何含复杂多边形!");
				}
			}
			blockUpdateMap.put("block几何含复杂多边形;", gridUpdateSet3);
			
			updateCity(conn);
			
			updateBlock(conn);
			
			//GRID未分配给任何block
			updateBlock2(conn);
			
			parseInitBlock(conn,citys.size(),blocks.size());
			
			updateCity2(conn);

		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	
	/**
	 * @Title: parseCity
	 * @Description: 处理city 数据
	 * @param conn
	 * @return
	 * @throws Exception  Map<String,City4Imp>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月19日 下午2:59:30 
	 */
	public static Map<String,City4Imp> parseCity(Connection conn)throws Exception{
		Statement pstmt = null;
		ResultSet resultSet = null;
		try{
			Map<String,City4Imp> citys = new HashMap<String,City4Imp>();
			String selectSql = " SELECT C.MESH_ID,C.PROVINCE,C.CITY,C.ERR_CODE,C.ERR_MSG,C.GEOMETRY FROM SHD_MESH_CITY C ";
			
			pstmt = conn.createStatement();
			resultSet = pstmt.executeQuery(selectSql);
			
			while (resultSet.next()) {
				String cityName = resultSet.getString("city");
				if(citys.containsKey(cityName)){
					citys.get(cityName).getMeshes().add(resultSet.getString("mesh_id"));
				}else{
					City4Imp newCity = new City4Imp();
					newCity.setCityName(cityName);
//					newCity.setAdminId(Integer.parseInt(resultSet.getString("code").substring(0, 6)));
					newCity.setProvName(resultSet.getString("province"));
					Set<String> meshes = new HashSet<String>();
					meshes.add(resultSet.getString("mesh_id"));
					newCity.setMeshes(meshes);
					citys.put(cityName, newCity);
				}
				
			}
			System.out.println("search city  over. city size:"+citys.size());
			return citys;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(pstmt != null){
				DbUtils.closeQuietly(pstmt);
			}
			if(resultSet != null){
				DbUtils.closeQuietly(resultSet);
			}
		}
	}
	
	
	/**
	 * @Title: parseBlock
	 * @Description: 处理block数据 并检查
	 * @param citys
	 * @param conn
	 * @return
	 * @throws Exception  Map<String,Block4Imp>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月19日 下午2:59:55 
	 */
	public static Map<String,Block4Imp> parseBlock(Map<String,City4Imp> citys,Connection conn)throws Exception{
		Statement pstmt = null;
		ResultSet resultSet = null;
		try{
			Map<String,Block4Imp> blocks = new HashMap<String,Block4Imp>();

			Map<String,String> meshCity = new HashMap<String,String>();
			
			String selectSql = "   SELECT G.GRID_ID,G.BLOCKCODE,G.GEOMETRY,U.CITY  FROM  SHD_GRID_BLOCK G,SHD_COUNTY_UNION U  WHERE G.BLOCKCODE = U.BLOCKCODE AND U.BLOCKCODE IS NOT NULL";
			
			pstmt = conn.createStatement();
			resultSet = pstmt.executeQuery(selectSql);
			Set<String> gridUpdateSet1 = new HashSet<String>();
			while (resultSet.next()){
				//先判断计算的grid是否超过了城市界定的图幅，如果超过了，忽略
				String cityName = resultSet.getString("city");
				String grid = resultSet.getString("grid_id");
				String mesh = grid.substring(0, 6);
				if(meshCity.containsKey(mesh)){
					//图幅已经有对应的city  比较已有city 与现有city是否一致
					String oldCityName = meshCity.get(mesh);
					if(!cityName.equals(oldCityName)){
						//同一个图幅的grid 被分配到了不同的 city
						gridUpdateSet1.add(grid);
					
						log.info(" 检查: "+"同一个图幅的grid被分配到了不同的 city ");
						log.info(" 当前grid: "+grid+" 当前city: "+cityName+" 同组grid分配city: "+oldCityName);
					}
				}else{
					meshCity.put(mesh, cityName);
				}
				if(citys.containsKey(cityName)&&citys.get(cityName).getMeshes().contains(mesh)){

					String blockCode = resultSet.getString("blockcode");
					if(blocks.containsKey(blockCode)){
						blocks.get(blockCode).getGrids().add(grid);
					}else{
						Block4Imp newBlock = new Block4Imp();
						newBlock.setCityName(cityName);
						Set<String> grids = new HashSet<String>();
						grids.add(grid);
						newBlock.setGrids(grids);
						blocks.put(blockCode, newBlock);
					}
				}
			}
			blockUpdateMap.put("同一个图幅的grid被分配到了不同的 city;", gridUpdateSet1);
			
			return blocks;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(pstmt != null){
				DbUtils.closeQuietly(pstmt);
			}
			if(resultSet != null){
				DbUtils.closeQuietly(resultSet);
			}
		}
		
	}
	
	/**
	 * @Title: parseInitBlock
	 * @Description: 处理原始数据
	 * @param conn
	 * @param cityCount
	 * @param blockCount
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月19日 下午3:00:18 
	 */
	private static void parseInitBlock(Connection conn,int cityCount,int blockCount) throws Exception {
		int initCityCount = 0;
		int initBlockCount = 0;
		Statement pstmt = null;
		ResultSet resultSet = null;
		try{
			String selectSql = " select count(distinct u.blockcode) block_count,count(distinct u.city) city_count from SHD_COUNTY_UNION u  ";
			
			
			pstmt = conn.createStatement();
			resultSet = pstmt.executeQuery(selectSql);
			System.out.println("Starting read initBlockFile file...");
			
			while (resultSet.next()){
				//先判断计算的grid是否超过了城市界定的图幅，如果超过了，忽略
				initBlockCount =resultSet.getInt("block_count");
				initCityCount =resultSet.getInt("city_count");
			}
			
			if(initCityCount != cityCount){
				log.info(conn+" 检查:"+" 导入的city ("+initCityCount+")和原始文件中的city("+cityCount+") 数量不一致!");
			}
			
			if(initBlockCount != blockCount){
				log.info(conn+" 检查:"+" 导入的block ("+initBlockCount+")和原始文件中的block("+blockCount+") 数量不一致!");
			}
			
			System.out.println("read initBlockFile file over.");
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(pstmt != null){
				DbUtils.closeQuietly(pstmt);
			}
			if(resultSet != null){
				DbUtils.closeQuietly(resultSet);
			}
		}
	}

	
	/**
	 * @Title: updateCity
	 * @Description: 更新city的 检查信息
	 * @param conn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月19日 下午2:57:02 
	 */
	public static void updateCity(Connection conn)throws Exception{
		PreparedStatement stmt = null;
		QueryRunner run = null;
		try{
			if(cityUpdateMap != null  && cityUpdateMap.size() >0){
				run = new QueryRunner();
				//write citys
				String updCitySql = "   UPDATE SHD_MESH_CITY C SET C.ERR_CODE = 1,C.ERR_MSG=C.ERR_MSG ||? WHERE C.MESH_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
				for(Map.Entry<String, Set<String>> city : cityUpdateMap.entrySet()){
					String key = city.getKey();
					Set<String> citySet = city.getValue();
					
					Clob clob1 = ConnectionUtil.createClob(conn);
					clob1.setString(1, StringUtils.join(citySet,","));
					run.update(conn, updCitySql,key, clob1);
					conn.commit();
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(stmt != null){
				DbUtils.closeQuietly(stmt);
			}
		}
	}
	/**
	 * @Title: updateBlock
	 * @Description: 更新 block 的检查信息
	 * @param conn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月19日 下午2:57:33 
	 */
	public static void updateBlock(Connection conn)throws Exception{
		PreparedStatement stmt = null;
		try{

			if(blockUpdateMap != null && blockUpdateMap.size() > 0){
				QueryRunner run = new QueryRunner();
				//write citys
				String updBlockSql = " UPDATE SHD_GRID_BLOCK G SET G.ERR_CODE = 1,G.ERR_MSG=G.ERR_MSG || ? WHERE G.GRID_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
				for(Map.Entry<String, Set<String>> block : blockUpdateMap.entrySet()){
					String key = block.getKey();
					Set<String> blockSet = block.getValue();
					
					Clob clob1 = ConnectionUtil.createClob(conn);
					clob1.setString(1, StringUtils.join(blockSet,","));
					run.update(conn, updBlockSql,key, clob1);
					conn.commit();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(stmt != null){
				DbUtils.closeQuietly(stmt);
			}
		}
	}
	/**
	 * @Title: updateBlock2
	 * @Description: 更新 block表: grid未分配给任何block
	 * @param conn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月19日 下午2:58:09 
	 */
	public static void updateBlock2(Connection conn)throws Exception{
		try{
				QueryRunner run = new QueryRunner();
				//write citys
				String updBlockSql = " UPDATE SHD_GRID_BLOCK G SET G.ERR_CODE = 1,G.ERR_MSG=G.ERR_MSG || ? WHERE G.BLOCKCODE IS NULL";
					run.update(conn, updBlockSql,"grid未分配给任何block;");
					conn.commit();
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
		}
	}
	/**
	 * @Title: updateCity2
	 * @Description: 更新city的 检查信息:city组成的省份图幅和cp_meshlist省份图幅不一致;
	 * @param conn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月19日 下午2:58:45 
	 */
	public static void updateCity2(Connection conn)throws Exception{
		PreparedStatement stmt = null;
		try{
				QueryRunner run = new QueryRunner();
				//write citys
				String updCitySql = "UPDATE SHD_MESH_CITY C SET C.ERR_CODE = 1,C.ERR_MSG=C.ERR_MSG ||? "
						+ " WHERE "
						+ " exists(select 1 from cp_meshlist@dblink_rms M where  M.MESH = C.MESH_ID and M.PROVINCE != C.PROVINCE ) ";
					run.update(conn, updCitySql,"city组成的省份图幅和cp_meshlist省份图幅不一致;");
					conn.commit();
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(stmt != null){
				DbUtils.closeQuietly(stmt);
			}
		}
	}
	
	

}
