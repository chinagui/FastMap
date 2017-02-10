package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;

/**
 * @ClassName FM14Sum060301
 * @author Han Shaoming
 * @date 2017年2月7日 下午4:08:10
 * @Description TODO
 * 检查条件：Lifecycle为“0（无）\1（删除）\4（验证）”不检查；
 * 检查原则：
 * 地址（address）中存在错别字配置表中的设施，并提示出错别字与正确字。
 * 当查询的错别字在SC_POINT_ADMINAREA中PROVINCE_SHORT，CITY_SHORT，DISTRICT_SHORT中存在，
 * 且所在的记录在地址中存在时，不报。充电桩（230227）不参与检查。
 * 备注：SC_POINT_NAMECK中“TYPE”=6且HM_FLAG<>’HM’时是地址错别字
 */
public class FM14Sum060301 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			IxPoiAddress ixPoiAddress=poiObj.getCHAddress();
			if(ixPoiAddress == null){return;}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			String fullname = ixPoiAddress.getFullname();
			if(fullname == null){return;}
			//地址（address）中存在错别字配置表中的设施，并提示出错别字与正确字
			Map<String, Map<String, String>> typeD6 = metadataApi.scPointNameckTypeD6();
			Map<String,String> checkTypeD6 = new HashMap<String, String>();
			for(Map.Entry<String, Map<String, String>> entry : typeD6.entrySet()){
				checkTypeD6.put(entry.getKey(), entry.getValue().get("resultKey"));
			}
			Map<String, String> keyResult6=ScPointNameckUtil.matchType(fullname, checkTypeD6);
			for(String preKey:keyResult6.keySet()){
				//当查询的错别字在SC_POINT_ADMINAREA中PROVINCE_SHORT，CITY_SHORT，DISTRICT_SHORT中存在，且所在的记录在地址中存在时，不报
				boolean flag = false;
				List<Map<String, Object>> adminAreas = metadataApi.searchByErrorName(preKey);
				if(adminAreas != null && !adminAreas.isEmpty()){
					for (Map<String, Object> map : adminAreas) {
						String adminAreaWhole = (String) map.get("whole");
						if(adminAreaWhole != null && fullname.contains(adminAreaWhole)){
							flag = true;
							break;
						}
					}
				}
				if(flag){continue;}
				if (fullname.contains(preKey)){
					String log="地址中错别字为“"+preKey+"”,正确字为“"+keyResult6.get(preKey)+"”";
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),log);
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
