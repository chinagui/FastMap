package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChildren;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
	查询条件：
	 分类为230215的poi，且存在子（既该POI为父）且下面条件满足其中之一时：
	1）该poi为新增；
	2）该POI为修改且存在改父子关系或改分类；
	3）该POI的子存在修改分类，修改后的分类存在(130105,140203,140302,210215,110101,110102,110103,110200,110301,110302,110303,110304,120101,120102,120103,120104,120201,120202)；
	批处理：
	批处理时，如果父不存在深度信息记录，则增加一条深度信息记录，其它字段赋默认值，service赋值原则如下：
	(1)如果该POI是大陆数据,则根据不同分类针对IX_POI_GASSTATION.SERVICE如下批处理，并生成履历：
	 ①子POI分类中有130105时，如果SERVICE为空，则SERVICE赋值为1；如果SERVICE不为空且不包含1时，则在值后面增加1，并用"|"分割；如果SERVICE不为空且包含1时，则不处理；
	 ②子POI的分类中有140203时，如果SERVICE为空，则SERVICE赋值为2；如果SERVICE不为空且不包含2时，则在值后面增加2，并用"|"分割；如果SERVICE不为空且包含2时，则不处理；
	 ③子POI的分类中有140302时，如果SERVICE为空，则SERVICE赋值为3；如果SERVICE不为空且不包含3时，则在值后面增加3，并用"|"分割；如果SERVICE不为空且包含3时，则不处理;
	 ④子POI的分类中有210215时，如果SERVICE为空，则SERVICE赋值为4；如果SERVICE不为空且不包含4时，则在值后面增加4，并用"|"分割；如果SERVICE不为空且包含4时，则不处理；
	 ⑤子POI的分类中有{110101,110102,110103,110200,110301,110302,110303,110304}中任意一个时，如果SERVICE为空，则SERVICE赋值为5；如果SERVICE不为空且不包含5时，则在值后面增加5，并用"|"分割；如果SERVICE不为空且包含5时，则不处理；
	⑥子POI的分类中有{120101,120102,120103,120104,120201,120202}时，如果SERVICE为空，则SERVICE赋值为6；如果SERVICE不为空且不包含6时，则在值后面增加6，并用"|"分割；如果SERVICE不为空且包含6时，则不处理；
	(2)如果该POI是港澳数据,则根据不同分类针对IX_POI_GASSTATION.SERVICE如下批处理，并生成履历：
	 ①子POI的分类中有140203时，如果SERVICE为空，则SERVICE赋值为12；如果SERVICE不为空且不包含12时，则在值后面增加12，并用"|"分割；如果SERVICE不为空且包含12时，则不处理；
	 ②子POI的分类中有130105时，如果SERVICE为空，则SERVICE赋值为13；如果SERVICE不为空且不包含13时，则在值后面增加13，并用"|"分割；如果SERVICE不为空且包含13时，则不处理；
	 ③子POI的分类中有210215时，如果SERVICE为空，则SERVICE赋值为14；如果SERVICE不为空且不包含14时，则在值后面增加14，并用"|"分割；如果SERVICE不为空且包含14时，则不处理；
	以上，均生成履历；
 * 
 * @author sunjiawei
 *
 */
public class FMBAT20194 extends BasicBatchRule {
	
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
		
		Map<Long, BasicObj> referObjs = getBatchRuleCommand().loadReferObjs(childPids, ObjectName.IX_POI, null, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		if (poiObj.getIxPoiParents()==null||!(poiObj.getIxPoiParents().size()>0)) {
			return;
		}
		
		if (!childrenMap.containsKey(poi.getPid()) || !poi.getKindCode().equals("230215")) {
			return;
		}
		
		List<Long> childrenList = childrenMap.get(poi.getPid());
		
		boolean flag = true;
		
		if(poi.getHisOpType().equals(OperationType.INSERT)){
			flag = true;
		}
		
		if(poi.getHisOpType().equals(OperationType.UPDATE)){
			for (Long childPid:childrenList) {
				BasicObj childObj=myReferDataMap.get(ObjectName.IX_POI).get(childPid);
				IxPoi childPoi = (IxPoi) childObj.getMainrow();
				if(childPoi.getHisOpType().equals(OperationType.INSERT)||
						childPoi.getHisOpType().equals(OperationType.UPDATE)||
						childPoi.getHisOpType().equals(OperationType.DELETE)){
					flag = true;
					break;
				}
			}
			
			if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
				String newKindCode=poi.getKindCode();
				String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
				if(!newKindCode.equals(oldKindCode)){
					flag = true;
				}
			}
		}
		
		for (Long childPid:childrenList) {
			BasicObj childObj=myReferDataMap.get(ObjectName.IX_POI).get(childPid);
			IxPoi childPoi = (IxPoi) childObj.getMainrow();
			if(childPoi.hisOldValueContains(IxPoi.KIND_CODE)){
				String newKindCode=childPoi.getKindCode();
				if(newKindCode.equals("130105")||newKindCode.equals("140203")||newKindCode.equals("140302")
						||newKindCode.equals("210215")||newKindCode.equals("110101")||newKindCode.equals("110102")
						||newKindCode.equals("110103")||newKindCode.equals("110200")||newKindCode.equals("110301")
						||newKindCode.equals("110302")||newKindCode.equals("110303")||newKindCode.equals("110304")
						||newKindCode.equals("120101")||newKindCode.equals("120102")||newKindCode.equals("120103")
						||newKindCode.equals("120104")||newKindCode.equals("120201")||newKindCode.equals("120202")){
					String oldKindCode=(String) childPoi.getHisOldValue(IxPoi.KIND_CODE);
					if(!newKindCode.equals(oldKindCode)){
						flag = true;
					}
				}
				
			}
		}
		
		String type = "1";//1： 大陆 		2：港澳
		
		if(!flag){return;}
		
		List<IxPoiGasstation> gasstationList = poiObj.getIxPoiGasstations();
		if (gasstationList.size()>0) {
			for (IxPoiGasstation gasstation:gasstationList) {
				String service = gasstation.getService();
				for (Long childPid:childrenList) {
					BasicObj childObj=myReferDataMap.get(ObjectName.IX_POI).get(childPid);
					IxPoi childPoi = (IxPoi) childObj.getMainrow();
					service = getService(childPoi,service,type);
				}
				gasstation.setService(service);
			}
		} else {
			IxPoiGasstation gasstation = poiObj.createIxPoiGasstation();
			gasstation.setPoiPid(poi.getPid());
			String service = gasstation.getService();
			for (Long childPid:childrenList) {
				BasicObj childObj=myReferDataMap.get(ObjectName.IX_POI).get(childPid);
				IxPoi childPoi = (IxPoi) childObj.getMainrow();
				service = getService(childPoi,service,type);
			}
			gasstation.setService(service);
		}

	}
	
	private String getService(IxPoi childPoi,String service,String type) {
		if(type.equals("1")){	//大陆
			if (childPoi.getKindCode().equals("130105")) {
				if (service != null && service.length() > 0) {
					if (service.indexOf("1") < 0) {
						service += "|1";
					}
				} else {
					service = "1";
				}
			}else if (childPoi.getKindCode().equals("140203")) {
				if (service != null && service.length() > 0) {
					if (service.indexOf("2") < 0) {
						service += "|2";
					}
				} else {
					service = "2";
				}
			}else if (childPoi.getKindCode().equals("140302")) {
				if (service != null && service.length() > 0) {
					if (service.indexOf("3") < 0) {
						service += "|3";
					}
				} else {
					service = "3";
				}
			}else if (childPoi.getKindCode().equals("210215")) {
				if (service != null && service.length() > 0) {
					if (service.indexOf("4") < 0) {
						service += "|4";
					}
				} else {
					service = "4";
				}
			}else if (childPoi.getKindCode().equals("110101") || childPoi.getKindCode().equals("110102")
					|| childPoi.getKindCode().equals("110103") || childPoi.getKindCode().equals("110200")
					|| childPoi.getKindCode().equals("110301") || childPoi.getKindCode().equals("110302")
					|| childPoi.getKindCode().equals("110303") || childPoi.getKindCode().equals("110304")) {
				if (service != null && service.length() > 0) {
					if (service.indexOf("5") < 0) {
						service += "|5";
					}
				} else {
					service = "5";
				}
			}else if (childPoi.getKindCode().equals("120101") || childPoi.getKindCode().equals("120102")
					|| childPoi.getKindCode().equals("120103") || childPoi.getKindCode().equals("120104")
					|| childPoi.getKindCode().equals("120201") || childPoi.getKindCode().equals("120202")) {
				if (service != null && service.length() > 0) {
					if (service.indexOf("6") < 0) {
						service += "|6";
					}
				} else {
					service = "6";
				}
			}	
		}else if(type.equals("2")){
			if (childPoi.getKindCode().equals("130105")) {
				if (service != null && service.length() > 0) {
					if (service.indexOf("13") < 0) {
						service += "|13";
					}
				} else {
					service = "13";
				}
			}else if (childPoi.getKindCode().equals("140203")) {
				if (service != null && service.length() > 0) {
					if (service.indexOf("12") < 0) {
						service += "|12";
					}
				} else {
					service = "12";
				}
			}else if (childPoi.getKindCode().equals("210215")) {
				if (service != null && service.length() > 0) {
					if (service.indexOf("14") < 0) {
						service += "|14";
					}
				} else {
					service = "14";
				}
			}
		}
		return service;
	}

}
