package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 通用深度信息传真个数检查 检查条件：非删除（根据履历判断删除）
 * 检查原则：传真个数（IX_POI_CONTACT.CONTACT_TYPE=11）不能超过3个； Log：传真个数超过三个；
 */
public class FMYW20219 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		List<IxPoiContact> contacts = poiObj.getIxPoiContacts();

		int count = 0;
		for (IxPoiContact contact : contacts) {
			if (contact.getContactType() == 11) {
				count += 1;
			}
		}

		if (count > 3) {
			this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), "传真个数超过三个；");
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
