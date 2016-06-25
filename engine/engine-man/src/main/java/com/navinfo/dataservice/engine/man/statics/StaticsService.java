package com.navinfo.dataservice.engine.man.statics;

import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.GridChangeStatInfo;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
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
		Set<String> grids= CompGeometryUtil.geoToMeshesWithoutBreak(geo);
		
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");

		return api.getChangeStatByGrids(grids, type, stage, date);
	}
}
