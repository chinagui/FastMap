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
 * GLM60143
 * 检查对象：IX_POI表中“STATE(状态)”非“1（删除）”的记录；
 * 检查原则：在IX_POI表中，如果KIND_CODE在元数据表SC_POINT_FOODTYPE中POIKIND列中存在,并且KIND_CODE和CHAIN字段值
 * 与元数据表SC_POINT_BRAND_FOODTYPE（品牌分类与FOODTYPE对照表）同一行中的POIKIND和CHAIN值相等，
 * 但FOOD_TYPE不等于对照表中同一行的FOODTYPE,报Log。
 * IX_POI_RESTAURANT
 * @author zhangxiaoyi
 */
public class GLM60143 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			String chain = poi.getChain();
			if(chain==null||chain.isEmpty()){return;}
			if(kind==null||kind.isEmpty()){return;}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<String> kindList = metadataApi.scPointFoodtypeKindList();
			if(!kindList.contains(kind)){return;}
			Map<String, String> map = metadataApi.scPointBrandFoodtypeKindBrandMap();
			String kindChainStr=kind+"|"+chain;
			if(!map.containsKey(kindChainStr)){return;}
			String food = map.get(kindChainStr);
			List<IxPoiRestaurant> restList = poiObj.getIxPoiRestaurants();
			if(restList==null||restList.isEmpty()){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), 
					"分类为"+kind+"连锁品牌为"+chain+"的POI对应的风味类型应为"+food);
				return;}
			for(IxPoiRestaurant res:restList){
				String foodtype=res.getFoodType();
				if(foodtype==null||foodtype.isEmpty()){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), 
							"分类为"+kind+"连锁品牌为"+chain+"的POI对应的风味类型应为"+food);
					return;
				}
				if(!foodtype.equals(food)){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), 
							"分类为"+kind+"连锁品牌为"+chain+"的POI对应的风味类型应为"+food);
					return;
				}
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
