package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * 在IX_POI_ADDRESS表中，LANG_CODE=“CHI”或“CHT”的除前三级地址外的后15个地址字段，内容相加长度为1，则报LOG：非法地址需要删除！
 * 
 */
public class GLM60442 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		IxPoiAddress address = poiObj.getCHAddress();
		if (address == null) {
			return;
		}
		int len=getLength(address.getTown())+getLength(address.getPlace())+getLength(address.getStreet())+
				getLength(address.getLandmark())+getLength(address.getPrefix())+getLength(address.getHousenum())+
				getLength(address.getType())+getLength(address.getSubnum())+getLength(address.getSurfix())+
				getLength(address.getEstab())+getLength(address.getBuilding())+getLength(address.getFloor())+
				getLength(address.getUnit())+getLength(address.getRoom())+getLength(address.getAddons());
		
		if(len==1){
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"非法地址需要删除！");
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
