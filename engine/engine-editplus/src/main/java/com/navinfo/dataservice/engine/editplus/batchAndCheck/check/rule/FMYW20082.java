package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName: FMYW20082
 * @author: zhangpengpeng
 * @date: 2017年1月10日
 * @Desc: FMYW20082.java 检查条件： 以下条件其中之一满足时，需要进行检查：
 *        (1)存在官方中文地址IX_POI_ADDRESS新增且FULLNAME不为空；
 *        (2)存在官方中文地址IX_POI_ADDRESS修改且FULLNAME不为空； 检查原则：
 *        当门牌号为空时，“子号、后缀、类型、前缀”中的一个不是空，报log：子号与门牌逻辑错误
 */
public class FMYW20082 extends BasicCheckRule {
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if (obj.objName().equals(ObjectName.IX_POI)) {
			IxPoiObj poiObj = (IxPoiObj) obj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();
			// 存在IxPoiAddress新增或者修改履历
			IxPoiAddress addr = poiObj.getCHAddress();
			if (addr == null) {
				return;
			}
			if (addr.getHisOpType().equals(OperationType.INSERT)
					|| (addr.getHisOpType().equals(OperationType.UPDATE))) {
				return;
			}
			if (addr.getFullname() == null || addr.getFullname().isEmpty()) {
				return;
			}
			List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();
			if (addresses == null || addresses.size() == 0) {
				return;
			}
			for (IxPoiAddress address : addresses) {
				if (address.isCH()) {
					String poiHousenum = address.getHousenum();
					String poiType = address.getType();
					String poiSubnum = address.getSubnum();
					String poiSurfix = address.getSurfix();
					String poiPrefix = address.getPrefix();
					if (StringUtils.isEmpty(poiHousenum)) {
						if (StringUtils.isNotEmpty(poiType) || StringUtils.isNotEmpty(poiSubnum)
								|| StringUtils.isNotEmpty(poiSurfix) || StringUtils.isNotEmpty(poiPrefix)) {
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), null);
							return;
						}
					}
				}
			}
		}
	}
}
