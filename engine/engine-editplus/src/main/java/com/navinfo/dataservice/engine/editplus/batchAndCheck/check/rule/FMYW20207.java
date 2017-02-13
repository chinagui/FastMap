package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMYW20207
 * @author Han Shaoming
 * @date 2017年2月13日 下午3:53:53
 * @Description TODO
 * 检查条件：    lifecycle！=1
 * 检查原则：
 * 1、充电桩编号（chargingPole-plotNum）中存在“GJGY”或者“CJCY”或者“CJGY”时，报log1
 * 2、充电桩编号（chargingPole-plotNum）中存在“GZGY“或者“CZCY”或者“CZGY”时，报log2
 * 3、充电桩编号（chargingPole-plotNum）中存在“CZSY时，报log3
 */
public class FMYW20207 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiChargingplot> ixPoiChargingPlots = poiObj.getIxPoiChargingplots();
			//错误数据
			if(ixPoiChargingPlots==null || ixPoiChargingPlots.isEmpty()){return;}
			for (IxPoiChargingplot ixPoiChargingPlot : ixPoiChargingPlots) {
				String plotNum = ixPoiChargingPlot.getPlotNum();
				if(plotNum != null){
					if("GJGY".equals(plotNum)||"CJCY".equals(plotNum)||"CJGY".equals(plotNum)){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "充电桩编号错误，请确认是否为“GJCY”");
					}
					else if("GZGY".equals(plotNum)||"CZCY".equals(plotNum)||"CZGY".equals(plotNum)){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "充电桩编号错误，请确认是否为“GZCY”");
					}
					else if("CZSY".equals(plotNum)){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "充电桩编号错误，请确认是否为“GZSY”");
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
