package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 检查条件：Lifecycle为“0（无）\1（删除）\4（验证）”不检查； 检查原则： 分类为“230111”的POI不能为子；
 *
 */
public class FM14Sum1105 extends BasicCheckRule {

	Map<Long, Long> parentIds = new HashMap<Long, Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			if (parentIds == null || !parentIds.containsKey(poi.getPid())) {
				return;
			}
			setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "分类为“230111”的POI不能为子");
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList = new HashSet<Long>();
		for (BasicObj obj : batchDataList) {
			pidList.add(obj.objPid());
		}
		parentIds = IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);

	}

}
