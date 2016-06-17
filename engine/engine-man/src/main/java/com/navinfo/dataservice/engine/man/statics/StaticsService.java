package com.navinfo.dataservice.engine.man.statics;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.api.statics.iface.StaticsApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

public class StaticsService {

	public StaticsService() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 根据输入的范围和类型，查询范围内的所有grid的相应的统计信息，并返回grid列表和统计信息。
	 * @param wkt
	 * @param type
	 */
	public void staticsGridQuery(String wkt,int type){
		//通过wkt获取gridIdList
		List<Integer> gridIds=new ArrayList<Integer>();
		StaticsApi api=(StaticsApi) ApplicationContextUtil.getBean("staticsApi");
		//0采集变迁图 1日编变迁图 2月编变迁图 3日出品变迁图 4月出品变迁图 5计划预期图

		//api.getCollectStatByGrids(grids)
	}
}
