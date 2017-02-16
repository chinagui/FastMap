package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

import net.sf.json.JSONObject;

/**
 * 检查对象：
 * 该POI发生变更(新增或修改主子表、删除子表)；
 * 检查原则：
 * 地址中“TOWN”、“PLACE”、“STREET”、“ESTAB”、“ADDONS”字段，与此POI所在的前三级（省市区）行政区划名称一致时，程序报log；
 * 前三级（省市区）行政区划名称筛选：ADMIN_TYPE等于“1、2、2.5、3、3.5、4、4.5、4.8”，并且名称为官方中文简体或中文繁体
 * log：四级地址中存在前三级地址
 * 备：ix_poi.region_id=ad_admin_name.region_id=ad_admin.region_id;
 * @author gaopengrong
 *
 */
public class FMGLM60377 extends BasicCheckRule {
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		System.out.println();
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiAddress> addrs = poiObj.getIxPoiAddresses();
			if(addrs.size()==0){return;}
			long regionId = poi.getRegionId();
			int region=(int)regionId;
			Connection conn= (Connection) getCheckRuleCommand().getConn();
			for(IxPoiAddress addr:addrs){
				if(addr.getLangCode().equals("CHI")){
					String town = addr.getTown();
					if(!(town==null||town.isEmpty())){
						if(CheckUtil.matchAdminName(town,region,conn)){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址"+town);
						}
					}
					String place = addr.getPlace();
					if(!(place==null||place.isEmpty())){
						if(CheckUtil.matchAdminName(place,region,conn)){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址"+place);
						}
					}
					String street = addr.getStreet();
					if(!(town==null||town.isEmpty())){
						if(CheckUtil.matchAdminName(street,region,conn)){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址"+street);
						}
					}
					String estab = addr.getEstab();
					if(!(estab==null||estab.isEmpty())){
						if(CheckUtil.matchAdminName(estab,region,conn)){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址:"+estab);
						}
					}
					String addons = addr.getAddons();
					if(!(addons==null||addons.isEmpty())){
						if(CheckUtil.matchAdminName(addons,region,conn)){
							setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "四级地址中存在前三级地址"+addons);
						}
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
