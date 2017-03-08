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
 * @ClassName FM-14Sum-12-09-01
 * @author zhangxiaoyi
 * @date 2017年2月8日 下午6:07:27
 * @Description TODO
 * 检查条件：非删除POI对象
 * 检查原则：
 * 官方原始中文名称中是否包含其分类必须包含的关键字，没有包含时，报log：指定分类必须包含指定关键字:
 * 分类:****,正确关键字:[****]！
 * 参考CI_PARA_KIND_KEYWORD
 */
public class FM14Sum120901 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;

			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String name=nameObj.getName();
			if(name==null||name.isEmpty()){return;}
			
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			
			MetadataApi api=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, List<String>> kindMap = api.ciParaKindKeywordMap();
			
			Map<String, String> kindNameByKindCode = api.getKindNameByKindCode();
			if(!kindMap.containsKey(kindCode)){return;}
			List<String> keyWords = kindMap.get(kindCode);
			boolean check = true;
			if(keyWords != null){
				for (String keyWord : keyWords) {
					if(name.contains(keyWord)){
						check = false;
						break;
					}
				}
				if(check){
					setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), "指定分类必须包含指定关键字:分类:"+kindNameByKindCode.get(kindCode)+",正确关键字:"+keyWords.toString());
					return;
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
