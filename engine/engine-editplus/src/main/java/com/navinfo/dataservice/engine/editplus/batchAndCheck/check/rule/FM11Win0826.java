package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;

/**
 * @ClassName FM11Win0826
 * @author Han Shaoming
 * @date 2017年2月8日 下午3:50:52
 * @Description TODO
 * 检查条件：    Lifecycle！=1（删除）
 * 检查原则：
 * 父名称修改，子设施地址未修改
 * 子设施为内部POI且其地址（address）不包含父POI的名称（name），
 * 报Log：父名称修改，子设施地址未修改
 */
public class FM11Win0826 extends BasicCheckRule {

	private Map<Long, Long> parentMap=new HashMap<Long, Long>();
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			IxPoiAddress ixPoiAddress=poiObj.getCHAddress();
			if(ixPoiAddress == null){return;}
			//是否有父
			if(!parentMap.containsKey(poi.getPid())){return;}
			Long parentId=parentMap.get(poi.getPid());
			BasicObj parentObj = myReferDataMap.get(ObjectName.IX_POI).get(parentId);
			//父名称修改，子设施地址未修改
			IxPoiName parentNameObj = ((IxPoiObj) parentObj).getOfficeOriginCHName();
			if(parentNameObj.getHisOpType().equals(OperationType.UPDATE)
					&&parentNameObj.hisOldValueContains(IxPoiName.NAME)){
				//子设施地址是否修改
				if(ixPoiAddress.getHisOpType().equals(OperationType.UPDATE)
						&&ixPoiAddress.hisOldValueContains(IxPoiAddress.FULLNAME)){return;}
				//子设施是否为内部POI
				int indoor = poi.getIndoor();
				String fullname = ixPoiAddress.getFullname();
				String name = parentNameObj.getName();
				if(fullname == null || name == null){return;}
				if(indoor == 1 && (!fullname.contains(name))){
					setCheckResult(poi.getGeometry(), poiObj,poi.getMeshId(), null);
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		Set<Long> pidList=new HashSet<Long>();
		for(BasicObj obj:batchDataList){
			pidList.add(obj.objPid());
		}
		parentMap = IxPoiSelector.getParentPidsByChildrenPids(getCheckRuleCommand().getConn(), pidList);
		Set<String> referSubrow=new HashSet<String>();
		referSubrow.add("IX_POI_NAME");
		Map<Long, BasicObj> referObjs = getCheckRuleCommand().loadReferObjs(parentMap.values(), ObjectName.IX_POI, referSubrow, false);
		myReferDataMap.put(ObjectName.IX_POI, referObjs);
	}

}
