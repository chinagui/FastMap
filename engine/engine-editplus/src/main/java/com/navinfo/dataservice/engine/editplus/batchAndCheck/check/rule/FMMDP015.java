package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiBusinesstime;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiDetail;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件：
 * 非删除（根据履历判断删除）
 * 检查原则：
 * 1.营业时长IX_POI_BUSINESSTIME表中的记录以POI_PID关联IX_POI_DETAIL表POI_PID，必须有关联记录
 * log1：BUSINESSTIME数据在其主表DETAIL中不存在
 */
public class FMMDP015 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		
		List<IxPoiBusinesstime> businesstimes = poiObj.getIxPoiBusinesstimes();
		List<IxPoiDetail> details = poiObj.getIxPoiDetails();
		
		if (businesstimes.size()>0&&details.size()==0) {
			this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), "BUSINESSTIME数据在其主表DETAIL中不存在；");
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
