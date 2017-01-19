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
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 检查条件：Lifecycle为“1（删除）”不检查； 
 * 检查原则： 
 * 加油站（分类：230215）与加气站（分类：230216）建立了父子关系的设施
 *
 */
public class FM14Sum1114 extends BasicCheckRule {
	
	Map<Long, Long> parentIds = new HashMap<Long, Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			
			if (!poi.getKindCode().equals("230215") || !poi.getKindCode().equals("230216")) {
				return;
			}
			
			if (parentIds==null || !parentIds.containsKey(poi.getPid())) {
				return;
			}
			Long parentPid = parentIds.get(poi.getPid());
			BasicObj parentobj = ObjSelector.selectByPid(getCheckRuleCommand().getConn(), "IX_POI", null, true,
					parentPid, false);
			IxPoiObj parentPoiObj = (IxPoiObj) parentobj;
			IxPoi parentPoi = (IxPoi) parentPoiObj.getMainrow();
			if (poi.getKindCode().equals("230215")) {
				if (parentPoi.getKindCode().equals("230216")) {
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"加油站与加气站做了父子关系");
				}
			} else if (poi.getKindCode().equals("230216")) {
				if (parentPoi.getKindCode().equals("230215")) {
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"加油站与加气站做了父子关系");
				}
			}
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
