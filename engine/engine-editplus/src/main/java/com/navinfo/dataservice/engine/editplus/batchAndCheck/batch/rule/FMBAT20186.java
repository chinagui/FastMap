package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
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
	(1)POI分类为230227且为子且非删除；
	(2)POI分类为230218且非删除且为父；
	满足(1)或(2)时，查询父的地址和电话，分别判断父的地址和电话是否与子一致，如果一致，则不批处理，如果不一致，则批处理；
	批处理：
	清空所有子POI电话，将父POI电话赋值给子POI电话，如果父电话不存在，则子的电话也删除；
	清空所有子POI地址，将父POI地址赋值给子POI地址，如果父地址不存在，则子的地址也删除；
	并生成履历；
 * 
 * @author sunjiawei
 *
 */
public class FMBAT20186 extends BasicBatchRule {
	
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	
	private Map<Long,Long> childPidParentPid;
	private Map<Long, List<Long>> childrenMap;


	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		childPidParentPid = IxPoiSelector.getParentPidsByChildrenPids(getBatchRuleCommand().getConn(), pidList);
		childrenMap = IxPoiSelector.getChildrenPidsByParentPidList(getBatchRuleCommand().getConn(), pidList);
		

		Set<Long> parentPids = new HashSet<Long>();
		if(!childPidParentPid.isEmpty()){
			for (Long childPid:childPidParentPid.keySet()) {
				parentPids.add(childPidParentPid.get(childPid));
			}
		}
		
		if(!childrenMap.isEmpty()){
			for (Long childPid:childrenMap.keySet()) {
				parentPids.addAll(childrenMap.get(childPid));
			}
		}
		if(parentPids.size()==0){
			return;
		}

		Set<String> referSubrow =  new HashSet<String>();
		referSubrow.add("IX_POI_ADDRESS");
		referSubrow.add("IX_POI_CONTACT");
		Map<Long, BasicObj> referObjs = getBatchRuleCommand().loadReferObjs(parentPids, ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		boolean isParent = false;
		boolean isChild = false;
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		
		if(!childPidParentPid.isEmpty()){
			if(childPidParentPid.containsKey(poi.getPid())){
				isChild = true;
			}
		}
		
		if(!childrenMap.isEmpty()){
			if(childrenMap.containsKey(poi.getPid())){
				isParent = true;
			}
		}
		
		if(isChild){
			if (!childPidParentPid.containsKey(poi.getPid()) || !poi.getKindCode().equals("230227")
					|| poi.getHisOpType().equals(OperationType.DELETE)) {
				return;
			}
			Long parentPid = childPidParentPid.get(poi.getPid());
			
			BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentPid);
			if(parentObj==null){return;}
			IxPoiObj parentPoiObj = (IxPoiObj) parentObj;
			
			// 地址
			List<IxPoiAddress> parentAddressList = parentPoiObj.getIxPoiAddresses();
			List<IxPoiAddress> addressList = poiObj.getIxPoiAddresses();
			boolean addressListNull = (addressList==null || addressList.isEmpty());//子地址为空
			boolean parentAddressListNull = (parentAddressList==null || parentAddressList.isEmpty());//父地址为空
			
			if(!(parentAddressListNull&&addressListNull)){//父子只要有一个不为空
					boolean childAddressEqualsParent = false;//判断父的地址和子的是否一致
					if(!parentAddressListNull&&!addressListNull){//父子都不为空
						if(parentAddressList.size()==addressList.size()){
							childAddressEqualsParent = judgeChildCHIAddressEqualsParent(poiObj, parentPoiObj);
						}
					}
					
					if(!childAddressEqualsParent){
						if(!addressListNull){
							for(int i=addressList.size()-1;i>=0;i--){
								poiObj.deleteSubrow(addressList.get(i));
							}
						}
						for (IxPoiAddress parentAddress:parentAddressList) {
							IxPoiAddress newAddress = poiObj.createIxPoiAddress();
							newAddress.setPoiPid(poi.getPid());
							newAddress.setNameGroupid(parentAddress.getNameGroupid());
							newAddress.setLangCode(parentAddress.getLangCode());
							newAddress.setSrcFlag(parentAddress.getSrcFlag());
							newAddress.setFullname(parentAddress.getFullname());
							newAddress.setFullnamePhonetic(parentAddress.getFullnamePhonetic());
							newAddress.setRoadname(parentAddress.getRoadname());
							newAddress.setRoadnamePhonetic(parentAddress.getRoadnamePhonetic());
							newAddress.setAddrname(parentAddress.getAddrname());
							newAddress.setAddrnamePhonetic(parentAddress.getAddrnamePhonetic());
						}
						
					}
					
			}
			
			// 电话
			List<IxPoiContact> parentContactList = parentPoiObj.getIxPoiContacts();
			List<IxPoiContact> contactList = poiObj.getIxPoiContacts();
			boolean childContactEqualsParent = judgeChildContactEqualsParent(parentContactList, contactList);
			
			if(!childContactEqualsParent){
				if(contactList!=null && !contactList.isEmpty()){
					for(int i=contactList.size()-1;i>=0;i--){
						poiObj.deleteSubrow(contactList.get(i));
					}
				}

				for (IxPoiContact parentContact:parentContactList) {
					IxPoiContact newContact = poiObj.createIxPoiContact();
					newContact.setPoiPid(poi.getPid());
					newContact.setContactType(parentContact.getContactType());
					newContact.setContact(parentContact.getContact());
					newContact.setContactDepart(parentContact.getContactDepart());
					newContact.setPriority(parentContact.getPriority());
				}
			}
		}
		if(isParent){
			if (!childrenMap.containsKey(poi.getPid()) || !poi.getKindCode().equals("230218")
					||poi.getHisOpType().equals(OperationType.DELETE)) {
				return;
			}

			List<Long> childrenList  = childrenMap.get(poi.getPid());
			
			// 地址
			List<IxPoiAddress> parentAddressList = poiObj.getIxPoiAddresses();
			// 电话
			List<IxPoiContact> parentContactList = poiObj.getIxPoiContacts();
			boolean parentAddressListNull = (parentAddressList==null || parentAddressList.isEmpty());//父地址为空			
			
			for (Long childPid:childrenList) {
				BasicObj childObj = myReferDataMap.get(ObjectName.IX_POI).get(childPid);
				if(childObj==null){return;}
				IxPoiObj child = (IxPoiObj) childObj;
				IxPoi childPoi = (IxPoi) child.getMainrow();
			
				List<IxPoiAddress> childAddressList = child.getIxPoiAddresses();
				//地址
				boolean childAddressListNull = (childAddressList==null || childAddressList.isEmpty());//子地址为空			
				
				if(!(parentAddressListNull&&childAddressListNull)){
					boolean childCHIAddressEqualsParent = false;//判断父的中文地址和子的是否一致
					if(!parentAddressListNull&&!childAddressListNull){
						if(parentAddressList.size()==childAddressList.size()){
							childCHIAddressEqualsParent = judgeChildCHIAddressEqualsParent(child, poiObj);
						}
					}
				
					if(!(childCHIAddressEqualsParent)){
						if(!childAddressListNull){
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
							newAddress.setRoadname(parentAddress.getRoadname());
							newAddress.setRoadnamePhonetic(parentAddress.getRoadnamePhonetic());
							newAddress.setAddrname(parentAddress.getAddrname());
							newAddress.setAddrnamePhonetic(parentAddress.getAddrnamePhonetic());
						}
					}
					
				}
				
				// 电话
				List<IxPoiContact> childContactList = child.getIxPoiContacts();
				boolean childContactListNull = (childContactList==null || childContactList.isEmpty());//子电话为空	
				
				boolean childContactEqualsParent = judgeChildContactEqualsParent(parentContactList, childContactList);
				
				if(!childContactEqualsParent){
					if(!childContactListNull){
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
	
	
	public boolean judgeChildCHIAddressEqualsParent(IxPoiObj childPoiObj,IxPoiObj parentPoiObj){
		
		IxPoiAddress parentCHAddress = parentPoiObj.getCHAddress();
		IxPoiAddress poiCHAddress = childPoiObj.getCHAddress();

		boolean childCHIAddressEqualsParent = false;
		
		if(parentCHAddress==null&&poiCHAddress==null){childCHIAddressEqualsParent = true;}
		if(parentCHAddress!=null&&poiCHAddress!=null){
			String parentCHAddressStr =  parentCHAddress.getLangCode()+parentCHAddress.getFullname();
			String poiCHAddressStr =  poiCHAddress.getLangCode()+poiCHAddress.getFullname();
			if(parentCHAddressStr.equals(poiCHAddressStr)){childCHIAddressEqualsParent = true;}//判断父的中文地址和子的是否一致
		}
		
		return childCHIAddressEqualsParent;
	}
	
	public boolean judgeChildContactEqualsParent(List<IxPoiContact> parentContactList,List<IxPoiContact> childContactList){
		boolean childContactEqualsParent = false;  
		if((parentContactList==null ||parentContactList.isEmpty())&&
				(childContactList==null ||childContactList.isEmpty())){childContactEqualsParent = true; }//父子电话都为空
		if(CollectionUtils.isNotEmpty(parentContactList)){
			for (IxPoiContact parentIxPoiContact : parentContactList) {
				for (IxPoiContact ixPoiContact : childContactList) {
					if(parentIxPoiContact.getContact().equals(ixPoiContact.getContact())
							&&parentIxPoiContact.getContactType()==ixPoiContact.getContactType()
							&&parentIxPoiContact.getContactDepart()==ixPoiContact.getContactDepart()
							&&parentIxPoiContact.getPriority()==ixPoiContact.getPriority()){
						childContactEqualsParent = true;
					}
				}
			}
		}
		
		return childContactEqualsParent;
	}

}
