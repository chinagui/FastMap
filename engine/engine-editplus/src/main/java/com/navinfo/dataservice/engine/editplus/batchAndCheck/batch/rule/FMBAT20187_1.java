package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiChargingplot;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/** 
 * descp:
 * 查询条件：
 *(1)如果新增充电桩(IX_POI_CHARGINGPLOT)且为子；
 *(2)修改充电桩且修改内容为PLUG_TYPE（插头类型）、ACDC（交直流充电）、MODE（充电模式），OPEN_TYPE（开放状态）其中任何一个字段，且该POI为子：
 *(3)修改充电桩且修改父子关系(新增父子关系或解除父子关系)；
 *(4)修改子充电桩且新增或删除一组桩信息；
 *(5)子充电桩POI删除(主要用于兄弟批处理)；
 *满足(1)或(2)或(3)或(4)或(5),则进行如下批处理：
 *定义：桩POI的PLUG_TYPE（插头类型）、ACDC（交直流充电）、MODE（充电模式），OPEN_TYPE（开放状态）与其它兄弟POI的PLUG_TYPE（插头类型）、ACDC（交直流充电）、MODE（充电模式）一致，则定义为同一组规格相同的充电桩；
 *批处理：
 *查询充电桩对应的充电站，根据充电站查询所有的子充电桩，按照同规格充电桩分组，进行如下批处理赋值：
 *GROUPID赋值原则：
 *有多组不同规格的充电桩时，GROUP_ID的值从1开始顺序递增；
 *COUNT赋值原则：
 *计算同一组相同规格的充电桩（即GROUPID相同）的总数量，同一组桩中分别赋值给IX_POI_CHARGINGPLOT.COUNT
 *注：一个桩可以有多条记录，每个桩记录的group_id和count都需要赋值；
* @ClassName: FMBAT20187_1 
* @author: zhangpengpeng 
* @date: 2017年4月26日
* @Desc: FMBAT20187_1.java
*/
public class FMBAT20187_1 extends BasicBatchRule{
	
	private Map<Long, List<Long>>  childrenMap = new HashMap<Long, List<Long>>();
	private Map<Long,Long> pidParentPidMap = new HashMap<Long,Long>();
	
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		if (pidList!=null&&pidList.size()>0){
			pidParentPidMap = IxPoiSelector.getParentPidByPids(getBatchRuleCommand().getConn(), pidList);
		}
		Set<Long> parentPidList = new HashSet<Long>();
		if(pidParentPidMap != null && pidParentPidMap.size()>0){
			for(Long pid: pidParentPidMap.keySet()){
				parentPidList.add(pidParentPidMap.get(pid));
			}
		}
		
		childrenMap = IxPoiSelector.getChildrenPidsByParentPidList(getBatchRuleCommand().getConn(), parentPidList);
		
		Set<Long> childPids = new HashSet<Long>();
		if (childrenMap != null && childrenMap.size()>0){
			for (Long parentPid:childrenMap.keySet()) {
				childPids.addAll(childrenMap.get(parentPid));
			}
		}
		Set<String> referSubrow =  new HashSet<String>();
		referSubrow.add("IX_POI_CHARGINGPLOT");
		//要修改子信息，所以此处isLock=true
		//由于提交已加锁，若批处理锁的childPids,和提交pidList有交集，就会发生死锁，jch update by 20170904
		pidList.retainAll(childPids);
		childPids.removeAll(pidList);
		//pidList为外层已经加锁的pid
		Map<Long, BasicObj> referObjs = getBatchRuleCommand().loadReferObjs(pidList, ObjectName.IX_POI, referSubrow, false);
		//childPids为还未加锁的pid
		Map<Long, BasicObj> referObjsSecond = getBatchRuleCommand().loadReferObjs(childPids, ObjectName.IX_POI, referSubrow, true);
		referObjs.putAll(referObjsSecond);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}
	@Override
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) obj.getMainrow();
		if (!"230227".equals(poi.getKindCode()) || !(pidParentPidMap.containsKey(poi.getPid()))){
			return;
		}
		// 条件1，2，3，4都不满足，则不批
		if (!ConditionOne(poi) && !ConditionTwo(poiObj) && !ConditionThree(poiObj) && !ConditionFour(poiObj)){
			return ;
		}
		List<Long> childrenList = new ArrayList<Long>();
		if(pidParentPidMap.containsKey(poi.getPid())){
			childrenList = childrenMap.get(pidParentPidMap.get(poi.getPid()));
		}
		List<IxPoiChargingplot> plotList = new ArrayList<IxPoiChargingplot>();
		for (Long childPid:childrenList) {
			BasicObj childObj=myReferDataMap.get(ObjectName.IX_POI).get(childPid);
			IxPoiObj child = (IxPoiObj) childObj;
			plotList.addAll(child.getIxPoiChargingplots());
		}
		FMBAT20187.detealNewData(plotList);
		
	}
	
	public boolean ConditionOne(IxPoi poi){
		//该poi状态为新增或者删除
		if (poi.getHisOpType().equals(OperationType.INSERT)||poi.getHisOpType().equals(OperationType.PRE_DELETED)){
			return true;
		}
		return false;
	}
	public boolean ConditionTwo(IxPoiObj poiObj){
		//该poi是修改，且存在IX_POI_CHARGINGPLOT记录修改，且修改内容为PLUG_TYPE（插头类型）、ACDC（交直流充电）、MODE（充电模式），OPEN_TYPE（开放状态）其中任何一个字段的履历
		if(poiObj.hisOldValueContains("IX_POI_CHARGINGPLOT",IxPoiChargingplot.PLUG_TYPE)||
				poiObj.hisOldValueContains("IX_POI_CHARGINGPLOT",IxPoiChargingplot.ACDC)||
				poiObj.hisOldValueContains("IX_POI_CHARGINGPLOT",IxPoiChargingplot.MODE)||
				poiObj.hisOldValueContains("IX_POI_CHARGINGPLOT",IxPoiChargingplot.OPEN_TYPE)){
			return true;
		}
		return false;
	}
	public boolean ConditionThree(IxPoiObj poiObj) throws Exception{
		//该poi是修改，且存在修改父子关系的履历
		try{
			List<Long> poiPids=new ArrayList<Long>();
			poiPids.add(poiObj.getMainrow().getObjPid());
			Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadByTBRowIDEditStatus(getBatchRuleCommand().getConn(), poiPids);
			for(List<LogDetail> value:logs.values()){
				for(LogDetail LogDetail:value){
					if(LogDetail.getTbNm().equals("IX_POI_CHILDREN")){
						return true;
					}
				}
			}
		}catch (Exception e) {
			throw e;
		}
		
		return false;
	}
	public boolean ConditionFour(IxPoiObj poiObj){
		//存在IX_POI_CHARGINGPLOT记录的新增或删除的履历
		List<IxPoiChargingplot>  chargePolts = poiObj.getIxPoiChargingplots();
		for (IxPoiChargingplot plot: chargePolts){
			if (plot.getHisOpType().equals(OperationType.INSERT) || plot.getHisOpType().equals(OperationType.DELETE)){
				return true;
			}
		}
		return false;
	}


}
