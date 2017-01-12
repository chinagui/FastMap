package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oracle.net.aso.f;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiRestaurant;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM-GLM60247-01
 * 检查对象：Lifecycle！=1（删除）
 * 检查原则：
 * 分类(kindCode)为110102（西餐），POI（官方原始中文名）含“牛排”，并且风味类型（FOOD_TYPE）非"1001"（国际化）、“3007”（牛排）、
 * “3008”（三明治）、“3010”、“3011”、“3013”（比萨）或为空的记录，报log：分类为西餐的牛排的餐饮风味类型错误
 * @author zhangxiaoyi
 */
public class FMGLM6024701 extends BasicCheckRule {
	
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
			if(!nameStr.contains("牛排")){return;}
			List<IxPoiRestaurant> restList = poiObj.getIxPoiRestaurants();
			if(restList==null||restList.isEmpty()){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
				return;}
			String[] tmp = new String[]{"1001","3007","3008","3010","3011","3013"};
			for(IxPoiRestaurant res:restList){
				String foodtype=res.getFoodType();
				if(foodtype==null||foodtype.isEmpty()){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
					return;}
				if(!Arrays.asList(tmp).contains(foodtype)){
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
