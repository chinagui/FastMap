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
 * @ClassName GLM60292
 * @author Han Shaoming
 * @date 2017年2月21日 下午7:08:35
 * @Description TODO
 * 检查条件： 非删除POI对象；
 * 检查原则：
 * 对于同一关系中多分类同属性(多义性)(IX_SAMEPOI.Relation_type=1)，且是同一组数据，检查数据分类，
 * 在SC_POINT_KIND_NEW中TYPE=6的条件下，一方种别在POIKIND列表中，另一方分类为R_KIND限定分类中，
 * 报出Log;XXXX不是指定分类，无法制作同一关系
 */
public class GLM60292 extends BasicCheckRule {
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_SAMEPOI)){
			IxSamePoiObj poiObj=(IxSamePoiObj) obj;
			IxSamepoi poi=(IxSamepoi) poiObj.getMainrow();
			if(poi.getRelationType()!=1){return;}
			List<IxSamepoiPart> parts = poiObj.getIxSamepoiParts();
			if(parts==null||parts.size()<2){return;}
			
			BasicObj obj1 = myReferDataMap.get(ObjectName.IX_POI).get(parts.get(0).getPoiPid());
			IxPoi poi1 = (IxPoi) obj1.getMainrow();
			String kind1=poi1.getKindCode();
			
			BasicObj obj2 = myReferDataMap.get(ObjectName.IX_POI).get(parts.get(1).getPoiPid());
			IxPoi poi2 = (IxPoi) obj2.getMainrow();
			String kind2=poi2.getKindCode();
			
			//SC_POINT_KIND_NEW表的TYPE=6
			MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<Map<String, String>> scPointKindNew6List = metadataApi.scPointKindNewChainKind6Map();
			List<String> keys = new ArrayList<String>();
			List<String> values = new ArrayList<String>();
			for (Map<String, String> scPointKindNew6Map : scPointKindNew6List) {
				for(Map.Entry<String, String> entry : scPointKindNew6Map.entrySet()){
					String key = entry.getKey();
					keys.add(key);
					String value = entry.getValue();
					values.add(value);
				}
			}
			Map<String, String> kindNameByKindCode = metadataApi.getKindNameByKindCode();
			if((keys.contains(kind1)&&values.contains(kind2))
					||(keys.contains(kind2)&&values.contains(kind1))){
				String targets = "[IX_POI,"+poi1.getPid()+"];[IX_POI,"+poi2.getPid()+"]";
				setCheckResult(poi1.getGeometry(), targets,poi1.getMeshId(), kindNameByKindCode.get(kind1)+"不是指定分类，无法制作同一关系");
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
