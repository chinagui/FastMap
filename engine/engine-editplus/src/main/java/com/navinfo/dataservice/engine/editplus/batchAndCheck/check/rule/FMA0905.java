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

/**
 * 检查条件： 以下条件其中之一满足时，需要进行检查： 
 * (1)存在官方中文地址IX_POI_ADDRESS新增且FULLNAME不为空；
 * (2)存在官方中文地址IX_POI_ADDRESS修改且FULLNAME不为空； 检查原则： “TOWN（乡镇街道办）”
 * 字段非空并且不以“地址拆分用检查配置表(SC_POINT_ADDRCK)”配置表中TYPE=10且
 * HM_FLAG=’D’对应的关键字（PRE_KEY）结尾的报出来。 
 * 提示：乡镇街道办检查：乡镇街道办字段不含关键字；
 * 
 *
 */
public class FMA0905 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			// 存在IxPoiAddress新增或者修改履历
			IxPoiAddress address = poiObj.getCHAddress();
			if (address == null) {
				return;
			}
			if (!address.getHisOpType().equals(OperationType.INSERT)
					&& !address.getHisOpType().equals(OperationType.UPDATE)) {
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
			List<String> addrck = metaApi.getAddrck(10, "D");
			boolean isRight = false;
			for (String addr : addrck) {
				if (town.endsWith(addr)) {
					isRight = true;
				}
			}

			if (!isRight) {
				setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
						"乡镇街道办检查：乡镇街道办字段不含关键字；");
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
