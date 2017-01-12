package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiParent;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
/**
 * FM-A07-12	机场及机场子设施简称制作	DHM	
 * 检查条件：
 *     以下条件中（1）、（2）满足其中之一，（3）必须满足时，进行检查：
 *     (1)存在IX_POI_NAME新增；
 *     (2)存在IX_POI_NAME修改；
 *     (3) 数据必须存在父子关系，且对应的第一级父分类为“230126”；
 *     检查原则：
 *     ① 子的官方标准化中文名称包含第一级父的官方标准化中文名称的记录，将子POI记录报出来；
 *     ② 如果POI作为第一级父子关系中的父，将此父POI记录报出来；
 *     提示：机场及机场子设施简称制作
 * @author zhangxiaoyi
 *
 */
public class FMA0712 extends BasicCheckRule {
	private Map<Long, Long> parentMap=new HashMap<Long, Long>();

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			if(!isCheck(poiObj)){return;}
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			Long pid=poi.getPid();
			//获取一级父pid
			Long parentPid=getFirstParentPid(pid);
			if(parentPid==null){// 如果POI作为第一级父子关系中的父，将此父POI记录报出来；
				if(!poi.getKindCode().equals("230126")){return;}
				List<IxPoiParent> parentObjs = poiObj.getIxPoiParents();
				if(parentObjs!=null&&parentObjs.size()>0){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					return;
				}
			}else{
				//数据必须存在父子关系，且对应的第一级父分类为“230126”
				//获取一级父poi
				Map<Long, BasicObj> poiMap = myReferDataMap.get(ObjectName.IX_POI);
				if(!poiMap.containsKey(parentPid)){return;}
				IxPoiObj parentObj = (IxPoiObj) poiMap.get(parentPid);
				IxPoi parentPoi=(IxPoi) parentObj.getMainrow();
				if(!parentPoi.getKindCode().equals("230126")){return;}
				//子的官方标准化中文名称包含第一级父的官方标准化中文名称的记录
				IxPoiName poiOfficeName=poiObj.getOfficeStandardCHName();
				IxPoiName parentOfficeName=parentObj.getOfficeStandardCHName();
				if(!poiOfficeName.getName().contains(parentOfficeName.getName())){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
					return;
				}
			}
		}
	}
	
	private Long getFirstParentPid(Long pid){
		if(!parentMap.containsKey(pid)){return null;}
		Long parentPid=parentMap.get(pid);
		while(parentMap.containsKey(parentPid)){
			parentPid=parentMap.get(parentPid);
		}
		return parentPid;
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		parentMap = IxPoiSelector.getAllParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		referSubrow.add("IX_POI_NAME");
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(parentMap.values(), ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}
	
	/**
	 * 以下条件其中之一满足时，需要进行检查：
	 * (1)存在IX_POI_NAME新增；
	 * (2)存在IX_POI_NAME修改；
	 * @param poiObj
	 * @return true满足检查条件，false不满足检查条件
	 * @throws Exception 
	 */
	private boolean isCheck(IxPoiObj poiObj) throws Exception{
		IxPoi poi=(IxPoi) poiObj.getMainrow();
		/*if(poi.hisOldValueContains(IxPoi.KIND_CODE)){
			String newKindCode=poi.getKindCode();
			String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			if(!newKindCode.equals(oldKindCode)){
				return true;
			}
		}*/
		//(1)存在IX_POI_NAME的新增；(2)存在IX_POI_NAME的修改；
		List<IxPoiName> names = poiObj.getIxPoiNames();
		for (IxPoiName br:names){
			if(br.getHisOpType().equals(OperationType.INSERT)){return true;}
			if(br.getHisOpType().equals(OperationType.UPDATE) && br.hisOldValueContains(IxPoiName.NAME)){
				String oldName=(String) br.getHisOldValue(IxPoiName.NAME);
				String newName=br.getName();
				if(!newName.equals(oldName)){
					return true;
				}}
		}
		return false;
	}

}
