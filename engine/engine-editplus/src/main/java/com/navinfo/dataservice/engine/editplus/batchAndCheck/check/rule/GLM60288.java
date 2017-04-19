package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.common.CheckUtil;

/**
 * @ClassName GLM60288
 * @author Han Shaoming
 * @date 2017年2月21日 下午3:00:14
 * @Description TODO
 * 检查条件：非删除POI对象
 * 检查原则：
 * 对于同一关系中多分类同属性(多义性)(IX_SAMEPOI.Relation_type=1)，且是同一组数据，分类相同的，
 * 报出Log：制作同一关系的同一组数据内，分类不能相同
 */
public class GLM60288 extends BasicCheckRule {

	private Map<Long, Long> samePoiMap=new HashMap<Long, Long>();
	private Set<String> filterPid = new HashSet<String>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode = poi.getKindCode();
			if(kindCode == null){return;}
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
			
			if(kindCode.equals(kindCodeP)){
				String targets = "[IX_POI,"+poi.getPid()+"];[IX_POI,"+parentId+"]";
				if(!filterPid.contains(targets)){
					setCheckResult(poi.getGeometry(), targets,poi.getMeshId(), null);
				}
				filterPid.add(targets);
				filterPid.add("[IX_POI,"+parentId+"];[IX_POI,"+poi.getPid()+"]");
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
