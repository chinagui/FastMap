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

/**
 * 
 * 检查条件：
    以下条件其中之一满足时，需要进行检查：
	(1)存在IX_POI_ADDRESS新增且FULLNAME不为空； 
	(2)存在IX_POI_ADDRESS修改且FULLNAME不为空；
	检查原则：
	“TYPE（类型名)”字段如果不为空且不等于“大陆地址检查地址拆分用检查配置表(SC_POINT_ADDRCK)”中TYPE=‘1’且HM_FLAG=’M’的关键字（PRE_KEY），报出log确认。
	log：类型不在对应的配置表里，确认是否正确
 *
 */
public class FMA0907 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
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
		String type = address.getType();
		if (type == null || type.isEmpty()) {
			return;
		}
		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		List<String> addrck = metaApi.getAddrck(1, "M");
		String error = "";
		if (!addrck.contains(type)) {
			error = type;
		}
		if (!error.isEmpty()) {
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"类型不在对应的配置表里，确认是否正确");
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
