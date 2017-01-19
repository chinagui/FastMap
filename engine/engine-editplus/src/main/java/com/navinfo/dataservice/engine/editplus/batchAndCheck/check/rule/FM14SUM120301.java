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
 * FM-14Sum-12-03-01	名称错别字检查	D	
 * 检查条件：
 * Lifecycle为“0（无）\1（删除）\4（验证）”不检查；
 * 检查原则：
 * 名称（name）中存在错别字配置表中的设施，提示出错别字与正确文字。
 * 备注：SC_POINT_NAMECK中“TYPE”=4且HM_FLAG<>’HM’时，是名称错别字
 * @author zhangxiaoyi
 *
 */
public class FM14SUM120301 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			IxPoiName nameObj = poiObj.getOfficeOriginCHName();
			if(nameObj==null){return;}
			String nameStr = nameObj.getName();
			if(nameStr==null||nameStr.isEmpty()){return;}
			
			MetadataApi metadataApi=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			Map<String, String> typeD4 = metadataApi.scPointNameckTypeD4();
			Map<String, String> result = ScPointNameckUtil.matchType(nameStr, typeD4);
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

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}

}
