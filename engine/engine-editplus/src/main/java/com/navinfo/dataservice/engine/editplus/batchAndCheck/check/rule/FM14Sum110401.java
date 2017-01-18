package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 
 * 检查条件：Lifecycle为“0（无）\1（删除）\4（验证）”不检查； 检查原则：
 * 分类为170101\170102的设施名字中不包含“门”或“門”与170101\170102的设施建立了父子关系。
 *
 */
public class FM14Sum110401 extends BasicCheckRule {

	Map<Long, Long> parentIds = new HashMap<Long, Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			if (!poi.getKindCode().equals("170101") && !poi.getKindCode().equals("170102")) {
				return;
			}
			String name = poiObj.getOfficeOriginCHName().getName();

			if (name.indexOf("门") >= 0 || name.indexOf("門") >= 0) {
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
			if (parentPoi.getKindCode().equals("170101") || parentPoi.getKindCode().equals("170102")) {
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),
						"分类为综合医院（170101）,专科医院（170102）的设施名字中不包含“门”，但与综合医院,专科医院的设施建立了父子关系。");
				return;
			}
			Set<Long> parentPids = new HashSet<Long>();
			parentPids.add(poi.getPid());
			List<Long> childrenPids = IxPoiSelector.getChildrenPidsByParentPid(getCheckRuleCommand().getConn(),
					parentPids);
			if (childrenPids.size() == 0) {
				return;
			}
			for (Long childPid : childrenPids) {
				BasicObj childobj = ObjSelector.selectByPid(getCheckRuleCommand().getConn(), "IX_POI", null, true,
						childPid, false);
				IxPoiObj childPoiObj = (IxPoiObj) childobj;
				IxPoi childPoi = (IxPoi) childPoiObj.getMainrow();
				if (childPoi.getKindCode().equals("170101") || childPoi.getKindCode().equals("170102")) {
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),
							"分类为综合医院（170101）,专科医院（170102）的设施名字中不包含“门”，但与综合医院,专科医院的设施建立了父子关系。");
					return;
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
