package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.util.StringUtils;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FMGLM60138
 * @author Han Shaoming
 * @date 2017年2月24日 上午9:01:30
 * @Description TODO
 * 检查对象，满足以下条件之一：
 * 1）新增POI对象；
 * 2）修改POI且改官方原始中文名称或改分类或改品牌CHAIN；
 * 检查原则：
 * 外业采集的CHAIN值：CHAIN值在配置表SC_POINT_CHAIN_CODE中TYPE=1，
 * 且官方原始中文名中包含“品牌关键字与Chain值对照表(SC_POINT_CHAIN_BRAND_KEY)”中HM_FLAG”包含“D”对应的关键字“pre_key”，
 * 但POI的Chain值不等于配置表对应的Chain值，
 * log描述：名称中含有品牌关键字：XXX，请确认品牌正确性
 */
public class FMGLM60138 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			boolean check = false;
			if(poi.getHisOpType().equals(OperationType.INSERT)){check = true;}
			if(poi.getHisOpType().equals(OperationType.UPDATE)
					&&(poi.hisOldValueContains(IxPoi.KIND_CODE)||poi.hisOldValueContains(IxPoi.CHAIN))){
				check = true;
			}
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
			if(ixPoiName == null){return;}
			String name = ixPoiName.getName();
			if(name==null||name.isEmpty()){return;}
			if(poi.getHisOpType().equals(OperationType.UPDATE)&&(ixPoiName.getHisOpType().equals(OperationType.UPDATE)
					&&ixPoiName.hisOldValueContains(IxPoiName.NAME))){
				check = true;
			}
			if(check){
				String chain=poi.getChain();
				MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				Map<String, String> brandDMap = api.scPointChainBrandKeyDMap();
				//SC_POINT_CHAIN_CODE中TYPE=1
				List<String> chainList = api.scPointChainCodeList();
				if(chainList.contains(chain)){
					for(String key:brandDMap.keySet()){
						if(name.contains(key)){
							if(!StringUtils.equals(chain, brandDMap.get(key))){
								setCheckResult(poi.getGeometry(),poiObj, poi.getMeshId(), "名称中含有品牌关键字："+key+"，请确认品牌正确性");
								return;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}
	
}
