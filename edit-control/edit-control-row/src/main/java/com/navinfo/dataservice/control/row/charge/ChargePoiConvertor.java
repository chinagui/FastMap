package com.navinfo.dataservice.control.row.charge;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
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
	 * @throws Exception 
	 */
	public JSONObject initPoi(IxPoiObj poiObj,Connection conn) throws Exception{
		//获取充电桩数据
		Map<Long, BasicObj> plotMap = this.getChargePlot(poiObj, conn);
		//过滤数据
		
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
		String name = "";
		if(ixPoiName != null){
			name = ixPoiName.getName();
		}
		chargePoi.put("name", name);
		//地址
		IxPoiAddress ixPoiAddress = poiObj.getCHAddress();
		String address = "";
		if(ixPoiAddress != null){
			address = ixPoiAddress.getFullname();
		}
		chargePoi.put("address", address);
		//poi类型
		String chargingType = this.getKindCode(poiObj);
		chargePoi.put("kindCode", chargingType);
		//联系方式
		String telephone = this.getTelephone(poiObj);
		chargePoi.put("telephone", telephone);
		//行政区划
		String adminCode = String.valueOf(poiObj.getAdminId());
		chargePoi.put("adminCode", adminCode);
		//24小时开放
		int open24H = 2;
		if(ixPoi.getOpen24h() == 0){
			open24H = 2;
		}else{
			open24H = ixPoi.getOpen24h();
		}
		chargePoi.put("open24H", open24H);
		//网址，预留字段
		chargePoi.put("website", "");
		//poi的存在状态
		int state = this.getState(poiObj);
		chargePoi.put("state", state);
		//poi的开放状态
		
		//可充电的车品牌
		//充电桩庄主,该字段不更新
		//插座详细信息描述
		//充电桩总数量

		
		
		
		
		
		
		
		
		return chargePoi;
		
	}
	
	/**
	 * 获取充电桩
	 * @author Han Shaoming
	 * @param poiObj
	 * @return
	 * @throws Exception 
	 */
	private Map<Long,BasicObj> getChargePlot(IxPoiObj poiObj,Connection conn) throws Exception{
		try {
			//获取子充电桩pid
			List<Long> childPids = new ArrayList<Long>();
			List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHILDREN");
			if(rows!=null && rows.size()>0){
				for(BasicRow row:rows){
					IxPoiChildren children = (IxPoiChildren) row;
					childPids.add(children.getChildPoiPid());
				}
			}
			//设置查询子表
			Set<String> selConfig = new HashSet<String>();
			selConfig.add("IX_POI_NAME");
			selConfig.add("IX_POI_ADDRESS");
			selConfig.add("IX_POI_CHARGINGPLOT");
			//查询数据
			Map<Long,BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, selConfig, false,childPids, true, false);
			return objs;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("查询充电桩出错"+e.getMessage(),e);
		}
	}
	
	/**
	 * 根据条件过滤数据
	 * @author Han Shaoming
	 * @param poiObj
	 * @return
	 */
	private boolean filterPoi(IxPoiObj poiObj){
		
		
		
		return false;
	}
	
	/**
	 * 获取POI类型
	 * @author Han Shaoming
	 * @param poiObj
	 * @return
	 */
	private String getKindCode(IxPoiObj poiObj){
		String kindCode = "";
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
		if(rows != null && rows.size() > 0){
			for (BasicRow row : rows) {
				IxPoiChargingstation ixPoiChargingstation = (IxPoiChargingstation) row;
				int type = ixPoiChargingstation.getChargingType();
				if(type == 1 || type == 3){
					kindCode = "1";
				}else if(type == 2){
					kindCode = "2";
				}else if(type == 4){
					kindCode = "4";
				}
			}
			
		}
		return kindCode;
	}
	
	/**
	 * 查询所有的联系方式
	 * @author Han Shaoming
	 * @return
	 */
	private String getTelephone(IxPoiObj poiObj){
		List<String> telephoneList = new ArrayList<String>();
		Map<Integer,List<String>> map = new HashMap<Integer,List<String>>();
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CONTACT");
		if(rows!=null && rows.size()>0){
			for(BasicRow row:rows){
				IxPoiContact ixPoiContact = (IxPoiContact) row;
				int contactType = ixPoiContact.getContactType();
				if(contactType != 1 && contactType != 2 && contactType != 3 && contactType != 4){
					continue;
				}
				String contact = ixPoiContact.getContact();
				if(!map.containsKey(contactType)){
					map.put(contactType, new ArrayList<String>());
				}
				map.get(contactType).add(contact);
			}
		}
		//处理数据
		if(map.containsKey(3)){
			telephoneList.addAll(map.get(3));
		}else if(map.containsKey(4)){
			telephoneList.addAll(map.get(4));
		}else if(map.containsKey(1)){
			telephoneList.addAll(map.get(1));
		}else if(map.containsKey(2)){
			telephoneList.addAll(map.get(2));
		}
		
		return StringUtils.join(telephoneList, "|");
	}
	
	/**
	 * 查询poi的存在状态
	 * @author Han Shaoming
	 * @return
	 */
	private int getState(IxPoiObj poiObj){
		int state = 0;
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
		if(rows != null && rows.size() > 0){
			for (BasicRow row : rows) {
				IxPoiChargingstation ixPoiChargingstation = (IxPoiChargingstation) row;
				int availableState = ixPoiChargingstation.getAvailableState();
				if(availableState != 0){
					state = 5;
				}
			}
		}
		return state;
	}
	
	
	/**
	 * 查询poi的开放状态和可充电的车品牌
	 * @author Han Shaoming
	 * @return
	 */
	private int getPlotKindAndCarBrand(IxPoiObj poiObj){
		String plotKind = "0";
		String carBrand = "";
		int state = 0;
		List<BasicRow> rows = poiObj.getRowsByName("IX_POI_CHARGINGSTATION");
		if(rows != null && rows.size() > 0){
			for (BasicRow row : rows) {
				IxPoiChargingstation ixPoiChargingstation = (IxPoiChargingstation) row;
				int availableState = ixPoiChargingstation.getAvailableState();
				if(availableState != 0){
					state = 5;
				}
			}
		}
		return state;
	}
	
	
	
	
}
