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
 * FM-BAT-20-188
	查询条件：
	 POI分类为230218且为父且非删除，且SERVICE_PROV不为[null,0~23],即为品牌chainID值；
	批处理：
	 将该POI的所有子POI(非删除)的OPEN_TYPE赋值查询出的chainID值；
	并生成履历；
 * @author sunjiawei
 *
 */
public class FMBAT20188 extends BasicBatchRule {

	private Map<Long, List<Long>>  childrenMap = new HashMap<Long, List<Long>>();
	private List<String> serviceList = new ArrayList<String>();
	
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
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		if (!childrenMap.containsKey(poi.getPid()) || !poi.getKindCode().equals("230218")) {
			return;
		}
		if(poi.getHisOpType().equals(OperationType.INSERT)||poi.getHisOpType().equals(OperationType.UPDATE)){

			List<Long> childrenList  =  childrenMap.get(poi.getPid());
			
			List<IxPoiChargingstation> chargingStationList = poiObj.getIxPoiChargingstations();
			if (chargingStationList.size() == 0) {
				return;
			}
			IxPoiChargingstation chargingStation = chargingStationList.get(0);
			String serviceProv = chargingStation.getServiceProv();
			if (serviceProv == null || serviceList.contains(serviceProv)) {
				return;
			}
			
			for (Long childPid:childrenList) {
				BasicObj childObj = myReferDataMap.get(ObjectName.IX_POI).get(childPid);
				IxPoiObj child = (IxPoiObj) childObj;
				List<IxPoiChargingplot> chargingPlotsList = child.getIxPoiChargingplots();
				for (IxPoiChargingplot plot:chargingPlotsList) {
					plot.setOpenType(serviceProv);
				}
			
			}
		}
	}

}
