package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 *	FM-D01-110		地址楼门号检查		DHM
	检查条件：
		非删除Poi对象
	检查原则：
	中文地址 IX_POI_ADDRESS表中lang_Code为CHI或CHT且unit字段不为空
	且不包含“SC_POINT_ADDRCK”表中“TYPE=8”的关键字“PRE_KEY”，就报log:楼门号拆分错误，确认地址拆分!
 *  sunjiawei
 *
 */
public class FMD01110 extends BasicCheckRule {

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
			String unit = address.getUnit();
			if (unit == null || unit.isEmpty()) {
				return;
			}
			MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<String> addrck = metaApi.getAddrck(8, "D");
			boolean isRight = false;
			for (String addr : addrck) {
				if (unit.contains(addr)) {
					isRight = true;
					break;
				}
			}

			if (!isRight) {
				setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),null);
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
