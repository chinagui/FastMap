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
 * 	查询条件：
	(1)POI分类为230227且为子且非删除；
	(2)POI分类为230218且非删除且为父，且SERVICE_PROV不为[null,0~23],即为品牌chainID值；
	满足(1)或(2)时，查询父的SERVICE_PROV.chainID，分别判断父的SERVICE_PROV.chainID与子openType.chainID一致，如果一致，则不批处理，如果不一致，则批处理；
	批处理：
	(1)若子的openType=1(对所有车辆开放),则不批处理；
	(2)若子的openType!=1(对所有车辆开放),且openType不包含chainID中的任何一个，则opentType直接追加父的SERVICE_PROV.chainID,多个值|分隔；
	(3)若子的openType!=1(对所有车辆开放),且openType包含chainID其中一个，判断父与子的chainID值是否一致，如果一致，则不批处理，如果不一致，则替换子的chainID；
	并生成履历；
 * @author sunjiawei
 *
 */
public class FMBAT20188 extends BasicBatchRule {

	private Map<Long,Long> childPidParentPid;
	private Map<Long, List<Long>> childrenMap;
	private boolean isParent = false;
	private boolean isChild = false;
	private List<String> serviceList = new ArrayList<String>();
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		childPidParentPid = IxPoiSelector.getParentPidsByChildrenPids(getBatchRuleCommand().getConn(), pidList);
		childrenMap = IxPoiSelector.getChildrenPidsByParentPidList(getBatchRuleCommand().getConn(), pidList);
		
		Set<Long> parentPids = new HashSet<Long>();
		
		if(childPidParentPid.isEmpty()&&childrenMap.isEmpty()){return;}
		if(!childPidParentPid.isEmpty()&&childrenMap.isEmpty()){
			isChild=true;
			for (Long childPid:childPidParentPid.keySet()) {
				parentPids.add(childPidParentPid.get(childPid));
			}
		}
		if(childPidParentPid.isEmpty()&&!childrenMap.isEmpty()){
			isParent=true;
			for (Long childPid:childrenMap.keySet()) {
				parentPids.addAll(childrenMap.get(childPid));
			}
		}
		
		Set<String> referSubrow =  new HashSet<String>();
		referSubrow.add("IX_POI_CHARGINGPLOT");
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
		if(isParent){
			System.out.println("--------FMBAT20188父触发批处理开始-----------");
			if (!childrenMap.containsKey(poi.getPid()) || !poi.getKindCode().equals("230218")
					||poi.getHisOpType().equals(OperationType.DELETE)) {
				System.out.println("--------FMBAT20188父不符合触发条件-----------");
				return;
			}
	
			List<Long> childrenList  =  childrenMap.get(poi.getPid());
			
			List<IxPoiChargingstation> chargingStationList = poiObj.getIxPoiChargingstations();
			if (chargingStationList==null||chargingStationList.isEmpty()) {
				System.out.println("--------FMBAT20188父表为空-----------");
				return;
			}
			IxPoiChargingstation chargingStation = chargingStationList.get(0);
			String serviceProv = chargingStation.getServiceProv();
			if (serviceProv == null || serviceList.contains(serviceProv) || serviceProv.length()!=4) {
				System.out.println("--------FMBAT20188 serviceProv不符合触发条件-----------");
				return;
			}
			
			for (Long childPid:childrenList) {
				BasicObj childObj = myReferDataMap.get(ObjectName.IX_POI).get(childPid);
				IxPoiObj child = (IxPoiObj) childObj;
				IxPoi childPoi = (IxPoi)  child.getMainrow();
				batchChildOpenType(child, childPoi, serviceProv);
			}
			System.out.println("--------FMBAT20188父触发批处理完成-----------");
		
		}
		if(isChild){
			System.out.println("--------FMBAT20188子触发批处理开始-----------");
			if (!childPidParentPid.containsKey(poi.getPid()) || !poi.getKindCode().equals("230227")
					||poi.getHisOpType().equals(OperationType.DELETE)) {
				System.out.println("--------FMBAT20188子不符合触发条件-----------");
				return;
			}

			Long parentPid = childPidParentPid.get(poi.getPid());
			
			BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentPid);
			IxPoiObj parentPoiObj = (IxPoiObj) parentObj;
			IxPoi parentPoi = (IxPoi) parentPoiObj.getMainrow();
			if (!parentPoi.getKindCode().equals("230218") || parentPoi.getHisOpType().equals(OperationType.DELETE)) {
				System.out.println("--------FMBAT20188 子对应的父不符合触发条件-----------");
				return;
			}
			List<IxPoiChargingstation> chargingStationList = parentPoiObj.getIxPoiChargingstations();
			if (chargingStationList==null||chargingStationList.isEmpty()) {
				System.out.println("--------FMBAT20188父表为空-----------");
				return;
			}
			IxPoiChargingstation chargingStation = chargingStationList.get(0);
			String serviceProv = chargingStation.getServiceProv();
			if (serviceProv == null || serviceList.contains(serviceProv)|| serviceProv.length()!=4) {
				System.out.println("--------FMBAT20188 serviceProv不符合触发条件-----------");
				return;
			}
			batchChildOpenType(poiObj, poi, serviceProv);
			System.out.println("--------FMBAT20188子触发批处理完成-----------");
		}
		
	}
	
	public void batchChildOpenType(IxPoiObj child,IxPoi childPoi,String serviceProv){
		if(childPoi.getKindCode().equals("230227")&&(!childPoi.getHisOpType().equals(OperationType.DELETE))){
			List<IxPoiChargingplot> chargingPlotsList = child.getIxPoiChargingplots();
			for (IxPoiChargingplot plot:chargingPlotsList) {
				String openType = plot.getOpenType();
				if(!openType.equals("1")){//(1)若子的openType=1(对所有车辆开放),则不批处理；
					String[] openTypeArray = openType.split("\\|");
					boolean hasChain = false;
					String childChain = "";
					for (String chOpenType : openTypeArray) {
						if(chOpenType.length()==4){
							hasChain = true;
							childChain = chOpenType;
							break;
						}
					}
					if(!hasChain){//(2)若子的openType!=1(对所有车辆开放),且openType不包含chainID中的任何一个，则opentType直接追加父的SERVICE_PROV.chainID,多个值|分隔；
						plot.setOpenType(openType+"|"+serviceProv);
					}else{
						if(!serviceProv.equals(childChain)){//(3)若子的openType!=1(对所有车辆开放),且openType包含chainID其中一个，判断父与子的chainID值是否一致，如果一致，则不批处理，如果不一致，则替换子的chainID；
							plot.setOpenType(openType.replace(childChain, serviceProv));
						}
					}
				}
			}
		}
	}

}
