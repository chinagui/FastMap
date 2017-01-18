package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * 在IX_POI_CHILDREN和IX_POI_PARENT两个表中，
 * GROUP_ID相同的两条记录的CHILD_POI_PID和PARENT_POI_PID字段不能相等
 *
 */
public class GLM60025 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			List<Long> parentGroupIds = CheckUtil.getParentGroupIds(poi.getPid(), getCheckRuleCommand().getConn());
			List<Long> childGroupIds = CheckUtil.getChildGroupIds(poi.getPid(), getCheckRuleCommand().getConn());
			if (parentGroupIds.size() == 0 || childGroupIds.size() == 0) {
				return;
			}
			for (Long parent : parentGroupIds) {
				if (childGroupIds.contains(parent)) {
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"GROUP_ID相同的两条记录的CHILD_POI_PID和PARENT_POI_PID字段不能相等");
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
