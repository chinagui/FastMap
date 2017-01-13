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
 * GLM60142
 * 检查对象：IX_POI表中“STATE(状态)”非“1（删除）”的记录；
 * 检查原则：1.在IX_POI表中，如果FOOD_TYPE有值，则KIND_CODE应在“FOOD_TYPE值域表（SC_POINT_FOODTYPE）”
 * 中的POIKIND存在，否则报LOG
 * 2.风味类型表IX_POI_RESTAURANT中字段FOOD_TYPE字段值为空，报LOG
 * 3.KIND_CODE在“FOOD_TYPE值域表（SC_POINT_FOODTYPE）”中的POIKIND存在,
 * 但PID没有在IX_POI_RESTAURANT中,报LOG
 * @author zhangxiaoyi
 */
public class GLM60142 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(kind==null||kind.isEmpty()){return;}		
			List<IxPoiRestaurant> restList = poiObj.getIxPoiRestaurants();
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, Map<String, String>> foods = metadataApi.scPointFoodtypeFoodTypes();
			//KIND_CODE在“FOOD_TYPE值域表（SC_POINT_FOODTYPE）”中的POIKIND存在,但PID没有在IX_POI_RESTAURANT中,报LOG
			if((restList==null||restList.isEmpty())&&foods.containsKey(kind)){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "分类在制作风味类型的值域范围内,没有制作风味类型,需要补充风味类型");
				return;
			}
			//风味类型表IX_POI_RESTAURANT中字段FOOD_TYPE字段值为空，报LOG
			String foodStr=restList.get(0).getFoodType();
			if(foodStr==null||foodStr.isEmpty()){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "风味类型字段值为空值错误");
				return;
			}
			//在IX_POI表中，如果FOOD_TYPE有值，则KIND_CODE应在“FOOD_TYPE值域表（SC_POINT_FOODTYPE）”
			//中的POIKIND存在，否则报LOG
			if(!foods.containsKey(kind)){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "允许制作餐饮风味类型的分类值域检查："+kind+"不可以制作风味类型");
				return;
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
