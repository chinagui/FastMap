package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;
import com.sun.tools.javac.jvm.Code.Chain;
/**
 * GLM60138
 * 检查对象：IX_POI表中“STATE(状态)”非“1（删除）”的记录；
 * 检查原则：IX_POI中，种别（kind_code）为“150101”且官方原始中文名中包含
 * “品牌关键字与Chain值对照表SC_POINT_CHAIN_BRAND_KEY”
 * 中HM_FLAG”=“D”对应的关键字“pre_key”，但POI的Chain值不等于配置表对应的Chain值，报出
 * @author zhangxiaoyi
 */
public class GLM60138 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(kind==null||kind.isEmpty()){return;}
			if(!kind.equals("150101")){return;}
			IxPoiName officeName = poiObj.getOfficeOriginCHName();
			if(officeName==null){return;}
			String nameStr=officeName.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			String chain=poi.getChain();
			MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, String> brandDMap = api.scPointChainBrandKeyDMap();
			for(String key:brandDMap.keySet()){
				if(nameStr.contains(key)){
					if(chain==null||!chain.equals(brandDMap.get(key))){
						setCheckResult(poi.getGeometry(),poiObj, poi.getMeshId(), "品牌关键字："+key+"，chain值："+brandDMap.get(key));
					}
					return;
				}
			}
		}
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
