package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * 检查对象：
 * 非删除POI对象
 * 检查原则：
 * 地址中“TOWN”、“PLACE”、“STREET”、“ESTAB”、“ADDONS”字段，与此POI所在的前三级（省市区）行政区划名称一致时，程序报log：四级地址中存在前三级地址！
 * 前三级（省市区）行政区划名称筛选：ADMIN_TYPE是“1.0”,“2.0”,“2.5”,“3.0”,“3.5”,“4.0”,“4.5”,“4.8”，
 * 并且名称（ad_admin_name.name)中文简体或中文繁体(ad_admin_name.lang_code=CHI或CHT)
 *
 */
public class GLM60377 extends BasicCheckRule {
	
//	@Override
//	public void runCheck(BasicObj obj) throws Exception {
//		Connection conn = null;
//		try{
//			if(obj.objName().equals(ObjectName.IX_POI)){
//				IxPoiObj poiObj=(IxPoiObj) obj;
//				IxPoi poi=(IxPoi) poiObj.getMainrow();
//				List<IxPoiAddress> addrs = poiObj.getIxPoiAddresses();
//				if(addrs.size()==0){return;}
//				long regionId = poi.getRegionId();
//				int region = (int)regionId;
//				conn = (Connection) getCheckRuleCommand().getConn();
//				for(IxPoiAddress addr:addrs){
//					if(addr.getLangCode().equals("CHI")||addr.getLangCode().equals("CHT")){
//						String town = addr.getTown();
//						if(!(town==null||town.isEmpty())){
//							if(CheckUtil.matchAdminName(town,region,conn)){
//								setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址"+town);
//							}
//						}
//						String place = addr.getPlace();
//						if(!(place==null||place.isEmpty())){
//							if(CheckUtil.matchAdminName(place,region,conn)){
//								setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址"+place);
//							}
//						}
//						String street = addr.getStreet();
//						if(!(street==null||street.isEmpty())){
//							if(CheckUtil.matchAdminName(street,region,conn)){
//								setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址"+street);
//							}
//						}
//						String estab = addr.getEstab();
//						if(!(estab==null||estab.isEmpty())){
//							if(CheckUtil.matchAdminName(estab,region,conn)){
//								setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址:"+estab);
//							}
//						}
//						String addons = addr.getAddons();
//						if(!(addons==null||addons.isEmpty())){
//							if(CheckUtil.matchAdminName(addons,region,conn)){
//								setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址"+addons);
//							}
//						}
//					}
//				}
//			}
//		}catch(Exception e){
//			log.error(e.getMessage(),e);
//			throw e;
//		}
//		
//	}
	
	public void run() throws Exception {
		Map<Long, BasicObj> rows = getRowList();
		Set<Long> regions = new HashSet<Long>();
		for (BasicObj obj: rows.values()){
			IxPoi poi = (IxPoi) obj.getMainrow();
			regions.add(poi.getRegionId());
		}
		Map<Long, String> regionAndNameMap = CheckUtil.queryRegionIdAndName(getCheckRuleCommand().getConn(), regions);
		
		for (Map.Entry<Long, BasicObj> entry : getRowList().entrySet()) {
			BasicObj basicObj = entry.getValue();

			if (!basicObj.objName().equals(ObjectName.IX_POI))
				continue;

			IxPoiObj poiObj = (IxPoiObj) basicObj;
			IxPoi poi = (IxPoi) poiObj.getMainrow();

			List<IxPoiAddress> addrs = poiObj.getIxPoiAddresses();
			if(addrs.size() == 0)
				continue;
			long regionId = poi.getRegionId();
			
			for(IxPoiAddress addr:addrs){
				if(addr.getLangCode().equals("CHI") || addr.getLangCode().equals("CHT")){
					String town = addr.getTown();
					if(town != null && StringUtils.isNotEmpty(town)){
						if(regionAndNameMap.containsKey(regionId) && town.equals(regionAndNameMap.get(regionId))){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址"+town);
						}
					}
					String place = addr.getPlace();
					if(place != null && StringUtils.isNotEmpty(place)){
						if(regionAndNameMap.containsKey(regionId) && place.equals(regionAndNameMap.get(regionId))){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址"+place);
						}
					}
					String street = addr.getStreet();
					if(street != null && StringUtils.isNotEmpty(street)){
						if(regionAndNameMap.containsKey(regionId) && street.equals(regionAndNameMap.get(regionId))){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址"+street);
						}
					}
					String estab = addr.getEstab();
					if(estab != null && StringUtils.isNotEmpty(estab)){
						if(regionAndNameMap.containsKey(regionId) && estab.equals(regionAndNameMap.get(regionId))){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址:"+estab);
						}
					}
					String addons = addr.getAddons();
					if(addons != null && StringUtils.isNotEmpty(addons)){
						if(regionAndNameMap.containsKey(regionId) && addons.equals(regionAndNameMap.get(regionId))){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址"+addons);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> checkDataList) throws Exception {
	}

}
