package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * 
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * IX_POI_Address表中“PROVINCE（省名)”、“CITY（市名）”、“COUNTY（区县名)”字段的内容若为空则不检查，若非空，则应在“行政区划管理表：SC_POINT_ADMINAREA”与其相对应的行政区划级别中存在；其中：
 * (1)IX_POI_Address表中“PROVINCE(省名)”应该在SC_POINT_ADMINAREA中PROVINCE或PROVINCE_SHORT中存在；
 * (2)IX_POI_Address表中“CITY(市名)”应该在SC_POINT_ADMINAREA中的CITY字段或CITY_SHORT字段中存在，或应该在SC_POINT_ADMINAREA中字段“REMARK”为“1”的DISTRICT或DISTRICT_SHORT中存在;
 * (3)IX_POI_Address表中“COUNTY(区县名)”应该在SC_POINT_ADMINAREA中DISCITY或DISCITY_SHORT中存在;
 * 以上只要有一个不存在，则报log：XX（对应的那个行政等级）不在该POI对应的行政区划中。

 *
 */
public class GLM60181 extends BasicCheckRule {

//	@Override
//	public void runCheck(BasicObj obj) throws Exception {
//		IxPoiObj poiObj=(IxPoiObj) obj;
//		IxPoi poi=(IxPoi) poiObj.getMainrow();
//
//		IxPoiAddress address=poiObj.getCHAddress();
//		if (address == null) {
//			return;
//		}
//
//		String province = address.getProvince();
//		String city = address.getCity();
//		String county = address.getCounty();
//		MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
//		Map<String, List<String>> addrAdminMap = metadataApi.scPointAdminareaDataMap();
//		List<String> errList = new ArrayList<String>();
//		if (StringUtils.isNotEmpty(province)) {
//			if(!(addrAdminMap.get("province").contains(province)||addrAdminMap.get("province_short").contains(province))){
//				errList.add("省名不在该POI对应的行政区划中；");
//			}
//		}
//		
//		if (StringUtils.isNotEmpty(city)) {
//			if(!(addrAdminMap.get("city").contains(city)||addrAdminMap.get("city_short").contains(city)||
//					addrAdminMap.get("district_remark1").contains(city)||addrAdminMap.get("district_short_remark1").contains(city))){
//				errList.add("市名不在该POI对应的行政区划中；");
//			}
//		}
//		
//		if (StringUtils.isNotEmpty(county)) {
//			if(!(addrAdminMap.get("district").contains(county)||addrAdminMap.get("district_short").contains(county))){
//				errList.add("区县名不在该POI对应的行政区划中；");
//			}
//		}
//		
//		if (errList.size()>0) {
//			String errStr = org.apache.commons.lang.StringUtils.join(errList, ";");
//			setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),errStr);
//		}
//
//	}

	private MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");

	public void run() throws Exception {
		Map<String, List<String>> addrAdminMap = metadataApi.scPointAdminareaDataMap();
		
		for (Map.Entry<Long, BasicObj> entry : getRowList().entrySet()) {
			BasicObj basicObj = entry.getValue();

			if (!basicObj.objName().equals(ObjectName.IX_POI)) continue;

			IxPoiObj poiObj = (IxPoiObj) basicObj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();

			IxPoiAddress address = poiObj.getCHAddress();
			
			if (address == null) continue;


			String province = address.getProvince();
			String city = address.getCity();
			String county = address.getCounty();
			List<String> errList = new ArrayList<String>();
			if (StringUtils.isNotEmpty(province)) {
				if(!(addrAdminMap.get("province").contains(province) || addrAdminMap.get("province_short").contains(province))){
					errList.add("省名不在该POI对应的行政区划中；");
				}
			}
			
			if (StringUtils.isNotEmpty(city)) {
				if(!(addrAdminMap.get("city").contains(city) || addrAdminMap.get("city_short").contains(city) || 
						addrAdminMap.get("district_remark1").contains(city) || addrAdminMap.get("district_short_remark1").contains(city))){
					errList.add("市名不在该POI对应的行政区划中；");
				}
			}
			
			if (StringUtils.isNotEmpty(county)) {
				if(!(addrAdminMap.get("district").contains(county) || addrAdminMap.get("district_short").contains(county))){
					errList.add("区县名不在该POI对应的行政区划中；");
				}
			}
			
			if (errList.size() > 0) {
				String errStr = org.apache.commons.lang.StringUtils.join(errList, ";");
				setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), errStr);
			}
		}
	}
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
	}
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
	}

}
