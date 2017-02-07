package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 检查条件：Lifecycle为“1（删除）”不检查； 检查原则： 没有子设施的机场设施（分类：230126）。
 *
 */
public class FM14Sum1113 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if (!kindCode.equals("230126")) {
				return;
			}
			Set<Long> parentPids = new HashSet<Long>();
			parentPids.add(poi.getPid());
			List<Long> childrenPids = IxPoiSelector.getChildrenPidsByParentPid(getCheckRuleCommand().getConn(),parentPids);
			if (childrenPids.size()==0) {
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "机场没有子设施");
				return;
			}
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
