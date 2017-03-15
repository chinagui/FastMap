package com.navinfo.dataservice.engine.editplus.convert;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiIcon;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiPhoto;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

/**
 * 对象层级结构及属性名一致互转
 * 
 * @ClassName: DefaultObjConvertor
 * @author xiaoxiaowen4127
 * @date 2016年11月13日
 * @Description: DefaultObjConvertor.java
 */
public class DefaultObjConvertor {

	public JSONArray objConvertorJson(Collection<BasicObj> objs) throws Exception {
		JSONArray json = new JSONArray();
		for (BasicObj basicObj : objs) {
			JSONObject jso = new JSONObject();
			// 主表
			BasicRow mainrow = basicObj.getMainrow();
			jso.put("command", changeOpType(mainrow.getHisOpType()));
			// 获取主表类型
			String objName = basicObj.objName();
			String objType = getObjType(objName);
			jso.put("type", objType);
			jso.put("objId", basicObj.objPid());
			JsonConfig mainConfig = new JsonConfig();
			// 过滤属性值为空的
			mainConfig.setJsonPropertyFilter(new PropertyFilter() {
				@Override
				public boolean apply(Object object, String fieldName, Object fieldValue) {
					// TODO Auto-generated method stub
					if (fieldValue == null||fieldName.equals("geometry")||fieldName.equals("geoPid")
							||fieldName.equals("geoType")||fieldName.equals("hisOpType")||fieldName.equals("objPid")
							||fieldName.equals("objType")||fieldName.equals("opType")||fieldName.equals("rawFields")
							||fieldName.equals("freshFlag")||fieldName.equals("changed") ) {
						return true;
					}
					return false;
				}
			});
			JSONObject mainJso = JSONObject.fromObject(mainrow, mainConfig);
			if(basicObj.objName().equals(ObjectName.IX_POI)){
				IxPoi  ixPoi = (IxPoi) mainrow;
				Geometry geometry = ixPoi.getGeometry();
				mainJso.put("geometry", geometry.toText());
			}
			mainJso.put("objStatus", changeOpType(mainrow.getHisOpType()));
			// 子表
			Map<String, List<BasicRow>> subrows = basicObj.getSubrows();
			JsonConfig subRowConfig = new JsonConfig();
			// 过滤属性值为空的
			subRowConfig.setJsonPropertyFilter(new PropertyFilter() {
				@Override
				public boolean apply(Object object, String fieldName, Object fieldValue) {
					// TODO Auto-generated method stub
					if (fieldValue == null || fieldName.equals("geometry")||fieldName.equals("geoPid")
							||fieldName.equals("geoType")||fieldName.equals("hisOpType")||fieldName.equals("objPid")
							||fieldName.equals("objType")||fieldName.equals("opType")||fieldName.equals("rawFields")
							||fieldName.equals("freshFlag")||fieldName.equals("changed")) {
						return true;
					}else{
						return false;
					}
				}
			});
			// 先处理有三级子表的二级子表
			List<BasicRow> nameList = subrows.get("IX_POI_NAME");
			if (nameList != null && !nameList.isEmpty()) {
				JSONArray nameJa = new JSONArray();
				for (BasicRow nameRow : nameList) {
					JSONObject nameJs = JSONObject.fromObject(nameRow, subRowConfig);
					nameJs.put("objStatus", changeOpType(nameRow.getOpType()));
					// 修改主键名称
					nameJs.put("pid", nameJs.get("nameId"));
					nameJs.remove("nameId");
					// 处理三级子表
					List<BasicRow> nameFlagList = subrows.get("IX_POI_NAME_FLAG");
					if (nameFlagList != null && !nameFlagList.isEmpty()) {
						JSONArray nameFlagJa = new JSONArray();
						for (BasicRow nameFlagRow : nameFlagList) {
							JSONObject nameFlagJs = JSONObject.fromObject(nameFlagRow, subRowConfig);
							nameFlagJs.put("objStatus", changeOpType(nameFlagRow.getOpType()));
							nameFlagJa.add(nameFlagJs);
						}

						nameJs.put("nameFlags", nameFlagJa);
					}
					List<BasicRow> nameToneList = subrows.get("IX_POI_NAME_TONE");
					if (nameToneList != null && !nameToneList.isEmpty()) {
						JSONArray nameToneJa = new JSONArray();
						for (BasicRow nameToneRow : nameFlagList) {
							JSONObject nameToneJs = JSONObject.fromObject(nameToneRow, subRowConfig);
							nameToneJs.put("objStatus", changeOpType(nameToneRow.getOpType()));
							nameToneJa.add(nameToneJs);
						}
						nameJs.put("nameTones", nameToneJa);
					}
					nameJa.add(nameJs);
				}
				mainJso.put("names", nameJa);
			}
			List<BasicRow> parentList = subrows.get("IX_POI_PARENT");
			if (parentList != null && !parentList.isEmpty()) {
				JSONArray parentJa = new JSONArray();
				for (BasicRow parentRow : parentList) {
					JSONObject parentJs = JSONObject.fromObject(parentRow, subRowConfig);
					parentJs.put("objStatus", changeOpType(parentRow.getOpType()));
					// 处理三级子表
					List<BasicRow> childrenList = subrows.get("IX_POI_CHILDREN");
					if (childrenList != null && !childrenList.isEmpty()) {
						JSONArray childrenJa = new JSONArray();
						for (BasicRow childrenRow : childrenList) {
							JSONObject childrenJs = JSONObject.fromObject(childrenRow, subRowConfig);
							childrenJs.put("objStatus", changeOpType(childrenRow.getOpType()));
							childrenJa.add(childrenJs);
						}
						parentJs.put("children", childrenJa);

					}
				}
				mainJso.put("parents", parentJa);
			}
			// 处理其他子表
			for (String key : subrows.keySet()) {
				if("IX_POI".equals(key)){continue;}
				if("IX_POI_NAME".equals(key)||"IX_POI_NAME_FLAG".equals(key)||"IX_POI_NAME_TONE".equals(key)
						||"IX_POI_PARENT".equals(key)||"IX_POI_CHILDREN".equals(key)){
					continue;
				}
				List<BasicRow> subrowList = subrows.get(key);
				if (subrowList != null && !subrowList.isEmpty()) {
					JSONArray subrowJa = new JSONArray();
					for (BasicRow subrowRow : subrowList) {
						JSONObject subrowJs = JSONObject.fromObject(subrowRow, subRowConfig);
						subrowJs.put("objStatus", changeOpType(subrowRow.getOpType()));
						// 修改主键名称
						String name = getSubRowPKName(key);
						if(name != null){
							subrowJs.put("pid", subrowJs.get(name));
							subrowJs.remove(name);
						}
						if("IX_POI_PHOTO".equals(key)){
							IxPoiPhoto ixPoiPhoto = (IxPoiPhoto) subrowRow;
							subrowJs.remove("pid");
							subrowJs.put("fccPid", ixPoiPhoto.getPid());
						}
						if("IX_POI_ICON".equals(key)){
							IxPoiIcon  ixPoiIcon = (IxPoiIcon) subrowRow;
							Geometry geometry = (Geometry) ixPoiIcon.getGeometry();
							subrowJs.put("geometry", geometry.toText());
						}
						subrowJa.add(subrowJs);
					}
					mainJso.put(getColumnBySubRowName(key), subrowJa);
				}
			}
			jso.put("data", mainJso);
			json.add(jso);
		}
		return json;
	}

	/**
	 * 获取主表类型
	 * 
	 * @author Han Shaoming
	 * @return
	 * @throws Exception
	 */
	private String getObjType(String type) throws Exception {
		// 添加所需的子表
		String objType = null;
		if (ObjectName.IX_POI.equals(type)) {
			objType = "IXPOI";
		} else if (ObjectName.IX_SAMEPOI.equals(type)) {
			objType = "IXSAMEPOI";
		} else if (ObjectName.IX_HAMLET.equals(type)) {
			objType = "IXHAMLET";
		} else if (ObjectName.AD_FACE.equals(type)) {
			objType = "ADFACE";
		} else if (ObjectName.AD_LINK.equals(type)) {
			objType = "ADLINK";
		} else if (ObjectName.AD_NODE.equals(type)) {
			objType = "ADNODE";
		} else {
			throw new Exception("未找到相应的主表类型");
		}
		return objType;
	}

	/**
	 * 修改状态字段
	 * 
	 * @author Han Shaoming
	 * @return
	 * @throws Exception
	 */
	private OperationType changeOpType(OperationType type) throws Exception {
		OperationType opType = null;
		if (OperationType.PRE_DELETED.equals(type)) {
			opType = OperationType.DELETE;
		} else {
			opType = type;
		}
		return opType;
	}
	
	/**
	 * 根据子表名称获取字段名
	 */
	private String getColumnBySubRowName(String subRowName) throws Exception {
		if("IX_POI_ADDRESS".equals(subRowName)){
			return "addresses";
		}else if("IX_POI_AUDIO".equals(subRowName)){
			return "audioes";
		}else if("IX_POI_CONTACT".equals(subRowName)){
			return "contacts";
		}else if("IX_POI_ENTRYIMAGE".equals(subRowName)){
			return "entryImages";
		}else if("IX_POI_FLAG".equals(subRowName)){
			return "flags";
		}else if("IX_POI_ICON".equals(subRowName)){
			return "icons";
		}else if("IX_POI_NAME".equals(subRowName)){
			return "names";
		}else if("IX_POI_PARENT".equals(subRowName)){
			return "parents";
		}else if("IX_POI_CHILDREN".equals(subRowName)){
			return "children";
		}else if("IX_POI_PHOTO".equals(subRowName)){
			return "photos";
		}else if("IX_POI_VIDEO".equals(subRowName)){
			return "videoes";
		}else if("IX_POI_PARKING".equals(subRowName)){
			return "parkings";
		}else if("IX_POI_TOURROUTE".equals(subRowName)){
			return "tourroutes";
		}else if("IX_POI_EVENT".equals(subRowName)){
			return "events";
		}else if("IX_POI_DETAIL".equals(subRowName)){
			return "details";
		}else if("IX_POI_BUSINESSTIME".equals(subRowName)){
			return "businesstimes";
		}else if("IX_POI_CHARGINGSTATION".equals(subRowName)){
			return "chargingstations";
		}else if("IX_POI_CHARGINGPLOT".equals(subRowName)){
			return "chargingplots";
		}else if("IX_POI_CHARGINGPLOT_PH".equals(subRowName)){
			return "chargingplotPhs";
		}else if("IX_POI_BUILDING".equals(subRowName)){
			return "buildings";
		}else if("IX_POI_ADVERTISEMENT".equals(subRowName)){
			return "advertisements";
		}else if("IX_POI_GASSTATION".equals(subRowName)){
			return "gasstations";
		}else if("IX_POI_INTRODUCTION".equals(subRowName)){
			return "introductions";
		}else if("IX_POI_ATTRACTION".equals(subRowName)){
			return "attractions";
		}else if("IX_POI_HOTEL".equals(subRowName)){
			return "hotels";
		}else if("IX_POI_RESTAURANT".equals(subRowName)){
			return "restaurants";
		}else if("IX_POI_CARRENTAL".equals(subRowName)){
			return "carrentals";
		}else if("IX_SAMEPOI_PART".equals(subRowName)){
			return "samepoiParts";
		}else if("IX_POI_NAME_FLAG".equals(subRowName)){
			return "nameFlags";
		}else if("IX_SAMEPOI".equals(subRowName)){
			return "samepois";
		}else if("IX_POI_NAME_TONE".equals(subRowName)){
			return "nameTones";
		}else{
			throw new Exception("字段名为:"+subRowName+"的子表未找到");
		}
	}
	
	/**
	 * 根据子表名称获取字段名
	 */
	private String getSubRowPKName(String subRowName) throws Exception {
		if("IX_POI_ADDRESS".equals(subRowName)){
			return "nameId";
		}else if("IX_POI_ICON".equals(subRowName)){
			return "relId";
		}else if("IX_POI_NAME".equals(subRowName)){
			return "nameId";
		}else if("IX_POI_PARKING".equals(subRowName)){
			return "parkingId";
		}else if("IX_POI_TOURROUTE".equals(subRowName)){
			return "tourId";
		}else if("IX_POI_EVENT".equals(subRowName)){
			return "eventId";
		}else if("IX_POI_CHARGINGSTATION".equals(subRowName)){
			return "chargingId";
		}else if("IX_POI_ADVERTISEMENT".equals(subRowName)){
			return "advertiseId";
		}else if("IX_POI_GASSTATION".equals(subRowName)){
			return "gasstationId";
		}else if("IX_POI_INTRODUCTION".equals(subRowName)){
			return "introductionId";
		}else if("IX_POI_ATTRACTION".equals(subRowName)){
			return "attractionId";
		}else if("IX_POI_HOTEL".equals(subRowName)){
			return "hotelId";
		}else if("IX_POI_RESTAURANT".equals(subRowName)){
			return "restaurantId";
		}else{
			return null;
		}
	}
	

}
