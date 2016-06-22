package com.navinfo.dataservice.engine.man.statics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.api.statics.model.GridStatInfo;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.vividsolutions.jts.geom.Geometry;

@Service
public class StaticsService {

	public StaticsService() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 根据输入的范围和类型，查询范围内的所有grid的相应的统计信息，并返回grid列表和统计信息。
	 * @param wkt
	 * @param type 0采集变迁图 1日编变迁图 2月编变迁图 3日出品变迁图 4月出品变迁图 5计划预期图
	 * @return 
	 * @throws Exception 
	 * @throws JSONException 
	 */
	public List<GridStatInfo> staticsGridQuery(String wkt,int type) throws JSONException, Exception{
		//通过wkt获取gridIdList
		Geometry geo=GeoTranslator.geojson2Jts(Geojson.wkt2Geojson(wkt));
		List<String> grids=(List<String>) CompGeometryUtil.geoToMeshesWithoutBreak(geo);
		
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
		if(type==0){return api.getCollectStatByGrids(grids);}
		if(type==1){return api.getDailyEditStatByGrids(grids);}
		if(type==2){return api.getMonthlyEditStatByGrids(grids);}
		//api.getCollectStatByGrids(grids)
		return null;
	}
}
