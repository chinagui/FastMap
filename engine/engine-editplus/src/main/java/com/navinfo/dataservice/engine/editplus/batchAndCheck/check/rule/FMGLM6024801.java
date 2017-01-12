package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-GLM60248-01
 * 检查条件：Lifecycle！=1（删除）
 * 检查原则：
 * 检查分类(kindCode)为110102（西餐），POI名称（name）（官方原始中文名）含"比萨"，“比薩”，"牛排"，"三明治"，“三文治”，
 * 并且风味类型（FOOD_TYPE）为"1001"（国际化）的记录，报log：分类为西餐的牛排、三明治，三文治的餐饮风味类型错误
 * @author zhangxiaoyi
 */
public class FMGLM6024801 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(kind==null||kind.isEmpty()){return;}
			if(!kind.equals("110102")){return;}
			IxPoiName name = poiObj.getOfficeOriginCHName();
			if(name==null){return;}
			String nameStr = name.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			if(!nameStr.contains("比萨")&&!nameStr.contains("比薩")&&!nameStr.contains("牛排")
					&&!nameStr.contains("三明治")&&!nameStr.contains("三文治")){return;}
			List<IxPoiRestaurant> restList = poiObj.getIxPoiRestaurants();
			if(restList==null||restList.isEmpty()){return;}
			for(IxPoiRestaurant res:restList){
				String foodtype=res.getFoodType();
				if(foodtype==null||foodtype.isEmpty()){continue;}
				if(foodtype.equals("1001")){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
					return;}
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
