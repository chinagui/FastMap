package com.navinfo.dataservice.engine.man.statics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.BlockExpectStatInfo;
import com.navinfo.dataservice.api.statics.model.GridChangeStatInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.block.BlockOperation;
import com.navinfo.dataservice.engine.man.block.BlockService;
import com.navinfo.dataservice.engine.man.city.CityService;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;

@Service
public class StaticsService {
	
	private Logger log = LoggerRepos.getLogger(this.getClass());

	private StaticsService(){}
	private static class SingletonHolder{
		private static final StaticsService INSTANCE =new StaticsService();
	}
	public static StaticsService getInstance(){
		return SingletonHolder.INSTANCE;
	}

	
	/**
	 * 根据输入的范围和类型，查询范围内的所有grid的相应的统计信息，并返回grid列表和统计信息。
	 * @param wkt
	 * @param type 0POI, 1ROAD
	 * @param stage 0采集 1日编 2月编
	 * @return 
	 * @throws Exception 
	 * @throws JSONException 
	 */
	public List<GridChangeStatInfo> gridChangeStaticQuery(String wkt,int stage, int type, String date) throws JSONException, Exception{
		//通过wkt获取gridIdList
		Geometry geo=GeoTranslator.geojson2Jts(Geojson.wkt2Geojson(wkt));
		Set<String> grids= CompGeometryUtil.geo2GridsWithoutBreak(geo);
		
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");

		return api.getChangeStatByGrids(grids, type, stage, date);
	}
	
	public List<HashMap> blockExpectStatQuery(String wkt) throws JSONException, Exception{
		BlockService service = BlockService.getInstance();
		
		JSONObject json = new JSONObject();
		
		json.put("wkt",wkt);
		
		JSONArray status = new JSONArray();
		
		status.add(0);
		
		status.add(1);
		
		json.put("snapshot", 0);
		
		json.put("planningStatus", status);
		
		List<HashMap> data = service.listByWkt(json);
		
		Set<Integer> blocks = new HashSet<Integer>();
		
		for(HashMap map : data){
			int blockId = (int) map.get("blockId");
			
			blocks.add(blockId);
		}
		
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
		
		Map<Integer,Integer> statusMap = api.getExpectStatusByBlocks(blocks);
		
		for(HashMap map : data){
			map.put("expectStatus", statusMap.get(map.get("blockId")));
		}
		
		return data;
	}
	
	public HashMap blockExpectStatQuery(int blockId, int stage) throws JSONException, Exception{
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
	
		HashMap data = new HashMap();
		
		List<BlockExpectStatInfo> poiStat = api.getExpectStatByBlock(blockId, stage, 0);
		
		List<BlockExpectStatInfo> roadStat = api.getExpectStatByBlock(blockId, stage, 1);
		
		data.put("poi", poiStat);
		
		data.put("road", roadStat);
		
		return data;
	}
	
	public List<HashMap> cityExpectStatQuery(String wkt) throws JSONException, Exception{
		CityService service = CityService.getInstance();
		
		JSONObject json = new JSONObject();
		
		json.put("wkt",wkt);
		
		JSONArray status = new JSONArray();
		
		status.add(0);
		
		status.add(1);
		
		json.put("planningStatus", status);
		
		List<HashMap> data = service.queryListByWkt(json);
		
		Set<Integer> citys = new HashSet<Integer>();
		
		for(HashMap map : data){
			int blockId = (int) map.get("cityId");
			
			citys.add(blockId);
		}
		
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
		
		Map<Integer,Integer> statusMap = api.getExpectStatusByCitys(citys);
		
		for(HashMap map : data){
			map.put("expectStatus", statusMap.get(map.get("cityId")));
		}
		
		return data;
	}
	
	public JSONObject subtaskStatQuery(int subtaskId) throws JSONException, Exception{
		
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
		
		return api.getStatBySubtask(subtaskId);

	}
	
	public JSONObject queryTaskOverView() throws JSONException, Exception{
		
		Connection conn = null;
		try {
	
			conn = DBConnector.getInstance().getManConnection();
			JSONObject taskStaticsJson= new JSONObject();
			taskStaticsJson.put(0, 0);
			taskStaticsJson.put(1, 0);
			taskStaticsJson.put(2, 0);
			
			JSONObject cityStaticsJson= new JSONObject();
			cityStaticsJson.put(0, 0);
			cityStaticsJson.put(1, 0);
			cityStaticsJson.put(2, 0);
			
			JSONObject inforStaticsJson= new JSONObject();
			
			String selectTaskSql = "SELECT status,COUNT(1) taskCount FROM task GROUP BY status";
			String selectCitySql = "SELECT plan_status,COUNT(1) planCount FROM city GROUP BY plan_status";
			String selectInforSql = "SELECT 2 status,COUNT(1) inforCount FROM INFOR I WHERE I.TASK_ID IS NULL"
					+" UNION ALL"
					+" SELECT 1 status,COUNT(1) inforCount FROM TASK T WHERE T.TASK_TYPE=4 AND T.STATUS IN (1,2)"
					+" UNION ALL"
					+" SELECT 0 status,COUNT(1) inforCount FROM TASK T WHERE T.TASK_TYPE=4 AND T.STATUS=0";	
			PreparedStatement stmtTask = null;
			PreparedStatement stmtCity = null;
			PreparedStatement stmtInfor = null;
			try {
				stmtTask = conn.prepareStatement(selectTaskSql);
				stmtCity = conn.prepareStatement(selectCitySql);
				stmtInfor = conn.prepareStatement(selectInforSql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ResultSet rsTask = stmtTask.executeQuery();
			ResultSet rsCity = stmtCity.executeQuery();
			ResultSet rsInfor = stmtInfor.executeQuery();
		
			while (rsTask.next()) {
				taskStaticsJson.put(rsTask.getInt("status"),rsTask.getInt("taskCount"));
			}
			while (rsCity.next()) {
				cityStaticsJson.put(rsCity.getInt("plan_status"),rsTask.getInt("planCount"));
			}
			while (rsInfor.next()) {
				inforStaticsJson.put(rsInfor.getInt("status"),rsTask.getInt("inforCount"));
			}
			JSONObject staticsJson= new JSONObject();
			staticsJson.put("task", taskStaticsJson);
			staticsJson.put("city", cityStaticsJson);
			staticsJson.put("infor", inforStaticsJson);
			return staticsJson;
			
			} catch (Exception e) {
				DbUtils.rollbackAndCloseQuietly(conn);
				log.error(e.getMessage(), e);
			throw new ServiceException("查询失败:" + e.getMessage(), e);
		} finally {
				DbUtils.commitAndCloseQuietly(conn);
			}
	}
	
	
}
