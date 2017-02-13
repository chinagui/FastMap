package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMYW20226
 * @author Han Shaoming
 * @date 2017年2月13日 下午3:09:54
 * @Description TODO
 * 检查条件：    lifecycle！=1
 * 检查原则：
 * 如果充电站类型（chargingStation.type)=2 或 4, chargingStation.changeBrands为空，则报log：
 */
public class FMYW20226 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//充电站类型
			List<IxPoiChargingstation> ixPoiChargingStations = poiObj.getIxPoiChargingstations();
			//错误数据
			if(ixPoiChargingStations==null || ixPoiChargingStations.isEmpty()){return;}
			for (IxPoiChargingstation ixPoiChargingStation : ixPoiChargingStations) {
				int type = ixPoiChargingStation.getChargingType();
				if(type == 2 || type == 4){
					String changeBrands = ixPoiChargingStation.getChangeBrands();
					if(changeBrands == null){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
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
