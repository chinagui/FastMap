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
 * 检查对象： Lifecycle！=1（删除） 检查原则：
 * 1）分类为230210停车场、230213换乘停车场（P+R停车场）、230214货车专用停车场时，只能做为230218电动汽车充电站的父，否则报log1
 * ：停车场的子POI不是电动汽车充电站
 * 2）如果分类不是230218电动汽车充电站，存在父子关系时，其父POI的分类不能是230210停车场、230213换乘停车场（P+R停车场）、
 * 230214货车专用停车场，否则报log2：非电动汽车充电站分类的父POI不能是停车场
 *
 */
public class FMYW20030 extends BasicCheckRule {

	Map<Long, Long> parentIds = new HashMap<Long, Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if (kindCode.equals("230210") || kindCode.equals("230213") || kindCode.equals("230214")) {
				Set<Long> parentPids = new HashSet<Long>();
				parentPids.add(poi.getPid());
				List<Long> childrenPids = IxPoiSelector.getChildrenPidsByParentPid(getCheckRuleCommand().getConn(),
						parentPids);
				for (Long childPid : childrenPids) {
					BasicObj childobj = ObjSelector.selectByPid(getCheckRuleCommand().getConn(), "IX_POI", null, true,
							childPid, false);
					IxPoiObj childPoiObj = (IxPoiObj) childobj;
					IxPoi childPoi = (IxPoi) childPoiObj.getMainrow();
					if (!childPoi.getKindCode().equals("230218")) {
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "停车场的子POI不是电动汽车充电站");
						return;
					}
				}
			} else {
				if (kindCode.equals("230218")) {
					return;
				}
				if (parentIds == null || !parentIds.containsKey(poi.getPid())) {
					return;
				}
				Long parentPid = parentIds.get(poi.getPid());
				BasicObj parentobj = ObjSelector.selectByPid(getCheckRuleCommand().getConn(), "IX_POI", null, true,
						parentPid, false);
				IxPoiObj parentPoiObj = (IxPoiObj) parentobj;
				IxPoi parentPoi = (IxPoi) parentPoiObj.getMainrow();
				if (parentPoi.getKindCode().equals("230210") || parentPoi.getKindCode().equals("230213") || parentPoi.getKindCode().equals("230214")) {
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "非电动汽车充电站分类的父POI不能是停车场");
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
