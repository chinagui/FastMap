package com.navinfo.dataservice.engine.editplus.model.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiHotel;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.engine.editplus.model.ixpoi.IxPoiRestaurant;

/** 
 * @ClassName: IxPoi
 * @author xiaoxiaowen4127
 * @date 2016年11月8日
 * @Description: IxPoi.java
 */
public class IxPoiObj extends AbstractIxObj {

	public IxPoiObj(BasicRow mainrow) {
		super(mainrow);
	}

//	//子对象
//	protected List<BasicObj> ixPoiName=null;
//	protected List<BasicObj> ixPoiAddress=null;
//	//...
//	//子表
//	protected List<BasicRow> ixPoiContact=null;

	
//	@Override
//	public Map<Class<? extends BasicRow>, List<BasicRow>> childRows() {
//		if(childrows==null){
//			childrows=new HashMap<Class<? extends BasicRow>, List<BasicRow>>();
//			childrows.put(IxPoiContact.class,contacts);
//			//...
//		}
//		return childrows;
//	}
	

//	@Override
//	public Map<Class<? extends BasicObj>, List<BasicObj>> childObjs() {
//		if(childobjs==null){
//			childobjs=new HashMap<Class<? extends BasicObj>, List<BasicObj>>();
//			childobjs.put(IxPoiName.class, names);
//			childobjs.put(IxPoiAddress.class, addresses);
//			//...
//		}
//		return childobjs;
//	}
	
	/**
	 * 根据名称分类,名称类型,语言代码获取名称内容
	 * @author Han Shaoming
	 * @param langCode
	 * @param nameClass
	 * @param nameType
	 * @return
	 */
	public IxPoiName getNameByLct(String langCode,int nameClass,int nameType){
		List<BasicRow> rows = getRowsByName("IX_POI_NAME");
		if(rows!=null && rows.size()>0){
			for(BasicRow row:rows){
				//
				IxPoiName name=(IxPoiName)row;
				if(langCode.equals(name.getLangCode())
						&&name.getNameClass()==nameClass
						&&name.getNameType()==nameType){
					return name;
				}
			}
		}
		return null;
	}
	
	/**
	 * 根据名称组号,语言代码获取地址全称
	 * @author Han Shaoming
	 * @param nameGroupId
	 * @param langCode
	 * @return
	 */
	public IxPoiAddress getFullNameByLg(String langCode,int nameGroupId){
		List<BasicRow> rows = getRowsByName("IX_POI_ADDRESS");
		if(rows!=null && rows.size()>0){
			for(BasicRow row:rows){
				//
				IxPoiAddress name=(IxPoiAddress)row;
				if(langCode.equals(name.getLangCode())
						&&name.getNameGroupid()==nameGroupId){
					return name;
				}
			}
		}
		return null;
	}

	/**
	 * 查询所有的联系方式
	 * @author Han Shaoming
	 * @return
	 */
	public List<Map<String,Object>> getContacts(){
		List<BasicRow> rows = getRowsByName("IX_POI_CONTACT");
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
	public Map<String,Object> getFoodTypes(){
		List<BasicRow> rows = getRowsByName("IX_POI_RESTAURANT");
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
	public Map<String,Object> getParkings(){
		List<BasicRow> rows = getRowsByName("IX_POI_PARKING");
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
	public Map<String,Object> getHotel(){
		List<BasicRow> rows = getRowsByName("IX_POI_HOTEL");
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
	public Map<String,Object> getChargingStation(){
		List<BasicRow> rows = getRowsByName("IX_POI_CHARGINGSTATION");
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
	public List<Map<String,Object>> getChargingPole(){
		List<BasicRow> rows = getRowsByName("IX_POI_CHARGINGPLOT");
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
	public Map<String,Object> getGasStation(){
		List<BasicRow> rows = getRowsByName("IX_POI_GASSTATION");
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
	 * 根据语言代码获取楼层
	 * @author Han Shaoming
	 * @return
	 */
	public IxPoiAddress getFloorByLangCode(String langCode){
		List<BasicRow> rows = getRowsByName("IX_POI_ADDRESS");
		if(rows!=null && rows.size()>0){
			for(BasicRow row:rows){
				IxPoiAddress floor = (IxPoiAddress) row;
				if(langCode.equals(floor.getLangCode())){
					return floor;
				}	
			}
		}
		return null;
	}
	
	@Override
	public String objType() {
		return ObjectType.IX_POI;
	}

}
