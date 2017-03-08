package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName FMYW20287
 * @author Han Shaoming
 * @date 2017年3月8日 上午10:35:34
 * @Description TODO
 * 检查条件：  lifecycle!=1且分类为充电站(kind_code=230218)
 * 检查原则：
 * 元数据库SC_POINT_KIND_RULE.CHECK_RULE=5的记录，
 * 如果充电站官方原始中文名称中包含元数据库表SC_POINT_KIND_RULE.POI_KIND_NAME(转为全角后判断)，
 * 但充电站的服务提供商(chargingStation.servicePro)与元数据库表SC_POINT_KIND_RULE.POI_KIND不一致，
 * 则报log：名称与充电站服务提供商不匹配！
 */
public class FMYW20287 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null||!"230218".equals(kindCode)){return;}
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
			if(ixPoiName == null){return;}
			String name = ixPoiName.getName();
			if(name == null){return;}
			String nameQ = CheckUtil.strB2Q(name);
			List<IxPoiChargingstation> ixPoiChargingstations = poiObj.getIxPoiChargingstations();
			if(ixPoiChargingstations == null || ixPoiChargingstations.isEmpty()){return;}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, String> scPointKindRule5 = metadataApi.scPointKindRule5();
			for (IxPoiChargingstation ixPoiChargingstation : ixPoiChargingstations) {
				String serviceProv = ixPoiChargingstation.getServiceProv();
				for(String poikind : scPointKindRule5.keySet()){
					String poiKindName = scPointKindRule5.get(poikind);
					if(nameQ.contains(poiKindName)&&!serviceProv.equals(poikind)){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
						return;
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
