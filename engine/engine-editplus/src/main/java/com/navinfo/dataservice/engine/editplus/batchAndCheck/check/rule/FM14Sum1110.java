package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 
 * 检查条件：Lifecycle为“1（删除）”不检查； 检查原则： 没有加油站子POI的服务区。 服务区分类：230206
 * 加油站：230215\230216\230217\230218
 */
public class FM14Sum1110 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if (!kindCode.equals("230206")) {
				return;
			}
			Set<Long> parentPids = new HashSet<Long>();
			parentPids.add(poi.getPid());
			List<Long> childrenPids = IxPoiSelector.getChildrenPidsByParentPid(getCheckRuleCommand().getConn(),parentPids);
			if (childrenPids.size()==0) {
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "服务区子POI没有加油站。");
				return;
			}
			boolean hasChild = false;
			for (Long childPid : childrenPids) {
				BasicObj childobj = ObjSelector.selectByPid(getCheckRuleCommand().getConn(), "IX_POI", null, true,childPid, false);
				IxPoiObj childPoiObj = (IxPoiObj) childobj;
				IxPoi childPoi = (IxPoi) childPoiObj.getMainrow();
				String childKindCode = childPoi.getKindCode();
				if (childKindCode.equals("230215") || childKindCode.equals("230216") || childKindCode.equals("230217") || childKindCode.equals("230218")) {
					hasChild = true;
					break;
				}
			}
			Set<Long> pidsC = new HashSet<Long>();
			pidsC.addAll(childrenPids);
			if (!hasChild) {
				String targets="[IX_POI,"+poi.getPid()+"]";
				for(Long pid2:pidsC){
					targets=targets+";[IX_POI,"+pid2+"]";
				}
				setCheckResult(poi.getGeometry(), targets, poi.getMeshId(), "服务区子POI没有加油站。");
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
