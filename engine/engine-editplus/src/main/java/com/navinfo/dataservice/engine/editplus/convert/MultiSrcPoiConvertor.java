package com.navinfo.dataservice.engine.editplus.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
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
		jo.put("parentFid", StringUtils.trimToEmpty(poi.getParentFid()));
		//[集合]父子关系,子列表；该POI作为父的子要素
		jo.put("relateChildren", this.getChildrens(poi));
		//[集合]联系方式
		jo.put("contacts", this.getContacts(poi));
		//{唯一}餐饮
		if(this.getFoodTypes(poi) != null){
			jo.put("foodtypes", this.getFoodTypes(poi));
		}else{
			jo.put("foodtypes", JSONNull.getInstance());
		}
		//{唯一}停车场扩展信息
		if(this.getParkings(poi) != null){
			jo.put("parkings", this.getParkings(poi));
		}else{
			jo.put("parkings", JSONNull.getInstance());
		}
		//{唯一}酒店
		if(this.getHotel(poi) != null){
			jo.put("hotel", this.getHotel(poi));
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
		if(this.getChargingStation(poi) != null){
			jo.put("chargingStation", this.getChargingStation(poi));
		}else{
			jo.put("chargingStation", JSONNull.getInstance());
		}
		//[集合]充电桩
		jo.put("chargingPole", this.getChargingPole(poi));
		//{唯一}加油站
		if(this.getGasStation(poi) != null){
			jo.put("gasStation", this.getGasStation(poi));
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
		jo.put("t_status", 0);
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
		//行政区划号
		jo.put("adminId", poi.getAdminId());
		
		return jo;
	}

	/**
	 * 参考接口说明
	 */
	public BasicObj fromJson(JSONObject jo, String objType,boolean isSetPid) throws ObjConvertException {
		// 暂不实现
		return null;
	}
	
	
	/**
	 * 查询所有的联系方式
	 * @author Han Shaoming
	 * @return
	 */
	public List<Map<String,Object>> getContacts(IxPoiObj poi){
		List<BasicRow> rows = poi.getRowsByName("IX_POI_CONTACT");
		List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
		if(rows!=null && rows.size()>0){
			for(BasicRow row:rows){
				IxPoiContact contact = (IxPoiContact) row;
				Map<String,Object> msg = new HashMap<String, Object>();
				msg.put("number", StringUtils.trimToEmpty(contact.getContact()));
				msg.put("type", contact.getContactType());
				int cd = contact.getContactDepart();
				List<String> linkman = new ArrayList<String>();
				if((cd&1) ==1){
					linkman.add("总机");
				}else if((cd&2) ==1){
					linkman.add("客服");
				}else if((cd&4) ==1){
					linkman.add("预订");
				}else if((cd&8) ==1){
					linkman.add("销售");
				}else if((cd&16) ==1){
					linkman.add("维修");
				}else if((cd&32) ==1){
					linkman.add("其他");
				}else if(cd ==0){
					linkman.add("");
				}
				msg.put("linkman", StringUtils.join(linkman.toArray(), "-"));
				msg.put("priority", contact.getPriority());
				msg.put("rowId", contact.getRowId());
				msgs.add(msg);
			}
		}
		return msgs;
	}
	
	/**
	 * 查询餐饮
	 * @author Han Shaoming
	 * @return
	 */
	public Map<String,Object> getFoodTypes(IxPoiObj poi){
		List<BasicRow> rows = poi.getRowsByName("IX_POI_RESTAURANT");
		if(rows!=null && rows.size()>0){
			Map<String,Object> msg = new HashMap<String, Object>();
			for(BasicRow row:rows){
				IxPoiRestaurant foodType = (IxPoiRestaurant) row;
				msg.put("foodtype", StringUtils.trimToEmpty(foodType.getFoodType()));
				msg.put("creditCards", StringUtils.trimToEmpty(foodType.getCreditCard()));
				msg.put("parking", foodType.getParking());
				msg.put("openHour", StringUtils.trimToEmpty(foodType.getOpenHour()));
				msg.put("avgCost", foodType.getAvgCost());
				msg.put("rowId", foodType.getRowId());
			}
			return msg;
		}
		return null;
	}
	
	/**
	 * 查询停车场扩展信息
	 * @author Han Shaoming
	 * @return
	 */
	public Map<String,Object> getParkings(IxPoiObj poi){
		List<BasicRow> rows = poi.getRowsByName("IX_POI_PARKING");
		if(rows!=null && rows.size()>0){
			Map<String,Object> msg = new HashMap<String, Object>();
			for(BasicRow row:rows){
				IxPoiParking parking = (IxPoiParking) row;
				msg.put("tollStd", StringUtils.trimToEmpty(parking.getTollStd()));
				msg.put("tollDes", StringUtils.trimToEmpty(parking.getTollDes()));
				msg.put("tollWay", StringUtils.trimToEmpty(parking.getTollWay()));
				msg.put("openTime", StringUtils.trimToEmpty(parking.getOpenTiime()));
				msg.put("totalNum", parking.getTotalNum());
				msg.put("payment", StringUtils.trimToEmpty(parking.getPayment()));
				msg.put("remark", StringUtils.trimToEmpty(parking.getRemark()));
				msg.put("buildingType", StringUtils.trimToEmpty(parking.getParkingType()));
				msg.put("resHigh", parking.getResHigh());
				msg.put("resWidth", parking.getResWidth());
				msg.put("resWeigh", parking.getResWeigh());
				msg.put("certificate", parking.getCertificate());
				msg.put("vehicle", parking.getVehicle());
				msg.put("haveSpecialPlace", StringUtils.trimToEmpty(parking.getHaveSpecialplace()));
				msg.put("womenNum", parking.getWomenNum());
				msg.put("handicapNum", parking.getHandicapNum());
				msg.put("miniNum", parking.getMiniNum());		
				msg.put("vipNum", parking.getVipNum());
				msg.put("rowId", parking.getRowId());		
			}
			return msg;
		}
		return null;
	}
	
	/**
	 * 查询酒店
	 * @author Han Shaoming
	 * @return
	 */
	public Map<String,Object> getHotel(IxPoiObj poi){
		List<BasicRow> rows = poi.getRowsByName("IX_POI_HOTEL");
		if(rows!=null && rows.size()>0){
			Map<String,Object> msg = new HashMap<String, Object>();
			for(BasicRow row:rows){
				IxPoiHotel hotel = (IxPoiHotel) row;
				msg.put("rating", hotel.getRating());
				msg.put("creditCards", StringUtils.trimToEmpty(hotel.getCreditCard()));
				msg.put("description", StringUtils.trimToEmpty(hotel.getLongDescription()));
				msg.put("checkInTime", hotel.getCheckinTime());
				msg.put("checkOutTime", hotel.getCheckoutTime());
				msg.put("roomCount", hotel.getRoomCount());
				msg.put("roomType", StringUtils.trimToEmpty(hotel.getRoomType()));
				msg.put("roomPrice", StringUtils.trimToEmpty(hotel.getRoomPrice()));
				msg.put("breakfast", hotel.getBreakfast());
				msg.put("service", StringUtils.trimToEmpty(hotel.getService()));
				msg.put("parking", hotel.getParking());
				msg.put("openHour", StringUtils.trimToEmpty(hotel.getOpenHour()));
				msg.put("rowId", hotel.getRowId());	
			}
			return msg;
		}
		return null;
	}
	
	/**
	 * 查询充电站
	 * @author Han Shaoming
	 * @return
	 */
	public Map<String,Object> getChargingStation(IxPoiObj poi){
		List<BasicRow> rows = poi.getRowsByName("IX_POI_CHARGINGSTATION");
		if(rows!=null && rows.size()>0){
			Map<String,Object> msg = new HashMap<String, Object>();
			for(BasicRow row:rows){
				IxPoiChargingstation chargingStation = (IxPoiChargingstation) row;
				msg.put("type", chargingStation.getChargingType());
				msg.put("changeBrands", StringUtils.trimToEmpty(chargingStation.getChangeBrands()));
				msg.put("changeOpenType", chargingStation.getChangeOpenType());
				msg.put("servicePro", chargingStation.getServiceProv());
				msg.put("chargingNum", chargingStation.getChargingNum());
				msg.put("openHour", StringUtils.trimToEmpty(chargingStation.getOpenHour()));
				msg.put("parkingFees", chargingStation.getParkingFees());
				msg.put("parkingInfo", StringUtils.trimToEmpty(chargingStation.getParkingInfo()));
				msg.put("availableState", chargingStation.getAvailableState());
				msg.put("rowId", chargingStation.getRowId());	
			}
			return msg;
		}
		return null;
	}
	
	/**
	 * 查询充电桩
	 * @author Han Shaoming
	 * @return
	 */
	public List<Map<String,Object>> getChargingPole(IxPoiObj poi){
		List<BasicRow> rows = poi.getRowsByName("IX_POI_CHARGINGPLOT");
		List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
		if(rows!=null && rows.size()>0){
			for(BasicRow row:rows){
				Map<String,Object> msg = new HashMap<String, Object>();
				IxPoiChargingplot chargingPole = (IxPoiChargingplot) row;
				msg.put("groupId", chargingPole.getGroupId());
				msg.put("acdc", chargingPole.getAcdc());
				msg.put("plugType", chargingPole.getPlugType());
				msg.put("power", StringUtils.trimToEmpty(chargingPole.getPower()));
				msg.put("voltage", StringUtils.trimToEmpty(chargingPole.getVoltage()));
				msg.put("current", StringUtils.trimToEmpty(chargingPole.getCurrent()));
				msg.put("mode", chargingPole.getMode());
				msg.put("count", chargingPole.getCount());
				msg.put("plugNum", chargingPole.getPlugNum());
				msg.put("prices", StringUtils.trimToEmpty(chargingPole.getPrices()));
				msg.put("openType", chargingPole.getOpenType());
				msg.put("availableState", chargingPole.getAvailableState());
				msg.put("manufacturer", StringUtils.trimToEmpty(chargingPole.getManufacturer()));
				msg.put("factoryNum", StringUtils.trimToEmpty(chargingPole.getFactoryNum()));
				msg.put("plotNum", StringUtils.trimToEmpty(chargingPole.getPlotNum()));
				msg.put("productNum", StringUtils.trimToEmpty(chargingPole.getProductNum()));
				msg.put("parkingNum", StringUtils.trimToEmpty(chargingPole.getParkingNum()));
				msg.put("floor", chargingPole.getFloor());
				msg.put("locationType", chargingPole.getLocationType());
				msg.put("payment", chargingPole.getPayment());
				msg.put("rowId", chargingPole.getRowId());
				msgs.add(msg);
			}
		}
		return msgs;
	}
	
	/**
	 * 查询加油站
	 * @author Han Shaoming
	 * @return
	 */
	public Map<String,Object> getGasStation(IxPoiObj poi){
		List<BasicRow> rows = poi.getRowsByName("IX_POI_GASSTATION");
		if(rows!=null && rows.size()>0){
			Map<String,Object> msg = new HashMap<String, Object>();
			for(BasicRow row:rows){
				IxPoiGasstation gasStation = (IxPoiGasstation) row;
				msg.put("fuelType", StringUtils.trimToEmpty(gasStation.getFuelType()));
				msg.put("oilType", StringUtils.trimToEmpty(gasStation.getOilType()));
				msg.put("egType", StringUtils.trimToEmpty(gasStation.getEgType()));
				msg.put("mgType", StringUtils.trimToEmpty(gasStation.getMgType()));
				msg.put("payment", StringUtils.trimToEmpty(gasStation.getPayment()));
				msg.put("service", StringUtils.trimToEmpty(gasStation.getService()));
				msg.put("servicePro", StringUtils.trimToEmpty(gasStation.getServiceProv()));
				msg.put("openHour", StringUtils.trimToEmpty(gasStation.getOpenHour()));
				msg.put("rowId", gasStation.getRowId());
			}
			return msg;
		}
		return null;
	}
	
	/**
	 * 父子关系,子列表
	 * @author Han Shaoming
	 * @return
	 */
	public List<Map<String,Object>> getChildrens(IxPoiObj poi){
		List<Map<String,Object>> msgs = new ArrayList<Map<String,Object>>();
		List<BasicRow> rows = poi.getRowsByName("IX_POI_CHILDREN");
		if(rows!=null && rows.size()>0){
			for(BasicRow row:rows){
				Map<String,Object> msg = new HashMap<String, Object>();
				IxPoiChildren children = (IxPoiChildren) row;
				long childPoiPid = children.getChildPoiPid();
				msg.put("type", children.getRelationType());
				msg.put("childPid", children.getChildPoiPid());
				List<Map<Long, Object>> childFids = poi.getChildFids();
				for (Map<Long, Object> map : childFids) {
					boolean flag = false;
					for (Map.Entry<Long, Object> entry : map.entrySet()) {
						if(childPoiPid==entry.getKey()){
							msg.put("childFid",StringUtils.trimToEmpty((String) map.get(childPoiPid)));
							flag = true;
							break;
						}
					}
					if(flag){
						break;
					}
				}
				msg.put("rowId", children.getRowId());
				msgs.add(msg);
			}
		}
		return msgs;
	}
	

}
