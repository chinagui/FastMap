package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

public class FMZY20151 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}

		List<IxPoiParking> parkings = poiObj.getIxPoiParkings();
		for (IxPoiParking parking : parkings) {
			String tollWay = parking.getTollWay();
			String errorLog = new String();
			if (StringUtils.isNotEmpty(tollWay)) {
				String value = "0,1,2";
				if (!tollWay.contains("|") && !value.contains(tollWay)) {
					errorLog = "收费方式的值没有" + "'" + "|" + "'" + "且不为空时，" + "值不在{0,1,2}中";
				}
				if (tollWay.contains("|")) {
					StringBuffer sf = new StringBuffer();
					String[] tollWayArry = tollWay.split("\\|");
					for (String toll : tollWayArry) {

						if (StringUtils.isEmpty(toll) || !"0,1,2".contains(toll)) {
							errorLog = "收费方式的值有" + "'" + "|" + "'时，" + "'" + "|" + "'" + "前后的值不在{0,1,2}中";
						}
						if (sf.toString().contains(toll)) {
							errorLog = "（收费方式）重复";
						}
						sf.append(toll + ",");
					}
				}
			}
			if (StringUtils.isNotEmpty(errorLog)) {
				this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), errorLog);
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
