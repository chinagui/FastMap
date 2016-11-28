package com.navinfo.dataservice.engine.editplus.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/** 
 * @ClassName: IxPoiConvertor
 * @author xiaoxiaowen4127
 * @date 2016年11月10日
 * @Description: MultiSrcPoiConvertor.java
 */
public class MultiSrcPoiConvertor {

	/**
	 * 参考接口说明
	 */
	public JSONObject toJson(IxPoiObj poi) throws ObjConvertException {
		JSONObject jo = new JSONObject();
		IxPoi ixPoi = (IxPoi)poi.getMainrow();
		//外业采集ID
		jo.put("fid", ixPoi.getPoiNum());
		//显示样式
		jo.put("display_style", "");
		//显示用的主名称
		jo.put("display_text", "");
		//采集用的主名称
		IxPoiName poiName = poi.getNameByLct("CHI", 1, 2);
		if(poiName==null)poiName=poi.getNameByLct("CHT", 1, 2);
		if(poiName!=null){
			jo.put("name", poiName.getName());
		}else{
			jo.put("name", "");
		}
		//永久ID
		jo.put("pid",ixPoi.getPid());
		//图幅号
		jo.put("meshid",ixPoi.getMeshId());
		//分类代码
		if(ixPoi.getKindCode() != null){
			jo.put("kindCode", ixPoi.getKindCode());
		}else{
			jo.put("kindCode", "");
		}
		//{唯一}引导
		if(ixPoi != null){
			if(ixPoi.getLinkPid()==0 
					&&ixPoi.getXGuide()==0 
					&&ixPoi.getYGuide()==0){
				jo.put("guide", JSONNull.getInstance());	
			}else{
				JSONObject guide = new JSONObject();
				guide.put("linkPid", ixPoi.getLinkPid());
				guide.put("longitude", ixPoi.getXGuide());
				guide.put("latitude", ixPoi.getYGuide());
				jo.put("guide", guide.toString());
			}
		}
		//采集、显示用的主地址
		if(poi.getFullNameByLg("CHI", 1) != null){
			jo.put("address", poi.getFullNameByLg("CHI", 1).getFullname());
		}else if(poi.getFullNameByLg("CHT", 1) != null){
			jo.put("address", poi.getFullNameByLg("CHT", 1).getFullname());
		}else{
			jo.put("address", "");
		}
		//邮政编码
		if(ixPoi.getPostCode() != null){
			jo.put("postCode", ixPoi.getPostCode());
		}else{
			jo.put("postCode", "");
		}
		//POI等级
		if(ixPoi.getLevel() != null){
			jo.put("level", ixPoi.getLevel());
		}else{
			jo.put("level", "");
		}
		//是否24小时开放
		if(ixPoi.getOpen24h()==0){
			jo.put("open24H", 2);
		}else {
			jo.put("open24H", ixPoi.getOpen24h());
		}
		//父POI的Fid
		jo.put("parentFid", poi.getParentFid());
		//[集合]父子关系,子列表；该POI作为父的子要素
		jo.put("relateChildren", poi.getChildrens());
		//[集合]联系方式
		jo.put("contacts", poi.getContacts());
		//{唯一}餐饮
		if(poi.getFoodTypes() != null){
			jo.put("foodtypes", poi.getFoodTypes());
		}else{
			jo.put("foodtypes", JSONNull.getInstance());
		}
		//{唯一}停车场扩展信息
		if(poi.getParkings() != null){
			jo.put("parkings", poi.getParkings());
		}else{
			jo.put("parkings", JSONNull.getInstance());
		}
		//{唯一}酒店
		if(poi.getHotel() != null){
			jo.put("hotel", poi.getHotel());
		}else{
			jo.put("hotel", JSONNull.getInstance());
		}
		//运动场馆
		if(ixPoi.getSportsVenue() != null){
			jo.put("sportsVenues", ixPoi.getSportsVenue());
		}else{
			jo.put("sportsVenues", "");
		}
		//{唯一}充电站
		if(poi.getChargingStation() != null){
			jo.put("chargingStation", poi.getChargingStation());
		}else{
			jo.put("chargingStation", JSONNull.getInstance());
		}
		//[集合]充电桩
		jo.put("chargingPole", poi.getChargingPole());
		//{唯一}加油站
		if(poi.getGasStation() != null){
			jo.put("gasStation", poi.getGasStation());
			//jo.put("gasStation", JSONNull.getInstance());
		}else{
			jo.put("gasStation", JSONNull.getInstance());
		}
		//{唯一}室内扩展信息
		JSONObject indoor = new JSONObject();
		//种别
		if(ixPoi.getIndoor()==1){
			indoor.put("type", 3);
		}else{
			indoor.put("type", ixPoi.getIndoor());
		}
		//楼层
		if(poi.getFloorByLangCode("CHI") != null){
			indoor.put("floor", StringUtils.trimToEmpty(poi.getFloorByLangCode("CHI").getFloor()));
		}else if(poi.getFloorByLangCode("CHT") != null){
			indoor.put("floor", StringUtils.trimToEmpty(poi.getFloorByLangCode("CHT").getFloor()));
		}else{
			indoor.put("floor", "");
		}
		jo.put("indoor", indoor.toString());
		//[集合]附件信息
		List<Map<String,Object>> attachments = new ArrayList<Map<String,Object>>();
		jo.put("attachments", attachments);
		//连锁品牌
		if(ixPoi.getChain() != null){
			jo.put("chain", ixPoi.getChain());
		}else{
			jo.put("chain", "");
		}
		//后期待修改字段
		jo.put("rawFields", "");
		//状态
		if(poi.getLifeCycle()==1){
			jo.put("t_lifecycle", 3);
		}else if(poi.getLifeCycle()==2){
			jo.put("t_lifecycle", 1);
		}else if(poi.getLifeCycle()==3){
			jo.put("t_lifecycle", 2);
		}else{
			jo.put("t_lifecycle", poi.getLifeCycle());
		}
		//当前阶段作业状态
		//jo.put("t_status", 0);
		//[集合]编辑履历
		List<Map<String,Object>> edits = new ArrayList<Map<String,Object>>();
		jo.put("edits", edits);
		//显示坐标
		jo.put("geometry", GeoTranslator.jts2Wkt(ixPoi.getGeometry()));
		//VIP标识
		if(ixPoi.getVipFlag() != null){
			jo.put("vipFlag", ixPoi.getVipFlag());
		}else{
			jo.put("vipFlag", "");
		}
		//记录android端最后一次操作的时间
		jo.put("t_operateDate", "");
		//卡车标识
		jo.put("truck", ixPoi.getTruckFlag());
		//与GDB库同步状态
		jo.put("t_sync", 1);
		//同一POI的
		jo.put("sameFid", "");

		return jo;
	}

	/**
	 * 参考接口说明
	 */
	public BasicObj fromJson(JSONObject jo, String objType,boolean isSetPid) throws ObjConvertException {
		// 暂不实现
		return null;
	}


}
