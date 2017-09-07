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
 * 在IX_POI_ADDRESS表中，标志物非空，否则报log：标志物逻辑检查：标志物有内容应该为空
 * 
 */
public class GLM60186 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		IxPoiAddress address = poiObj.getCHAddress();
		if (address == null) {
			return;
		}
		if(address.getLandmark() != null && address.getLandmark().length() > 0){
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"标志物逻辑检查：标志物有内容应该为空");
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {}
}
