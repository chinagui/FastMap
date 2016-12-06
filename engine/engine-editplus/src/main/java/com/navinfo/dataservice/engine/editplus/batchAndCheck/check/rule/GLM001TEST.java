package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

public class GLM001TEST extends BasicCheckRule {
	Map<Long, Long> parentIds=new HashMap<Long, Long>();

	public GLM001TEST() {
	}

	@Override
	public void runCheck(BasicObj obj) throws Exception{
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			//if(!poi.hisOldValueContains(IxPoi.KIND_CODE)){return;}
			//String oldKindCode=(String) poi.getHisOldValue(IxPoi.KIND_CODE);
			//if(oldKindCode.isEmpty()){return;}
			List<IxPoiName> subRows=poiObj.getIxPoiNames();
			Long parentId=parentIds.get(poiObj.objPid());
			BasicObj parentObj=myReferDataMap.get(ObjectName.IX_POI).get(parentId);
			if(parentObj!=null){
				IxPoiObj ixPoiParentObj=(IxPoiObj) parentObj;}
			for(BasicRow br:subRows){
				if(br.getHisOpType().equals(OperationType.UPDATE)){
					//增加方法，传入IxPoiObj对象
					this.setCheckResult(poi.getGeometry(), "[IX_POI,"+poi.getPid()+"]", poi.getMeshId(),"kindcode错误");
				}
			}
		}else if(obj.objName().equals(ObjectName.AD_LINK)){}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		parentIds = IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		referSubrow.add("IX_POI_GASSTATION");
		//要修改父信息，所以此处isLock=true
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(parentIds.values(), ObjectName.IX_POI, referSubrow, true);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

}
