package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMYW20191
 * @author Han Shaoming
 * @date 2017年2月13日 下午7:55:19
 * @Description TODO
 * 检查条件：    lifecycle！=1
 * 检查原则：
 * 充电桩（分类为230227）至少有一组深度信息，至多有一组交流或直流深度信息，否则报log：
 */
public class FMYW20191 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode != null && "230227".equals(kindCode)){
				boolean check = false;
				List<IxPoiChargingplot> ixPoiChargingPlots = poiObj.getIxPoiChargingplots();
				//至少有一组深度信息
				if(ixPoiChargingPlots == null || ixPoiChargingPlots.isEmpty()){check = true;}
				if(ixPoiChargingPlots.size() > 2){check = true;}
				Set<Integer> acdcList = new HashSet<Integer>();
				for (IxPoiChargingplot ixPoiChargingPlot : ixPoiChargingPlots) {
					int acdc = ixPoiChargingPlot.getAcdc();
					acdcList.add(acdc);
				}
				//至多有一组交流或直流深度信息
				if(acdcList.size() >1){
					check = true;
				}
				if(check){
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
