package com.navinfo.dataservice.commons.fileConvert;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ImportOracle {
	private static Logger log = LogManager.getLogger(ImportOracle.class);

	public ImportOracle() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * 主要用于导入数据字段名与oracle库表字段名一致的情况。
	 * @param conn
	 * @param oracleTableName
	 * @param dataList 数据
	 * @throws Exception
	 */
	public static void writeOracle(Connection conn,String oracleTableName,List<Map<String, Object>> dataList) throws Exception{
		try{
			log.info("start writeOracle,oracleTableName="+oracleTableName);
			ImportOracle importOracle =new ImportOracle();
			List<List<String>> result=importOracle.getTableColumns(conn,oracleTableName);
			List<Object[]> dataValues=new ArrayList<Object[]>();
			String columnStr="";
			String columnStrWenHao="";
			boolean flag=true;
			//按照oracleTableName表的字段，查询对应的value。需要保证dataList中map的key所存的字段名与数据库表名一致
			for (Map<String, Object> dataMap:dataList) {
				Object[] value=new Object[result.size()];
				for(int i=0;i<result.size();i++){
					List<String> columnName=result.get(i);
					String ObjectName=columnName.get(0);
					try{
						if(ObjectName.equals("GEOMETRY")){
							STRUCT struct = GeoTranslator.wkt2Struct(conn, (String) dataMap.get(ObjectName));
							value[i]=struct;
						}else{value[i]=dataMap.get(ObjectName);}
						if(flag){
							if(!columnStr.isEmpty()){columnStr=columnStr+",";columnStrWenHao=columnStrWenHao+",";}
							columnStr=columnStr+columnName.get(0);
							columnStrWenHao=columnStrWenHao+"?";}
					}
					catch(Exception e){
						log.warn("没有该列columnName="+ObjectName,e);
						value[i]=null;
					}
				}
				flag=false;
				dataValues.add(value);
				//break;
			}
				
			String insertSql="insert into "+oracleTableName+" ("+columnStr+")"
					+ " values ("+columnStrWenHao+")";
			QueryRunner run=new QueryRunner();
			Object[][] valueList=new Object[dataValues.size()][result.size()];
			for(int i=0;i<dataValues.size();i++){
				valueList[i]=dataValues.get(i);
			}
			String dropSql="truncate table "+oracleTableName;
			run.update(conn, dropSql);
			run.batch(conn, insertSql,valueList);
			log.info("end writeOracle,oracleTableName="+oracleTableName);
		}catch (Exception e){
			log.error("fail writeOracle 入库失败",e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}
	}
	/**
	 * 主要用于导入数据字段名与oracle库表字段名不一致的情况。所有表名均采用大写
	 * @param conn
	 * @param oracleTableName
	 * @param dataList 数据
	 * @param tableColumns oracle表字段名列表
	 * @param dataColumns 与oracle表字段名对应的dataList数据的字段名
	 * @throws Exception
	 */
	public static void writeOracle(Connection conn,String oracleTableName,List<Map<String, Object>> dataList,List<String> tableColumns,List<String>dataColumns) throws Exception{
		try{
			log.info("start writeOracle,oracleTableName="+oracleTableName);
			List<Object[]> dataValues=new ArrayList<Object[]>();
			String columnStr="";
			String columnStrWenHao="";
			boolean flag=true;
			//按照oracleTableName表的字段，查询对应的value。需要保证dataList中map的key所存的字段名与数据库表名一致
			for (Map<String, Object> dataMap:dataList) {
				Object[] value=new Object[tableColumns.size()];
				int i=0;
				for(String ObjectName:tableColumns){
					try{
						if(dataColumns.get(i).equals("GEOMETRY")){
							STRUCT struct = GeoTranslator.wkt2Struct(conn, (String) dataMap.get(dataColumns.get(i)));
							value[i]=struct;
						}else{value[i]=dataMap.get(dataColumns.get(i));}
						if(flag){
							if(!columnStr.isEmpty()){columnStr=columnStr+",";columnStrWenHao=columnStrWenHao+",";}
							columnStr=columnStr+ObjectName;
							columnStrWenHao=columnStrWenHao+"?";}
					}
					catch(Exception e){
						log.warn("没有该列columnName="+ObjectName,e);
						value[i]=null;
					}
					i++;
				}
				flag=false;
				dataValues.add(value);
				break;
			}
				
			String insertSql="insert into "+oracleTableName+" ("+columnStr+")"
					+ " values ("+columnStrWenHao+")";
			QueryRunner run=new QueryRunner();
			Object[][] valueList=new Object[dataValues.size()][tableColumns.size()];
			for(int i=0;i<dataValues.size();i++){
				valueList[i]=dataValues.get(i);
			}
			String dropSql="truncate table "+oracleTableName;
			run.update(conn, dropSql);
			run.batch(conn, insertSql,valueList);
			log.info("end writeOracle,oracleTableName="+oracleTableName);
		}catch (Exception e){
			log.error("fail writeOracle 入库失败",e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}
	}

	private List<List<String>> getTableColumns(Connection conn,String tableName) throws SQLException{
		log.info("start 获取表字段列表,oracleTableName="+tableName);
		//获取表字段
		String getTableColumsSql="SELECT COLUMN_NAME,DATA_TYPE FROM USER_TAB_COLUMNS WHERE TABLE_NAME = '"+tableName.toUpperCase()+"'";
		ResultSetHandler<List<List<String>>> rsHandler=new ResultSetHandler<List<List<String>>>() {

			@Override
			public List<List<String>> handle(ResultSet rs) throws SQLException {
				List<List<String>> result=new ArrayList<List<String>>();
				while(rs.next()){
					List<String> tmp=new ArrayList<String>();
					tmp.add(rs.getString("COLUMN_NAME"));
					tmp.add(rs.getString("DATA_TYPE"));
					result.add(tmp);
				}
				return result;
			}
			
		};
		QueryRunner run=new QueryRunner();
		List<List<String>> result=run.query(conn, getTableColumsSql,rsHandler);
		log.info("end 获取表字段列表,oracleTableName="+tableName);
		return result;
	}
	
	/**
	 * @Title: writeOracle
	 * @Description: 想数据库存入 city 或block 信息
	 * @param conn
	 * @param type
	 * @param ja
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年2月24日 下午4:35:17 
	 */
	public static void writeOracle(Connection conn,int type,JSONArray ja) throws Exception{
		try{
			String oracleTableName ="city";
			
			log.info("start writeOracle,oracleTableName="+oracleTableName);
			ImportOracle importOracle =new ImportOracle();
			//List<List<String>> result=importOracle.getTableColumns(conn,oracleTableName);
			List<Object[]> dataValues=new ArrayList<Object[]>();
			String columnStr="";
			String columnStrWenHao="";
			if(type == 1){//读取的是block json
				oracleTableName = "block";
				columnStr=" BLOCK_ID,CITY_ID,BLOCK_NAME,GEOMETRY,REGION_ID,WORK_PROPERTY,PLAN_STATUS ";
				//System.out.println("columnStr: "+columnStr);
				Iterator<Object> it = ja.iterator();
				Map<String,Block> blockGridMap = new HashMap<>();
 		        while (it.hasNext()) {
		            JSONObject ob = (JSONObject) it.next();
		            String codeVal = ob.getString("code");
		            String gridIdVal = ob.getString("gridId");
		            String cityVal = ob.getString("city");
		            String countyVal = ob.getString("county");
		            String workPropertyVal = ob.getString("workProperty");
		            if(blockGridMap.containsKey(codeVal)){//block 已存在
		            	Block blockOld = blockGridMap.get(codeVal);
		            	Set<String> gridsOldVal = blockOld.getGridIds();
		            	gridsOldVal.add(gridIdVal);
		            	blockOld.setGridIds(gridsOldVal);
		            }else{
		            	Set<String> gridSet = new HashSet<String>();
		            	gridSet.add(gridIdVal);
		            	Block blockNew = new Block();
		            	blockNew.setCity(cityVal);
		            	blockNew.setBlockCode(codeVal);
		            	blockNew.setWorkProperty(workPropertyVal);
		            	blockNew.setCounty(countyVal);
		            	blockNew.setGridIds(gridSet);
		            	blockGridMap.put(codeVal, blockNew);
		            }
		            
		        }
 		        if(blockGridMap != null && blockGridMap.size() >0){
 		        	Map<String,String> blockGridMapNew = new HashMap<String,String>();
 		        	QueryRunner run=new QueryRunner();
 		        	for(String key : blockGridMap.keySet()){
 		        		Block block = blockGridMap.get(key);
 		        		String city = block.getCity();
 		        		Set<String> gridsNew = block.getGridIds();
 		        		String wkt = GridUtils.grids2Wkt(gridsNew);
 		        		STRUCT struct = GeoTranslator.wkt2Struct(conn,wkt);
 		        		
 		        		
 		        		String gridStr = "";
			            int i =0;
			    		for(String grid : gridsNew){
			    			i=i+1;
			    			gridStr = gridStr+grid;
			    			if(i < gridsNew.size()){
			    				gridStr= gridStr+ ",";
			    			}
			    		}
			    		Clob gridsClob = ConnectionUtil.createClob(conn);		       		
 		        		gridsClob.setString(1, gridStr);
			    		int regionId = getRegionIdByGrids(conn,gridsClob);
			    		if(regionId != 0){//存在 regionId
			    			int city_id = getCityIdByCityName(conn, city);
	 		        		if(city_id != 0){
	 		        			
			    			if(block.getCounty() != null  && StringUtils.isNotEmpty(block.getCounty())){
				    			blockGridMapNew.put(block.getCounty(), gridStr);
				    		}
	 		        		
	 		        			columnStrWenHao="block_seq.nextval"+" ,("+city_id+") ,'"+block.getCounty()+"' ,?,"+regionId+",'"+block.getWorkProperty()+"' ,0";
		 			         //   System.out.println("columnStrWenHao: "+columnStrWenHao);
		 			           String insertCitySql="insert into "+oracleTableName+" ("+columnStr+")"
		 								+ " values ("+columnStrWenHao+")";
		 			         //   System.out.println("insertCitySql : "+insertCitySql);
		 			            run.update(conn, insertCitySql, struct);
	 		        		}
	 		        		
			    		}
			    		
 		        	}
 		        	//存储 grid 表
 		        	for(String key : blockGridMapNew.keySet()){
 			        	
 			        	String updateGridSql =" update grid set block_id =(select block_id from block where block_name ='"+key+"' and rownum=1) where grid_id in("+blockGridMapNew.get(key)+") " ;
 			        //	System.out.println("updateGridSql: "+updateGridSql);
 			        	run.execute(conn, updateGridSql);	
 			        }
 		        	
 		        }
			}else{//读取的是city json
				columnStr=" CITY_ID,CITY_NAME,PROVINCE_NAME,GEOMETRY,REGION_ID,PLAN_STATUS ";
				//System.out.println("columnStr: "+columnStr);
				Iterator<Object> it = ja.iterator();
				Map<String,String> cityGridMap = new HashMap<String,String>();
				 QueryRunner run=new QueryRunner();
		        while (it.hasNext()) {
		            JSONObject ob = (JSONObject) it.next();
		            String provinceVal = ob.getString("province");
		            String cityVal = ob.getString("city");
		           // String codeVal = ob.getString("code");
		            String geometryWkt = ob.getString("geometry");
		            STRUCT struct = GeoTranslator.wkt2Struct(conn,geometryWkt);
		            
		            String meshId = ob.getString("meshId");
		            Set<String> grids = CompGridUtil.mesh2Grid(meshId);
		            String gridStr = "";
			            int i =0;
			    		for(String grid : grids){
			    			i=i+1;
			    			gridStr = gridStr+grid;
			    			if(i < grids.size()){
			    				gridStr= gridStr+ ",";
			    			}
			    		}
		    		Clob gridsClob = ConnectionUtil.createClob(conn);		       		
	        		gridsClob.setString(1, gridStr);
			    	int regionId = 	getRegionIdByGrids(conn,gridsClob);
			    //	System.out.println(regionId);
			    	if(regionId != 0){//存在 regionId
			    		cityGridMap.put(cityVal, gridStr);
			    		
			            columnStrWenHao="city_seq.nextval,'"+cityVal+"' ,'"+provinceVal+"' ,? ,"+regionId+", 0 ";
			        //    System.out.println("columnStrWenHao: "+columnStrWenHao);
			            String insertCitySql="insert into "+oracleTableName+" ("+columnStr+")"
								+ " values ("+columnStrWenHao+")";
			         //   System.out.println("insertCitySql : "+insertCitySql);
			           
			            run.update(conn, insertCitySql, struct);
			    	}
			    	
		            
		        }
		        for(String key : cityGridMap.keySet()){
		        	String updateGridSql =" update grid set city_id =(select city_id from city where city_name ='"+key+"' and rownum=1) where grid_id in("+cityGridMap.get(key)+") " ;
		        	//System.out.println("updateGridSql: "+updateGridSql);
		        	run.execute(conn, updateGridSql);	
		        }
			}
		}catch (Exception e){
			log.error("fail writeOracle 入库失败",e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}
	}
	
	/**
	 * @Title: getRegionIdByGrids
	 * @Description: 通过grid 获取regionId
	 * @param conn
	 * @param gridsClob
	 * @return
	 * @throws SQLException  int
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年2月24日 下午6:52:59 
	 */
	public static int getRegionIdByGrids(Connection conn,Clob gridsClob) throws SQLException{
		 QueryRunner run=new QueryRunner();
		 String sql = "select distinct t.region_id from grid t where t.grid_id in (select to_number(column_value) from table(clob_to_table(?))) and rownum=1";
		 
		 ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {
				public Integer handle(ResultSet rs) throws SQLException {
						Integer regionId = 0;
					if (rs.next()) {
						if(rs.getObject("region_id") != null ){
							regionId = rs.getInt("region_id");			
						}					
						return regionId;
					}
					return 0;
				}	
			};
		 
		 return run.query(conn,sql, rsHandler,gridsClob);
	}
	
	/**
	 * @Title: getCityIdByCityName
	 * @Description: 通过cityName 获取city_id
	 * @param conn
	 * @param cityName
	 * @return
	 * @throws SQLException  int
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年2月24日 下午6:53:25 
	 */
	public static int getCityIdByCityName(Connection conn,String cityName) throws SQLException{
		 QueryRunner run=new QueryRunner();
		 String sql = "select city_id from city where city_name = '"+cityName+"' and rownum=1  ";
		 ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {
				public Integer handle(ResultSet rs) throws SQLException {
						Integer city_id = 0;
					if (rs.next()) {
						if(rs.getObject("city_id") != null ){
							city_id = rs.getInt("city_id");			
						}					
						return city_id;
					}
					return 0;
				}	
			};
		 
		 return run.query(conn,sql, rsHandler);
	}
	
}
