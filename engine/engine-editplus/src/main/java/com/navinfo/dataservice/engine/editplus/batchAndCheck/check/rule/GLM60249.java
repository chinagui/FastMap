package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * GLM60249
 * 检查对象：IX_POI表中“STATE(状态)”非“1（删除）”的数据
 * 检查原则：
 * 数据中已经制作的牛排（3007），三明治（3008）的餐饮风味类型，分类（KIND_CODE）不为“异国风味（110102）”或“快餐（110200）”，报log
 * @author zhangxiaoyi
 */
public class GLM60249 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();	
			List<IxPoiRestaurant> restList = poiObj.getIxPoiRestaurants();
			if(restList==null||restList.isEmpty()){return;}
			if(restList.size()>1){return;}
			String food=restList.get(0).getFoodType();
			String kind=poi.getKindCode();
			if(food!=null&&(food.equals("3007")||food.equals("3008"))&&!kind.equals("110102")&&!kind.equals("110200")){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
