package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Region;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.editman.PoiEditStatus;
import com.navinfo.dataservice.engine.man.region.RegionService;
import com.navinfo.dataservice.expcore.snapshot.GdbDataExporter;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;

public class AutoAddTaskId2PoiScriptsInterface {

	private static Map<Integer, Map<Integer, Set<Integer>>> getProvinceMeshList(String type) throws SQLException {
	
		Connection conn = DBConnector.getInstance().getManConnection();

		QueryRunner runner = new QueryRunner();
		String sql = null;
		if("month".equals(type)){
			sql = "select a.region_id,a.monthly_db_id as db_id,c.admincode,c.mesh from region a, cp_region_province b, cp_meshlist@metadb_link c where a.region_id=b.region_id and b.admincode=c.admincode and a.monthly_db_id is not null order by region_id,admincode";
		}else if("day".equals(type)){
			sql = "select a.region_id,a.daily_db_id as db_id,c.admincode,c.mesh from region a, cp_region_province b, cp_meshlist@metadb_link c where a.region_id=b.region_id and b.admincode=c.admincode and a.daily_db_id is not null order by region_id,admincode";
		}
		ResultSetHandler<Map<Integer, Map<Integer, Set<Integer>>>> rsh = new ResultSetHandler<Map<Integer, Map<Integer, Set<Integer>>>>() {

			@Override
			public Map<Integer, Map<Integer, Set<Integer>>> handle(ResultSet rs)
					throws SQLException {
				if (rs != null) {
					Map<Integer, Map<Integer, Set<Integer>>> data = new HashMap<Integer, Map<Integer, Set<Integer>>>();

					while (rs.next()) {

						int dbId = rs.getInt("db_id");

						int adminCode = rs.getInt("admincode");

						int mesh = rs.getInt("mesh");

						Map<Integer, Set<Integer>> map = null;
						if (data.containsKey(dbId)) {
							map = data.get(dbId);
						} else {
							map = new HashMap<Integer, Set<Integer>>();
						}

						Set<Integer> meshes = null;
						if (map.containsKey(adminCode)) {
							meshes = map.get(adminCode);
						} else {
							meshes = new HashSet<Integer>();
						}

						meshes.add(mesh);

						map.put(adminCode, meshes);

						data.put(dbId, map);

					}
					return data;
				}
				return null;
			}
		};

		return runner.query(conn, sql, rsh);

	}

/*	
	Geometry geo = null;
	geo = (Geometry) poiObj.getMainrow().getAttrByColName("GEOMETRY");
	//通过 geo 获取 grid 
	Coordinate[] coordinate = geo.getCoordinates();
	CompGridUtil gridUtil = new CompGridUtil();
	String grid = gridUtil.point2Grids(coordinate[0].x, coordinate[0].y)[0];
	//调用 manapi 获取 对应的 快线任务id,及中线任务id
	Integer quickTaskId = 0;
	Integer centreTaskId = 0;
	ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
	Map<String,Integer> taskMap = manApi.queryTaskIdsByGrid(grid);
	if(taskMap != null && taskMap.containsKey("quickTaskId") && taskMap.containsKey("centreTaskId")){
		quickTaskId = taskMap.get("quickTaskId");
		centreTaskId = taskMap.get("centreTaskId");
	}
	//维护 poi_edit_status 表中 快线及中线任务标识
	PoiEditStatus.updateTaskIdByPid(conn, poiPid, quickTaskId, centreTaskId);
	*/
	
	/**
	 * @Title: queryPidsInPoiEditStatus
	 * @Description: 查询poi_edit_status 表中 快线任务存在但中线任务不存在的数据
	 * @param conn
	 * @return
	 * @throws Exception  List<Long>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年2月10日 下午3:19:44 
	 */
	public static List<Map<String,Object>> queryPidsInPoiEditStatus(Connection conn) throws Exception{
		StringBuilder sb = new StringBuilder();
		//sb.append("SELECT T.PID FROM POI_EDIT_STATUS T WHERE T.QUICK_TASK_ID != 0 and T.CENTRE_TASK_ID = 0 ");
		sb.append("SELECT i.pid,i.geometry  FROM POI_EDIT_STATUS T, ix_poi i  WHERE T.QUICK_TASK_ID != 0 and T.CENTRE_TASK_ID = 0 and T.PID = i.pid ");
	
		ResultSetHandler<List<Map<String,Object>>> rsHandler = new ResultSetHandler<List<Map<String,Object>>>() {
			public List<Map<String,Object>> handle(ResultSet rs) throws SQLException {
				List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
				while (rs.next()) {
					Map<String,Object> map = new HashMap<String,Object>();
					map.put("pid", rs.getLong("pid"));
					STRUCT struct = (STRUCT) rs.getObject("geometry");
					Geometry geometry = null;
					try {
						 geometry = GeoTranslator.struct2Jts(struct);
					} catch (Exception e) {
						System.out.println("查询结果获取Geometry失败");
						//throw new Exception("查询结果获取Geometry失败");
					}
					
					map.put("geo", geometry);
					result.add(map);
				}
				return result;
			}
	
		};
		
		List<Map<String,Object>> poiList = new ArrayList<Map<String,Object>>();
			try {
				poiList = new QueryRunner().query(conn, sb.toString(), rsHandler);
			} catch (SQLException e) {
				throw new Exception("查询poi_edit_status 表数据失败");
			}
		return poiList;
	}
	
	/**
	 * @Title: updateTaskIdByPid
	 * @Description: 更新poi_edit_status 表中的快线,中线任务标识
	 * @param conn
	 * @param pid
	 * @param quickTaskId
	 * @param centreTaskId
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年2月9日 下午7:02:25 
	 */
	public static void updateTaskIdByPid(Connection conn, Long pid ,Integer quickTaskId,Integer centreTaskId) throws Exception {
		try{
			
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE POI_EDIT_STATUS T SET quickTaskId="+quickTaskId);
			sb.append(",centreTaskId="+centreTaskId);
			
				sb.append(" WHERE T.PID = "+pid);

			
				new QueryRunner().update(conn, sb.toString());
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new Exception("采集成果自动批任务标识失败");
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			//初始化context
			JobScriptsInterface.initContext();
			//调用datahubApi
			DatahubApi datahub = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");
			
			//获取所有日库的 dbid
			List<Region> list = RegionService.getInstance().list();
			for(Region region : list){
				int region_day_dbId = region.getDailyDbId();
				//根据 dbid 获取 conn
				DbInfo dbinfo = datahub.getDbById(region_day_dbId);

				DbConnectConfig connConfig = DbConnectConfig
						.createConnectConfig(dbinfo.getConnectParam());

				DataSource datasource = MultiDataSourceFactory.getInstance()
						.getDataSource(connConfig);

				Connection conn = datasource.getConnection();
				//查询 poi_edit_status 表中符合条件的 数据
				List<Map<String, Object>> poiList = queryPidsInPoiEditStatus(conn);
				for(Map<String, Object> map : poiList){
					Long pid = (Long) map.get("pid");
					Geometry geo = (Geometry) map.get("geo");
					
					//通过 geo 获取 grid 
					Coordinate[] coordinate = geo.getCoordinates();
					CompGridUtil gridUtil = new CompGridUtil();
					String grid = gridUtil.point2Grids(coordinate[0].x, coordinate[0].y)[0];
					//调用 manapi 获取 对应的 快线任务id,及中线任务id
					Integer quickTaskId = 0;
					Integer centreTaskId = 0;
					ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
					Map<String,Integer> taskMap = manApi.queryTaskIdsByGrid(grid);
					if(taskMap != null && taskMap.containsKey("quickTaskId") && taskMap.containsKey("centreTaskId")){
						quickTaskId = taskMap.get("quickTaskId");
						centreTaskId = taskMap.get("centreTaskId");
					}
					//维护 poi_edit_status 表中 快线及中线任务标识
					PoiEditStatus.updateTaskIdByPid(conn, pid, quickTaskId, centreTaskId);
					
				}
			}
			
			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}
	}
}