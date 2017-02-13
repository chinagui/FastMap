package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMYW20205
 * @author Han Shaoming
 * @date 2017年2月13日 下午7:09:57
 * @Description TODO
 * 检查条件：    lifecycle！=1
 * 检查原则：
 * 插头类型（chargingPole-plugType）大于4个值，报LOG：
 */
public class FMYW20205 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiChargingplot> ixPoiChargingPlots = poiObj.getIxPoiChargingplots();
			//错误数据
			if(ixPoiChargingPlots==null || ixPoiChargingPlots.isEmpty()){return;}
			for (IxPoiChargingplot ixPoiChargingPlot : ixPoiChargingPlots) {
				String plugType = ixPoiChargingPlot.getPlugType();
				if(plugType != null){
					String[] types = plugType.split("|");
					if(types.length > 4){
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
