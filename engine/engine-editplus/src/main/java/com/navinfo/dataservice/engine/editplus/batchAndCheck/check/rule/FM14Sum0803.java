package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.Map;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * @Title: FM14Sum0804
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description:检查条件：非删除POI对象；
				检查原则：满足条件（1）或（2），就报log：引导坐标和显示坐标必须在中国范围内！
				 	（1）POI的显示坐标（经、纬度）或引导坐标（经、纬度）不在国内范围的设施。值域：( cint(x) not between 72 and 138 or cint(y) not between 15 and 55)
					（2）POI的显示坐标或引导坐标（经、纬度）为0的设施；
 * @Author: LittleDog
 * @Date: 2017年8月9日
 * @Version: V1.0
 */
public class FM14Sum0803 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
	}

	public void run() throws Exception {
		for (Map.Entry<Long, BasicObj> entry : getRowList().entrySet()) {
			BasicObj basicObj = entry.getValue();

			if (!basicObj.objName().equals(ObjectName.IX_POI))
				continue;

			IxPoiObj poiObj = (IxPoiObj) basicObj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();

			Geometry poiGeo = poi.getGeometry();
			if (poiGeo == null)
				continue;

			Point point = poiGeo.getCentroid();
			double xShow = point.getX();
			double yShow = point.getY();
			double xGuide = poi.getXGuide();
			double yGuide = poi.getYGuide();
			
			if (xShow == 0 || yShow == 0 || xGuide == 0 || yGuide == 0) {
				setCheckResult(poiGeo, poiObj, poi.getMeshId(), null);
			} else if (xShow <= 72 || xShow >= 138 || xGuide <= 72 || xGuide >= 138 || yShow <= 15 || yShow >= 55
					|| yGuide <= 15 || yGuide >= 55) {
				setCheckResult(poiGeo, poiObj, poi.getMeshId(), null);
			}
		}
	}
}
