package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMYW20203
 * @author Han Shaoming
 * @date 2017年2月13日 下午7:36:44
 * @Description TODO
 * 检查条件：    lifecycle！=1
 * 检查原则：
 * 1. 充电桩位置类型（chargingPole-locationType）为室外0，楼层（chargingPole-floor）不为正整数。
 * 2. 充电桩位置类型（chargingPole-locationType）为室内地上1，楼层（chargingPole-floor）不为正整数。
 * 3. 充电桩位置类型（chargingPole-locationType）为地下2，楼层（chargingPole-floor）不为负整数。
 * 若存在以上三种情况，报LOG：
 */
public class FMYW20203 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiChargingplot> ixPoiChargingPlots = poiObj.getIxPoiChargingplots();
			//错误数据
			if(ixPoiChargingPlots==null || ixPoiChargingPlots.isEmpty()){return;}
			for (IxPoiChargingplot ixPoiChargingPlot : ixPoiChargingPlots) {
				//充电桩位置类型
				int locationType = ixPoiChargingPlot.getLocationType();
				//楼层
				int floor = ixPoiChargingPlot.getFloor();
				if((locationType == 0 && floor < 1)||(locationType == 1 && floor < 1)
						||(locationType == 2 && floor > -1)){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
