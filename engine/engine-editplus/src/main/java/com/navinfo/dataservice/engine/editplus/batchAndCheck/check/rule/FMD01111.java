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
 *	FM-D01-111		地址房间号检查		DHM
 *	检查条件：
	非删除Poi对象
	检查原则：
	中文地址 IX_POI_ADDRESS表中lang_Code为CHI或CHT且room字段不为空
		且包含“SC_POINT_ADDRCK”表“TYPE=6”或“TYPE=8”的关键字“PRE_KEY”，就报log:房间号拆分错误，确认地址拆分!
 *  sunjiawei
 *
 */
public class FMD01111 extends BasicCheckRule {

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
			String room = address.getRoom();
			if (room == null || room.isEmpty()) {
				return;
			}
			MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<String> addrck1 = metaApi.getAddrck(6, "D");
			List<String> addrck2 = metaApi.getAddrck(8, "D");
			boolean isRight1 = false;
			boolean isRight2 = false;
			for (String addr : addrck1) {
				if (room.contains(addr)) {
					isRight1 = true;
					break;
				}
			}
			for (String addr : addrck2) {
				if (room.contains(addr)) {
					isRight2 = true;
					break;
				}
			}

			if (isRight1||isRight2) {
				setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),null);
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
