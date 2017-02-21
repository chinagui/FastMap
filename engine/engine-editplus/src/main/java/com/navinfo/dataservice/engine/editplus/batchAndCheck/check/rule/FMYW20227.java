package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件： 非删除（根据履历判断删除） 检查原则：
 * 1.收费标准（IX_POI_PARKING.TOLL_STD)为5时,停车备注(IX_POI_PARKING.REMARK)应该是“含0，
 * 且不含1、2、3、4、5、6”，否则报log:收费标准为免费，停车场备注不为无条件免费。
 * 
 * @author gaopengrong
 */
public class FMYW20227 extends BasicCheckRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub
	}

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

			if ("5".equals(tollStd)) {
				String remark = parking.getRemark();
				if (remark.indexOf("0") < 0) {
					setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
							"收费标准为免费，停车场备注不为无条件免费");
				} else if (remark.indexOf("1") >= 0 || remark.indexOf("2") >= 0 || remark.indexOf("3") >= 0
						|| remark.indexOf("4") >= 0 || remark.indexOf("5") >= 0 || remark.indexOf("6") >= 0) {
					setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
							"收费标准为免费，停车场备注不为无条件免费");
				}
			}

		}

	}
}
