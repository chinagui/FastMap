package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

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

	private Map<Long, Long> samePoiMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null || "180400".equals(kindCode)){return;}
			//是否有同一关系
			if(!samePoiMap.containsKey(poi.getPid())){return;}
			//存在同一关系且IX_SAMEPOI.RELATION_TYPE=1
			List<Long> samePoiGroupIds = CheckUtil.getSamePoiGroupIds(poi.getPid(), 1, this.getCheckRuleCommand().getConn());
			if(samePoiGroupIds == null ||samePoiGroupIds.isEmpty()){return;}
			Long parentId=samePoiMap.get(poi.getPid());
			BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentId);
			IxPoiObj parentPoiObj = (IxPoiObj) parentObj;
			IxPoi parentPoi = (IxPoi) parentPoiObj.getMainrow();
			String kindCodeP = parentPoi.getKindCode();
			if(kindCodeP == null || "180400".equals(kindCode)){return;}
			//SC_POINT_KIND_NEW表的TYPE=5
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
			if((!keys.contains(kindCode)&&!values.contains(kindCode))
					||(!keys.contains(kindCodeP)&&!values.contains(kindCodeP))){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), kindCode+"与"+kindCodeP+"分类之间不可制作同一关系");
				return;
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		samePoiMap = IxPoiSelector.getSamePoiPidsByThisPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(samePoiMap.values(), ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

}
