package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;


/**
 * 当18个中文字段均为空时，删除所有地址(中文和英葡文均删除)；
 * 当中文地址fullname不为空，英文地址fullname为空时，删除英文地址组；
 * 当中文地址fullname不为空，葡文地址fullname为空时，删除葡文地址组；
 * @author gaopengrong
 */
public class FMBAT20164 extends BasicBatchRule {

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoiAddress chiAddress = poiObj.getChiAddress();
		if(chiAddress==null){return;} 
		
		//当18个中文字段均为空时，删除所有地址(中文和英葡文均删除)；
		if(chiAddress.getProvince()==null&&chiAddress.getCity()==null&&chiAddress.getCounty()==null&&
				chiAddress.getTown()==null&&chiAddress.getPlace()==null&&chiAddress.getStreet()==null&&
				chiAddress.getLandmark()==null&&chiAddress.getPrefix()==null&&chiAddress.getHousenum()==null&&
				chiAddress.getType()==null&&chiAddress.getSubnum()==null&&chiAddress.getSurfix()==null&&
				chiAddress.getEstab()==null&&chiAddress.getBuilding()==null&&chiAddress.getFloor()==null&&
				chiAddress.getUnit()==null&&chiAddress.getRoom()==null&&chiAddress.getAddons()==null){
			for(IxPoiAddress ad:poiObj.getIxPoiAddresses()){
				poiObj.deleteSubrow(ad);
			}
		}
		
		long nameGroupId=chiAddress.getNameGroupid();
		String chiFullname = chiAddress.getFullname();
		if(chiFullname==null){return;}
		
		IxPoiAddress engAddress = poiObj.getENGAddress(nameGroupId);
		String engFullname = null;
		
		IxPoiAddress porAddress = poiObj.getPORAddress(nameGroupId);
		String porFullname = null; 
		
		if(engAddress!=null){
			engFullname = engAddress.getFullname();
		}
		
		if(porAddress!=null){
			engFullname = porAddress.getFullname();
		}
		
		//当中文地址fullname不为空，英文地址fullname为空时，删除英文地址组；
		//当中文地址fullname不为空，葡文地址fullname为空时，删除葡文地址组；
		if(chiFullname!=null&&engFullname==null&&engAddress!=null){poiObj.deleteSubrow(engAddress);}
		if(chiFullname!=null&&porFullname==null&&porAddress!=null){poiObj.deleteSubrow(porAddress);}
		
	}
}
