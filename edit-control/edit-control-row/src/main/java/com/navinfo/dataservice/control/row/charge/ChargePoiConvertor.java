package com.navinfo.dataservice.control.row.charge;

import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName ChargePoiConvertor
 * @author Han Shaoming
 * @date 2017年7月17日 下午8:25:21
 * @Description TODO
 */
public class ChargePoiConvertor {

	/**
	 * 初始化
	 * @author Han Shaoming
	 * @param poi
	 * @return
	 */
	public JSONObject initPoi(IxPoiObj poiObj){
		//处理通用字段
		JSONObject chargePoi = toJson(poiObj);
		return null;
		
	}
	/**
	 * 增量
	 * @author Han Shaoming
	 * @param poi
	 * @return
	 */
	public JSONObject addPoi(IxPoiObj poiObj){
		return null;
		
	}
	
	/**
	 * 通用字段处理
	 * @author Han Shaoming
	 * @param poiObj
	 * @return
	 */
	private JSONObject toJson(IxPoiObj poiObj){
		JSONObject chargePoi = new JSONObject();
		IxPoi ixPoi = (IxPoi)poiObj.getMainrow();
		long pid = ixPoi.getPid();
		//当POI的pid为0时，此站或桩不转出（外业作业中的新增POI未经过行编）
		if(pid == 0){return null;}
		chargePoi.put("pid", pid);
		String kindCode = ixPoi.getKindCode();
		if(kindCode == null || !"230218".equals(kindCode)){return null;}
		//显示坐标
		Geometry geometry = ixPoi.getGeometry();
		double longitude = geometry.getCoordinate().x;
		double latitude = geometry.getCoordinate().y;
		JSONObject location = new JSONObject();
		location.put("longitude", longitude);
		location.put("latitude", latitude);
		chargePoi.put("location", location);
		//引导坐标
		JSONObject guide = new JSONObject();
		guide.put("longitude", ixPoi.getXGuide());
		guide.put("latitude", ixPoi.getYGuide());
		chargePoi.put("guide", guide.toString());
		//名称
		IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
		String name = ixPoiName.getName();
		
		
		
		
		
		
		return chargePoi;
		
	}
}
