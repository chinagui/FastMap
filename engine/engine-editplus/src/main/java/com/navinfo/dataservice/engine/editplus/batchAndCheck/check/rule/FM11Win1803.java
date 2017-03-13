package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 检查条件： Lifecycle！=1（删除） 检查原则：
 * POI位移后，其子POI（未验证的内部POI）与其坐标不一致，
 * 报Log：未点开子POI位置和点开的父不同点
 * 备注：需要将报出的一组POI同时显示，界面上可以点父POI，直接移动到父POI点上
 * 
 * @author wangdongbin
 *
 */
public class FM11Win1803 extends BasicCheckRule {

	@SuppressWarnings("static-access")
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		// 已删除的数据不检查
		if (poi.getOpType().equals(OperationType.PRE_DELETED)) {
			return;
		}
		// POI无位移
		if (!poi.hisOldValueContains(IxPoi.GEOMETRY)) {
			return;
		}
		Set<Long> pidList = new HashSet<Long>();
		pidList.add(poi.getPid());
		List<Long> childPids = IxPoiSelector.getChildrenPidsByParentPid(getCheckRuleCommand().getConn(), pidList);
		// 不存在子poi
		if (childPids.size() == 0) {
			return;
		}
		PoiLogDetailStat logDetail = new PoiLogDetailStat();
		Map<Long,List<LogDetail>> submitLogs = logDetail.loadAllLog(getCheckRuleCommand().getConn(), childPids);
		for (Long logPid:submitLogs.keySet()) {
			if (childPids.contains(logPid)) {
				childPids.remove(logPid);
			}
		}
		List<Long> sameLocPids = new ArrayList<Long>();
		for (long pid:childPids) {
			BasicObj basicObj=ObjSelector.selectByPid(getCheckRuleCommand().getConn(), "IX_POI", null,true, pid, false);
			IxPoi childPoi = (IxPoi) basicObj.getMainrow();
			if (childPoi.getIndoor()==1) {
				if (!childPoi.getGeometry().equals(poi.getGeometry())) {
					sameLocPids.add(childPoi.getPid());
				}
			}
		}
		if (sameLocPids.size()>0) {
//			String err = StringUtils.join(sameLocPids, ",");
			String targets="[IX_POI,"+poi.getPid()+"]";
			for(Long pid2:sameLocPids){
				targets=targets+";[IX_POI,"+pid2+"]";
			}
			setCheckResult(poi.getGeometry(), targets,poi.getMeshId(), "未点开子POI位置和点开的父不同点");
			return;
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
