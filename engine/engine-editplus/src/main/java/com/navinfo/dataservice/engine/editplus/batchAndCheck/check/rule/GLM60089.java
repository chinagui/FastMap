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
 * GLM60089
 * 检查对象：
 *   IX_POI表中“STATE(状态)”非“1（删除）”的记录；
 *   检查原则：
 *     在IX_POI表中，
 *     当KIND_CODE为快餐（110200）时，FOOD_TYE值不在“FOOD_TYPE值域表 (SC_POINT_FOODTYPE)” POIKIND为快餐
 *     （110200）的FOODTYPE的报出
 * @author zhangxiaoyi
 */
public class GLM60089 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(kind==null||kind.isEmpty()){return;}
			if(!kind.equals("110200")){return;}			
			List<IxPoiRestaurant> restList = poiObj.getIxPoiRestaurants();
			if(restList==null||restList.isEmpty()){return;}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, Map<String, String>> foods = metadataApi.scPointFoodtypeFoodTypes();
			Map<String, String> foodMap=foods.get(kind);
			String foodType = restList.get(0).getFoodType();
			if(foodType ==null || !foodMap.containsKey(foodType)){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "110200分类的POI对应的FOOD_TYPE为"+foodMap.keySet().toString().replace("[", "").replace("]", ""));
				return;
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
