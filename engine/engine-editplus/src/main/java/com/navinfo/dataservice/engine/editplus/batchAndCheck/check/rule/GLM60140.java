package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * GLM60140
 * 检查对象：
 *   IX_POI表中“STATE(状态)”非“1（删除）”的记录；
 *   检查原则：
 *   在IX_POI表中，当KIND_CODE为异国风味（110102）时，
 *   1.FOOD_TYPE值为单个值时，不在“FOOD_TYPE值域表 (SC_POINT_FOODTYPE)” POIKIND为110102对应的FOODTYPE的报出；
 *   2.FOOD_TYPE值为组合形式时，应该是“FOOD_TYPE值域表 (SC_POINT_FOODTYPE)”中POIKIND为110102对应的地方风味和形式风味的组合形式：
 *     B……|A……，TYPE为A是地方风味，TYPE为B是形式风味，否则报出；
 * @author zhangxiaoyi
 */
public class GLM60140 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(kind==null||kind.isEmpty()){return;}
			if(!kind.equals("110102")){return;}			
			List<IxPoiRestaurant> restList = poiObj.getIxPoiRestaurants();
			if(restList==null||restList.isEmpty()){return;}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, Map<String, String>> foods = metadataApi.scPointFoodtypeFoodTypes();
			Map<String, String> food110102s=foods.get("110102");
			String types="";
			for(IxPoiRestaurant res:restList){
				String foodtype=res.getFoodType();
				if(foodtype==null||foodtype.isEmpty()){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "风味类型值不在值域中");
					return;
				}
				String[] foodList = foodtype.split("\\|");
				for(int i=0;i<foodList.length;i++){
					String foodTmp=foodList[i];
					if(!food110102s.keySet().contains(foodTmp)){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"风味类型值不在值域中");
						return;}
					types+=food110102s.get(foodTmp);
					if(i%2!=1){continue;}
					Pattern P = Pattern.compile("^B+A$");
					Matcher m = P.matcher(types);
					if(!m.matches()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"风味类型不满足组合形式：形式风味|地方风味");
						return;
					}
				}
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
