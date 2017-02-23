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
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName GLM60291
 * @author Han Shaoming
 * @date 2017年2月21日 下午6:42:24
 * @Description TODO
 * 检查条件： 非删除POI对象；
 * 检查原则：
 * 同一关系中关系类型为“多分类同属性(多义性)”（RELATION_TYPE=1）的一组POI（如：KIND=1或KIND=2），
 * 其两方分类必须在SC_POINT_KIND_NEW中TYPE=5的POIKIND和R_KIND中存在，即：KIND=1或KIND=2必须存在于任意同一行的一组
 * POIKIND或R_KIND中,如果不存在，则报Log：XXXX与XXXX分类之间不可制作同一关系
 */
public class GLM60291 extends BasicCheckRule {

	private Map<Long, Long> samePoiMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null ){return;}
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
			if(kindCodeP == null ){return;}
			//SC_POINT_KIND_NEW表的TYPE=5
			MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
			List<Map<String, String>> scPointKindNew5List = metadataApi.scPointKindNewChainKind5Map();
			boolean check = true;
			for (Map<String, String> scPointKindNew5Map : scPointKindNew5List) {
				for(Map.Entry<String, String> entry : scPointKindNew5Map.entrySet()){
					String key = entry.getKey();
					String value = entry.getValue();
					if((kindCode.equals(key)&&kindCodeP.equals(value))||(kindCode.equals(value)&&kindCodeP.equals(key))){
						check = false;
					}
				}
			}
			if(check){
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
