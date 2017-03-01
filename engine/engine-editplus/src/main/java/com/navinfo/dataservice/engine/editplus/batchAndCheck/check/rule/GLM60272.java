package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
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
	
	private Map<Long,Long> samePoiPid = new HashMap<Long,Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		// 已删除的数据不检查
		if (poi.getOpType().equals(OperationType.PRE_DELETED)) {
			return;
		}

		Set<Long> pidList = new HashSet<Long>();
		pidList.add(poi.getPid());
		
		
		// 无同一关系
		if (!samePoiPid.containsKey(poi.getPid())) {
			return;
		}
		
		Map<Long,Long> parentPids = IxPoiSelector.getAllParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		List<Long> childPids = IxPoiSelector.getAllChildPidsByParentPid(getCheckRuleCommand().getConn(), pidList);
		// 不存在父子关系
		if (parentPids.size() == 0 && childPids.size() == 0) {
			return;
		}
		List<Long> allPids = new ArrayList<Long>();
		for (Long parentPid:parentPids.keySet()) {
			allPids.add(parentPid);
		}
		allPids.addAll(childPids);
		if (allPids.contains(samePoiPid.get(poi.getPid()))) {
			setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
			return;
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		samePoiPid = IxPoiSelector.getSamePoiPidsByThisPids(getCheckRuleCommand().getConn(), pidList);
	}

}
