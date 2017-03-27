package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 查询条件：
 * 存在IX_POI新增或者修改且KIND_CODE为230215且该POI存在子 
 * 批处理：
 * 批处理时，如果父不存在深度信息记录，则增加一条深度信息记录，其它字段赋默认值，service赋值原则如下：
 * (1)如果该POI是大陆数据,则根据不同分类针对IX_POI_GASSTATION.SERVICE如下批处理，并生成履历：
 * ①子POI分类中有130105时，如果SERVICE为空，则SERVICE赋值为1；如果SERVICE不为空且不包含1时，则在值后面增加1，并用"|"分割
 * ；如果SERVICE不为空且包含1时，则不处理；
 * ②子POI的分类中有140203时，如果SERVICE为空，则SERVICE赋值为2；如果SERVICE不为空且不包含2时，则在值后面增加2，
 * 并用"|"分割；如果SERVICE不为空且包含2时，则不处理；
 * ③子POI的分类中有140302时，如果SERVICE为空，则SERVICE赋值为3；如果SERVICE不为空且不包含3时，则在值后面增加3，
 * 并用"|"分割；如果SERVICE不为空且包含3时，则不处理;
 * ④子POI的分类中有210215时，如果SERVICE为空，则SERVICE赋值为4；如果SERVICE不为空且不包含4时，则在值后面增加4，
 * 并用"|"分割；如果SERVICE不为空且包含4时，则不处理；
 * ⑤子POI的分类中有{110101,110102,110103,110200,110301,110302,110303,110304}中任意一个时，
 * 如果SERVICE为空，则SERVICE赋值为5；如果SERVICE不为空且不包含5时，则在值后面增加5，并用"|"分割；
 * 如果SERVICE不为空且包含5时，则不处理；
 * ⑥子POI的分类中有{120101,120102,120103,120104,120201,120202}时，如果SERVICE为空，
 * 则SERVICE赋值为6；如果SERVICE不为空且不包含6时，则在值后面增加6，并用"|"分割；如果SERVICE不为空且包含6时，则不处理；
 * 
 * @author wangdongbin
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
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		if (!childrenMap.containsKey(poi.getPid()) || !poi.getKindCode().equals("230215")) {
			return;
		}
		List<Long> childrenList = childrenMap.get(poi.getPid());
		List<IxPoiGasstation> gasstationList = poiObj.getIxPoiGasstations();
		if (gasstationList.size()>0) {
			for (IxPoiGasstation gasstation:gasstationList) {
				String service = gasstation.getService();
				for (Long childPid:childrenList) {
					BasicObj childObj=myReferDataMap.get(ObjectName.IX_POI).get(childPid);
					IxPoi childPoi = (IxPoi) childObj.getMainrow();
					service = getService(childPoi,service);
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
				service = getService(childPoi,service);
			}
			gasstation.setService(service);
		}

	}
	
	private String getService(IxPoi childPoi,String service) {
		if (childPoi.getKindCode().equals("130105")) {
			if (service != null && service.length() > 0) {
				if (service.indexOf("1") < 0) {
					service += "|1";
				}
			} else {
				service = "1";
			}
		}
		if (childPoi.getKindCode().equals("140203")) {
			if (service != null && service.length() > 0) {
				if (service.indexOf("2") < 0) {
					service += "|2";
				}
			} else {
				service = "2";
			}
		}
		if (childPoi.getKindCode().equals("140302")) {
			if (service != null && service.length() > 0) {
				if (service.indexOf("3") < 0) {
					service += "|3";
				}
			} else {
				service = "3";
			}
		}
		if (childPoi.getKindCode().equals("210215")) {
			if (service != null && service.length() > 0) {
				if (service.indexOf("4") < 0) {
					service += "|4";
				}
			} else {
				service = "4";
			}
		}
		if (childPoi.getKindCode().equals("110101") || childPoi.getKindCode().equals("110102")
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
		}
		if (childPoi.getKindCode().equals("120101") || childPoi.getKindCode().equals("120102")
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
		return service;
	}

}
