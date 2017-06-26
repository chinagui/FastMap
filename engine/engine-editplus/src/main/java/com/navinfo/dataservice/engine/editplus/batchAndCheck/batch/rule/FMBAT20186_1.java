package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
	查询条件：
	 POI分类为230218且为父且非删除时，且该父POI修改电话或者修改地址
	批处理：
	清空子POI电话，将父POI电话赋值给子POI电话，如果父电话不存在，则子的电话也删除；
	清空子POI地址，将父POI地址赋值给子POI地址，如果父地址不存在，则子的地址也删除；
	并生成履历；
 *
 */
public class FMBAT20186_1 extends BasicBatchRule {
	
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
		referSubrow.add("IX_POI_ADDRESS");
		referSubrow.add("IX_POI_CONTACT");
		Map<Long, BasicObj> referObjs = getBatchRuleCommand().loadReferObjs(childPids, ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
		
	}
	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		if (!childrenMap.containsKey(poi.getPid()) || !poi.getKindCode().equals("230218")) {
			return;
		}

		List<Long> childrenList  = childrenMap.get(poi.getPid());
		
		// 地址
		List<IxPoiAddress> parentAddressList = poiObj.getIxPoiAddresses();
		// 电话
		List<IxPoiContact> parentContactList = poiObj.getIxPoiContacts();
		if((parentAddressList==null||parentAddressList.isEmpty())&&(parentContactList==null||parentContactList.isEmpty())){return;}
		boolean addressFlag=false;
		boolean contactFlag=false;
		for(IxPoiAddress parentAdd:parentAddressList){
			if(parentAdd.getHisOpType().equals(OperationType.INSERT)||parentAdd.getHisOpType().equals(OperationType.UPDATE)){
				addressFlag=true;
			}
		}
		for(IxPoiContact parentCont:parentContactList){
			if(parentCont.getHisOpType().equals(OperationType.INSERT)||parentCont.getHisOpType().equals(OperationType.UPDATE)){
				contactFlag=true;
			}
		}
		
		
		for (Long childPid:childrenList) {
			BasicObj childObj = myReferDataMap.get(ObjectName.IX_POI).get(childPid);
			IxPoiObj child = (IxPoiObj) childObj;
			IxPoi childPoi = (IxPoi) child.getMainrow();
		
			List<IxPoiAddress> childAddressList = child.getIxPoiAddresses();
			List<IxPoiContact> childContactList = child.getIxPoiContacts();
			if(addressFlag){
				if(childAddressList!=null && !childAddressList.isEmpty()){
					// 地址
					for(int i=childAddressList.size()-1;i>=0;i--){
						child.deleteSubrow(childAddressList.get(i));
					}
				}
				for (IxPoiAddress parentAddress:parentAddressList) {
					IxPoiAddress newAddress = child.createIxPoiAddress();
					newAddress.setPoiPid(childPoi.getPid());
					newAddress.setNameGroupid(parentAddress.getNameGroupid());
					newAddress.setLangCode(parentAddress.getLangCode());
					newAddress.setSrcFlag(parentAddress.getSrcFlag());
					newAddress.setFullname(parentAddress.getFullname());
					newAddress.setFullnamePhonetic(parentAddress.getFullnamePhonetic());
				}
				
			}
			
			if(contactFlag){
				if(childContactList!=null && !childContactList.isEmpty()){
					// 电话
					for(int i=childContactList.size()-1;i>=0;i--){
						child.deleteSubrow(childContactList.get(i));
					}
				}
				
				for (IxPoiContact parentContact:parentContactList) {
					IxPoiContact newContact = child.createIxPoiContact();
					newContact.setPoiPid(childPoi.getPid());
					newContact.setContactType(parentContact.getContactType());
					newContact.setContact(parentContact.getContact());
					newContact.setContactDepart(parentContact.getContactDepart());
					newContact.setPriority(parentContact.getPriority());
				}
			}
			
		}

	}

}
