package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxSamepoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxSamepoiPart;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxSamePoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

/**
 * @ClassName GLM60290
 * @author Han Shaoming
 * @date 2017年2月21日 下午3:56:02
 * @Description TODO
 * 检查条件：  非删除POI对象
 * 检查原则：
 * 对于同一关系中多分类同属性(多义性)(IX_SAMEPOI.Relation_type=1)，且是同一组数据，
 * 如果分类(只有有一个分类)在（SC_POINT_KIND_NEW表的TYPE=5）记录中的POIKIND或R_KIND列表中不存在，这组数据中不包含180400分类的记录，
 * 报出Log：XXXX与XXXX分类之间不可制作同一关系
 */
public class GLM60290 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_SAMEPOI)){
			IxSamePoiObj poiObj=(IxSamePoiObj) obj;
			IxSamepoi poi=(IxSamepoi) poiObj.getMainrow();
			if(poi.getRelationType()!=1){return;}
			List<IxSamepoiPart> parts = poiObj.getIxSamepoiParts();
			if(parts==null||parts.size()<2){return;}
			
			//180400分类,不判断
			BasicObj obj1 = myReferDataMap.get(ObjectName.IX_POI).get(parts.get(0).getPoiPid());
			IxPoi poi1 = (IxPoi) obj1.getMainrow();
			String kind1=poi1.getKindCode();
			if(kind1 == null || "180400".equals(kind1)){return;}
			
			BasicObj obj2 = myReferDataMap.get(ObjectName.IX_POI).get(parts.get(1).getPoiPid());
			IxPoi poi2 = (IxPoi) obj2.getMainrow();
			String kind2=poi2.getKindCode();
			if(kind2 == null || "180400".equals(kind2)){return;}
			
			//判断kind是否满足元数据表配置
			MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<Map<String, String>> scPointKindNew5List = metadataApi.scPointKindNewChainKind5Map();
			List<String> keys = new ArrayList<String>();
			List<String> values = new ArrayList<String>();
			for (Map<String, String> scPointKindNew5Map : scPointKindNew5List) {
				for(Map.Entry<String, String> entry : scPointKindNew5Map.entrySet()){
					String key = entry.getKey();
					keys.add(key);
					String value = entry.getValue();
					values.add(value);
				}
			}
			Map<String, String> kindNameByKindCode = metadataApi.getKindNameByKindCode();
			if((!keys.contains(kind1)&&!values.contains(kind2))
					||(!keys.contains(kind2)&&!values.contains(kind1))){
				String targets = "[IX_POI,"+poi1.getPid()+"];[IX_POI,"+poi2.getPid()+"]";
				setCheckResult(poi2.getGeometry(), targets,poi2.getMeshId(), kindNameByKindCode.get(kind1)+"与"+kindNameByKindCode.get(kind2)+"分类之间不可制作同一关系");
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			IxSamePoiObj poiObj=(IxSamePoiObj) obj;
			IxSamepoi poi=(IxSamepoi) poiObj.getMainrow();
			if(poi.getRelationType()!=1){continue;}
			List<IxSamepoiPart> parts = poiObj.getIxSamepoiParts();
			for(IxSamepoiPart tmp:parts){
				pidList.add(tmp.getPoiPid());
			}
		}
		Map<Long, BasicObj> result = getCheckRuleCommand().loadReferObjs(pidList, ObjectName.IX_POI, null, false);
		myReferDataMap.put(ObjectName.IX_POI, result);
	}

}
