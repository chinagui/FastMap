package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName FMYW20287
 * @author Han Shaoming
 * @date 2017年2月28日 下午3:05:34
 * @Description TODO
 * 检查条件：  lifecycle!=1
 * 检查原则：
 * 充电站(分类230218)名称name中包含“ＥＶＣＡＲＤ”、“特斯拉”、“国家电网”、“循道新能源”、“中国普天”、“依威能源”、“星星充电”其中之一，
 * 服务提供商(chargingStation.servicePro)不是对应的服务商:
 * 1（国家电网）、 6（中国普天）、 8（循道新能源）、15（EVCARD）、16（星星充电）、18（依威能源）、ChainID（汽车品牌）特斯拉(348D)，
 * 则报log：名称与充电站服务提供商不匹配！
 */
public class FMYW20287 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
//		if(obj.objName().equals(ObjectName.IX_POI)){
//			IxPoiObj poiObj=(IxPoiObj) obj;
//			IxPoi poi=(IxPoi) poiObj.getMainrow();
//			String kindCode = poi.getKindCode();
//			if(kindCode == null){return;}
//			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
//			if(ixPoiName == null){return;}
//			String name = ixPoiName.getName();
//			if(name == null){return;}
//			String nameB = CheckUtil.strQ2B(name).toUpperCase();
//			if(nameB.contains("SPA")&&!"210205".equals(kindCode)){
//				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
//				return;
//			}
//		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
