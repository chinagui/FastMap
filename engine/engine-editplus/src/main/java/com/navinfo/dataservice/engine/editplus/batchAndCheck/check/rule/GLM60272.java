package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxSamepoiPart;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxSamePoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 
 * 检查对象： 
 * IX_POI中状态不为删除（state不为1）的POI 检查原则 
 * 1.两个POI既做了父子关系也制作了同一关系，报LOG
 * 注：父子关系和同一关系必须是成组出现 
 * 2.两个POI做了同一关系,并且他们存在于多级父子关系组中,报LOG.
 * 例如:A.B制作了同一关系,一组父子关系为A->C->B,则认为A.B之间存在父子关系.
 *
 * @author wangdongbin
 */
public class GLM60272 extends BasicCheckRule {

	Map<Long, Long> childParentMap = null;

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_SAMEPOI)) {
			IxSamePoiObj poiObj = (IxSamePoiObj) obj;
			List<IxSamepoiPart> parts = poiObj.getIxSamepoiParts();
			List<Long> samePoiPids = new ArrayList<>();
			for (IxSamepoiPart tmp : parts) {
				samePoiPids.add(tmp.getPoiPid());
			}
			Set<Long> childSet = childParentMap.keySet();
			// 父子关系列表
			Set<Long> allChildParent = new HashSet<Long>();
			for (IxSamepoiPart tmp : parts) {
				Long pid = tmp.getPoiPid();
				if (allChildParent.contains(pid)) {
					String targets = "";
					for (Long tmpPid : allChildParent) {
						if (!StringUtils.isEmpty(targets)) {
							targets = targets + ";";
						}
						targets = targets + "[IX_POI," + tmpPid + "]";
					}
					setCheckResult("", targets, 0, null);
					return;
				}
				allChildParent.add(pid);
				Long childPid = pid;
				int num = 0;
				while (childParentMap.containsKey(childPid)) {
					Long parentPid = childParentMap.get(pid);
					if (allChildParent.contains(parentPid) && !childSet.containsAll(samePoiPids)) {
						String targets = "";
						for (Long tmpPid : allChildParent) {
							if (!StringUtils.isEmpty(targets)) {
								targets = targets + ";";
							}
							targets = targets + "[IX_POI," + tmpPid + "]";
						}
						setCheckResult("", targets, 0, null);
						return;
					}
					allChildParent.add(parentPid);
					childPid = parentPid;
					num++;
					if (num == 6) {
						log.info("存在父子关系死循环，规则直接返回");
						return;
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList = new HashSet<Long>();
		for (BasicObj obj : batchDataList) {
			IxSamePoiObj poiObj = (IxSamePoiObj) obj;
			List<IxSamepoiPart> parts = poiObj.getIxSamepoiParts();
			for (IxSamepoiPart tmp : parts) {
				pidList.add(tmp.getPoiPid());
			}
		}
		childParentMap = IxPoiSelector.getAllParentChildByPids(getCheckRuleCommand().getConn(), pidList);
	}

}
