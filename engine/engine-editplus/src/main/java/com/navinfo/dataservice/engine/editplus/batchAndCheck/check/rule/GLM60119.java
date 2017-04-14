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
 * 检查条件：非删除POI对象
 * 检查原则：
 * 在I_POI_ADDRESS表中，
  1)  若“TOWN（乡镇名）STREET（街巷名）、PLACE（地名小区名）”同时为空，但“LANDMARK（标志物名）、PREFIX（前缀）、HOUSENUM（门牌号）、
  TYPE（类型名）、SUBNUM（子号）、SURFIX（后缀）”中的任意一个字段有内容，程序报log：“乡镇/街道办、地名小区名、街道名”字段为空，
  “前缀、门牌号、类型、子号、后缀”应为空！
  2) “HOUSENUM（门牌号）”为空，但“TYPE（类型名）、SUBNUM（子号）、SURFIX（后缀）、PREFIX（前缀）”的任意一个字段有内容，
  程序报log：“门牌号”为空，“类型、子号、后缀、前缀”不能存在内容！
 *
 */
public class GLM60119 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		IxPoiAddress address = poiObj.getCHAddress();
		if (address == null) {
			return;
		}
		List<String> errList = new ArrayList<String>();
		if (StringUtils.isEmpty(address.getTown()) && StringUtils.isEmpty(address.getStreet())
				&& StringUtils.isEmpty(address.getPlace())) {
			if (StringUtils.isNotEmpty(address.getLandmark()) || StringUtils.isNotEmpty(address.getPrefix())
					|| StringUtils.isNotEmpty(address.getHousenum()) || StringUtils.isNotEmpty(address.getType())
					|| StringUtils.isNotEmpty(address.getSubnum()) || StringUtils.isNotEmpty(address.getSurfix())) {
				errList.add("“乡镇街道办”、“街巷名”、“地名小区名”字段为空，“前缀、门牌号、类型、子号、后缀”应为空");
			}
		}
		if (StringUtils.isEmpty(address.getHousenum())) {
			if (StringUtils.isNotEmpty(address.getType())|| StringUtils.isNotEmpty(address.getSubnum()) || StringUtils.isNotEmpty(address.getSurfix())) {
				errList.add("“门牌号”为空，“类型、子号、后缀”不能存在内容；");
			}
		}
		if (errList.size()>0) {
			String errStr = org.apache.commons.lang.StringUtils.join(errList, ",");
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),errStr);
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
