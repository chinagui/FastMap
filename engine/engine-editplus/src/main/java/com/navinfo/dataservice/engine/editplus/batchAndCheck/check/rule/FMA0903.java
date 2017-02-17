package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

/**
 * 
 * 检查条件： 以下条件其中之一满足时，需要进行检查： (1)存在官方中文地址IX_POI_ADDRESS新增且FULLNAME不为空；
 * (2)存在官方中文地址IX_POI_ADDRESS修改且FULLNAME不为空； 检查原则：
 * “PROVINCE（省名)”、“CITY（市名）”、“COUNTY（区县名)”字段中的内容，到“地址拆分用行政区划对照表（
 * SC_POINT_ADDR_ADMIN）”中的“ADMIN_NAME”中查找，若找到的“行政区划等级”内容与行政等级对照表一致，为正确，不报出；
 * 若查找不到或找到的结果与行政等级对照表不一致的POI，全部报出。 见《备注》中行政等级对照表 提示：地址行政区域字段检查： ① 若省名、市名、区县名在
 * “地址拆分用行政区划对照表”中找不到，则报：省名、市名、区县名在 “地址拆分用行政区划对照表”中找不到； ② 若省名、市名、区县名在
 * “地址拆分用行政区划对照表”中能找到，但与行政区划等级不一致，则将配置表中的行政区划号（ADMIN_ID）和行政区划名称名称（ADMIN_NAME）
 * 报出来；
 *
 */
public class FMA0903 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj=(IxPoiObj) obj;
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		//存在IxPoiAddress新增或者修改履历
		IxPoiAddress address=poiObj.getCHAddress();
		if (address == null) {
			return;
		}
		if(!address.getHisOpType().equals(OperationType.INSERT)&&!address.getHisOpType().equals(OperationType.UPDATE)){
			return;
		}
		if (address.getFullname() == null || address.getFullname().isEmpty()) {
			return;
		}
		String province = address.getProvince();
		String city = address.getCity();
		String county = address.getCounty();
		MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		Map<String, Map<String,String>> addrAdminMap = metadataApi.getAddrAdminMap();
		List<String> errList = new ArrayList<String>();
		if (StringUtils.isEmpty(province)) {
			if (addrAdminMap.containsKey(province)) {
				if (!addrAdminMap.get(province).get("adminLevel").equals("1")) {
					errList.add(addrAdminMap.get(province).get("adminId")+":"+province);
				}
			} else {
				errList.add("省名、市名、区县名在 “地址拆分用行政区划对照表”中找不到；");
			}
		}
		
		if (StringUtils.isEmpty(city)) {
			if (addrAdminMap.containsKey(city)) {
				if (!addrAdminMap.get(city).get("adminLevel").equals("2")) {
					errList.add(addrAdminMap.get(city).get("admin_id")+":"+city);
				}
			} else {
				errList.add("省名、市名、区县名在 “地址拆分用行政区划对照表”中找不到；");
			}
		}
		
		if (StringUtils.isEmpty(county)) {
			if (addrAdminMap.containsKey(county)) {
				if (!addrAdminMap.get(county).get("adminLevel").equals("3")) {
					errList.add(addrAdminMap.get(county).get("admin_id")+":"+county);
				}
			} else {
				errList.add("省名、市名、区县名在 “地址拆分用行政区划对照表”中找不到；");
			}
		}
		
		if (errList.size()>0) {
			String errStr = org.apache.commons.lang.StringUtils.join(errList, ";");
			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),errStr);
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
