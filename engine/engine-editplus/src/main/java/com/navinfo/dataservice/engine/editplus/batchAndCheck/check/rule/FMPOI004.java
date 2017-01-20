package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import oracle.net.aso.k;

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
 * FM_POI_004
 * 检查对象：Lifecycle为“1（删除）”不检查；
 * 检查原则：
 * 1）外业采集的CHAIN值：CHAIN值在配置表SC_POINT_CHAIN_CODE中TYPE=1
 * 2）根据POI的CHAIN到配置表SC_POINT_BRAND_FOODTYPE（品牌分类与FOODTYPE对照表）中查找匹配的记录，
 * 找到多条记录，且POI的分类（KIND_CODE）和配置表SC_POINT_BRAND_FOODTYPE中CHAIN对应分类都不一致，报log:POI分类不在品牌对应的分类中，请确认。
 * @author zhangxiaoyi
 */
public class FMPOI004 extends BasicCheckRule {
	
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
			Map<String, List<String>> chainKindMap = api.scPointBrandFoodtypeChainKindMap();
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
