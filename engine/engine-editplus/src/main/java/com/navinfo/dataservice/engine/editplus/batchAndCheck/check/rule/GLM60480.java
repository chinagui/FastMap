package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * IX_POI_ADDRESS表中，只有附属设施字段(estab)且其他附加信息字段（addons）有值，其余16个地址字段为空，报LOG：附属设施拆分错误！
 * 
 */
public class GLM60480 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		IxPoiAddress address = poiObj.getCHAddress();
		if (address == null) {
			return;
		}
		int len1=getLength(address.getEstab());
		int len2=getLength(address.getAddons());
		int len3=getLength(address.getProvince())+getLength(address.getCity())+getLength(address.getCounty())+
				getLength(address.getTown())+getLength(address.getPlace())+getLength(address.getStreet())+
				getLength(address.getLandmark())+getLength(address.getPrefix())+getLength(address.getHousenum())+
				getLength(address.getType())+getLength(address.getSubnum())+getLength(address.getSurfix())+
				getLength(address.getBuilding())+getLength(address.getFloor())+getLength(address.getUnit())+
				getLength(address.getRoom());
		
		if(len1>0&&len2>0&&len3==0){
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"附属设施拆分错误！");
		}
	}
	
	private int getLength(String str) {
		if (str == null) {
			return 0;
		} else {
			return str.length();
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
