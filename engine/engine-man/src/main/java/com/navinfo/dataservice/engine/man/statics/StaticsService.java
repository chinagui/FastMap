package com.navinfo.dataservice.engine.man.statics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.json.JSONException;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.BlockExpectStatInfo;
import com.navinfo.dataservice.api.statics.model.GridChangeStatInfo;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.block.BlockService;
import com.navinfo.dataservice.engine.man.city.CityService;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;

@Service
public class StaticsService {

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
	
	
}
