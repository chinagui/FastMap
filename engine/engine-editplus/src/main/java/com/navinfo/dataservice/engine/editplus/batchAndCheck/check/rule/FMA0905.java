package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

public class FMA0905 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//存在IxPoiAddress新增或者修改履历
			IxPoiAddress address=poiObj.getCHAddress();
			if(!address.getHisOpType().equals(OperationType.INSERT)&&!address.getHisOpType().equals(OperationType.UPDATE)){
				return;
			}
			if (address.getFullname() == null || address.getFullname().isEmpty()) {
				return;
			}
			String town = address.getTown();
			if (town == null || town.isEmpty()) {
				return;
			}
			MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<String> addrck = metaApi.getAddrck(10, "M");
			String error = "";
			if (!addrck.contains(town.substring(town.length()-1))) {
				error = town.substring(town.length()-1);
			}
			if (!error.isEmpty()) {
				setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"乡镇街道办检查：乡镇街道办字段不含关键字；");
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
