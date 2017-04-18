package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FM14Sum0203
 * @author Han Shaoming
 * @date 2017年2月23日 下午6:57:27
 * @Description TODO
 * 检查条件：非删除POI对象；
 * 检查原则：
 * kindCode值是非餐饮分类(kindCode not like "11*")，IX_POI_RESTAURANT.food_type值不为空。
 * 通过分类kind_code值在SC_POINT_FOODTYPE.poikind中查询，如果分类在SC_POINT_FOODTYPE中找不到，
 * 而数据中的food_type值不为空，则报log：非餐饮分类调查表不为空！
 */
public class FM14Sum0203 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode==null||kindCode.startsWith("11")){return;}
			List<IxPoiRestaurant> ixPoiRestaurants = poiObj.getIxPoiRestaurants();
			if(ixPoiRestaurants==null||ixPoiRestaurants.isEmpty()){return;}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<String> scPointFoodtypes = metadataApi.scPointFoodtypeKindList();
			for (IxPoiRestaurant ixPoiRestaurant : ixPoiRestaurants) {
				String foodType = ixPoiRestaurant.getFoodType();
				if(!scPointFoodtypes.contains(kindCode)&&foodType!=null){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					return;
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
