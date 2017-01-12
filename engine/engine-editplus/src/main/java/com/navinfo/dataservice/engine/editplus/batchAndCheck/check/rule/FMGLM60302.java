package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 检查条件：
 * 该POI发生变更(新增或修改主子表、删除子表)；
 * 检查原则：
 * 1.语言代码为CHI或CHT，FULLNAME字段为空或拆分后合并的15个字段都为空时，报log
 * 1：中文地址全称为空或拆分后合并的15个字段都为空
 * 2.语言代码为ENG或POR，FULLNAME字段为空时，报log：英文地址全称为空
 * 15个字段顺序：TOWN,PLACE,STREET,LANDMARK,PREFIX,HOUSENUM,TYPE,SUBNUM,SURFIX,ESTAB,BUILDING,FLOOR,UNIT,ROOM,ADDONS
 * @author gaopengrong
 *
 */
public class FMGLM60302 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
			List<String> errorList=new ArrayList<String>();
			for(IxPoiAddress address:addresses){
				if(address.isCH()){
					String mergeAddr = merge15addr(address);
					if(mergeAddr==null||mergeAddr.isEmpty()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "拆分后合并的15个字段都为空");
					}
					String fullname = address.getFullname();
					if(fullname==null||fullname.isEmpty()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "中文地址全称为空");
					}
				}
				if(address.isEng()||address.isPor()){
					String fullname = address.getFullname();
					if(fullname==null||fullname.isEmpty()){
						setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "英文地址全称为空");
					}
				}
			}			
		}
	}
	private static String  merge15addr(IxPoiAddress addr){
		String mergeAddr = "";
		if (addr.getProvince() != null) {
			mergeAddr += addr.getProvince();
		}
		if (addr.getCity() != null) {
			mergeAddr += addr.getCity();
		}
		if (addr.getCounty() != null) {
			mergeAddr += addr.getCounty();
		}
		if (addr.getTown() != null) {
			mergeAddr += addr.getTown();
		}
		if (addr.getPlace() != null) {
			mergeAddr += addr.getPlace();
		}
		if (addr.getStreet() != null) {
			mergeAddr += addr.getStreet();
		}
		if (addr.getLandmark() != null) {
			mergeAddr += addr.getLandmark();
		}
		if (addr.getPrefix() != null) {
			mergeAddr += addr.getPrefix();
		}
		if (addr.getHousenum() != null) {
			mergeAddr += addr.getHousenum();
		}
		if (addr.getType() != null) {
			mergeAddr += addr.getType();
		}
		if (addr.getSubnum() != null) {
			mergeAddr += addr.getSubnum();
		}
		if (addr.getSurfix() != null) {
			mergeAddr += addr.getSurfix();
		}
		if (addr.getEstab() != null) {
			mergeAddr += addr.getEstab();
		}
		if (addr.getBuilding() != null) {
			mergeAddr += addr.getBuilding();
		}
		if (addr.getUnit() != null) {
			mergeAddr += addr.getUnit();
		}
		if (addr.getFloor() != null) {
			mergeAddr += addr.getFloor();
		}
		if (addr.getRoom() != null) {
			mergeAddr += addr.getRoom();
		}
		if (addr.getAddons() != null) {
			mergeAddr += addr.getAddons();
		}
		return mergeAddr;
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
