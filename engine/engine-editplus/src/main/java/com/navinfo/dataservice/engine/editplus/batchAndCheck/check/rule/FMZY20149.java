package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

public class FMZY20149 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}

		List<IxPoiParking> parkings = poiObj.getIxPoiParkings();
		for (IxPoiParking parking : parkings) {
			String tollStd = parking.getTollStd();
			String errorLog = new String();
			if (StringUtils.isNotEmpty(tollStd)) {
				// 字符串只能含有5或{0,1,2,3,4,|}
				if (tollStd.contains("5") && tollStd.length() != 1) {
					errorLog = "收费标准" + "'" + "5" + "'" + "只能单独存在，且不能重复";
				}
				// 收费标准的值没有'|'且不为空时，值不在{0,1,2,3,4,5}中
				String value = "0,1,2,3,4,5";
				if (!tollStd.contains("|") && !value.contains(tollStd)) {
					errorLog = "收费标准的值没有" + "'" + "|" + "'" + "且不为空时，" + "值不在{0,1,2,3,4,5}中";
				}
				if (tollStd.contains("|")) {
					StringBuffer sf = new StringBuffer();
					String[] tollStdArry = tollStd.split("\\|");
					for (String toll : tollStdArry) {

						if (StringUtils.isEmpty(toll) || !"0,1,2,3,4".contains(toll)) {
							errorLog = "收费标准的值有" + "'" + "|" + "'时，" + "'" + "|" + "'" + "前后的值不在{0,1,2,3,4}中";
						}
						if (sf.toString().contains(toll)) {
							errorLog = "（收费标准）重复";
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
