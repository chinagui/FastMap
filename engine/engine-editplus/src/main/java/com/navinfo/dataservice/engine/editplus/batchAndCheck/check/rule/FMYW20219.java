package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiDetail;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 通用深度信息传真个数检查 检查条件：非删除（根据履历判断删除）
 * 检查原则：1.传真个数（IX_POI_DETAIL.FAX以“|”拆分后）不能超过3个;2.多个传真时不能重复； Log1：传真个数超过三个；Log2：传真重复
 */
public class FMYW20219 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		List<IxPoiDetail> details = poiObj.getIxPoiDetails();
		
		for (IxPoiDetail detail : details) {
			int count = 0;
			Set<String> tels= new HashSet<String>();
				
			String fax = detail.getFax();
			if (fax == null) {
				continue;
			}
			String[] numbers=fax.split("\\|");
			count=numbers.length;
			for (String oneNumber : numbers) {
				tels.add(oneNumber);
			}
			
			if (count > 3) {
				this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), "传真个数超过三个；");
			}
			
			if (count > tels.size()) {
				this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), "传真重复；");
			}
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
