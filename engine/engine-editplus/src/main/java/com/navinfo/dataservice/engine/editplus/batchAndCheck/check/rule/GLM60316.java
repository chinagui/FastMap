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
 * @ClassName GLM60316
 * @author Han Shaoming
 * @date 2017年2月21日 下午8:03:41
 * @Description TODO
 * 检查条件： 非删除POI且存在同一关系
 * 检查原则：
 * IX_SAMEPOI.RELATION_TYPE为1的一组POI，且都存在父，则他们对应的父必须存在同一关系，
 * 否则报LOG：制作同一关系的一组POI，它们的父也应该制作同一关系！
 * 注：这组POI对应是同一父，不报LOG
 */
public class GLM60316 extends BasicCheckRule {

	private Map<Long, Long> samePoiMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//是否有同一关系
			if(!samePoiMap.containsKey(poi.getPid())){return;}
			//存在同一关系且IX_SAMEPOI.RELATION_TYPE=1
			List<Long> samePoiGroupIds = CheckUtil.getSamePoiGroupIds(poi.getPid(), 1, this.getCheckRuleCommand().getConn());
			if(samePoiGroupIds == null ||samePoiGroupIds.isEmpty()){return;}
			//查询父
			Set<Long> pidList=new HashSet<Long>();
			long pid = poi.getPid();
			long samePid = samePoiMap.get(pid);
			pidList.add(pid);
			pidList.add(samePid);
			Map<Long, Long> parentMap = IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
			if(!parentMap.containsKey(pid)){return;}
			if(!parentMap.containsKey(samePid)){return;}
			//这组POI对应是同一父，不报LOG
			if(parentMap.get(pid).equals(parentMap.get(samePid))){return;}
			//判断父是否存在同一关系
			Set<Long> samePoiPidList=new HashSet<Long>();
			long pidP = parentMap.get(pid);
			long samePidP = parentMap.get(samePid);
			samePoiPidList.add(pidP);
			Map<Long, Long> samePoiParentMap = IxPoiSelector.getSamePoiPidsByThisPids(getCheckRuleCommand().getConn(), samePoiPidList);
			if(!samePoiParentMap.containsKey(pidP)
					||!samePoiParentMap.get(pidP).equals(samePidP)){
				setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
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
	}

}
