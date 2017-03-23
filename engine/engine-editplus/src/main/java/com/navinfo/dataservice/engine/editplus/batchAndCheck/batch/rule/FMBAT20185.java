package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 查询条件：
 * POI分类为230218且非删除且为父，根据父关联存在充电桩(IX_POI_CHARGINGPLOT.U_RECORD)信息的子(非删除)的所有记录总和N； 
 * 批处理：
 * 批处理该父POI的充电站IX_POI_CHARGINGSTATION.CHARGING_NUM=N; 并生成履历；
 * 
 * @author wangdongbin
 *
 */
public class FMBAT20185 extends BasicBatchRule {
	
	private Map<Long, List<Long>>  childrenMap = new HashMap<Long, List<Long>>();

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		childrenMap = IxPoiSelector.getChildrenPidsByParentPidList(getBatchRuleCommand().getConn(), pidList);
		
		Set<Long> childPids = new HashSet<Long>();
		for (Long parentPid:childrenMap.keySet()) {
			childPids.addAll(childrenMap.get(parentPid));
		}
		Set<String> referSubrow =  new HashSet<String>();
		referSubrow.add("IX_POI_CHARGINGPLOT");
		Map<Long, BasicObj> referObjs = getBatchRuleCommand().loadReferObjs(childPids, ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		if (!childrenMap.containsKey(poi.getPid()) || !poi.getKindCode().equals("230218")) {
			return;
		}
		List<IxPoiChargingstation>  charginstions = poiObj.getIxPoiChargingstations();
		if (charginstions.size() == 0) {
			return;
		}
		List<Long> childrenList = childrenMap.get(poi.getPid());
		List<IxPoiChargingplot> plotList = new ArrayList<IxPoiChargingplot>();
		
		for (Long childPid:childrenList) {
			BasicObj childObj = myReferDataMap.get(ObjectName.IX_POI).get(childPid);
			IxPoiObj child = (IxPoiObj) childObj;
			plotList.addAll(child.getIxPoiChargingplots());
		}
		for (IxPoiChargingstation station:charginstions) {
			station.setChargingNum(plotList.size());
		}

	}

}
