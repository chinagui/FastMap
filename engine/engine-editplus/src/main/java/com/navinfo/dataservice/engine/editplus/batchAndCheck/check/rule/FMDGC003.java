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
 * FM_POI_001
 * 检查对象：
 * IX_POI表中“STATE(状态)”非“1（删除）”的记录
 * 检查原则：
 * 1）分类为空，报出；
 * 2）分类不为空，在POI分类配置表（SC_POINT_POICODE_NEW（KIND_USE=1)）中，分类不在“大陆分类”中的报出；
 * 同时满足以下两个条件的是大陆分类：
 * ①POI分类一览表中“大陆港澳分类说明”包含D；
 * @author zhangxiaoyi
 */
public class FMDGC003 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kind=poi.getKindCode();
			if(kind==null||kind.isEmpty()){
				setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"分类为空，请确认");
				return;
			}else{
				MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				List<String> kindDlist = metadataApi.getKindCodeDList();
				Map<String, String> kindNameByKindCode = metadataApi.getKindNameByKindCode();
				if(!kindDlist.contains(kind)){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(),"分类绝对错误：“"+kindNameByKindCode.get(kind)+"”不在分类一览表中（绝对错误）");
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
