package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.GeometryExceptionWithContext;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.model.Block;
import com.navinfo.dataservice.api.man.model.City;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbServerType;
import com.navinfo.dataservice.commons.database.oracle.MyPoolGuardConnectionWrapper;
import com.navinfo.dataservice.commons.database.oracle.MyPoolableConnection;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.expcore.ExportConfig;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class ImportBlockByGrid {
	
	private static int CityId = 0;
	
	private static int BlockId = 0;

	private static QueryRunner runner = new QueryRunner();
	
	private static int getCityId(Connection conn)throws SQLException{
		String sql = "SELECT CITY_SEQ.NEXTVAL FROM DUAL";
		return runner.queryForInt(conn, sql);
	}
	
	private static int getBlockId(Connection conn)throws SQLException{
		String sql = "SELECT BLOCK_SEQ.NEXTVAL FROM DUAL";
		return runner.queryForInt(conn, sql);
	}

	private static void insertBlock(Connection conn, Block block, Set<String> grids) throws SQLException{
		
		
		
		String sql = "insert into BLOCK (block_id, city_id, block_name, geometry,plan_status,region_id) values (?,?,?,sdo_geometry(?,8307),0,(select region_id from city where city_id=?))";
		Clob clob = ConnectionUtil.createClob(conn);
		clob.setString(1, block.getGeometry().toString());
		
		runner.update(conn, sql, block.getBlockId(),block.getCityId(),block.getBlockName(),clob,block.getCityId());
		
		//Set<Integer> set = convertSet(grids);
		
		Clob clob2 = conn.createClob();
		clob2.setString(1, StringUtils.join(grids, ","));
		
		String updateSql = "INSERT INTO BLOCK_GRID_MAPPING select to_number(column_value),? from table(clob_to_table(?))";
		
		runner.update(conn, updateSql, block.getBlockId(), clob2);
	}
	
	private static void insertCity(Connection conn, City city, Set<String> grids) throws SQLException, ParseException, GeometryExceptionWithContext{
		
		String wkt = GridUtils.grids2Wkt(grids);
		
		String sql = "insert into CITY (city_id, city_name, admin_id, province_name, geometry, region_id, plan_status) values (?,?,(select admincode from cp_region_province where province=?),?,sdo_geometry(?,8307),(select region_id from cp_region_province where province=?),0)";
		Clob clob = ConnectionUtil.createClob(conn);
		clob.setString(1, wkt);
		
		runner.update(conn, sql, city.getCityId(),city.getCityName(),city.getProvinceName(),city.getProvinceName(),clob,city.getProvinceName());	
	}
	
	private static void insertDay2MonthConfig(Connection conn) throws SQLException{		
		String sql = "INSERT INTO DAY2MONTH_CONFIG"
				+ "  (CONF_ID, CITY_ID, TYPE, STATUS)"
				+ "  SELECT DAY2MONTH_CONFIG_SEQ.NEXTVAL, CITY_ID, 'POI', 0"
				+ "    FROM CITY"
				+ "   WHERE CITY_ID NOT IN (100000, 100001, 100002, 100003)";
		runner.update(conn, sql);	
	}
	
	public static JSONObject execute(JSONObject request) throws Exception{
		JSONObject response = new JSONObject();
		Connection conn = null;
		try {
			Map<String,City> cityMap = new HashMap<String,City>();//key:name,value:city object
			
			Map<String,Set<String>> cityGrids = new HashMap<String,Set<String>>();
			
			List<Block> blockList = new ArrayList<Block>();//
			
			Map<Integer,Set<String>> blockGrids = new HashMap<Integer,Set<String>>();	//key:blockId,value:grids
			

			conn = DBConnector.getInstance().getManConnection();
			JSONArray ja = request.getJSONArray("blocks");
			for (Object obj:ja) {

				JSONObject json = (JSONObject)obj;
				String province=json.getString("province");
				String city=json.getString("city");
				String name = json.getString("name");
				String county = json.getString("county");
				String area=json.getString("area");
				String job1=json.getString("job1");
				String job2=json.getString("job2");
				String code=json.getString("code");
				String workProperty=json.getString("workProperty");
				//String geometry =json.getString("geometry");
				
				String comName = (StringUtils.isEmpty(province)?"":province)
								+(StringUtils.isEmpty(city)?"":city)
								+(StringUtils.isEmpty(county)?"":county)
								+(StringUtils.isEmpty(job1)?"":job1)
								+(StringUtils.isEmpty(area)?"":area);
				
				Set<String> grids = new HashSet<String>();
				for(Object g:json.getJSONArray("grids")){
					grids.add((String)g);
				}

				
				//city
				int cityId = 0;
				if(cityMap.containsKey(city)){
					cityId = cityMap.get(city).getCityId();
					cityGrids.get(city).addAll(grids);
				}else{
					cityId = getCityId(conn);
					City cityObj = new City();
					cityObj.setCityId(cityId);
					cityObj.setCityName(city);
					cityObj.setProvinceName(province);
					cityMap.put(city, cityObj);
					Set<String> cg = new HashSet<String>();
					cg.addAll(grids);
					cityGrids.put(city, cg);
				}
				//block
				String wkt = GridUtils.grids2Wkt(grids);
				//String blockName = name+"-"+job+"-"+name+"-"+area;等具体原则
				int blockId = getBlockId(conn);
				Block block  = new Block();
				block.setBlockId(blockId);
				block.setBlockName(comName);
				block.setCityId(cityId);
				block.setGeometry(wkt);
				blockList.add(block);
				blockGrids.put(blockId, grids);
	 		}
			
			//insert city
			for(City city : cityMap.values()){
				System.out.println("insert city:"+city.getCityId());
				insertCity(conn, city,cityGrids.get(city.getCityName()));
			}
			//insert block
			for(Block b:blockList){
				System.out.println("insret block:"+b.getBlockId());
				insertBlock(conn,b,blockGrids.get(b.getBlockId()));
			}
			//insert day2monthConfig日落月poi开关表初始化
			insertDay2MonthConfig(conn);
			conn.commit();
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return response;
	}

}
