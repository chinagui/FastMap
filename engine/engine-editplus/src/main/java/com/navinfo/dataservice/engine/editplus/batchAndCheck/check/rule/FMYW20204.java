package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * @ClassName FMYW20204
 * @author Han Shaoming
 * @date 2017年2月13日 下午7:18:54
 * @Description TODO
 * 检查条件：    非删除POI对象
 * 检查原则：
 * 1. 名称以充电桩结尾，但分类赋的是充电站（230218）分类。
 * 2. 名称以充电站、充电点结尾，但分类赋的是充电桩（230227）分类。
 * 若存在以上情况，报LOG：
 */
public class FMYW20204 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj=(IxPoiObj) obj;
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		String kindCode = poi.getKindCode();
		if(kindCode == null){return;}
		IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
		if(ixPoiName == null){return;}
		String name = ixPoiName.getName();
		if(name == null){return;}
		if((name.endsWith("充电桩") && "230218".equals(kindCode))
				||(name.endsWith("充电站") && "230227".equals(kindCode))
				||(name.endsWith("充电点") && "230227".equals(kindCode))){
			setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
