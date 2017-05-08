package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * 查询条件：POI分类为230227且为子且非删除时，查询父的地址和电话的非删除记录 
 * 批处理：
 * 子POI的电话赋值父POI的电话，如果父记录删除，则子的电话也删除； 
 * 子POI的地址赋值父POI的地址，如果父记录删除，则子的地址也删除； 
 * 并生成履历；
 * 
 * @author wangdongbin
 *
 */
public class FMBAT20186 extends BasicBatchRule {
	
	private Map<Long,Long> childPidParentPid;

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
		referSubrow.add("IX_POI_ADDRESS");
		referSubrow.add("IX_POI_CONTACT");
		Map<Long, BasicObj> referObjs = getBatchRuleCommand().loadReferObjs(parentPids, ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		if (!childPidParentPid.containsKey(poi.getPid()) || !poi.getKindCode().equals("230227")) {
			return;
		}
		Long parentPid = childPidParentPid.get(poi.getPid());
		
		BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentPid);
		IxPoiObj parentPoiObj = (IxPoiObj) parentObj;
		
		// 地址
		List<IxPoiAddress> parentAddressList = parentPoiObj.getIxPoiAddresses();
		List<IxPoiAddress> addressList = poiObj.getIxPoiAddresses();
		for(int i=addressList.size()-1;i>=0;i--){
			poiObj.deleteSubrow(addressList.get(i));
		}
		for (IxPoiAddress parentAddress:parentAddressList) {
			IxPoiAddress newAddress = poiObj.createIxPoiAddress();
			newAddress.setPoiPid(poi.getPid());
			newAddress.setNameGroupid(parentAddress.getNameGroupid());
			newAddress.setLangCode(parentAddress.getLangCode());
			newAddress.setSrcFlag(parentAddress.getSrcFlag());
			newAddress.setFullname(parentAddress.getFullname());
			newAddress.setFullnamePhonetic(parentAddress.getFullnamePhonetic());
		}
		
		// 电话
		List<IxPoiContact> parentContactList = parentPoiObj.getIxPoiContacts();
		List<IxPoiContact> contactList = poiObj.getIxPoiContacts();
		for(int i=contactList.size()-1;i>=0;i--){
			poiObj.deleteSubrow(contactList.get(i));
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
