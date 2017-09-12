package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.ScPointNameckUtil;
/**
 * FM-D01-09   名称统一  官方标准中文名称错别字检查   D	
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * 官方标准中文名称如果包含SC_POINT_NAMECK中“TYPE”=4且HM_FLAG<>’HM’对应的pre_key的值，则存在错别字，报log：名称中**是错别字，正确应为**。
 * (SC_POINT_NAMECK中“TYPE”=4且HM_FLAG<>’HM’对应的pre_key为错别字，result_key为正确字)
 * 标准化中文名称（name_type=1，name_class=1，langCode=CHI）
 * @author gaopengrong
 *
 */
public class FMD0109 extends BasicCheckRule {
	private MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
	Map<String, String> typeD4 = new HashMap<String, String>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			List<IxPoiName> names = poiObj.getIxPoiNames();
			for(IxPoiName nameTmp:names){
				if(nameTmp.getLangCode().equals("CHI")&&nameTmp.getNameType()==1
						&&nameTmp.getNameClass()==1){
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
						return;
					}
				}
			}
			
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		typeD4 = metadataApi.scPointNameckTypeD4();
	}

}
