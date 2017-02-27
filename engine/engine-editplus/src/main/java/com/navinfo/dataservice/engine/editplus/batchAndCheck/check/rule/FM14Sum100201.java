package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName FM14Sum100201
 * @author Han Shaoming
 * @date 2017年2月24日 下午7:36:17
 * @Description TODO
 * 检查条件：非删除POI对象；
 * 检查原则：
 * 官方原始中文名称包含指定的关键字，分类却不是配置表中给定的，给出正确分类与关键字内，报log：指定关键字不是指定分类！
 * 备注：SC_POINT_KIND_RULE表中type值为{1,2,3}
 * 1 关键字必须在中间出现；2 以关键字结尾；3 以关键字开头
 */
public class FM14Sum100201 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode==null){return;}
			IxPoiName ixPoiName = poiObj.getOfficeOriginCHName();
			if(ixPoiName==null){return;}
			String name = ixPoiName.getName();
			if(name == null){return;}
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<Map<String, Object>> scPointKindRules = metadataApi.scPointKindRule();
			for (Map<String, Object> map : scPointKindRules) {
				String poiKind = (String) map.get("poiKind");
				String poiKindName = (String) map.get("poiKindName");
				int type = (int) map.get("type");
				//1 关键字必须在中间出现
				if(type == 1){
					if(!name.startsWith(poiKindName)&&!name.endsWith(poiKindName)&&name.contains(poiKindName)&&!kindCode.equals(poiKind)){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
						return;
					}
				}
				//2 以关键字结尾
				if(type == 1){
					if(name.endsWith(poiKindName)&&!kindCode.equals(poiKind)){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
						return;
					}
				}
				//3 以关键字开头
				if(type == 1){
					if(name.startsWith(poiKindName)&&!kindCode.equals(poiKind)){
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
						return;
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
