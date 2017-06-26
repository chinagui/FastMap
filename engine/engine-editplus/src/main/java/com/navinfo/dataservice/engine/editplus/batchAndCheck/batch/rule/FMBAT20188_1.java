package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.ArrayList;
import java.util.Collection;
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
 * (1)新增POI或者修改IX_POI_CHARGINGPLOT(非删除)记录； 
 * (2)分类为POI230227；
 * (3)存在父分类为230218且为父为非删除，且SERVICE_PROV不为[null,0~18],即为chainID值；
 * 批处理：将该POI的OPEN_TYPE赋值查询出的chainID值；
 * 
 * @author sunjiawei
 *
 */
public class FMBAT20188_1 extends BasicBatchRule {

	private Map<Long,Long> childPidParentPid;
	private List<String> serviceList = new ArrayList<String>();
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		childPidParentPid = IxPoiSelector.getParentPidsByChildrenPids(getBatchRuleCommand().getConn(), pidList);
		
		Set<Long> parentPids = new HashSet<Long>();
		for (Long childPid:childPidParentPid.keySet()) {
			parentPids.add(childPidParentPid.get(childPid));
		}
		Set<String> referSubrow =  new HashSet<String>();
		referSubrow.add("IX_POI_CHARGINGSTATION");
		Map<Long, BasicObj> referObjs = getBatchRuleCommand().loadReferObjs(parentPids, ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
		
		serviceList.add("0");
		serviceList.add("1");
		serviceList.add("2");
		serviceList.add("3");
		serviceList.add("4");
		serviceList.add("5");
		serviceList.add("6");
		serviceList.add("7");
		serviceList.add("8");
		serviceList.add("9");
		serviceList.add("10");
		serviceList.add("11");
		serviceList.add("12");
		serviceList.add("13");
		serviceList.add("14");
		serviceList.add("15");
		serviceList.add("16");
		serviceList.add("17");
		serviceList.add("18");
		serviceList.add("19");
		serviceList.add("20");
		serviceList.add("21");
		serviceList.add("22");
		serviceList.add("23");
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		if (!childPidParentPid.containsKey(poi.getPid()) || !poi.getKindCode().equals("230227")) {
			return;
		}
		if(poi.getHisOpType().equals(OperationType.INSERT)||poi.getHisOpType().equals(OperationType.UPDATE)){

			Long parentPid = childPidParentPid.get(poi.getPid());
			
			BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentPid);
			IxPoiObj parentPoiObj = (IxPoiObj) parentObj;
			IxPoi parentPoi = (IxPoi) parentPoiObj.getMainrow();
			if (!parentPoi.getKindCode().equals("230218") || parentPoi.getHisOpType().equals(OperationType.DELETE)) {
				return;
			}
			List<IxPoiChargingstation> chargingStationList = parentPoiObj.getIxPoiChargingstations();
			if (chargingStationList==null||chargingStationList.isEmpty()) {
				return;
			}
			IxPoiChargingstation chargingStation = chargingStationList.get(0);
			String serviceProv = chargingStation.getServiceProv();
			if (serviceProv == null || serviceList.contains(serviceProv)) {
				return;
			}
			List<IxPoiChargingplot> chargingPlotsList = poiObj.getIxPoiChargingplots();
			for (IxPoiChargingplot plot:chargingPlotsList) {
				plot.setOpenType(serviceProv);
			}
		}
	}

}
