package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParking;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 
 * 检查条件： 非删除（根据履历判断删除） 检查原则：
 * 1.停车场停车备注的值没有"|"且不为空时，值只能在{0,1,2,3,4,5,6,7,11,12,14,16,17,18}中；
 * 2.停车场停车备注的值有"|"时，"|"前后的值必须在{0,1,2,3,4,5,6,7,11,12,14,16,17,18}中；
 * 3.停车场停车备注的值有"|"时，"|"前后的值只能出现一次。
 * 
 * Log1：停车场停车备注的值没有"|"且不为空时，值不在{0,1,2,3,4,5,6,7,11,12,14,16,17,18}中；
 * Log2：停车场停车备注的值有"|"时，"|"前后的值不在{0,1,2,3,4,5,6,7,11,12,14,16,17,18}中；
 * log3：（停车场停车备注）重复。
 * 
 * @author gaopengrong
 */
public class FMZY20155 extends BasicCheckRule {

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
		List<String> defaultList = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "11", "12", "14", "16", "17",
				"18");
		for (IxPoiParking parking : parkings) {
			String remark = parking.getRemark();
			if (remark == null) {
				continue;
			}
			if (!remark.isEmpty() && remark.indexOf("|") < 0) {
				for (char c : remark.toCharArray()) {
					if (!defaultList.contains(String.valueOf(c))) {
						setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
								"停车场停车备注的值没有'| '且不为空时，值不在{0,1,2,3,4,5,6,7,11,12,14,16,17,18}中");
						break;
					}
				}
			} else if (remark.indexOf("|") >= 0) {
				String[] remarks = remark.split("\\|");
				for (int i=0;i<remarks.length;i++) {
					String tempRemark = remarks[i];
					if (!defaultList.contains(tempRemark)) {
						setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
								"停车场停车备注的值有'|'时，'|'前后的值不在{0,1,2,3,4,5,6,7,11,12,14,16,17,18}中");
						break;
					}
					for (int j=i+1;j<remarks.length;j++) {
						if (tempRemark.equals(remarks[j])) {
							setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
									"（停车场停车备注）重复");
							break;
						}
					}
				}
			}
		}
	}
}
