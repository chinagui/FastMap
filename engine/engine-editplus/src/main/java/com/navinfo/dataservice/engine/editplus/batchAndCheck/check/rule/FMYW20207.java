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
 * 检查条件：   非删除POI对象且非鲜度验证数据且分类为充电桩分类230227；
 * 检查原则：
 * 1、充电桩编号（chargingPole-plotNum）中存在“GJGY”或者“CJCY”或者“CJGY”时，报log1：充电桩编号错误，请确认是否为“GJCY”！
 * 2、充电桩编号（chargingPole-plotNum）中存在“GZGY“或者“CZCY”或者“CZGY”时，报log2：充电桩编号错误，请确认是否为“GZCY”！
 * 3、充电桩编号（chargingPole-plotNum）中存在“CZSY时，报log3：充电桩编号错误，请确认是否为“GZSY”！
 */
public class FMYW20207 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null || !"230227".equals(kindCode)){return;}
			List<IxPoiChargingplot> ixPoiChargingPlots = poiObj.getIxPoiChargingplots();
			//错误数据
			if(ixPoiChargingPlots==null || ixPoiChargingPlots.isEmpty()){return;}
			for (IxPoiChargingplot ixPoiChargingPlot : ixPoiChargingPlots) {
				String plotNum = ixPoiChargingPlot.getPlotNum();
				if(plotNum != null){
					if(plotNum.contains("GJGY")||plotNum.contains("CJCY")||plotNum.contains("CJGY")){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "充电桩编号错误，请确认是否为“GJCY”");
					}
					else if(plotNum.contains("GZGY")||plotNum.contains("CZCY")||plotNum.contains("CZGY")){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), "充电桩编号错误，请确认是否为“GZCY”");
					}
					else if(plotNum.contains("CZSY")){
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
