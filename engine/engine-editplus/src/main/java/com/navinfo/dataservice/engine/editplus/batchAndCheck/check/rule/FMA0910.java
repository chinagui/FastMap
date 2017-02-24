package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 检查条件： 该POI发生变更(新增或修改主子表、删除子表)； 
 * 检查原则： 
 * 1） 乡镇街道办、地名小区名、街巷名均为空，标志物名不为空，报出； 
 * 2）标志物有内容，则门楼地址（拼起来：前缀、门牌号、类型、子号、后缀、附属设施名、楼栋号、楼号、楼层号、房间号）不为空，报出。 
 * 提示：
 * 标志物逻辑检查： 
 * 1）乡镇街道办、地名小区名、街巷名为空，标志物名应该为空；
 * 2）标志物有内容，则门楼地址（拼起来：前缀、门牌号、类型、子号、后缀、附属设施名、楼栋号、楼号、楼层号、房间号）应该为空。
 *
 */
public class FMA0910 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		IxPoiAddress address = poiObj.getCHAddress();
		if (address == null) {
			return;
		}
		List<String> errList = new ArrayList<String>();
		if (StringUtils.isEmpty(address.getTown()) && StringUtils.isEmpty(address.getPlace())
				&& StringUtils.isEmpty(address.getStreet())) {
			if (StringUtils.isNotEmpty(address.getLandmark())) {
				errList.add("乡镇街道办、地名小区名、街巷名为空，标志物名应该为空；");
			}
		}
		if (StringUtils.isNotEmpty(address.getLandmark())) {
			if (StringUtils.isNotEmpty(address.getPrefix()) || StringUtils.isNotEmpty(address.getHousenum())
					|| StringUtils.isNotEmpty(address.getType()) || StringUtils.isNotEmpty(address.getSubnum())
					|| StringUtils.isNotEmpty(address.getSurfix()) || StringUtils.isNotEmpty(address.getEstab())
					|| StringUtils.isNotEmpty(address.getBuilding()) || StringUtils.isNotEmpty(address.getFloor())
					|| StringUtils.isNotEmpty(address.getUnit())) {
				errList.add("标志物有内容，则门楼地址（拼起来：前缀、门牌号、类型、子号、后缀、附属设施名、楼栋号、楼号、楼层号、房间号）应该为空。");
			}
		}
		if (errList.size() > 0) {
			String errStr = org.apache.commons.lang.StringUtils.join(errList, ";");
			setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), errStr);
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
