package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingstation;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;


/**
 * 查询条件：
 * 当充电站(非删除)下有多个非删除子(充电桩)时，进行如下批处理：
 * 同一组相同规格电桩判断原则：PLUG_TYPE（插头类型）、ACDC（交直流充电）、MODE（充电模式），
 * OPEN_TYPE（开放状态）一致的时定义为一组规格相同的充电桩； 
 * 批处理： 
 * GROUPID赋值原则：
 * 有多组不同规格的充电桩时，GROUP_ID的值从1开始顺序递增；如果原GROUP_ID已经存在大于1的值，
 * 则后续新增的GROUPID的值以最大值+1顺序递增； 
 * COUNT赋值原则：
 * 计算同一组相同规格的充电桩（即GROUPID相同）的总数量，同一组桩中分别赋值给IX_POI_CHARGINGPLOT.COUNT
 * 
 * @author wangdongbin
 *
 */
public class FMBAT20187 extends BasicBatchRule {
	
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
		//要修改子信息，所以此处isLock=true
		//由于提交已加锁，若批处理锁的childPids,和提交pidList有交集，就会发生死锁，jch update by 20170904
		pidList.retainAll(childPids);
		childPids.removeAll(pidList);
		
		//pidList为外层已经加锁的pid
		Map<Long, BasicObj> referObjs = getBatchRuleCommand().loadReferObjs(pidList, ObjectName.IX_POI, referSubrow, false);
		//childPids为还未加锁的pid
		Map<Long, BasicObj> referObjsSecond = getBatchRuleCommand().loadReferObjs(childPids, ObjectName.IX_POI, referSubrow, true);
		referObjs.putAll(referObjsSecond);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
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
			BasicObj childObj=myReferDataMap.get(ObjectName.IX_POI).get(childPid);
			IxPoiObj child = (IxPoiObj) childObj;
			plotList.addAll(child.getIxPoiChargingplots());
		}
		detealNewData(plotList);
	}
	
	/**
	 * 重新分组所有桩
	 * @param maxGroupId
	 * @param newPlotsList
	 * @return
	 * @throws Exception
	 */
	public static void detealNewData(List<IxPoiChargingplot> newPlotsList) throws Exception {
		Map<Integer,List<IxPoiChargingplot>> newPlotsMap = new HashMap<Integer,List<IxPoiChargingplot>>();
		int maxGroupId = 1;
		// 将新增的数据分组
		for (IxPoiChargingplot newPlots:newPlotsList) {
			if (newPlotsMap.containsKey(maxGroupId)) {
				boolean flag = false;
				for (int tempGroupId:newPlotsMap.keySet()) {
					IxPoiChargingplot tempPlot = newPlotsMap.get(tempGroupId).get(0);
					// 判断是否同规格
					if (newPlots.getPlugType().equals(tempPlot.getPlugType())&&newPlots.getAcdc()==tempPlot.getAcdc()&&newPlots.getMode()==tempPlot.getMode()&&newPlots.getOpenType().equals(tempPlot.getOpenType())) {
						flag = true;
						List<IxPoiChargingplot> tempList = newPlotsMap.get(tempPlot.getGroupId());
						newPlots.setGroupId(tempPlot.getGroupId());
						tempList.add(newPlots);
						newPlotsMap.put(tempPlot.getGroupId(), tempList);
						break;
					} 
				}
				if (!flag) {
					maxGroupId += 1;
					List<IxPoiChargingplot> tempList = new ArrayList<IxPoiChargingplot>();
					newPlots.setGroupId(maxGroupId);
					tempList.add(newPlots);
					newPlotsMap.put(maxGroupId, tempList);
				}
			} else {
				List<IxPoiChargingplot> tempList = new ArrayList<IxPoiChargingplot>();
				newPlots.setGroupId(maxGroupId);
				tempList.add(newPlots);
				newPlotsMap.put(maxGroupId, tempList);
			}
		}
		
		// 更改count
		for (Integer groupId:newPlotsMap.keySet()) {
			List<IxPoiChargingplot> tempList = newPlotsMap.get(groupId);
			for (IxPoiChargingplot temp:tempList) {
				temp.setCount(tempList.size());
			}
		}
	}

}
