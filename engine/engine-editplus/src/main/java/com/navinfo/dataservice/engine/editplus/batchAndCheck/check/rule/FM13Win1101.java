package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FM13Win1101
 * @author Han Shaoming
 * @date 2017年2月14日 上午10:12:07
 * @Description TODO
 * 检查条件：    Lifecycle！=1（删除）
 * 检查原则：
 * 当加油站的深度信息（gasStation.oilType）存在90、93、97号任意一项时，不允许存在89、92、95号汽油任意一项，
 * 否则报Log：汽油油品信息错误，请确认
 */
public class FM13Win1101 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiGasstation> ixPoiGasStations = poiObj.getIxPoiGasstations();
			//错误数据
			if(ixPoiGasStations==null || ixPoiGasStations.isEmpty()){return;}
			for (IxPoiGasstation ixPoiGasStation : ixPoiGasStations) {
				//汽油类型
				String oilType = ixPoiGasStation.getOilType();
				if(oilType == null){return;}
				if(oilType.contains("90")||oilType.contains("93")||oilType.contains("97")){
					if(oilType.contains("89")||oilType.contains("92")||oilType.contains("95")){
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
