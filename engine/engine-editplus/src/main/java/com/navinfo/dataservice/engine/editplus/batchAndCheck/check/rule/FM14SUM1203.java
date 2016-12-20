package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 * FM-14Sum-12-03	名称错别字检查	D	
 * 检查条件：
 * 该POI发生变更(新增或修改主子表、删除子表)
 * 检查原则：
 * 名称中存在错别字配置表中的设施，提示出错别字与正确文字。
 * 备注：SC_POINT_NAMECK中“TYPE”=4且HM_FLAG<>’HM’时，是名称错别字
 * 检查名称：标准化中文名称（NAME_TYPE=1，class={1,5}，LANG_CODE=CHI）
 * 名称错别字：“xxxx”应为“xxxx”
 * @author zhangxiaoyi
 *
 */
public class FM14SUM1203 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			for(IxPoiName nameTmp:names){
				if(nameTmp.getLangCode().equals("CHI")&&nameTmp.getNameType()==1
						&&(nameTmp.getNameClass()==1 ||nameTmp.getNameClass()==5)){
					MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
					Map<String, String> typeD4 = metadataApi.scPointNameckTypeD4();
					Map<String, String> result = ScPointNameckUtil.matchType(nameTmp.getName(), typeD4);
					if(result!=null&&result.size()>0){
						//String log="名称错别字：“xxxx”应为“xxxx”";
						String log="";
						for(String key:result.keySet()){
							if(!log.isEmpty()){log+=",";}
							log+="“"+key+"”应为“"+result.get(key)+"”";
						}
						log="名称错别字："+log;
						setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), log);
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
