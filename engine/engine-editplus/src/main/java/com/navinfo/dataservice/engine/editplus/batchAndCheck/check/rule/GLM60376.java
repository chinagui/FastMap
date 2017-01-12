package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * GLM60376
 * 检查对象：
 * IX_POI表中“STATE(状态)”非“1（删除）”的POI
 * 检查原则：
 * ①当KIND_CODE为冷饮店（110302）时，FOOD_TYPE的值应在“FOOD_TYPE值域表（SC_POINT_FOODTYPE）”
 * 中POIKIND为110302对应的FOODTYPE中存在，否则报出；
 * ②当FOOD_TYPE值在SC_POINT_FOODTYPE中“MEMO”字段为“饮品”对应的“FOODTYPE”存在时，
 * 则POI的KIND_CODE必须为SC_POINT_FOODTYPE中“MEMO”字段为“饮品”对应的POIKIND，否则报log；
 * @author zhangxiaoyi
 */
public class GLM60376 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(kind==null||kind.isEmpty()){return;}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			//①当KIND_CODE为冷饮店（110302）时，FOOD_TYPE的值应在“FOOD_TYPE值域表（SC_POINT_FOODTYPE）”
			// 中POIKIND为110302对应的FOODTYPE中存在，否则报出；
			if(kind.equals("110302")){
				List<String> foods = metadataApi.scPointFoodtype110302FoodTypes();
				List<IxPoiRestaurant> restList = poiObj.getIxPoiRestaurants();
				if(restList==null||restList.isEmpty()){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "冷饮分类对应风味类型制作错误");
					return;
				}
				for(IxPoiRestaurant res:restList){
					String foodtype=res.getFoodType();
					if(foodtype==null||foodtype.isEmpty()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "冷饮分类对应风味类型制作错误");
						return;
					}
					if(!foods.contains(foodtype)){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"冷饮分类对应风味类型制作错误");
						return;}
				}
			}else{
				//②当FOOD_TYPE值在SC_POINT_FOODTYPE中“MEMO”字段为“饮品”对应的“FOODTYPE”存在时，
				//则POI的KIND_CODE必须为SC_POINT_FOODTYPE中“MEMO”字段为“饮品”对应的POIKIND，否则报log；
				Map<String, String> foodMap = metadataApi.scPointFoodtypeDrinkMap();
				List<IxPoiRestaurant> restList = poiObj.getIxPoiRestaurants();
				if(restList==null||restList.isEmpty()){return;}
				for(IxPoiRestaurant res:restList){
					String foodtype=res.getFoodType();
					if(foodtype==null||foodtype.isEmpty()){continue;}
					if(foodMap.containsKey(foodtype)&&!foodMap.get(foodtype).equals(kind)){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"冷饮风味类型对应分类制作错误");
						return;}
				}
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
