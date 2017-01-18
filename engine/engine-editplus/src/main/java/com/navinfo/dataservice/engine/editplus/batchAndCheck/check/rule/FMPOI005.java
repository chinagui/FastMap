package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * FM_POI_005
 * 检查对象：IX_POI、SC_POINT_KIND_NEW（TYPE值为8的）
 * 检查原则：
 * 1）外业采集的CHAIN值：CHAIN值在配置表SC_POINT_CHAIN_CODE中TYPE=1
 * 2）IX_POI表中的一条记录chain字段与SC_POINT_KIND_NEW表中n（n>0）条记录的R_KIND字段相同,
 * 但该POI的kind_code分类在n条记录的POIKIND中找不到对应值的,报LOG
 * @author zhangxiaoyi
 */
public class FMPOI005 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String chain=poi.getChain();
			if(chain==null||chain.isEmpty()){return;}
			String kind=poi.getKindCode();
			if(kind==null||kind.isEmpty()){return;}			
			MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<String> chainList = api.scPointChainCodeList();
			if(!chainList.contains(chain)){return;}
			Map<String, List<String>> chainKindMap = api.scPointKindNewChainKind8Map();
			if(!chainKindMap.containsKey(chain)){return;}
			if(!chainKindMap.get(chain).contains(kind)){
				setCheckResult(poi.getGeometry(),poiObj, poi.getMeshId(), null);
				return;
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
