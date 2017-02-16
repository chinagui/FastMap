package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;

/**
 * @ClassName FM14Sum060301HM
 * @author Han Shaoming
 * @date 2017年2月9日 下午2:30:50
 * @Description TODO
 * 检查条件：Lifecycle为“0（无）\1（删除）\4（验证）”不检查；
 * 检查原则：
 * 地址（address）中存在错别字配置表中的设施，并提示出错别字与正确字。
 * 当查询的错别字在SC_POINT_ADMINAREA中PROVINCE_SHORT，CITY_SHORT，DISTRICT_SHORT中存在，
 * 且所在的记录在地址中存在时，不报。
 * 充电桩（230227）不参与检查。
 * 备注：SC_POINT_NAMECK中“TYPE”=6且HM_FLAG<>’D’时是地址错别字
 */
public class FM14Sum060301HM extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//充电桩（230227）不参与检查
			String kindCode = poi.getKindCode();
			if(kindCode == null || "230227".equals(kindCode)){return;}
			IxPoiAddress ixPoiAddress=poiObj.getCHAddress();
			if(ixPoiAddress == null){return;}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			String fullname = ixPoiAddress.getFullname();
			if(fullname == null){return;}
			//地址（address）中存在错别字配置表中的设施，并提示出错别字与正确字
			Map<String, String> typeHM6 = metadataApi.scPointNameckTypeHM6();
			Map<String, String> keyResult6=ScPointNameckUtil.matchType(fullname, typeHM6);
			List<String> errorList = new ArrayList<String>();
			List<String> rightList = new ArrayList<String>();
			for(String preKey:keyResult6.keySet()){
				//当查询的错别字在SC_POINT_ADMINAREA中PROVINCE_SHORT，CITY_SHORT，DISTRICT_SHORT中存在，且所在的记录在地址中存在时，不报
				boolean flag = false;
				List<String> adminAreas = metadataApi.searchByErrorName(preKey);
				if(adminAreas != null && !adminAreas.isEmpty()){
					for (String str : adminAreas) {
						if(!fullname.contains(str)){
							flag = true;
						}
					}
				}
				if(adminAreas == null || adminAreas.isEmpty() || flag){
					errorList.add(preKey);
					rightList.add(keyResult6.get(preKey));
				}
			}
			if(errorList.size()>0){
				String log="地址中存在错别字为“"+StringUtils.join(errorList, ",")+
						"”,对应的正确字为“"+StringUtils.join(rightList, ",")+"”,请确认正确性";
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),log);
			}
		}

	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
